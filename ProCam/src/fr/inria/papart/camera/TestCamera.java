/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.camera;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import processing.core.PApplet;

/**
 *
 * @author jeremy
 */
public class TestCamera extends PApplet {

    DepthCamera depthCamera;

    @Override
    public void setup() {

        // window size
        this.size(640, 480, PApplet.P2D);

        // load the Asus Xtion
        //  depthCamera = new DepthCamera(OpenCVDepthFrameGrabber.DepthCameraType.XTION);
        depthCamera = new DepthCamera(0);

        depthCamera.setCopyToProcessing(true);

        // draw is called 30 times / seconds
        frameRate(30);
    }

    @Override
    public void draw() {
        background(0);
         depthCamera.grab();
         depthCamera.grabAsArray();
//         image(depthCamera.getPImage(), 0, 0, 640, 480);
    }

    static public void main(String args[]) {
        PApplet.main(new String[]{"fr.inria.papart.camera.TestCamera"});
    }
}
