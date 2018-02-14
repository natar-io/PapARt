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
package fr.inria.papart.tracking;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.logging.Logger;

import static org.bytedeco.javacpp.opencv_calib3d.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_features2d.*;
import static org.bytedeco.javacpp.opencv_flann.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacv.BaseChildSettings;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;

/**
 *
 * @author Samuel Audet
 *
 * ObjectFinder does not work out-of-the-box under Android, because it lacks the
 * standard java.beans.beancontext package. We can work around it by doing the
 * following *BEFORE* following the instructions in the README.md file:
 *
 * 1. Remove BaseChildSettings.class and BaseSettings.class from javacv.jar 2.
 * Follow the instructions in the README.md file 3. In your project, define
 * empty classes BaseChildSettings and BaseSettings under the
 * org.bytedeco.javacv package name
 */
public class ObjectFinder {

    public static int briskParam1 = 15;
    public static int briskParam2 = 4;
    public static float briskParam3 = 1;

    /**
     * Find an object looking like the objectImage.
     * @param objectImage image to find
     */
    public ObjectFinder(IplImage objectImage) {
        settings = new Settings();
        settings.setObjectImage(objectImage);
        setSettings(settings);
    }

    /**
     * Test purposes
     * @param settings 
     */
    public ObjectFinder(Settings settings) {
        setSettings(settings);
    }

    public static class Settings extends BaseChildSettings {

        IplImage objectImage = null;
//        ORB detector = ORB.create();

        // Marche très bien
        ORB detector = ORB.create(1200/*=500*/, 1.6f /*=1.2f*/, 8 /*=8*/, 31/*=31*/,
            0/*=0*/, 2/*=2*/, ORB.HARRIS_SCORE/*=cv::ORB::HARRIS_SCORE*/, 31 /*=31*/, 20/*=20*/);
        // default--> quite awesome 
//        BRISK detector = BRISK.create(50, 2, 1);

        // Tests 
//        BRISK detector = BRISK.create(briskParam1, briskParam2, briskParam3);
//        AKAZE detector = AKAZE.create();  // -> CRAZY memory leaks ?!
//        AKAZE detector = AKAZE.create(AKAZE.DESCRIPTOR_KAZE,
//                    0, 3, 0.001f,
//                    4, 4, KAZE.DIFF_CHARBONNIER);
//
//         create(int descriptor_type/*=cv::AKAZE::DESCRIPTOR_MLDB*/,
//                                         int descriptor_size/*=0*/, int descriptor_channels/*=3*/,
//                                         float threshold/*=0.001f*/, int nOctaves/*=4*/,
//                                         int nOctaveLayers/*=4*/, int diffusivity/*=cv::KAZE::DIFF_PM_G2*/);
//        MSER detector = MSER.create();  // -> Not implemented.
//        FastFeatureDetector detector = FastFeatureDetector.create();  // -> Not implemented.
//        AgastFeatureDetector detector = AgastFeatureDetector.create(); // -> not implemented
        double distanceThreshold = 0.75;
        int matchesMin = 8;
        double ransacReprojThreshold = 2.0;

// Flann causes a native crash 
//        boolean useFLANN = true;
        boolean useFLANN = false;

        public IplImage getObjectImage() {
            return objectImage;
        }

        public void setObjectImage(IplImage objectImage) {
            this.objectImage = objectImage;
        }

        public double getDistanceThreshold() {
            return distanceThreshold;
        }

        public void setDistanceThreshold(double distanceThreshold) {
            this.distanceThreshold = distanceThreshold;
        }

        public int getMatchesMin() {
            return matchesMin;
        }

        public void setMatchesMin(int matchesMin) {
            this.matchesMin = matchesMin;
        }

        public double getRansacReprojThreshold() {
            return ransacReprojThreshold;
        }

        public void setRansacReprojThreshold(double ransacReprojThreshold) {
            this.ransacReprojThreshold = ransacReprojThreshold;
        }

        public boolean isUseFLANN() {
            return useFLANN;
        }

        public void setUseFLANN(boolean useFLANN) {
            this.useFLANN = useFLANN;
        }
    }

    Settings settings;

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;

        objectKeypoints = new KeyPointVector();
        objectDescriptors = new Mat();
        settings.detector.detectAndCompute(cvarrToMat(settings.objectImage),
                new Mat(), objectKeypoints, objectDescriptors, false);

       
        int total = (int) objectKeypoints.size();
        if (settings.useFLANN) {
            indicesMat = new Mat(total, 2, CV_32SC1);
            distsMat = new Mat(total, 2, CV_32FC1);
            flannIndex = new Index();
            indexParams = new LshIndexParams(12, 20, 2); // using LSH Hamming distance
            searchParams = new SearchParams(64, 0, true); // maximum number of leafs checked
        }
        pt1 = new Mat(total, 1, CV_32FC2);
        pt2 = new Mat(total, 1, CV_32FC2);
        mask = new Mat(total, 1, CV_8UC1);
        H = new Mat(3, 3, CV_64FC1);
        ptpairs = new ArrayList<Integer>(2 * objectDescriptors.rows());
         System.out.println("KeyPoints found: " + total + " " + objectDescriptors.rows());
        logger.info(total + " object descriptors");
    }

    static final Logger logger = Logger.getLogger(ObjectFinder.class.getName());

    KeyPointVector objectKeypoints = null, imageKeypoints = null;
    Mat objectDescriptors = null, imageDescriptors = null;
    Mat indicesMat, distsMat;
    Index flannIndex = null;
    IndexParams indexParams = null;
    SearchParams searchParams = null;
    Mat pt1 = null, pt2 = null, mask = null, H = null;
    ArrayList<Integer> ptpairs = null;

    CvRect roi = null;
    CvRect defaultRoi = null;

    public double[] find(IplImage image) {
        if (objectDescriptors.rows() < settings.getMatchesMin()) {
            System.out.println("Object descriptor problem " + objectDescriptors.rows());
            return null;
        }
        imageKeypoints = new KeyPointVector();
        imageDescriptors = new Mat();

        // mask is a single channel image. 
        if (roi == null) {
            roi = cvRect(0, 0, image.width(), image.height());
            defaultRoi = cvRect(0, 0, image.width(), image.height());
        }

        cvSetImageROI(image, roi);

        System.out.println("retreived roi: " + roi.x() + " " + roi.y() + " " + roi.width() + " " + roi.height());
        long startTime = System.currentTimeMillis();
        settings.detector.detectAndCompute(cvarrToMat(image),
                new Mat(), imageKeypoints, imageDescriptors, false);
        //                cvarrToMat(maskImg), imageKeypoints, imageDescriptors, false);

        if (imageDescriptors.rows() < settings.getMatchesMin()) {
            // Reset the mask, nothing found.
            roi.x(0);
            roi.y(0);
            roi.width(image.width());
            roi.height(image.height());
            cvSetImageROI(image, defaultRoi);
            return null;
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
//        System.out.println("Detection time: " + totalTime + " ms.");
        int total = (int) imageKeypoints.size();
        logger.info(total + " image descriptors");
        System.out.println(total + " image descriptors");

        int w = settings.objectImage.width();
        int h = settings.objectImage.height();
        double[] srcCorners = {0, 0, w, 0, w, h, 0, h};
        double[] dstCorners = locatePlanarObject(objectKeypoints, objectDescriptors,
                imageKeypoints, imageDescriptors, srcCorners, image.roi());

        if (dstCorners == null) {
            roi.x(0);
            roi.y(0);
            roi.width(image.width());
            roi.height(image.height());
            cvSetImageROI(image, defaultRoi);
            return null;
        }

        adjustROI(image, dstCorners);
        // update mask from dstCorners... 

        cvSetImageROI(image, defaultRoi);
        System.out.println("Updated roi: " + roi.x() + " " + roi.y());
        return dstCorners;
    }

    private void adjustROI(IplImage image, double[] dstCorners) {
        int minX = image.width(), minY = image.height(), maxX = 0, maxY = 0;
        for (int i = 0; i < 4; i++) {
            int x = (int) dstCorners[i * 2];
            int y = (int) dstCorners[i * 2 + 1];
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }
        }
        int larger = 100;
        minX -= larger;
        maxX += larger;
        minY -= larger;
        maxY += larger;
        if (minX < 0) {
            minX = 0;
        }
        if (maxX >= image.width()) {
            maxX = image.width() - 1;
        }
        if (minY < 0) {
            minY = 0;
        }
        if (maxY >= image.height()) {
            maxY = image.height() - 1;
        }
        roi.x(minX);
        roi.y(minY);
        roi.width(maxX - minX);
        roi.height(maxY - minY);
    }

    static final int[] bits = new int[256];

    static {
        for (int i = 0; i < bits.length; i++) {
            for (int j = i; j != 0; j >>= 1) {
                bits[i] += j & 0x1;
            }
        }
    }

    int compareDescriptors(ByteBuffer d1, ByteBuffer d2, int best) {
        int totalCost = 0;
        assert d1.limit() - d1.position() == d2.limit() - d2.position();
        while (d1.position() < d1.limit()) {
            totalCost += bits[(d1.get() ^ d2.get()) & 0xFF];
            if (totalCost > best) {
                break;
            }
        }
        return totalCost;
    }

    int naiveNearestNeighbor(ByteBuffer vec, ByteBuffer modelDescriptors) {
        int neighbor = -1;
        int d, dist1 = Integer.MAX_VALUE, dist2 = Integer.MAX_VALUE;
        int size = vec.limit() - vec.position();

        for (int i = 0; i * size < modelDescriptors.capacity(); i++) {
            ByteBuffer mvec = (ByteBuffer) modelDescriptors.position(i * size).limit((i + 1) * size);
            d = compareDescriptors((ByteBuffer) vec.reset(), mvec, dist2);
            if (d < dist1) {
                dist2 = dist1;
                dist1 = d;
                neighbor = i;
            } else if (d < dist2) {
                dist2 = d;
            }
        }
        if (dist1 < settings.distanceThreshold * dist2) {
            return neighbor;
        }
        return -1;
    }

    void findPairs(Mat objectDescriptors, Mat imageDescriptors) {
        int size = imageDescriptors.cols();
        ByteBuffer objectBuf = objectDescriptors.createBuffer();
        ByteBuffer imageBuf = imageDescriptors.createBuffer();

        for (int i = 0; i * size < objectBuf.capacity(); i++) {
            ByteBuffer descriptor = (ByteBuffer) objectBuf.position(i * size).limit((i + 1) * size).mark();
            int nearestNeighbor = naiveNearestNeighbor(descriptor, imageBuf);
            if (nearestNeighbor >= 0) {
                ptpairs.add(i);
                ptpairs.add(nearestNeighbor);
            }
        }
    }

    void flannFindPairs(Mat objectDescriptors, Mat imageDescriptors) {
        int length = objectDescriptors.rows();

        // find nearest neighbors using FLANN
        flannIndex.build(imageDescriptors, indexParams, FLANN_DIST_HAMMING);
        flannIndex.knnSearch(objectDescriptors, indicesMat, distsMat, 2, searchParams);

        IntBuffer indicesBuf = indicesMat.createBuffer();
        IntBuffer distsBuf = distsMat.createBuffer();
        for (int i = 0; i < length; i++) {
            if (distsBuf.get(2 * i) < settings.distanceThreshold * distsBuf.get(2 * i + 1)) {
                ptpairs.add(i);
                ptpairs.add(indicesBuf.get(2 * i));
            }
        }
    }

    /**
     * a rough implementation for object location
     */
    double[] locatePlanarObject(KeyPointVector objectKeypoints, Mat objectDescriptors,
            KeyPointVector imageKeypoints, Mat imageDescriptors, double[] srcCorners, IplROI roi) {
        ptpairs.clear();
        if (settings.useFLANN) {
            flannFindPairs(objectDescriptors, imageDescriptors);
        } else {
            findPairs(objectDescriptors, imageDescriptors);
        }
        int n = ptpairs.size() / 2;
        logger.info(n + " matching pairs found");
        if (n < settings.matchesMin) {
            return null;
        }

        int xShift = 0;
        int yShift = 0;
        if (roi != null) {
            xShift = roi.xOffset();
            yShift = roi.yOffset();
        }

        pt1.resize(n);
        pt2.resize(n);
        mask.resize(n);
        FloatBuffer pt1Idx = pt1.createBuffer();
        FloatBuffer pt2Idx = pt2.createBuffer();
        for (int i = 0; i < n; i++) {
            Point2f p1 = objectKeypoints.get(ptpairs.get(2 * i)).pt();
            pt1Idx.put(2 * i, p1.x());
            pt1Idx.put(2 * i + 1, p1.y());
            Point2f p2 = imageKeypoints.get(ptpairs.get(2 * i + 1)).pt();
            pt2Idx.put(2 * i, p2.x() + xShift);
            pt2Idx.put(2 * i + 1, p2.y() + yShift);

//            System.out.println("KeyPoints found object: " + p1.x() + " " + p1.y());
//            System.out.println("KeyPoints found image: " + p2.x() + " " + p2.y());
        }

        H = findHomography(pt1, pt2, CV_RANSAC, settings.ransacReprojThreshold, mask, 2000, 0.995);
        if (H.empty() || countNonZero(mask) < settings.matchesMin) {
            return null;
        }

        double[] h = (double[]) H.createIndexer(false).array();
        double[] dstCorners = new double[srcCorners.length];
        for (int i = 0; i < srcCorners.length / 2; i++) {
            double x = srcCorners[2 * i], y = srcCorners[2 * i + 1];
            double Z = 1 / (h[6] * x + h[7] * y + h[8]);
            double X = (h[0] * x + h[1] * y + h[2]) * Z;
            double Y = (h[3] * x + h[4] * y + h[5]) * Z;
            dstCorners[2 * i] = X;
            dstCorners[2 * i + 1] = Y;
        }
        return dstCorners;
    }

    public static void main(String[] args) throws Exception {
//        Logger.getLogger("org.bytedeco.javacv").setLevel(Level.OFF);

//        String objectFilename = args.length == 2 ? args[0] : "/home/jiii/sketchbook/libraries/PapARt/data/markers/dlink.png";
        String objectFilename = args.length == 2 ? args[0] : "/home/jiii/repos/Papart-github/papart-examples/Camera/ExtractPlanarObjectForTracking/ExtractedView.bmp";
        String sceneFilename = args.length == 2 ? args[1] : "/home/jiii/my_photo-7.jpg";

        IplImage object = cvLoadImage(objectFilename, CV_LOAD_IMAGE_GRAYSCALE);
        IplImage image = cvLoadImage(sceneFilename, CV_LOAD_IMAGE_GRAYSCALE);
        if (object == null || image == null) {
            System.err.println("Can not load " + objectFilename + " and/or " + sceneFilename);
            System.exit(-1);
        }

        IplImage objectColor = IplImage.create(object.width(), object.height(), 8, 3);
        cvCvtColor(object, objectColor, CV_GRAY2BGR);

        IplImage correspond = IplImage.create(image.width(), object.height() + image.height(), 8, 1);
        cvSetImageROI(correspond, cvRect(0, 0, object.width(), object.height()));
        cvCopy(object, correspond);
        cvSetImageROI(correspond, cvRect(0, object.height(), correspond.width(), correspond.height()));
        cvCopy(image, correspond);
        cvResetImageROI(correspond);

        ObjectFinder.Settings settings = new ObjectFinder.Settings();
        settings.objectImage = object;
        settings.useFLANN = true;
        settings.ransacReprojThreshold = 5;
        ObjectFinder finder = new ObjectFinder(settings);

        long start = System.currentTimeMillis();
        double[] dst_corners = finder.find(image);
//        System.out.println("Finding time = " + (System.currentTimeMillis() - start) + " ms");

        if (dst_corners != null) {
            for (int i = 0; i < 4; i++) {
                int j = (i + 1) % 4;
                int x1 = (int) Math.round(dst_corners[2 * i]);
                int y1 = (int) Math.round(dst_corners[2 * i + 1]);
                int x2 = (int) Math.round(dst_corners[2 * j]);
                int y2 = (int) Math.round(dst_corners[2 * j + 1]);
                line(cvarrToMat(correspond), new Point(x1, y1 + object.height()),
                        new Point(x2, y2 + object.height()),
                        Scalar.WHITE, 1, 8, 0);
            }
        }

        for (int i = 0; i < finder.ptpairs.size(); i += 2) {
            Point2f pt1 = finder.objectKeypoints.get(finder.ptpairs.get(i)).pt();
            Point2f pt2 = finder.imageKeypoints.get(finder.ptpairs.get(i + 1)).pt();
            line(cvarrToMat(correspond), new Point(Math.round(pt1.x()), Math.round(pt1.y())),
                    new Point(Math.round(pt2.x()), Math.round(pt2.y() + object.height())),
                    Scalar.WHITE, 1, 8, 0);
        }

        CanvasFrame objectFrame = new CanvasFrame("Object");
        CanvasFrame correspondFrame = new CanvasFrame("Object Correspond");
        OpenCVFrameConverter converter = new OpenCVFrameConverter.ToIplImage();

        correspondFrame.showImage(converter.convert(correspond));
        for (int i = 0; i < finder.objectKeypoints.size(); i++) {
            KeyPoint r = finder.objectKeypoints.get(i);
            Point center = new Point(Math.round(r.pt().x()), Math.round(r.pt().y()));
            int radius = Math.round(r.size() / 2);
            circle(cvarrToMat(objectColor), center, radius, Scalar.RED, 1, 8, 0);
        }
        objectFrame.showImage(converter.convert(objectColor));

        objectFrame.waitKey();

        objectFrame.dispose();
        correspondFrame.dispose();
    }
}
