/*
 * Copyright (c) 2002-2023, City of Paris
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.Test;

import jakarta.enterprise.inject.Vetoed;

import fr.paris.lutece.plugins.libraryelastic.util.ElasticClientException;

/**
 * Removal of an appointment from the index while the indexing of its workflow history is running
 */
public class IndexingAppointmentServiceDeletionTest
{
    private static final int ID_APPOINTMENT = 42;

    /**
     * Test double: records the writes and the deletions of the history documents instead of calling Elastic (not a CDI bean)
     */
    @Vetoed
    private static class RecordingService extends IndexingAppointmentService
    {
        private final List<String> _listOperations = new CopyOnWriteArrayList<>( );
        private boolean _bRemoveDuringIndexing;
        private int _nConflictingDeletions;

        @Override
        void indexingAppointmentStateAndHistory( AppointmentDataSource appointmentDataSource, AppointmentHistoryDataSource appointmentHistoryDataSource,
                int nIdresource ) throws ElasticClientException
        {
            // the history of the appointment has been read; the appointment is removed before its documents are written
            if ( _bRemoveDuringIndexing )
            {
                _bRemoveDuringIndexing = false;
                deleteAppointmentAndHistory( appointmentDataSource, appointmentHistoryDataSource, nIdresource );
            }
            _listOperations.add( "history-write:" + nIdresource );
        }

        @Override
        void deleteAppointmentDocument( AppointmentDataSource appointmentDataSource, int nIdAppointment )
        {
            _listOperations.add( "appointment-delete:" + nIdAppointment );
        }

        @Override
        void deleteHistoryDocuments( AppointmentHistoryDataSource appointmentHistoryDataSource, int nIdAppointment ) throws ElasticClientException
        {
            if ( _nConflictingDeletions > 0 )
            {
                _nConflictingDeletions--;
                throw new ElasticClientException( "ElasticLibrary : Error deleting by query : 2 document(s) still in version_conflicts after 3 passes", null );
            }
            _listOperations.add( "history-delete:" + nIdAppointment );
        }

        String lastHistoryOperation( )
        {
            String strLast = null;
            for ( String strOperation : _listOperations )
            {
                if ( strOperation.startsWith( "history-" ) )
                {
                    strLast = strOperation;
                }
            }
            return strLast;
        }
    }

    /**
     * The appointment is removed while the indexing of its history is writing it: the deletion of the history documents must come after that write,
     * otherwise the history of a removed appointment stays in the index (and the concurrent write is what puts the deletion in version conflict)
     */
    @Test
    public void testHistoryDeletedAfterConcurrentHistoryIndexing( )
    {
        RecordingService service = new RecordingService( );
        service._bRemoveDuringIndexing = true;

        service.indexAppointmentStateAndHistory( null, null, ID_APPOINTMENT );

        assertEquals( "history-delete:" + ID_APPOINTMENT, service.lastHistoryOperation( ), "operations: " + service._listOperations );
        assertEquals( true, service._listOperations.contains( "appointment-delete:" + ID_APPOINTMENT ) );
    }

    /**
     * A removal without concurrent indexing deletes the appointment and its history at once
     */
    @Test
    public void testRemovalWithoutConcurrentIndexing( )
    {
        RecordingService service = new RecordingService( );

        service.deleteAppointmentAndHistory( null, null, ID_APPOINTMENT );

        assertEquals( List.of( "appointment-delete:" + ID_APPOINTMENT, "history-delete:" + ID_APPOINTMENT ), service._listOperations );
    }

    /**
     * A deletion of the history that still meets version conflicts is retried once the running history indexing is over, and never thrown
     */
    @Test
    public void testConflictingHistoryDeletionIsRetried( )
    {
        RecordingService service = new RecordingService( );
        service._bRemoveDuringIndexing = true;
        service._nConflictingDeletions = 1;

        assertDoesNotThrow( ( ) -> service.indexAppointmentStateAndHistory( null, null, ID_APPOINTMENT ) );

        assertEquals( "history-delete:" + ID_APPOINTMENT, service.lastHistoryOperation( ), "operations: " + service._listOperations );
    }
}
