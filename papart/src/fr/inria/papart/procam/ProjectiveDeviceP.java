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
package fr.inria.papart.procam;

import fr.inria.papart.calibration.files.ProjectiveDeviceCalibration;
import org.bytedeco.javacv.CameraDevice;
import org.bytedeco.javacv.ProjectiveDevice;
import org.bytedeco.javacv.ProjectorDevice;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.opencv_calib3d;

import static org.bytedeco.javacpp.opencv_calib3d.*;
import static org.bytedeco.javacpp.opencv_core.*;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.ReadonlyVec3D;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole
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
    private Mat intrinsicsMat;
    private boolean hasExtrinsics = false;
    private boolean handleDistorsion = false;

    private ProjectiveDeviceP() {
    }

    public float getFx() {
        return fx;
    }

    public float getFy() {
        return fy;
    }

    public float getCx() {
        return cx;
    }

    public float getCy() {
        return cy;
    }

//    public ProjectiveDeviceP(int width, int height) {
//        this.w = width;
//        this.h = height;
//    }
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

    @Override
    public boolean hasExtrinsics() {
        return this.hasExtrinsics;
    }

    public PVector getCoordinates(int offset) {
        return new PVector(offset % w, offset / w);
    }

    public boolean handleDistorsions() {
        return this.handleDistorsion;
    }

    /**
     * Compute a pixel position at 1 depth.
     *
     * @param x in pixel space
     * @param y in pixel space
     * @return
     */
    public PVector pixelToWorldNormP(int x, int y) {
        PVector result = new PVector();
        result.x = ((float) x - cx) / fx;
        result.y = ((float) y - cy) / fy;
        result.z = 1;
        return result;
    }

    /**
     * Hande distorsions
     *
     * @param x
     * @param y
     * @return
     */
    public PVector pixelToWorldNormalized(float x, float y) {
        return pixelToWorldNormalized(x, y, true);
    }

    /**
     * Hande distorsions
     *
     * @param x
     * @param y
     * @param distort
     * @return
     */
    public PVector pixelToWorldNormalized(float x, float y, boolean distort) {

        if (this.handleDistorsion && distort) {
            double[] out = device.distort(x * w, y * h);
//            double[] out = device.distort(x * w, y * h);
            x = (float) (out[0]) / (float) w;
            y = (float) (out[1]) / (float) h;
        }
        PVector result = new PVector();
        result.x = (x * this.w - cx) / fx;
        result.y = (y * this.h - cy) / fy;
        result.z = 1;
        return result;
    }

    /**
     * For internal use, the result is passed, not generated.
     *
     * @param x
     * @param y
     * @param depthValue
     * @param result
     */
    public void pixelToWorld(int x, int y, float depthValue, Vec3D result) {
        float depth = depthValue;
//        float depth = 1000 * depthLookUp[depthValue]; 
        result.x = (float) (((float) x - cx) * depth * ifx);
        result.y = (float) (((float) y - cy) * depth * ify);
        result.z = depth;
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

    public int worldToPixel(ReadonlyVec3D pt) {
        return worldToPixel(pt.x(), pt.y(), pt.z());
    }

    public int worldToPixel(Vec3D pt) {
        return worldToPixel(pt.x(), pt.y(), pt.z());
    }

    public int worldToPixel(float x, float y, float z) {
        // Reprojection 
        float invZ = 1.0f / z;

        int px = PApplet.constrain(PApplet.round((x * invZ * fx) + cx), 0, w - 1);
        int py = PApplet.constrain(PApplet.round((y * invZ * fy) + cy), 0, h - 1);

        return (int) (py * w + px);
    }

    public PVector worldToPixelCoord(Vec3D pt) {

        // Reprojection 
        float invZ = 1.0f / pt.z();

        int px = PApplet.constrain(PApplet.round((pt.x() * invZ * fx) + cx), 0, w - 1);
        int py = PApplet.constrain(PApplet.round((pt.y() * invZ * fy) + cy), 0, h - 1);

        return new PVector(px, py);
    }

    /**
     * Handle distorsions
     *
     * @param pt
     * @return
     */
    public PVector worldToPixelCoord(PVector pt) {
        return worldToPixel(pt, true);
    }

    /**
     * Handle distorsions
     *
     * @param pt
     * @return
     */
    public PVector worldToPixelCoord(PVector pt, boolean undist) {

        // Reprojection 
        float invZ = 1.0f / pt.z;

        int px = PApplet.constrain(PApplet.round((pt.x * invZ * fx) + cx), 0, w - 1);
        int py = PApplet.constrain(PApplet.round((pt.y * invZ * fy) + cy), 0, h - 1);

        if (this.handleDistorsion && undist) {
            double[] out = device.undistort(px, py);
            px = (int) (out[0]);
            py = (int) (out[1]);
        }

        return new PVector(px, py);
    }

    public int worldToPixel(PVector pt) {

        // Reprojection 
        float invZ = 1.0f / pt.z;

        int px = PApplet.constrain(PApplet.round((pt.x * invZ * fx) + cx), 0, w - 1);
        int py = PApplet.constrain(PApplet.round((pt.y * invZ * fy) + cy), 0, h - 1);

        return (int) (py * w + px);
    }

    /**
     * Similar tor worldToPixel without border checking.
     *
     * @param pt
     * @return
     */
    public PVector worldToPixelUnconstrained(PVector pt) {

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

        if (undistort && this.handleDistorsion) {
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

        Mat op = new Mat(objectPoints.length, 3, CV_32FC1);
        Mat ip = new Mat(imagePoints.length, 2, CV_32FC1);
        Mat rotation = new Mat(3, 1, CV_64FC1);
        Mat translation = new Mat(3, 1, CV_64FC1);

        initNativeIntrinsic();
        fillNative(objectPoints, imagePoints, op, ip);

        boolean solved = opencv_calib3d.solvePnP(op,
                ip,
                intrinsicsMat, new Mat(),
                rotation, translation,
                false, opencv_calib3d.SOLVEPNP_ITERATIVE);

        Mat rotMat = new Mat(3, 3, CV_64FC1);
        Rodrigues(rotation, rotMat);

        double[] rotationIndex = (double[]) rotMat.createIndexer(false).array();
        double[] translationIndex = (double[]) translation.createIndexer(false).array();

//        float RTMat[] = {
//            (float) rotationIndex[0], (float) rotationIndex[1], (float) rotationIndex[2], (float) translationIndex[0],
//            (float) rotationIndex[3], (float) rotationIndex[4], (float) rotationIndex[5], (float) translationIndex[1],
//            (float) rotationIndex[6], (float) rotationIndex[7], (float) rotationIndex[8], (float) translationIndex[2],
//            0, 0, 0, 1f};
        PMatrix3D mat = new PMatrix3D((float) rotationIndex[0], (float) rotationIndex[1], (float) rotationIndex[2], (float) translationIndex[0],
                (float) rotationIndex[3], (float) rotationIndex[4], (float) rotationIndex[5], (float) translationIndex[1],
                (float) rotationIndex[6], (float) rotationIndex[7], (float) rotationIndex[8], (float) translationIndex[2],
                0, 0, 0, 1f);
//        mat.set(RTMat);

        return mat;
    }

    // TODO: update this estimationRansac !
    public PMatrix3D estimateOrientationRansac(PVector[] objectPoints,
            PVector[] imagePoints) {

        assert (objectPoints.length == imagePoints.length);

        Mat op = new Mat(objectPoints.length, 3, CV_32FC1);
        Mat ip = new Mat(imagePoints.length, 2, CV_32FC1);
        Mat rotation = new Mat(3, 1, CV_64FC1);
        Mat translation = new Mat(3, 1, CV_64FC1);

        initNativeIntrinsic();
        fillNative(objectPoints, imagePoints, op, ip);

//        Mat distorsions = new Mat(4, 1, CV_32FC1);
//        FloatIndexer distort = distorsions.createIndexer();
//        distort.put(0, 0, 0);
//        distort.put(1, 0, 0);
//        distort.put(2, 0, 0);
//        distort.put(3, 0, 0);
        boolean solvePnPRansac = opencv_calib3d.solvePnPRansac(
                op,
                ip,
                intrinsicsMat,
                new Mat(),
                rotation,
                translation,
                false, // extrinsic guess
                100, // iterationsCount
                8.0f, // reprojError
                0.99, // confidence
                new Mat(), // outputArray
                opencv_calib3d.SOLVEPNP_ITERATIVE);
//        boolean solvePnPRansac = opencv_calib3d.solvePnPRansac(
//                op,
//                ip,
//                intrinsicsMat, new Mat(),
//                rotation, translation);

//        @Namespace("cv") public static native @Cast("bool") boolean solvePnPRansac( @ByVal Mat objectPoints, @ByVal Mat imagePoints,
//                                  @ByVal Mat cameraMatrix, @ByVal Mat distCoeffs,
//                                  @ByVal Mat rvec, @ByVal Mat tvec,
//                                  @Cast("bool") boolean useExtrinsicGuess/*=false*/, int iterationsCount/*=100*/,
//                                  float reprojectionError/*=8.0*/, double confidence/*=0.99*/,
//                                  @ByVal(nullValue = "cv::noArray()") Mat inliers/*=cv::noArray()*/, int flags/*=cv::SOLVEPNP_ITERATIVE*/ );
        Mat rotMat = new Mat(3, 3, CV_64FC1);
        Rodrigues(rotation, rotMat);

        double[] rotationIndex = (double[]) rotMat.createIndexer(false).array();
        double[] translationIndex = (double[]) translation.createIndexer(false).array();

        PMatrix3D mat = new PMatrix3D((float) rotationIndex[0], (float) rotationIndex[1], (float) rotationIndex[2], (float) translationIndex[0],
                (float) rotationIndex[3], (float) rotationIndex[4], (float) rotationIndex[5], (float) translationIndex[1],
                (float) rotationIndex[6], (float) rotationIndex[7], (float) rotationIndex[8], (float) translationIndex[2],
                0, 0, 0, 1f);
//        mat.set(RTMat);
        return mat;
    }

    private void initNativeIntrinsic() {
        if (intrinsicsMat == null) {
            intrinsicsMat = new Mat(3, 3, CV_32FC1);
            FloatIndexer intrinsicIdx = intrinsicsMat.createIndexer(true);

            // init to 0
            int k = 0;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    intrinsicIdx.put(k++, 0);
                }
            }

            // set the values
            intrinsicIdx.put(0, 0, intrinsics.m00);
            intrinsicIdx.put(1, 1, intrinsics.m11);
            intrinsicIdx.put(0, 2, intrinsics.m02);
            intrinsicIdx.put(1, 2, intrinsics.m12);
            intrinsicIdx.put(2, 2, 1);
        }
    }

    private void fillNative(PVector[] objectPoints,
            PVector[] imagePoints,
            Mat op, Mat ip) {

        FloatIndexer opIdx = op.createIndexer();
        FloatIndexer ipIdx = ip.createIndexer();

        // Fill the object and image matrices.
        for (int i = 0; i < objectPoints.length; i++) {
            opIdx.put(i, 0, objectPoints[i].x);
            opIdx.put(i, 1, objectPoints[i].y);
            opIdx.put(i, 2, objectPoints[i].z);

            ipIdx.put(i, 0, imagePoints[i].x);
            ipIdx.put(i, 1, imagePoints[i].y);
        }
    }

    /**
     * Save an intrinsic calibration. 
     * Store a calibration, either in XML (Papart) or YAML (opencv) format. 
     * It also stores the extrinsic calibration. 
     * @param applet
     * @param filename  path, can end with ".xml" or ".yaml"
     */
    public void saveTo(PApplet applet, String filename) {
        saveTo(applet, filename, true);
    }

        /**
     * Save an intrinsic calibration. 
     * Store a calibration, either in XML (Papart) or YAML (opencv) format. 
     *  It also stores the extrinsic calibration. 
     * @param applet
     * @param filename  path, can end with ".xml" or ".yaml"
     * @param isCamera  true if it is a camera, false is it is a projector. 
     */
    public void saveTo(PApplet applet, String filename, boolean isCamera) {
        ProjectiveDeviceCalibration calib = new ProjectiveDeviceCalibration();
        calib.setWidth(this.w);
        calib.setHeight(this.h);
        calib.setIntrinsics(intrinsics);
        calib.isCamera(isCamera);
        if (this.hasExtrinsics()) {
            calib.setExtrinsics(extrinsics);
        }
        calib.saveTo(applet, filename);
    }

    public void saveCameraTo(PApplet applet, String filename) {
        saveTo(applet, filename, true);
    }

    public void saveProjectorTo(PApplet applet, String filename) {
        saveTo(applet, filename, false);
    }

    public static ProjectiveDeviceP createSimpleDevice(float fx, float fy, float cx, float cy, int w, int h) {
        ProjectiveDeviceP p = new ProjectiveDeviceP();
        // Do not update the handle distorsions ?
//        p.handleDistorsion = false;
        p.w = w;
        p.h = h;
        p.intrinsics = new PMatrix3D(fx, 0, cx, 0,
                0, fy, cy, 0,
                0, 0, 0, 0,
                0, 0, 0, 0);
        p.updateFromIntrinsics();
        p.device = null;
        return p;
    }

    public static ProjectiveDeviceP createDevice(float fx, float fy, float cx, float cy, int w, int h,
            float k1, float k2, float k3, float k4, float k5) {
        ProjectiveDeviceP p = new ProjectiveDeviceP();
        // Do not update the handle distorsions ?
//        p.handleDistorsion = false;
        p.w = w;
        p.h = h;
        p.intrinsics = new PMatrix3D(fx, 0, cx, 0,
                0, fy, cy, 0,
                0, 0, 0, 0,
                0, 0, 0, 0);
        p.updateFromIntrinsics();

        p.device = new ProjectiveDevice("device");
        ProjectiveDevice d = p.device;

        d.cameraMatrix = CvMat.create(3, 3);

        d.cameraMatrix.put(fx, 0.0, cx,
                0.0, fy, cy,
                0.0, 0.0, 1);

        d.imageWidth = w;
        d.imageHeight = h;
        d.distortionCoeffs = CvMat.create(1, 5);
        d.distortionCoeffs.put(0, k1);
        d.distortionCoeffs.put(1, k2);
        d.distortionCoeffs.put(2, k3);
        d.distortionCoeffs.put(3, k4);
        d.distortionCoeffs.put(4, k5);
        p.handleDistorsion = true;

        return p;
    }

    public static ProjectiveDeviceP loadCameraDevice(PApplet parent, String filename) throws Exception {
        return loadCameraDevice(parent, filename, 0);
    }

    public static ProjectiveDeviceP loadCameraDevice(PApplet parent, String filename, int id) throws Exception {
        ProjectiveDeviceP p = new ProjectiveDeviceP();

        if (filename.endsWith(".yaml")) {
            CameraDevice[] camDev = CameraDevice.read(filename);
            if (camDev.length <= id) {
                throw new Exception("No camera device with the id " + id + " in the calibration file: " + filename);
            }
            CameraDevice cameraDevice = camDev[id];
            loadParameters(cameraDevice, p);
        }

        if (filename.endsWith((".xml"))) {
            ProjectiveDeviceCalibration calib = new ProjectiveDeviceCalibration();
            calib.loadFrom(parent, filename);
            loadParameters(calib, p);
        }

        return p;
    }

    public static ProjectiveDeviceP loadProjectorDevice(PApplet parent, String filename) throws Exception {
        return loadProjectorDevice(parent, filename, 0);
    }

    public static ProjectiveDeviceP loadProjectorDevice(PApplet parent, String filename, int id) throws Exception {

        ProjectiveDeviceP p = new ProjectiveDeviceP();
        if (filename.endsWith((".yaml"))) {
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
        }

        if (filename.endsWith((".xml"))) {
            ProjectiveDeviceCalibration calib = new ProjectiveDeviceCalibration();
            calib.loadFrom(parent, filename);
            loadParameters(calib, p);
        }
        return p;
    }

    @Deprecated
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

    public static void loadParameters(ProjectiveDevice dev, ProjectiveDeviceP p) {
        double[] camMat = dev.cameraMatrix.get();

        p.handleDistorsion = dev.distortionCoeffs != null;

        p.intrinsics = new PMatrix3D((float) camMat[0], (float) camMat[1], (float) camMat[2], 0,
                (float) camMat[3], (float) camMat[4], (float) camMat[5], 0,
                (float) camMat[6], (float) camMat[7], (float) camMat[8], 0,
                0, 0, 0, 1);

        p.w = dev.imageWidth;
        p.h = dev.imageHeight;

        p.updateFromIntrinsics();

        p.hasExtrinsics = dev.R != null && dev.T != null;

        if (p.hasExtrinsics()) {
            double[] projR = dev.R.get();
            double[] projT = dev.T.get();

            p.extrinsics = new PMatrix3D((float) projR[0], (float) projR[1], (float) projR[2], (float) projT[0],
                    (float) projR[3], (float) projR[4], (float) projR[5], (float) projT[1],
                    (float) projR[6], (float) projR[7], (float) projR[8], (float) projT[2],
                    0, 0, 0, 1);
        }
        p.device = dev;
    }

    public static void loadParameters(ProjectiveDeviceCalibration dev, ProjectiveDeviceP p) {
        // Not implemented yet
        p.handleDistorsion = false;
        p.intrinsics = dev.getIntrinsics();

        p.w = dev.getWidth();
        p.h = dev.getHeight();
        p.updateFromIntrinsics();

        if (p.hasExtrinsics()) {
            p.extrinsics = dev.getExtrinsics();
        }

        p.device = null;
    }

    public void setIntrinsics(PMatrix3D intrinsics) {
        this.intrinsics.set(intrinsics);
        updateFromIntrinsics();
    }

    public void updateFromIntrinsics() {
        fx = intrinsics.m00;
        fy = intrinsics.m11;
        ifx = 1f / intrinsics.m00;
        ify = 1f / intrinsics.m11;
        cx = intrinsics.m02;
        cy = intrinsics.m12;
    }

    public String toString() {
        return "intr " + intrinsics.toString() + (extrinsics != null ? " extr " + extrinsics.toString() : " ") + " "
                + " width " + w + " height " + h
                + " fx " + fx + " fy " + fy
                + " cx " + cx + " cy " + cy;
    }
}
