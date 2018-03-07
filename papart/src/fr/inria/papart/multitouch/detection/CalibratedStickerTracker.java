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
    TouchDetectionInnerCircles innerCirclesDetection;
    private TrackedView circleView;
    protected float circleSize = 8f;
//       protected TouchDetectionColor touchDetectionCircles;

    public float bias = 1.05f;
    private int circleViewWidth, circleViewHeight;
    private int[] conv;
    private byte[] innerCircles;

    public CalibratedStickerTracker(PaperScreen paperScreen, float size) {
        super();  // 1 px / mm ?
        this.circleSize = size;
        references = ColorReferenceThresholds.loadDefaultThresholds(numberOfRefs);
        
        this.paperScreen = paperScreen;
        initTouchDetection();

    }

    protected TrackedView createViewForCircle(float circleDiameter) {
        TrackedView view = new TrackedView(paperScreen);

        // it is scae to mm... 
        scale = 1f / circleDiameter * 5 * bias;
        circleViewWidth = (int) (paperScreen.getDrawingSize().x * scale);
        circleViewHeight = (int) (paperScreen.getDrawingSize().y * scale);

        // We need to scale the circles to 5 pixels 
        view.setImageWidthPx(circleViewWidth);
        view.setImageHeightPx(circleViewHeight);
        view.init();

        conv = new int[circleViewWidth * circleViewHeight];
        innerCircles = new byte[circleViewWidth * circleViewHeight];
        return view;
    }

    PlanarTouchCalibration largerTouchCalibration;

    @Override
    public void initTouchDetection() {

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

        innerCircles = innerCirclesDetection.createInputArray();


    }

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
                if (v) {
//                    System.out.println("eroded ok: " + x + " " + y );
                }
                innerCircles[loc] = (byte) (v ? TouchDetectionInnerCircles.UNKNOWN_COLOR : TouchDetectionInnerCircles.INVALID_COLOR);
            }
        }
        // At this step, the circles are max  2x2 pixels wide booleans.

        // Start from the eroded points, find out the color and positions of possible circles.
        smallElements = innerCirclesDetection.compute(time, references,
                circleImage, this.scale);

             // Increase the quality by looking again in a higher resolution ???
        trackedElements.clear();
        trackedElements.addAll(smallElements);   
        
        // Take all the points, 
        // Sort them by distance, and try to make cluster of d < 5cm ?
       clusters = StickerCluster.createCluster(smallElements, 55); // 40mm

        return trackedElements;
    }
    ArrayList<StickerCluster> clusters;
        
    public ArrayList<StickerCluster> clusters(){
        return clusters;
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
