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

import java.util.Arrays;
import org.bytedeco.javacpp.IntPointer;

import static org.bytedeco.javacpp.ARToolKitPlus.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacv.JavaCV;

/**
 *
 * @author Samuel Audet
 */
public class MarkerDetector {

    public MarkerDetector() {

        setSettings();
    }

    private int thresholdWindowMin = 5;
    private int thresholdWindowMax = 63;
    private double thresholdVarMultiplier = 1.0;
    private double thresholdKBlackMarkers = 0.6;
    private double thresholdKWhiteMarkers = 1.0;
    private int subPixelWindow = 11;

    public void setSettings() {
        this.subPixelSize = cvSize(subPixelWindow / 2, subPixelWindow / 2);
        this.subPixelZeroZone = cvSize(-1, -1);
        this.subPixelTermCriteria = cvTermCriteria(CV_TERMCRIT_EPS, 100, 0.001);
    }

    private MultiTracker tracker = null;
    private IntPointer markerNum = new IntPointer(1);
    private int width = 0, height = 0, depth = 0, channels = 0;
    private IplImage tempImage, tempImage2, sumImage, sqSumImage, thresholdedImage;
    private CvMat points = CvMat.create(1, 4, CV_32F, 2);
    private CvPoint2D32f corners = new CvPoint2D32f(4);
    private CvMemStorage memory = CvMemStorage.create();
    private CvSize subPixelSize = null, subPixelZeroZone = null;
    private CvTermCriteria subPixelTermCriteria = null;

    private CvFont font = cvFont(1, 1);
    private CvSize textSize = new CvSize();

    public IplImage getThresholdedImage() {
        return thresholdedImage;
    }

    private void init(IplImage image) {
        if (tracker != null && image.width() == width && image.height() == height
                && image.depth() == depth && image.nChannels() == channels) {
            return;
        }

        width = image.width();
        height = image.height();
        depth = image.depth();
        channels = image.nChannels();

//        System.out.println("Fist init..." + width + " " + height + " " + depth + " " + channels);
        // TODO: make functions instead of elements in if()
        if (depth != IPL_DEPTH_8U || channels > 1) {
            tempImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
        }
        if (depth != IPL_DEPTH_8U && channels > 1) {
            tempImage2 = IplImage.create(width, height, IPL_DEPTH_8U, 3);
        }
        sumImage = IplImage.create(width + 1, height + 1, IPL_DEPTH_64F, 1);
        sqSumImage = IplImage.create(width + 1, height + 1, IPL_DEPTH_64F, 1);
        thresholdedImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);

        tracker = new MultiTracker(thresholdedImage.widthStep(), thresholdedImage.height());
//        System.out.println("WidthStep " + thresholdedImage.widthStep() + " " + thresholdedImage.height() );
        
        int pixfmt = PIXEL_FORMAT_LUM;
        tracker.setPixelFormat(pixfmt);
        tracker.setBorderWidth(0.125f);
//        tracker.setThreshold(128);
        tracker.activateAutoThreshold(true);
//        tracker.setNumAutoThresholdRetries(10);
        tracker.setUndistortionMode(UNDIST_NONE);
//        tracker.setPoseEstimator(POSE_ESTIMATOR_RPP);
        tracker.setMarkerMode(MARKER_ID_BCH);
        tracker.setImageProcessingMode(IMAGE_HALF_RES);
//        System.out.println("Tracker ready");

    }

    public DetectedMarker[] detect(IplImage image) {
        init(image);

        // TODO: make functions instead of elements in if()
        if (depth != IPL_DEPTH_8U && channels > 1) {
            cvConvertScale(image, tempImage2, 255 / image.highValue(), 0);
            cvCvtColor(tempImage2, tempImage, channels > 3 ? CV_RGBA2GRAY : CV_BGR2GRAY);
            image = tempImage;
        } else if (depth != IPL_DEPTH_8U) {
            cvConvertScale(image, tempImage, 255 / image.highValue(), 0);
            image = tempImage;
        } else if (channels > 1) {
            cvCvtColor(image, tempImage, channels > 3 ? CV_RGBA2GRAY : CV_BGR2GRAY);
            image = tempImage;
//            System.out.println("Convert to gray.");
        }

        int n = 0;
        ARMarkerInfo markers = new ARMarkerInfo(null);
//        tracker.arDetectMarkerLite(image.imageData(), 128 /*tracker.getThreshold()*/, markers, markerNum);
        tracker.arDetectMarkerLite(image.imageData(), tracker.getThreshold(), markers, markerNum);
        DetectedMarker[] markers2 = new DetectedMarker[markerNum.get(0)];

        for (int i = 0; i < markers2.length && !markers.isNull(); i++) {
            markers.position(i);
            int id = markers.id();
            if (id < 0) {
                // no detected ID...
                continue;
            }
            int dir = markers.dir();
            float confidence = markers.cf();
            float[] vertex = new float[8];
            markers.vertex().get(vertex);

            int w = subPixelWindow / 2 + 1;
            if (vertex[0] - w < 0 || vertex[0] + w >= width || vertex[1] - w < 0 || vertex[1] + w >= height
                    || vertex[2] - w < 0 || vertex[2] + w >= width || vertex[3] - w < 0 || vertex[3] + w >= height
                    || vertex[4] - w < 0 || vertex[4] + w >= width || vertex[5] - w < 0 || vertex[5] + w >= height
                    || vertex[6] - w < 0 || vertex[6] + w >= width || vertex[7] - w < 0 || vertex[7] + w >= height) {
                // too tight for cvFindCornerSubPix...
                continue;
            }

            points.getFloatBuffer().put(vertex);
            CvBox2D box = cvMinAreaRect2(points, memory);
            float bw = box.size().width();
            float bh = box.size().height();
            cvClearMemStorage(memory);
            if (bw <= 0 || bh <= 0 || bw / bh < 0.1 || bw / bh > 10) {
                // marker is too "flat" to have been IDed correctly...
                continue;
            }

            for (int j = 0; j < 4; j++) {
                corners.position(j).put(vertex[2 * j], vertex[2 * j + 1]);
            }

            cvFindCornerSubPix(image, corners.position(0), 4, subPixelSize, subPixelZeroZone, subPixelTermCriteria);
            double[] d = {corners.position((4 - dir) % 4).x(), corners.position((4 - dir) % 4).y(),
                corners.position((5 - dir) % 4).x(), corners.position((5 - dir) % 4).y(),
                corners.position((6 - dir) % 4).x(), corners.position((6 - dir) % 4).y(),
                corners.position((7 - dir) % 4).x(), corners.position((7 - dir) % 4).y()};

            markers2[n++] = new DetectedMarker(id, d, confidence);
        }
        return Arrays.copyOf(markers2, n);
    }

    public void draw(IplImage image, DetectedMarker[] markers) {
        for (DetectedMarker m : markers) {
            int cx = 0, cy = 0;
            int[] pts = new int[8];
            for (int i = 0; i < 4; i++) {
                int x = (int) Math.round(m.corners[i * 2] * (1 << 16));
                int y = (int) Math.round(m.corners[i * 2 + 1] * (1 << 16));
                pts[2 * i] = x;
                pts[2 * i + 1] = y;
                cx += x;
                cy += y;

// draw little colored squares in corners to confirm that the corners
// are returned in the right order...
//                CvPoint pt2a = cvPoint(pts[i].x+200000, pts[i].y+200000);
//                cvRectangle(image, pts, pt2a,
//                        i == 0? CV_RGB(maxIntensity, 0, 0) :
//                            i == 1? CV_RGB(0, maxIntensity, 0) :
//                                i == 2? CV_RGB(0, 0, maxIntensity) :
//                                    CV_RGB(maxIntensity, maxIntensity, maxIntensity),
//                        CV_FILLED, CV_AA, 16);
            }
            cx /= 4;
            cy /= 4;

            cvPolyLine(image, pts, new int[]{pts.length / 2}, 1, 1, CV_RGB(0, 0, image.highValue()), 1, CV_AA, 16);

            String text = Integer.toString(m.id);
            int[] baseline = new int[1];
            cvGetTextSize(text, font, textSize, baseline);

            int[] pt1 = {cx - (textSize.width() * 3 / 2 << 16) / 2,
                cy + (textSize.height() * 3 / 2 << 16) / 2};
            int[] pt2 = {cx + (textSize.width() * 3 / 2 << 16) / 2,
                cy - (textSize.height() * 3 / 2 << 16) / 2};
            cvRectangle(image, pt1, pt2, CV_RGB(0, image.highValue(), 0), CV_FILLED, CV_AA, 16);

            int[] pt = {(int) Math.round((double) cx / (1 << 16) - textSize.width() / 2),
                (int) Math.round((double) cy / (1 << 16) + textSize.height() / 2) + 1};
            cvPutText(image, text, pt, font, CvScalar.BLACK);
        }
    }
}
