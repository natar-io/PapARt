/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2017-2018 RealityTech
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
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.camera.TrackedView;
import java.util.ArrayList;
import java.util.List;
import static java.util.stream.Collectors.toList;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * [experimental] Find small round colored stickers (gomettes).
 *
 * @author Jérémy Laviole - laviole@rea.lity.tech
 */
public class CalibratedStickerTracker extends ColorTracker {

    public int numberOfRefs = 5;
    private ColorReferenceThresholds references[];
    private TouchDetectionInnerCircles innerCirclesDetection;
    public TrackedView circleView;
    protected float circleSize;

//       protected TouchDetectionColor touchDetectionCircles;
    public float bias = 1.00f;
    private int circleViewWidth, circleViewHeight;
    private float[] conv;
    private byte[] innerCircles;

    public CalibratedStickerTracker(PaperScreen paperScreen, float size) {
        this(paperScreen, new PVector(), new PVector(), size);
    }

    /**
     * Create a calibrated sticker tracker: calibrated means that it uses the
     * color calibrations to validate the colored circles. It detects colors
     * circles of a give size.
     *
     * @param paperScreen
     * @param offset
     * @param capSize
     * @param size : size of the circluse
     */
    public CalibratedStickerTracker(PaperScreen paperScreen, PVector offset,
            PVector capSize, float size) {
        super();  // 1 px / mm ?
        this.circleSize = size;
        this.paperScreen = paperScreen;

        loadDefaultColorReferences(numberOfRefs);
        initTouchDetection(offset, capSize);
    }

    /**
     * Set the color references for identification.
     *
     * @param refs
     */
    public void setColorReferences(ColorReferenceThresholds[] refs) {
        references = refs;
    }
    
    public void setSensitivity(float sensitive) {
        innerCirclesDetection.setSentivity(sensitive);
    }
    
    /**
     * Load the default color references from PapARt.
     * @param numberRefs
     */
    public void loadDefaultColorReferences(int numberRefs) {
        this.numberOfRefs = numberRefs;
        setColorReferences(ColorReferenceThresholds.loadDefaultThresholds(numberOfRefs));
    }
    
     public void setAutoRefineColors(boolean auto){
        innerCirclesDetection.setAutoRefineColors(auto);
    }

    /**
     * Initialize the memory, automatically called on creation. This method
     * creates the view buffer used for circle detection.
     *
     * @param offset
     * @param capSize, optionnal
     */
    public void initTouchDetection(PVector offset, PVector capSize) {
        circleView = new TrackedView(paperScreen);

        PVector captureSize = new PVector();
        // No cap size, we capture the whole paperscreen.
        if (capSize == null || capSize.x == 0 || capSize.y == 0) {
            captureSize.set(paperScreen.getDrawingSize());
            capSize.set(paperScreen.getDrawingSize());
        } else {
            captureSize.set(capSize);
        }
        // it is scae to mm... 
        scale = 1f / circleSize * 5 * bias;

        circleViewWidth = (int) (captureSize.x * scale);
        circleViewHeight = (int) (captureSize.y * scale);

        circleView.setTopLeftCorner(offset);
        circleView.setCaptureSizeMM(captureSize);

        // We need to scale the circles to 5 pixels 
        circleView.setImageWidthPx(circleViewWidth);
        circleView.setImageHeightPx(circleViewHeight);
        circleView.init();
        this.trackedView = circleView;
        conv = new float[circleViewWidth * circleViewHeight];
        innerCircles = new byte[circleViewWidth * circleViewHeight];

        innerCirclesDetection = new TouchDetectionInnerCircles(circleView);

        PlanarTouchCalibration innerCirclesCalibration = Papart.getPapart().getDefaultColorZoneCalibration();
        innerCirclesCalibration.setMaximumDistance(2);
        innerCirclesCalibration.setMinimumComponentSize(1);
        innerCirclesCalibration.setSearchDepth(1);
        innerCirclesCalibration.setPrecision(1);
        innerCirclesCalibration.setTrackingMaxDistance(70);
//        innerCirclesCalibration.setTrackingForgetTime(300);
        innerCirclesCalibration.setMaximumRecursion(1);
        innerCirclesDetection.setCalibration(innerCirclesCalibration);

        innerCircles = innerCirclesDetection.createInputArray();
    }

    public int getReferenceColor(int id) {
        return references[id].getReferenceColor();
    }
    
    public ColorReferenceThresholds getReference(int id) {
        return references[id];
    }

    /**
     * Find the circular colored elements. It only selects the one with a known
     * color
     *
     * @param time
     * @return
     */
    @Override
    public ArrayList<TrackedElement> findColor(int time) {

        if (paperScreen.getCameraTracking() == null) {
            return new ArrayList<>();
        }
        int currentImageTime = paperScreen.getCameraTracking().getTimeStamp();

        // once per image
        if (lastImageTime == currentImageTime) {
            // return the last known points. 
            return trackedElements;
        }
        lastImageTime = currentImageTime;

        PImage circleImage = circleView.getViewOf(paperScreen.getCameraTracking());
        if(circleImage == null){
            return new ArrayList<>();
        }
        circleImage.loadPixels();
        int xstart = 0;
        int ystart = 0;
        int xend = circleImage.width;
        int yend = circleImage.height;
        int matrixsize = 5;

        // Convolution -> get the circles.
        for (int x = xstart; x < xend; x++) {
            for (int y = ystart; y < yend; y++) {
                float c = convolution(x, y, matrix5conv, matrixsize, circleImage);
                int loc = x + y * circleImage.width;
                conv[loc] = c;
            }
        }
        // Erosion -> narrow down the error.
        for (int x = xstart; x < xend; x++) {
            for (int y = ystart; y < yend; y++) {
                int loc = x + y * circleImage.width;
                float v = erosion(x, y, circleImage.width, matrix3erode, 3, conv);

                innerCircles[loc] = (byte) (v >= convolutionErr ? TouchDetectionInnerCircles.UNKNOWN_COLOR : TouchDetectionInnerCircles.INVALID_COLOR);
            }
        }
        // At this step, the circles are max  2x2 pixels wide booleans.

        // Start from the eroded points, find out the color and positions of possible circles.
        ArrayList<TrackedElement> newStickers = innerCirclesDetection.compute(time, references,
                circleImage, this.scale);

        // Increase the quality by looking again in a higher resolution ???
//        trackedElements.clear();
//        trackedElements.addAll(newStickers);
        // Tracking must be enabled for touch with skatolo. 
        // TODO: track by color, then merge again.
        ArrayList<TrackedElement> all = new ArrayList<>();
        for (int i = 0; i < numberOfRefs; i++) {

            ArrayList<TrackedElement> current = getColor(trackedElements, i);
            TouchPointTracker.trackPoints(current, getColor(newStickers, i), time);
            TouchPointTracker.filterPositions(current, time);

            all.addAll(current);
        }

        trackedElements.clear();
        trackedElements.addAll(all);

//        TouchPointTracker.trackPoints(trackedElements, newStickers, time);
//        TouchPointTracker.filterPositions(trackedElements, time);
        
        return trackedElements;
    }

    public ArrayList<TrackedElement> getColor(ArrayList<TrackedElement> source, int id) {
        ArrayList<TrackedElement> output = new ArrayList<>();

        source.stream()
                .filter((t) -> t.attachedValue == id)
                .forEach((t) -> output.add(t));

//        ArrayList<TrackedElement> output = new ArrayList<>();
//        for (TrackedElement t : source) {
//            if (t.attachedValue == id) {
//                output.add(t);
//            }
//        }
        return output;
    }

    /**
     * Create a line cluster, of a given size in millimeters.
     *
     * @param size
     * @return
     */
    public ArrayList<LineCluster> createLineClusters(float size) {
        return LineCluster.createLineCluster(trackedElements, size);
    }

    /**
     * Create a zone cluster of a given size. A zone cluster regroups the
     * tracked elements in a zone.
     *
     * @param size
     * @return
     */
    public ArrayList<StickerCluster> clusters(float size) {
        return StickerCluster.createZoneCluster(trackedElements, size);

    }

    /**
     * Create Touch from the tracked elements.
     *
     * @param id
     * @return
     */
    public TouchList getTouchList(int id) {
        TouchList output = new TouchList();
        for (TrackedElement te : trackedElements) {
            if (te.attachedValue == id) {
                Touch t = te.getTouch();
                t.setPosition(te.getPosition());
                output.add(t);
            }
        }
        return output;
    }

    ArrayList<TrackedElement> smallElements;
    protected final ArrayList<TrackedElement> trackedLargeElements = new ArrayList<>();

    public ArrayList<TrackedElement> smallElements() {
        return trackedElements;
    }

    ////// Erosion and convolution tests... 
    private float[][] matrix5conv = {{3, 1, -1, 1, 3},
    {1, -2, -2, -2, 1},
    {-1, -2, -3, -2, -1},
    {1, -2, -2, -2, 1},
    {3, 1, -1, 1, 3}};

    private float[][] matrix3erode = {{1, 1, 1},
    {1, 1, 1},
    {1, 1, 1}};

    public static int convolutionMin = 2;
    public static int convolutionErr = 1;

    /**
     * Perform the erosion operation, given a structure in a matrix..
     *
     * @param x
     * @param y
     * @param w
     * @param matrix
     * @param matrixsize
     * @param img
     * @return
     */
    private float erosion(int x, int y, int w, float[][] matrix, int matrixsize, float[] img) {
        int matSum = 0;
        for (int i = 0; i < matrixsize; i++) {
            for (int j = 0; j < matrixsize; j++) {
                matSum += matrix[i][j];
            }
        }

        int sum = 0;
        int offset = matrixsize / 2;
        for (int i = 0; i < matrixsize; i++) {
            for (int j = 0; j < matrixsize; j++) {
                // What pixel are we testing
                int xloc = x + i - offset;
                int yloc = y + j - offset;
                int loc = xloc + w * yloc;
                // Make sure we haven't walked off our image, we could do better here
                loc = PApplet.constrain(loc, 0, img.length - 1);

                // Calculate erosion
                // if (matrix[i][j] == 1 && img[loc] >= convolutionMin) {
                //     sum++;
                // }

                sum += img[loc]; 

            }
        }
        return sum / matSum; //  + convolutionErr >= matSum;
    }

    /**
     * Perform a convolution on an input img pixel at position x,y with a given
     * matrix.
     *
     * @param x
     * @param y
     * @param matrix
     * @param matrixsize
     * @param img
     * @return
     */
    private float convolution(int x, int y, float[][] matrix, int matrixsize, PImage img) {
        float total = 0;
        int offset = matrixsize / 2;
        for (int i = 0; i < matrixsize; i++) {
            for (int j = 0; j < matrixsize; j++) {
                // What pixel are we testing
                int xloc = x + i - offset;
                int yloc = y + j - offset;
                int loc = xloc + img.width * yloc;
                // Make sure we haven't walked off our image, we could do better here
                loc = PApplet.constrain(loc, 0, img.pixels.length - 1);
                // Calculate the convolution
                total += ((img.pixels[loc] >> 16) & 0xFF) * matrix[i][j] * 0.216f; // r
                total += ((img.pixels[loc] >> 8) & 0xFF) * matrix[i][j] * 0.750f; // g 
                total += (img.pixels[loc] & 0xFF) * matrix[i][j] * 0.072f;  // b
            }
        }
        return total;
    }

}
