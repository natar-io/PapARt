/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch.detection;

import fr.inria.papart.calibration.files.PlanarTouchCalibration;
import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import fr.inria.papart.procam.FFT;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.utils.MathUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import static processing.core.PApplet.abs;
import static processing.core.PApplet.sqrt;
import processing.core.PConstants;

/**
 *
 * @author Jeremy Laviole - laviole@rea.lity.tech
 */
public class BlinkTracker extends ColorTracker {

    public BlinkTracker(PaperScreen paperScreen, PlanarTouchCalibration calibration, float scale) {
        super(paperScreen, calibration, scale);

        fft = new FFT(frameSize);
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

    public void resetImages() {
        images.clear();
        framesTime.clear();
    }

    /**
     * For now it only finds one color.
     *
     * @param time currernt time in Processing.
     * @return List of colored elements found
     */
    public ArrayList<TrackedElement> findColor(int time) {

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

        images.push(capturedImage.pixels.clone());
        framesTime.push(time);
        if (images.size() > frameSize) {
            images.removeLast();
            framesTime.removeLast();
        }
        if (images.size() < frameSize) {
            System.out.println("Frames too short: " + images.size());
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

        epsilon = errorRange * frameRate / (int) frameSize;

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
                boolean good = fftPx(offset, freqToFind);
                if (good) {
                    colorFoundArray[offset] = id;
                }

            }
        }

        ArrayList<TrackedElement> newElements
                = touchDetectionColor.compute(time, reference.erosion, this.scale);
        TouchPointTracker.trackPoints(trackedElements, newElements, time);
//        for(TrackedElement te : trackedElements){
//            te.filter(time);
//        }

        lastFound = colorFoundArray.clone();

        return trackedElements;
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

        float total = max;
        // get a finer estimate with 3 values average.
        if (id > 0 && id < frameSize - 1) {
            f = (float) id * max;
            float sm1 = strength(id - 1, re, im);
            float sp1 = strength(id + 1, re, im);
            float fNext = ((float) id + 1) * sm1;
            float fPrev = ((float) id - 1) * sp1;
            total = max + sm1 + sp1;
            f = ((f + fNext + fPrev) / (max + sm1 + sp1)) / (float) frameSize * rate;
        }
//          System.out.println("found ? freq: " + f + " total: " + total);

        if (abs(f - freq) < epsilon && total > maxValue) {
//            System.out.println("found: " + f + " total: " + total);
            return true;
        }
        return false;
    }

    byte[] lastFound;

    public byte[] getLastFoundArray() {
        return lastFound;
    }

    private float maxValue = 10000f; // depends on number of frames ?
    private float errorRange = 1.5f;

    private float idToFreq(float id) {
        return (id) / (float) frameSize * rate;

    }

    public float strength(int i, float[] re, float[] im) {
        return sqrt(re[i] * re[i] + im[i] * im[i]);
    }

    private float freqToFind = 4.5f;

    public void setFreq(float freq) {
        this.freqToFind = freq;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    public float getErrorRange() {
        return errorRange;
    }

    public void setErrorRange(float errorRange) {
        this.errorRange = errorRange;
    }

}
