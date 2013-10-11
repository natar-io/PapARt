/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.ARToolKitPlus;
import com.googlecode.javacv.cpp.ARToolKitPlus.MultiTracker;
import com.googlecode.javacv.cpp.ARToolKitPlus.TrackerMultiMarker;
import java.util.logging.Logger;
import processing.core.PApplet;

/**
 *
 * @author jeremy
 */
public class ARTagDetector {

    protected ARTagDetector(PApplet applet, Camera camera, String cameraFile, int w, int h, MarkerBoard[] paperSheets, int type) {


        for (MarkerBoard sheet : paperSheets) {

            
    // create a tracker that does:
    //  - 6x6 sized marker images (required for binary markers)
    //  - samples at a maximum of 6x6 
    //  - works with luminance (gray) images
    //  - can load a maximum of 0 non-binary pattern
    //  - can detect a maximum of 8 patterns in one image
            
            
            TrackerMultiMarker tracker = new ARToolKitPlus.TrackerMultiMarker(w, h, 10, 6, 6, 6, 0);
            
            // ARToolKit 2.1.1 - version
            // MultiTracker tracker = new MultiTracker(w, h);

            //            int pixfmt = ARToolKitPlus.PIXEL_FORMAT_LUM;

            int pixfmt = 0;

            if (type == Camera.OPENCV_VIDEO) {
                pixfmt = ARToolKitPlus.PIXEL_FORMAT_BGR;
            }
            if (type == Camera.GSTREAMER_VIDEO) {
                pixfmt = ARToolKitPlus.PIXEL_FORMAT_ABGR;
            }

            tracker.setPixelFormat(pixfmt);
            tracker.setBorderWidth(0.125f);
            tracker.activateAutoThreshold(true);
            tracker.setUndistortionMode(ARToolKitPlus.UNDIST_NONE);
            tracker.setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_RPP);
            tracker.setMarkerMode(ARToolKitPlus.MARKER_ID_BCH);
            tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_FULL_RES);
//            tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_HALF_RES);
            tracker.setUseDetectLite(false);
//            tracker.setUseDetectLite(true);

            
            if (!tracker.init(cameraFile, sheet.getFileName(), 1.0f, 1000.f)) {
                System.err.println("Init ARTOOLKIT Error " +  cameraFile  + " "+ sheet.getFileName() + " " + sheet.getName());
            }

            float[] transfo = new float[16];
            for (int i = 0; i < 3; i++) {
                transfo[12 + i] = 0;
            }
            transfo[15] = 0;
            sheet.addTracker(applet, camera, tracker, transfo);
        }

    }

//    public float[] findMarkers(MarkerBoard sheet, IplImage img) {
//        sheet.updatePosition(img);
//        return sheet.getTransfo();
//    }
}
