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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.paris.lutece.plugins.appointment.business.category.Category;
import fr.paris.lutece.plugins.appointment.business.category.CategoryHome;
import fr.paris.lutece.plugins.appointment.business.planning.WeekDefinition;
import fr.paris.lutece.plugins.appointment.business.planning.WeekDefinitionHome;
import fr.paris.lutece.plugins.appointment.business.slot.Slot;
import fr.paris.lutece.plugins.appointment.service.FormService;
import fr.paris.lutece.plugins.appointment.service.SlotService;
import fr.paris.lutece.plugins.appointment.service.listeners.IFormListener;
import fr.paris.lutece.plugins.appointment.service.listeners.ISlotListener;
import fr.paris.lutece.plugins.appointment.service.listeners.IWeekDefinitionListener;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFormDTO;
import fr.paris.lutece.plugins.elasticdata.business.AbstractDataSource;
import fr.paris.lutece.plugins.elasticdata.business.DataObject;
import fr.paris.lutece.plugins.elasticdata.business.DataSource;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.business.AppointmentSlotDataObject;
import fr.paris.lutece.plugins.elasticdata.service.DataSourceService;
import fr.paris.lutece.plugins.libraryelastic.util.ElasticClientException;
import fr.paris.lutece.portal.service.util.AppLogService;

/**
 * Data source for appointment
 */
public class AppointmentSlotDataSource extends AbstractDataSource implements IFormListener, ISlotListener, IWeekDefinitionListener
{

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<DataObject> fetchDataObjects( )
    {

        Collection<DataObject> collResult = new ArrayList<DataObject>( );

        LocalDateTime localDateTime = LocalDateTime.now( );
        List<Slot> listSlots = new ArrayList<Slot>( );

        for ( AppointmentFormDTO appointment : FormService.buildAllActiveAppointmentForm( ) )
        {
            Category category = CategoryHome.findByPrimaryKey( appointment.getIdCategory( ) );
            listSlots = AppointmentSlotUtil.getAllSlots( appointment, localDateTime );
            for ( Slot appointmentSlot : listSlots )
            {
                collResult.add(
                        new AppointmentSlotDataObject( appointment, appointmentSlot, AppointmentSlotUtil.INSTANCE_NAME, listSlots, localDateTime, category ) );

            }
            listSlots.clear( );
            for ( Slot slot : SlotService.findListSlot( appointment.getIdForm( ) ) )
            {
                if ( slot.getStartingDateTime( ).isBefore( localDateTime ) && slot.getNbPlacesTaken( ) > 0 )
                {

                    collResult.add( new AppointmentSlotDataObject( appointment, slot, AppointmentSlotUtil.INSTANCE_NAME, listSlots, localDateTime, category ) );
                }
            }

        }
        return collResult;
    }

    /**
     * Reindex the form and the slots in elastic-searche
     * 
     * @param appoointmentData
     *            the form appointment
     */
    public static void reindexForm( DataSource appoointmentData, AppointmentFormDTO apptFormdto )
    {

        ( new Thread( )
        {
            @Override
            public void run( )
            {
                StringBuilder sbuilderLogs = new StringBuilder( );
                Collection<DataObject> collResult = new ArrayList<DataObject>( );
                String instanceName = AppointmentSlotUtil.INSTANCE_NAME;
                LocalDateTime localDateTime = LocalDateTime.now( );
                List<Slot> listSlots = new ArrayList<Slot>( );
                Category category = CategoryHome.findByPrimaryKey( apptFormdto.getIdCategory( ) );
                listSlots = AppointmentSlotUtil.getAllSlots( apptFormdto, localDateTime );

                for ( Slot appointmentSlot : listSlots )
                {
                    collResult.add( new AppointmentSlotDataObject( apptFormdto, appointmentSlot, instanceName, listSlots, localDateTime, category ) );

                }

                try
                {

                    DataSourceService.deleteByQuery( appoointmentData, AppointmentSlotUtil.buildQuery( apptFormdto.getIdForm( ) ) );
                    DataSourceService.processIncrementalIndexing( sbuilderLogs, appoointmentData, collResult );

                }
                catch( ElasticClientException e )
                {
                    AppLogService.error( "Error during ElasticDataAppointmentListener reindexForm: " + sbuilderLogs, e );
                }

            }
        } ).start( );
    }

    /**
     * Reindex the slot (and the related form to have the good number of available places) in solr
     * 
     * @param nIdSlot
     *            the slot id
     */
    public static void reindexSlot( Slot slot, DataSource appoointmentData, AppointmentFormDTO apptFormdto )
    {
        String instanceName = AppointmentSlotUtil.INSTANCE_NAME;
        List<Slot> listSlots = new ArrayList<Slot>( );
        Category category = CategoryHome.findByPrimaryKey( apptFormdto.getIdCategory( ) );
        listSlots = AppointmentSlotUtil.getAllSlots( apptFormdto, LocalDateTime.now( ) );

        try
        {
            AppointmentSlotDataObject appointmentSlot = new AppointmentSlotDataObject( apptFormdto, slot, instanceName, listSlots, LocalDateTime.now( ),
                    category );
            DataSourceService.processIncrementalIndexing( appoointmentData, appointmentSlot );
        }
        catch( ElasticClientException e )
        {
            AppLogService.error( "Error during ElasticDataAppointmentListener reindexSlot: " + e.getMessage( ), e );
        }
    }

    @Override
    public void notifyWeekDefinitionChange( int nIdWeekDefinition )
    {
        WeekDefinition weekDefinition = WeekDefinitionHome.findByPrimaryKey( nIdWeekDefinition );
        reindexForm( this, FormService.buildAppointmentFormLight( weekDefinition.getIdForm( ) ) );

    }

    @Override
    public void notifyWeekDefinitionCreation( int nIdWeekDefinition )
    {
        WeekDefinition weekDefinition = WeekDefinitionHome.findByPrimaryKey( nIdWeekDefinition );
        reindexForm( this, FormService.buildAppointmentFormLight( weekDefinition.getIdForm( ) ) );

    }

    @Override
    public void notifyWeekDefinitionRemoval( int nIdForm )
    {
        reindexForm( this, FormService.buildAppointmentFormLight( nIdForm ) );

    }

    @Override
    public void notifySlotChange( int nIdSlot )
    {
        Slot slot = SlotService.findSlotById( nIdSlot );
        reindexSlot( slot, this, FormService.buildAppointmentFormLight( slot.getIdForm( ) ) );
    }

    @Override
    public void notifySlotCreation( int nIdSlot )
    {
        Slot slot = SlotService.findSlotById( nIdSlot );
        reindexSlot( slot, this, FormService.buildAppointmentFormLight( slot.getIdForm( ) ) );

    }

    @Override
    public void notifySlotRemoval( int nIdSlot )
    {
        // The listener is called before the actual deletion, so we can get the
        // slot.
        Slot slot = SlotService.findSlotById( nIdSlot );
        try
        {
            DataSourceService.deleteById( this, AppointmentSlotUtil.getSlotUid( slot, AppointmentSlotUtil.INSTANCE_NAME ) );
        }
        catch( ElasticClientException e )
        {
            AppLogService.error( "Error during ElasticDataAppointmentListener remove slot: " + e.getMessage( ), e );
        }
        reindexForm( this, FormService.buildAppointmentFormLight( slot.getIdForm( ) ) );

    }

    @Override
    public void notifyFormChange( int nIdForm )
    {
        reindexForm( this, FormService.buildAppointmentFormLight( nIdForm ) );

    }

    @Override
    public void notifyFormCreation( int nIdForm )
    {
        reindexForm( this, FormService.buildAppointmentFormLight( nIdForm ) );

    }

    @Override
    public void notifyFormRemoval( int nIdForm )
    {
        try
        {
            DataSourceService.deleteByQuery( this, AppointmentSlotUtil.buildQuery( nIdForm ) );
        }
        catch( ElasticClientException e )
        {

            AppLogService.error( "Error during ElasticDataAppointmentListener remove Form: " + e.getMessage( ), e );
            ;
        }

    }

}
