/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import com.googlecode.javacpp.BytePointer;
import com.googlecode.javacv.FrameGrabber.ImageMode;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.ARToolKitPlus;
import com.googlecode.javacv.cpp.ARToolKitPlus.ARMultiMarkerInfoT;
import com.googlecode.javacv.cpp.ARToolKitPlus.ArtLogFunction;
import com.googlecode.javacv.cpp.ARToolKitPlus.MultiTracker;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.logging.Logger;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public class ARTagDetector {

    private FrameGrabber grabber;
    private IplImage iimg;
    private PImage pimg;
    private CameraDevice cam;
    private IplImage img2 = null;
    private boolean lastUndistorted;
    private boolean isCopy = false;
//    static private boolean useSafeMode = false;

    public ARTagDetector(int device, int w, int h, int framerate, String yamlCameraProj, String cameraFile, MarkerBoard[] paperSheets) {
        this(device, null, w, h, framerate, yamlCameraProj, cameraFile, paperSheets);
    }

    public ARTagDetector(String fileName, int w, int h, int framerate, String yamlCameraProj, String cameraFile, MarkerBoard[] paperSheets) {
        this(-1, fileName, w, h, framerate, yamlCameraProj, cameraFile, paperSheets);
    }

    // TODO: ARTagDetector avec CameraDevice en parametre...
    // TODO: Gestion du Grabber dans la classe Camera...
    protected ARTagDetector(int device, String videoFile, int w, int h, int framerate, String yamlCameraProj, String cameraFile, MarkerBoard[] paperSheets) {

        // check the files
        File f1 = new File(cameraFile);
        assert (f1.exists());
        for (MarkerBoard p : paperSheets) {
            String name = p.getFileName();
            File f2 = new File(name);
            assert (f2.exists());
        }

        // Init the camera parameters
        try {
            CameraDevice[] c = CameraDevice.read(yamlCameraProj);
            if (c.length > 0) {
                cam = c[0];
            }

            if (device == -1) {
                grabber = new OpenCVFrameGrabber(videoFile);
            } else {
                if (videoFile == null) {
                    grabber = new OpenCVFrameGrabber(device);

                }
            }

            grabber.setImageWidth(w);
            grabber.setImageHeight(h);
            grabber.setImageMode(ImageMode.RAW);
            grabber.setFrameRate(framerate);
            grabber.setDeinterlace(true);

            pimg = new PImage(w, h, PApplet.RGB);

            initTracker(cameraFile, paperSheets);
            
            grabber.start();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    
    protected ARTagDetector(FrameGrabber grabber, String yamlCameraProj, String cameraFile, MarkerBoard[] paperSheets) {

        // Init the camera parameters
        try {
            this.grabber = grabber;
            initTracker(cameraFile, paperSheets);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    
    // The frame Grabber is already set
     private void initTracker(String cameraFile, MarkerBoard[] paperSheets) throws Exception{

            ArtLogFunction f = new ArtLogFunction() {
                @Override
                public void call(String nStr) {
                    Logger.getLogger(MarkerDetector.class.getName()).warning(nStr);
                }
            };
            ARToolKitPlus.Logger log = new ARToolKitPlus.Logger(null);

            for (MarkerBoard sheet : paperSheets) {

                MultiTracker tracker = new MultiTracker(grabber.getImageWidth(), grabber.getImageHeight());

                //            int pixfmt = ARToolKitPlus.PIXEL_FORMAT_LUM;
                int pixfmt = ARToolKitPlus.PIXEL_FORMAT_BGR;

                tracker.setPixelFormat(pixfmt);
                tracker.setBorderWidth(0.125f);
                tracker.activateAutoThreshold(true);
                tracker.setUndistortionMode(ARToolKitPlus.UNDIST_NONE);
                tracker.setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_RPP);
                tracker.setMarkerMode(ARToolKitPlus.MARKER_ID_BCH);
                tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_FULL_RES);
                tracker.setUseDetectLite(false);

                if (!tracker.init(cameraFile, sheet.getFileName(), 1.0f, 1000.f, log)) {
                    throw new Exception("Init ARTOOLKIT Error" + sheet.getFileName() + " " + sheet.getName());
                }

                float[] transfo = new float[16];
                for (int i = 0; i < 3; i++) {
                    transfo[12 + i] = 0;
                }
                transfo[15] = 0;
                sheet.setTracker(tracker, transfo);
            }
     }

    public void grab() {
        grab(false, isCopy);
    }

    public void grab(boolean undistort) {
        grab(undistort, isCopy);
    }
    public int nbImagesCopied = 0;

    public void grab(boolean undistort, boolean copy) {
        this.lastUndistorted = undistort;

        try {
            iimg = grabber.grab();
        } catch (Exception e) {
            System.out.println("Exception in Grabbing the frame " + e);
            return;
        }
        if (undistort) {
            if (img2 == null) {
                img2 = iimg.clone();
            }
            cam.undistort(iimg, img2);
        }

        // Image drawing
        if (copy) {
            ByteBuffer buff1 = iimg.getByteBuffer();
            pimg.loadPixels();
            for (int i = 0; i
                    < iimg.width() * iimg.height(); i++) {
                int offset = i * 3;
                pimg.pixels[i] = (buff1.get(offset + 2) & 0xFF) << 16
                        | (buff1.get(offset + 1) & 0xFF) << 8
                        | (buff1.get(offset) & 0xFF);
            }

            pimg.updatePixels();

            // TODO: HACK
            if (nbImagesCopied++ == 60) {
                System.gc();
                nbImagesCopied = 0;
            }

        }
    }

    public float[] findMarkers(MarkerBoard sheet) {
        return findMarkers(sheet, lastUndistorted ? img2 : iimg);
    }
    
    public float[] findMarkers(MarkerBoard sheet, IplImage img) {
        sheet.updatePosition(img);
        return sheet.getTransfo();
    }

    public boolean isReady(boolean undistort) {
        if (undistort) {
            return img2 != null;
        }
        return iimg != null;
    }

    public PImage getImage() {
        return pimg;
    }

    public IplImage getImageIpl() {
        if (img2 == null) {
            return iimg;
        }
        return img2;
    }

    public void setCopyToPimage(boolean isCopy) {
        this.isCopy = isCopy;
    }

    public void close() {
        try {
            grabber.stop();
        } catch (Exception e) {
        }
    }

    public FrameGrabber getGrabber() {
        return this.grabber;
    }
}
