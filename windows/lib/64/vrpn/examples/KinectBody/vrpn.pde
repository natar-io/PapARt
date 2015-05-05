import vrpn.*;
import vrpn.TrackerRemote;
import vrpn.TrackerRemote.*;

TrackerRemote m_tracker = null;
TrackerRemoteListener m_trackerListener = null;

PVector trackerPos = new PVector();

void initVrpn(){

    String trackerName = "Tracker0@localhost";
    TrackerRemote tracker = null;

    try
	{
	    tracker = new TrackerRemote( trackerName, null, null, null, null );
	}
    catch( InstantiationException e )
	{
	    // do something b/c you couldn't create the tracker
	    System.out.println( "We couldn't connect to tracker " + trackerName + "." );
	}
    
    TrackerTest test = new TrackerTest( );
    tracker.addPositionChangeListener( test );
    // tracker.addVelocityChangeListener( test );
    // tracker.addAccelerationChangeListener( test );

}


class TrackerTest 
	implements TrackerRemote.PositionChangeListener,
	TrackerRemote.VelocityChangeListener,
	TrackerRemote.AccelerationChangeListener

{
	public void trackerPositionUpdate( TrackerRemote.TrackerUpdate u,
									   TrackerRemote tracker )
	{

	    // head with FAAST Kinect VRPN 
	    if(u.sensor == 0){
		//		println("Sensor");
		trackerPos.x = (float) u.pos[0];
		trackerPos.y = -(float) u.pos[1];
		trackerPos.z = (float) u.pos[2];	
	    }

	    body[u.sensor].x = (float) u.pos[0];
	    body[u.sensor].y = -(float) u.pos[1];
	    body[u.sensor].z = (float) u.pos[2];


	}
	
	public void trackerVelocityUpdate( TrackerRemote.VelocityUpdate v,
									   TrackerRemote tracker )
	{
	}
	
	public void trackerAccelerationUpdate( TrackerRemote.AccelerationUpdate a,
										   TrackerRemote tracker )
	{
	}
	

}
