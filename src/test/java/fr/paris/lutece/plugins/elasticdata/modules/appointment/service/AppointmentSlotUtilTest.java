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

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.paris.lutece.test.LuteceTestCase;

/**
 * Covers the delete-by-query bodies sent to Elasticsearch.
 */
public class AppointmentSlotUtilTest extends LuteceTestCase
{
    private static final ObjectMapper MAPPER = new ObjectMapper( );

    /**
     * The form query targets the form id of the indexed documents.
     *
     * @throws Exception
     *             if the body is not JSON
     */
    @Test
    public void testFormQuery( ) throws Exception
    {
        JsonNode query = MAPPER.readTree( AppointmentSlotUtil.buildQuery( 9001 ) );
        assertEquals( 9001, query.at( "/query/term/appointmentForm.idForms" ).asInt( ) );
    }

    /**
     * The range query keeps the form and bounds the timestamp.
     *
     * @throws Exception
     *             if the body is not JSON
     */
    @Test
    public void testDateRangeQuery( ) throws Exception
    {
        JsonNode must = MAPPER.readTree( AppointmentSlotUtil.buildQueryDateRange( 9001, 10L, 20L ) ).at( "/query/bool/must" );
        assertEquals( 9001, must.get( 0 ).at( "/term/appointmentForm.idForms" ).asInt( ) );
        assertEquals( 10L, must.get( 1 ).at( "/range/timestamp/from" ).asLong( ) );
        assertEquals( 20L, must.get( 1 ).at( "/range/timestamp/to" ).asLong( ) );
    }

    /**
     * The history query matches the site-prefixed appointment id and stays valid JSON whatever the site name.
     *
     * @throws Exception
     *             if the body is not JSON
     */
    @Test
    public void testResourceQuery( ) throws Exception
    {
        JsonNode query = MAPPER.readTree( AppointmentSlotUtil.buildQueryIdResource( 42 ) );
        assertEquals( AppointmentSlotUtil.INSTANCE_NAME + "_42", query.at( "/query/match/appointmentId" ).asText( ) );
    }
}
