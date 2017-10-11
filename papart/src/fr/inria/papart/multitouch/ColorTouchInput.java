/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch;

import fr.inria.papart.calibration.files.PlanarTouchCalibration;
import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.files.PlaneCalibration;
import static fr.inria.papart.calibration.files.PlaneCalibration.CreatePlaneCalibrationFrom;
import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.TouchList;
import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import fr.inria.papart.multitouch.detection.TouchDetectionColor;
import fr.inria.papart.procam.Papart;
import static fr.inria.papart.procam.Papart.planeAndProjectionCalib;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.procam.Screen;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.utils.MathUtils;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Ray3D;
import toxi.geom.ReadonlyVec3D;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class ColorTouchInput extends TouchInput {

//    private final HashMap<String, Integer> trackedColors;
    private TouchDetectionColor touchDetectionColor;
    private byte[] colorFoundArray;
    private final ArrayList<TrackedElement> trackedElements;
    private float scale = 1f;

    private float brightness, saturation;
    private float hue;
    private float redThreshold;

    protected PlanarTouchCalibration calibration;
    private final PApplet parent;
    private final Camera camera;

    private final Semaphore touchPointSemaphore = new Semaphore(1, true);

    public ColorTouchInput(PApplet parent, Camera irCamera) {

        this.parent = parent;
        this.camera = irCamera;

//        this.trackedColors = new HashMap<>();
        this.calibration = Papart.getPapart().getDefaultColorTouchCalibration();
        trackedElements = new ArrayList<>();

        hue = 40;
        saturation = 70;
        brightness = 80;
        redThreshold = 15;
    }

    public void setCalibration(PlanarTouchCalibration calibration) {
        this.calibration = calibration;
    }

    public PlanarTouchCalibration getCalibration() {
        return this.calibration;
    }

    protected PImage currentImage, copy;

    public PImage getCurrentImage() {
        return currentImage;
    }

    public boolean calib = false;
    public PlaneAndProjectionCalibration planeCalib;

    @Override
    public void update() {
        if (touchDetectionColor == null) {
            touchDetectionColor = new TouchDetectionColor(this.camera);
            touchDetectionColor.setCalibration(calibration);
            colorFoundArray = touchDetectionColor.createInputArray();

            planeCalib = new PlaneAndProjectionCalibration();
            planeCalib.loadFrom(parent, planeAndProjectionCalib);
        }
        if (camera.getIplImage() != null) {

            // Copy not necessay outside calibration
            if (copy == null) {
                copy = parent.createImage(camera.getWidth(), camera.getHeight(), PConstants.RGB);
            }
            this.camera.getPImageCopyTo(copy);

            findColor(copy, parent.millis());
            currentImage = copy;
//                findColor(camera.getPImage(), parent.millis());
//                currentImage = camera.getPImage();
        }

    }

    @Override
    public TouchList projectTouchToScreen(Screen screen, BaseDisplay display) {
        try {
            touchPointSemaphore.acquire();
        } catch (InterruptedException ie) {
            System.err.println("Semaphore Exception: " + ie);
        }

        System.out.println("Project touch to Screen: " + trackedElements.size());
        TouchList touchList = new TouchList();

        for (TrackedElement te : trackedElements) {
            Touch touch = te.createTouch();
            PVector position = te.getPosition();
            ProjectiveDeviceP projectiveDeviceP = camera.getProjectiveDevice();
            PVector originP = new PVector(0, 0, 0);
            PVector viewedPtP = projectiveDeviceP.pixelToWorldNormP((int) position.x, (int) position.y);
            Ray3D ray
                    = new Ray3D(new Vec3D(originP.x,
                            originP.y,
                            originP.z),
                            new Vec3D(viewedPtP.x,
                                    viewedPtP.y,
                                    viewedPtP.z));

            screen.computeScreenPosTransform(camera);
            // 3D intersection with the screen plane. 
//            ReadonlyVec3D inter = screen.getPlane().getIntersectionWithRay(ray);
            ReadonlyVec3D inter = planeCalib.getPlane().getIntersectionWithRay(ray);

//            paperScreenCoord = new PVector();
//            PVector pKinectP = new PVector(pKinect.x, pKinect.y, pKinect.z);
//            PMatrix3D transfo = screen.getLocation(display.getCamera());
//            transfo.invert();
//            transfo.mult(pKinectP, paperScreenCoord);
//        dist = screen.plane.intersectRayDistance(ray);
//            System.out.println("Intersection: " + inter);
            // 3D -> 2D transformation
            if (inter != null) {
//                Vec3D res = screen.getWorldToScreen().applyTo(inter);
//                PVector out = new PVector(res.x() / res.z(),
//                        1f - (res.y() / res.z()), 1);
                PVector out = new PVector();
                PVector intersection = new PVector(inter.x(), inter.y(), inter.z());
                System.out.println("proj1: " + intersection);

                PMatrix3D extrinsics = ((ProjectorDisplay) display).getExtrinsics();

                PMatrix3D screenLoc = screen.getLocation(camera).get();
                screenLoc.invert();
                screenLoc.mult(intersection, out);
                extrinsics.mult(out, out);
                System.out.println("final: " + out.toString());
//            pdp.createRayFrom(position);
            }
        }

        touchPointSemaphore.release();
        return touchList;
    }

    public ArrayList<TrackedElement> findColor(PImage capturedImage, int time) {

        capturedImage.loadPixels();

        // Reset the colorFoundArray
        touchDetectionColor.resetInputArray();

        // Default to RGB 255 for now. 
//        parent.g.colorMode(PConstants.RGB, 255);
        int[] pixels = capturedImage.pixels;

        // each pixels.
        byte id = 0;
        int reference = (int) calibration.getTest2();

        for (int x = 0; x < capturedImage.width; x++) {
            for (int y = 0; y < capturedImage.height; y++) {
                int offset = x + y * capturedImage.width;
                int c = capturedImage.pixels[offset];
                boolean good = MathUtils.threshold(parent.g, c, calibration.getTest1());
                if (good) {
//                    px[offset] = parent.g.color(255, 0, 0);
                    colorFoundArray[offset] = id;
                } else {
//                    px[offset] = parent.g.color(0, 255, 50);
                }
            }
        }
//        capturedImage.updatePixels();
        ArrayList<TrackedElement> newElements
                = touchDetectionColor.compute(time, 0, this.scale);

//        System.out.println("Plane1: " + planeCalib.getPlane());
//        plane.getPlane().invert();
//        System.out.println("Plane: " + plane.getPlane());
        for (TrackedElement te : trackedElements) {
            PVector position = te.getPosition();
            ProjectiveDeviceP projectiveDeviceP = camera.getProjectiveDevice();
            PVector originP = new PVector(0, 0, 0);

            PVector viewedPtP = projectiveDeviceP.pixelToWorldNormalized(position.x / camera.width(),
                    position.y / camera.height());

            PVector originC = originP.copy();
            PVector viewedPtC = viewedPtP.copy();
//            if (hasExtrinsics()) {
//                // Pass it to the camera point of view (origin)
//                PMatrix3D proCamExtrinsics = getExtrinsicsInv();
//                originC = new PVector();
//                viewedPtC = new PVector();
//                proCamExtrinsics.mult(originP, originC);
//                proCamExtrinsics.mult(viewedPtP, viewedPtC);
//            }

            // Second argument is a direction
            viewedPtC.sub(originC);

            Ray3D ray = new Ray3D(new Vec3D(originC.x,
                    originC.y,
                    originC.z),
                    new Vec3D(viewedPtC.x,
                            viewedPtC.y,
                            viewedPtC.z));

            PMatrix3D tableLocation = Papart.getPapart().getTableLocation();
            PlaneCalibration plane = CreatePlaneCalibrationFrom(tableLocation, new PVector(100, 100));
            // Intersect ray with Plane
//            ReadonlyVec3D inter = planeCalib.getPlane().getIntersectionWithRay(ray);
            ReadonlyVec3D inter = plane.getPlane().getIntersectionWithRay(ray);

//        dist = screen.plane.intersectRayDistance(ray);
            // 3D -> 2D transformation
            if (inter != null && inter.z() < 2000 && inter.z() > 50) {
                System.out.println("Intersection: " + inter);

                PVector out = new PVector();
                PVector intersection = new PVector(inter.x(), inter.y(), inter.z());
                ProjectorDisplay projector = Papart.getPapart().getProjectorDisplay();
                PMatrix3D extrinsics = projector.getExtrinsics().get();
//                extrinsics.invert();
//                ProjectiveDeviceP projDev = projector.getProjectiveDeviceP();

                extrinsics.mult(intersection, out);

                PVector out1 = projector.getProjectiveDeviceP().worldToPixelUnconstrained(out);
                position.x = out1.x;
                position.y = out1.y;

//                int p = projDev.worldToPixel(out);
//                    position.set(intersection);
//                    position.set(out);
                //
//                position.x = p % projDev.getWidth();
//                position.y = p / projDev.getWidth();
//                Vec3D res = screen.getWorldToScreen().applyTo(inter);
//                PVector out = new PVector(res.x() / res.z(),
//                        1f - (res.y() / res.z()), 1);
//                touch.position.set(out);
            }
        }
        try {
            touchPointSemaphore.acquire();
        } catch (InterruptedException ie) {
            System.err.println("Semaphore Exception: " + ie);
        }

        TouchPointTracker.trackPoints(trackedElements, newElements, time);
        for (TrackedElement te : trackedElements) {
            te.filter(time);
        }

        touchPointSemaphore.release();

        return trackedElements;
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
//    

    public void setThresholds(float hue, float saturation, float brightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }

    public void setRedThreshold(float red) {
        this.redThreshold = red;
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

}
