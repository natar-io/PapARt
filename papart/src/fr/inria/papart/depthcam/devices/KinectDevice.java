/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.depthcam.devices;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public abstract class KinectDevice implements DepthCameraDevice {

    // IR and Depth image size 
    public static int WIDTH;
    public static int HEIGHT;
    public static int SIZE;

    // RGB image size
    public static int RGB_WIDTH;
    public static int RGB_HEIGHT;
    public static int RGB_SIZE;

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
