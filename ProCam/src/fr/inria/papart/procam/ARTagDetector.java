/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.ARToolKitPlus;
import com.googlecode.javacv.cpp.ARToolKitPlus.ArtLogFunction;
import com.googlecode.javacv.cpp.ARToolKitPlus.MultiTracker;
import java.util.logging.Logger;
import processing.core.PApplet;

/**
 *
 * @author jeremy
 */
public class ARTagDetector {

    protected ARTagDetector(PApplet applet, Camera camera, String cameraFile, int w, int h, MarkerBoard[] paperSheets, int type) {

        ArtLogFunction f = new ArtLogFunction() {
            @Override
            public void call(String nStr) {
                Logger.getLogger(MarkerDetector.class.getName()).warning(nStr);
            }
        };
        ARToolKitPlus.Logger log = new ARToolKitPlus.Logger(null);

        for (MarkerBoard sheet : paperSheets) {

            MultiTracker tracker = new MultiTracker(w, h);

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

            if (!tracker.init(cameraFile, sheet.getFileName(), 1.0f, 1000.f, log)) {
                System.err.println("Init ARTOOLKIT Error" + sheet.getFileName() + " " + sheet.getName());
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
