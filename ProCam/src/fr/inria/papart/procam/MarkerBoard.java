/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import com.googlecode.javacv.cpp.ARToolKitPlus;
import com.googlecode.javacv.cpp.ARToolKitPlus.MultiTracker;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import processing.core.PMatrix3D;

/**
 *
 * @author jeremylaviole
 */
public class MarkerBoard {

    private String fileName, name;
    protected int width;
    protected int height;
    protected float[] transfo;
    ARToolKitPlus.MultiTracker tracker;

    public MarkerBoard(String fileName, String name, int width, int height) {
        this.fileName = fileName;
        this.width = width;
        this.height = height;
        this.name = name;

    }

    public void setTracker(MultiTracker tracker, float[] transfo) {
        this.tracker = tracker;
        this.transfo = transfo;
    }

    public MultiTracker getTracker() {
        return this.tracker;
    }

    public void updatePosition(IplImage img) {
        tracker.calc(img.imageData());

        if (tracker.getNumDetectedMarkers() <= 0) {
            return;
        }

        ARToolKitPlus.ARMultiMarkerInfoT multiMarkerConfig = tracker.getMultiMarkerConfig();
        for (int i = 0; i < 12; i++) {
            transfo[i] = (float) multiMarkerConfig.trans().get(i);
        }
    }

    public float[] getTransfo() {
        return this.transfo;
    }

    public PMatrix3D getTransfoMat() {
        return new PMatrix3D(transfo[0], transfo[1], transfo[2], transfo[3],
                transfo[4], transfo[5], transfo[6], transfo[7],
                transfo[8], transfo[9], transfo[10], transfo[11],
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
