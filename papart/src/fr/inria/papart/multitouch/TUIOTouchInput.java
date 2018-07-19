/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.multitouch;

import fr.inria.papart.procam.display.BaseDisplay;

import TUIO.*;
import fr.inria.papart.multitouch.detection.TouchDetection;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import fr.inria.papart.procam.PaperScreen;
import processing.core.PApplet;
import java.util.*;
import processing.core.PVector;

/**
 *
 * @author Jeremy Laviole
 */
public class TUIOTouchInput extends TouchInput {

    private final TuioProcessing tuioClient;
    private final PApplet parent;
    private final HashMap<Integer, TrackedElement> tuioObjects = new HashMap<>();
    private final HashMap<Integer, TrackedElement> tuioCursors = new HashMap<>();

    private PaperScreen paperScreen;
    private BaseDisplay display;
    private boolean useDisplay = false;
    private boolean useScreen = false;

    public TUIOTouchInput(PApplet parent, BaseDisplay display, int port) {
        tuioClient = new TuioProcessing(parent, this, port);
        this.parent = parent;
        this.useDisplay = true;
        useDisplay(display);
    }

    public TUIOTouchInput(PApplet parent, PaperScreen screen, int port) {
        tuioClient = new TuioProcessing(parent, this, port);
        this.parent = parent;
        useScreen(screen);
    }

    private void useDisplay(BaseDisplay display) {
        useDisplay = true;
        useScreen = false;
        this.display = display;
    }

    private void useScreen(PaperScreen screen) {
        useDisplay = false;
        useScreen = true;
        this.paperScreen = screen;
    }

    @Override
    public void update() {
    }

    private int getCursorID(TuioCursor tcur) {
        return tcur.getCursorID() + 1;
    }

    @Override
    public TouchList projectTouchToScreen(PaperScreen screen, BaseDisplay display) {
        // TODO: check disp$lay ? 
        TouchList touchList = new TouchList();

        Vector<TuioCursor> tuioCursorList = tuioClient.getTuioCursors();
        for (TuioCursor tcur : tuioCursorList) {
            try {
                TuioPoint tuioPoint = tcur.getPosition();
                PVector v = display.project(screen, tuioPoint.getX(), tuioPoint.getY());
                v.x = v.x * screen.getDrawingSize().x;
                v.y = v.y * screen.getDrawingSize().y;

                Touch touch = new Touch();
                touch.setPosition(v);
                touch.id = getCursorID(tcur);
                touchList.add(touch);
            } catch (Exception e) {
                System.out.println("No Intersection" + e);
            }
        }

        Vector<TuioObject> tuioObjectList = tuioClient.getTuioObjects();
        for (TuioObject tobj : tuioObjectList) {
            try {
                TuioPoint tuioPoint = tobj.getPosition();
                PVector v = display.project(screen, tuioPoint.getX(), tuioPoint.getY());
                v.x = v.x * screen.getDrawingSize().x;
                v.y = v.y * screen.getDrawingSize().y;

                Touch touch = new Touch();
                touch.setPosition(v);
                touch.id = tobj.getSymbolID();
                touch.isObject = true;
                touchList.add(touch);
            } catch (Exception e) {
                System.out.println("No Intersection" + e);
            }
        }

        return touchList;
    }

    public TouchList getTouch() {
        TouchList touchList = new TouchList();
        for (TrackedElement te : tuioObjects.values()) {

            Touch touch = te.getTouch();
            touch.position = te.getPosition();
            touch.pposition = te.getPreviousPosition();
            touch.speed = te.getSpeed();
            touchList.add(touch);
        }
        for (TrackedElement te : tuioCursors.values()) {
            Touch touch = te.getTouch();
            touch.position = te.getPosition();
            touch.pposition = te.getPreviousPosition();
            touch.speed = te.getSpeed();
            touchList.add(touch);
        }
        return touchList;
    }

// these callback methods are called whenever a TUIO event occurs
// called when an object is added to the scene
    public void addTuioObject(TuioObject tobj) {
        tuioObjects.put(tobj.getSymbolID(), createTouchPointFrom(tobj));
    }

    // called when a cursor is added to the scene
    public void addTuioCursor(TuioCursor tcur) {
        tuioCursors.put(getCursorID(tcur), createTouchPointFrom(tcur));
    }

    private TrackedElement createTouchPointFrom(TuioObject tObj) {
        TrackedElement tp = new TrackedElement();
        tp.setCreationTime(parent.millis());
        tp.forceID(tObj.getSymbolID());
        PVector v = getLocalPosition(tObj.getPosition());
        tp.setPosition(v);
        return tp;
    }

    private TrackedElement createTouchPointFrom(TuioCursor tcur) {
        TrackedElement tp = new TrackedElement();
        tp.setCreationTime(parent.millis());
        tp.forceID(getCursorID(tcur));
        PVector v = getLocalPosition(tcur.getPosition());
        tp.setPosition(v);
        return tp;
    }

    public void updateTuioObject(TuioObject tobj) {
        TrackedElement tp = createTouchPointFrom(tobj);
        TrackedElement known = tuioObjects.get(tobj.getSymbolID());
             known.setUpdated(false);
        known.updateWith(tp);
    }

    public void updateTuioCursor(TuioCursor tcur) {
        TrackedElement touchPoint = createTouchPointFrom(tcur);
        TrackedElement known = tuioCursors.get(getCursorID(tcur));
        known.setUpdated(false);
        boolean up = known.updateWith(touchPoint);
    }

    private PVector getLocalPosition(TuioPoint tuioPoint) {
        PVector v = new PVector(tuioPoint.getX(), tuioPoint.getY());
        if (useScreen) {
            v.x = v.x * paperScreen.getDrawingSize().x;
            v.y = v.y * paperScreen.getDrawingSize().y;
            System.out.println("Local: " + v);
        }
        return v;
    }

    public void removeTuioObject(TuioObject tobj) {
        int id = tobj.getSymbolID();

        tuioObjects.get(tobj.getSymbolID()).delete(0);
        tuioObjects.remove(id);
//        System.out.println("remove object " + tobj.getSymbolID() + " (" + tobj.getSessionID() + ")");
    }

// called when a cursor is removed from the scene
    public void removeTuioCursor(TuioCursor tcur) {
        tuioCursors.get(getCursorID(tcur)).delete(0);
        tuioCursors.remove(getCursorID(tcur));
        //   System.out.println("remove cursor " + getCursorID(tcur) + " (" + tcur.getSessionID() + ")");
    }

// called after each message bundle
// representing the end of an image frame
    public void refresh(TuioTime bundleTime) {
    }

    public Touch projectTouch(PaperScreen paperScreen, BaseDisplay display, TrackedElement e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TouchList projectTouch(PaperScreen paperScreen, BaseDisplay display, TouchDetection td) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
