/*
 * Copyright (c) 2002-2018, Mairie de Paris
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


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import fr.paris.lutece.plugins.appointment.business.appointment.Appointment;
import fr.paris.lutece.plugins.appointment.business.category.Category;
import fr.paris.lutece.plugins.appointment.business.category.CategoryHome;
import fr.paris.lutece.plugins.appointment.business.form.Form;
import fr.paris.lutece.plugins.appointment.business.form.FormHome;
import fr.paris.lutece.plugins.appointment.business.slot.Slot;
import fr.paris.lutece.plugins.appointment.service.AppointmentService;
import fr.paris.lutece.plugins.appointment.service.FormService;
import fr.paris.lutece.plugins.appointment.service.listeners.IAppointmentListener;
import fr.paris.lutece.plugins.appointment.service.listeners.IAppointmentWorkflowActionListener;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentDTO;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFilterDTO;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFormDTO;
import fr.paris.lutece.plugins.elasticdata.business.AbstractDataSource;
import fr.paris.lutece.plugins.elasticdata.business.DataObject;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.business.AppointmentDataObject;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.business.AppointmentDataObjectHome;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.business.AppointmentForm;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.business.AppointmentPartialDataObject;
import fr.paris.lutece.plugins.elasticdata.service.DataSourceService;
import fr.paris.lutece.plugins.libraryelastic.util.ElasticClientException;
import fr.paris.lutece.plugins.workflowcore.business.action.Action;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import fr.paris.lutece.plugins.workflowcore.service.action.IActionService;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.portal.service.util.AppLogService;


/**
 * Data source for appointment
 */
public class AppointmentDataSource extends AbstractDataSource implements IAppointmentListener, IAppointmentWorkflowActionListener
{
	  @Inject
      private  IResourceHistoryService _resourceHistoryService;
	  @Inject
	  private IActionService _actionService;
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<DataObject> fetchDataObjects( )
    {
 
    	Collection<DataObject> collResult = new ArrayList< >( );
        LocalDateTime localDate =LocalDateTime.now();
        List<Slot> listSlots= new ArrayList<Slot>();
        AppointmentFilterDTO appointmentFilter= new AppointmentFilterDTO();
 
        List<Form> listForm= FormHome.findActiveForms( );
        for(Form form: listForm){
        	Category category = CategoryHome.findByPrimaryKey( form.getIdCategory( ) );
        	AppointmentForm appointmentForm = new AppointmentForm(form, category);
            appointmentFilter.setIdForm(form.getIdForm());
            collResult.addAll(AppointmentDataObjectHome.selectListAppointData(form.getIdForm(), listSlots, localDate, appointmentForm));
        	
        }
        
        return collResult;
    }
    public void reindexAppointment(int nIdAppointment ) {
    	
		try {
		
		Action action=null;
		State stateAppointment= null;
		AppointmentDTO appointment=AppointmentService.buildAppointmentDTOFromIdAppointment(nIdAppointment);
		LocalDateTime localDate =LocalDateTime.now();
		LocalDate startingDateOfDisplay = LocalDate.now( );
		AppointmentFormDTO form=FormService.buildAppointmentFormLight(appointment.getIdForm( ));
		List<Slot> listSlots=AppointmentSlotUtil.getAllSlots(form , startingDateOfDisplay );
		
		Category category = CategoryHome.findByPrimaryKey( form.getIdCategory( ) );
    	AppointmentForm appointmentForm = new AppointmentForm(form, category);
    	
		ResourceHistory resourceHist= _resourceHistoryService.getLastHistoryResource(appointment.getIdAppointment(), Appointment.APPOINTMENT_RESOURCE_TYPE, form.getIdWorkflow());
		if(resourceHist != null){
			action= (resourceHist.getAction().isAutomaticReflexiveAction())? resourceHist.getAction(): null;
			stateAppointment = resourceHist.getAction().getStateAfter();
			
		}else{
			stateAppointment = AppointmentSlotUtil.getState(appointment.getIdAppointment(), form.getIdWorkflow());
		}

		AppointmentDataObject apptData= new AppointmentDataObject(appointment, stateAppointment, action, listSlots, localDate, appointmentForm);
		
		DataSourceService.processIncrementalIndexing(this, apptData);
		
		} catch (ElasticClientException e) {
			AppLogService.error( "Error during ElasticDataAppointmentListener rindexing appointment: " + e.getMessage( ), e );
		}
    }

	@Override
	public void notifyAppointmentRemoval(int nIdAppointment) {
		try {
			DataSourceService.deleteById(this, AppointmentSlotUtil.getAppointmentId(nIdAppointment, AppointmentSlotUtil.INSTANCE_NAME));
		} catch (ElasticClientException e) {
			AppLogService.error( "Error during ElasticDataAppointmentListener remove appointment: " + e.getMessage( ), e );
		}
		
	}

	@Override
	public String appointmentDateChanged(int nIdAppointment, List<Integer> listIdSlot, Locale locale) {
		reindexAppointment(nIdAppointment);
		return null;
	}
    
	
	@Override
	public void notifyAppointmentCreated(int nIdAppointment) {
		reindexAppointment(nIdAppointment);
		
	}
	@Override
	public void notifyAppointmentUpdated(int nIdAppointment) {
		
		
	}
	@Override
	public void notifyAppointmentWFActionTriggered(int nIdAppointment,
			int nIdAction) {
		
		Appointment appt= AppointmentService.findAppointmentById(nIdAppointment);
		Action action= _actionService.findByPrimaryKey(nIdAction);
		
		AppointmentPartialDataObject appPartialData=new AppointmentPartialDataObject (appt, action.getStateAfter() ,action ); 
		
		try {
			DataSourceService.partialUpdate(this, appPartialData.getId( ), appPartialData);
		} catch (ElasticClientException e) {
			AppLogService.error( "Error during ElasticDataAppointmentListener update partial appointment: " + e.getMessage( ), e );
		}
	
	}

   

}
