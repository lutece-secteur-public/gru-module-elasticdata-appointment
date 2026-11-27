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
package fr.paris.lutece.plugins.elasticdata.modules.appointment.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import fr.paris.lutece.plugins.appointment.business.category.Category;
import fr.paris.lutece.plugins.appointment.business.category.CategoryHome;
import fr.paris.lutece.plugins.appointment.business.slot.Slot;
import fr.paris.lutece.plugins.appointment.service.FormService;
import fr.paris.lutece.plugins.appointment.service.SlotService;
import fr.paris.lutece.plugins.appointment.service.WeekDefinitionService;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFormDTO;
import fr.paris.lutece.plugins.elasticdata.business.DataObject;
import fr.paris.lutece.plugins.elasticdata.business.DataSource;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.business.AppointmentSlotDataObject;
import fr.paris.lutece.plugins.elasticdata.service.DataSourceService;
import fr.paris.lutece.plugins.libraryelastic.util.ElasticClientException;
import fr.paris.lutece.portal.service.util.AppLogService;

public class IndexingSlotService
{

    private static final int _nBatchSize = 100;
    private static ConcurrentMap<Integer, AtomicBoolean> _lockIndexerIsRuning = new ConcurrentHashMap<>( );
    private static ConcurrentMap<Integer, AtomicBoolean> _lockIndexToLunch = new ConcurrentHashMap<>( );
    private static AtomicBoolean _bIndexIsRunning = new AtomicBoolean( false );
    private static Queue<Integer> _queueSlotToIndex = new ConcurrentLinkedQueue<>( );
    // writes of the slots of a form in the index (replaced by a test double in the unit tests)
    static FormSlotsIndex _formSlotsIndex = new ElasticFormSlotsIndex( );
    // forms removed while their reindexing was running: their slots are deleted when that reindexing ends
    private static Set<Integer> _setFormToDelete = ConcurrentHashMap.newKeySet( );
    private static final int FORM_DELETION_ATTEMPTS = 2;

    private IndexingSlotService( )
    {

    }

    /**
     * Index the form and the slots in elastic-searche
     * 
     * @param dataSource
     *            the data source
     * @param apptFormdto
     *            the form
     */
    public static void indexForm( DataSource dataSource, AppointmentFormDTO apptFormDTO )
    {
        AtomicBoolean bIndexIsRunning = getIndexRuningLock( apptFormDTO.getIdForm( ) );
        AtomicBoolean bIndexToLunch = getIndexToLunchLock( apptFormDTO.getIdForm( ) );
        bIndexToLunch.set( true );
        if ( bIndexIsRunning.compareAndSet( false, true ) )
        {
            StringBuilder sbuilderLogs = new StringBuilder( );
            try
            {
                while ( bIndexToLunch.compareAndSet( true, false ) )
                {
                    _formSlotsIndex.reindex( dataSource, apptFormDTO, sbuilderLogs );
                }

            }
            catch( ElasticClientException e )
            {
                AppLogService.error( "Error during ElasticDataAppointmentListener reindexForm: {}", sbuilderLogs, e );
            }
            finally
            {
                // a removal of the form received during this reindexing is applied after its writes
                deletePendingSlots( dataSource, apptFormDTO.getIdForm( ) );
                bIndexIsRunning.set( false );
                processFormDeletion( dataSource, apptFormDTO.getIdForm( ) );
            }
        }

    }

    /**
     * Index the form and the slots in elastic-searche
     * 
     * @param dataSource
     *            the data source
     * @param apptFormdto
     *            the form
     */
    public static void indexFormByDateRange( DataSource dataSource, AppointmentFormDTO apptFormDTO, LocalDate startingDate, LocalDate endingDate )
    {
        StringBuilder sbuilderLogs = new StringBuilder( );
        try
        {
            Collection<DataObject> collResult = new ArrayList<>( );
            Category category = CategoryHome.findByPrimaryKey( apptFormDTO.getIdCategory( ) );
            List<Slot> listSlots = SlotService.buildListSlot( apptFormDTO.getIdForm( ), WeekDefinitionService.findAllWeekDefinition( apptFormDTO.getIdForm( ) ),
                    startingDate, endingDate );

            for ( Slot appointmentSlot : listSlots )
            {
                collResult.add( new AppointmentSlotDataObject( apptFormDTO, appointmentSlot, AppointmentSlotUtil.INSTANCE_NAME, category ) );
            }

            String strQuery = AppointmentSlotUtil.buildQueryDateRange( apptFormDTO.getIdForm( ),
                    Timestamp.valueOf( startingDate.atStartOfDay( ) ).getTime( ), Timestamp.valueOf( endingDate.atTime( LocalTime.MAX ) ).getTime( ) );
            replaceDocuments( ( ) -> DataSourceService.deleteByQuery( dataSource, strQuery ),
                    ( ) -> DataSourceService.processIncrementalIndexing( sbuilderLogs, dataSource, collResult ) );
        }
        catch( ElasticClientException e )
        {
            AppLogService.error( "Error during ElasticDataAppointmentListener reindexForm: {}", sbuilderLogs, e );
        }
    }

    /**
     * Index the slot (and the related form to have the good number of available places) in elasticsearch
     * 
     * @param nIdSlot
     *            the id slot
     * @param dataSource
     *            the data source
     */
    public static void indexSlot( int nIdSlot, DataSource dataSource )
    {
        if ( _bIndexIsRunning.compareAndSet( false, true ) )
        {
            try
            {
                Integer nIdresource = nIdSlot;
                do
                {

                    indexingSlot( nIdresource, dataSource );
                    nIdresource = _queueSlotToIndex.poll( );

                }
                while ( nIdresource != null );
            }
            catch( ElasticClientException e )
            {
                AppLogService.error( "Error during ElasticDataAppointmentListener reindexSlot: {}", e.getMessage( ), e );
            }
            finally
            {
                _bIndexIsRunning.set( false );
                if ( !_queueSlotToIndex.isEmpty( ) )
                {
                    indexSlot( _queueSlotToIndex.poll( ), dataSource );
                }
            }
        }
        else
            if ( !_queueSlotToIndex.contains( nIdSlot ) )
            {
                _queueSlotToIndex.add( nIdSlot );
            }
    }

    /**
     * Index the slot (and the related form to have the good number of available places) in elasticsearch
     * 
     * @param nIdSlot
     *            the id slot
     * @param dataSource
     *            the data source
     * @throws ElasticClientException
     *             the Exception
     */
    private static void indexingSlot( int nIdresource, DataSource dataSource ) throws ElasticClientException
    {

        if ( _queueSlotToIndex.isEmpty( ) || _queueSlotToIndex.size( ) < _nBatchSize )
        {

            DataSourceService.processIncrementalIndexing( dataSource, builAppointmentSlotDataObject( nIdresource ) );

        }
        else
        {
            indexListSlot( _queueSlotToIndex, nIdresource, dataSource );
        }
    }

    /**
     * Delete list slot of the form in the index
     * 
     * @param dataSource
     *            the data source
     * @param nIdForm
     *            the form id
     */
    public static void deleteSlotsForm( DataSource dataSource, int nIdForm )
    {
        // the slots are deleted under the reindexing lock of the form: if a reindexing is running (it may be writing the slots read before
        // the removal), the deletion is applied when its writes are done; a pending reindexing of the removed form is cancelled
        getIndexToLunchLock( nIdForm ).set( false );
        _setFormToDelete.add( nIdForm );
        processFormDeletion( dataSource, nIdForm );
    }

    /**
     * Apply a pending removal of the form now if no reindexing of the form is running; otherwise the running reindexing applies it when its
     * writes are done
     *
     * @param dataSource
     *            the data source
     * @param nIdForm
     *            the form id
     */
    private static void processFormDeletion( DataSource dataSource, int nIdForm )
    {
        AtomicBoolean bIndexIsRunning = getIndexRuningLock( nIdForm );
        if ( _setFormToDelete.contains( nIdForm ) && bIndexIsRunning.compareAndSet( false, true ) )
        {
            try
            {
                deletePendingSlots( dataSource, nIdForm );
            }
            finally
            {
                bIndexIsRunning.set( false );
            }
        }
    }

    /**
     * Delete the slots of the form if its removal is pending. Must be called while holding the reindexing lock of the form. A deletion still in
     * version conflict is tried once more.
     *
     * @param dataSource
     *            the data source
     * @param nIdForm
     *            the form id
     */
    private static void deletePendingSlots( DataSource dataSource, int nIdForm )
    {
        if ( _setFormToDelete.remove( nIdForm ) )
        {
            for ( int nAttempt = 1; nAttempt <= FORM_DELETION_ATTEMPTS; nAttempt++ )
            {
                try
                {
                    _formSlotsIndex.deleteSlots( dataSource, nIdForm );
                    return;
                }
                catch( ElasticClientException e )
                {
                    if ( nAttempt == FORM_DELETION_ATTEMPTS )
                    {
                        AppLogService.error( "Error during ElasticDataAppointmentListener remove Form: {}", e.getMessage( ), e );
                    }
                }
            }
        }
    }

    /**
     * Reindex list of slot
     * 
     * @param queueSlotToIndex
     *            the queue of slot id
     * @param nIdSlot
     *            the id slot
     * @param dataSource
     *            the dataSource
     * @throws ElasticClientException
     *             the Exception
     */
    private static void indexListSlot( Queue<Integer> queueSlotToIndex, int nIdSlot, DataSource dataSource ) throws ElasticClientException
    {

        StringBuilder builder = new StringBuilder( );
        List<Integer> listIdSlot = new ArrayList<>( );
        if ( !queueSlotToIndex.contains( nIdSlot ) )
            queueSlotToIndex.add( nIdSlot );
        while ( !queueSlotToIndex.isEmpty( ) && listIdSlot.size( ) <= _nBatchSize )
        {
            listIdSlot.add( queueSlotToIndex.poll( ) );
        }
        DataSourceService.processIncrementalIndexing( builder, dataSource, builAppointmentSlotDataObject( listIdSlot ) );
        AppLogService.debug( builder.toString( ) );
    }

    /**
     * build AppointmentSlotDataObject
     * 
     * @param nIslot
     *            the slot id
     * @return AppointmentSlotDataObject
     */
    private static AppointmentSlotDataObject builAppointmentSlotDataObject( int nIdSlot )
    {
        Slot slot = SlotService.findSlotById( nIdSlot );
        AppointmentFormDTO apptFormdto = FormService.buildAppointmentFormWithoutReservationRule( slot.getIdForm( ) );
        return new AppointmentSlotDataObject( apptFormdto, slot, AppointmentSlotUtil.INSTANCE_NAME,
                CategoryHome.findByPrimaryKey( apptFormdto.getIdCategory( ) ) );

    }

    /**
     * Build list of AppointmentSlotDataObject
     * 
     * @param lisIdSlot
     *            the list of id slot
     * @return the list of AppointmentSlotDataObject
     */
    private static List<DataObject> builAppointmentSlotDataObject( List<Integer> lisIdSlot )
    {

        List<DataObject> collResult = new ArrayList<>( );
        Map<Integer, Category> mapCategory = CategoryHome.findAllCategories( ).stream( ).collect( Collectors.toMap( Category::getIdCategory, cat -> cat ) );
        Map<Integer, AppointmentFormDTO> mapForm = new HashMap<>( );

        for ( int nIdSlot : lisIdSlot )
        {

            Slot slot = SlotService.findSlotById( nIdSlot );
            AppointmentFormDTO appointmentForm = mapForm.get( slot.getIdForm( ) );
            if ( appointmentForm == null )
            {

                appointmentForm = FormService.buildAppointmentFormWithoutReservationRule( slot.getIdForm( ) );
                mapForm.put( appointmentForm.getIdForm( ), appointmentForm );
                collResult.add( new AppointmentSlotDataObject( appointmentForm, slot, AppointmentSlotUtil.INSTANCE_NAME,
                        mapCategory.get( appointmentForm.getIdCategory( ) ) ) );
            }
        }

        return collResult;
    }

    private static synchronized AtomicBoolean getIndexRuningLock( int nkey )
    {
        _lockIndexerIsRuning.putIfAbsent( nkey, new AtomicBoolean( false ) );
        return _lockIndexerIsRuning.get( nkey );
    }

    private static synchronized AtomicBoolean getIndexToLunchLock( int nkey )
    {
        _lockIndexToLunch.putIfAbsent( nkey, new AtomicBoolean( false ) );
        return _lockIndexToLunch.get( nkey );
    }


    /**
     * A call to Elastic
     */
    @FunctionalInterface
    interface ElasticCall
    {
        /**
         * Run the call
         *
         * @throws ElasticClientException
         *             if Elastic reports an error
         */
        void run( ) throws ElasticClientException;
    }

    /**
     * Replace the documents of a form: delete the documents matching the query, then index the current slots.
     * A deletion that ends in error (for instance documents still in version conflict after the passes of the library,
     * i.e. rewritten meanwhile by a concurrent indexing) does not stop the indexing: the slots are indexed by their
     * document id, so the remaining documents are overwritten with the current data instead of the form being left
     * without slots in the index.
     *
     * @param delete
     *            the deletion of the documents of the form
     * @param insert
     *            the indexing of the current slots
     * @throws ElasticClientException
     *             if the indexing fails
     */
    static void replaceDocuments( ElasticCall delete, ElasticCall insert ) throws ElasticClientException
    {
        try
        {
            delete.run( );
        }
        catch( ElasticClientException e )
        {
            AppLogService.info( "ElasticDataAppointment: the documents of the form could not all be deleted before its reindexing, the slots are indexed anyway: {}",
                    e.getMessage( ) );
        }
        insert.run( );
    }

    /**
     * The writes of the slots of a form in the index
     */
    interface FormSlotsIndex
    {
        /**
         * Replace the documents of the form by its current slots
         *
         * @param dataSource
         *            the data source
         * @param apptFormDTO
         *            the form
         * @param sbuilderLogs
         *            the logs
         * @throws ElasticClientException
         *             if the indexing fails
         */
        void reindex( DataSource dataSource, AppointmentFormDTO apptFormDTO, StringBuilder sbuilderLogs ) throws ElasticClientException;

        /**
         * Delete the documents of the form
         *
         * @param dataSource
         *            the data source
         * @param nIdForm
         *            the form id
         * @throws ElasticClientException
         *             if the deletion fails
         */
        void deleteSlots( DataSource dataSource, int nIdForm ) throws ElasticClientException;
    }

    /**
     * The writes of the slots of a form in Elastic
     */
    static class ElasticFormSlotsIndex implements FormSlotsIndex
    {
        @Override
        public void reindex( DataSource dataSource, AppointmentFormDTO apptFormDTO, StringBuilder sbuilderLogs ) throws ElasticClientException
        {
            Collection<DataObject> collResult = new ArrayList<>( );
            Category category = CategoryHome.findByPrimaryKey( apptFormDTO.getIdCategory( ) );
            List<Slot> listSlots = AppointmentSlotUtil.getAllSlots( apptFormDTO );

            for ( Slot appointmentSlot : listSlots )
            {
                collResult.add( new AppointmentSlotDataObject( apptFormDTO, appointmentSlot, AppointmentSlotUtil.INSTANCE_NAME, category ) );
            }

            String strQuery = AppointmentSlotUtil.buildQuery( apptFormDTO.getIdForm( ) );
            replaceDocuments( ( ) -> DataSourceService.deleteByQuery( dataSource, strQuery ),
                    ( ) -> DataSourceService.processIncrementalIndexing( sbuilderLogs, dataSource, collResult ) );
        }

        @Override
        public void deleteSlots( DataSource dataSource, int nIdForm ) throws ElasticClientException
        {
            DataSourceService.deleteByQuery( dataSource, AppointmentSlotUtil.buildQuery( nIdForm ) );
        }
    }
}
