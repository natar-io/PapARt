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
import com.googlecode.javacv.ProjectiveDevice;
import com.googlecode.javacv.cpp.opencv_highgui;

import java.io.File;
import java.io.FileNotFoundException;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 *
 * @author jiii
 */
public class ImageWithTags {

    private CameraDevice cam;
    private final PMatrix3D camIntrinsics;
    private final MultiTracker tracker;
    private IplImage imageSrc;
    private final float[] transfo;
    private final int outWidth, outHeight;
    private final MarkerBoard board;

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
        } catch (ProjectiveDevice.Exception e) {
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

        if (!tracker.init(artConfig, board.getFileName(), 1.0f, 1000.f)) {
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

    public static PMatrix3D getCamMarkerTransform(PApplet pa,
            String cameraConfig,
            String artConfig,
            MarkerBoard board,
            String imageFilename) throws FileNotFoundException {

        /////////////// Load the inital image  ////////////////
        IplImage imageSrc = opencv_highgui.cvLoadImage(imageFilename);

        // Check the distorsions if available ? 
        return getCamMarkerTransform(pa, cameraConfig, artConfig, board, imageSrc);
    }

    public static PMatrix3D getCamMarkerTransform(PApplet pa,
            String cameraConfig,
            String artConfig,
            MarkerBoard board,
            IplImage imageSrc) throws FileNotFoundException {

        // TODO: errors instead of assert
//        File f1 = new File(filename);
//        assert (f1.exists());
        String boardName = board.getFileName();
        File f2 = new File(boardName);

        if (!f2.exists()) {
            throw new FileNotFoundException("MarkerBoard file not found.");
        }

        ProjectiveDeviceP cam;
        try {
            cam = ProjectiveDeviceP.loadCameraDevice(cameraConfig, 0);
        } catch (Exception e) {
            throw new FileNotFoundException("Camera configuration file error. " + e);
        }

        // TODO: check this...
        Camera.convertARParams(pa, cameraConfig, artConfig);

        assert (imageSrc.width() == cam.getWidth());
        assert (imageSrc.height() == cam.getHeight());

        ///////////// Load ARToolkit parameters /////////////
        MultiTracker tracker = new MultiTracker(cam.getWidth(), cam.getHeight());

        int pixfmt = ARToolKitPlus.PIXEL_FORMAT_BGR;
        tracker.setPixelFormat(pixfmt);
        tracker.setBorderWidth(0.125f);
//        tracker.activateAutoThreshold(true);
        tracker.activateAutoThreshold(false);
        tracker.setUndistortionMode(ARToolKitPlus.UNDIST_NONE);
        tracker.setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_RPP);
        tracker.setMarkerMode(ARToolKitPlus.MARKER_ID_BCH);
        tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_FULL_RES);
        tracker.setUseDetectLite(false);

        if (!tracker.init(artConfig, board.getFileName(), 1.0f, 1000.f)) {
            System.out.println("ERROR at ARToolkIT");
            // throw new Exception("Init ARTOOLKIT Error" + board.getFileName() + " " + board.getName());
        }

        float[] t = new float[16];
        for (int i = 0; i < 3; i++) {
            t[12 + i] = 0;
        }
        t[15] = 0;

        // Find the markers
        tracker.calc(imageSrc.imageData());

        if (tracker.getNumDetectedMarkers() <= 0) {
            return null;
        }

        ARToolKitPlus.ARMultiMarkerInfoT multiMarkerConfig = tracker.getMultiMarkerConfig();

        for (int i = 0; i < 12; i++) {
            t[i] = (float) multiMarkerConfig.trans().get(i);
        }

        return new PMatrix3D(t[0], t[1], t[2], t[3],
                t[4], t[5], t[6], t[7],
                t[8], t[9], t[10], t[11],
                0, 0, 0, 1);
    }

}
