/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2017 RealityTech
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
package fr.inria.papart.multitouch.detection;

import fr.inria.papart.calibration.files.PlanarTouchCalibration;
import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.TouchList;
import static fr.inria.papart.multitouch.detection.CalibratedColorTracker.colorFinderLAB;
import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.camera.TrackedView;
import fr.inria.papart.utils.MathUtils;
import java.util.ArrayList;
import processing.core.PConstants;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class ColorTracker {

    protected PaperScreen paperScreen;
    protected final TrackedView trackedView;

    protected PImage capturedImage;

//    protected final HashMap<String, Integer> trackedColors;
    protected TouchDetectionColor touchDetectionColor;
    protected byte[] colorFoundArray;
    protected final ArrayList<TrackedElement> trackedElements;
    protected float scale = 1f;

    protected ColorReferenceThresholds reference = new ColorReferenceThresholds();
    protected String name;
    private final PlanarTouchCalibration calibration;

    protected ColorTracker() {
        trackedElements = new ArrayList<TrackedElement>();
        trackedView = null;
        calibration = null;
    }

    public ColorTracker(PaperScreen paperScreen) {
        this(paperScreen, 1);
    }

    public ColorTracker(PaperScreen paperScreen, PlanarTouchCalibration calibration) {
        this(paperScreen, calibration, 1);
    }

    /**
     * Color Tracker that find zones of a given color.
     *
     * @param paperScreen
     * @param scale, number of pixels per millimiters.
     */
    public ColorTracker(PaperScreen paperScreen, float scale) {
        this(paperScreen, Papart.getPapart().getDefaultColorTouchCalibration(), scale);
    }

    /**
     * Color Tracker that find zones of a given color.
     *
     * @param paperScreen
     * @param calibration, zone parameters.
     * @param scale, number of pixels per millimeters.
     */
    public ColorTracker(PaperScreen paperScreen, PlanarTouchCalibration calibration, float scale) {
        this.paperScreen = paperScreen;

        this.calibration = calibration;
        this.trackedView = new TrackedView(paperScreen);
        this.trackedView.setScale(scale);
        trackedView.init();
        this.scale = scale;

        initTouchDetection();

//        this.trackedColors = new HashMap<>();
        trackedElements = new ArrayList<TrackedElement>();

        reference.hue = 40;
        reference.saturation = 70;
        reference.brightness = 80;
        reference.redThreshold = 15;
    }

    /**
     * Call this if you modify the trackedView
     */
    public void initTouchDetection() {
//        SimpleSize size = new SimpleSize(paperScreen.getDrawingSize().copy().mult(scale));
        touchDetectionColor = new TouchDetectionColor(trackedView);

        calibration.setMaximumDistance(calibration.getMaximumDistance() * scale);
        calibration.setMinimumComponentSize((int) (calibration.getMinimumComponentSize() * scale * scale)); // Quadratic (area)
        calibration.setSearchDepth((int) (calibration.getSearchDepth() * scale));
        calibration.setTrackingMaxDistance(calibration.getTrackingMaxDistance() * scale);
        calibration.setMaximumRecursion((int) (calibration.getMaximumRecursion() * scale));

        touchDetectionColor.setCalibration(calibration);

        colorFoundArray = touchDetectionColor.createInputArray();
    }

    public TrackedView getTrackedView() {
        return trackedView;
    }

    public ArrayList<TrackedElement> findColor(int time) {
        return findColor(name, time, reference.erosion);
    }

    protected int lastImageTime = 0;

    /**
     * For now it only finds one color.
     *
     * @param name can be "red", "blue" or something else to disable this
     * matching.
     * @param time currernt time in Processing.
     * @param erosion Erosion to apply before the tracking.
     * @return List of colored elements found
     */
    public ArrayList<TrackedElement> findColor(String name, int time, int erosion) {

        int currentImageTime = paperScreen.getCameraTracking().getTimeStamp();

        // once per image
        if (lastImageTime == currentImageTime) {
            // return the last known points. 
            return trackedElements;
        }
        lastImageTime = currentImageTime;

        // Get the image
        capturedImage = trackedView.getViewOf(paperScreen.getCameraTracking());
        capturedImage.loadPixels();

        // Reset the colorFoundArray
        touchDetectionColor.resetInputArray();

        // Default to RGB 255 for now. 
        paperScreen.getGraphics().colorMode(PConstants.RGB, 255);

        // each pixels.
        byte id = 0;

        // Tag each pixels
        for (int x = 0; x < capturedImage.width; x++) {
            for (int y = 0; y < capturedImage.height; y++) {
                int offset = x + y * capturedImage.width;
                int c = capturedImage.pixels[offset];

                boolean good = false;

                if ("red".equals(name)) {
//                    good = MathUtils.colorFinderHSBRedish(paperScreen.getGraphics(),
//                            reference.referenceColor, c, reference.hue, reference.saturation, reference.brightness);
                    good = colorFinderLAB(paperScreen.getGraphics(),
                            c, reference);
                    boolean red = true;
//                    boolean red = MathUtils.isRed(paperScreen.getGraphics(),
//                            c, ref2, reference.redThreshold);
                    good = good && red;

                    // TODO: LAB only ?!!
                } else {
                    if ("blue".equals(name)) {
                        good = MathUtils.colorFinderHSB(paperScreen.getGraphics(),
                                reference.referenceColor, c, reference.hue, reference.saturation, reference.brightness);

                        boolean blue = MathUtils.isBlue(paperScreen.getGraphics(),
                                c, reference.referenceColor, reference.blueThreshold);
                        good = good && blue;
                    } else {
                        good = MathUtils.colorFinderHSB(paperScreen.getGraphics(),
                                reference.referenceColor, c, reference.hue, reference.saturation, reference.brightness);
                    }
                }

                if (good) {
                    colorFoundArray[offset] = id;
                }

            }
        }

        ArrayList<TrackedElement> newElements
                = touchDetectionColor.compute(time, erosion, this.scale);

        TouchPointTracker.trackPoints(trackedElements, newElements, time);
        for (TrackedElement te : trackedElements) {
            te.filter(time);
        }

        return trackedElements;
    }

    public byte[] getColorFoundArray() {
        return colorFoundArray;
    }

    public PImage getTrackedImage() {
        return this.capturedImage;
    }

    public ArrayList<TrackedElement> getTrackedElements() {
        return trackedElements;
    }

    public TouchList getTouchList() {
        TouchList output = new TouchList();
        for (TrackedElement te : trackedElements) {
            Touch t = te.getTouch();
            t.is3D = false;
//            System.out.println("pid: " + t.id);
//            System.out.println("p: " + te.getPosition());
            t.setPosition(te.getPosition());
            output.add(t);
        }
        return output;
    }

    public TouchList getTouchListOfOlderThan(int currentTime, int minAge) {
        TouchList output = new TouchList();
        for (TrackedElement te : trackedElements) {
            if (te.getAge(currentTime) > minAge) {
                Touch t = te.getTouch();
                t.setPosition(te.getPosition());
                output.add(t);
            }
        }
        return output;
    }

    public void loadParameter(String data) {
        reference.loadParameter(data);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
