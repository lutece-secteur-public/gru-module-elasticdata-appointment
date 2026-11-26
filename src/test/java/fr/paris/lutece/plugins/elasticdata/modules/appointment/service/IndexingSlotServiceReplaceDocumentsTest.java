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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.libraryelastic.util.ElasticClientException;

/**
 * Reindexing of the slots of a form when the deletion of its documents leaves documents in version conflict
 */
public class IndexingSlotServiceReplaceDocumentsTest
{
    private static final String CONFLICT_MESSAGE = "ElasticLibrary : Error deleting by query : 1 document(s) still in version_conflicts after 3 passes";

    /**
     * The deletion of the documents of the form ends with documents still in version conflict (written by a concurrent indexing): the current
     * slots must still be indexed, otherwise the form keeps no slot in the index until its next reindexing
     */
    @Test
    public void testSlotsIndexedWhenDeletionLeavesVersionConflicts( )
    {
        AtomicInteger nInserts = new AtomicInteger( );

        assertDoesNotThrow( ( ) -> IndexingSlotService.replaceDocuments( ( ) -> {
            throw new ElasticClientException( CONFLICT_MESSAGE, null );
        }, nInserts::incrementAndGet ) );

        assertEquals( 1, nInserts.get( ), "the current slots are indexed even though the deletion reported version conflicts" );
    }

    /**
     * An error of the indexing itself is still reported to the caller
     */
    @Test
    public void testIndexingErrorIsReported( )
    {
        assertThrows( ElasticClientException.class, ( ) -> IndexingSlotService.replaceDocuments( ( ) -> {
        }, ( ) -> {
            throw new ElasticClientException( "bulk error", null );
        } ) );
    }
}
