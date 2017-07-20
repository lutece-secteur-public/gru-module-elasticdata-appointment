package fr.paris.lutece.plugins.elasticdata.modules.appointment.business;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import fr.paris.lutece.plugins.appointment.business.appointment.Appointment;
import fr.paris.lutece.plugins.appointment.business.slot.Slot;
import fr.paris.lutece.plugins.appointment.service.SlotService;
import fr.paris.lutece.plugins.elasticdata.business.AbstractDataObject;

public class AppointmentDataObject extends AbstractDataObject {
	private int _nIdAppointment;
	private int _nIdSlot;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private Date _dateStartAppointment;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private Date _dateEndAppointment;

	/**
	 * Constructor with Appointment
	 * 
	 * @param demand
	 *            The appointment
	 */
	public AppointmentDataObject(Appointment appointment) {
		super();
		if (appointment != null) {
			_nIdAppointment = appointment.getIdAppointment();
			_nIdSlot = appointment.getIdSlot();
			Slot slot = SlotService.findSlotById(appointment.getIdSlot());
			_dateStartAppointment = Date.from(slot.getStartingDateTime().atZone(ZoneId.systemDefault()).toInstant());
			_dateEndAppointment = Date.from(slot.getEndingDateTime().atZone(ZoneId.systemDefault()).toInstant());
			setTimestamp(Timestamp.valueOf(slot.getStartingDateTime()).getTime());
		}
	}

	/**
	 * Returns the IdAppointment
	 * 
	 * @return The IdAppointment
	 */
	public int getIdAppointment() {
		return _nIdAppointment;
	}

	/**
	 * Sets the IdAppointment
	 * 
	 * @param nIdAppointment
	 *            The IdAppointment
	 */
	public void setIdAppointment(int nIdAppointment) {
		_nIdAppointment = nIdAppointment;
	}

	/**
	 * Get the id of the slot
	 * 
	 * @return The id of the slot
	 */
	public int getIdSlot() {
		return _nIdSlot;
	}

	/**
	 * Set the id of the slot
	 * 
	 * @param nIdSlot
	 *            The id of the slot
	 */
	public void setIdSlot(int nIdSlot) {
		this._nIdSlot = nIdSlot;
	}

	/**
	 * Get the date of start appointment
	 * 
	 * @return the date of start appointment
	 */
	public Date getStartAppointment() {
		return _dateStartAppointment;
	}

	/**
	 * Set the date of start appointment
	 * 
	 * @param date
	 *            The date of start appointment
	 */
	public void setStartAppointment(Date date) {
		this._dateStartAppointment = date;
	}

	/**
	 * Get the date of end appointment
	 * 
	 * @return the date of end appointment
	 */
	public Date getEndAppointment() {
		return _dateEndAppointment;
	}

	/**
	 * Set the date of end appointment
	 * 
	 * @param date
	 *            The date of end appointment
	 */
	public void setEndAppointment(Date date) {
		this._dateEndAppointment = date;
	}

}
