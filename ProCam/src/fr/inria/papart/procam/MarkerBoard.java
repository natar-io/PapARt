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
import processing.core.PMatrix3D;

/**
 *
 * @author jeremylaviole
 */
public class MarkerBoard {

    private String fileName, name;
    protected int width;
    protected int height;
    protected ArrayList<Camera> cameras;
    protected ArrayList<float[]> transfos;
    protected ArrayList<ARToolKitPlus.MultiTracker> trackers;
    
    public MarkerBoard(String fileName, String name, int width, int height) {
        this.fileName = fileName;
        this.width = width;
        this.height = height;
        this.name = name;

        cameras = new ArrayList<Camera>();
        transfos = new ArrayList<float[]>();
        trackers = new ArrayList<ARToolKitPlus.MultiTracker>();
    }

    // Legacy ?!
//    public void setTracker(MultiTracker tracker, float[] transfo) {
//        addTracker(tracker, transfo);
//    }
//    
    public void addTracker(Camera camera, MultiTracker tracker, float[] transfo) {
        this.trackers.add(tracker);
        this.transfos.add(transfo);
        cameras.add(camera);
    }

//    public MultiTracker getTracker() {
//        return this.tracker;
//    }
    public synchronized void updatePosition(Camera camera, IplImage img) {


        int id = cameras.indexOf(camera);

        ARToolKitPlus.MultiTracker tracker = trackers.get(id);
        float transfo[] = transfos.get(id);

        
        tracker.calc(img.imageData());

        if (tracker.getNumDetectedMarkers() <= 0) {
            return;
        }

        ARToolKitPlus.ARMultiMarkerInfoT multiMarkerConfig = tracker.getMultiMarkerConfig();
        for (int i = 0; i < 12; i++) {
            transfo[i] = (float) multiMarkerConfig.trans().get(i);
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
