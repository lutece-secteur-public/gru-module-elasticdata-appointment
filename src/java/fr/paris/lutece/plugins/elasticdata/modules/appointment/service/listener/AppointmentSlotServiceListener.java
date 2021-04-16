/*
 * Copyright (c) 2002-2021, City of Paris
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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import fr.paris.lutece.plugins.appointment.business.planning.WeekDefinition;
import fr.paris.lutece.plugins.appointment.business.rule.ReservationRule;
import fr.paris.lutece.plugins.appointment.business.slot.Slot;
import fr.paris.lutece.plugins.appointment.service.FormService;
import fr.paris.lutece.plugins.appointment.service.ReservationRuleService;
import fr.paris.lutece.plugins.appointment.service.listeners.IFormListener;
import fr.paris.lutece.plugins.appointment.service.listeners.ISlotListener;
import fr.paris.lutece.plugins.appointment.service.listeners.IWeekDefinitionListener;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.service.AppointmentSlotDataSource;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.service.IndexingSlotService;

/**
 * Data source for appointment
 */
public class AppointmentSlotServiceListener implements IFormListener, ISlotListener, IWeekDefinitionListener
{
    @Inject
    private AppointmentSlotDataSource _appointmentSlotDataSource;

    @Override
    public void notifyListWeeksChanged( int nIdForm, List<WeekDefinition> listWeek )
    {
        WeekDefinition weekWithDateMin = listWeek.stream( ).min( Comparator.comparing( WeekDefinition::getDateOfApply ) ).orElse( null );
        WeekDefinition weekWithDateMax = listWeek.stream( ).max( Comparator.comparing( WeekDefinition::getEndingDateOfApply ) ).orElse( null );

        IndexingSlotService.indexFormByDateRange( _appointmentSlotDataSource, FormService.buildAppointmentFormWithoutReservationRule( nIdForm ),
                weekWithDateMin.getDateOfApply( ), weekWithDateMax.getEndingDateOfApply( ) );

    }

    @Override
    public void notifyWeekAssigned( WeekDefinition weekDefinition )
    {

        ReservationRule reservationRule = ReservationRuleService.findReservationRuleById( weekDefinition.getIdReservationRule( ) );
        IndexingSlotService.indexFormByDateRange( _appointmentSlotDataSource,
                FormService.buildAppointmentFormWithoutReservationRule( reservationRule.getIdForm( ) ), weekDefinition.getDateOfApply( ),
                weekDefinition.getEndingDateOfApply( ) );

    }

    @Override
    public void notifyWeekUnassigned( WeekDefinition weekDefinition )
    {
        notifyWeekAssigned( weekDefinition );

    }

    @Override
    public void notifySlotChange( int nIdSlot )
    {
        IndexingSlotService.indexSlot( nIdSlot, _appointmentSlotDataSource );
    }

    @Override
    public void notifySlotCreation( int nIdSlot )
    {
        notifySlotChange( nIdSlot );
    }

    @Override
    public void notifySlotRemoval( Slot slot )
    {
        IndexingSlotService.indexFormByDateRange( _appointmentSlotDataSource, FormService.buildAppointmentFormWithoutReservationRule( slot.getIdForm( ) ),
                slot.getEndingDateTime( ).toLocalDate( ), slot.getEndingDateTime( ).toLocalDate( ) );

    }

    @Override
    public void notifySlotEndingTimeHasChanged( int nIdSlot, int nIdFom, LocalDateTime endingDateTime )
    {

        IndexingSlotService.indexFormByDateRange( _appointmentSlotDataSource, FormService.buildAppointmentFormWithoutReservationRule( nIdFom ),
                endingDateTime.toLocalDate( ), endingDateTime.toLocalDate( ) );

    }

    @Override
    public void notifyFormChange( int nIdForm )
    {
        IndexingSlotService.indexForm( _appointmentSlotDataSource, FormService.buildAppointmentFormWithoutReservationRule( nIdForm ) );

    }

    @Override
    public void notifyFormCreation( int nIdForm )
    {
        IndexingSlotService.indexForm( _appointmentSlotDataSource, FormService.buildAppointmentFormWithoutReservationRule( nIdForm ) );

    }

    @Override
    public void notifyFormRemoval( int nIdForm )
    {
        IndexingSlotService.deleteSlotsForm( _appointmentSlotDataSource, nIdForm );
    }

}
