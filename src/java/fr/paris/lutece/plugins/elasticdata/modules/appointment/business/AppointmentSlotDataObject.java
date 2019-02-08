package fr.paris.lutece.plugins.elasticdata.modules.appointment.business;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import fr.paris.lutece.plugins.appointment.business.category.Category;
import fr.paris.lutece.plugins.appointment.business.slot.Slot;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFormDTO;
import fr.paris.lutece.plugins.elasticdata.business.AbstractDataObject;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.service.AppointmentSlotUtil;

public class AppointmentSlotDataObject extends AbstractDataObject{

	 @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm" )
	 private Date _dateStartingDateTime;
	 @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm" )
	 private Date _dateEndingDateTime;
	 @JsonFormat(pattern = "KK:mm")
	 private LocalTime _timeStartingDateTime;
	 private boolean _bIsOpen;
	 private long _lDuration;
	 private int _nMaxCapacity;
	 private int _nNbRemainingPlaces;
	 private String _strNameInstance;
	 private int _nNbrPlacesTaken;
	 private AppointmentForm _appointmentForm;
	 
	 
	/**
     * Constructor with Appointment
     * 
     * @param demand
     *            The appointment
     */
    public AppointmentSlotDataObject( AppointmentFormDTO appointmentFormDto, Slot appointmentSlot, String instanceName, List<Slot> listSlots,  LocalDateTime localDate, Category category )
    {
        super( );
        if ( appointmentFormDto != null && appointmentSlot != null)
        {
        	_dateStartingDateTime= Date.from( appointmentSlot.getStartingDateTime( ).atZone( ZoneId.systemDefault( ) ).toInstant( ) );
        	_dateEndingDateTime= Date.from( appointmentSlot.getEndingDateTime().atZone( ZoneId.systemDefault( ) ).toInstant( ) );
        	_nNbRemainingPlaces = appointmentSlot.getNbRemainingPlaces();
        	_bIsOpen= appointmentSlot.getIsOpen();
        	_timeStartingDateTime= appointmentSlot.getStartingTime();
        	_lDuration = Math.abs(_dateStartingDateTime.getTime() - _dateEndingDateTime.getTime()); 
        	_nMaxCapacity = appointmentSlot.getMaxCapacity();
        	_strNameInstance= instanceName;
        	_appointmentForm = new AppointmentForm(appointmentFormDto, category);
        	_nNbrPlacesTaken= appointmentSlot.getNbPlacesTaken();

        	setTimestamp( Timestamp.valueOf( appointmentSlot.getStartingDateTime( ) ).getTime( ) );		
        	setId(AppointmentSlotUtil.getSlotUid(appointmentSlot, instanceName));
        }
        			
     }

    /**
     * Returns the StartingDateTime
     * @return The StartingDateTime
     */ 
     public Date getStartingDateTime()
     {
         return _dateStartingDateTime;
     }
 
    /**
     * Sets the StartingDateTime
     * @param dateStartingDateTime The StartingDateTime
     */ 
     public void setStartingDateTime( Date dateStartingDateTime )
     {
         _dateStartingDateTime = dateStartingDateTime;
     }
     /**
      * Returns the EndingDateTime
      * @return The EndingDateTime
      */ 
      public Date getEndingDateTime()
      {
          return _dateEndingDateTime;
      }
  
     /**
      * Sets the EndingDateTime
      * @param dateEndingDateTime The EndingDateTime
      */ 
      public void setEndingDateTime( Date dateEndingDateTime )
      {
    	  _dateEndingDateTime = dateEndingDateTime;
      }
 
      /**
       * Returns the Duration
       * @return The Duration
       */ 
       public long getDuration()
       {
           return _lDuration;
       }
   
      /**
       * Sets the Duration
       * @param lDuration The Duration
       */ 
       public void setDuration( long lDuration )
       {
           _lDuration = lDuration;
       }
    /**
     * Returns the MaxCapacity
     * @return The MaxCapacity
     */ 
     public int getMaxCapacity()
     {
         return _nMaxCapacity;
     }
 
    /**
     * Sets the MaxCapacity
     * @param nMaxCapacity The MaxCapacity
     */ 
     public void setMaxCapacity( int nMaxCapacity )
     {
         _nMaxCapacity = nMaxCapacity;
     }
 
    /**
     * Returns the NbRemainingPlaces
     * @return The NbRemainingPlaces
     */ 
     public int getNbRemainingPlaces()
     {
         return _nNbRemainingPlaces;
     }
 
    /**
     * Sets the NbRemainingPlaces
     * @param nNbRemainingPlaces The NbRemainingPlaces
     */ 
     public void setNbRemainingPlaces( int nNbRemainingPlaces )
     {
         _nNbRemainingPlaces = nNbRemainingPlaces;
     }
 
    /**
     * Returns the NameInstance
     * @return The NameInstance
     */ 
     public String getNameInstance()
     {
         return _strNameInstance;
     }
 
    /**
     * Sets the NameInstance
     * @param strNameInstance The NameInstance
     */ 
     public void setNameInstance( String strNameInstance )
     {
         _strNameInstance = strNameInstance;
     }
 
    /**
     * Returns the AppointmentForm
     * @return The AppointmentForm
     */ 
     public AppointmentForm getAppointmentForm()
     {
         return _appointmentForm;
     }
 
    /**
     * Sets the AppointmentForm
     * @param appointmentForm The AppointmentForm
     */ 
     public void setAppointmentForm( AppointmentForm appointmentForm )
     {
         _appointmentForm = appointmentForm;
     }
 
    /**
     * Returns the nbrPlacesTaken
     * @return The nbrPlacesTaken
     */ 
     public int getNbrPlacesTaken()
     {
         return _nNbrPlacesTaken;
     }
 
    /**
     * Sets the nbrPlacesTaken
     * @param nbrPlacesTaken The nbrPlacesTaken
     */ 
     public void setNbrPlacesTaken( int nbrPlacesTaken )
     {
    	 _nNbrPlacesTaken = nbrPlacesTaken;
     }
     
     /**
      * Returns the IsOpen
      * @return The IsOpen
      */ 
      public boolean getIsOpen()
      {
          return _bIsOpen;
      }
  
     /**
      * Sets the IsOpen
      * @param bIsOpen The IsOpen
      */ 
      public void setIsOpen( boolean bIsOpen )
      {
          _bIsOpen = bIsOpen;
      }
  
     /**
      * Returns the TimeStartingDateTime
      * @return The TimeStartingDateTime
      */ 
      public LocalTime getTimeStartingDateTime()
      {
          return _timeStartingDateTime;
      }
  
     /**
      * Sets the TimeStartingDateTime
      * @param timeStartingDateTime The TimeStartingDateTime
      */ 
      public void setTimeStartingDateTime( LocalTime timeStartingDateTime )
      {
          _timeStartingDateTime = timeStartingDateTime;
      }
}
