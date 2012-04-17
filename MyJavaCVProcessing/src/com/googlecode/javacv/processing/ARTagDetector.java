/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.javacv.processing;

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
    private ProjectorDevice proj;
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

    public ARTagDetector(int device, int w, int h, int framerate, String yamlCameraProj, String cameraFile, String boardFile) {

        this.cameraFile = cameraFile;

        try {
            CameraDevice[] c = CameraDevice.read(yamlCameraProj);
            if (c.length > 0) {
                cam = c[0];
            }

            ProjectorDevice[] p = ProjectorDevice.read(yamlCameraProj);
            if (p.length > 0) {
                proj = p[0];
            }

            cam.cameraMatrix.get();
            proj.cameraMatrix.get();

//            System.out.println(proj.toString());
//            System.out.println(cam.toString());

            pimg = new PImage(w, h, PApplet.RGB);

//            grabber = new VideoInputFrameGrabber(device);
            grabber = new OpenCVFrameGrabber(device);

            grabber.setImageWidth(w);
            grabber.setImageHeight(h);
            grabber.setColorMode(ColorMode.RAW);
            //          grabber.setColorMode(ColorMode.GRAY);
            grabber.setFrameRate(framerate);
            grabber.setDeinterlace(true);

            double fr = grabber.getFrameRate();
//            System.out.println("Opening camera at framerate : " + framerate + " framerate obtained : " + fr);


//            markerDetector = new MarkerDetector();

//            System.out.println(cam.getSettings());
//            System.out.println(markerDetector.getSettings());
//            markerDetector.setSettings(Settings);

            tracker = new MultiTracker(w, h);
//            tracker = new MultiTracker(binarized.widthStep(), binarized.height());

//            System.out.println(tracker.getDescription());
            ArtLogFunction f = new ArtLogFunction() {

                @Override
                public void call(String nStr) {
                    Logger.getLogger(MarkerDetector.class.getName()).warning(nStr);
                }
            };

            ARToolKitPlus.Logger log = new ARToolKitPlus.Logger(null);

            File f1 = new File(cameraFile);
            File f2 = new File(boardFile);

            assert (f1.exists());
            assert (f2.exists());

//            int pixfmt = ARToolKitPlus.PIXEL_FORMAT_LUM;
            int pixfmt = ARToolKitPlus.PIXEL_FORMAT_BGR;

//        switch (nChannels) {
//            case 4: pixfmt = PIXEL_FORMAT_BGRA; break;
//            case 3: pixfmt = PIXEL_FORMAT_BGR;  break;
//            case 1: pixfmt = PIXEL_FORMAT_LUM;  break;
//            default:
//                throw new Exception("Unsupported format: No support for IplImage with " + channels + " channels.");
//        }
            tracker.setPixelFormat(pixfmt);

            // the marker in the BCH test image has a thiner border...
            tracker.setBorderWidth(0.125f);

            // set a threshold. we could also activate automatic thresholding
//            tracker.setThreshold(160);
            tracker.activateAutoThreshold(true);

            // let's use lookup-table undistortion for high-speed
            // note: LUT only works with images up to 1024x1024
//            tracker.setUndistortionMode(ARToolKitPlus.UNDIST_LUT);
            tracker.setUndistortionMode(ARToolKitPlus.UNDIST_NONE);

            // RPP is more robust than ARToolKit's standard pose estimator
            tracker.setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_RPP);
//            tracker.setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_ORIGINAL);

            // switch to simple ID based markers
            // use the tool in tools/IdPatGen to generate markers
            tracker.setMarkerMode(ARToolKitPlus.MARKER_ID_BCH);

            tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_FULL_RES);
//            tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_HALF_RES);

            tracker.setUseDetectLite(false);
//            tracker.setUseDetectLite(true);

            if (!tracker.init(cameraFile, boardFile, 1.0f, 1000.f, log)) {
                throw new Exception("Init ARTOOLKIT Error");
            }

            transfo = new float[16];


            for (int i = 0; i < 3; i++) {
                transfo[12 + i] = 0;
            }
            transfo[15] = 0;

            grabber.start();


        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public ARTagDetector(int device, int w, int h, int framerate, String yamlCameraProj, String cameraFile, String[] boardFile) {

        this.cameraFile = cameraFile;

        // check the files
        File f1 = new File(cameraFile);
        assert (f1.exists());
        for (String name : boardFile) {
            File f2 = new File(name);
            assert (f2.exists());
        }

        // Init the camera parameters
        try {
            CameraDevice[] c = CameraDevice.read(yamlCameraProj);
            if (c.length > 0) {
                cam = c[0];
            }

            ProjectorDevice[] p = ProjectorDevice.read(yamlCameraProj);
            if (p.length > 0) {
                proj = p[0];
            }

            cam.cameraMatrix.get();
            proj.cameraMatrix.get();
            pimg = new PImage(w, h, PApplet.RGB);
            grabber = new OpenCVFrameGrabber(device);
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
            trackers = new MultiTracker[boardFile.length];
            transfos = new float[boardFile.length][];
            int k = 0;
            for (String name : boardFile) {

                trackers[k] = new MultiTracker(w, h);

                //            int pixfmt = ARToolKitPlus.PIXEL_FORMAT_LUM;
                int pixfmt = ARToolKitPlus.PIXEL_FORMAT_BGR;

                trackers[k].setPixelFormat(pixfmt);
                trackers[k].setBorderWidth(0.125f);
                trackers[k].activateAutoThreshold(true);
                trackers[k].setUndistortionMode(ARToolKitPlus.UNDIST_NONE);
                trackers[k].setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_RPP);
                trackers[k].setMarkerMode(ARToolKitPlus.MARKER_ID_BCH);
                trackers[k].setImageProcessingMode(ARToolKitPlus.IMAGE_FULL_RES);
                trackers[k].setUseDetectLite(false);

                if (!trackers[k].init(cameraFile, name, 1.0f, 1000.f, log)) {
                    throw new Exception("Init ARTOOLKIT Error");
                }

                transfos[k] = new float[16];
                for (int i = 0; i < 3; i++) {
                    transfos[k][12 + i] = 0;
                }
                transfos[k][15] = 0;
                k++;
            }
            grabber.start();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


        public ARTagDetector(String videoFile, int w, int h, int framerate, String yamlCameraProj, String cameraFile, String[] boardFile) {

        this.cameraFile = cameraFile;

        // check the files
        File f1 = new File(cameraFile);
        assert (f1.exists());
        for (String name : boardFile) {
            File f2 = new File(name);
            assert (f2.exists());
        }

        // Init the camera parameters
        try {
            CameraDevice[] c = CameraDevice.read(yamlCameraProj);
            if (c.length > 0) {
                cam = c[0];
            }

            ProjectorDevice[] p = ProjectorDevice.read(yamlCameraProj);
            if (p.length > 0) {
                proj = p[0];
            }

            cam.cameraMatrix.get();
            proj.cameraMatrix.get();
            pimg = new PImage(w, h, PApplet.RGB);
            grabber = new OpenCVFrameGrabber(videoFile);
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
            trackers = new MultiTracker[boardFile.length];
            transfos = new float[boardFile.length][];
            int k = 0;
            for (String name : boardFile) {

                trackers[k] = new MultiTracker(w, h);

                //            int pixfmt = ARToolKitPlus.PIXEL_FORMAT_LUM;
                int pixfmt = ARToolKitPlus.PIXEL_FORMAT_BGR;

                trackers[k].setPixelFormat(pixfmt);
                trackers[k].setBorderWidth(0.125f);
                trackers[k].activateAutoThreshold(true);
                trackers[k].setUndistortionMode(ARToolKitPlus.UNDIST_NONE);
                trackers[k].setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_RPP);
                trackers[k].setMarkerMode(ARToolKitPlus.MARKER_ID_BCH);
                trackers[k].setImageProcessingMode(ARToolKitPlus.IMAGE_FULL_RES);
                trackers[k].setUseDetectLite(false);

                if (!trackers[k].init(cameraFile, name, 1.0f, 1000.f, log)) {
                    throw new Exception("Init ARTOOLKIT Error");
                }

                transfos[k] = new float[16];
                for (int i = 0; i < 3; i++) {
                    transfos[k][12 + i] = 0;
                }
                transfos[k][15] = 0;
                k++;
            }
            grabber.start();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public ARTagDetector(int w, int h, String yamlCameraProj, String cameraFile, String boardFile) {

        this.cameraFile = cameraFile;

        try {
            CameraDevice[] c = CameraDevice.read(yamlCameraProj);
            if (c.length > 0) {
                cam = c[0];
            }

            ProjectorDevice[] p = ProjectorDevice.read(yamlCameraProj);
            if (p.length > 0) {
                proj = p[0];
            }

            cam.cameraMatrix.get();
            proj.cameraMatrix.get();

//            System.out.println(proj.toString());
//            System.out.println(cam.toString());


            tracker = new MultiTracker(w, h);
//            tracker = new MultiTracker(binarized.widthStep(), binarized.height());

//            System.out.println(tracker.getDescription());
            ArtLogFunction f = new ArtLogFunction() {

                @Override
                public void call(String nStr) {
                    Logger.getLogger(MarkerDetector.class.getName()).warning(nStr);
                }
            };


            ARToolKitPlus.Logger log = new ARToolKitPlus.Logger(null);

            File f1 = new File(cameraFile);
            File f2 = new File(boardFile);

            assert (f1.exists());
            assert (f2.exists());

//            int pixfmt = ARToolKitPlus.PIXEL_FORMAT_LUM;
            int pixfmt = ARToolKitPlus.PIXEL_FORMAT_BGR;

//        switch (nChannels) {
//            case 4: pixfmt = PIXEL_FORMAT_BGRA; break;
//            case 3: pixfmt = PIXEL_FORMAT_BGR;  break;
//            case 1: pixfmt = PIXEL_FORMAT_LUM;  break;
//            default:
//                throw new Exception("Unsupported format: No support for IplImage with " + channels + " channels.");
//        }
            tracker.setPixelFormat(pixfmt);

            // the marker in the BCH test image has a thiner border...
            tracker.setBorderWidth(0.125f);

            // set a threshold. we could also activate automatic thresholding
//            tracker.setThreshold(160);

            tracker.activateAutoThreshold(true);


            // let's use lookup-table undistortion for high-speed
            // note: LUT only works with images up to 1024x1024
//            tracker.setUndistortionMode(ARToolKitPlus.UNDIST_LUT);
            tracker.setUndistortionMode(ARToolKitPlus.UNDIST_NONE);

            // RPP is more robust than ARToolKit's standard pose estimator
            tracker.setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_RPP);
//            tracker.setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_ORIGINAL);

            // switch to simple ID based markers
            // use the tool in tools/IdPatGen to generate markers
            tracker.setMarkerMode(ARToolKitPlus.MARKER_ID_BCH);

            tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_FULL_RES);
//            tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_HALF_RES);

            tracker.setUseDetectLite(false);

            if (!tracker.init(cameraFile, boardFile, 1.0f, 1000.f, log)) {
                throw new Exception("Init ARTOOLKIT Error");
            }
            transfo = new float[16];
            for (int i = 0; i < 3; i++) {
                transfo[12 + i] = 0;
            }
            transfo[15] = 0;

        } catch (Exception e) {
            System.out.println("ERROR while initializing " + e);
        }
    }

    public void showMultiConfig() {

        ARMultiMarkerInfoT multiMarkerConfig = tracker.getMultiMarkerConfig();
        int nb = multiMarkerConfig.marker_num();
        System.out.println("Config : " + nb + " markers  \n Marker Matrices :");

        ARMultiEachMarkerInfoT marker = multiMarkerConfig.marker();

        for (int m = 0; m < nb; m++) {

            marker.position(m);
            System.out.println("Marker : " + m + " id : " + marker.patt_id());
//            DoubleBuffer buff = marker.trans().asBuffer(12);
            DoubleBuffer buff = marker.trans().asBuffer();

            int k = 0;
            System.out.println("Trans ");
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 4; j++) {
                    System.out.print(buff.get(k++) + " ");
                }
                System.out.println("");
            }
        }

    }

    public void grab() {
        try {
            iimg = grabber.grab();
        } catch (Exception e) {
        }
    }
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

    public IplImage getImageIpl(){
        if(img2 == null)
            return iimg;
        return img2;
    }

//    public void calcParams() {
//
//        DoublePointer ModelviewMatrix = tracker.getModelViewMatrix();
////        DoubleBuffer asBuffer = ModelviewMatrix.asBuffer();
//        DoubleBuffer asBuffer = ModelviewMatrix.asBuffer(16);
//        int k = 0;
//        for (int i = 0; i < 12; i++) {
//            modelview[k++] = asBuffer.get(k);
//        }
//        DoublePointer projectionMatrix = tracker.getProjectionMatrix();
//        asBuffer = projectionMatrix.asBuffer(16);
////        asBuffer = projectionMatrix.asBuffer();
//        k = 0;
//        for (int i = 0; i < 12; i++) {
//            projection[k++] = asBuffer.get(k);
//        }
//    }
//    public void print() {
//
//        ///////////////////////////////
//        // Detected Markers
//        int k = 0;
//        IntPointer intPointer = new IntPointer(numDetect);
//        tracker.getDetectedMarkers(intPointer);
//        for (int i = 0; i < numDetect; i++) {
////            int id = intPointer.asBuffer().get(i);
//            int id = intPointer.asBuffer(6).get(i);
////            ARMarkerInfo detectedMarker = tracker.getDetectedMarker(intPointer.asBuffer().get(i));
//            ARMarkerInfo detectedMarker = tracker.getDetectedMarker(intPointer.asBuffer(6).get(i));
//            DoublePointer pos = detectedMarker.pos();
////            DoubleBuffer buff = pos.asBuffer();
//            DoubleBuffer buff = pos.asBuffer(2);
//            System.out.println("Marker " + id + " found at " + buff.get(0) + " " + buff.get(1));
//        }
//
//        ///////////////////////////////
//        // Transformation sent
//        ARMultiMarkerInfoT multiMarkerConfig = tracker.getMultiMarkerConfig();
//        DoubleBuffer buff = multiMarkerConfig.trans().asBuffer(12);
////        DoubleBuffer buff = multiMarkerConfig.trans().asBuffer();
//        k = 0;
//        System.out.println("Trans ");
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 4; j++) {
//
//                System.out.print(buff.get(k++) + " ");
//            }
//            System.out.println("");
//        }
//
//
//// //            tracker.calcOpenGLMatrixFromMarker()
//        DoublePointer ModelviewMatrix = tracker.getModelViewMatrix();
//        DoubleBuffer asBuffer = ModelviewMatrix.asBuffer(16);
////        DoubleBuffer asBuffer = ModelviewMatrix.asBuffer();
//        k = 0;
//        System.out.println("Modelview Matrix ");
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 4; j++) {
//                modelview[k] = asBuffer.get(k);
//                System.out.print(asBuffer.get(k++) + " ");
//            }
//            System.out.println("");
//        }
//
//
//        DoublePointer projectionMatrix = tracker.getProjectionMatrix();
//        asBuffer = projectionMatrix.asBuffer(16);
////        asBuffer = projectionMatrix.asBuffer();
//        k = 0;
//        System.out.println("Projection Matrix ");
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 4; j++) {
//                projection[k] = asBuffer.get(k);
//                System.out.print(asBuffer.get(k++) + " ");
//            }
//            System.out.println("");
//        }
//
//    }
    public void close() {
        try {
            grabber.stop();
        } catch (Exception e) {
        }
    }
}
