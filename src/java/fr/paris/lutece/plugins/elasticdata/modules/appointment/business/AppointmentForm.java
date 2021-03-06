/*
 * Copyright (c) 2002-2019, Mairie de Paris
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

import fr.paris.lutece.plugins.appointment.business.category.Category;
import fr.paris.lutece.plugins.appointment.business.form.Form;
import fr.paris.lutece.plugins.appointment.business.localization.Localization;
import fr.paris.lutece.plugins.appointment.service.LocalizationService;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFormDTO;

/**
 * This is the business class for the object AppointmentForm
 */
public class AppointmentForm
{
    // Variables declarations
    private int _nIdForms;
    private String _strTitleForms;
    private String _strCategory;
    private boolean _bIsActive;
    private String _strGeoPoint;
    private String _strAddress;

    public AppointmentForm( AppointmentFormDTO appointmentFormDto, Category category )
    {

        _nIdForms = appointmentFormDto.getIdForm( );
        _strTitleForms = appointmentFormDto.getTitle( );
        _bIsActive = appointmentFormDto.getIsActive( );
        if ( category != null )
        {
            _strCategory = category.getLabel( );
        }
        setLocalization( );

    }

    public AppointmentForm( Form form, Category category )
    {

        _nIdForms = form.getIdForm( );
        _strTitleForms = form.getTitle( );
        _bIsActive = form.getIsActive( );
        if ( category != null )
        {
            _strCategory = category.getLabel( );
        }
        setLocalization( );
    }

    /**
     * Set AppointmentForm Localization variable(s)
     * 
     */
    private void setLocalization( )
    {
        Localization localization = LocalizationService.findLocalizationWithFormId( _nIdForms );
        if ( localization != null &&  localization.getLatitude( ) != null && localization.getLongitude( ) != null )
        {
            _strGeoPoint = localization.getLatitude( ) + ", " + localization.getLongitude( );
            _strAddress = localization.getAddress( );
        }
    }

    /**
     * Returns the IdForms
     * 
     * @return The IdForms
     */
    public int getIdForms( )
    {
        return _nIdForms;
    }

    /**
     * Sets the IdForms
     * 
     * @param nIdForms
     *            The IdForms
     */
    public void setIdForms( int nIdForms )
    {
        _nIdForms = nIdForms;
    }

    /**
     * Returns the TitleForms
     * 
     * @return The TitleForms
     */
    public String getTitleForms( )
    {
        return _strTitleForms;
    }

    /**
     * Sets the TitleForms
     * 
     * @param strTitleForms
     *            The TitleForms
     */
    public void setTitleForms( String strTitleForms )
    {
        _strTitleForms = strTitleForms;
    }

    /**
     * Returns the Category
     * 
     * @return The Category
     */
    public String getCategory( )
    {
        return _strCategory;
    }

    /**
     * Sets the Category
     * 
     * @param strCategory
     *            The Category
     */
    public void setCategory( String strCategory )
    {
        _strCategory = strCategory;
    }

    /**
     * Returns the IsActive
     * 
     * @return The IsActive
     */
    public boolean getIsActive( )
    {
        return _bIsActive;
    }

    /**
     * Sets the IsActive
     * 
     * @param bIsActive
     *            The IsActive
     */
    public void setIsActive( boolean bIsActive )
    {
        _bIsActive = bIsActive;
    }

    /**
     * Return The GeoPoint
     * 
     * @return The GeoPoint
     */
    public String getGeoPoint( )
    {
        return _strGeoPoint;
    }

    /**
     * Sets the GeoPoint
     * 
     * @param strGeoPoint
     *            The GeoPoint
     */
    public void setGeoPoint( String strGeoPoint )
    {
        _strGeoPoint = strGeoPoint;
    }

    /**
     * Return The Address
     * 
     * @return The Address
     */
    public String getAddress( )
    {
        return _strAddress;
    }

    /**
     * Sets the Address
     * 
     * @param strAddress
     *            The GeoPoint
     */
    public void setAddress( String strAddress )
    {
        _strAddress = strAddress;
    }
}
