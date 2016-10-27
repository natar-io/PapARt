/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
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
package fr.inria.papart.procam.camera;

import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.tracking.DetectedMarker;
import fr.inria.papart.tracking.MarkerBoard;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.RealSense;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.RealSenseFrameGrabber;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraRealSense extends Camera {

    protected RealSenseFrameGrabber grabber;
    protected CameraRealSenseDepth depthCamera;
    protected CameraRealSenseColor colorCamera;
    protected CameraRealSenseIR IRCamera;
    private boolean useIR = true;
    private boolean useDepth = true;
    private boolean useColor = true;

    private boolean actAsColorCamera = false;
    private boolean actAsIRCamera = false;

    private Camera actAsCamera = null;

    protected CameraRealSense(int cameraNo) {
        try {
            RealSenseFrameGrabber.tryLoad();
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(CameraRealSense.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.systemNumber = cameraNo;

        grabber = new RealSenseFrameGrabber(this.systemNumber);

        depthCamera = new CameraRealSenseDepth(this);
        colorCamera = new CameraRealSenseColor(this);
        IRCamera = new CameraRealSenseIR(this);
    }

    @Override
    public void setParent(PApplet parent) {
        super.setParent(parent);
        depthCamera.setParent(parent);
        IRCamera.setParent(parent);
        colorCamera.setParent(parent);
    }

    public RealSenseFrameGrabber getFrameGrabber() {
        return this.grabber;
    }

    public CameraRealSenseDepth getDepthCamera() {
        return this.depthCamera;
    }

    public CameraRealSenseIR getIRCamera() {
        return this.IRCamera;
    }

    public CameraRealSenseColor getColorCamera() {
        return this.colorCamera;
    }

    public Camera getActingCamera() {
        return this.actAsCamera;
    }

    public PMatrix3D getHardwareExtrinsics() {
        RealSense.extrinsics extrinsics = grabber.getRealSenseDevice().get_extrinsics(RealSense.color, RealSense.depth);
        FloatBuffer fb = extrinsics.position(0).asByteBuffer().asFloatBuffer();
        return new PMatrix3D(
                fb.get(0), fb.get(3), fb.get(6), -fb.get(9) * 1000f,
                fb.get(1), fb.get(4), fb.get(7), fb.get(10) * 1000f,
                fb.get(2), fb.get(5), fb.get(8), fb.get(11) * 1000f,
                0, 0, 0, 1);
    }

    public void setSize(int w, int h) {
        colorCamera.setSize(w, h);
    }

    @Override
    public void start() {

        if (useDepth) {
            depthCamera.start();
        }
        if (useColor) {
            colorCamera.start();
        }
        if (useIR) {
            IRCamera.start();
        }

        try {
            grabber.start();
            this.isConnected = true;
        } catch (Exception e) {
            System.err.println("Could not start frameGrabber... " + e);
            System.err.println("Could not camera start frameGrabber... " + e);
            System.err.println("Camera ID " + this.systemNumber + " could not start.");
            System.err.println("Check cable connection, ID and resolution asked.");

            this.grabber = null;
        }
    }

    @Override
    public void grab() {

        if (this.isClosing()) {
            return;
        }
        // update the images.
        try {
            grabber.grab();

            if (useColor) {
                colorCamera.grab();
                if (actAsColorCamera) {
                    currentImage = colorCamera.currentImage;
                }
            }

            if (useIR) {
                IRCamera.grab();
                if (actAsIRCamera) {
                    currentImage = IRCamera.currentImage;
                }
            }

            if (useDepth) {
                depthCamera.grab();
            }
        } catch (Exception e) {
            System.out.println("Exception :" + e);
            e.printStackTrace();
        }
    }

    public IplImage getDepthImage() {
        return depthCamera.getIplImage();
    }

    public PImage getDepthPImage() {
        return depthCamera.getPImage();
    }

    public PImage getColorImage() {
        return colorCamera.getPImage();
    }

    public PImage getIRImage() {
        return IRCamera.getPImage();
    }

    @Override
    public void close() {
        this.setClosing();
        if (grabber != null) {
            try {
                grabber.stop();
                System.out.println("Stopping grabber (RealSense)");

            } catch (Exception e) {
                System.out.println("Impossible to close " + e);
            }
        }
    }

    public void useDepth(boolean useDepth) {
        this.useDepth = useDepth;
    }

    public void useIR(boolean useIR) {
        this.useIR = useIR;
    }

    public void useColor(boolean useColor) {
        this.useColor = useColor;
    }

    public void actAsColorCamera(boolean isColorCam) {
        this.actAsColorCamera = isColorCam;
        if (isColorCam) {
            actAsCamera = colorCamera;
        }
    }

    public void actAsIRCamera(boolean isIRCam) {
        this.actAsIRCamera = isIRCam;
        if (isIRCam) {
            actAsCamera = IRCamera;
        }
    }

    // Generated delegate methods... 
    @Override
    public PImage getPImage() {
        return actAsCamera.getPImage();
    }

    @Override
    public String toString() {
        return actAsCamera.toString();
    }

    @Override
    public PImage getImage() {
        return actAsCamera.getImage();
    }

    @Override
    void setMarkers(DetectedMarker[] detectedMarkers) {
        actAsCamera.setMarkers(detectedMarkers);
    }

    @Override
    protected void checkParameters() {
        actAsCamera.checkParameters();
    }

    @Override
    public void setSimpleCalibration(float fx, float fy, float cx, float cy, int w, int h) {
        actAsCamera.setSimpleCalibration(fx, fy, cx, cy, w, h);
    }

    @Override
    public void setCalibration(String fileName) {
        actAsCamera.setCalibration(fileName);
    }

    @Override
    public PImage getPImageCopy() {
        return actAsCamera.getPImageCopy();
    }

    @Override
    public PImage getPImageCopy(PApplet context) {
        return actAsCamera.getPImageCopy(context);
    }

    @Override
    public PImage getPImageCopyTo(PImage out) {
        return actAsCamera.getPImageCopyTo(out);
    }

    @Override
    public void setCameraDevice(String description) {
        actAsCamera.setCameraDevice(description);
    }

    @Override
    protected String getCameraDevice() {
        return actAsCamera.getCameraDevice();
    }

    @Override
    public void setSystemNumber(int systemNumber) {
        actAsCamera.setSystemNumber(systemNumber);
    }

    @Override
    public void setFrameRate(int frameRate) {
        actAsCamera.setFrameRate(frameRate);
    }

    @Override
    public int width() {
        return actAsCamera.width();
    }

    @Override
    public int height() {
        return actAsCamera.height();
    }

    @Override
    public int getFrameRate() {
        return actAsCamera.getFrameRate();
    }

    @Override
    public boolean isUndistort() {
        return actAsCamera.isUndistort();
    }

    @Override
    public void setUndistort(boolean undistort) {
        actAsCamera.setUndistort(undistort);
    }

    @Override
    public boolean isCalibrated() {
        return actAsCamera.isCalibrated();
    }

    @Override
    public String getCalibrationFile() {
        return actAsCamera.getCalibrationFile();
    }

    @Override
    public void initMarkerDetection(String calibrationARToolkit) {
        actAsCamera.initMarkerDetection(calibrationARToolkit);
    }

    @Override
    public String getCalibrationARToolkit() {
        return actAsCamera.getCalibrationARToolkit();
    }
//
//    @Override
//    public void trackMarkerBoard(MarkerBoard sheet) {
//        sheet.addTracker(parent, actAsCamera);
//        try {
//            getSheetSemaphore().acquire();
//            actAsCamera.getSheets().add(sheet);
//            getSheetSemaphore().release();
//        } catch (InterruptedException ex) {
//            System.out.println("Interrupted !");
//            Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (NullPointerException e) {
//            throw new RuntimeException("Marker detection not initialized. " + e);
//        }
//    }

    @Override
    public void trackMarkerBoard(MarkerBoard sheet) {
        System.out.println("In trackMarkerboard in CameaRealSense BAAAADD");
        actAsCamera.trackMarkerBoard(sheet);
    }

    @Override
    public void trackSheets(boolean auto) {
        actAsCamera.trackSheets(auto);
    }

    @Override
    public boolean tracks(MarkerBoard board) {
        return actAsCamera.tracks(board);
    }

    @Override
    public List<MarkerBoard> getTrackedSheets() {
        return actAsCamera.getTrackedSheets();
    }

    @Override
    public DetectedMarker[] getDetectedMarkers() {
        return actAsCamera.getDetectedMarkers();
    }

    @Override
    protected Semaphore getSheetSemaphore() {
        return actAsCamera.getSheetSemaphore();
    }
    //
    @Override
    public void setThread() {
        if (thread == null) {
            thread = new CameraThread(this);
            thread.setCompute(actAsCamera.trackSheets);
            actAsCamera.thread = thread;
            thread.start();
        } else {
            System.err.println("Camera: Error Thread already launched");
        }
    }

    @Override
    public void forceCurrentImage(IplImage img) {
        actAsCamera.forceCurrentImage(img);
    }

    @Override
    protected void updateCurrentImage(IplImage img) {
        actAsCamera.updateCurrentImage(img);
    }

    @Override
    protected void checkCamImage() {
        actAsCamera.checkCamImage();
    }

    @Override
    protected boolean isPixelFormatGray() {
        return actAsCamera.isPixelFormatGray();
    }

    @Override
    protected boolean isPixelFormatColor() {
        return actAsCamera.isPixelFormatColor();
    }

    @Override
    public IplImage getIplImage() {
        return actAsCamera.getIplImage();
    }

    @Override
    public ProjectiveDeviceP getProjectiveDevice() {
        return actAsCamera.getProjectiveDevice();
    }

    @Override
    protected void setClosing() {
        actAsCamera.setClosing();
    }

    @Override
    public boolean isClosing() {
        return actAsCamera.isClosing();
    }

    @Override
    public PixelFormat getPixelFormat() {
        return actAsCamera.getPixelFormat();
    }

    @Override
    public void setPixelFormat(PixelFormat format) {
        actAsCamera.setPixelFormat(format);
    }

    @Override
    public boolean hasExtrinsics() {
        return actAsCamera.hasExtrinsics();
    }

    @Override
    public PMatrix3D getExtrinsics() {
        return actAsCamera.getExtrinsics();
    }

    @Override
    public void setExtrinsics(PMatrix3D extrinsics) {
        actAsCamera.setExtrinsics(extrinsics);
    }

    @Override
    public PVector getViewPoint(PVector point) {
        return actAsCamera.getViewPoint(point);
    }

}
