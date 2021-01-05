/*
 * Copyright (c) 2002-2020, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.elasticdata.modules.appointment.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import fr.paris.lutece.plugins.appointment.business.appointment.Appointment;
import fr.paris.lutece.plugins.appointment.business.display.Display;
import fr.paris.lutece.plugins.appointment.business.planning.WeekDefinition;
import fr.paris.lutece.plugins.appointment.business.rule.FormRule;
import fr.paris.lutece.plugins.appointment.business.slot.Slot;
import fr.paris.lutece.plugins.appointment.service.DisplayService;
import fr.paris.lutece.plugins.appointment.service.FormRuleService;
import fr.paris.lutece.plugins.appointment.service.SlotService;
import fr.paris.lutece.plugins.appointment.service.WeekDefinitionService;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFormDTO;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.business.AppointmentDataObject;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.business.AppointmentForm;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import fr.paris.lutece.plugins.workflowcore.service.state.StateService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.web.l10n.LocaleService;

/**
 * Utils for the slots (Uid, Url, Item ...)
 * 
 * @author Laurent Payen
 *
 */
public final class AppointmentSlotUtil
{

    public static final String PROPERTY_SITE = "lutece.name";
    public static final DateTimeFormatter SLOT__ID_DATE_FORMATTER = DateTimeFormatter.ofPattern( "yyyyMMdd'T'HHmmss" );
    private final transient static StateService _stateService = SpringContextService.getBean( StateService.BEAN_SERVICE );
    public static final String INSTANCE_NAME = AppPropertiesService.getProperty( AppointmentSlotUtil.PROPERTY_SITE );

    /**
     * Private constructor - this class does not need to be instantiated
     */
    private AppointmentSlotUtil( )
    {
    }

    /**
     * Generate a unique ID for solr.
     *
     * Slots don't have ids anymore, so we use the form_id and the slot date as an ID. We try to make a "readable" id with the form id and the slot datetime,
     * using only alphanumerical caracters to avoid potential problems with code parsing this ID.
     * 
     */
    public static String getSlotUid( Slot slot, String instanceName )
    {
        String strSlotDateFormatted = slot.getStartingDateTime( ).format( SLOT__ID_DATE_FORMATTER );
        return instanceName + "_F" + slot.getIdForm( ) + "D" + strSlotDateFormatted + "_appointment-slot";
    }

    public static String getAppointmentId( int appointmentId, String instanceName )
    {

        return instanceName + "_" + appointmentId;
    }

    /**
     * Get all the slots of a form by calling the method buildListSlot of the plugin RDV
     * 
     * @param appointmentForm
     *            the appointment form
     * @return all the slots of a form
     */
    public static List<Slot> getAllSlots( AppointmentFormDTO appointmentForm, LocalDateTime localDateTime, int nNbPlacesToTake )
    {
        Display display = DisplayService.findDisplayWithFormId( appointmentForm.getIdForm( ) );
        // Get the nb weeks to display
        int nNbWeeksToDisplay = display.getNbWeeksToDisplay( );
        // Calculate the ending date of display with the nb weeks to display
        // since today
        // We calculate the number of weeks including the current week, so it
        // will end to the (n) next sunday
        TemporalField fieldISO = WeekFields.of( LocaleService.getDefault( ) ).dayOfWeek( );
        LocalDate dateOfSunday = localDateTime.toLocalDate( ).with( fieldISO, DayOfWeek.SUNDAY.getValue( ) );
        LocalDate endingDateOfDisplay = dateOfSunday.plusWeeks( nNbWeeksToDisplay - 1 );
        LocalDate endingValidityDate = null;
        if ( appointmentForm.getDateEndValidity( ) != null )
        {
            endingValidityDate = appointmentForm.getDateEndValidity( ).toLocalDate( );
        }
        if ( endingValidityDate != null && endingDateOfDisplay.isAfter( endingValidityDate ) )
        {
            endingDateOfDisplay = endingValidityDate;
        }

        HashMap<LocalDate, WeekDefinition> mapWeekDefinition = WeekDefinitionService.findAllWeekDefinition( appointmentForm.getIdForm( ) );
        LocalDateTime minTimeBeforeAppointment = localDateTime.plusHours( appointmentForm.getMinTimeBeforeAppointment( ) );

        if ( appointmentForm.getIsMultislotAppointment( ) )
        {
            return SlotService.buildListSlot( appointmentForm.getIdForm( ), mapWeekDefinition, minTimeBeforeAppointment.toLocalDate( ),
                    localDateTime.toLocalDate( ), nNbPlacesToTake );
        }
        else
        {
            return SlotService.buildListSlot( appointmentForm.getIdForm( ), mapWeekDefinition, localDateTime.toLocalDate( ), endingDateOfDisplay );
        }

    }

    /**
     * Get all the slots of a form by calling the method buildListSlot of the plugin RDV
     * 
     * @param appointmentForm
     *            the appointment form
     * @return all the slots of a form
     */
    public static List<Slot> getAllSlots( AppointmentFormDTO appointmentForm, LocalDateTime localDateTime )
    {
        Display display = DisplayService.findDisplayWithFormId( appointmentForm.getIdForm( ) );
        // Get the nb weeks to display
        int nNbWeeksToDisplay = display.getNbWeeksToDisplay( );
        // Calculate the ending date of display with the nb weeks to display
        // since today
        // We calculate the number of weeks including the current week, so it
        // will end to the (n) next sunday
        TemporalField fieldISO = WeekFields.of( LocaleService.getDefault( ) ).dayOfWeek( );
        LocalDate dateOfSunday = localDateTime.toLocalDate( ).with( fieldISO, DayOfWeek.SUNDAY.getValue( ) );
        LocalDate endingDateOfDisplay = dateOfSunday.plusWeeks( nNbWeeksToDisplay - 1 );
        LocalDate endingValidityDate = null;
        if ( appointmentForm.getDateEndValidity( ) != null )
        {
            endingValidityDate = appointmentForm.getDateEndValidity( ).toLocalDate( );
        }
        if ( endingValidityDate != null && endingDateOfDisplay.isAfter( endingValidityDate ) )
        {
            endingDateOfDisplay = endingValidityDate;
        }

        return SlotService.buildListSlot( appointmentForm.getIdForm( ), WeekDefinitionService.findAllWeekDefinition( appointmentForm.getIdForm( ) ),
                localDateTime.toLocalDate( ), endingDateOfDisplay );
    }

    /**
     * Get the state of appointment
     * 
     * @param idAppointment
     *            The id appointment
     * @param idWorkflow
     *            The if form
     * @return State of appointment
     */
    public static State getState( int idAppointment, int idWorkflow )
    {

        return _stateService.findByResource( idAppointment, Appointment.APPOINTMENT_RESOURCE_TYPE, idWorkflow );
    }

    /**
     * build query for delete a document into elastic-search
     * 
     * @param idForm
     *            the Id form
     * @return delete query
     */
    public static String buildQuery( int idForm )
    {
        StringBuilder sbuilder = new StringBuilder( );
        sbuilder.append( "{ \"query\": { \"term\": { \"appointmentForm.idForms\":" + idForm + "} }}" );

        return sbuilder.toString( );
    }

    /**
     * 
     * @param apptData
     *            the AppointmentDataObject
     * @param listSlots
     *            The list slots
     * @param localTime
     *            The localTime
     * @param appointmentForm
     *            The appointment form
     * @return AppointmentDataObject builded
     */
    public static AppointmentDataObject buildAppointmentDataObject( AppointmentDataObject apptData, List<Slot> listSlots, List<Slot> listCatagorySlots,
            LocalDateTime localTime, AppointmentForm appointmentForm )
    {

        long lSumNbPlacesBeforeAppointment;
        LocalDateTime startAppointment = LocalDateTime.ofInstant( Instant.ofEpochMilli( Long.parseLong( apptData.getTimestamp( ) ) ),
                TimeZone.getDefault( ).toZoneId( ) );

        FormRule formRule = FormRuleService.findFormRuleWithFormId( appointmentForm.getIdForms( ) );
        int minTimeBeforeAppointment = formRule.getMinTimeBeforeAppointment( );
        LocalDateTime dateTimeBeforeAppointment = LocalDateTime.now( ).plusHours( minTimeBeforeAppointment );

        if ( CollectionUtils.isNotEmpty( listSlots ) )
        {
            listSlots = listSlots.stream( ).filter( s -> s.getStartingDateTime( ).isAfter( dateTimeBeforeAppointment ) ).collect( Collectors.toList( ) );
        }

        List<Slot> listAvailableSlots = listSlots.stream( )
                .filter( s -> s.getStartingDateTime( ).isBefore( startAppointment ) && s.getIsOpen( ) == Boolean.TRUE ).collect( Collectors.toList( ) );

        apptData.setTimeUntilAvailability( getTimeUntilAvailability( listAvailableSlots, localTime, startAppointment ) );

        lSumNbPlacesBeforeAppointment = listAvailableSlots.stream( ).mapToLong( s -> s.getMaxCapacity( ) ).sum( );
        apptData.setSumNbPlacesBeforeAppointment( lSumNbPlacesBeforeAppointment );

        apptData.setAppointmentForm( appointmentForm );
        return apptData;

    }

    /**
     * Get the state of appointment
     * 
     * @param listAvailableSlots
     *            The list of available slots
     * @param idWorkflow
     *            The if form
     * @return State of appointment
     */
    public static long getTimeUntilAvailability( List<Slot> listAvailableSlots, LocalDateTime localTime, LocalDateTime startAppointment )
    {
        long lTimeUntilAvailability = 0;
        LocalDateTime firstDateOfFreeOpenSlot = null;

        listAvailableSlots = listAvailableSlots.stream( ).filter( s -> s.getNbRemainingPlaces( ) > 0 ).collect( Collectors.toList( ) );
        if ( CollectionUtils.isNotEmpty( listAvailableSlots ) )
        {
            // if
            firstDateOfFreeOpenSlot = listAvailableSlots.stream( ).min( ( s1, s2 ) -> s1.getStartingDateTime( ).compareTo( s2.getStartingDateTime( ) ) ).get( )
                    .getStartingDateTime( );
            if ( firstDateOfFreeOpenSlot.isBefore( startAppointment ) )
            {
                lTimeUntilAvailability = Duration.between( localTime, firstDateOfFreeOpenSlot ).toMillis( );
            }
            else
            {
                lTimeUntilAvailability = Duration.between( localTime, startAppointment ).toMillis( );
            }
        }
        else
        {

            lTimeUntilAvailability = Duration.between( localTime, startAppointment ).toMillis( );
        }

        return lTimeUntilAvailability;

    }

    public static int calculateConsecutiveSlots( Slot slot, List<Slot> allSlots )
    {
        if ( slot.getNbPotentialRemainingPlaces( ) == 0 )
        {
            return 0;
        }
        AtomicInteger consecutiveSlots = new AtomicInteger( 1 );
        doCalculateConsecutiveSlots( slot, allSlots, consecutiveSlots );
        return consecutiveSlots.get( );
    }

    private static void doCalculateConsecutiveSlots( Slot slot, List<Slot> allSlots, AtomicInteger consecutiveSlots )
    {
        for ( Slot nextSlot : allSlots )
        {
            if ( Objects.equals( slot.getEndingDateTime( ), nextSlot.getStartingDateTime( ) ) )
            {
                if ( nextSlot.getNbPotentialRemainingPlaces( ) > 0 )
                {
                    consecutiveSlots.addAndGet( 1 );
                    doCalculateConsecutiveSlots( nextSlot, allSlots, consecutiveSlots );
                }
                else
                {
                    break;
                }
            }
        }
    }
}
