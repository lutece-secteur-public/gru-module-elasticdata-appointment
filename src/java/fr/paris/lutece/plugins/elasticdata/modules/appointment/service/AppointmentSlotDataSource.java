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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import fr.paris.lutece.plugins.appointment.business.category.Category;
import fr.paris.lutece.plugins.appointment.business.category.CategoryHome;
import fr.paris.lutece.plugins.appointment.business.slot.Slot;
import fr.paris.lutece.plugins.appointment.service.FormService;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFormDTO;
import fr.paris.lutece.plugins.elasticdata.business.AbstractDataSource;
import fr.paris.lutece.plugins.elasticdata.business.BatchDataObjectsIterator;
import fr.paris.lutece.plugins.elasticdata.business.DataObject;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.business.AppointmentSlotDataObject;

/**
 * Data source for appointment
 */
public class AppointmentSlotDataSource extends AbstractDataSource
{

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getIdDataObjects( )
    {

        return FormService.findAllForms( ).stream( ).map( form -> String.valueOf( form.getIdForm( ) ) ).collect( Collectors.toList( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataObject> getDataObjects( List<String> listIdDataObjects )
    {

        this.setBatchSize( BATCH_SIZE );
        List<DataObject> collResult = new ArrayList<>( );
        for ( String strIdForm : listIdDataObjects )
        {

            AppointmentFormDTO appointmentForm = FormService.buildAppointmentFormWithoutReservationRule( Integer.parseInt( strIdForm ) );
            Category category = CategoryHome.findByPrimaryKey( appointmentForm.getIdCategory( ) );
            List<Slot> listSlots = AppointmentSlotUtil.getAllSlotsToFullIndexing( appointmentForm );
            for ( Slot appointmentSlot : listSlots )
            {
                collResult.add( new AppointmentSlotDataObject( appointmentForm, appointmentSlot, AppointmentSlotUtil.INSTANCE_NAME, category ) );

            }
        }
        return collResult;
    }

    @Override
    public Iterator<DataObject> getDataObjectsIterator( )
    {
        List<String> listIdDataObject = this.getIdDataObjects( );
        this.getIndexingStatus( ).setnNbTotalObj( listIdDataObject.size( ) );
        this.setBatchSize( 1 );
        return new BatchDataObjectsIterator( this, listIdDataObject );
    }

}
