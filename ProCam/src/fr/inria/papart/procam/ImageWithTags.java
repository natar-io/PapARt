/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import com.googlecode.javacv.cpp.ARToolKitPlus;
import com.googlecode.javacv.cpp.ARToolKitPlus.ARMultiMarkerInfoT;
import com.googlecode.javacv.cpp.ARToolKitPlus.MultiTracker;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import com.googlecode.javacv.CameraDevice;
import com.googlecode.javacv.cpp.opencv_highgui;

import java.io.File;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 *
 * @author jiii
 */
public class ImageWithTags {

    private CameraDevice cam;
    private PMatrix3D camIntrinsics;
    private MultiTracker tracker;
    private IplImage imageSrc;
    private float[] transfo;
    private int outWidth, outHeight;
    private MarkerBoard board;

    public ImageWithTags(String filename, String cameraConfig, String artConfig, MarkerBoard board, int outWidth, int outHeight) {

        this.outWidth = outWidth;
        this.outHeight = outHeight;
        this.board = board;

        
        // TODO: errors instead of assert
//        File f1 = new File(filename);
//        assert (f1.exists());

        String boardName = board.getFileName();
        File f2 = new File(boardName);
        assert (f2.exists());

        /////////////// Load the inital image  ////////////////
        imageSrc = opencv_highgui.cvLoadImage(filename);

        try {
            CameraDevice[] c = CameraDevice.read(cameraConfig);

            if (c.length > 0) {
                cam = c[0];
            }
        } catch (Exception e) {
            System.out.println("No camera error..." + e);
        }

        //////////// Load the camera parameters ////////////////
        double[] camMat = cam.cameraMatrix.get();
        camIntrinsics = new PMatrix3D((float) camMat[0], (float) camMat[1], (float) camMat[2], 0,
                (float) camMat[3], (float) camMat[4], (float) camMat[5], 0,
                (float) camMat[6], (float) camMat[7], (float) camMat[8], 0,
                0, 0, 0, 1);

        ///////////// Load ARToolkit parameters /////////////
        tracker = new MultiTracker(imageSrc.width(), imageSrc.height());

        int pixfmt = ARToolKitPlus.PIXEL_FORMAT_BGR;

        tracker.setPixelFormat(pixfmt);
        tracker.setBorderWidth(0.125f);
        tracker.activateAutoThreshold(true);
        tracker.setUndistortionMode(ARToolKitPlus.UNDIST_NONE);
        tracker.setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_RPP);
        tracker.setMarkerMode(ARToolKitPlus.MARKER_ID_BCH);
        tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_FULL_RES);
        tracker.setUseDetectLite(false);

        ARToolKitPlus.Logger log = new ARToolKitPlus.Logger(null);

        if (!tracker.init(artConfig, board.getFileName(), 1.0f, 1000.f, log)) {
            System.out.println("ERROR at ARToolkIT");
            // throw new Exception("Init ARTOOLKIT Error" + board.getFileName() + " " + board.getName());
        }
        transfo = new float[16];
        for (int i = 0; i < 3; i++) {
            transfo[12 + i] = 0;
        }
        transfo[15] = 0;

        ////////// Find the markers ///////////////
        //findMarkers();

    }

    public IplImage getImageFrom(String filename) {

        imageSrc = opencv_highgui.cvLoadImage(filename);

        tracker.calc(imageSrc.imageData());
        if (tracker.getNumDetectedMarkers() <= 0) {
            System.out.println("No marker found...");
            // TODO: error
        }
 System.out.println(tracker.getNumDetectedMarkers() + " markers found");
 
        ARMultiMarkerInfoT multiMarkerConfig = tracker.getMultiMarkerConfig();

        for (int i = 0; i < 12; i++) {
            transfo[i] = (float) multiMarkerConfig.trans().get(i);
        }

        TrackedView trackedView = new TrackedView(board, outWidth, outHeight);
        trackedView.setPos(transfo);
        trackedView.computeCorners(this);
        
        return trackedView.getImageIpl(imageSrc);
    }

    protected PVector getCamViewPoint(PVector pt) {
        PVector tmp = new PVector();
        camIntrinsics.mult(new PVector(pt.x, pt.y, pt.z), tmp);
        //TODO: lens distorsion ?
        return new PVector(tmp.x / tmp.z, tmp.y / tmp.z);
    }
}
