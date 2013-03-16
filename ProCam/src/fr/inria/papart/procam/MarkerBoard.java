/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import com.googlecode.javacv.cpp.ARToolKitPlus;
import com.googlecode.javacv.cpp.ARToolKitPlus.MultiTracker;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 *
 * @author jeremylaviole
 */
public class MarkerBoard {

    private String fileName, name;
    protected int width;
    protected int height;
    protected ArrayList<Camera> cameras;
    protected ArrayList<Boolean> drawingMode;
    protected ArrayList<Float> minDistanceDrawingMode;
    protected ArrayList<float[]> transfos;
    protected ArrayList<ARToolKitPlus.MultiTracker> trackers;
    protected ArrayList<OneEuroFilter[]> filters;
    protected ArrayList<PVector> lastPos;
    protected ArrayList<Integer> nextTimeEvent;
    protected ArrayList<Integer> updateStatus;
    private PApplet applet;
    private static final int updateTime = 1000; // 1 sec
    static public final int NORMAL = 1;
    static public final int BLOCK_UPDATE = 2;
    static public final int FORCE_UPDATE = 3;

    public MarkerBoard(String fileName, String name, int width, int height) {
        this.fileName = fileName;
        this.width = width;
        this.height = height;
        this.name = name;

        cameras = new ArrayList<Camera>();
        transfos = new ArrayList<float[]>();
        trackers = new ArrayList<ARToolKitPlus.MultiTracker>();
        filters = new ArrayList<OneEuroFilter[]>();
        drawingMode = new ArrayList<Boolean>();
        minDistanceDrawingMode = new ArrayList<Float>();
        lastPos = new ArrayList<PVector>();
        nextTimeEvent = new ArrayList<Integer>();
        updateStatus = new ArrayList<Integer>();

    }

    // Legacy ?!
//    
//    public void addTracker(Camera camera, MultiTracker tracker, float[] transfo) {
//        addTracker(camera, tracker, transfo, true);
//    }
    public void addTracker(PApplet applet, Camera camera, MultiTracker tracker, float[] transfo) {

        this.applet = applet;

        this.trackers.add(tracker);
        this.transfos.add(transfo);
        this.cameras.add(camera);

        this.drawingMode.add(false);
        this.lastPos.add(new PVector());
        this.minDistanceDrawingMode.add(2f);
        this.nextTimeEvent.add(0);
        this.updateStatus.add(NORMAL);

        OneEuroFilter[] filter = null;
        this.filters.add(filter);
    }

    private int getId(Camera camera) {
        return cameras.indexOf(camera);
    }

    public void setFiltering(Camera camera, double freq, double minCutOff) {
        int id = cameras.indexOf(camera);
        OneEuroFilter[] filter = createFilter(freq, minCutOff);
        filters.set(id, filter);
    }

    public void setDrawingMode(Camera camera, boolean dm) {
        setDrawingMode(camera, dm, 2);
    }

    public void setDrawingMode(Camera camera, boolean dm, float dist) {
        int id = getId(camera);

        drawingMode.set(id, dm);
        minDistanceDrawingMode.set(id, dist);
    }

    private OneEuroFilter[] createFilter(double freq, double minCutOff) {
        OneEuroFilter[] f = new OneEuroFilter[12];
        try {
            for (int i = 0; i < 12; i++) {
                f[i] = new OneEuroFilter(freq);
                f[i].setFrequency(freq);
                f[i].setMinCutoff(minCutOff);
            }
        } catch (Exception e) {
            System.out.println("Filter init error" + e);
        }

        return f;
    }

//    public MultiTracker getTracker() {
//        return this.tracker;
//    }
    public void forceUpdate(Camera camera, int time) {
        int id = getId(camera);
        nextTimeEvent.set(id, applet.millis() + time);
        updateStatus.set(id, FORCE_UPDATE);
    }

    public void blockUpdate(Camera camera, int time) {
        int id = getId(camera);
        nextTimeEvent.set(id, applet.millis() + time);
        updateStatus.set(id, BLOCK_UPDATE);
    }

    public synchronized void updatePosition(Camera camera, IplImage img) {

        int id = cameras.indexOf(camera);

        ARToolKitPlus.MultiTracker tracker = trackers.get(id);

        int currentTime = applet.millis();
        int endTime = nextTimeEvent.get(id);
        int mode = updateStatus.get(id);

        // If the update is still blocked
        if (mode == BLOCK_UPDATE && currentTime < endTime) {
            return;
        }

        // Find the markers
        tracker.calc(img.imageData());

        if (tracker.getNumDetectedMarkers() <= 0) {
            return;
        }


        ARToolKitPlus.ARMultiMarkerInfoT multiMarkerConfig = tracker.getMultiMarkerConfig();

        // if the update is forced 
        if (mode == FORCE_UPDATE && currentTime < endTime) {
            update(multiMarkerConfig, id);
            return;
        }

        // the force and block updates are finished, revert back to normal
        if (mode == FORCE_UPDATE || mode == BLOCK_UPDATE && currentTime > endTime) {
            updateStatus.set(id, NORMAL);
        }

        PVector currentPos = new PVector((float) multiMarkerConfig.trans().get(3),
                (float) multiMarkerConfig.trans().get(7),
                (float) multiMarkerConfig.trans().get(11));

//        System.out.println("Current Pos " + currentPos);
//        System.out.println("Distance " + currentPos.dist(lastPos.get(id)));

        float distance = currentPos.dist(lastPos.get(id));

        // if it is a drawing mode
        if (drawingMode.get(id)) {

            if (distance > this.minDistanceDrawingMode.get(id)) {
                update(multiMarkerConfig, id);

                lastPos.set(id, currentPos);
                updateStatus.set(id, FORCE_UPDATE);
                nextTimeEvent.set(id, applet.millis() + MarkerBoard.updateTime);
//                    System.out.println("Next Update for x seconds");
            }

        } else {
            update(multiMarkerConfig, id);
        }

//        Arrays.copyOf(transfo, width)

    }

    private void update(ARToolKitPlus.ARMultiMarkerInfoT multiMarkerConfig, int id) {

        float transfo[] = transfos.get(id);
        OneEuroFilter filter[] = filters.get(id);

        if (filter == null) {
            for (int i = 0; i < 12; i++) {
                transfo[i] = (float) multiMarkerConfig.trans().get(i);
            }
        } else {
            try {
                for (int i = 0; i < 12; i++) {
                    float v = (float) multiMarkerConfig.trans().get(i);
                    transfo[i] = (float) filter[i].filter(v);
                }
            } catch (Exception e) {
                System.out.println("Filtering error " + e);
            }
        }
    }

    public float[] getTransfo(Camera camera) {
        return this.transfos.get(cameras.indexOf(camera));
    }

    public PMatrix3D getTransfoMat(Camera camera) {
        float[] t = getTransfo(camera);
        return new PMatrix3D(t[0], t[1], t[2], t[3],
                t[4], t[5], t[6], t[7],
                t[8], t[9], t[10], t[11],
                0, 0, 0, 1);
    }

    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return "MarkerBoard " + getName();
    }
}
