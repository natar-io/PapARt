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

/**
 *
 * @author Jeremy Laviole
 */
public class CameraFactory {

    public static Camera createCamera(Camera.Type type) throws CannotCreateCameraException {
        return createCamera(type, "0", "");
    }

    public static Camera createCamera(Camera.Type type, String description) throws CannotCreateCameraException {
        return createCamera(type, description, "");
    }

    /**
     *
     * @param type must be PROCESSING_VIDEO or FFMPEG
     * @param description device of the camera (/dev/video0) or name. see the
     * Processing GettingStartedCamera example to get the name.
     * @param format either ffmpeg format information, or RGB, IR, DEPTH to
     * specify the default camera type.
     * @return
     */
    public static Camera createCamera(Camera.Type type, String description, String format) throws CannotCreateCameraException {
        Camera camera = null;
        CameraRGBIRDepth cameraMulti = null;
        int cameraNo = 0;

//        System.out.println("CameraFactory: new " + type.name() + ", " + description + " " + format);
        boolean isInt = false;
        try {
            cameraNo = Integer.parseInt(description);
            isInt = true;
        } catch (NumberFormatException e) {
            isInt = false;
        }
        boolean isRGB = format.equalsIgnoreCase("rgb") || format.equalsIgnoreCase("color");
        boolean isIR = format.equalsIgnoreCase("ir");
        boolean isDepth = format.equalsIgnoreCase("depth");

        try{
        switch (type) {
            case FFMPEG:
                camera = new CameraFFMPEG(description, format);
                break;
            case PROCESSING:
                camera = new CameraProcessing(description);
                break;
            // Depth Cameras
            case OPEN_KINECT_2:
                cameraMulti = new CameraOpenKinect2(cameraNo);
                break;
            case REALSENSE:
                cameraMulti = new CameraRealSense(cameraNo);
                break;
            case OPEN_KINECT:
                cameraMulti = new CameraOpenKinect(cameraNo);
                break;
            case OPENCV:
                camera = new CameraOpenCV(cameraNo);
                break;
            case FLY_CAPTURE:
                camera = new CameraFlyCapture(cameraNo);
                break;
            case OPENNI2:
                cameraMulti = new CameraOpenNI2(cameraNo);
                break;
            default:
                throw new RuntimeException("ProCam, Camera: Unspported camera Type");
        }

        if (cameraMulti != null) {
            if (isRGB) {  
                cameraMulti.setUseColor(true);
                cameraMulti.actAsColorCamera();
            }
            if (isIR) {
                cameraMulti.setUseIR(true);
                cameraMulti.actAsIRCamera();
            }
            if (isDepth) {
                cameraMulti.setUseDepth(true);
                cameraMulti.actAsDepthCamera();
            }
            return cameraMulti;
        }
        if (camera != null) {
            return camera;
        }
        } catch(NullPointerException e){
            throw new CannotCreateCameraException("Cannot create the camera type " + type.toString() + " " + description);
        }
        throw new RuntimeException("ProCam, Camera: Unspported camera Type");
    }

}
