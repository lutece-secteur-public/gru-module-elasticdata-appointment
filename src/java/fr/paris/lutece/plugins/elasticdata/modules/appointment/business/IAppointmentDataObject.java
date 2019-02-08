package fr.paris.lutece.plugins.elasticdata.modules.appointment.business;

import java.time.LocalDateTime;
import java.util.List;

import fr.paris.lutece.plugins.appointment.business.slot.Slot;
import fr.paris.lutece.portal.service.plugin.Plugin;

public interface IAppointmentDataObject {
	/**
	 * Select all appointments 
	 * @param nIdForm The Id Form
	 * @param listSlots The list slots
	 * @param localTime The localTime 
	 * @param appointmentForm The appointment form
	 * @param plugin the plugin
	 * @return list appointments
	 */
	public List<AppointmentDataObject> select( int nIdForm, List<Slot> listSlots, LocalDateTime localTime, AppointmentForm appointmentForm , Plugin plugin );
}
