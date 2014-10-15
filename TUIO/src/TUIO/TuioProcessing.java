/*
	TUIO processing library - part of the reacTIVision project
	http://reactivision.sourceforge.net/

	Copyright (c) 2005-2009 Martin Kaltenbrunner <mkalten@iua.upf.edu>

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package TUIO;

import java.awt.event.*;
import java.lang.reflect.*;
import processing.core.*;
import java.util.*;

public class TuioProcessing implements TuioListener {
		
	private Object parent;
	private Method addTuioObject, removeTuioObject, updateTuioObject, addTuioCursor, removeTuioCursor, updateTuioCursor, refresh;
	private TuioClient client;
			
	public TuioProcessing(PApplet applet) {
		this(applet, applet, 3333);
	}
	
        public TuioProcessing(PApplet applet, Object parent) {
		this(applet, parent, 3333);
	}
	
	public TuioProcessing(PApplet applet, Object parent, int port) {
		this.parent = parent;
		applet.registerMethod("dispose", this); 
		
		try { refresh = parent.getClass().getMethod("refresh",new Class[] { TuioTime.class } ); }
		catch (Exception e) { 
			System.out.println("TUIO: missing or wrong 'refresh(TuioTime bundleTime)' method implementation");
			refresh = null;
		}
		
		try { addTuioObject = parent.getClass().getMethod("addTuioObject", new Class[] { TuioObject.class }); }
		catch (Exception e) { 
			System.out.println("TUIO: missing or wrong 'addTuioObject(TuioObject tobj)' method implementation");
			addTuioObject = null;
		}
		
		try { removeTuioObject = parent.getClass().getMethod("removeTuioObject", new Class[] { TuioObject.class }); }
		catch (Exception e) { 
			System.out.println("TUIO: missing or wrong 'removeTuioObject(TuioObject tobj)' method implementation");
			removeTuioObject = null;
		}
		
		try { updateTuioObject = parent.getClass().getMethod("updateTuioObject", new Class[] { TuioObject.class }); }
		catch (Exception e) { 
			System.out.println("TUIO: missing or wrong 'updateTuioObject(TuioObject tobj)' method implementation");
			updateTuioObject = null;
		}
		
		try { addTuioCursor = parent.getClass().getMethod("addTuioCursor", new Class[] { TuioCursor.class }); }
		catch (Exception e) { 
			System.out.println("TUIO: missing or wrong 'addTuioCursor(TuioCursor tcur)' method implementation");
			addTuioCursor = null;
		}
		
		try { removeTuioCursor = parent.getClass().getMethod("removeTuioCursor", new Class[] { TuioCursor.class }); }
		catch (Exception e) { 
			System.out.println("TUIO:missing or wrong 'removeTuioCursor(TuioCursor tcur)' method implementation");
			removeTuioCursor = null;
		}
		
		try { updateTuioCursor = parent.getClass().getMethod("updateTuioCursor", new Class[] { TuioCursor.class }); }
		catch (Exception e) { 
			System.out.println("TUIO: missing or wrong 'updateTuioCursor(TuioCursor tcur)' method implementation");
			updateTuioCursor = null;
		}
		
		client = new TuioClient(port);
		client.addTuioListener(this);
		client.connect();
	}

	public void addTuioObject(TuioObject tobj) {
		if (addTuioObject!=null) {
			try { 
				addTuioObject.invoke(parent, new Object[] { tobj });
			}
			catch (IllegalAccessException e) {}
			catch (IllegalArgumentException e) {}
			catch (InvocationTargetException e) {}
		}
	}
	
	public void updateTuioObject(TuioObject tobj) {
		
		if (updateTuioObject!=null) {
			try { 
				updateTuioObject.invoke(parent, new Object[] { tobj });
			}
			catch (IllegalAccessException e) {}
			catch (IllegalArgumentException e) {}
			catch (InvocationTargetException e) {}
		}
	}
	
	public void removeTuioObject(TuioObject tobj) {
		if (removeTuioObject!=null) {
			try { 
				removeTuioObject.invoke(parent, new Object[] { tobj });
			}
			catch (IllegalAccessException e) {}
			catch (IllegalArgumentException e) {}
			catch (InvocationTargetException e) {}
		}
	}
	
	public void addTuioCursor(TuioCursor tcur) {
		if (addTuioCursor!=null) {
			try { 
				addTuioCursor.invoke(parent, new Object[] { tcur });
			}
			catch (IllegalAccessException e) {}
			catch (IllegalArgumentException e) {}
			catch (InvocationTargetException e) {}
		}
	}
	
	public void updateTuioCursor(TuioCursor tcur) {
		if (updateTuioCursor!=null) {
			try { 
				updateTuioCursor.invoke(parent, new Object[] { tcur });
			}
			catch (IllegalAccessException e) {}
			catch (IllegalArgumentException e) {}
			catch (InvocationTargetException e) {}
		}
	}
	
	public void removeTuioCursor(TuioCursor tcur) {
		if (removeTuioCursor!=null) {
			try { 
				removeTuioCursor.invoke(parent, new Object[] { tcur });
			}
			catch (IllegalAccessException e) {}
			catch (IllegalArgumentException e) {}
			catch (InvocationTargetException e) {}
		}
	}
	
	public void refresh(TuioTime bundleTime) {
		if (refresh!=null) {
			try { 
				refresh.invoke(parent,new Object[] { bundleTime });
			}
			catch (IllegalAccessException e) {}
			catch (IllegalArgumentException e) {}
			catch (InvocationTargetException e) {}
		}
	}
	
	public Vector getTuioObjects() {
		return client.getTuioObjects();
	}
	
	public Vector getTuioCursors() {
		return client.getTuioCursors();
	}	
	
	public TuioObject getTuioObject(long s_id) {
		return client.getTuioObject(s_id);
	}
	
	public TuioCursor getTuioCursor(long s_id) {
		return client.getTuioCursor(s_id);
	}	
		
	public void pre() {
		//method that's called just after beginFrame(), meaning that it 
		//can affect drawing.
	}

	public void draw() {
		//method that's called at the end of draw(), but before endFrame().
	}
	
	public void mouseEvent(MouseEvent e) {
		//called when a mouse event occurs in the parent applet
	}
	
	public void keyEvent(KeyEvent e) {
		//called when a key event occurs in the parent applet
	}
	
	public void post() {
		//method called after draw has completed and the frame is done.
		//no drawing allowed.
	}
	
	public void size(int width, int height) {
		//this will be called the first time an applet sets its size, but
		//also any time that it's called while the PApplet is running.
	}
	
	public void stop() {
		//can be called by users, for instance movie.stop() will shut down
		//a movie that's being played, or camera.stop() stops capturing 
		//video. server.stop() will shut down the server and shut it down
		//completely, which is identical to its "dispose" function.
	}
	
	public void dispose() {
	
		if (client.isConnected()) client.disconnect();
	
		//this should only be called by PApplet. dispose() is what gets 
		//called when the host applet is stopped, so this should shut down
		//any threads, disconnect from the net, unload memory, etc. 
	}
}
