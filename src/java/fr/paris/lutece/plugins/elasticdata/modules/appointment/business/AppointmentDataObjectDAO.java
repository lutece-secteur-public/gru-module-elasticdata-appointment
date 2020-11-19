package fr.paris.lutece.plugins.elasticdata.modules.appointment.business;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import fr.paris.lutece.plugins.appointment.business.appointment.Appointment;
import fr.paris.lutece.plugins.appointment.business.slot.Slot;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.service.AppointmentSlotUtil;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

public class AppointmentDataObjectDAO implements IAppointmentDataObject{

	

	private static final String SQL_QUERY_SELECT_BY_ID_FORM= "SELECT  app.id_appointment,state.name, wh.action_name, app.nb_places, app.is_cancelled, slot.starting_date_time, app.date_appointment_create, app.admin_access_code_create"
															+" FROM appointment_appointment app INNER JOIN appointment_appointment_slot appslot ON app.id_appointment = appslot.id_appointment INNER JOIN appointment_slot slot ON appslot.id_slot = slot.id_slot"
	+" LEFT JOIN workflow_resource_workflow wsw on (wsw.id_resource = app.id_appointment and resource_type=?) LEFT JOIN  workflow_state state on (state.id_state= wsw.id_state)"+
    " LEFT JOIN   (select  DISTINCT a.id_resource, a.id_action, a.creation_date, wa.name as action_name from workflow_resource_history a "
	+ " inner join (Select MAX(b.creation_date) creation_date, b.id_resource from workflow_resource_history b, workflow_action wa "
	+ " where b.resource_type=? and wa.is_automatic_reflexive_action=0 and wa.id_action = b.id_action  "
			+ " group by b.id_resource) s on (s.creation_date = a.creation_date and s.id_resource= a.id_resource )  "
			+ " INNER JOIN workflow_action wa on (wa.id_action = a.id_action and wa.is_automatic_reflexive_action=0 )) wh on (app.id_appointment = wh.id_resource) WHERE slot.id_form = ?";
	
	
    public List<AppointmentDataObject> select( int nIdForm, List<Slot> listSlots, LocalDateTime localTime, AppointmentForm appointmentForm , Plugin plugin )
    {
        DAOUtil daoUtil = null;
        List<AppointmentDataObject> listAppointment = new ArrayList<AppointmentDataObject>( );
        try
        {
            daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_ID_FORM, plugin );
            daoUtil.setString( 1, Appointment.APPOINTMENT_RESOURCE_TYPE );
            daoUtil.setString( 2, Appointment.APPOINTMENT_RESOURCE_TYPE );
            daoUtil.setInt(3, nIdForm);
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                listAppointment.add( buildAppointmentDataObject( daoUtil, listSlots, localTime, appointmentForm ) );
            }
        }
        finally
        {
            if ( daoUtil != null )
            {
                daoUtil.free( );
            }
        }
        return listAppointment;
    }
    
    /**
     * Build AppointmentDataObject 
     * @param daoUtil the daoUtil
     * @param listSlots The list slots
	 * @param localTime The localTime 
	 * @param appointmentForm The appointment form
     * @return AppointmentDataObject
     */
    private AppointmentDataObject buildAppointmentDataObject(DAOUtil daoUtil,  List<Slot> listSlots, LocalDateTime localTime, AppointmentForm appointmentForm  ){
    	
    	AppointmentDataObject apptData= new AppointmentDataObject();
    	int nIndex = 1;
    	
    	apptData.setIdAppointment(daoUtil.getInt(nIndex++));
    	apptData.setState(daoUtil.getString(nIndex++));    	
    	apptData.setLastAction(daoUtil.getString(nIndex++));
    	apptData.setNbPlaces(daoUtil.getInt(nIndex++));
    	apptData.setIsCancelled(daoUtil.getBoolean(nIndex++));
    	apptData.setTimestamp( daoUtil.getTimestamp(nIndex++).getTime() );
    	apptData.setCreatedTimestamp( daoUtil.getTimestamp(nIndex++).getTime() );
    	apptData.setAdminCreator(daoUtil.getString(nIndex++));

    	apptData.setId(AppointmentSlotUtil.getAppointmentId(apptData.getIdAppointment( ), AppointmentSlotUtil.INSTANCE_NAME));
    	apptData.setNameInstance(AppointmentSlotUtil.INSTANCE_NAME);
    	AppointmentSlotUtil.buildAppointmentDataObject(apptData, listSlots, localTime, appointmentForm);

    	
    	return apptData;
    }

}
