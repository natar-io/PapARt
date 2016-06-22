/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.depthcam.devices;

import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.multitouch.KinectTouchInput;
import fr.inria.papart.procam.HasExtrinsics;
import fr.inria.papart.procam.camera.Camera;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public abstract class KinectDevice implements DepthCameraDevice {

    public enum Type {
        ONE, X360, NONE
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

    abstract public Camera getCameraRGB();

    abstract public Camera getCameraIR();

    abstract public Camera getCameraDepth();

    abstract public int rawDepthSize();

    abstract public Type type();

    abstract public void setTouch(KinectTouchInput kinectTouchInput);

    public void close() {
        getCameraDepth().close();
        getCameraRGB().close();
    }

    public static KinectDevice createKinect360(PApplet parent) {
        return new Kinect360(parent);
    }

    public static KinectDevice createKinectOne(PApplet parent) {
        return new KinectOne(parent);
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
     * Depth -> color extrinsics
     */
    public PMatrix3D getStereoCalibration() {
        return KinectRGBIRCalibration;
    }

    /**
     * Color -> Depth extrinsics
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

    @Override
    public int colorWidth() {
        return RGB_WIDTH;
    }

    @Override
    public int colorHeight() {
        return RGB_HEIGHT;
    }

    @Override
    public int colorSize() {
        return RGB_SIZE;
    }

    @Override
    public int depthWidth() {
        return WIDTH;
    }

    @Override
    public int depthHeight() {
        return HEIGHT;
    }

    @Override
    public int depthSize() {
        return SIZE;
    }

}
