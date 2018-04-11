package fr.paris.lutece.plugins.elasticdata.modules.appointment.business;

import java.util.ArrayList;
import java.util.Collection;

import fr.paris.lutece.plugins.appointment.business.appointment.Appointment;
import fr.paris.lutece.plugins.appointment.service.AppointmentService;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFilter;
import fr.paris.lutece.plugins.elasticdata.business.AbstractDataSource;
import fr.paris.lutece.plugins.elasticdata.business.DataObject;
import fr.paris.lutece.plugins.elasticdata.business.DataSource;

public class AppointmentDataSource extends AbstractDataSource implements DataSource
{

    @Override
    public Collection<DataObject> getDataObjects( )
    {

        AppointmentFilter appointmentFilter = new AppointmentFilter( );
        Collection<DataObject> collResult = new ArrayList<DataObject>( );

        for ( Appointment appointment : AppointmentService.findListAppointmentsByFilter( appointmentFilter ) )
        {

            collResult.add( new AppointmentDataObject( appointment ) );

        }
        return collResult;
    }

}
