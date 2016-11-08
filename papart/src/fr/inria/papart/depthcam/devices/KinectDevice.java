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
package fr.inria.papart.depthcam.devices;

import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.multitouch.KinectTouchInput;
import fr.inria.papart.procam.HasExtrinsics;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.SubCamera;
import fr.inria.papart.procam.camera.SubDepthCamera;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public abstract class KinectDevice {

    public enum Type {
        ONE, X360, REALSENSE, NONE
    }
    // IR and Depth image size 
    public static int WIDTH;
    public static int HEIGHT;
    public static int SIZE;

    // RGB image size
    public static int RGB_WIDTH;
    public static int RGB_HEIGHT;
    public static int RGB_SIZE;
    private final PMatrix3D KinectRGBIRCalibration = new PMatrix3D(1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1);
    private PMatrix3D KinectRGBIRCalibrationInv = new PMatrix3D(1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1);

    protected PApplet parent;

    abstract public SubCamera getCameraRGB();

    abstract public SubCamera getCameraIR();

    abstract public SubDepthCamera getCameraDepth();

    abstract public int rawDepthSize();

    abstract public Type type();

    abstract public void setTouch(KinectTouchInput kinectTouchInput);

    public void close() {
        getCameraDepth().close();
        getCameraRGB().close();
    }

    public void setStereoCalibration(String fileName) {
        HomographyCalibration calib = new HomographyCalibration();
        calib.loadFrom(parent, fileName);
        setStereoCalibration(calib.getHomography());
    }

    public void setStereoCalibration(PMatrix3D matrix) {
        KinectRGBIRCalibration.set(matrix);
        KinectRGBIRCalibrationInv = KinectRGBIRCalibration.get();
        KinectRGBIRCalibrationInv.invert();
    }

    /**
     * Depth to color extrinsics
     */
    public PMatrix3D getStereoCalibration() {
        return KinectRGBIRCalibration;
    }

    /**
     * Color to Depth extrinsics
     */
    public PMatrix3D getStereoCalibrationInv() {
        return KinectRGBIRCalibrationInv;
    }

    public void setExtrinsics(PMatrix3D extr) {
        getCameraRGB().setExtrinsics(extr);

        // Get color -> Depth 
        PMatrix3D stereo = getStereoCalibrationInv();
        PMatrix3D tmp = extr.get();
        tmp.apply(stereo);
        getCameraDepth().setExtrinsics(tmp);
    }

    public int findColorOffset(Vec3D v) {
        return findColorOffset(v.x, v.y, v.z);
    }

    public int findColorOffset(PVector v) {
        return findColorOffset(v.x, v.y, v.z);
    }

    private PVector vt = new PVector();
    private PVector vt2 = new PVector();

    /**
     * Warning not thread safe.
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public int findColorOffset(float x, float y, float z) {
        vt.set(x, y, z);
        vt2.set(0, 0, 0);
        //  Ideally use a calibration... 
//        kinectCalibRGB.getExtrinsics().mult(vt, vt2);       
        getStereoCalibration().mult(vt, vt2);
        return getCameraRGB().getProjectiveDevice().worldToPixel(vt2.x, vt2.y, vt2.z);
    }

    // TODO: find the use of all this..
    public int colorWidth() {
        return RGB_WIDTH;
    }

    public int colorHeight() {
        return RGB_HEIGHT;
    }

    public int colorSize() {
        return RGB_SIZE;
    }

    public int depthWidth() {
        return WIDTH;
    }

    public int depthHeight() {
        return HEIGHT;
    }

    public int depthSize() {
        return SIZE;
    }

}
