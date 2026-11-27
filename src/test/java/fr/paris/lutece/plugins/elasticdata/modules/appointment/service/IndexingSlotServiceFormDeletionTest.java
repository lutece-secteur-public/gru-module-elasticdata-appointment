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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFormDTO;
import fr.paris.lutece.plugins.elasticdata.business.DataSource;
import fr.paris.lutece.plugins.libraryelastic.util.ElasticClientException;

/**
 * Removal of a form from the index while a reindexing of the same form is running
 */
public class IndexingSlotServiceFormDeletionTest
{
    private static final int ID_FORM = 7;
    private final IndexingSlotService.FormSlotsIndex _defaultIndex = IndexingSlotService._formSlotsIndex;

    /**
     * Test double: records the writes and the deletions of the slots of a form instead of calling Elastic
     */
    private static class RecordingIndex implements IndexingSlotService.FormSlotsIndex
    {
        private final List<String> _listOperations = new CopyOnWriteArrayList<>( );
        private boolean _bRemoveDuringReindex;
        private int _nConflictingDeletions;

        @Override
        public void reindex( DataSource dataSource, AppointmentFormDTO apptFormDTO, StringBuilder sbuilderLogs )
        {
            // the slots of the form have been read; the form is removed before they are written
            if ( _bRemoveDuringReindex )
            {
                _bRemoveDuringReindex = false;
                IndexingSlotService.deleteSlotsForm( dataSource, apptFormDTO.getIdForm( ) );
            }
            _listOperations.add( "slots-write:" + apptFormDTO.getIdForm( ) );
        }

        @Override
        public void deleteSlots( DataSource dataSource, int nIdForm ) throws ElasticClientException
        {
            if ( _nConflictingDeletions > 0 )
            {
                _nConflictingDeletions--;
                throw new ElasticClientException( "ElasticLibrary : Error deleting by query : 1 document(s) still in version_conflicts after 3 passes", null );
            }
            _listOperations.add( "slots-delete:" + nIdForm );
        }

        String lastOperation( )
        {
            return _listOperations.isEmpty( ) ? null : _listOperations.get( _listOperations.size( ) - 1 );
        }
    }

    @AfterEach
    public void restoreIndex( )
    {
        IndexingSlotService._formSlotsIndex = _defaultIndex;
    }

    private static AppointmentFormDTO form( )
    {
        AppointmentFormDTO form = new AppointmentFormDTO( );
        form.setIdForm( ID_FORM );
        return form;
    }

    /**
     * The form is removed while its reindexing is writing its slots: the deletion must come after that write, otherwise the slots of a removed
     * form stay in the index
     */
    @Test
    public void testSlotsDeletedAfterConcurrentReindex( )
    {
        RecordingIndex index = new RecordingIndex( );
        index._bRemoveDuringReindex = true;
        IndexingSlotService._formSlotsIndex = index;

        IndexingSlotService.indexForm( null, form( ) );

        assertEquals( "slots-delete:" + ID_FORM, index.lastOperation( ), "operations: " + index._listOperations );
    }

    /**
     * A removal without reindexing deletes the slots at once
     */
    @Test
    public void testRemovalWithoutConcurrentReindex( )
    {
        RecordingIndex index = new RecordingIndex( );
        IndexingSlotService._formSlotsIndex = index;

        IndexingSlotService.deleteSlotsForm( null, ID_FORM );

        assertEquals( List.of( "slots-delete:" + ID_FORM ), index._listOperations );
    }

    /**
     * A deletion still in version conflict is tried once more, and nothing is thrown
     */
    @Test
    public void testConflictingDeletionIsRetried( )
    {
        RecordingIndex index = new RecordingIndex( );
        index._bRemoveDuringReindex = true;
        index._nConflictingDeletions = 1;
        IndexingSlotService._formSlotsIndex = index;

        assertDoesNotThrow( ( ) -> IndexingSlotService.indexForm( null, form( ) ) );

        assertEquals( "slots-delete:" + ID_FORM, index.lastOperation( ), "operations: " + index._listOperations );
    }
}
