/* 
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.inria.papart.multitouch;

import fr.inria.papart.procam.BaseDisplay;
import fr.inria.papart.procam.Screen;
import TUIO.*;
import processing.core.PApplet;
import java.util.*;
import processing.core.PVector;

/**
 *
 * @author jiii
 */
public class TUIOTouchInput extends TouchInput {

    private final TuioProcessing tuioClient;

    public TUIOTouchInput(PApplet parent, int port) {
        tuioClient = new TuioProcessing(parent, this, port);
    }

    @Override
    public void update() {
    }

    @Override
    public TouchList projectTouchToScreen(Screen screen, BaseDisplay display) {
        TouchList touchList = new TouchList();

        Vector<TuioCursor> tuioCursorList = tuioClient.getTuioCursors();
        for (TuioCursor tuioCursor : tuioCursorList) {
            try {
                Touch touch = createCursor(screen, display, tuioCursor);
                touchList.add(touch);
            } catch (Exception e) {
                System.out.println("No Intersection" + e);
            }
        }

        Vector<TuioObject> tuioObjectList = tuioClient.getTuioObjects();
        for (TuioObject tuioObject : tuioObjectList){
            try {
                Touch touch = createObject(screen, display, tuioObject);
                touchList.add(touch);
            } catch (Exception e) {
                System.out.println("No Intersection" + e);
            }
        }

        return touchList;
    }

    private Touch createCursor(Screen screen, BaseDisplay display, TuioCursor tcur) throws Exception {
        Touch touch = new Touch();
        TuioPoint tuioPoint = tcur.getPosition();
        PVector v = project(screen, display, tuioPoint.getX(), tuioPoint.getY());
        touch.setPosition(v);
        return touch;
    }
    
    private Touch createObject(Screen screen, BaseDisplay display, TuioObject tobj) throws Exception {
        Touch touch = new Touch();
        TuioPoint tuioPoint = tobj.getPosition();
        PVector v = project(screen, display, tuioPoint.getX(), tuioPoint.getY());
        touch.setPosition(v);
        touch.isObject = true;
        touch.id = tobj.getSymbolID();
        // TODO: implement this ?
        touch.size = new PVector(10, 10);
        return touch;
    }

// these callback methods are called whenever a TUIO event occurs
// called when an object is added to the scene
    void addTuioObject(TuioObject tobj) {
        System.out.println("add object " + tobj.getSymbolID() + " (" + tobj.getSessionID() + ") " + tobj.getX() + " " + tobj.getY() + " " + tobj.getAngle());
    }

// called when an object is removed from the scene
    void removeTuioObject(TuioObject tobj) {
        System.out.println("remove object " + tobj.getSymbolID() + " (" + tobj.getSessionID() + ")");
    }

// called when an object is moved
    void updateTuioObject(TuioObject tobj) {
        System.out.println("update object " + tobj.getSymbolID() + " (" + tobj.getSessionID() + ") " + tobj.getX() + " " + tobj.getY() + " " + tobj.getAngle()
                + " " + tobj.getMotionSpeed() + " " + tobj.getRotationSpeed() + " " + tobj.getMotionAccel() + " " + tobj.getRotationAccel());
    }

// called when a cursor is added to the scene
    void addTuioCursor(TuioCursor tcur) {
        System.out.println("add cursor " + tcur.getCursorID() + " (" + tcur.getSessionID() + ") " + tcur.getX() + " " + tcur.getY());
    }

// called when a cursor is moved
    void updateTuioCursor(TuioCursor tcur) {
        System.out.println("update cursor " + tcur.getCursorID() + " (" + tcur.getSessionID() + ") " + tcur.getX() + " " + tcur.getY()
                + " " + tcur.getMotionSpeed() + " " + tcur.getMotionAccel());
    }

// called when a cursor is removed from the scene
    void removeTuioCursor(TuioCursor tcur) {
        System.out.println("remove cursor " + tcur.getCursorID() + " (" + tcur.getSessionID() + ")");
    }

// called after each message bundle
// representing the end of an image frame
    void refresh(TuioTime bundleTime) {
    }
}
