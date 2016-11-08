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

import fr.inria.papart.procam.camera.Camera.Type;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraFactory {

    /**
     *
     * @param type either OPENCV_VIDEO or KINECT_VIDEO
     * @param cameraNo id of the Camera starting from 0.
     * @return
     */
    public static Camera createCamera(Camera.Type type, int cameraNo) {
        if (type == Type.PROCESSING) {
            throw new RuntimeException("PROCESSING_VIDEO requires a String describing the camera.");
        }

        Camera camera;
        CameraRealSense camRS;
        switch (type) {
            case OPENCV:
                camera = new CameraOpenCV(cameraNo);
                break;
            case OPEN_KINECT_2_RGB:
                camera = new CameraOpenKinect2(0);
                ((CameraRGBIRDepth) camera).actAsColorCamera();
                break;
            case OPEN_KINECT_2_IR:
                camera = new CameraOpenKinect2(0);
                ((CameraRGBIRDepth) camera).actAsIRCamera();
                break;
            case KINECT2_RGB:  // Hack for now with V4L loopback. 
                camera = new CameraOpenCV(0);
                break;
            case KINECT2_IR:  // Hack for now with V4L loopback. 
                camera = new CameraOpenCV(2);
                break;
            case OPENCV_DEPTH:
                camera = new CameraOpenCVDepth(cameraNo);
                break;
            case REALSENSE_RGB:
                camRS = new CameraRealSense(cameraNo);
                camRS.actAsColorCamera();
                camera = camRS;
                break;
            case REALSENSE_IR:
                camRS = new CameraRealSense(cameraNo);
                camRS.actAsIRCamera();
                camera = camRS;
                break;
//            case REALSENSE_DEPTH:
//                camRS = new CameraRealSense(cameraNo);
//                camRS.useColor(true);
//                camera = camRS;
//                break;
            case OPEN_KINECT_IR:
                camera = new CameraOpenKinect(cameraNo);
                ((CameraOpenKinect) camera).getIRVideo();
                break;
            case OPEN_KINECT:
                camera = new CameraOpenKinect(cameraNo);
                ((CameraRGBIRDepth) camera).actAsColorCamera();
                break;
            case FLY_CAPTURE:
                camera = new CameraFlyCapture(cameraNo);
                break;
            default:
                throw new RuntimeException("ProCam, Camera: Unspported camera Type");
        }
        return camera;
    }

    public static Camera createCamera(Camera.Type type, String description) {
        return createCamera(type, description, "");
    }

    /**
     *
     * @param type must be PROCESSING_VIDEO or FFMPEG
     * @param description device of the camera (/dev/video0) or name. see the
     * Processing GettingStartedCamera example to get the name.
     * @return
     */
    public static Camera createCamera(Camera.Type type, String description, String format) {
        Camera camera;

        switch (type) {
            case FFMPEG:
                camera = new CameraFFMPEG(description, format);
                break;
            case PROCESSING:
                camera = new CameraProcessing(description);
                break;
            default:
                boolean isInt = checkInt(description);
                if (!isInt) {
                    throw new RuntimeException("The description must be a number for this type.");
                }
                return createCamera(type, Integer.parseInt(description));
        }

        return camera;
    }

    private static boolean checkInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
