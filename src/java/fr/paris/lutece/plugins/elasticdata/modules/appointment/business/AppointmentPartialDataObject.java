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

import fr.paris.lutece.plugins.appointment.business.appointment.Appointment;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.service.AppointmentSlotUtil;
import fr.paris.lutece.plugins.workflowcore.business.action.Action;
import fr.paris.lutece.plugins.workflowcore.business.state.State;



/**
 * This is the business class for the object Partial Appointments
 */ 
public class AppointmentPartialDataObject 
{
        // Variables declarations 
	    
        private String _strId;
        private int _nNbPlaces;
        private boolean _bIsCancelled;
        private String _strState;
        private String _strLastAction;
    
        public AppointmentPartialDataObject (Appointment appointment, State stateAppointment, Action action ){
        	
        	_strId= AppointmentSlotUtil.getAppointmentId(appointment.getIdAppointment( ), AppointmentSlotUtil.INSTANCE_NAME);
            _nNbPlaces= appointment.getNbPlaces();
        	_bIsCancelled= appointment.getIsCancelled();
        	if(action != null){
        		_strLastAction= action.getName();
        	}        
        	if(stateAppointment != null ){
        	_strState= stateAppointment.getName();
        	}
        }
       /**
        * Returns the Id
        * @return The Id
        */ 
        public String getId()
        {
            return _strId;
        }
    
       /**
        * Sets the Id
        * @param nId The Id 
        */ 
        public void setId( String nId )
        {
        	_strId = nId;
        }
    
       /**
        * Returns the NbPlaces
        * @return The NbPlaces
        */ 
        public int getNbPlaces()
        {
            return _nNbPlaces;
        }
    
       /**
        * Sets the NbPlaces
        * @param nNbPlaces The NbPlaces
        */ 
        public void setNbPlaces( int nNbPlaces )
        {
            _nNbPlaces = nNbPlaces;
        }
    
       /**
        * Returns the IsCancelled
        * @return The IsCancelled
        */ 
        public boolean getIsCancelled()
        {
            return _bIsCancelled;
        }
    
       /**
        * Sets the IsCancelled
        * @param bIsCancelled The IsCancelled
        */ 
        public void setIsCancelled( boolean bIsCancelled )
        {
            _bIsCancelled = bIsCancelled;
        }
        
       /**
        * Returns the State
        * @return The State
        */ 
        public String getState()
        {
            return _strState;
        }
    
       /**
        * Sets the State
        * @param strState The State
        */ 
        public void setState( String strState )
        {
            _strState = strState;
        }
  
         /**
          * 
          * @return
          */
         public String getLastAction()
         {
             return _strLastAction;
         }
     
        /**
         * Sets the _strLastAction
         * @param _strLastAction The ActionLastAction
         */ 
         public void setLastAction( String strLastAction )
         {
        	 _strLastAction= strLastAction;
         }
}
