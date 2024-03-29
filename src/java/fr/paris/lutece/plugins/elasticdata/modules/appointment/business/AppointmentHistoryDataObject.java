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
package fr.paris.lutece.plugins.elasticdata.modules.appointment.business;

import java.sql.Timestamp;

import fr.paris.lutece.plugins.elasticdata.business.AbstractDataObject;

/**
 * FormResponseDataObject
 */
public class AppointmentHistoryDataObject extends AbstractDataObject
{
    private String _strId;
    private String _strAppointmentId;
    private String _strActionName;
    private long _lTaskDuration;
    private long _lAppointmentDuration;
    private long _lAppointmentDateActionDateDuration;
    private String _strUnitName;
    private Timestamp _tCreationDate;
    private AppointmentForm _appointmentForm;

    public AppointmentHistoryDataObject( int resourceHistoryId, AppointmentForm appointmentForm )
    {
        setId( String.valueOf( resourceHistoryId ) );
        _appointmentForm = appointmentForm;
    }

    /**
     * Returns the Appointment id
     * 
     * @return The Appointment id
     */
    public String getId( )
    {
        return _strId;
    }

    /**
     * Sets the Appointment id
     * 
     * @param strId
     *            The Appointment id
     */
    public void setId( String strId )
    {
        _strId = strId;
    }

    /**
     * Returns the Appointment id
     * 
     * @return The Appointment id
     */
    public String getAppointmentId( )
    {
        return _strAppointmentId;
    }

    /**
     * Sets the Appointment id
     * 
     * @param nAppointmentId
     *            The Appointment id
     */
    public void setAppointmentId( String strAppointmentId )
    {
        _strAppointmentId = strAppointmentId;
    }

    /**
     * Returns the Duration
     * 
     * @return The Duration
     */
    public long getTaskDuration( )
    {
        return _lTaskDuration;
    }

    /**
     * Sets the Duration
     * 
     * @param lTaskDuration
     *            The Duration
     */
    public void setTaskDuration( long lTaskDuration )
    {
        _lTaskDuration = lTaskDuration;
    }

    /**
     * Returns the Duration between appointment taken date and action date
     * 
     * @return The Duration
     */
    public long getAppointmentDuration( )
    {
        return _lAppointmentDuration;
    }

    /**
     * Sets the Duration between appointment taken date and action date
     * 
     * @param lAppointmentDuration
     *            The Duration
     */
    public void setAppointmentDuration( long lAppointmentDuration )
    {
        _lAppointmentDuration = lAppointmentDuration;
    }

    /**
     * Returns the Duration between appointment date and action date
     * 
     * @return The Duration
     */
    public long getAppointmentDateActionDateDuration( )
    {
        return _lAppointmentDateActionDateDuration;
    }

    /**
     * Sets the Duration between appointment date and action date
     * 
     * @param lAppointmentDateActionDateDuration
     *            The Duration
     */
    public void setAppointmentDateActionDateDuration( long lAppointmentDateActionDateDuration )
    {
        _lAppointmentDateActionDateDuration = lAppointmentDateActionDateDuration;
    }

    /**
     * Returns the Unit Name
     * 
     * @return The Unit Name
     */
    public String getUnitName( )
    {
        return _strUnitName;
    }

    /**
     * Sets the Unit Name
     * 
     * @param strUnitName
     *            The Unit Name
     */
    public void setUnitName( String strUnitName )
    {
        _strUnitName = strUnitName;
    }

    /**
     * Returns the action name
     * 
     * @return The Action Name
     */
    public String getActionName( )
    {
        return _strActionName;
    }

    /**
     * Sets the Action Name
     * 
     * @param strActionName
     *            The Action Name
     */
    public void setActionName( String strActionName )
    {
        _strActionName = strActionName;
    }

    /**
     *
     * @return the creation date
     */
    public Timestamp getCreationDate( )
    {
        return _tCreationDate;
    }

    /**
     * set the creation date
     * 
     * @param dateCreation
     *            the creation date
     */
    public void setCreationDate( Timestamp dateCreation )
    {
        _tCreationDate = dateCreation;
    }

    /**
     * Returns the AppointmentForm
     * 
     * @return The AppointmentForm
     */
    public AppointmentForm getAppointmentForm( )
    {
        return _appointmentForm;
    }

    /**
     * Sets the AppointmentForm
     * 
     * @param appointmentForm
     *            The AppointmentForm
     */
    public void setAppointmentForm( AppointmentForm appointmentForm )
    {
        _appointmentForm = appointmentForm;
    }

}
