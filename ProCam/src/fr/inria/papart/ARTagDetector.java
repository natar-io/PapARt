/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart;

import com.googlecode.javacpp.BytePointer;
import com.googlecode.javacpp.DoublePointer;
import com.googlecode.javacpp.IntPointer;
import com.googlecode.javacv.CameraDevice;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameGrabber.ColorMode;
import com.googlecode.javacv.MarkerDetector;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.ProjectorDevice;
import com.googlecode.javacv.cpp.ARToolKitPlus;
import com.googlecode.javacv.cpp.ARToolKitPlus.ARMarkerInfo;
import com.googlecode.javacv.cpp.ARToolKitPlus.ARMultiEachMarkerInfoT;
import com.googlecode.javacv.cpp.ARToolKitPlus.ARMultiMarkerInfoT;
import com.googlecode.javacv.cpp.ARToolKitPlus.ArtLogFunction;
import com.googlecode.javacv.cpp.ARToolKitPlus.MultiTracker;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.logging.Logger;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author jeremy
 */
public class ARTagDetector {

    private PApplet applet;
    private FrameGrabber grabber;
    private IplImage iimg;
    public PImage pimg;
    private MarkerDetector markerDetector;
    private CameraDevice cam;
    private MultiTracker tracker = null;
    private MultiTracker[] trackers = null;
    private String cameraFile;
    private float[] transfo;
    private float[][] transfos;
    int numDetect;
    public double[] projection = new double[12];
    public double[] modelview = new double[12];
    private IplImage img2 = null;
    HashMap<PaperSheet, float[]> transfosMap;
    HashMap<PaperSheet, MultiTracker> trackerMap;

    public ARTagDetector(int device, int w, int h, int framerate, String yamlCameraProj, String cameraFile, PaperSheet[] paperSheets) {
        this(device, null, w, h, framerate, yamlCameraProj, cameraFile, paperSheets);
    }

    public ARTagDetector(String fileName, int w, int h, int framerate, String yamlCameraProj, String cameraFile, PaperSheet[] paperSheets) {
        this(-1, fileName, w, h, framerate, yamlCameraProj, cameraFile, paperSheets);
    }

    private ARTagDetector(int device, String videoFile, int w, int h, int framerate, String yamlCameraProj, String cameraFile, PaperSheet[] paperSheets) {

        this.cameraFile = cameraFile;

        // check the files
        File f1 = new File(cameraFile);
        assert (f1.exists());
        for (PaperSheet p : paperSheets) {
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

            pimg = new PImage(w, h, PApplet.RGB);

            if (device == -1) {
                grabber = new OpenCVFrameGrabber(videoFile);
            } else {
                grabber = new OpenCVFrameGrabber(device);
            }
            grabber.setImageWidth(w);
            grabber.setImageHeight(h);
            grabber.setColorMode(ColorMode.RAW);
            grabber.setFrameRate(framerate);
            grabber.setDeinterlace(true);

            ArtLogFunction f = new ArtLogFunction() {

                @Override
                public void call(String nStr) {
                    Logger.getLogger(MarkerDetector.class.getName()).warning(nStr);
                }
            };
            ARToolKitPlus.Logger log = new ARToolKitPlus.Logger(null);

            // ARToolkitPlus tracker 
            trackers = new MultiTracker[paperSheets.length];
            int k = 0;

            transfosMap = new HashMap<PaperSheet, float[]>();
            trackerMap = new HashMap<PaperSheet, MultiTracker>();

            for (PaperSheet sheet : paperSheets) {

                MultiTracker tracker = new MultiTracker(w, h);

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

                trackerMap.put(sheet, tracker);
                transfosMap.put(sheet, transfo);
            }
            grabber.start();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void grab() {
        grab(false, false);
    }

    public void grab(boolean undistort) {
        grab(undistort, false);
    }

    public void grab(boolean undistort, boolean copy) {
        try {
            iimg = grabber.grab();

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
            }

        } catch (Exception e) {
            System.out.println("Exception in findMarkers " + e);
        }
    }

    public float[] findMarkers(PaperSheet sheet) {

        MultiTracker tracker = trackerMap.get(sheet);
        float[] transfo = transfosMap.get(sheet);


        if (tracker.getNumDetectedMarkers() < 0) {
            return transfo;
        }

        ARMultiMarkerInfoT multiMarkerConfig = tracker.getMultiMarkerConfig();
//        DoubleBuffer buff = multiMarkerConfig.trans().asBuffer();
        DoubleBuffer buff = multiMarkerConfig.trans().asBuffer(12);

        for (int i=0, k=0 ; i < 12; i++) {
            transfo[i] = (float) buff.get(i);
//            transfos[i] = (float) multiMarkerConfig.trans().get(i);
        }
        return null;

    }

    public float[][] findMultiMarkers(boolean undistort, boolean copy) {
        try {
            iimg = grabber.grab();

            if (undistort) {
                if (img2 == null) {
                    img2 = iimg.clone();
                }
                cam.undistort(iimg, img2);
                for (MultiTracker tracker : trackers) {
                    tracker.calc(img2.imageData());
                }
            } else {
                for (MultiTracker tracker : trackers) {
                    tracker.calc(img2.imageData());
                }
            }

            int trackId = 0;
            for (MultiTracker tracker : trackers) {
                if (tracker.getNumDetectedMarkers() < 0) {
                    continue;
                }

                ARMultiMarkerInfoT multiMarkerConfig = tracker.getMultiMarkerConfig();
//                DoubleBuffer buff = multiMarkerConfig.trans().asBuffer(12);
                DoubleBuffer buff = multiMarkerConfig.trans().asBuffer();
                int k = 0;

//                for (int i = 0; i < 12; i++) {
//                    transfos[trackId][i] = (float) buff.get(k++);
//                }
                for (int i = 0; i < 12; i++) {
                    transfos[trackId][i] = (float) multiMarkerConfig.trans().get(k++);
                }

//
                trackId++;
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
            }
            return transfos;

        } catch (Exception e) {
            System.out.println("Exception in findMarkers " + e);
        }

        return null;
    }
    // findMarkers avec une autre bibliothèque d'entrée vidéo que JavaCV
    int imgSizeByte = 640 * 480 * 4;
    BytePointer bp = null;
    ByteBuffer bb = null;
    IntBuffer ib = null;
// ------------------------ Working. ----------------

    public float[] findMarkers(PImage img) {

        if (bp == null) {
            bp = new BytePointer(imgSizeByte);
//            bb = bp.asByteBuffer();
//            bb = bp.asByteBuffer(imgSizeByte);
            ib = bb.asIntBuffer();
        }

        try {

            int k;

//            BufferedImage bufferedImage = (BufferedImage) img.getImage();
//            int[] imgData = new int[648 * 480];
//            imgData = bufferedImage.getRGB(0, 0, 640, 480, imgData, 0, 1);
//            System.out.println(imgData);

//            Raster data = bufferedImage.getData();
//
//            DataBuffer dataBuffer = data.getDataBuffer();
//            DataBufferInt dbi = (DataBufferInt) dataBuffer;
//            int[] imgData = dbi.getData();

//            ib.put(img.pixels);
//
//            for (int i = 0; i < 8; i++) {
//                System.out.print(bb.get(i) + " ");
//            }
//            System.out.print("\n");
//
//            ib.rewind();

            int[] px = img.pixels;
            k = 0;
            for (int i = 0; i < 640 * 480; i++) {
//                bb.put(k++, (byte) (px[i]>> 24));
                bb.put(k++, (byte) (px[i] >> 16));
                bb.put(k++, (byte) (px[i] >> 8));
                bb.put(k++, (byte) (px[i]));
            }

//            for (int i = 0; i < 8; i++) {
//                       System.out.print(bb.get(i) + " ");
//            }
//            System.out.print("\n");


            tracker.calc(bp);

            numDetect = tracker.getNumDetectedMarkers();
//            System.out.println(numDetect + " detected ");

            if (numDetect < 0) {
                return null;
            }

            tracker.getDetectedMarker(0);

            ARMultiMarkerInfoT multiMarkerConfig = tracker.getMultiMarkerConfig();
//            DoubleBuffer buff = multiMarkerConfig.trans().asBuffer(12);
            DoubleBuffer buff = multiMarkerConfig.trans().asBuffer();
            k = 0;
            for (int i = 0; i < 12; i++) {
                transfo[i] = (float) buff.get(k++);
            }

            return transfo;

        } catch (Exception e) {
            System.out.println("Exception : " + e);
        }
        return null;
    }

    public float[] findMarkers(boolean undistort) {
        return findMarkers(undistort, false);
    }

    public float[] findMarkers(boolean undistort, boolean copy) {
        try {
            iimg = grabber.grab();

            if (undistort) {
                if (img2 == null) {
                    img2 = iimg.clone();
                }
                cam.undistort(iimg, img2);
                tracker.calc(img2.imageData());
            } else {
                tracker.calc(iimg.imageData());
            }

            numDetect = tracker.getNumDetectedMarkers();
//            System.out.println(numDetect + " detected ");

            if (numDetect < 0) {
                return transfo;
            }

            tracker.getDetectedMarker(0);

            ARMultiMarkerInfoT multiMarkerConfig = tracker.getMultiMarkerConfig();
//            DoubleBuffer buff = multiMarkerConfig.trans().asBuffer(12);
            DoubleBuffer buff = multiMarkerConfig.trans().asBuffer();
            int k = 0;
//            for (int i = 0; i < 12; i++) {
//                transfo[i] = (float) buff.get(k++);
//            }

            for (int i = 0; i < 12; i++) {
                transfo[i] = (float) multiMarkerConfig.trans().get(k++);
            }

            // Image drawing
            if (copy) {
                ByteBuffer buff1 = iimg.getByteBuffer();
                pimg.loadPixels();
                for (int i = 0; i
                        < iimg.width() * iimg.height(); i++) {
                    int offset = i * 3;
//            ret.pixels[i] = applet.color(buff.get(offset + 0) & 0xff, buff.get(offset + 1) & 0xFF, buff.get(offset + 2) & 0xff);
                    pimg.pixels[i] = (buff1.get(offset + 2) & 0xFF) << 16
                            | (buff1.get(offset + 1) & 0xFF) << 8
                            | (buff1.get(offset) & 0xFF);
                }
                pimg.updatePixels();
            }
            return transfo;

        } catch (Exception e) {
            System.out.println("Exception in findMarkers " + e);
        }

        return null;
    }

    public float[][] findMultiMarkers(boolean undistort, boolean copy) {
        try {
            iimg = grabber.grab();

            if (undistort) {
                if (img2 == null) {
                    img2 = iimg.clone();
                }
                cam.undistort(iimg, img2);
                for (MultiTracker tracker : trackers) {
                    tracker.calc(img2.imageData());
                }
            } else {
                for (MultiTracker tracker : trackers) {
                    tracker.calc(img2.imageData());
                }
            }

            int trackId = 0;
            for (MultiTracker tracker : trackers) {
                if (tracker.getNumDetectedMarkers() < 0) {
                    continue;
                }

                ARMultiMarkerInfoT multiMarkerConfig = tracker.getMultiMarkerConfig();
//                DoubleBuffer buff = multiMarkerConfig.trans().asBuffer(12);
                DoubleBuffer buff = multiMarkerConfig.trans().asBuffer();
                int k = 0;

//                for (int i = 0; i < 12; i++) {
//                    transfos[trackId][i] = (float) buff.get(k++);
//                }
                for (int i = 0; i < 12; i++) {
                    transfos[trackId][i] = (float) multiMarkerConfig.trans().get(k++);
                }

//
                trackId++;
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
            }
            return transfos;

        } catch (Exception e) {
            System.out.println("Exception in findMarkers " + e);
        }

        return null;
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

    public void close() {
        try {
            grabber.stop();
        } catch (Exception e) {
        }
    }
}
