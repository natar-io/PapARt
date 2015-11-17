/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.procam.camera;

import fr.inria.papart.procam.camera.Camera.Type;

/**
 *
 * @author jiii
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
        switch (type) {
            case OPENCV:
                camera = new CameraOpenCV(cameraNo);
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
            case OPEN_KINECT:
                camera = new CameraOpenKinect(cameraNo);
                break;
            case FLY_CAPTURE:
                camera = new CameraFlyCapture(cameraNo);
                break;
            default:
                throw new RuntimeException("ProCam, Camera: Unspported camera Type");
        }
        return camera;
    }

    /**
     *
     * @param type must be PROCESSING_VIDEO
     * @param description device of the camera (/dev/video0) or name. see the
     * Processing GettingStartedCamera example to get the name.
     * @return
     */
    public static Camera createCamera(Camera.Type type, String description) {
        Camera camera;

        if (type != Type.PROCESSING) {
            boolean isInt = checkInt(description);
            if (!isInt) {
                throw new RuntimeException("The description must be a numbrer for this type.");
            }
            camera = createCamera(type, Integer.parseInt(description));
        } else {
            camera = new CameraProcessing(description);
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
