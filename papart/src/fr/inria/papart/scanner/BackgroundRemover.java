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
package fr.inria.papart.scanner;

/**
 *
 * @author Jeremy Laviole
 */
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import java.nio.ByteBuffer;
import processing.core.PConstants;
import processing.core.PVector;

public class BackgroundRemover implements PConstants {

    public enum BackgroundState {

        NO_BACKGROUND, FULL_IMAGE, PROJ_ZONE
    }

    // TODO: Use the orientation estimation of the detected points. 
    private IplImage yuvImage, rawImage, foreimage, projZone, background, output, diff, hsvImage;
    private BackgroundState state;

    public BackgroundRemover(int w, int h) {
        CvSize size = cvSize(w, h);

        projZone = IplImage.create(size, IPL_DEPTH_8U, 1);
        output = IplImage.create(size, IPL_DEPTH_8U, 1);
        diff = IplImage.create(size, IPL_DEPTH_8U, 3);

        this.state = BackgroundState.NO_BACKGROUND;
    }

    public void close() {
        projZone.release();
        output.release();
        diff.release();
        releaseBackground();
        releaseProjZone();
    }

    private void releaseBackground() {
        if (background != null) {
            background.release();
        }
    }
    private void releaseProjZone() {
        if (background != null) {
            background.release();
        }
    }

    /**
     * Please wait for a few frames ~5 or 10 before calling
     *
     * @param rawImage background image to store.
     */
    public void setBackground(IplImage rawImage) {
        this.background = rawImage.clone();
        this.state = BackgroundState.FULL_IMAGE;
        
    }

    public IplImage getProjZone() {
        return this.projZone;
    }

    public IplImage getBackground() {
        return this.background;
    }

    /**
     * Activate the projection to project a bright image. Put this image in this
     * function to set the zone to check.
     *
     * @param rawImage
     */
    public void setProjZone(IplImage rawImage) {
        // Remove the background.
        IplImage out = this.applyTo(rawImage, 1);
        projZone = out.clone();
        this.state = BackgroundState.PROJ_ZONE;
    }

    public void reset() {
        this.state = BackgroundState.NO_BACKGROUND;
    }

    public boolean isBackgroundSet() {
        return this.state != BackgroundState.NO_BACKGROUND;
    }

    public boolean isProjSet() {
        return this.state == BackgroundState.PROJ_ZONE;
    }

    public PVector findSinglePixel(IplImage rawImage) {
        return findPos(applyTo(rawImage));
    }

    /**
     * Not used ?
     *
     * @param rawImage
     * @return
     */
//    public IplImage threshold(IplImage rawImage, CvScalar min, CvScalar max) {
//        cvCvtColor(rawImage, hsvImage, CV_BGR2HSV);
//        cvInRangeS(hsvImage, min, max, output);
//        return output;
//    }
    public IplImage applyTo(IplImage rawImage) {
        return this.applyTo(rawImage, 0);
    }

    /**
     * Remove the background, and search within the proj zone. With noise
     * reduction.
     *
     * @param rawImage
     * @param filterPower 0 no filtering, 1 or more 1 pixel or more closing.
     * @return
     */
    public IplImage applyTo(IplImage rawImage, int filterPower) {
        assert (this.state != BackgroundState.NO_BACKGROUND);

        // cvCvtColor(rawImage, hsvImage, CV_BGR2HSV); 
        // cvSplit(hsvImage, null, pict, null, null);
        //	cvSplit(hsvImage, picts, null, null, null);
        // Difference with the background
        cvAbsDiff(rawImage, background, diff);

        // Difference to GRAY
        cvCvtColor(diff, output, CV_BGR2GRAY);

        //	cvAbsDiff(pict , picts,output); 
        // Threshold on the difference. 
        cvThreshold(output, output, 40, 255, CV_THRESH_BINARY);

        if (filterPower > 0) {
            // Erode the result
            cvErode(output, output, null, filterPower);

            // cvSmooth(output,output,2,3,3,0,0); 
            cvDilate(output, output, null, filterPower);
        }

        //Consider the result only in the projection zone
        if (this.state == BackgroundState.PROJ_ZONE) {
            cvAnd(projZone, output, output, null);
        }

        return output;
    }

    /**
     * Find a non-zero pixel, and return it.
     *
     * @param img
     * @return
     */
    public PVector findPos(IplImage img) {
        ByteBuffer buff = img.getByteBuffer();

        for (int i = 0; i < img.width() * img.height(); i++) {
            if (buff.get(i) != 0) {
                return new PVector(i % img.width(), i / img.width());
            }
        }

        return null;
    }

}
//    // To remove
//    public static float minContourSize = 1;
//    int ellipseSize = 50;
//
//    /**
//     * ****
//     * Not used anymore
//     *
//     * @param binaryImage
//     * @return
//     */
//    public static PVector findContours(IplImage binaryImage) {
//
//        CvMemStorage storage = CvMemStorage.create();
//
//        CvSeq contour = new CvContour(null);
//        cvFindContours(binaryImage, storage, contour, Loader.sizeof(CvContour.class),
//                CV_RETR_TREE, CV_CHAIN_APPROX_NONE);
//        //		   CV_RETR_EXTERNAL, CV_CHAIN_APPROX_NONE);
//
//        double largestContourEdgeArea = 0;
//        CvSeq largestContour = null;
//        CvPoint contourPoints = null;
//        IntBuffer contourPointsBuffer = null;
//
//        while (contour != null && !contour.isNull()) {
//
//            int contourPointsSize = contour.total();
//
//            if (contourPointsSize < minContourSize) {
//                contour = contour.h_next();
//                continue;
//            }
//
//            if (contourPoints == null || contourPoints.capacity() < contourPointsSize) {
//                contourPoints = new CvPoint(contourPointsSize);
//                contourPointsBuffer = contourPoints.asByteBuffer().asIntBuffer();
//            }
//
//            cvCvtSeqToArray(contour, contourPoints.position(0));
//
//            PVector contourPos = new PVector(0, 0);
//
//            for (int i = 0; i < contourPointsSize; i++) {
//                int x = contourPointsBuffer.get(2 * i),
//                        y = contourPointsBuffer.get(2 * i + 1);
//
//             //   fill(200, 0, 0);
//                //   ellipse(frameSizeX + x, y, 3, 3);
//                contourPos.add(x, y, 0);
//            }
//
//            contourPos.mult(1f / (float) contourPointsSize);
//
//            contour = contour.h_next();
//
//            return contourPos;
//        }
//        return null;
//    }
