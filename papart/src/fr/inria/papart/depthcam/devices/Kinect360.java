/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.depthcam.devices;

/**
 *
 * @author jiii
 */
public class Kinect360 extends KinectDevice {

    public static final int KINECT_MM = 1;
    public static final int KINECT_10BIT = 0;

    public Kinect360() {
        WIDTH = 640;
        HEIGHT = 480;
        SIZE = WIDTH * HEIGHT;
        RGB_WIDTH = 640;
        RGB_HEIGHT = 480;
        RGB_SIZE = WIDTH * HEIGHT;
    }

}
