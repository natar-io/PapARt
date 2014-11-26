/* 
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.inria.papart.multitouch;

import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.DepthPoint;
import org.bytedeco.javacpp.opencv_core.IplImage;

import fr.inria.papart.procam.display.ARDisplay;
import fr.inria.papart.procam.Screen;
import fr.inria.papart.depthcam.Kinect;
import fr.inria.papart.depthcam.calibration.PlaneAndProjectionCalibration;
import fr.inria.papart.procam.Camera;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.procam.camera.CameraOpenKinect;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PApplet;
import processing.core.PMatrix;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Matrix4x4;
import toxi.geom.Vec3D;

/**
 * Touch input, using a Kinect device for now.
 *
 * TODO: Refactor all this.
 *
 * @author jeremylaviole
 */
public class KinectTouchInput extends TouchInput {

    public static final int NO_TOUCH = -1;
    private int touch2DPrecision, touch3DPrecision;
    private Kinect kinect;
    private PApplet parent;

    private final Semaphore touchPointSemaphore = new Semaphore(1, true);
    private final Semaphore depthDataSem = new Semaphore(1);

// Tracking parameters
    static public final float trackNearDist = 30f;  // in mm
    static public final float trackNearDist3D = 70f;  // in mm

    // List of TouchPoints, given to the user
    private final CameraOpenKinect kinectCamera;

    private final PlaneAndProjectionCalibration calibration;

    // List of TouchPoints, given to the user
    ArrayList<TouchPoint> touchPoints2D = new ArrayList<TouchPoint>();
    ArrayList<TouchPoint> touchPoints3D = new ArrayList<TouchPoint>();
    private final TouchDetectionSimple2D touchDetection2D;
    private final TouchDetectionSimple3D touchDetection3D;

    public KinectTouchInput(PApplet applet,
            CameraOpenKinect kinectCamera,
            Kinect kinect,
            PlaneAndProjectionCalibration calibration) {
        this.parent = applet;
        this.kinect = kinect;
        this.kinectCamera = kinectCamera;
        this.calibration = calibration;
        this.touchDetection2D = new TouchDetectionSimple2D(Kinect.SIZE);
        this.touchDetection3D = new TouchDetectionSimple3D(Kinect.SIZE);
    }

    @Override
    public void update() {
        try {
            IplImage depthImage = kinectCamera.getDepthIplImage();
            IplImage colImage = kinectCamera.getIplImage();
            depthDataSem.acquire();

            if (colImage == null || depthImage == null) {
                return;
            }

            if (touch2DPrecision > 0 && touch3DPrecision > 0) {
                kinect.updateMT(depthImage, colImage, calibration, touch2DPrecision, touch3DPrecision);
                findAndTrack2D();
                findAndTrack3D();
            } else {
                if (touch2DPrecision > 0) {
                    kinect.updateMT2D(depthImage, colImage, calibration, touch2DPrecision);
                    findAndTrack2D();
                }
                if (touch3DPrecision > 0) {
                    kinect.updateMT3D(depthImage, colImage, calibration, touch3DPrecision);
                    findAndTrack3D();
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(KinectTouchInput.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            depthDataSem.release();
        }
    }

    private void updateDepthOnly() {

    }

    private void updateColor() {

    }

    @Override
    public TouchList projectTouchToScreen(Screen screen, BaseDisplay display) {

        TouchList touchList = new TouchList();

        try {
            touchPointSemaphore.acquire();
        } catch (InterruptedException ie) {
            System.err.println("Semaphore Exception: " + ie);
        }

        for (TouchPoint tp : touchPoints2D) {
            try {
                Touch touch = createTouch(screen, display, tp);
                touchList.add(touch);
            } catch (Exception e) {
                System.err.println("Intersection fail." + e);
            }
        }

        for (TouchPoint tp : touchPoints3D) {
            try {
                Touch touch = createTouch(screen, display, tp);
                touchList.add(touch);
            } catch (Exception e) {
                System.err.println("Intersection fail. " + e);
            }
        }

        touchPointSemaphore.release();
        return touchList;
    }

    private Touch createTouch(Screen screen, BaseDisplay display, TouchPoint tp) throws Exception {
        Touch touch = new Touch();
        projectPositionAndSpeed(screen, display, touch, tp);
        touch.is3D = tp.is3D();
        touch.touchPoint = tp;
        return touch;
    }

    // TODO: Raw Depth is for Kinect Only, find a cleaner solution.
    private ProjectiveDeviceP pdp;
    private boolean useRawDepth = false;

    public void useRawDepth(Camera camera) {
        this.useRawDepth = true;
        this.pdp = camera.getProjectiveDevice();
    }

    private void projectPositionAndSpeed(Screen screen,
            BaseDisplay display,
            Touch touch, TouchPoint tp) throws Exception {
        projectPosition(screen, display, touch, tp);
        projectSpeed(screen, display, touch, tp);
    }

    private void projectPosition(Screen screen,
            BaseDisplay display,
            Touch touch, TouchPoint tp) throws Exception {

        PVector paperScreenCoord = projectPointToScreen(screen,
                display,
                tp.getPositionKinect(),
                tp.getPosition());

        touch.setPosition(paperScreenCoord);
    }

    private void projectSpeed(Screen screen,
            BaseDisplay display,
            Touch touch, TouchPoint tp) {
        try {
            PVector paperScreenCoord = projectPointToScreen(screen,
                    display,
                    tp.getPreviousPositionKinect(),
                    tp.getPreviousPosition());

            touch.setPrevPos(paperScreenCoord);
        } catch (Exception e) {
            // Speed is set to 0
            touch.defaultPrevPos();
        }
    }

    public ArrayList<DepthPoint> projectDepthData(ARDisplay display, Screen screen) {
        ArrayList<DepthPoint> list = projectDepthData2D(display, screen);
        list.addAll(projectDepthData3D(display, screen));
        return list;
    }

    public ArrayList<DepthPoint> projectDepthData2D(ARDisplay display, Screen screen) {
        return projectDepthDataXD(display, screen, true);
    }

    public ArrayList<DepthPoint> projectDepthData3D(ARDisplay display, Screen screen) {
        return projectDepthDataXD(display, screen, false);
    }

    private ArrayList<DepthPoint> projectDepthDataXD(ARDisplay display, Screen screen, boolean is2D) {
        try {
            depthDataSem.acquire();
            DepthData depthData = kinect.getDepthData();
            ArrayList<DepthPoint> projected = new ArrayList<DepthPoint>();
            ArrayList<Integer> list = is2D ? depthData.validPointsList : depthData.validPointsList3D;
            for (Integer i : list) {
                DepthPoint depthPoint = tryCreateDepthPoint(display, screen, i);
                if (depthPoint != null) {
                    projected.add(depthPoint);
                }
            }
            depthDataSem.release();
            return projected;

        } catch (InterruptedException ex) {
            Logger.getLogger(KinectTouchInput.class
                    .getName()).log(Level.SEVERE, null, ex);

            return null;
        }
    }

    private DepthPoint tryCreateDepthPoint(ARDisplay display, Screen screen, int offset) {
        Vec3D vec = kinect.getDepthData().projectedPoints[offset];
        PVector projectedPt = new PVector(vec.x, vec.y, vec.z);

        try {
            PVector screenPosition = projectPointToScreen(screen, display,
                    kinect.getDepthData().kinectPoints[offset],
                    projectedPt);

            int c = kinect.getDepthData().pointColors[offset];
            return new DepthPoint(screenPosition.x, screenPosition.y, screenPosition.z, c);
        } catch (Exception e) {
//            System.out.println("tryCreateDepthPoint: Point creation exception : " + e);
//            e.printStackTrace();
            return null;
        }
    }

    private PVector projectPointToScreen(Screen screen,
            BaseDisplay display, Vec3D pKinect, PVector pNorm) throws Exception {

        PVector paperScreenCoord;
        if (useRawDepth) {

            // Method 1  -> Loose information of Depth !
            // Stays here, might be used later.
//            PVector p = pdp.worldToPixelCoord(pKinect);
//            paperScreenCoord = project(screen, display,
//                    p.x / (float) pdp.getWidth(),
//                    p.y / (float) pdp.getHeight());
            paperScreenCoord = new PVector();
            PVector pKinectP = new PVector(pKinect.x, pKinect.y, pKinect.z);

            PMatrix3D transfo = screen.getPosition().get();
            transfo.invert();
            transfo.mult(pKinectP, paperScreenCoord);
        } else {
            paperScreenCoord = project(screen, display,
                    pNorm.x,
                    pNorm.y);
            paperScreenCoord.z = pNorm.z;
        }
        return paperScreenCoord;
    }

    public void getTouch2DColors(IplImage colorImage) {
        getTouchColors(colorImage, this.touchPoints2D);
    }

    public void getTouchColors(IplImage colorImage,
            ArrayList<TouchPoint> touchPointList) {

        if (touchPointList.isEmpty()) {
            return;
        }
        ByteBuffer cBuff = colorImage.getByteBuffer();

        for (TouchPoint tp : touchPointList) {
            int offset = 3 * kinect.findColorOffset(tp.getPositionKinect());

            tp.setColor((255 & 0xFF) << 24
                    | (cBuff.get(offset + 2) & 0xFF) << 16
                    | (cBuff.get(offset + 1) & 0xFF) << 8
                    | (cBuff.get(offset) & 0xFF));
        }
    }

    // Raw versions of the algorithm are providing each points at each time. 
    // no updates, no tracking. 
    public ArrayList<TouchPoint> find2DTouchRaw(int skip) {
        assert (skip > 0);
        return touchDetection2D.compute(kinect.getDepthData(), skip);
    }

    public ArrayList<TouchPoint> find3DTouchRaw(int skip) {
        assert (skip > 0);
        return touchDetection3D.compute(kinect.getDepthData(), skip);
    }

    protected void findAndTrack2D() {
        assert (touch2DPrecision != 0);
        ArrayList<TouchPoint> newList = touchDetection2D.compute(
                kinect.getDepthData(),
                touch2DPrecision);
        TouchPointTracker.trackPoints(touchPoints2D, newList,
                parent.millis(), trackNearDist);
    }

    protected void findAndTrack3D() {
        assert (touch3DPrecision != 0);
        ArrayList<TouchPoint> newList = touchDetection3D.compute(
                kinect.getDepthData(),
                touch3DPrecision);
        TouchPointTracker.trackPoints(touchPoints3D, newList,
                parent.millis(),
                trackNearDist3D);
    }

    public void setPrecision(int precision2D, int precision3D) {
        setPrecision2D(precision2D);
        setPrecision3D(precision3D);
    }

    public void setPrecision2D(int precision) {
        this.touch2DPrecision = precision;
    }

    public void setPrecision3D(int precision) {
        this.touch3DPrecision = precision;
    }

    public ArrayList<TouchPoint> getTouchPoints2D() {
        return this.touchPoints2D;
    }

    public ArrayList<TouchPoint> getTouchPoints3D() {
        return this.touchPoints3D;
    }

    public PlaneAndProjectionCalibration getCalibration() {
        return calibration;
    }

    public boolean useRawDepth() {
        return useRawDepth;
    }

    public void lock() {
        try {
            touchPointSemaphore.acquire();
        } catch (Exception e) {
        }
    }

    public void unlock() {
        touchPointSemaphore.release();
    }

}
