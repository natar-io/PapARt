/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import com.googlecode.javacv.CameraDevice;
import com.googlecode.javacv.ProjectiveDevice;
import com.googlecode.javacv.ProjectorDevice;
import processing.core.PMatrix3D;

/**
 *
 * @author jiii
 */
public class ProjectiveDeviceP {

    private PMatrix3D intrinsics;
    private PMatrix3D extrinsics;
    private ProjectiveDevice device;

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
