/*
 * Copyright (C) 2016 RealityTech.
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
package fr.inria.papart.procam.camera;

import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.tracking.DetectedMarker;
import fr.inria.papart.tracking.MarkerBoard;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.OpenKinectFrameGrabber;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 *
 * @author Jeremy Laviole
 */
public abstract class CameraRGBIRDepth extends Camera {

    protected SubDepthCamera depthCamera;
    protected SubCamera colorCamera;
    protected SubCamera IRCamera;
    private SubCamera actAsCamera = null;

    protected boolean useIR = false;
    protected boolean useDepth = false;
    protected boolean useColor = false;

    public CameraRGBIRDepth() {
        colorCamera = new SubCamera(this, SubCamera.Type.COLOR);
        depthCamera = new SubDepthCamera(this, SubCamera.Type.DEPTH);
        IRCamera = new SubCamera(this, SubCamera.Type.IR);
    }

    @Override
    public void setParent(PApplet parent) {
        super.setParent(parent);
        if (depthCamera != null) {
            depthCamera.setParent(parent);
        }
        if (IRCamera != null) {
            IRCamera.setParent(parent);
        }
        if (colorCamera != null) {
            colorCamera.setParent(parent);
        }
    }

    public SubDepthCamera getDepthCamera() {
        return this.depthCamera;
    }

    public SubCamera getIRCamera() {
        return this.IRCamera;
    }

    public SubCamera getColorCamera() {
        return this.colorCamera;
    }

    public SubCamera getActingCamera() {
        return this.actAsCamera;
    }

    public opencv_core.IplImage getDepthImage() {
        return depthCamera.getIplImage();
    }

    public PImage getDepthPImage() {
        if (useDepth) {
            return depthCamera.getPImage();
        }
        return null;
    }

    public PImage getColorImage() {
        if (useColor) {
            return colorCamera.getPImage();
        }
        return null;
    }

    public PImage getIRImage() {
        if (useIR) {
            return IRCamera.getPImage();
        }
        return null;
    }

    protected abstract void grabIR();

    protected abstract void grabDepth();

    protected abstract void grabColor();

    public void disable(SubCamera camera) {
        if (camera.type == SubCamera.Type.DEPTH) {
            setUseDepth(false);
        }
        if (camera.type == SubCamera.Type.IR) {
            setUseIR(false);
        }
        if (camera.type == SubCamera.Type.COLOR) {
            setUseColor(false);
        }
    }

    void grab(SubCamera camera) {
        if (camera.type == SubCamera.Type.COLOR) {
            grabColor();
        }
        if (camera.type == SubCamera.Type.IR) {
            grabIR();
        }
        if (camera.type == SubCamera.Type.DEPTH) {
            grabDepth();
        }
    }

    protected abstract void internalStart() throws Exception;

    protected boolean isStarting = false;

    @Override
    public void start() {
        isStarting = true;
        try {
            if (isUseColor()) {
                colorCamera.start();
            }
            if (isUseDepth()) {
                depthCamera.start();
            }
            if (isUseIR()) {
                IRCamera.start();
            }
            internalStart();
            this.isConnected = true;
        } catch (Exception e) {
            System.err.println("Could not start frameGrabber... " + e);
            System.err.println("Depth camera ID " + this.systemNumber + " could not start.");
            System.err.println("Check cable connection and ID.");
            e.printStackTrace();
        }
        isStarting = false;
    }

    protected abstract void internalGrab() throws Exception;

    @Override
    public void grab() {

        if (this.isClosing()) {
            return;
        }
        // update the images.
        try {
            internalGrab();

            if (useColor) {
                colorCamera.grab();
            }

            if (useIR) {
                IRCamera.grab();
            }

            if (useDepth) {
                depthCamera.grab();
            }

            if (actAsCamera != null) {
                currentImage = actAsCamera.currentImage;
            }
        } catch (Exception e) {
            System.out.println("Exception :" + e);
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        this.isClosing = true;
        if (useColor) {
            colorCamera.close();
        }

        if (useIR) {
            IRCamera.close();
        }

        if (useDepth) {
            depthCamera.close();
        }
    }

    public void actAsColorCamera() {
        actAsCamera = colorCamera;
    }

    public void actAsIRCamera() {
        actAsCamera = IRCamera;
    }

    public void actAsDepthCamera() {
        actAsCamera = depthCamera;
    }

    // Generated delegate methods... 
    @Override
    public PImage getPImage() {
        return actAsCamera.getPImage();
    }

    @Override
    public String toString() {
        if (actAsCamera == null) {
            return "RGBDIR camear";
        }
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
    public void setSize(int width, int height) {
        actAsCamera.setSize(width, height);
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
    public void setCalibrationARToolkit(String calibrationARToolkit) {
        actAsCamera.setCalibrationARToolkit(calibrationARToolkit);
    }

    @Override
    public String getCalibrationARToolkit() {
        return actAsCamera.getCalibrationARToolkit();
    }

    @Override
    public void trackMarkerBoard(MarkerBoard sheet) {
//        System.out.println("In trackMarkerboard in CameaRGBIRDepth BAAAADD");
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

    /**
     * SetThread called by a subcamera, either color or IR (not checked).
     *
     * @param subCam
     */
    void setThread(SubCamera subCam) {
        if (thread == null) {
            thread = new CameraThread(this);
            thread.setCompute(subCam.trackSheets);
            subCam.thread = thread;

            thread.setCompute(trackSheets);
            thread.start();
        } else {
            System.err.println("Camera: Error Thread already launched");
        }
    }

    /**
     * Called from aynwhere, start the thread and use the acting camera to track
     * sheets of paper.
     */
    @Override
    public void setThread() {
        if (thread == null) {
            thread = new CameraThread(this);
            if (this.actAsCamera != null) {
                thread.setCompute(actAsCamera.trackSheets);
                actAsCamera.thread = thread;
            } else {
                thread.setCompute(trackSheets);
            }

            thread.start();
        } else {
            System.err.println("Camera: Error Thread already launched");
        }

        if (useDepth) {
            depthCamera.thread = thread;
        }
        if (useColor) {
            colorCamera.thread = thread;
        }
        if (useIR) {
            IRCamera.thread = thread;
        }
    }

    @Override
    public void forceCurrentImage(opencv_core.IplImage img) {
        actAsCamera.forceCurrentImage(img);
    }

    @Override
    protected void updateCurrentImage(opencv_core.IplImage img) {
        actAsCamera.updateCurrentImage(img);
    }

    @Override
    protected void checkCamImage() {
        actAsCamera.checkCamImage();
    }

    @Override
    public boolean isPixelFormatGray() {
        return actAsCamera.isPixelFormatGray();
    }

    @Override
    public boolean isPixelFormatColor() {
        return actAsCamera.isPixelFormatColor();
    }

    @Override
    public opencv_core.IplImage getIplImage() {
        return actAsCamera.getIplImage();
    }

    @Override
    public ProjectiveDeviceP getProjectiveDevice() {
        return actAsCamera.getProjectiveDevice();
    }

    @Override
    protected void setClosing() {
        super.setClosing();
    }

    @Override
    public boolean isClosing() {
        if (actAsCamera == null) {
            return super.isClosing();
        }
        return actAsCamera.isClosing();
    }

    @Override
    public Camera.PixelFormat getPixelFormat() {
        return actAsCamera.getPixelFormat();
    }

    @Override
    public void setPixelFormat(Camera.PixelFormat format) {
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

    public boolean isUseIR() {
        return useIR;
    }

    public void setUseIR(boolean useIR) {
        this.useIR = useIR;
    }

    public boolean isUseDepth() {
        return useDepth;
    }

    public void setUseDepth(boolean useDepth) {
        this.useDepth = useDepth;
    }

    public boolean isUseColor() {
        return useColor;
    }

    public void setUseColor(boolean useColor) {
        this.useColor = useColor;
    }

}
