/*
 */
package fr.inria.papart.tracking;

import java.util.Arrays;

import static org.bytedeco.javacpp.ARToolKitPlus.*;
import static org.bytedeco.javacpp.opencv_core.*;
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
    
    public PVector[] getCorners(){
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

}
