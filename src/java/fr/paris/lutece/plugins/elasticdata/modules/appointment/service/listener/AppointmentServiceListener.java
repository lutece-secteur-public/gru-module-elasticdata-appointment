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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;

import fr.paris.lutece.plugins.appointment.business.appointment.Appointment;
import fr.paris.lutece.plugins.appointment.service.event.AppointmentDateChangedEvent;
import fr.paris.lutece.plugins.appointment.service.event.AppointmentEvent;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.service.AppointmentDataSource;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.service.AppointmentHistoryDataSource;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.service.IndexingAppointmentService;
import fr.paris.lutece.portal.business.event.ResourceEvent;
import fr.paris.lutece.portal.service.event.EventAction;
import fr.paris.lutece.portal.service.event.Type;

/**
 * CDI event listener for appointment indexing in Elasticsearch
 */
@ApplicationScoped
public class AppointmentServiceListener
{

    @Inject
    private AppointmentDataSource _appointmentDataSource;
    @Inject
    private AppointmentHistoryDataSource _appointmentHistoryDataSource;
    @Inject
    private IndexingAppointmentService _indexingAppointmentService;

    /**
     * Handle appointment removed event
     *
     * @param event
     *            the appointment event
     */
    public void onAppointmentRemoved( @ObservesAsync @Type( EventAction.REMOVE ) AppointmentEvent event )
    {
        _indexingAppointmentService.deleteAppointmentAndHistory( _appointmentDataSource, _appointmentHistoryDataSource, event.getIdAppointment( ) );
    }

    /**
     * Handle appointment date changed event
     *
     * @param event
     *            the appointment date changed event
     */
    public void onAppointmentDateChanged( @ObservesAsync AppointmentDateChangedEvent event )
    {
        _indexingAppointmentService.indexAppointment( _appointmentDataSource, event.getIdAppointment( ) );
    }

    /**
     * Handle appointment created event
     *
     * @param event
     *            the appointment event
     */
    public void onAppointmentCreated( @ObservesAsync @Type( EventAction.CREATE ) AppointmentEvent event )
    {
        _indexingAppointmentService.indexAppointment( _appointmentDataSource, event.getIdAppointment( ) );
    }

    /**
     * CDI observer for workflow resource update events
     *
     * @param event
     *            the resource event
     */
    public void updatedResource( @Observes @Type( EventAction.UPDATE ) ResourceEvent event )
    {
        // listener on the actions of the workflow
        if ( event.getTypeResource( ).equals( Appointment.APPOINTMENT_RESOURCE_TYPE ) )
        {
            _indexingAppointmentService.indexAppointmentStateAndHistory( _appointmentDataSource, _appointmentHistoryDataSource,
                    Integer.parseInt( event.getIdResource( ) ) );
        }

    }

}
