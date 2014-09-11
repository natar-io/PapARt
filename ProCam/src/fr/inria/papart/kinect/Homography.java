/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

/**
 *
 * @author jeremy
 */
import processing.core.PApplet;
import processing.core.PVector;
import toxi.geom.Matrix4x4;
import java.io.FileNotFoundException;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_calib3d.*;
import processing.core.PMatrix3D;
import toxi.geom.Vec3D;

public class Homography {

    CvMat srcPoints;
    CvMat dstPoints;
    int nbPoints;
    CvMat homography;
    int currentPoint = 0;
    
    // TEMPORARY PUBLIC
    Matrix4x4 transform = null;
    boolean isValid;
    int srcDim;
    int dstDim;
    String filename;
    PApplet pa;

    public Homography(String filename) throws FileNotFoundException {
        load(Kinect.papplet, filename);
    }

    public Homography(PApplet parent, String filename) throws FileNotFoundException {
        load(parent, filename);
    }

    public Homography(Matrix4x4 transfo) throws FileNotFoundException {
        this.transform = transfo;
        isValid = true;
    }

    public Homography(PApplet parent) {
        this(parent, 2, 3, 6);
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

    // TODO : exception errors
    public boolean add3DPoint(PVector dst) {
        setPoint(true, currentPoint++, dst);
        if (currentPoint == nbPoints) {
            findHomography();
            currentPoint = 0;
            return true;
        }
        return false;
    }

    // TODO : exception errors
    public boolean add3DPoint(PVector src, PVector dst) {

        setPoint(true, currentPoint, src);
        setPoint(false, currentPoint++, dst);

        if (currentPoint == nbPoints) {
            findHomography();
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
                dstPoints.put(id + nbPoints * 2, point.z);

            }
        }
    }

    public void findHomography() {

//        cvFindHomography(srcPoints, dstPoints, homography, 4, 2, null);
//        cvFindHomography(srcPoints, dstPoints, homography, 0, 3, null);
//            cvFindHomography(srcPoints, dstPoints, homography);

        // If 4 points && 2D
        if (srcDim == 2 && dstDim == 2 && nbPoints == 4) {

//            System.out.println("Perspective transform...");
//
//            double[] d1 = new double[]{
//                srcPoints.get(0), srcPoints.get(4 + 0),
//                srcPoints.get(1), srcPoints.get(4 + 1),
//                srcPoints.get(2), srcPoints.get(4 + 2),
//                srcPoints.get(3), srcPoints.get(4 + 3)};
//            double[] d2 = new double[]{
//                dstPoints.get(0), dstPoints.get(4 + 0),
//                dstPoints.get(1), dstPoints.get(4 + 1),
//                dstPoints.get(2), dstPoints.get(4 + 2),
//                dstPoints.get(3), dstPoints.get(4 + 3)};
//            float[] f1 = new float[]{
//                (float) srcPoints.get(0), (float) srcPoints.get(4 + 0),
//                (float) srcPoints.get(1), (float) srcPoints.get(4 + 1),
//                (float) srcPoints.get(2), (float) srcPoints.get(4 + 2),
//                (float) srcPoints.get(3), (float) srcPoints.get(4 + 3)};
//            float[] f2 = new float[]{
//                (float) dstPoints.get(0), (float) dstPoints.get(4 + 0),
//                (float) dstPoints.get(1), (float) dstPoints.get(4 + 1),
//                (float) dstPoints.get(2), (float) dstPoints.get(4 + 2),
//                (float) dstPoints.get(3), (float) dstPoints.get(4 + 3)};
//
//            JavaCV.getPerspectiveTransform(d1, d2, homography);
//            System.out.println("javacv persp");
//            transform2D = new PMatrix2D((float) homography.get(0), (float) homography.get(1), (float) homography.get(2),
//                    (float) homography.get(3), (float) homography.get(4), (float) homography.get(5));
//            transform2D.print();
//
//
//            cvGetPerspectiveTransform(f1, f2, homography);
//            System.out.println("opencv persp");
//            transform2D = new PMatrix2D((float) homography.get(0), (float) homography.get(1), (float) homography.get(2),
//                    (float) homography.get(3), (float) homography.get(4), (float) homography.get(5));
//            transform2D.print();
//

            cvFindHomography(srcPoints, dstPoints, homography);
//            System.out.println("findhomography");
//            transform2D = new PMatrix2D((float) homography.get(0), (float) homography.get(1), (float) homography.get(2),
//                    (float) homography.get(3), (float) homography.get(4), (float) homography.get(5));
//            transform2D.print();

        } else {
//            cvFindHomography(srcPoints, dstPoints, homography, 4, 2, null);
            cvFindHomography(srcPoints, dstPoints, homography);
            //    cvFindHomography(srcPoints, dstPoints, homography, int method, int reprojThresholderror);
        }

        initMatrices();

        isValid = true;
    }

    private void initMatrices() {

        if (srcDim == dstDim && srcDim == 2) {

            transform = new Matrix4x4(homography.get(0), homography.get(1), 0, homography.get(2),
                    homography.get(3), homography.get(4), 0, homography.get(5),
                    0, 0, 1, 0,
                    0, 0, 0, 1);
            
            transform.scale(1, 1, 1);

//            transform2D = new PMatrix2D((float) homography.get(0), (float) homography.get(1), (float) homography.get(2),
//                    (float) homography.get(2), (float) homography.get(4), (float) homography.get(5));

        } else {
            transform = new Matrix4x4(homography.get(0), homography.get(1), homography.get(2), 0,
                    homography.get(3), homography.get(4), homography.get(5), 0,
                    homography.get(6), homography.get(7), homography.get(8), 0,
                    0, 0, 0, 1);

//            transform3D = getTransformationP();

        }


    }

    public Matrix4x4 getTransformation() {
        return transform;
    }

    public PMatrix3D getTransformationP() {
        return new PMatrix3D(
                (float) transform.matrix[0][0],
                (float) transform.matrix[0][1],
                (float) transform.matrix[0][2],
                (float) transform.matrix[0][3],
                (float) transform.matrix[1][0],
                (float) transform.matrix[1][1],
                (float) transform.matrix[1][2],
                (float) transform.matrix[1][3],
                (float) transform.matrix[2][0],
                (float) transform.matrix[2][1],
                (float) transform.matrix[2][2],
                (float) transform.matrix[2][3],
                (float) transform.matrix[3][0],
                (float) transform.matrix[3][1],
                (float) transform.matrix[3][2],
                (float) transform.matrix[3][3]);
    }

    public Vec3D applyTo(Vec3D src) {
        return this.transform.applyTo(src);
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

    private void load(PApplet parent, String filename) throws FileNotFoundException {
        String[] lines = parent.loadStrings(filename);
        if (lines == null) {
            throw new FileNotFoundException(filename);
        }
        transform = new Matrix4x4(Float.parseFloat(lines[0]), Float.parseFloat(lines[1]), Float.parseFloat(lines[2]), Float.parseFloat(lines[3]),
                Float.parseFloat(lines[4]), Float.parseFloat(lines[5]), Float.parseFloat(lines[6]), Float.parseFloat(lines[7]),
                Float.parseFloat(lines[8]), Float.parseFloat(lines[9]), Float.parseFloat(lines[10]), Float.parseFloat(lines[11]),
                Float.parseFloat(lines[12]), Float.parseFloat(lines[13]), Float.parseFloat(lines[14]), Float.parseFloat(lines[15]));

        System.out.println("Homography successfully loaded");
        isValid = true;
    }
}
