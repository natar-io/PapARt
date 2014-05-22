/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import com.googlecode.javacv.CameraDevice;
import com.googlecode.javacv.ProjectiveDevice;
import com.googlecode.javacv.ProjectorDevice;
import com.googlecode.javacv.cpp.ARToolKitPlus.ARMarkerInfo;
import com.googlecode.javacv.cpp.opencv_calib3d;
import com.googlecode.javacv.cpp.opencv_core.CvMat;

import static com.googlecode.javacv.cpp.opencv_calib3d.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

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
public class ProjectiveDeviceP implements PConstants {

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

    public ProjectiveDeviceP() {
    }

    public ProjectiveDeviceP(int width, int height) {
        this.w = width;
        this.h = height;
    }

    public PMatrix3D getIntrinsics() {
        return this.intrinsics;
    }

    public PMatrix3D getExtrinsics() {
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

        float sizeY = sizeX *  ((float)h/(float) w);
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

    public int worldToPixel(PVector pt) {

        // Reprojection 
        float invZ = 1.0f / pt.z;

        int px = PApplet.constrain(PApplet.round((pt.x * invZ * fx) + cx), 0, w - 1);
        int py = PApplet.constrain(PApplet.round((pt.y * invZ * fy) + cy), 0, h - 1);

        return (int) (py * w + px);
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

//    public PMatrix3D estimateOrientation2(PVector[] objectPoints, PVector[] imagePoints) {
//        ARMarkerInfo marker_info = new ARMarkerInfo();
//        public native float arGetTransMat(ARMarkerInfo marker_info, float center[/*2*/],
//                float width, @Cast("ARFloat(*)[4]") float conv[/*3][4*/]);
//        public native float arGetTransMatCont(ARMarkerInfo marker_info,
//                @Cast("ARFloat(*)[4]") float prev_conv[/*3][4*/],   float center[/*2*/],
//                float width, @Cast("ARFloat(*)[4]") float conv[/*3][4*/]);
//        return null;
//    }
    public PMatrix3D estimateOrientation(PVector[] objectPoints,
            PVector[] imagePoints) {

        assert (objectPoints.length == imagePoints.length);

        CvMat op = CvMat.create(objectPoints.length, 3);
        CvMat ip = CvMat.create(imagePoints.length, 2);

        CvMat rotation = CvMat.create(3, 1);
        CvMat translation = CvMat.create(3, 1);

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

//        ITERATIVE=CV_ITERATIVE,
//        EPNP=CV_EPNP,
//        P3P=CV_P3P;
        boolean solved = opencv_calib3d.solvePnP(op, ip, intrinsicsMat, null, rotation, translation,
                false, opencv_calib3d.ITERATIVE);

//        boolean solvePnP(@InputArray CvMat objectPoints,
//            @InputArray CvMat imagePoints, @InputArray CvMat cameraMatrix,
//            @InputArray CvMat distCoeffs,  @OutputArray CvMat rvec,
//            @OutputArray CvMat tvec, boolean useExtrinsicGuess/*=false*/, int flags/*=ITERATIVE*/);
        PMatrix3D mat = new PMatrix3D();

        CvMat rotMat = CvMat.create(3, 3);
        rotation.put(0, rotation.get(0));
        rotation.put(1, rotation.get(1));
        rotation.put(2, rotation.get(2));

        cvRodrigues2(rotation, rotMat, null);

        float RTMat[] = {
            (float) rotMat.get(0), (float) rotMat.get(1), (float) rotMat.get(2), (float) translation.get(0),
            (float) rotMat.get(3), (float) rotMat.get(4), (float) rotMat.get(5), (float) translation.get(1),
            (float) rotMat.get(6), (float) rotMat.get(7), (float) rotMat.get(8), (float) translation.get(2),
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

        try {

            double[] projR = dev.R.get();
            double[] projT = dev.T.get();
            p.extrinsics = new PMatrix3D((float) projR[0], (float) projR[1], (float) projR[2], (float) projT[0],
                    (float) projR[3], (float) projR[4], (float) projR[5], (float) projT[1],
                    (float) projR[6], (float) projR[7], (float) projR[8], (float) projT[2],
                    0, 0, 0, 1);
        } catch (NullPointerException npe) {
//            System.out.println("Loading Parameters, without extrinsics");
        }

    }

    public static ProjectiveDeviceP loadCameraDevice(String filename, int id) throws Exception {
        ProjectiveDeviceP p = new ProjectiveDeviceP();
        try {
//            System.out.println("Loading camera device file :" + filename);

            CameraDevice[] camDev = CameraDevice.read(filename);
//            System.out.println("Loading camera device OK.");

            if (camDev.length <= id) {
                throw new Exception("No camera device with the id " + id + " in the calibration file: " + filename);
            }
            CameraDevice cameraDevice = camDev[id];
            p.device = cameraDevice;
            loadParameters(cameraDevice, p);

        } catch (Exception e) {
            throw new Exception("Error reading the calibration file : " + filename + " \n" + e);
        }

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
}
