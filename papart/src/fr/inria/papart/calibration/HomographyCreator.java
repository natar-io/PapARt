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
package fr.inria.papart.calibration;

import fr.inria.papart.calibration.files.HomographyCalibration;
import org.bytedeco.opencv.opencv_core.*;
// import static org.bytedeco.opencv.global.opencv_core.CV_32FC1;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_core.cvCreateMat;
import static org.bytedeco.opencv.global.opencv_core.cvMat;

import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.indexer.UByteArrayIndexer;
import org.bytedeco.javacpp.indexer.UByteRawIndexer;

import static org.bytedeco.opencv.global.opencv_calib3d.findHomography;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Matrix4x4;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class HomographyCreator {

    private Mat cvMat;
    private Matrix4x4 mat;
    private PMatrix3D pmatrix;

    //private Mat srcPoints;
    //private Mat dstPoints;

    private FloatPointer srcPoints, dstPoints; 

    private final int srcDim;
    private final int dstDim;
    private final int nbPoints;

    private HomographyCalibration homographyCalibrationOutput;

    private int currentPoint = 0;

    public HomographyCreator(int srcDim, int dstDim, int nbPoints) {
        this.srcDim = srcDim;
        this.dstDim = dstDim;
        this.nbPoints = nbPoints;
        init();
    }

    private void init() {
        currentPoint = 0;
        new Point2f(nbPoints);
        
        srcPoints = createPointer(srcDim, nbPoints);
        dstPoints = createPointer(dstDim, nbPoints);
        // cvMat = new Mat(3, 3, CV_32FC1);
        cvMat = new Mat(3, 3, CV_32FC1);
        homographyCalibrationOutput = new HomographyCalibration();
    }

    private FloatPointer createPointer(int dim, int size){
      if(dim == 2){
        return new Point2f(size);
      }
      if(dim == 3){
        return new Point3f(size);
      }
      return new Point2f(size);
    }
    
    public boolean addPoint(PVector src, PVector dst) {
        System.out.println("dim: " + srcDim + " " + dstDim + " " + nbPoints);
        System.out.println("addpoint: " + currentPoint + " " + src + " " + dst);
        addPointCvMat(srcPoints, src, srcDim);
        addPointCvMat(dstPoints, dst, dstDim);
        currentPoint++;
        return checkAndComputeHomography();
    }

    private void addPointCvMat(FloatPointer points, PVector point, int dim) {

        // FloatIndexer idx = points.createIndexer();

        // System.out.println("add: " + (currentPoint + nbPoints) );
    
        if(dim == 2){
          points.put(point.x);
          points.put(point.y);
        }
        if(dim == 3){
          points.put(point.z);
        }

        //points.put(0, currentPoint, point.x);
        //points.put(0, currentPoint, point.x); 
        //points.put(1, currentPoint, point.y);
        //if (points == srcPoints && srcDim == 3
         //       || points == dstPoints && dstDim == 3) {
        //    points.put(2, currentPoint, point.z);
        //}
    }

    private boolean checkAndComputeHomography() {
        if (currentPoint == nbPoints) {
            createHomography();
            return true;
        }
        return false;
    }

    private void createHomography() {

      Mat srcMat, dstMat;
      if(srcDim == 2){
        srcMat = new Mat((Point2f) srcPoints);
      }else {
        srcMat = new Mat((Point3f) srcPoints);
      }
      if(dstDim == 2){
        dstMat = new Mat((Point2f) dstPoints);
      }else {
        dstMat = new Mat((Point3f) dstPoints);
      }
      
        findHomography(srcMat, dstMat, cvMat);
        float[] h = (float[])cvMat.createIndexer(false).array();
        // FloatIndexer idx = homographyMatrix.createIndexer();

        if (srcDim == dstDim && srcDim == 2) {
            mat = new Matrix4x4(h[0], h[1], 0, h[2],
                    h[3], h[4], 0, h[5],
                    0, 0, 1, 0,
                    0, 0, 0, 1);
        } else {
            mat = new Matrix4x4(h[0], h[1], h[2], 0,
                    h[3], h[4], h[5], 0,
                    h[6], h[7], h[8], 0,
                    0, 0, 0, 1);
        }
        this.pmatrix = new PMatrix3D(
                (float) mat.matrix[0][0],
                (float) mat.matrix[0][1],
                (float) mat.matrix[0][2],
                (float) mat.matrix[0][3],
                (float) mat.matrix[1][0],
                (float) mat.matrix[1][1],
                (float) mat.matrix[1][2],
                (float) mat.matrix[1][3],
                (float) mat.matrix[2][0],
                (float) mat.matrix[2][1],
                (float) mat.matrix[2][2],
                (float) mat.matrix[2][3],
                (float) mat.matrix[3][0],
                (float) mat.matrix[3][1],
                (float) mat.matrix[3][2],
                (float) mat.matrix[3][3]);

        homographyCalibrationOutput.setMatrix(pmatrix);
    }

    public boolean isComputed() {
        return this.homographyCalibrationOutput.isValid();
    }

    public HomographyCalibration getHomography() {
        assert (isComputed());
        return this.homographyCalibrationOutput;
    }

    @Override
    public String toString() {
        return this.mat.toString();
    }

}
