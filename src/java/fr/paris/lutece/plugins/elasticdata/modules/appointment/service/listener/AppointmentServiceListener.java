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

import java.util.List;
import java.util.Locale;
import javax.inject.Inject;

import fr.paris.lutece.plugins.appointment.business.appointment.Appointment;
import fr.paris.lutece.plugins.appointment.service.listeners.IAppointmentListener;
import fr.paris.lutece.plugins.appointment.service.listeners.IAppointmentWorkflowActionListener;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.service.AppointmentDataSource;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.service.AppointmentHistoryDataSource;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.service.IndexingAppointmentService;
import fr.paris.lutece.portal.business.event.EventRessourceListener;
import fr.paris.lutece.portal.business.event.ResourceEvent;

/**
 * Data source for appointment
 */
public class AppointmentServiceListener implements IAppointmentListener, IAppointmentWorkflowActionListener, EventRessourceListener
{

    private static final String NAME = "APPOINTMENT_ELASTIC_DATA_LISTNER";
    @Inject
    private AppointmentDataSource _appointmentDataSource;
    @Inject
    private AppointmentHistoryDataSource _appointmentHistoryDataSource;

    @Override
    public void notifyAppointmentRemoval( int nIdAppointment )
    {
        IndexingAppointmentService.getService( ).deleteAppointmentAndHistory( _appointmentDataSource, _appointmentHistoryDataSource, nIdAppointment );

    }

    @Override
    public String appointmentDateChanged( int nIdAppointment, List<Integer> listIdSlot, Locale locale )
    {
        IndexingAppointmentService.getService( ).indexAppointment( _appointmentDataSource, nIdAppointment );
        return null;
    }

    @Override
    public void notifyAppointmentCreated( int nIdAppointment )
    {
        IndexingAppointmentService.getService( ).indexAppointment( _appointmentDataSource, nIdAppointment );

    }

    @Override
    public void notifyAppointmentUpdated( int nIdAppointment )
    {
        // update of functional data (name, tel...)
    }

    @Override
    public void notifyAppointmentWFActionTriggered( int nIdAppointment, int nIdAction )
    {
        // it's already called by updatedResource(ResourceEvent event)
    }

    @Override
    public String getName( )
    {

        return NAME;
    }

    @Override
    public void addedResource( ResourceEvent event )
    {
        // listener on the actions of the workflow
        // addition of the resource in the workflow_resource_workflow
    }

    @Override
    public void deletedResource( ResourceEvent event )
    {
        // listener on the actions of the workflow
        // it's already called by notifyAppointmentRemoval( int nIdAppointment )

    }

    @Override
    public void updatedResource( ResourceEvent event )
    {
        // listener on the actions of the workflow
        if ( event.getTypeResource( ).equals( Appointment.APPOINTMENT_RESOURCE_TYPE ) )
        {
            IndexingAppointmentService.getService( ).indexAppointmentStateAndHistory( _appointmentDataSource, _appointmentHistoryDataSource,
                    Integer.parseInt( event.getIdResource( ) ) );
        }

    }

}
