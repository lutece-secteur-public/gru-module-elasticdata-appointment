package fr.paris.lutece.plugins.elasticdata.modules.appointment.business;

import java.time.LocalDateTime;
import java.util.List;

import fr.paris.lutece.plugins.appointment.business.slot.Slot;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;

public class AppointmentDataObjectHome {

	 // Static variable pointed at the DAO instance
    private static IAppointmentDataObject _dao = SpringContextService.getBean( "elasticdata-appointment.appointmentDataObjectDAO");
    private static Plugin _plugin = PluginService.getPlugin( ElasticDataAppointmentPlugin.PLUGIN_NAME );
    
    
	/**
	 * Select all appointments 
	 * @param nIdForm The Id Form
	 * @param listSlots The list slots
	 * @param localTime The localTime 
	 * @param appointmentForm The appointment form
	 * @return list appointments
	 */
    public static List<AppointmentDataObject> selectListAppointData(int nIdForm, List<Slot> listSlots, LocalDateTime localTime, AppointmentForm appointmentForm ){
    	
    	return _dao.select(nIdForm, listSlots, localTime, appointmentForm, _plugin);
    }
}
