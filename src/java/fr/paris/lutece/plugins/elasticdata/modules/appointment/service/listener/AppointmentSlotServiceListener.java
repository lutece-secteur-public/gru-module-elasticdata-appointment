/*
 * Copyright (c) 2002-2022, City of Paris
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
package fr.paris.lutece.plugins.elasticdata.modules.appointment.service.listener;

import java.util.Comparator;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;

import fr.paris.lutece.plugins.appointment.business.planning.WeekDefinition;
import fr.paris.lutece.plugins.appointment.business.rule.ReservationRule;
import fr.paris.lutece.plugins.appointment.service.FormService;
import fr.paris.lutece.plugins.appointment.service.ReservationRuleService;
import fr.paris.lutece.plugins.appointment.service.event.FormEvent;
import fr.paris.lutece.plugins.appointment.service.event.SlotEndingTimeChangedEvent;
import fr.paris.lutece.plugins.appointment.service.event.SlotEvent;
import fr.paris.lutece.plugins.appointment.service.event.WeekDefinitionEvent;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.service.AppointmentSlotDataSource;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.service.IndexingSlotService;
import fr.paris.lutece.portal.service.event.EventAction;
import fr.paris.lutece.portal.service.event.Type;

/**
 * CDI event listener for appointment slot indexing in Elasticsearch
 */
@ApplicationScoped
public class AppointmentSlotServiceListener
{
    @Inject
    private AppointmentSlotDataSource _appointmentSlotDataSource;

    /**
     * Handle week definition list changed event
     *
     * @param event
     *            the week definition event
     */
    public void onListWeeksChanged( @ObservesAsync @Type( EventAction.UPDATE ) WeekDefinitionEvent event )
    {
        List<WeekDefinition> listWeek = event.getListWeekDefinition( );
        if ( listWeek != null && !listWeek.isEmpty( ) )
        {
            WeekDefinition weekWithDateMin = listWeek.stream( ).min( Comparator.comparing( WeekDefinition::getDateOfApply ) ).orElse( null );
            WeekDefinition weekWithDateMax = listWeek.stream( ).max( Comparator.comparing( WeekDefinition::getEndingDateOfApply ) ).orElse( null );

            IndexingSlotService.indexFormByDateRange( _appointmentSlotDataSource,
                    FormService.buildAppointmentFormWithoutReservationRule( event.getIdForm( ) ), weekWithDateMin.getDateOfApply( ),
                    weekWithDateMax.getEndingDateOfApply( ) );
        }
    }

    /**
     * Handle week definition assigned event
     *
     * @param event
     *            the week definition event
     */
    public void onWeekAssigned( @ObservesAsync @Type( EventAction.CREATE ) WeekDefinitionEvent event )
    {
        WeekDefinition weekDefinition = event.getWeekDefinition( );
        ReservationRule reservationRule = ReservationRuleService.findReservationRuleById( weekDefinition.getIdReservationRule( ) );
        IndexingSlotService.indexFormByDateRange( _appointmentSlotDataSource,
                FormService.buildAppointmentFormWithoutReservationRule( reservationRule.getIdForm( ) ), weekDefinition.getDateOfApply( ),
                weekDefinition.getEndingDateOfApply( ) );
    }

    /**
     * Handle week definition unassigned event
     *
     * @param event
     *            the week definition event
     */
    public void onWeekUnassigned( @ObservesAsync @Type( EventAction.REMOVE ) WeekDefinitionEvent event )
    {
        WeekDefinition weekDefinition = event.getWeekDefinition( );
        ReservationRule reservationRule = ReservationRuleService.findReservationRuleById( weekDefinition.getIdReservationRule( ) );
        IndexingSlotService.indexFormByDateRange( _appointmentSlotDataSource,
                FormService.buildAppointmentFormWithoutReservationRule( reservationRule.getIdForm( ) ), weekDefinition.getDateOfApply( ),
                weekDefinition.getEndingDateOfApply( ) );
    }

    /**
     * Handle slot changed event
     *
     * @param event
     *            the slot event
     */
    public void onSlotChanged( @ObservesAsync @Type( EventAction.UPDATE ) SlotEvent event )
    {
        IndexingSlotService.indexSlot( event.getIdSlot( ), _appointmentSlotDataSource );
    }

    /**
     * Handle slot created event
     *
     * @param event
     *            the slot event
     */
    public void onSlotCreated( @ObservesAsync @Type( EventAction.CREATE ) SlotEvent event )
    {
        IndexingSlotService.indexSlot( event.getIdSlot( ), _appointmentSlotDataSource );
    }

    /**
     * Handle slot removed event
     *
     * @param event
     *            the slot event
     */
    public void onSlotRemoved( @ObservesAsync @Type( EventAction.REMOVE ) SlotEvent event )
    {
        if ( event.getSlot( ) != null )
        {
            IndexingSlotService.indexFormByDateRange( _appointmentSlotDataSource,
                    FormService.buildAppointmentFormWithoutReservationRule( event.getSlot( ).getIdForm( ) ),
                    event.getSlot( ).getEndingDateTime( ).toLocalDate( ), event.getSlot( ).getEndingDateTime( ).toLocalDate( ) );
        }
    }

    /**
     * Handle slot ending time changed event
     *
     * @param event
     *            the slot ending time changed event
     */
    public void onSlotEndingTimeChanged( @ObservesAsync SlotEndingTimeChangedEvent event )
    {
        IndexingSlotService.indexFormByDateRange( _appointmentSlotDataSource,
                FormService.buildAppointmentFormWithoutReservationRule( event.getIdForm( ) ), event.getEndingDateTime( ).toLocalDate( ),
                event.getEndingDateTime( ).toLocalDate( ) );
    }

    /**
     * Handle form changed event
     *
     * @param event
     *            the form event
     */
    public void onFormChanged( @ObservesAsync @Type( EventAction.UPDATE ) FormEvent event )
    {
        IndexingSlotService.indexForm( _appointmentSlotDataSource, FormService.buildAppointmentFormWithoutReservationRule( event.getIdForm( ) ) );
    }

    /**
     * Handle form created event
     *
     * @param event
     *            the form event
     */
    public void onFormCreated( @ObservesAsync @Type( EventAction.CREATE ) FormEvent event )
    {
        IndexingSlotService.indexForm( _appointmentSlotDataSource, FormService.buildAppointmentFormWithoutReservationRule( event.getIdForm( ) ) );
    }

    /**
     * Handle form removed event
     *
     * @param event
     *            the form event
     */
    public void onFormRemoved( @ObservesAsync @Type( EventAction.REMOVE ) FormEvent event )
    {
        IndexingSlotService.deleteSlotsForm( _appointmentSlotDataSource, event.getIdForm( ) );
    }

}
