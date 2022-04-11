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
import java.nio.FloatBuffer;

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

    private Mat srcPoints, dstPoints;
    private FloatBuffer srcIdx, dstIdx;

    private final int srcDim;
    private final int dstDim;
    private final int nbPoints;

    public static final Mat INVALID_HOMOGRAPHY = new Mat(); 

    private HomographyCalibration homographyCalibrationOutput;
    private Mat homographyMat;
    private int currentPoint = 0;

    // SRCDim is always 3 for now
    public HomographyCreator(int srcDim, int dstDim, int nbPoints) {
        this.srcDim = srcDim;
        this.dstDim = dstDim;
        this.nbPoints = nbPoints;
        init();
    }


    private void init() {
        currentPoint = 0;
        srcPoints = new Mat(nbPoints,1, CV_32FC2);

        if(dstDim == 3){
        dstPoints = new Mat(nbPoints,1, CV_32FC3);
        }else {
          dstPoints = new Mat(nbPoints,1, CV_32FC2);
        }
        srcIdx = srcPoints.createBuffer();
        dstIdx = dstPoints.createBuffer();
    
        homographyCalibrationOutput = new HomographyCalibration();
    }
    
    public boolean addPoint(PVector src, PVector dst) {
        //System.out.println("dim: " + srcDim + " " + dstDim + " " + nbPoints);
        //System.out.println("addpoint: " + currentPoint + " " + src + " " + dst);

        srcIdx.put(currentPoint * 2,    src.x);  
        srcIdx.put(currentPoint * 2 +1, src.y); 
    
        if(dstDim == 3){ 
          dstIdx.put(currentPoint * 3,    dst.x);  
          dstIdx.put(currentPoint * 3 +1, dst.y); 
          dstIdx.put(currentPoint * 3 +2, dst.z); 
        } else {
          dstIdx.put(currentPoint * 2,    dst.x);  
          dstIdx.put(currentPoint * 2 +1, dst.y); 
        }

        currentPoint++;
        return checkAndComputeHomography();
    }

    private boolean checkAndComputeHomography() {
        if (currentPoint == nbPoints) {
            createHomography();
            return true;
        }
        return false;
    }

    private void createHomography() {

      Mat H = findHomography(srcPoints, dstPoints); // , cvMat);
      homographyMat = H;

      if(H.empty()){
        System.out.println("H empty");

        homographyCalibrationOutput.setMatrix(
          new PMatrix3D(0,0,0,0, 
                        0,0,0,0, 
                        0,0,0,0, 
                        0,0,0,0));
        return;
      }

     //  Mat H = findHomography(pt1, pt2, CV_RANSAC, settings.ransacReprojThreshold, mask, 2000, 0.995);
      double[] h = (double[])H.createIndexer(false).array();

      //  float[] h = (float[])cvMat.createIndexer(false).array();
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

    public Mat getHomographyMat(){
      return homographyMat;
    }

    @Override
    public String toString() {
        return this.mat.toString();
    }

}
