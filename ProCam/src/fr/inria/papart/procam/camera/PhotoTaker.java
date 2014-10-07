///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package fr.inria.papart.procam.camera;
//
//import fr.inria.papart.procam.Camera;
//import fr.inria.papart.procam.Utils;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.bytedeco.javacpp.opencv_core;
//import processing.core.PImage;
//
///**
// *
// * @author jiii
// */
//public class PhotoTaker extends Thread {
//
//    {
//        /**
//         * *******************************
//         */
//        /// TODO: Check the end of the code 
//        /**
//         * Used only in photoTaker...
//         *
//         * @param pimg
//         * @param undist
//         */
//    public void grabTo(PImage pimg, boolean undist) {
//        try {
//
//            opencv_core.IplImage img = this.grab(undist);
//
//            if (img != null) {
//                Utils.IplImageToPImage(img, false, pimg);
//            }
//
//        } catch (Exception e) {
//            System.err.println("Error while grabbing frame " + e);
//            e.printStackTrace();
//        }
//    }
//
//    protected void imageRetreived() {
//        this.hasNewPhoto = false;
//    }
//    protected boolean photoCapture;
//
//    // TODO: check this code... may be broken.
//    public void setPhotoCapture() {
//
//        this.photoCapture = true;
//        if (useProcessingVideo()) {
//
//            captureIpl.stop();
//        }
//        if (useOpenCV()) {
//            try {
//                grabber.stop();
//            } catch (Exception e) {
//                System.out.println("Error " + e);
//            }
//        }
//
//    }
//    protected boolean isTakingPhoto = false;
//    protected boolean hasNewPhoto = false;
//
//    public void takePhoto(boolean undistort) {
//        takePhoto(null, undistort);
//    }
//
//    public void takePhoto(PImage img, boolean undistort) {
//        System.out.println("Is Takiing ? " + this.isTakingPhoto);
//        if (!this.isTakingPhoto) {
//            this.hasNewPhoto = false;
//            this.isTakingPhoto = true;
//            System.out.println("Is Takiing ? " + this.isTakingPhoto);
//            PhotoTaker pt = new PhotoTaker(this, img, undistort);
//            pt.start();
//            System.out.println("Photo started...");
//        } else {
//            System.out.println("Wait for the previous photo.");
//        }
//    }
//
//    public boolean isTakingPhoto() {
//        return this.isTakingPhoto;
//    }
//
//    public boolean hasNewPhoto() {
//        return this.hasNewPhoto;
//    }
//
//    /////////////// TODO: MOVE TO A CLASS ??? /////////
//    private PImage img;
//    private boolean undist;
//    private Camera cam;
//
//    PhotoTaker(Camera cam, PImage img, boolean undistort) {
//        this.img = img;
//        this.undist = undistort;
//        this.cam = cam;
//    }
//
//    // TODO: Magic numbers... 
//    @Override
//    public void run() {
//
//        System.out.println("Grabbing frames");
//        if (cam.useProcessingVideo()) {
//
//            captureIpl.start();
//
//            // check gotPicture etc... 
//            while (!gotPicture) {
//                try {
//                    Thread.sleep(20);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//
//            // Drop 10 frames
//            for (int i = 0; i < 10; i++) {
//                if (img != null) {
//                    grabTo(img, false);
//                }
//
//                while (!gotPicture) {
//
//                    try {
//                        Thread.sleep(20);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//
//                }
//                grab(false);
//
//                // NO IDEA WHY THIS IS NOT WORKING
//            }
//
//            if (img != null) {
//                grabTo(img, undist);
//            }
////                gsCapture.stop();
//        }
//
//        if (cam.useOpenCV()) {
//            try {
//                grabber.start();
//            } catch (Exception e) {
//                System.err.println("Could not start frameGrabber... " + e);
//            }
//            // Drop 10 frames
//            for (int i = 0; i < 30; i++) {
////                    grabTo(img, undist);
//                grab(false);
//                System.out.println("Grabbed " + i);
//            }
//
//            if (img != null) {
//                grabTo(img, undist);
//            }
//
//            try {
//                grabber.stop();
//            } catch (Exception e) {
//                System.err.println("Could not stop frameGrabber... " + e);
//            }
//        }
//
//        System.out.println("Photo OK ");
//        cam.isTakingPhoto = false;
//        cam.hasNewPhoto = true;
//    }
//}
