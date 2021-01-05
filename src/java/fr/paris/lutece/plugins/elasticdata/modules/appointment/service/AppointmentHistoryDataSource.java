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

import fr.paris.lutece.plugins.appointment.service.AppointmentService;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentDTO;
import fr.paris.lutece.plugins.appointment.business.appointment.Appointment;
import fr.paris.lutece.plugins.appointment.business.appointment.AppointmentHome;
import fr.paris.lutece.plugins.appointment.business.form.Form;
import fr.paris.lutece.plugins.appointment.business.form.FormHome;
import fr.paris.lutece.plugins.elasticdata.business.AbstractDataSource;
import fr.paris.lutece.plugins.elasticdata.business.DataObject;
import fr.paris.lutece.plugins.elasticdata.business.DataSource;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.business.AppointmentHistoryDataObject;
import fr.paris.lutece.plugins.elasticdata.service.DataSourceService;
import fr.paris.lutece.plugins.libraryelastic.util.ElasticClientException;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.resource.ResourceHistoryService;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * AppointmentHistoryDataSource
 */
public class AppointmentHistoryDataSource extends AbstractDataSource
{
    @Override
    public Collection<DataObject> fetchDataObjects( )
    {
        ArrayList<DataObject> collResult = new ArrayList<>( );
        List<Form> listForm = FormHome.findActiveForms( );
        for ( Form form : listForm )
        {
            List<Appointment> listAppointment = AppointmentService.findListAppointmentByIdForm( form.getIdForm( ) );
            listAppointment.parallelStream( ).forEach( appointment -> {

                collResult.addAll( getResourceHistoryList( appointment, form ) );

            } );
        }
        return collResult;
    }

    /**
     * Create a form response object
     * 
     * @param appointment
     *            The Appointment
     * @param form
     *            The Form
     * @return The form response object
     */
    public List<AppointmentHistoryDataObject> getResourceHistoryList( Appointment appointment, Form form )
    {

        List<AppointmentHistoryDataObject> appointmentHistoryList = new ArrayList<>( );
        IResourceHistoryService _resourceHistoryService = SpringContextService.getBean( ResourceHistoryService.BEAN_SERVICE );
        List<ResourceHistory> listResourceHistory = _resourceHistoryService.getAllHistoryByResource( appointment.getIdAppointment( ), "appointment",
                form.getIdWorkflow( ) );
        List<ResourceHistory> listResourceHistorySorted = listResourceHistory.stream( ).sorted( Comparator.comparing( ResourceHistory::getId ) )
                .collect( Collectors.toList( ) );

        Timestamp appointmentCreation = appointment.getAppointmentTakenSqlDate( );
        Timestamp appointmentPreviousActionCreation = appointmentCreation;

        for ( ResourceHistory resourceHistory : listResourceHistorySorted )
        {

            long lTaskDuration = duration( appointmentPreviousActionCreation, resourceHistory.getCreationDate( ) );
            long lAppointmentDuration = duration( appointmentCreation, resourceHistory.getCreationDate( ) );

            AppointmentHistoryDataObject appointmentHistoryDataObject = new AppointmentHistoryDataObject( resourceHistory.getId( ) );
            appointmentHistoryDataObject.setFormName( form.getTitle( ) );
            appointmentHistoryDataObject.setFormId( form.getIdForm( ) );
            appointmentHistoryDataObject.setAppointmentId( AppointmentSlotUtil.INSTANCE_NAME + "_" + appointment.getIdAppointment( ) );
            appointmentHistoryDataObject.setTimestamp( resourceHistory.getCreationDate( ).getTime( ) );
            appointmentHistoryDataObject.setTaskDuration( lTaskDuration );
            appointmentHistoryDataObject.setAppointmentDuration( lAppointmentDuration );
            appointmentHistoryDataObject.setActionName( resourceHistory.getAction( ).getName( ) );

            appointmentPreviousActionCreation = resourceHistory.getCreationDate( );

            appointmentHistoryList.add( appointmentHistoryDataObject );

        }

        return appointmentHistoryList;
    }

    /**
     * Index Form Response data object to Elasticdata
     * 
     * @param nIdResource
     *            The Appointment id
     * @param nIdTask
     *            The Form
     */
    public void indexDocument( int nIdResource, int nIdTask )
    {
        Appointment appointment = AppointmentHome.findByPrimaryKey( nIdResource );
        AppointmentDTO appointmentDTO = AppointmentService.buildAppointmentDTOFromIdAppointment( nIdResource );
        Form form = FormHome.findByPrimaryKey( appointmentDTO.getIdForm( ) );
        try
        {
            // Force init data sources of ElasticData plugin
            DataSourceService.getDataSources( );
            DataSource source = DataSourceService.getDataSource( "AppointmentHistoryDataSource" );
            List<AppointmentHistoryDataObject> appointmentHistoryList = getResourceHistoryList( appointment, form );
            for ( AppointmentHistoryDataObject appointmentHistoryDataObject : appointmentHistoryList )
            {
                DataSourceService.processIncrementalIndexing( source, appointmentHistoryDataObject );
            }
        }
        catch( ElasticClientException e )
        {
            AppLogService.error( "Unable to process incremental indexing of idRessource :" + nIdResource, e );
        }
        catch( NullPointerException e )
        {
            AppLogService.error( "Unable to get AppointmentHistoryDataSource :" + nIdResource, e );
        }
    }

    /**
     * return The duration in milli
     * 
     * @param start
     *            The start time.
     * @param end
     *            The end time.
     * @return The duration in milli
     */
    private static long duration( java.sql.Timestamp start, java.sql.Timestamp end )
    {
        long milliseconds1 = start.getTime( );
        long milliseconds2 = end.getTime( );
        long diff = milliseconds2 - milliseconds1;
        return diff;
    }

    public static <T> Predicate<T> distinctByKey( Function<? super T, Object> keyExtractor )
    {
        Map<Object, Boolean> map = new ConcurrentHashMap<>( );
        return t -> map.putIfAbsent( keyExtractor.apply( t ), Boolean.TRUE ) == null;
    }
}
