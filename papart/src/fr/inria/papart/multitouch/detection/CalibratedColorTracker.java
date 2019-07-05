/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch.detection;

import fr.inria.papart.calibration.files.PlanarTouchCalibration;
import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.TouchList;
import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.utils.MathUtils;
import static fr.inria.papart.utils.MathUtils.absd;
import static fr.inria.papart.utils.MathUtils.constrain;
import java.util.ArrayList;
import processing.core.PConstants;
import processing.core.PGraphics;
import tech.lity.rea.colorconverter.ColorConverter;
import tech.lity.rea.nectar.camera.CameraNectar;

/**
 * Color Tracker with the calibrated colors.
 *
 * @author Jérémy Laviole
 */
public class CalibratedColorTracker extends ColorTracker {

    private int numberOfRefs = 4;
    private ColorReferenceThresholds references[];
    private TouchDetectionLargeColor largeDetectionColor;
    private PlanarTouchCalibration largerTouchCalibration;

    public CalibratedColorTracker(PaperScreen paperScreen, float scale) {
        super(paperScreen, scale);
        loadDefaultColorReferences();
    }

    /**
     * Set the color references for identification.
     *
     * @param refs
     */
    public void setColorReferences(ColorReferenceThresholds[] refs) {
        references = refs;
    }

    /**
     * Load the default color references from PapARt.
     */
    public void loadDefaultColorReferences() {
        if (paperScreen.getCameraTracking() instanceof CameraNectar) {
            CameraNectar cam = (CameraNectar) paperScreen.getCameraTracking();

            setColorReferences(ColorReferenceThresholds.loadThresholds(numberOfRefs,
                    cam.getRedisClient(), cam.getCameraDescription()));
        } else {
            System.out.println("Cannot load color calibrations without a Natar Camera.");
        }
    }

    @Override
    public void initTouchDetection() {
        super.initTouchDetection();

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

        // Get the image
        capturedImage = trackedView.getViewOf(paperScreen.getCameraTracking());
        capturedImage.loadPixels();

        // Reset the colorFoundArray
        touchDetectionColor.resetInputArray();

        // Default to RGB 255 for now, for color distances. 
        paperScreen.getGraphics().colorMode(PConstants.RGB, 255);

        // Tag each pixels
        for (int x = 0; x < capturedImage.width; x++) {
            for (int y = 0; y < capturedImage.height; y++) {
                int offset = x + y * capturedImage.width;
                int c = capturedImage.pixels[offset];

                // for each except the last DEBUG
//               byte id = 0;
                for (byte id = 0; id < numberOfRefs; id++) {

                    reference = references[id];
                    boolean good = false;

//                    if (id == 0) {
//                        good = MathUtils.colorFinderHSBRedish(paperScreen.getGraphics(),
//                                reference.referenceColor, c, reference.hue, reference.saturation, reference.brightness);
//                    } else {
                    good = colorFinderLAB(paperScreen.getGraphics(),
                            c, reference);
//                        good = MathUtils.colorFinderHSB(paperScreen.getGraphics(),
//                                c, reference.referenceColor, reference.hue, reference.saturation, reference.brightness);
//                    }
                    // HSB only for now.
                    if (good) {
//                    if (references[id].colorFinderHSB(paperScreen.getGraphics(), c)) {
                        colorFoundArray[offset] = id;
                    }

                }
            }
        }

        int erosion = 0;

        lastFound = colorFoundArray.clone();
//        ArrayList<TrackedElement> newElements
//                = touchDetectionColor.compute(time, erosion, this.scale);
        smallElements = touchDetectionColor.compute(time, erosion, this.scale);

        // Step 2 -> Large -scale colors (ensemble de gomettes) 
        TouchPointTracker.trackPoints(trackedElements, smallElements, time);

//        for(TrackedElement te : trackedElements){
//            te.filter(time);
//        }
        return trackedElements;
    }

    public byte[] lastFound;

    ArrayList<TrackedElement> smallElements;
    protected final ArrayList<TrackedElement> trackedLargeElements = new ArrayList<>();

    public ArrayList<TrackedElement> smallElements() {
        return trackedElements;
    }

    public TouchList getTouchList(int id) {
        TouchList output = new TouchList();
        for (TrackedElement te : trackedElements) {
            Touch t = te.getTouch();
            if (t.trackedSource().attachedValue == id) {
                t.is3D = false;
//            System.out.println("pid: " + t.id);
//            System.out.println("p: " + te.getPosition());
                t.setPosition(te.getPosition());
                output.add(t);
            }
        }
        return output;
    }

    public static ColorConverter converter = new ColorConverter();

    /**
     * Color distance on the LAB scale. The incomingPix is compared with the
     * baseline. The method returns true if each channel validates the condition
     * for the given threshold.
     *
     * @param g
     * @param baseline
     * @param incomingPix
     * @param LTresh
     * @param ATresh
     * @param BTresh
     * @return
     */
    public static boolean colorFinderLAB(PGraphics g, int baseline, int incomingPix,
            float LTresh, float ATresh, float BTresh) {

        double[] labBase = converter.RGBtoLAB((int) g.red(baseline), (int) g.green(baseline), (int) g.blue(baseline));
        double[] labIncoming = converter.RGBtoLAB((int) g.red(incomingPix), (int) g.green(incomingPix), (int) g.blue(incomingPix));
        return labIncoming[0] > 50.0 // Very large light base
                //                absd(labBase[0] - labIncoming[0]) < LTresh * 20  // Very large light base
                && absd(labBase[1] - labIncoming[1]) < ATresh
                && absd(labBase[2] - labIncoming[2]) < BTresh;
    }

    /**
     * Color distance on the LAB scale. The incomingPix is compared with the
     * baseline. The method returns true if each channel validates the condition
     * for the given threshold.
     *
     * @param g
     * @param ref
     * @return
     */
    public static boolean colorFinderLAB(PGraphics g, int incomingPix,
            ColorReferenceThresholds ref) {

        double[] lab = converter.RGBtoLAB((int) g.red(incomingPix), (int) g.green(incomingPix), (int) g.blue(incomingPix));

        double l = constrain(lab[0], 0, 100);
        double A = constrain(lab[1], -128, 128);
        double B = constrain(lab[2], -128, 128);

        double d
                = Math.sqrt(Math.pow(l - ref.averageL, 2)
                        + Math.pow(A - ref.averageA, 2)
                        + Math.pow(B - ref.averageB, 2));

//        System.out.println("d: "  + d);
        return d < (ref.AThreshold + ref.BThreshold + ref.LThreshold) * 2;
    }

    /**
     * Color distance on the LAB scale. The incomingPix is compared with the
     * baseline. The method returns true if each channel validates the condition
     * for the given threshold. Use the sum of error thresholds.
     *
     * @param incomingPix
     * @param ref
     * @return
     */
    public static boolean colorFinderLAB(int incomingPix,
            ColorReferenceThresholds ref) {

        double[] lab = converter.RGBtoLAB((int) ((incomingPix >> 16) & 0xFF),
                (int) ((incomingPix >> 8) & 0xFF), (int) (incomingPix & 0xFF));

        double l = constrain(lab[0], 0, 100);
        double A = constrain(lab[1], -128, 128);
        double B = constrain(lab[2], -128, 128);

        double d
                = Math.sqrt(Math.pow(l - ref.averageL, 2)
                        + Math.pow(A - ref.averageA, 2)
                        + Math.pow(B - ref.averageB, 2));

//        System.out.println("d: "  + d);
        return d < (ref.AThreshold + ref.BThreshold + ref.LThreshold) * 2f;
    }

    /**
     * Color distance on the LAB scale. The incomingPix is compared with the
     * baseline. The method returns true if each channel validates the condition
     * for the given threshold. Use the error d to match the points.
     *
     * @param incomingPix
     * @param ref
     * @param error
     * @return
     */
    public static boolean colorFinderLAB(int incomingPix,
            ColorReferenceThresholds ref, float error) {

        double[] lab = converter.RGBtoLAB((int) ((incomingPix >> 16) & 0xFF),
                (int) ((incomingPix >> 8) & 0xFF), (int) (incomingPix & 0xFF));

        double l = constrain(lab[0], 0, 100);
        double A = constrain(lab[1], -128, 128);
        double B = constrain(lab[2], -128, 128);

        double d
                = Math.sqrt(Math.pow(l - ref.averageL, 2)
                        + Math.pow(A - ref.averageA, 2)
                        + Math.pow(B - ref.averageB, 2));

//        System.out.println("d: "  + d);
        return d < error;
    }

    /**
     * Color distance on the LAB scale. The incomingPix is compared with the
     * baseline. The method returns true if each channel validates the condition
     * for the given threshold. Use the error d to match the points.
     *
     * @param incomingPix
     * @param ref
     * @return
     */
    public static float colorFinderLABError(int incomingPix,
            ColorReferenceThresholds ref) {

        double[] lab = converter.RGBtoLAB((int) ((incomingPix >> 16) & 0xFF),
                (int) ((incomingPix >> 8) & 0xFF), (int) (incomingPix & 0xFF));

        double l = constrain(lab[0], 0, 100);
        double A = constrain(lab[1], -128, 128);
        double B = constrain(lab[2], -128, 128);

        double d
                = Math.sqrt(Math.pow(l - ref.averageL, 2)
                        + Math.pow(A - ref.averageA, 2)
                        + Math.pow(B - ref.averageB, 2));

//        System.out.println("d: "  + d);
        return (float) d;
    }

}
