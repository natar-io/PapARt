/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.camera;

import com.googlecode.javacv.CameraDevice;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core;
import java.io.File;
import java.nio.ByteBuffer;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author jeremy
 */
public class CalibratedCamera extends OpenCVCamera {

    protected CameraDevice cam;
    protected opencv_core.IplImage img2 = null;
    
    public CalibratedCamera(int device, String yamlCameraProj) {

        // Init the camera parameters
        try {
            CameraDevice[] c = CameraDevice.read(yamlCameraProj);
            if (c.length > 0) {
                cam = c[0];
            } else {
                throw new Exception("Invalid calibration, no camera found");
            }

            grabber = new OpenCVFrameGrabber(device);

            grabber.setImageMode(FrameGrabber.ImageMode.RAW);
            grabber.setDeinterlace(true);
//                grabber.setTriggerMode(false);
//                grabber.setNumBuffers(6);
//                grabber.setTriggerMode(true);
//                grabber.flush();

            // creation of a PImage ?
            pimg = new PImage(cam.imageWidth, cam.imageHeight, PApplet.RGB);

            grabber.start();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

// default, always undistort the camera input    
    public void grab() {
        try {
//            grabber.trigger();
            iimg = grabber.grab();

            if (img2 == null) {
                img2 = iimg.clone();
            }
            cam.undistort(iimg, img2);

            // Image drawing
            if (copyToProcessing) {
                ByteBuffer buff1 = img2.getByteBuffer();
                pimg.loadPixels();
                for (int i = 0; i
                        < iimg.width() * img2.height(); i++) {
                    int offset = i * 3;
                    pimg.pixels[i] = (buff1.get(offset + 2) & 0xFF) << 16
                            | (buff1.get(offset + 1) & 0xFF) << 8
                            | (buff1.get(offset) & 0xFF);
                }
                pimg.updatePixels();
            }

        } catch (Exception e) {
            System.out.println("Exception in calibrated image grabbing " + e);
        }
    }
    
        protected void initPimg(){
         pimg = new PImage(cam.imageWidth, cam.imageHeight, PApplet.RGB);
   
    }
    

}
