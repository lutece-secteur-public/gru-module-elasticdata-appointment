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
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides Data Access methods for AppointmentDataHistory objects
 */
public final class AppointmentDataHistoryDAO implements IAppointmentDataHistoryDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT = "SELECT id_data_history, data_type, data_value, id_ressource FROM elasticdata_appointment_data_history WHERE id_data_history = ?";
    private static final String SQL_QUERY_SELECT_BY_TYPE = "SELECT id_data_history, data_type, data_value, id_ressource FROM elasticdata_appointment_data_history WHERE id_ressource = ? AND data_type = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO elasticdata_appointment_data_history ( data_type, data_value, id_ressource ) VALUES ( ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM elasticdata_appointment_data_history WHERE id_data_history = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE elasticdata_appointment_data_history SET id_data_history = ?, data_type = ?, data_value = ?, id_ressource = ? WHERE id_data_history = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_data_history, data_type, data_value, id_ressource FROM elasticdata_appointment_data_history";
    private static final String SQL_QUERY_SELECTALL_ID = "SELECT id_data_history FROM elasticdata_appointment_data_history";

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( AppointmentDataHistory appointmentDataHistory, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setString( nIndex++, appointmentDataHistory.getDataType( ) );
            daoUtil.setString( nIndex++, appointmentDataHistory.getDataValue( ) );
            daoUtil.setString( nIndex++, appointmentDataHistory.getIdRessource( ) );

            daoUtil.executeUpdate( );
            if ( daoUtil.nextGeneratedKey( ) )
            {
                appointmentDataHistory.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AppointmentDataHistory load( int nKey, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );
            AppointmentDataHistory appointmentDataHistory = null;

            if ( daoUtil.next( ) )
            {
                appointmentDataHistory = new AppointmentDataHistory( );
                int nIndex = 1;

                appointmentDataHistory.setId( daoUtil.getInt( nIndex++ ) );
                appointmentDataHistory.setDataType( daoUtil.getString( nIndex++ ) );
                appointmentDataHistory.setDataValue( daoUtil.getString( nIndex++ ) );
                appointmentDataHistory.setIdRessource( daoUtil.getString( nIndex ) );
            }

            daoUtil.free( );
            return appointmentDataHistory;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nKey, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeUpdate( );
            daoUtil.free( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( AppointmentDataHistory appointmentDataHistory, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setInt( nIndex++, appointmentDataHistory.getId( ) );
            daoUtil.setString( nIndex++, appointmentDataHistory.getDataType( ) );
            daoUtil.setString( nIndex++, appointmentDataHistory.getDataValue( ) );
            daoUtil.setString( nIndex++, appointmentDataHistory.getIdRessource( ) );
            daoUtil.setInt( nIndex, appointmentDataHistory.getId( ) );

            daoUtil.executeUpdate( );
            daoUtil.free( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<AppointmentDataHistory> selectAppointmentDataHistorysList( Plugin plugin )
    {
        List<AppointmentDataHistory> appointmentDataHistoryList = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                AppointmentDataHistory appointmentDataHistory = new AppointmentDataHistory( );
                int nIndex = 1;

                appointmentDataHistory.setId( daoUtil.getInt( nIndex++ ) );
                appointmentDataHistory.setDataType( daoUtil.getString( nIndex++ ) );
                appointmentDataHistory.setDataValue( daoUtil.getString( nIndex++ ) );
                appointmentDataHistory.setIdRessource( daoUtil.getString( nIndex ) );

                appointmentDataHistoryList.add( appointmentDataHistory );
            }

            daoUtil.free( );
            return appointmentDataHistoryList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Integer> selectIdAppointmentDataHistorysList( Plugin plugin )
    {
        List<Integer> appointmentDataHistoryList = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_ID, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                appointmentDataHistoryList.add( daoUtil.getInt( 1 ) );
            }

            daoUtil.free( );
            return appointmentDataHistoryList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectAppointmentDataHistorysReferenceList( Plugin plugin )
    {
        ReferenceList appointmentDataHistoryList = new ReferenceList( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                appointmentDataHistoryList.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
            }

            daoUtil.free( );
            return appointmentDataHistoryList;
        }
    }

    @Override
    public AppointmentDataHistory loadByType( String strIdRessource, String strType, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_TYPE, plugin ) )
        {
            daoUtil.setString( 1, strIdRessource );
            daoUtil.setString( 2, strType );
            daoUtil.executeQuery( );
            AppointmentDataHistory appointmentDataHistory = null;

            if ( daoUtil.next( ) )
            {
                appointmentDataHistory = new AppointmentDataHistory( );
                int nIndex = 1;

                appointmentDataHistory.setId( daoUtil.getInt( nIndex++ ) );
                appointmentDataHistory.setDataType( daoUtil.getString( nIndex++ ) );
                appointmentDataHistory.setDataValue( daoUtil.getString( nIndex++ ) );
                appointmentDataHistory.setIdRessource( daoUtil.getString( nIndex ) );
            }

            daoUtil.free( );
            return appointmentDataHistory;
        }
    }
}
