/*
 */
package fr.inria.papart.tracking;

import java.util.Arrays;
import org.bytedeco.javacpp.ARToolKitPlus;

import static org.bytedeco.javacpp.ARToolKitPlus.*;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.cvFindCornerSubPix;
import static org.bytedeco.javacpp.opencv_imgproc.cvMinAreaRect2;
import processing.core.PGraphics;
import processing.core.PVector;

public class DetectedMarker implements Cloneable {

    public int id;
    public double[] corners;
    public double confidence;

    public DetectedMarker(int id, double[] corners, double confidence) {
        this.id = id;
        this.corners = corners;
        this.confidence = confidence;
    }

    public void drawSelf(PGraphics g, int size) {
        for (int i = 0; i < 8; i += 2) {
            g.ellipse((float) corners[i], (float) corners[i + 1], size, size);
        }
    }

    public PVector[] getCorners() {
        PVector[] out = new PVector[4];
        out[0] = new PVector((float) corners[0], (float) corners[1]);
        out[1] = new PVector((float) corners[2], (float) corners[3]);
        out[2] = new PVector((float) corners[4], (float) corners[5]);
        out[3] = new PVector((float) corners[6], (float) corners[7]);
        return out;
    }

    public DetectedMarker(int id, double... corners) {
        this(id, corners, 1.0);
    }

    @Override
    public DetectedMarker clone() {
        return new DetectedMarker(id, corners.clone(), confidence);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DetectedMarker) {
            DetectedMarker m = (DetectedMarker) o;
            return m.id == id && Arrays.equals(m.corners, corners);
        }
        return false;
    }

    public double[] getCenter() {
        double x = 0, y = 0;
        if (true) {
// the centroid is not what we want as it does not remain at
// the same physical point under projective transformations..
// But it has the advantage of averaging noise better, and does
// give better results
            for (int i = 0; i < 4; i++) {
                x += corners[2 * i];
                y += corners[2 * i + 1];
            }
            x /= 4;
            y /= 4;
        } else {
            double x1 = corners[0];
            double y1 = corners[1];
            double x2 = corners[4];
            double y2 = corners[5];
            double x3 = corners[2];
            double y3 = corners[3];
            double x4 = corners[6];
            double y4 = corners[7];

            double u = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3))
                    / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
            x = x1 + u * (x2 - x1);
            y = y1 + u * (y2 - y1);
        }
        return new double[]{x, y};
    }

    public IplImage getImage() {
        return getImage(id);
    }

    private static IplImage imageCache[] = new IplImage[4096];

    public static IplImage getImage(int id) {
        if (imageCache[id] == null) {
            imageCache[id] = IplImage.create(8, 8, IPL_DEPTH_8U, 1);
            createImagePatternBCH(id, imageCache[id].getByteBuffer());
        }
        return imageCache[id];
    }

    public static DetectedMarker[] detect(ARToolKitPlus.TrackerMultiMarker tracker, opencv_core.IplImage image) {

        int cameraWidth = image.width();
        int cameraHeight = image.height();
        // TODO: check imgWith and init width.

        opencv_core.CvPoint2D32f corners = new opencv_core.CvPoint2D32f(4);
        opencv_core.CvMemStorage memory = opencv_core.CvMemStorage.create();
        opencv_core.CvSize subPixelSize = null, subPixelZeroZone = null;
        opencv_core.CvTermCriteria subPixelTermCriteria = null;
        int subPixelWindow = 11;

        subPixelSize = cvSize(subPixelWindow / 2, subPixelWindow / 2);
        subPixelZeroZone = cvSize(-1, -1);
        subPixelTermCriteria = cvTermCriteria(CV_TERMCRIT_EPS, 100, 0.001);

        int n = 0;
        IntPointer markerNum = new IntPointer(1);
        ARToolKitPlus.ARMarkerInfo markers = new ARToolKitPlus.ARMarkerInfo(null);
        tracker.arDetectMarkerLite(image.imageData(), tracker.getThreshold() /* 100 */, markers, markerNum);

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
            if (vertex[0] - w < 0 || vertex[0] + w >= cameraWidth || vertex[1] - w < 0 || vertex[1] + w >= cameraHeight
                    || vertex[2] - w < 0 || vertex[2] + w >= cameraWidth || vertex[3] - w < 0 || vertex[3] + w >= cameraHeight
                    || vertex[4] - w < 0 || vertex[4] + w >= cameraWidth || vertex[5] - w < 0 || vertex[5] + w >= cameraHeight
                    || vertex[6] - w < 0 || vertex[6] + w >= cameraWidth || vertex[7] - w < 0 || vertex[7] + w >= cameraHeight) {
                // too tight for cvFindCornerSubPix...

                continue;
            }

            opencv_core.CvMat points = opencv_core.CvMat.create(1, 4, CV_32F, 2);
            points.getFloatBuffer().put(vertex);
            opencv_core.CvBox2D box = cvMinAreaRect2(points, memory);

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

}
