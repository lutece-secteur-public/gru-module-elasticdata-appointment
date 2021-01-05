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
package fr.paris.lutece.plugins.elasticdata.modules.appointment.business;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceList;

import java.util.List;

/**
 * This class provides instances management methods (create, find, ...) for AppointmentDataHistory objects
 */
public final class AppointmentDataHistoryHome
{
    // Static variable pointed at the DAO instance
    private static IAppointmentDataHistoryDAO _dao = SpringContextService.getBean( "elasticdata-appointment.appointmentDataHistoryDAO" );
    private static Plugin _plugin = PluginService.getPlugin( ElasticDataAppointmentPlugin.PLUGIN_NAME );

    /**
     * Private constructor - this class need not be instantiated
     */
    private AppointmentDataHistoryHome( )
    {
    }

    /**
     * Create an instance of the appointmentDataHistory class
     * 
     * @param appointmentDataHistory
     *            The instance of the AppointmentDataHistory which contains the informations to store
     * @return The instance of appointmentDataHistory which has been created with its primary key.
     */
    public static AppointmentDataHistory create( AppointmentDataHistory appointmentDataHistory )
    {
        _dao.insert( appointmentDataHistory, _plugin );

        return appointmentDataHistory;
    }

    /**
     * Update of the appointmentDataHistory which is specified in parameter
     * 
     * @param appointmentDataHistory
     *            The instance of the AppointmentDataHistory which contains the data to store
     * @return The instance of the appointmentDataHistory which has been updated
     */
    public static AppointmentDataHistory update( AppointmentDataHistory appointmentDataHistory )
    {
        _dao.store( appointmentDataHistory, _plugin );

        return appointmentDataHistory;
    }

    /**
     * Remove the appointmentDataHistory whose identifier is specified in parameter
     * 
     * @param nKey
     *            The appointmentDataHistory Id
     */
    public static void remove( int nKey )
    {
        _dao.delete( nKey, _plugin );
    }

    /**
     * Returns an instance of a appointmentDataHistory whose identifier is specified in parameter
     * 
     * @param nKey
     *            The appointmentDataHistory primary key
     * @return an instance of AppointmentDataHistory
     */
    public static AppointmentDataHistory findByPrimaryKey( int nKey )
    {
        return _dao.load( nKey, _plugin );
    }

    /**
     * Returns an instance of a appointmentDataHistory
     * 
     * @param strRessourceId
     *            The ressource id
     * @param strType
     *            The data Type
     * @return an instance of AppointmentDataHistory
     */
    public static AppointmentDataHistory findByType( String strRessourceId, String strType )
    {
        return _dao.loadByType( strRessourceId, strType, _plugin );
    }

    /**
     * Load the data of all the appointmentDataHistory objects and returns them as a list
     * 
     * @return the list which contains the data of all the appointmentDataHistory objects
     */
    public static List<AppointmentDataHistory> getAppointmentDataHistorysList( )
    {
        return _dao.selectAppointmentDataHistorysList( _plugin );
    }

    /**
     * Load the id of all the appointmentDataHistory objects and returns them as a list
     * 
     * @return the list which contains the id of all the appointmentDataHistory objects
     */
    public static List<Integer> getIdAppointmentDataHistorysList( )
    {
        return _dao.selectIdAppointmentDataHistorysList( _plugin );
    }

    /**
     * Load the data of all the appointmentDataHistory objects and returns them as a referenceList
     * 
     * @return the referenceList which contains the data of all the appointmentDataHistory objects
     */
    public static ReferenceList getAppointmentDataHistorysReferenceList( )
    {
        return _dao.selectAppointmentDataHistorysReferenceList( _plugin );
    }
}
