package fr.paris.lutece.plugins.elasticdata.modules.appointment.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import fr.paris.lutece.plugins.appointment.business.appointment.Appointment;
import fr.paris.lutece.plugins.appointment.business.category.Category;
import fr.paris.lutece.plugins.appointment.business.category.CategoryHome;
import fr.paris.lutece.plugins.appointment.business.form.Form;
import fr.paris.lutece.plugins.appointment.service.AppointmentService;
import fr.paris.lutece.plugins.appointment.service.AppointmentUtilities;
import fr.paris.lutece.plugins.appointment.service.FormService;
import fr.paris.lutece.plugins.appointment.service.SlotService;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFilterDTO;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFormDTO;
import fr.paris.lutece.plugins.elasticdata.business.DataObject;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.business.AppointmentDataObject;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.business.AppointmentForm;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.business.AppointmentHistoryDataObject;
import fr.paris.lutece.plugins.elasticdata.modules.appointment.business.AppointmentPartialDataObject;
import fr.paris.lutece.plugins.elasticdata.service.DataSourceService;
import fr.paris.lutece.plugins.libraryelastic.util.ElasticClientException;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistoryFilter;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import fr.paris.lutece.plugins.workflowcore.business.state.StateFilter;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceWorkflowService;
import fr.paris.lutece.plugins.workflowcore.service.resource.ResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.resource.ResourceWorkflowService;
import fr.paris.lutece.plugins.workflowcore.service.state.IStateService;
import fr.paris.lutece.plugins.workflowcore.service.state.StateService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.ReferenceItem;

public class IndexingAppointmentService {
	

	private IResourceHistoryService _resourceHistoryService = SpringContextService.getBean( ResourceHistoryService.BEAN_SERVICE );
    private IStateService _stateService = SpringContextService.getBean( StateService.BEAN_SERVICE );
    private IResourceWorkflowService _resourceWorkflowService = SpringContextService.getBean( ResourceWorkflowService.BEAN_SERVICE );	
	private static AtomicBoolean _bIndexAppointmentIsRunning = new AtomicBoolean( false );
	private static AtomicBoolean _bIndexHistoryIsRunning = new AtomicBoolean( false );
	private static Queue<Integer> _queueAppointmentToIndex = new ConcurrentLinkedQueue< >(); 
	private static Queue<Integer> _queueAppointmentHistoryToIndex = new ConcurrentLinkedQueue< >(); 
	private static IndexingAppointmentService _instance;


    private static final int _nBatchSize= 100;

	
	/**
	 * Return an instance of Indexing Appointment Service
	 * @return instance of IndexingAppointmentService
	 */
	public static IndexingAppointmentService getService( ) {
		
		return ( _instance == null )? new IndexingAppointmentService():_instance ;
	}
	
	/**
	* index appointmentpartial data object and history data object 
	* @param appointmentDataSource the appointment DataSource
	* @param appointmentHistoryDataSource the appointment history DataSource
	* @param nIdAppointment the id of appointment to index
	*/
	public void indexAppointmentStateAndHistory( AppointmentDataSource appointmentDataSource, AppointmentHistoryDataSource appointmentHistoryDataSource, int nIdAppointment ) {    	
    	 
		if(  _bIndexHistoryIsRunning.compareAndSet( false, true ) ) 
		{
			( new Thread( )
	        {
	            @Override
	            public void run( )
	            {
				    try
				    {			    	
				    	if( _queueAppointmentHistoryToIndex.isEmpty( ) || _queueAppointmentHistoryToIndex.size() < _nBatchSize ) 
	    	        	{
						   	Appointment appt = AppointmentService.findAppointmentById( nIdAppointment );
						   	appt.setSlot(SlotService.findListSlotByIdAppointment( appt.getIdAppointment( ) ));
						    Form form= FormService.findFormLightByPrimaryKey(appt.getSlot().get(0).getIdForm( ));
						    AppointmentPartialDataObject appPartialData = new AppointmentPartialDataObject( nIdAppointment, _stateService.findByResource(nIdAppointment, Appointment.APPOINTMENT_RESOURCE_TYPE, form.getIdWorkflow()) );	       
				    		
						    DataSourceService.partialUpdate( appointmentDataSource, appPartialData.getId( ), appPartialData );          
					        DataSourceService.processIncrementalIndexing(new StringBuilder(), appointmentHistoryDataSource , getResourceHistoryDataObject( nIdAppointment,  appt, form  ));
					    }
				    	else 
	    	        	{	
						    indexListAppointmentAndHistory( appointmentDataSource, appointmentHistoryDataSource, _queueAppointmentHistoryToIndex, nIdAppointment );
	    	        	}
			        }
			        catch( ElasticClientException e )
			        {
			            AppLogService.error( "Error during ElasticDataAppointmentListener update partial appointment: " + e.getMessage( ), e );
			        }
				    finally
	                {
				    	_bIndexHistoryIsRunning.set( false );
	                	 if(!_queueAppointmentHistoryToIndex.isEmpty( )) {	                		 
	                		 indexAppointmentStateAndHistory( appointmentDataSource, appointmentHistoryDataSource,  _queueAppointmentHistoryToIndex.poll( ) );
	                		 
		                  }
	                }
	            }
	         } ).start( ); 
		}
		else if(!_queueAppointmentHistoryToIndex.contains( nIdAppointment ))
    	{
			_queueAppointmentHistoryToIndex.add( nIdAppointment );
    	}
    }
	/**
	 * Index appointment data object 
	 * @param appointmentDataSource the appointment Datasource
	 * @param nIdAppointment the id of appointment to index
	 */
	public void indexAppointment( AppointmentDataSource appointmentDataSource, int nIdAppointment  )
    {
    	if(  _bIndexAppointmentIsRunning.compareAndSet( false, true ) ) {
    		    		
    		( new Thread( )
	        {
	            @Override
	            public void run( )
	            {
	    	        try
	    	        {
	    	        	if( _queueAppointmentToIndex.isEmpty( ) || _queueAppointmentToIndex.size() < _nBatchSize ) 
	    	        	{
	    	        		
	    	        		DataSourceService.processIncrementalIndexing( appointmentDataSource, builAppointmentDataObject( nIdAppointment ) );
	    	        	}else 
	    	        	{	
	    	        		indexListAppointment( appointmentDataSource, _queueAppointmentToIndex, nIdAppointment );
	    	        	}
	    	        }
	    	        catch( ElasticClientException e )
	    	        {
	    	            AppLogService.error( "Error during ElasticDataAppointmentListener reindexSlot: " + e.getMessage( ), e );
	    	        }finally
	                {
	    	        	_bIndexAppointmentIsRunning.set( false );
	                	 if(!_queueAppointmentToIndex.isEmpty( )) {
	                		 
	                		 indexAppointment( appointmentDataSource, _queueAppointmentToIndex.poll() );
		                  }
	                }
	            }
	        } ).start( );
	    	
    	}else  if(!_queueAppointmentToIndex.contains( nIdAppointment ))
    	{
    		
    		_queueAppointmentToIndex.add( nIdAppointment );
    	}
    }

	/**
	 * Delete appointment in the index and his workflow history 
	 * @param appointmentDataSource the appointment DataSource
	 * @param appointmentHistoryDataSource the appointment history DataSource
	 * @param nIdAppointment the appointment id to remove in the index
	 */
	public void deleteAppointmentAndHistory( AppointmentDataSource appointmentDataSource, AppointmentHistoryDataSource appointmentHistoryDataSource, int nIdAppointment )
	{
	   try
	   {
	            DataSourceService.deleteById( appointmentDataSource, AppointmentSlotUtil.getAppointmentId( nIdAppointment, AppointmentSlotUtil.INSTANCE_NAME ) );
	            DataSourceService.deleteByQuery( appointmentHistoryDataSource, AppointmentSlotUtil.buildQueryIdResource( nIdAppointment ));
	   }
	   catch( ElasticClientException e )
	   {
	            AppLogService.error( "Error during ElasticDataAppointmentListener remove appointment: " + e.getMessage( ), e );
	   }

	 }
	/**
     * build data object to indexing
     * @param listIdDataObject the list of data object
     * @return the list of data object
     */
     public List<DataObject> buildDataObjects( List<Integer>  listIdDataObject ){
    	
    	Integer nIdstate;
        List<DataObject> collResult = new ArrayList<>( );
        Map<Integer, AppointmentForm> mapForm= new HashMap<>();
        AppointmentFilterDTO filter= new AppointmentFilterDTO( );
        filter.setListIdAppointment( listIdDataObject );
        Map<Integer, State> mapState= _stateService.getListStateByFilter(new StateFilter()).stream().collect(Collectors.toMap(State::getId, state -> state));
    	Map<Integer, Category> mapCategory= CategoryHome.findAllCategories().stream().collect(Collectors.toMap(Category::getIdCategory, cat -> cat));
        Map<Integer, Integer>  mapIdState= new  HashMap<>( );
        
        for(ReferenceItem ref: FormService.findAllInReferenceList()) {
        	
        	AppointmentFormDTO appointmentFormDTO= FormService.buildAppointmentFormWithoutReservationRule(Integer.parseInt( ref.getCode( )));
        	mapForm.put(appointmentFormDTO.getIdForm( ),new AppointmentForm (appointmentFormDTO, mapCategory.get(appointmentFormDTO.getIdCategory( ))));
        	mapIdState.putAll( _resourceWorkflowService.getListIdStateByListId( listIdDataObject, appointmentFormDTO.getIdWorkflow( ), Appointment.APPOINTMENT_RESOURCE_TYPE, appointmentFormDTO.getIdForm( )));
        }  
        List<Appointment> listAppointment= AppointmentService.findListAppointmentsByFilter(filter);
    	if( !CollectionUtils.isEmpty( listAppointment ))
	    {	        
		   for (Appointment appointment: listAppointment) {
		   
			   nIdstate= mapIdState.get(appointment.getIdAppointment( ));
			   AppointmentDataObject apptDataObject= new AppointmentDataObject( appointment, (nIdstate!=null)? mapState.get( nIdstate ):null, mapForm.get(appointment.getSlot().get(0).getIdForm( )) );
			   collResult.add( apptDataObject );
		   }
	    }
    	return  collResult;
     }
     /**
 	 * Build history workflow data objects
 	 * @param listIdDataObjects the list id resources/appointment
 	 * @return list DataObject
 	 */
 	public List<DataObject> buildHistoryWfDataObjects(List<Integer> listIdDataObjects) {
 		
 		  ResourceHistoryFilter filter= new ResourceHistoryFilter();
 		  filter.setListIdResources( listIdDataObjects );
 		  filter.setResourceType( Appointment.APPOINTMENT_RESOURCE_TYPE );
 		  List<DataObject> listResourceHistoryDataObject = new ArrayList<>();
 	      List<ResourceHistory> listFormResourceHistory  = _resourceHistoryService.getAllHistoryByFilter( filter );
 		  listResourceHistoryDataObject.addAll( getResourceHistoryListDataObject( listFormResourceHistory));			  
 	      	
 		return listResourceHistoryDataObject;
 	}
 	
     /**
      * build list of AppointmentHistoryDataObject object
      * 
      * @param listResourceHistory
      *            the resource workflow history

      * @return The list of AppointmentHistoryDataObject object
      */
     private List<AppointmentHistoryDataObject> getResourceHistoryListDataObject( List<ResourceHistory> listResourceHistory  )
     {   
         List<AppointmentHistoryDataObject> appointmentHistoryList = new ArrayList<>( );
     	if( CollectionUtils.isNotEmpty( listResourceHistory)) {
     		
 	    	Map<Integer, Form> listForm= FormService.findAllForms( ).stream().collect(Collectors.toMap(Form::getIdForm, form -> form));
 	        Map<Integer, List<ResourceHistory>> resourceHistoryByResource = listResourceHistory.stream().collect(Collectors.groupingBy(ResourceHistory::getIdResource));
 	        AppointmentFilterDTO filter= new AppointmentFilterDTO();
 	        filter.setListIdAppointment(new ArrayList<Integer>(resourceHistoryByResource.keySet( )));
 	        List<Appointment> listAppointment= AppointmentService.findListAppointmentsByFilter( filter );
 	    	Form form;
 	        Timestamp appointmentCreation;
 	        Timestamp appointmentPreviousActionCreation;
 	        Timestamp appointmentDate;
 	        if( !CollectionUtils.isEmpty( listAppointment ))
 	        {	        
 		        for (Appointment appointment: listAppointment) {
 		        	
 		        	List<ResourceHistory> listResourceHistorySorted= resourceHistoryByResource.get(appointment.getIdAppointment( ) ).stream().sorted( Comparator.comparing( ResourceHistory::getCreationDate) )
 		                    .collect( Collectors.toList( ) );
 		        	appointmentCreation= appointment.getAppointmentTakenSqlDate( );
 			        appointmentPreviousActionCreation = appointmentCreation;
 			        appointmentDate= Timestamp.valueOf(AppointmentUtilities.getStartingDateTime( appointment ));	
 		        	for(ResourceHistory resourceHistory: listResourceHistorySorted) {
 			
 			            AppointmentHistoryDataObject appointmentHistoryDataObject = new AppointmentHistoryDataObject( resourceHistory.getId( ) );
 			            form= listForm.get(appointment.getSlot().get(0).getIdForm());
 			            appointmentHistoryDataObject.setFormName( form.getTitle( ) );
 			            appointmentHistoryDataObject.setFormId( form.getIdForm( ) );
 			            appointmentHistoryDataObject.setAppointmentId( AppointmentSlotUtil.INSTANCE_NAME + "_" + resourceHistory.getIdResource( ) );
 			            appointmentHistoryDataObject.setTimestamp( resourceHistory.getCreationDate( ).getTime( ) );
 			            appointmentHistoryDataObject.setTaskDuration( duration( appointmentPreviousActionCreation, resourceHistory.getCreationDate( ) ) );
 			            appointmentHistoryDataObject.setAppointmentDuration( duration( appointmentCreation, resourceHistory.getCreationDate( ) ) );
 			            appointmentHistoryDataObject.setAppointmentDateActionDateDuration(duration( resourceHistory.getCreationDate( ), appointmentDate ));
 			            appointmentHistoryDataObject.setActionName( resourceHistory.getAction( ).getName( ) );
 			            appointmentHistoryDataObject.setCreationDate( resourceHistory.getCreationDate( ) );
 			            
 			            appointmentPreviousActionCreation = resourceHistory.getCreationDate( );
 			
 			            appointmentHistoryList.add( appointmentHistoryDataObject );
 		        	}
 		        }
 	        }
     	}
         return appointmentHistoryList;
     }

     /**
      * return The duration in milli
      * 
      * @param start
      *            The start time.
      * @param end
      *            The end time.
      * @return The duration in milli
      */
     private long duration( java.sql.Timestamp start, java.sql.Timestamp end )
     {
         return  end.getTime( ) - start.getTime( );
     }
	/**
     * Index list of appointment
     * @param queueApptToIndex the Queue of id appointment
     * @param nIdAppointment the id appointment
     * @throws ElasticClientException the Exception
     */
    private void indexListAppointment( AppointmentDataSource appointmentDataSource, Queue<Integer> queueApptToIndex, int nIdAppointment ) throws ElasticClientException {
    	
    	StringBuilder builder =new StringBuilder();
    	List<Integer> listIdappointment= new ArrayList<>( ); 
    	if(!queueApptToIndex.contains( nIdAppointment )) queueApptToIndex.add( nIdAppointment );
    	while (!queueApptToIndex.isEmpty() && listIdappointment.size( ) <= _nBatchSize) {
    		
    		listIdappointment.add(queueApptToIndex.poll());
    	}
    	DataSourceService.processIncrementalIndexing( builder , appointmentDataSource, buildDataObjects( listIdappointment ));
    	AppLogService.debug( builder.toString( ));
    }
    /**
     * Build appointment data object
     * @param nIdAppointment the appointment id
     */
    private AppointmentDataObject builAppointmentDataObject( int nIdAppointment )
    {
    	Appointment appointment = AppointmentService.findAppointmentById( nIdAppointment );
        appointment.setSlot(SlotService.findListSlotByIdAppointment( appointment.getIdAppointment( ) ));
        AppointmentFormDTO form= FormService.buildAppointmentFormWithoutReservationRule( appointment.getSlot().get(0).getIdForm( ));              
        Category category= ( form.getIdCategory() != 0 )?CategoryHome.findByPrimaryKey(form.getIdCategory( )):null;            
        return new AppointmentDataObject( appointment, AppointmentSlotUtil.getState( appointment.getIdAppointment( ), form.getIdWorkflow( ) ), new AppointmentForm ( form, category) ); 	
    }
    /**
     * build  AppointmentHistoryDataObject objects
     * @param nIdresource the id resource
     * @param appt the appointment obect
     * @return return an AppointmentHistoryDataObject object
     */
    private List<DataObject> getResourceHistoryDataObject(  int nIdresource, Appointment appt, Form form  )
    {   
        List<DataObject> appointmentHistoryList = new ArrayList<>( );
    	ResourceHistoryFilter filter= new ResourceHistoryFilter();
		filter.setListIdResources( Arrays.asList( nIdresource));
		filter.setResourceType( Appointment.APPOINTMENT_RESOURCE_TYPE );
	    filter.setIdWorkflow(form.getIdWorkflow( ));
    	List<ResourceHistory> listResourceHistory = _resourceHistoryService.getAllHistoryByFilter( filter ).stream().sorted( Comparator.comparing( ResourceHistory::getCreationDate )).collect( Collectors.toList( ) );		   	
		Timestamp appointmentPreviousActionCreation = appt.getAppointmentTakenSqlDate( );
		Timestamp appointmentDate= Timestamp.valueOf(AppointmentUtilities.getStartingDateTime( appt ));
    	for(ResourceHistory resourceHistory: listResourceHistory) {

            AppointmentHistoryDataObject appointmentHistoryDataObject = new AppointmentHistoryDataObject( resourceHistory.getId( ) );
            appointmentHistoryDataObject.setFormName( form.getTitle( ) );
            appointmentHistoryDataObject.setFormId( form.getIdForm( ) );
            appointmentHistoryDataObject.setAppointmentId( AppointmentSlotUtil.INSTANCE_NAME + "_" + resourceHistory.getIdResource( ) );
            appointmentHistoryDataObject.setTimestamp( resourceHistory.getCreationDate( ).getTime( ) );
            appointmentHistoryDataObject.setTaskDuration( duration( appointmentPreviousActionCreation, resourceHistory.getCreationDate( ) ) );
            appointmentHistoryDataObject.setAppointmentDuration( duration( appt.getAppointmentTakenSqlDate( ), resourceHistory.getCreationDate( ) ) );
            appointmentHistoryDataObject.setAppointmentDateActionDateDuration(duration( resourceHistory.getCreationDate( ), appointmentDate ));
            appointmentHistoryDataObject.setActionName( resourceHistory.getAction( ).getName( ) );
            appointmentHistoryDataObject.setCreationDate( resourceHistory.getCreationDate( ) );
            
            appointmentPreviousActionCreation = resourceHistory.getCreationDate( );

            appointmentHistoryList.add( appointmentHistoryDataObject );
    	}
		return appointmentHistoryList;
    }
    
    /**
	 * Index list appointment and their history workflow
	 * @param appointmentDataSource the appointment DataSource
	 * @param appointmentHistoryDataSource the appointment history DataSource
	 * @param queueAppointmentHistoryToIndex the queue
	 * @param nIdAppointment the id of appointment to index
	 * @param nIdAction the action id 
	 * @throws ElasticClientException the ElasticClientException
	 */
	private void indexListAppointmentAndHistory( AppointmentDataSource appointmentDataSource, AppointmentHistoryDataSource appointmentHistoryDataSource, Queue<Integer> queueAppointmentHistoryToIndex, int nIdAppointment ) throws ElasticClientException {
		
		StringBuilder builder =new StringBuilder();
    	List<Integer> listIdappointment= new ArrayList<>( ); 
    	queueAppointmentHistoryToIndex.add(nIdAppointment  );
  
    	while (!queueAppointmentHistoryToIndex.isEmpty() && listIdappointment.size( ) <= _nBatchSize) {
    		
    		listIdappointment.add( queueAppointmentHistoryToIndex.poll( ) );

    	}
    	DataSourceService.processIncrementalIndexing( builder , appointmentDataSource , buildDataObjects( listIdappointment ));
    	DataSourceService.processIncrementalIndexing( builder , appointmentHistoryDataSource , buildHistoryWfDataObjects( listIdappointment ));    	
    	AppLogService.debug( builder.toString( ));
	}
}
