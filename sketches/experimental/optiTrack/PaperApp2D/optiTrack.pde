import vrpn.*;

TrackerRemote m_tracker = null;
TrackerRemoteListener m_trackerListener = null;

PVector trackerPos = new PVector();

void initVrpn() {

  String trackerName = "Tracker0@127.0.0.1:3883";
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
   tracker.addVelocityChangeListener( test );
  tracker.addAccelerationChangeListener( test );
}

class TrackerTest 
implements TrackerRemote.PositionChangeListener, 
TrackerRemote.VelocityChangeListener, 
TrackerRemote.AccelerationChangeListener

{
  public void trackerPositionUpdate( TrackerRemote.TrackerUpdate u, 
  TrackerRemote tracker )
  {
    
    if (u.sensor == 0) {
      // BeginDraw3D version ... 
      trackerPos.x = -1000 * (float) u.pos[0];
      trackerPos.y = -(1000 * (float) u.pos[1]) + 210f;
      trackerPos.z = 1000 *(float) u.pos[2];
      
            // BeginDraw2D version ... 
//      trackerPos.x = -1000 * (float) u.pos[0];
//      trackerPos.y = 1000 * (float) u.pos[1];
//      trackerPos.z = 1000 *(float) u.pos[2];
      
      println("tracker " + trackerPos);
       quatToMatrix(-(float) u.quat[2], -(float) u.quat[1], (float) u.quat[0], (float) u.quat[3]);

    }

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

