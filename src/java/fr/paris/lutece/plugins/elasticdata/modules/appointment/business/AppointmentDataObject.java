/*
 * Copyright (c) 2002-2021, City of Paris
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
import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.appointment.business.appointment.Appointment;
import fr.paris.lutece.plugins.appointment.service.AppointmentUtilities;
import fr.paris.lutece.plugins.elasticdata.business.AbstractDataObject;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.service.AppointmentSlotUtil;
import fr.paris.lutece.plugins.workflowcore.business.state.State;

/**
 * This is the business class for the object Appointments
 */
public class AppointmentDataObject extends AbstractDataObject
{
    // Variables declarations

    private int _nIdAppointment;
    private int _nNbPlaces;
    private boolean _bIsCancelled;
    private long _lTimeUntilAvailability;
    private long _lTimeUntilAvailabilityCategory;
    private long _lSumNbPlacesBeforeAppointment;
    private long _lCategorySumNbPlacesBeforeAppointment;
    private String _strState;
    private AppointmentForm _appointmentForm;
    private String _strNameInstance;
    private long _lCreatedTimestamp;
    private boolean _bIsAdminCreator;

    public AppointmentDataObject( )
    {

    }

    public AppointmentDataObject( Appointment appointment, State stateAppointment, AppointmentForm appointmentForm )
    {

        setId( String.valueOf( appointment.getIdAppointment( ) ) );
        _nIdAppointment = appointment.getIdAppointment( );
        _nNbPlaces = appointment.getNbPlaces( );
        _bIsCancelled = appointment.getIsCancelled( );
        _strNameInstance = AppointmentSlotUtil.INSTANCE_NAME;
        _bIsAdminCreator = StringUtils.isNotEmpty( appointment.getAdminUserCreate( ) );
        _appointmentForm = appointmentForm;

        if ( stateAppointment != null )
        {
            _strState = stateAppointment.getName( );
        }
        setTimestamp( Timestamp.valueOf( AppointmentUtilities.getStartingDateTime( appointment ) ).getTime( ) );
    }

    /**
     * Returns the IdAppointmebt
     * 
     * @return The IdAppointmebt
     */
    public int getIdAppointment( )
    {
        return _nIdAppointment;
    }

    /**
     * Sets the IdAppointmet
     * 
     * @param nIdAppointmebt
     *            The IdAppointmet
     */
    public void setIdAppointment( int nIdAppointmet )
    {
        _nIdAppointment = nIdAppointmet;
    }

    /**
     * Returns the NbPlaces
     * 
     * @return The NbPlaces
     */
    public int getNbPlaces( )
    {
        return _nNbPlaces;
    }

    /**
     * Sets the NbPlaces
     * 
     * @param nNbPlaces
     *            The NbPlaces
     */
    public void setNbPlaces( int nNbPlaces )
    {
        _nNbPlaces = nNbPlaces;
    }

    /**
     * Returns the IsCancelled
     * 
     * @return The IsCancelled
     */
    public boolean getIsCancelled( )
    {
        return _bIsCancelled;
    }

    /**
     * Sets the IsCancelled
     * 
     * @param bIsCancelled
     *            The IsCancelled
     */
    public void setIsCancelled( boolean bIsCancelled )
    {
        _bIsCancelled = bIsCancelled;
    }

    /**
     * Returns the TimeUntilAvailability
     * 
     * @return The TimeUntilAvailability
     */
    public long getTimeUntilAvailability( )
    {
        return _lTimeUntilAvailability;
    }

    /**
     * Sets the TimeUntilAvailability
     * 
     * @param lTimeUntilAvailability
     *            The TimeUntilAvailability
     */
    public void setTimeUntilAvailability( long lTimeUntilAvailability )
    {
        _lTimeUntilAvailability = lTimeUntilAvailability;
    }

    /**
     * Returns the TimeUntilAvailabilityCategory
     * 
     * @return The TimeUntilAvailabilityCategory
     */
    public long getTimeUntilCategoryAvailability( )
    {
        return _lTimeUntilAvailabilityCategory;
    }

    /**
     * Sets the TimeUntilAvailabilityCategory
     * 
     * @param lTimeUntilAvailabilityCategory
     *            The TimeUntilAvailabilityCategory
     */
    public void setTimeUntilCategoryAvailability( long lTimeUntilAvailabilityCategory )
    {
        _lTimeUntilAvailabilityCategory = lTimeUntilAvailabilityCategory;
    }

    /**
     * Returns the SumNbPlacesBeforeAppointment
     * 
     * @return The SumNbPlacesBeforeAppointment
     */
    public long getSumNbPlacesBeforeAppointment( )
    {
        return _lSumNbPlacesBeforeAppointment;
    }

    /**
     * Sets the SumNbPlacesBeforeAppointment
     * 
     * @param lSumNbPlacesBeforeAppointment
     *            The SumNbPlacesBeforeAppointment
     */
    public void setSumNbPlacesBeforeAppointment( long lSumNbPlacesBeforeAppointment )
    {
        _lSumNbPlacesBeforeAppointment = lSumNbPlacesBeforeAppointment;
    }

    /**
     * Returns the CategorySumNbPlacesBeforeAppointment
     * 
     * @return The CategorySumNbPlacesBeforeAppointment
     */
    public long getCategorySumNbPlacesBeforeAppointment( )
    {
        return _lCategorySumNbPlacesBeforeAppointment;
    }

    /**
     * Sets the CategorySumNbPlacesBeforeAppointment
     * 
     * @param lCategorySumNbPlacesBeforeAppointment
     *            The CategorySumNbPlacesBeforeAppointment
     */
    public void setCategorySumNbPlacesBeforeAppointment( long lCategorySumNbPlacesBeforeAppointment )
    {
        _lCategorySumNbPlacesBeforeAppointment = lCategorySumNbPlacesBeforeAppointment;
    }

    /**
     * Returns the State
     * 
     * @return The State
     */
    public String getState( )
    {
        return _strState;
    }

    /**
     * Sets the State
     * 
     * @param strState
     *            The State
     */
    public void setState( String strState )
    {
        _strState = strState;
    }

    /**
     * Returns the NameInstance
     * 
     * @return The NameInstance
     */
    public String getNameInstance( )
    {
        return _strNameInstance;
    }

    /**
     * Sets the NameInstance
     * 
     * @param strNameInstance
     *            The NameInstance
     */
    public void setNameInstance( String strNameInstance )
    {
        _strNameInstance = strNameInstance;
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

    /**
     * Returns the created timestamp
     * 
     * @return The created timestamp
     */
    public long getCreatedTimestamp( )
    {
        return _lCreatedTimestamp;
    }

    /**
     * Sets the created timestamp
     * 
     * @param lCreatedTimestamp
     *            The created timestamp
     */
    public void setCreatedTimestamp( long lCreatedTimestamp )
    {
        _lCreatedTimestamp = lCreatedTimestamp;
    }

    /**
     * Returns if the appointment is taken in FO calendar
     * 
     * @return The bollean
     */
    public boolean getAdminCreator( )
    {
        return _bIsAdminCreator;
    }

    /**
     * Sets the bIsFront
     * 
     * @param bIsFront
     *            The bIsFront
     */
    public void setAdminCreator( boolean bIsAdminCreator )
    {
        _bIsAdminCreator = bIsAdminCreator;
    }
}
