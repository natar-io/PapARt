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
package fr.inria.papart.procam.camera;

import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.RealSense;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.RealSenseFrameGrabber;
import processing.core.PImage;
import processing.core.PMatrix3D;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraRealSense extends Camera {

    protected RealSenseFrameGrabber grabber;
    protected CameraRealSenseDepth depthCamera;
    private boolean useDepth = true;

    protected CameraRealSense(int cameraNo) {
        try {
            RealSenseFrameGrabber.tryLoad();
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(CameraRealSense.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.systemNumber = cameraNo;
        setPixelFormat(PixelFormat.RGB);
        grabber = new RealSenseFrameGrabber(this.systemNumber);
        depthCamera = new CameraRealSenseDepth(this);
    }

    public CameraRealSenseDepth getDepthCamera() {
        return this.depthCamera;
    }

    public PMatrix3D getHardwareExtrinsics() {
        RealSense.extrinsics extrinsics = grabber.getRealSenseDevice().get_extrinsics(RealSense.color, RealSense.depth);
        FloatBuffer fb = extrinsics.position(0).asByteBuffer().asFloatBuffer();
        return new PMatrix3D(
                fb.get(0),fb.get(3),fb.get(6), -fb.get(9) * 1000f,
                fb.get(1),fb.get(4),fb.get(7), fb.get(10)* 1000f,
                fb.get(2),fb.get(5),fb.get(8), fb.get(11)* 1000f,
                0, 0, 0, 1);
    }
    
    public void useHarwareIntrinsics(){
        RealSense.intrinsics intrinsics = grabber.getRealSenseDevice().get_stream_intrinsics(RealSense.color);
        FloatBuffer fb = intrinsics.position(0).asByteBuffer().asFloatBuffer();
        float cx = fb.get(2);
        float cy = fb.get(3);
        float fx = fb.get(4);
        float fy = fb.get(5);
        this.setSimpleCalibration(fx, fy, cx, cy, this.width, this.height);
    }
    
    public float getDepthScale() {
        return grabber.getDepthScale();

    }

    public void useDepth(boolean useDepth) {
        this.useDepth = useDepth;
    }

    @Override
    public void start() {
        grabber.setImageWidth(width());
        grabber.setImageHeight(height());
        grabber.enableColorStream();

        if (useDepth) {
            depthCamera.start();
        }

        try {
            grabber.start();
            this.isConnected = true;
        } catch (Exception e) {
            System.err.println("Could not start frameGrabber... " + e);
            System.err.println("Could not camera start frameGrabber... " + e);
            System.err.println("Camera ID " + this.systemNumber + " could not start.");
            System.err.println("Check cable connection, ID and resolution asked.");

            this.grabber = null;
        }
    }

    @Override
    public void grab() {

        if (this.isClosing()) {
            return;
        }
        // update the images.
        try {
            grabber.grab();

            // get the color
            IplImage img = grabber.grabVideo();
//            updateCurrentImage(img);

            currentImage = img;

            if (useDepth) {
//                depthCamera.grab();
            }

        } catch (Exception e) {
            System.out.println("Exception :" + e);
            e.printStackTrace();
        }
    }

    public IplImage getDepthImage() {
        return depthCamera.getIplImage();
    }

    public PImage getDepthPImage() {
        return depthCamera.getPImage();
    }

//    public void grab() {
//        System.out.println("Wait for frames...");
//        device.wait_for_frames();
//        System.out.println("frames ok");
//        testImage.loadPixels();
//        int[] pixels = testImage.pixels;
//
////        float scale = device.get_depth_scale();
////        Pointer data = (Pointer) device.get_frame_data(RealSense.depth);
////        ShortBuffer bb = data.position(0).limit(640 * 480 * 2).asByteBuffer().asShortBuffer();
////        System.out.println("Sizes: " + pixels.length + " " + bb.limit());
//
////              for (int i = 0; i < bb.capacity(); i++) {
////            float value = bb.get(i);
////            pixels[i] = (int) (value * scale * 1000f);
////        }
//              
//        Pointer data_color = (Pointer) device.get_frame_data(RealSense.color);
//        ByteBuffer bb_color = data_color.position(0).limit(640 * 480 * 3).asByteBuffer();
//        
//        System.out.println("Sizes " + bb_color.capacity() + " " + pixels.length * 3);
//                
//        for (int i = 0; i < bb_color.capacity() / 3; i++) {
//            pixels[i] =  (bb_color.get(i*3 + 0) & 0xFF) >> 0 |
//                         (bb_color.get(i*3 + 1) & 0xFF) << 8 |
//                         (bb_color.get(i*3 + 2) & 0xFF) << 16;
//        }
//        testImage.updatePixels();
//    }
    @Override
    public PImage getPImage() {
        this.checkCamImage();
        if (currentImage != null) {
            camImage.update(currentImage);
            return camImage;
        }
        // TODO: exceptions !!!
        return null;
    }

    @Override
    public void close() {
        this.setClosing();
        if (grabber != null) {
            try {
                grabber.stop();
                System.out.println("Stopping grabber (RealSense)");

            } catch (Exception e) {
                System.out.println("Impossible to close " + e);
            }
        }
    }

}
