/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import com.googlecode.javacv.CameraDevice;
import com.googlecode.javacv.ProjectiveDevice;
import com.googlecode.javacv.ProjectorDevice;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class ProjectiveDeviceP {

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

    public ProjectiveDeviceP() {
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
    
    public int getWidth(){
        return this.w;
    }
    public int getHeight(){
        return this.h;
    }

    public int getSize(){
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

        // TODO: put it back to -z somehow ?
        result.z = depth;
        return result;
    }

    public int worldToPixel(Vec3D pt) {

        // Reprojection 
        float invZ = 1.0f / pt.z();

        int px = PApplet.constrain(PApplet.round((pt.x() * invZ * fx) + cx), 0, w - 1);
        int py = PApplet.constrain(PApplet.round((pt.y() * invZ * fy) + cy), 0, h - 1);

        return (int) (py * w + px);
    }

    private static void loadParameters(ProjectiveDevice dev, ProjectiveDeviceP p) {
        double[] camMat = dev.cameraMatrix.get();

        p.intrinsics = new PMatrix3D((float) camMat[0], (float) camMat[1], (float) camMat[2], 0,
                (float) camMat[3], (float) camMat[4], (float) camMat[5], 0,
                (float) camMat[6], (float) camMat[7], (float) camMat[8], 0,
                0, 0, 0, 1);

        double[] projR = dev.R.get();
        double[] projT = dev.T.get();
        p.extrinsics = new PMatrix3D((float) projR[0], (float) projR[1], (float) projR[2], (float) projT[0],
                (float) projR[3], (float) projR[4], (float) projR[5], (float) projT[1],
                (float) projR[6], (float) projR[7], (float) projR[8], (float) projT[2],
                0, 0, 0, 1);

        p.w = dev.imageWidth;
        p.h = dev.imageHeight;
        p.fx = p.intrinsics.m00;
        p.fy = p.intrinsics.m11;
        p.ifx = 1f/ p.intrinsics.m00;
        p.ify = 1f/ p.intrinsics.m11;
        p.cx = p.intrinsics.m02;
        p.cy = p.intrinsics.m12;
    }

    public static ProjectiveDeviceP loadCameraDevice(String filename, int id) throws Exception {
        ProjectiveDeviceP p = new ProjectiveDeviceP();
        try {
            CameraDevice[] camDev = CameraDevice.read(filename);

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
