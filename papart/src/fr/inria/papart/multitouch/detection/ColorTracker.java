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
import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import fr.inria.papart.multitouch.detection.TouchDetectionColor;
import fr.inria.papart.procam.FFT;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.camera.TrackedView;
import fr.inria.papart.utils.MathUtils;
import fr.inria.papart.utils.SimpleSize;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import org.bytedeco.javacpp.opencv_core.IplImage;
import static processing.core.PApplet.abs;
import static processing.core.PApplet.sqrt;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class ColorTracker {

    protected final PaperScreen paperScreen;
    protected final TrackedView trackedView;

    protected PImage capturedImage;

//    protected final HashMap<String, Integer> trackedColors;
    protected final TouchDetectionColor touchDetectionColor;
    protected final byte[] colorFoundArray;
    protected final ArrayList<TrackedElement> trackedElements;
    protected float scale = 1f;

    protected float brightness, saturation;
    protected float hue;
    protected float redThreshold, blueThreshold;
    protected int referenceColor, erosion;
    protected String name;

    public ColorTracker(PaperScreen paperScreen) {
        this(paperScreen, 1);
    }

    public ColorTracker(PaperScreen paperScreen, PlanarTouchCalibration calibration) {
        this(paperScreen, calibration, 1);
    }

    public ColorTracker(PaperScreen paperScreen, float scale) {
        this(paperScreen, Papart.getPapart().getDefaultColorTouchCalibration(), scale);
    }

    public ColorTracker(PaperScreen paperScreen, PlanarTouchCalibration calibration, float scale) {
        this.paperScreen = paperScreen;
        this.trackedView = new TrackedView(paperScreen);
        this.trackedView.setScale(scale);
        trackedView.init();
        this.scale = scale;

//        this.trackedColors = new HashMap<>();
        SimpleSize size = new SimpleSize(paperScreen.getDrawingSize().copy().mult(scale));
        touchDetectionColor = new TouchDetectionColor(size);

        PlanarTouchCalibration calib = Papart.getPapart().getDefaultColorTouchCalibration();
        calib.setMaximumDistance(calib.getMaximumDistance() * scale);
        calib.setMinimumComponentSize((int) (calib.getMinimumComponentSize() * scale * scale)); // Quadratic (area)
        calib.setSearchDepth((int) (calib.getSearchDepth() * scale));
        calib.setTrackingMaxDistance(calib.getTrackingMaxDistance() * scale);
        calib.setMaximumRecursion((int) (calib.getMaximumRecursion() * scale));

        touchDetectionColor.setCalibration(calib);
        trackedElements = new ArrayList<TrackedElement>();

        colorFoundArray = touchDetectionColor.createInputArray();
        hue = 40;
        saturation = 70;
        brightness = 80;
        redThreshold = 15;

    }

    public TrackedView getTrackedView() {
        return trackedView;
    }

    public ArrayList<TrackedElement> findColor(int time) {
        return findColor(name, referenceColor, time, erosion);
    }

    protected int lastImageTime = 0;

    /**
     * For now it only finds one color.
     *
     * @param name can be "red", "blue" or something else to disable this
     * matching.
     * @param reference Reference color found by the camera somewhere. -1 to
     * disable it.
     * @param time currernt time in Processing.
     * @param erosion Erosion to apply before the tracking.
     * @return List of colored elements found
     */
    public ArrayList<TrackedElement> findColor(String name, int reference, int time, int erosion) {

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
                    good = MathUtils.colorFinderHSB(paperScreen.getGraphics(),
                            reference, c, hue, saturation, brightness);

                    boolean red = MathUtils.isRed(paperScreen.getGraphics(),
                            c, reference, redThreshold);
                    good = good && red;
                } else {
                    if ("blue".equals(name)) {
                        good = MathUtils.colorFinderHSB(paperScreen.getGraphics(),
                                reference, c, hue, saturation, brightness);

                        boolean blue = MathUtils.isBlue(paperScreen.getGraphics(),
                                c, reference, blueThreshold);
                        good = good && blue;
                    } else {
                        good = MathUtils.colorFinderHSB(paperScreen.getGraphics(),
                                reference, c, hue, saturation, brightness);
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
//        for(TrackedElement te : trackedElements){
//            te.filter(time);
//        }

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
//
//    /**
//     * Add a color to track, returns the associated ID to modifiy it.
//     *
//     * @param name Tag of the color to store, like "red", or "blue"
//     * @param initialValue initial value
//     */
//    public void addTrackedColor(String name, int initialValue) {
//        this.trackedColors.put(name, initialValue);
//    }
//
//    /**
//     * Update the value of a given color.
//     *
//     * @param name
//     * @param value
//     */
//    public void updateTrackedColor(String name, int value) {
//        this.trackedColors.replace(name, value);
//    }
//
//    /**
//     * Remove the tracking of a color
//     *
//     * @param name
//     */
//    public void removeTrackedColor(String name) {
//        this.trackedColors.remove(name);
//    }

    public void loadParameter(String data) {
        try {
            String[] pair = data.split(":");
            if (pair[0].startsWith("hue")) {
                hue = Float.parseFloat(pair[1]);
            }
            if (pair[0].startsWith("sat")) {
                saturation = Float.parseFloat(pair[1]);
            }
            if (pair[0].startsWith("intens")) {
                this.brightness = Float.parseFloat(pair[1]);
            }

            if (pair[0].startsWith("erosion")) {
                this.erosion = Integer.parseInt(pair[1]);
            }
            if (pair[0].startsWith("col")) {
                this.referenceColor = Integer.parseInt(pair[1]);
            }

            if (pair[0].startsWith("red")) {
                this.redThreshold = Float.parseFloat(pair[1]);
            }
            if (pair[0].startsWith("blue")) {
                this.blueThreshold = Float.parseFloat(pair[1]);
            }
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
    }

    public void setThresholds(float hue, float saturation, float brightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }

    public void setRedThreshold(float red) {
        this.redThreshold = red;
    }

    public void setBlueThreshold(float blue) {
        this.blueThreshold = blue;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public void setHue(float hue) {
        this.hue = hue;
    }

    public float getBrightness() {
        return brightness;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getHue() {
        return hue;
    }

    public float getRedThreshold() {
        return redThreshold;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getErosion() {
        return erosion;
    }

    public void setErosion(int erosion) {
        this.erosion = erosion;
    }

    public int getReferenceColor() {
        return referenceColor;
    }

    public void setReferenceColor(int referenceColor) {
        this.referenceColor = referenceColor;
    }

}
