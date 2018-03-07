/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch.detection;

import fr.inria.papart.calibration.files.PlanarTouchCalibration;
import static fr.inria.papart.multitouch.detection.CalibratedColorTracker.colorFinderLAB;
import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.camera.TrackedView;
import fr.inria.papart.utils.MathUtils;
import static fr.inria.papart.utils.MathUtils.absd;
import static fr.inria.papart.utils.MathUtils.constrain;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import tech.lity.rea.colorconverter.ColorConverter;

/**
 * [experimental] Find small round colored stickers (gomettes).
 *
 * @author Jérémy Laviole
 */
public class CalibratedStickerTracker extends ColorTracker {

    int numberOfRefs = 4;
    private final ColorReferenceThresholds references[];
    TouchDetectionLargeColor largeDetectionColor;
    TouchDetectionInnerCircles innerCirclesDetection;
    private TrackedView circleView;
    protected float circleSize = 8f;
//       protected TouchDetectionColor touchDetectionCircles;

    public CalibratedStickerTracker(PaperScreen paperScreen, float size) {
        super(paperScreen);  // 1 px / mm ?
        this.circleSize = size;
        references = ColorReferenceThresholds.loadDefaultThresholds(numberOfRefs);
    }

    public float bias = 1.1f;
    protected TrackedView createViewForCircle(float circleDiameter) {
        TrackedView view = new TrackedView(paperScreen);

        int w = (int) (paperScreen.getDrawingSize().x / circleDiameter * 5 * bias);
        int h = (int) (paperScreen.getDrawingSize().y / circleDiameter * 5 * bias);

        // We need to scale the circles to 5 pixels 
        view.setImageWidthPx(w);
        view.setImageHeightPx(h);
        view.init();

//        conv = parent.createImage(w, h, PApplet.RGB);
//        eroded = parent.createImage(w, h, PApplet.RGB);
        conv = new int[w * h];
        innerCircles = new byte[w * h];
        return view;
    }

    private int[] conv;
    private byte[] innerCircles;

    PlanarTouchCalibration largerTouchCalibration;

    @Override
    public void initTouchDetection() {
        super.initTouchDetection();

        circleView = createViewForCircle(this.circleSize);

        innerCirclesDetection = new TouchDetectionInnerCircles(circleView);

        PlanarTouchCalibration innerCirclesCalibration = Papart.getPapart().getDefaultColorZoneCalibration();
        innerCirclesCalibration.setMaximumDistance(2);
        innerCirclesCalibration.setMinimumComponentSize(1);
        innerCirclesCalibration.setSearchDepth(1);
        innerCirclesCalibration.setPrecision(1);
        innerCirclesCalibration.setTrackingMaxDistance(1);
        innerCirclesCalibration.setMaximumRecursion(1);
        innerCirclesDetection.setCalibration(innerCirclesCalibration);

        // Work on the same
        innerCircles = innerCirclesDetection.createInputArray();
        innerCirclesDetection.setSegmentedImage(innerCircles);
        
        
        ////////// FOR later
        largeDetectionColor = new TouchDetectionLargeColor(trackedView);
        largerTouchCalibration = Papart.getPapart().getDefaultColorZoneCalibration();

        largerTouchCalibration.setMaximumDistance(largerTouchCalibration.getMaximumDistance() * scale);
        largerTouchCalibration.setMinimumComponentSize((int) (largerTouchCalibration.getMinimumComponentSize() * scale * scale)); // Quadratic (area)
        largerTouchCalibration.setSearchDepth((int) (largerTouchCalibration.getSearchDepth() * scale));
        largerTouchCalibration.setTrackingMaxDistance(largerTouchCalibration.getTrackingMaxDistance() * scale);
        largerTouchCalibration.setMaximumRecursion((int) (largerTouchCalibration.getMaximumRecursion() * scale));

        largeDetectionColor.setCalibration(largerTouchCalibration);

        System.out.println("Second Calibration loaded");
        // share the colorFoundArray ?
        largeColorFoundArray = largeDetectionColor.createInputArray();

    }
    protected byte[] largeColorFoundArray;

    public int getReferenceColor(int id) {
        return references[id].getReferenceColor();
    }

    @Override
    public ArrayList<TrackedElement> findColor(int time) {
        int currentImageTime = paperScreen.getCameraTracking().getTimeStamp();

        // once per image
        if (lastImageTime == currentImageTime) {
            // return the last known points. 
            return trackedElements;
        }
        lastImageTime = currentImageTime;

        PImage circleImage = circleView.getViewOf(paperScreen.getCameraTracking());
        circleImage.loadPixels();
        int xstart = 0;
        int ystart = 0;
        int xend = circleImage.width;
        int yend = circleImage.height;
        int matrixsize = 5;

        // Convolution -> get the circles.
        for (int x = xstart; x < xend; x++) {
            for (int y = ystart; y < yend; y++) {
                int c = convolution(x, y, matrix5conv, matrixsize, circleImage);
                int loc = x + y * circleImage.width;
                conv[loc] = c;
            }
        }
        // Erosion -> narrow down the error.
        for (int x = xstart; x < xend; x++) {
            for (int y = ystart; y < yend; y++) {
                int loc = x + y * circleImage.width;
                boolean v = erosion(x, y, circleImage.width, matrix3erode, 3, conv);
                if(v){
//                    System.out.println("eroded ok: " + x + " " + y );
                }
                innerCircles[loc] = (byte) (v ? TouchDetectionInnerCircles.UNKNOWN_COLOR : TouchDetectionInnerCircles.INVALID_COLOR);
            }
        }
        // At this step, the circles are max  2x2 pixels wide booleans.

        // Start from the eroded points, find out the color and positions of possible circles.
        smallElements = innerCirclesDetection.compute(time, references, circleImage);
//        System.out.println("elements: " + smallElements.size());
//        for(TrackedElement te: smallElements){
//            System.out.println("t: " + te);
//        }
        return smallElements;
        
        // Get the image
//        capturedImage = trackedView.getViewOf(paperScreen.getCameraTracking());
//        capturedImage.loadPixels();
//
//        // Reset the colorFoundArray
//        touchDetectionColor.resetInputArray();
//
//        // Default to RGB 255 for now, for color distances. 
//        paperScreen.getGraphics().colorMode(PConstants.RGB, 255);
//
//        // Tag each pixels
//        for (int x = 0; x < capturedImage.width; x++) {
//            for (int y = 0; y < capturedImage.height; y++) {
//                int offset = x + y * capturedImage.width;
//                int c = capturedImage.pixels[offset];
//
//                // for each except the last DEBUG
////               byte id = 0;
//                for (byte id = 0; id < numberOfRefs; id++) {
//
//                    reference = references[id];
//                    boolean good = false;
//
////                    if (id == 0) {
////                        good = MathUtils.colorFinderHSBRedish(paperScreen.getGraphics(),
////                                reference.referenceColor, c, reference.hue, reference.saturation, reference.brightness);
////                    } else {
//                    good = colorFinderLAB(paperScreen.getGraphics(),
//                            c, reference);
////                        good = MathUtils.colorFinderHSB(paperScreen.getGraphics(),
////                                c, reference.referenceColor, reference.hue, reference.saturation, reference.brightness);
////                    }
//                    // HSB only for now.
//                    if (good) {
////                    if (references[id].colorFinderHSB(paperScreen.getGraphics(), c)) {
//                        colorFoundArray[offset] = id;
//                    }
//
//                }
//            }
//        }
//
//        int erosion = 0;
//
//        // Step1 -> small-scale colors (gomettes)
//// EROSION by color ?!
////        ArrayList<TrackedElement> newElements
////                = touchDetectionColor.compute(time, erosion, this.scale);
//        smallElements = touchDetectionColor.compute(time, erosion, this.scale);
//
//        ///
//        System.arraycopy(colorFoundArray, 0, largeColorFoundArray, 0, colorFoundArray.length);
//
//        ArrayList<TrackedElement> newElements2
//                = largeDetectionColor.compute(time, erosion, this.scale);
////
//        // Step 2 -> Large -scale colors (ensemble de gomettes) 
////        TouchPointTracker.trackPoints(trackedElements, smallElements, time);
//        trackedElements.clear();
//        trackedElements.addAll(smallElements);
//        TouchPointTracker.trackPoints(trackedLargeElements, newElements2, time);
//
////        for(TrackedElement te : trackedElements){
////            te.filter(time);
////        }
////        return trackedElements;
//        return trackedLargeElements;
    }

    ArrayList<TrackedElement> smallElements;
    protected final ArrayList<TrackedElement> trackedLargeElements = new ArrayList<>();

    public ArrayList<TrackedElement> smallElements() {
        return trackedElements;
    }

    ////// Erosion and convolution tests... 
    float[][] matrix5conv = {{3, 1, -1, 1, 3},
    {1, -2, -2, -2, 1},
    {-1, -2, -3, -2, -1},
    {1, -2, -2, -2, 1},
    {3, 1, -1, 1, 3}};

    float[][] matrix3erode = {{1, 1, 1},
    {1, 1, 1},
    {1, 1, 1}};

    int convolutionMin = 5;

    boolean erosion(int x, int y, int w, float[][] matrix, int matrixsize, int[] img) {
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
                if (matrix[i][j] == 1 && img[loc] >= convolutionMin) {
                    sum++;
                }

            }
        }
        return sum == matSum;
    }

    int convolution(int x, int y, float[][] matrix, int matrixsize, PImage img) {
        int total = 0;
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
                total += ((img.pixels[loc] >> 16) & 0xFF) * matrix[i][j]; // r
                total += ((img.pixels[loc] >> 8) & 0xFF) * matrix[i][j]; // g 
                total += (img.pixels[loc] & 0xFF) * matrix[i][j];  // b
            }
        }
        return total / 3;
//        rtotal = PApplet.constrain(rtotal / 4f, 0, 255);
//        gtotal = PApplet.constrain(gtotal / 4f, 0, 255);
//        btotal = PApplet.constrain(btotal / 4f, 0, 255);
    }

}
