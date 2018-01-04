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

    private final PaperScreen paperScreen;
    private final TrackedView trackedView;
    private PImage capturedImage;

//    private final HashMap<String, Integer> trackedColors;
    private final TouchDetectionColor touchDetectionColor;
    private final byte[] colorFoundArray;
    private final ArrayList<TrackedElement> trackedElements;
    private float scale = 1f;

    private float brightness, saturation;
    private float hue;
    private float redThreshold, blueThreshold;
    private int referenceColor, erosion;
    private String name;

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

        fft = new FFT(frameSize);
    }

    public ArrayList<TrackedElement> findColor(int time) {
        return findColor(name, referenceColor, time, erosion);
    }

    LinkedList<int[]> images = new LinkedList<>();
    int frameSize = 128;
    float frameRate = 30;
    float elapsedTime = 0;
    FFT fft;
    float epsilon;
    float[] re;
    float[] im;
    float rate = 0;

    LinkedList<Integer> framesTime = new LinkedList<>();

    int lastImageTime = 0;
    int lastcomputeTime = 0;
    int timeBetweenCompute = 500;

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

        if ("x".equals(name)) {
            images.push(capturedImage.pixels.clone());
            framesTime.push(time);
            if (images.size() > frameSize) {
                images.removeLast();
                framesTime.removeLast();
            }
            if (images.size() < frameSize) {
//                System.out.println("Frames too short: " + images.size());
            }

            if (currentImageTime > lastcomputeTime + timeBetweenCompute) {
                lastcomputeTime = currentImageTime;
            } else {
                // return the last known points. 
                return trackedElements;
            }

            // compute the real framerate
            int initFrame = framesTime.getLast();
            int lastFrame = framesTime.getFirst();
//        frameRate =  (float)(initFrame - lastFrame) / (float)(framesTime.size());
            elapsedTime = (float) (lastFrame - initFrame);
//        System.out.println("Framerate: " + frameRate);
            rate = (float) frameSize / elapsedTime * 1000f; // in ms
//       System.out.println("rate:" + rate);

            if (re == null || im == null || re.length != frameSize) {
                re = new float[frameSize];
                im = new float[frameSize];
            }

            epsilon = 2 * frameRate / (int) frameSize;
        }

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

                        // fft finding 5Hz signal.
                        if ("x".equals(name)) {
                            good = fftPx(offset, 4.5f);
                        } else {
                            good = MathUtils.colorFinderHSB(paperScreen.getGraphics(),
                                    reference, c, hue, saturation, brightness);
                        }
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

    private boolean fftPx(int offset, float freq) {

        // todo: do this before
        int nbImages = images.size();
        if (nbImages < frameSize) {
            return false;
        }

        int k = 0;
        for (int[] image : images) {

            int c1 = image[offset];
            int r1 = c1 >> 16 & 255;
            int g1 = c1 >> 8 & 255;
            int b1 = c1 >> 0 & 255;

            // todo: faster than this
            re[k] = (float) (r1 + g1 + b1) / 3f * 255f;
            im[k] = 0;
            k++;
        }
        fft.fft(re, im);

        float max = 0;
        int id = 0;
        for (int i = 2; i < re.length / 2; i++) {
            float v = strength(i, re, im);
            if (v > max) {
                max = v;
                id = i;
            }
        }

        // error can be computed... 
//        float epsilon = 0.3f;
//        float f = +(float) id / (float) frameSize * (float) frameRate * 2;
//        float f = +(float) id / (float) frameSize * (float) frameRate;
        float f = idToFreq(id);

        // get a finer estimate with 3 values average.
        if (id > 0 && id < frameSize - 1) {
            f = (float) id * max;
            float sm1 = strength(id - 1, re, im);
            float sp1 = strength(id + 1, re, im);
            float fNext = ((float) id + 1) * sm1;
            float fPrev = ((float) id - 1) * sp1;
            f = ((f + fNext + fPrev) / (max + sm1 + sp1)) / (float) frameSize * rate;
        }
        if (abs(f - freq) < epsilon && max > 10f) {
//            System.out.println("found freq: " + f);
            return true;
        }
        return false;
    }

    private float idToFreq(float id) {
        return (id) / (float) frameSize * rate;

    }

    public float strength(int i, float[] re, float[] im) {
        return sqrt(re[i] * re[i] + im[i] * im[i]);
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
