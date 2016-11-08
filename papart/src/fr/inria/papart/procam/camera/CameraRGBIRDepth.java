/*
 * Copyright (C) 2016 jiii.
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
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 *
 * @author jiii
 */
public abstract class CameraRGBIRDepth extends Camera {

    protected SubDepthCamera depthCamera;
    protected SubCamera colorCamera;
    protected SubCamera IRCamera;
    private SubCamera actAsCamera = null;

    protected boolean useIR = true;
    protected boolean useDepth = true;
    protected boolean useColor = true;
    protected boolean actAsColorCamera = false;
    protected boolean actAsIRCamera = false;

    @Override
    public void setParent(PApplet parent) {
        super.setParent(parent);
        depthCamera.setParent(parent);
        IRCamera.setParent(parent);
        colorCamera.setParent(parent);
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
        return depthCamera.getPImage();
    }

    public PImage getColorImage() {
        return colorCamera.getPImage();
    }

    public PImage getIRImage() {
        return IRCamera.getPImage();
    }

    public abstract void enableIR();

    public abstract void disableIR();

    public abstract void enableDepth();

    public abstract void disableDepth();

    public abstract void enableColor();

    public abstract void disableColor();

    public abstract void grabIR();

    public abstract void grabDepth();

    public abstract void grabColor();

    public void disable(SubCamera camera) {
        if (camera.type == SubCamera.Type.DEPTH) {
            disableDepth();
        }
        if (camera.type == SubCamera.Type.IR) {
            disableIR();
        }
        if (camera.type == SubCamera.Type.COLOR) {
            disableColor();
        }
    }

    void start(SubCamera camera) {
        if (camera.type == SubCamera.Type.DEPTH) {
            enableDepth();
        }
        if (camera.type == SubCamera.Type.IR) {
            enableIR();
        }
        if (camera.type == SubCamera.Type.COLOR) {
            enableColor();
        }
    }

    void grab(SubCamera camera) {
        if (camera.type == SubCamera.Type.DEPTH) {
            grabDepth();
        }
        if (camera.type == SubCamera.Type.IR) {
            grabIR();
        }
        if (camera.type == SubCamera.Type.COLOR) {
            grabColor();
        }
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

            if (actAsColorCamera || actAsIRCamera) {
                currentImage = actAsCamera.currentImage;
            }
        } catch (Exception e) {
            System.out.println("Exception :" + e);
            e.printStackTrace();
        }
    }

    public void actAsColorCamera() {
        actAsCamera = colorCamera;
    }

    public void actAsIRCamera() {
        actAsCamera = IRCamera;
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
    public void initMarkerDetection(String calibrationARToolkit) {
        actAsCamera.initMarkerDetection(calibrationARToolkit);
    }

    @Override
    public String getCalibrationARToolkit() {
        return actAsCamera.getCalibrationARToolkit();
    }

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
    protected boolean isPixelFormatGray() {
        return actAsCamera.isPixelFormatGray();
    }

    @Override
    protected boolean isPixelFormatColor() {
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
        actAsCamera.setClosing();
    }

    @Override
    public boolean isClosing() {
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

}
