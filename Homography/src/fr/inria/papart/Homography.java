/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart;

/**
 *
 * @author jeremy
 */
import processing.core.PApplet;
import processing.core.PMatrix2D;
import processing.core.PVector;
import toxi.geom.Matrix4x4;
import java.io.FileNotFoundException;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;

// TODO: create another class homography loader, without any OpenCV dependency

public class Homography {

    CvMat srcPoints;
    CvMat dstPoints;
    int nbPoints;
    CvMat homography;
    int currentPoint = 0;
    Matrix4x4 transform = null;
    boolean isValid;
    int srcDim;
    int dstDim;
    String filename;
    PApplet pa;

    public Homography(String filename) throws FileNotFoundException {
        load(filename);
    }

    public Homography(PApplet parent) {
        srcDim = 3;
        dstDim = 2;
        nbPoints = 6;
        init(parent);
    }

    public Homography(PApplet parent, int srcDim, int dstDim, int nbPoints) {
        this.srcDim = srcDim;
        this.dstDim = dstDim;
        this.nbPoints = nbPoints;
        init(parent);
    }

    private void init(PApplet parent) {
        pa = parent;
        pa.println("Initilizing the homography");
        currentPoint = 0;
        isValid = false;
        srcPoints = cvCreateMat(srcDim, nbPoints, CV_32FC1);
        dstPoints = cvCreateMat(dstDim, nbPoints, CV_32FC1);
        homography = cvCreateMat(3, 3, CV_32FC1);
    }

    public void loadKinectMultitouch() {
        setPoint(false, 0, new PVector(0, 0));
        setPoint(false, 1, new PVector(0, 1));
        setPoint(false, 2, new PVector(0.5f, 0));
        setPoint(false, 3, new PVector(0.5f, 1));
        setPoint(false, 4, new PVector(1, 0));
        setPoint(false, 5, new PVector(1, 1));
        // setPoint(true, 0, new PVector(0.12630571, 0.09737079, -0.66325593));
        // setPoint(true, 1, new PVector(0.09521521, 0.023055185, -0.9139001));
        // setPoint(true, 2, new PVector(-0.06756949, 0.103283286, -0.6680066));
        // setPoint(true, 3, new PVector(-0.08622512, 0.025369404, -0.9214087));
        // setPoint(true, 4, new PVector(-0.23903821, 0.103002936, -0.65605295));
        // setPoint(true, 5, new PVector(-0.2700226, 0.031900354, -0.8922543));
        // compute();
    }

    // TODO : exception errors
    public boolean add3DPoint(PVector dst) {
        setPoint(true, currentPoint++, dst);
        if (currentPoint == nbPoints) {
            compute();
            currentPoint = 0;
            return true;
        }
        return false;
    }

    // TODO : exception errors
    public boolean add3DPoint(PVector src, PVector dst) {

        // if(debug){
        //   println("Adding : " + src + " and " + dst) ;
        //   fill(#FF3150);
        //   drawEllipse(src);
        //   fill(#3731FF);
        //   drawEllipse(dst);
        // }

        setPoint(true, currentPoint, src);
        setPoint(false, currentPoint++, dst);

        if (currentPoint == nbPoints) {
            compute();
            currentPoint = 0;
            return true;
        }
        return false;
    }

    // todo throws ...
    public void setPoint(boolean isSrc, int id, PVector point) {

        if (isSrc) {
            srcPoints.put(id, point.x);
            srcPoints.put(id + nbPoints, point.y);
            if (srcDim == 3) {
                srcPoints.put(id + nbPoints * 2, point.z);

            }
        } else {
            dstPoints.put(id, point.x);
            dstPoints.put(id + nbPoints, point.y);
            if (dstDim == 3) {
                dstPoints.put(id + nbPoints * 2, point.y);

            }
        }
    }
    PMatrix2D transform2D;

    public boolean compute() {

        // println(srcPoints);
        // println(dstPoints);

        cvFindHomography(srcPoints, dstPoints, homography, 4, 2, null);
        //    cvFindHomography(srcPoints, dstPoints, homography);

        //    cvFindHomography(srcPoints, dstPoints, homography, int method, int reprojThresholderror);
        // naive : 0
        // CV_LMEDS = 4,
        // CV_RANSAC = 8,

        // TODO: if ( hom == ...)

        if (srcDim == dstDim && srcDim == 2) {
            transform = new Matrix4x4(homography.get(0), homography.get(1), 0, homography.get(2),
                    homography.get(3), homography.get(4), 0, homography.get(5),
                    0, 0, 1, 0,
                    0, 0, 0, 1);

        } else {
            transform = new Matrix4x4(homography.get(0), homography.get(1), homography.get(2), 0,
                    homography.get(3), homography.get(4), homography.get(5), 0,
                    homography.get(6), homography.get(7), homography.get(8), 0,
                    0, 0, 0, 1);

        }
        transform2D = new PMatrix2D((float) homography.get(0), (float) homography.get(1), (float) homography.get(2),
                (float) homography.get(3), (float) homography.get(4), (float) homography.get(5));

        // if(debug){
        //   println("Calibration done");
        // }

        isValid = true;
        return true;
    }

    // Matrix4x4 getPerspectiveTransfo(PVector srcV, PVector dstV){
    //   // CvPoint2D32f src1 = new CvPoint2D32f(src.x, src.y);
    //   // CvPoint2D32f dst1 = new CvPoint2D32f(dst.x, dst.y);
    //   float[] src = new float[2];
    //   float[] dst = new float[2];
    //   src[0] = srcV.x; src[1] = srcV.y;
    //   dst[0] = dstV.x; dst[1] = dstV.y;
    //   CvMat map = cvCreateMat(3, 3, CV_32FC1);
    //   map = getPerspectiveTransform(src, dst, map);
    //   return new Matrix4x4(homography.get(0), homography.get(1), homography.get(2), 0,
    // 			 homography.get(3), homography.get(4), homography.get(5), 0,
    // 			 homography.get(6), homography.get(7), homography.get(8), 0,
    // 			 0, 0, 0, 1);
    // }
    public PMatrix2D getTransform2D() {
        return transform2D;
    }

    public Matrix4x4 getTransformation() {
        return transform;
    }

    CvMat getHomography() {
        return homography;
    }

    // TODO:  throws ...
    public void save(String filename) {
        String[] lines = new String[16];
        double[] transformArray = new double[16];
        transform.toArray(transformArray);
        for (int i = 0; i < 16; i++) {
            lines[i] = "" + transformArray[i];

        }
        pa.saveStrings(filename, lines);
        pa.println("Homography successfully saved");

    }

    private void load(String filename) throws FileNotFoundException {
        String[] lines = pa.loadStrings(filename);
        if (lines == null) {
            throw new FileNotFoundException(filename);
        }
        transform = new Matrix4x4(Float.parseFloat(lines[0]), Float.parseFloat(lines[1]), Float.parseFloat(lines[2]), Float.parseFloat(lines[3]),
                Float.parseFloat(lines[4]), Float.parseFloat(lines[5]), Float.parseFloat(lines[6]), Float.parseFloat(lines[7]),
                Float.parseFloat(lines[8]), Float.parseFloat(lines[9]), Float.parseFloat(lines[10]), Float.parseFloat(lines[11]),
                Float.parseFloat(lines[12]), Float.parseFloat(lines[13]), Float.parseFloat(lines[14]), Float.parseFloat(lines[15]));

        transform2D = new PMatrix2D(Float.parseFloat(lines[0]), Float.parseFloat(lines[1]), Float.parseFloat(lines[3]),
                Float.parseFloat(lines[4]), Float.parseFloat(lines[5]), Float.parseFloat(lines[7]));


        System.out.println("Homography successfully loaded");
        isValid = true;
    }
}
