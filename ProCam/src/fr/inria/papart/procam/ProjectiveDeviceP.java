/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import com.googlecode.javacv.CameraDevice;
import com.googlecode.javacv.ProjectiveDevice;
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
    
    public PMatrix3D getIntrinsics(){
        return this.intrinsics;
    }

    public PMatrix3D getExtrinsics(){
        return this.extrinsics;
    }
    
    public ProjectiveDevice getDevice(){
        return this.device;
    }

    public ProjectiveDeviceP loadCameraDevice(String filename, int id) throws Exception{

        ProjectiveDeviceP p = new ProjectiveDeviceP();
        
        try {
            CameraDevice[] camDev = CameraDevice.read(filename);
            
            if (camDev.length <= id) {
                throw new Exception("No camera device with the id " + id + " in the calibration file: " + filename);
            }
            CameraDevice cameraDevice = camDev[id];
            p.device = cameraDevice;
            
            double[] camMat = cameraDevice.cameraMatrix.get();
            p.intrinsics = new PMatrix3D((float) camMat[0], (float) camMat[1], (float) camMat[2], 0,
                    (float) camMat[3], (float) camMat[4], (float) camMat[5], 0,
                    (float) camMat[6], (float) camMat[7], (float) camMat[8], 0,
                    0, 0, 0, 1);
            
            camMat = cameraDevice.extrParams.get();
            p.extrinsics = new PMatrix3D((float) camMat[0], (float) camMat[1], (float) camMat[2], 0,
                    (float) camMat[3], (float) camMat[4], (float) camMat[5], 0,
                    (float) camMat[6], (float) camMat[7], (float) camMat[8], 0,
                    0, 0, 0, 1);

        } catch (Exception e) {
            throw new Exception("Error reading the calibration file : " + filename + " \n" + e);
        }

        return p;
    }
}
