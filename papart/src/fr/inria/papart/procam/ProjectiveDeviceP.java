/* 
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.inria.papart.procam;

import org.bytedeco.javacv.CameraDevice;
import org.bytedeco.javacv.ProjectiveDevice;
import org.bytedeco.javacv.ProjectorDevice;
import org.bytedeco.javacpp.ARToolKitPlus.ARMarkerInfo;
import org.bytedeco.javacpp.opencv_calib3d;
import org.bytedeco.javacpp.opencv_core.CvMat;

import static org.bytedeco.javacpp.opencv_calib3d.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Ray3D;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class ProjectiveDeviceP implements PConstants, HasExtrinsics {

    private PMatrix3D intrinsics;
    private PMatrix3D extrinsics;
    private ProjectiveDevice device;
    private int w, h;
    private float ifx;
    private float ify;
    private float fx;
    private float fy;
    private float cx;
    private float cy;
    private CvMat intrinsicsMat;
    private boolean hasExtrinsics = false;

    public ProjectiveDeviceP() {
    }

    public ProjectiveDeviceP(int width, int height) {
        this.w = width;
        this.h = height;
    }

    public PMatrix3D getIntrinsics() {
        return this.intrinsics;
    }

    @Override
    public PMatrix3D getExtrinsics() {
        assert (this.hasExtrinsics());
        return this.extrinsics;
    }

    public ProjectiveDevice getDevice() {
        return this.device;
    }

    public int getWidth() {
        return this.w;
    }

    public int getHeight() {
        return this.h;
    }

    public int getSize() {
        return this.w * this.h;
    }

    public void loadParameters() {
    }

    public Vec3D pixelToWorld(int x, int y, float depthValue) {

        Vec3D result = new Vec3D();
        float depth = depthValue;
//        float depth = 1000 * depthLookUp[depthValue]; 
        result.x = (float) ((x - cx) * depth * ifx);
        result.y = (float) ((y - cy) * depth * ify);

        result.z = depth;
        return result;
    }

    // without depth value, focal distance is assumed
    public Vec3D pixelToWorld(int x, int y) {

        Vec3D result = new Vec3D();
        float depth = fx;
//        float depth = 1000 * depthLookUp[depthValue]; 
        result.x = (float) (x - cx);
        result.y = (float) (y - cy);
        result.z = depth;
        return result;
    }

    public PVector pixelToWorldP(int x, int y) {
        PVector result = new PVector();
        float depth = (fx + fy) / 2;
        result.x = (float) x - cx;
        result.y = (float) y - cy;
        result.z = depth;
        return result;
    }

    // To use with a projector...
    public PVector pixelToWorldPDistort(int x, int y, boolean distort) {
        PVector result = new PVector();

        if (distort) {
            double[] out = device.distort(x, y);
            result.x = (float) out[0];
            result.y = (float) out[1];
        }

        float depth = (fx + fy) / 2;
        result.x = (float) x - cx;
        result.y = (float) y - cy;
        result.z = depth;
        return result;
    }

    public PVector pixelToWorldPUndistort(int x, int y, boolean distort) {
        PVector result = new PVector();

        if (distort) {
            double[] out = device.undistort(x, y);
            result.x = (float) out[0];
            result.y = (float) out[1];
        }

        float depth = fx;
        result.x = (float) x - cx;
        result.y = (float) y - cy;
        result.z = depth;
        return result;
    }

    /* * Working, use this one for Low error !
        
     */
    public PVector pixelToWorldNormP(int x, int y) {
        PVector result = new PVector();
        result.x = ((float) x - cx) / fx;
        result.y = ((float) y - cy) / fy;
        result.z = 1;
        return result;
    }

    public PVector pixelToWorldNormPMM(int x, int y, float sizeX) {

        PVector result = pixelToWorldNormP(x, y);

        float sizeY = sizeX * ((float) h / (float) w);
        result.x *= (float) sizeX / (float) w;
        result.y *= (float) sizeY / (float) h;
        return result;
    }

    public int worldToPixel(Vec3D pt) {

        // Reprojection 
        float invZ = 1.0f / pt.z();

        int px = PApplet.constrain(PApplet.round((pt.x() * invZ * fx) + cx), 0, w - 1);
        int py = PApplet.constrain(PApplet.round((pt.y() * invZ * fy) + cy), 0, h - 1);

        return (int) (py * w + px);
    }

    public PVector worldToPixelCoord(Vec3D pt) {

        // Reprojection 
        float invZ = 1.0f / pt.z();

        int px = PApplet.constrain(PApplet.round((pt.x() * invZ * fx) + cx), 0, w - 1);
        int py = PApplet.constrain(PApplet.round((pt.y() * invZ * fy) + cy), 0, h - 1);

        return new PVector(px, py);
    }

    public int worldToPixel(PVector pt) {

        // Reprojection 
        float invZ = 1.0f / pt.z;

        int px = PApplet.constrain(PApplet.round((pt.x * invZ * fx) + cx), 0, w - 1);
        int py = PApplet.constrain(PApplet.round((pt.y * invZ * fy) + cy), 0, h - 1);

        return (int) (py * w + px);
    }

    // TODO: find a name...
    public PVector worldToPixelReal(PVector pt) {

        // Reprojection 
        float invZ = 1.0f / pt.z;

        float px = ((pt.x * invZ * fx) + cx);
        float py = ((pt.y * invZ * fy) + cy);

        return new PVector(px, py);
    }

    public PVector worldToPixel(PVector pt, boolean undistort) {

        // Reprojection 
        float invZ = 1.0f / pt.z;

        int px = PApplet.constrain(PApplet.round((pt.x * invZ * fx) + cx), 0, w - 1);
        int py = PApplet.constrain(PApplet.round((pt.y * invZ * fy) + cy), 0, h - 1);

        if (undistort) {
            double[] out = device.distort(px, py);
            return new PVector((float) out[0], (float) out[1]);
        } else {
            return new PVector(px, py);
        }
    }

    public PVector createRayFrom(PVector pixels) {

        double[] out = device.undistort(pixels.x, pixels.y);

        float norm = PApplet.sqrt(PApplet.pow((float) out[0], 2)
                + PApplet.pow((float) out[1], 2)
                + 1.0f);

        PVector v = new PVector((float) out[0] / norm,
                (float) out[1] / norm,
                1.f / norm);
        return v;
    }

    public PMatrix3D estimateOrientation(PVector[] objectPoints,
            PVector[] imagePoints) {

        assert (objectPoints.length == imagePoints.length);

        CvMat op = CvMat.create(objectPoints.length, 3);
        CvMat ip = CvMat.create(imagePoints.length, 2);
//        Mat op = new Mat(objectPoints.length, 3, CV_32FC1);
//        Mat ip = new Mat(imagePoints.length, 2, CV_32FC1);

        Mat rotation = new Mat(3, 1, CV_32FC1);
        Mat translation = new Mat(3, 1, CV_32FC1);

//        CvMat rotation = CvMat.create(3, 1);
//        CvMat translation = CvMat.create(3, 1);
        // Create internal parameters matrix.
        if (intrinsicsMat == null) {
            intrinsicsMat = CvMat.create(3, 3);

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    intrinsicsMat.put(i, j, 0);
                }
            }

            intrinsicsMat.put(0, 0, intrinsics.m00);
            intrinsicsMat.put(1, 1, intrinsics.m11);
            intrinsicsMat.put(0, 2, intrinsics.m02);
            intrinsicsMat.put(1, 2, intrinsics.m12);
            intrinsicsMat.put(2, 2, 1);
        }

        // Fill the object and image matrices.
        for (int i = 0; i < objectPoints.length; i++) {
            op.put(i, 0, objectPoints[i].x);
            op.put(i, 1, objectPoints[i].y);
            op.put(i, 2, objectPoints[i].z);

            ip.put(i, 0, imagePoints[i].x);
            ip.put(i, 1, imagePoints[i].y);
        }

        // TODO: remove the new ...
//        ITERATIVE=CV_ITERATIVE,
//        EPNP=CV_EPNP,
//        P3P=CV_P3P;
        // Convert all to Mat, instead of CvMat
        boolean solved = opencv_calib3d.solvePnP(new Mat(op),
                new Mat(ip),
                new Mat(intrinsicsMat), new Mat(),
                rotation, translation,
                false, opencv_calib3d.ITERATIVE);

//        boolean solvePnP(@InputArray CvMat objectPoints,
//            @InputArray CvMat imagePoints, @InputArray CvMat cameraMatrix,
//            @InputArray CvMat distCoeffs,  @OutputArray CvMat rvec,
//            @OutputArray CvMat tvec, boolean useExtrinsicGuess
        PMatrix3D mat = new PMatrix3D();

        CvMat rotMat = CvMat.create(3, 3);
        cvRodrigues2(rotation.asCvMat(), rotMat, null);

        CvMat translationCv = translation.asCvMat();

        float RTMat[] = {
            (float) rotMat.get(0), (float) rotMat.get(1), (float) rotMat.get(2), (float) translationCv.get(0),
            (float) rotMat.get(3), (float) rotMat.get(4), (float) rotMat.get(5), (float) translationCv.get(1),
            (float) rotMat.get(6), (float) rotMat.get(7), (float) rotMat.get(8), (float) translationCv.get(2),
            0, 0, 0, 1f};
        mat.set(RTMat);
        return mat;
    }

    private static void loadParameters(ProjectiveDevice dev, ProjectiveDeviceP p) {
        double[] camMat = dev.cameraMatrix.get();

        p.intrinsics = new PMatrix3D((float) camMat[0], (float) camMat[1], (float) camMat[2], 0,
                (float) camMat[3], (float) camMat[4], (float) camMat[5], 0,
                (float) camMat[6], (float) camMat[7], (float) camMat[8], 0,
                0, 0, 0, 1);

        p.w = dev.imageWidth;
        p.h = dev.imageHeight;
        p.fx = p.intrinsics.m00;
        p.fy = p.intrinsics.m11;
        p.ifx = 1f / p.intrinsics.m00;
        p.ify = 1f / p.intrinsics.m11;
        p.cx = p.intrinsics.m02;
        p.cy = p.intrinsics.m12;

        p.hasExtrinsics = dev.R != null && dev.T != null;

        if (p.hasExtrinsics()) {
            double[] projR = dev.R.get();
            double[] projT = dev.T.get();

            p.extrinsics = new PMatrix3D((float) projR[0], (float) projR[1], (float) projR[2], (float) projT[0],
                    (float) projR[3], (float) projR[4], (float) projR[5], (float) projT[1],
                    (float) projR[6], (float) projR[7], (float) projR[8], (float) projT[2],
                    0, 0, 0, 1);
        }

    }

    public static ProjectiveDeviceP loadCameraDevice(String filename, int id) throws Exception {
        ProjectiveDeviceP p = new ProjectiveDeviceP();

        CameraDevice[] camDev = CameraDevice.read(filename);
        if (camDev.length <= id) {
            throw new Exception("No camera device with the id " + id + " in the calibration file: " + filename);
        }
        CameraDevice cameraDevice = camDev[id];
        p.device = cameraDevice;
        loadParameters(cameraDevice, p);

        return p;
    }

    public static ProjectiveDeviceP loadProjectorDevice(String filename, int id) throws Exception {

        ProjectiveDeviceP p = new ProjectiveDeviceP();

        try {
            ProjectorDevice[] camDev = ProjectorDevice.read(filename);

            if (camDev.length <= id) {
                throw new Exception("No projector device with the id " + id + " in the calibration file: " + filename);
            }
            ProjectorDevice projectorDevice = camDev[id];
            p.device = projectorDevice;
            loadParameters(projectorDevice, p);

        } catch (Exception e) {
            throw new Exception("Error reading the calibration file : " + filename + " \n" + e);
        }

        return p;
    }

    public static ProjectiveDeviceP loadProjectiveDevice(String filename, int id) throws Exception {

        ProjectiveDeviceP p = new ProjectiveDeviceP();

        try {
            ProjectiveDevice[] camDev = ProjectiveDevice.read(filename);

            if (camDev.length <= id) {
                throw new Exception("No projective device with the id " + id + " in the calibration file: " + filename);
            }
            ProjectiveDevice projectiveDevice = camDev[id];
            p.device = projectiveDevice;
            loadParameters(projectiveDevice, p);

        } catch (Exception e) {
            throw new Exception("Error reading the calibration file : " + filename + " \n" + e);
        }

        return p;
    }

    @Override
    public boolean hasExtrinsics() {
        return this.hasExtrinsics;
    }
    
    public PVector getCoordinates(int offset){
        return new PVector(offset % w, offset / w);
    }

    public String toString() {
        return "intr " + intrinsics.toString() + " extr " + extrinsics.toString() + " "
                + " width " + w + " height " + h;
    }
}
