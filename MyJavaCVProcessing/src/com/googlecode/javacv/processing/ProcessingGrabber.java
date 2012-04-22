/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.javacv.processing;

import com.googlecode.javacv.DC1394FrameGrabber;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameGrabber.ImageMode;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author jeremy
 */
public class ProcessingGrabber extends Thread {

    private PImage pimg;
    private IplImage iimg;
    private BufferedImage bimg;
    private PApplet applet;
//    private MultiTracker tracker;
//    private Logger logger;
    private FrameGrabber grabber;
    private int framerate;
    private float frameskip;
    private int imgType;

    public ProcessingGrabber(int w, int h, PApplet applet, int imgType) {
        this(w, h, applet, imgType, 0, 30);
    }

    public ProcessingGrabber(int w, int h, PApplet applet, int imgType, int device, int framerate) {
        this.applet = applet;
        bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_BGR);
        pimg = new PImage(w, h, PApplet.RGB);

//        if (imgType == 0) {
//            try {
//                //    grabber = new DC1394FrameGrabber(0);
//                grabber = new DC1394FrameGrabber(0);
////                grabber.setColorMode(ColorMode.BGR);
//
//            } catch (Exception e) {
//                PApplet.println("Impossible to set the dc1394 device " + e);
//            }
//        } else {
//            grabber = new OpenCVFrameGrabber(device);
//            grabber.setColorMode(FrameGrabber.ColorMode.RAW);
//        }

        grabber = new OpenCVFrameGrabber(device);

//        try {
//            grabber = new DC1394FrameGrabber(0);
//        } catch (Exception ex) {
//            Logger.getLogger(ProcessingGrabber.class.getName()).log(Level.SEVERE, null, ex);
//        }

        grabber.setImageWidth(w);
        grabber.setImageHeight(h);
        grabber.setFrameRate(framerate);
        grabber.setImageMode(ImageMode.RAW);
        grabber.setDeinterlace(true);
        this.framerate = framerate;
        this.imgType = imgType;

        this.frameskip = 1f / framerate * 1000;
        try {
            grabber.start();
        } catch (Exception e) {
            PApplet.println(e + " \n exception in grabber start");
        }
        this.start();
    }

    public void findARMarker(IplImage img) {
//        ARMarkerInfo info = new ARMarkerInfo();
//        int[] markerNum = new int[2];
//        int nbFound = tracker.calc(img.getByteBuffer());
//         tracker.arDetectMarker(img.getByteBuffer(), 110, info, markerNum);
        // System.out.println(markerNum + " info " + info);
    }

    public void IplImageToPImage(int type) {

// naive and working not well
        if (type == 0) {
//            ShortBuffer buff = img.getShortBuffer();
//            //  PImage ret = new PImage(img.w(), img.h(), PApplet.RGB);
//            pimg.loadPixels();
//            System.arraycopy(buff.array(), 0, pimg.pixels, 0, pimg.w * pimg.h);
//            pimg.updatePixels();
        }

        if (type == 1) {
            iimg.copyTo(bimg);
            pimg = new PImage(bimg);



        }

// naive and working well
        if (type == 2) {
            ByteBuffer buff = iimg.getByteBuffer();
            //  PImage ret = new PImage(img.w(), img.h(), PApplet.RGB);
            pimg.loadPixels();


            for (int i = 0; i
                    < iimg.width() * iimg.height(); i++) {
                int offset = i * 3;
//            ret.pixels[i] = applet.color(buff.get(offset + 0) & 0xff, buff.get(offset + 1) & 0xFF, buff.get(offset + 2) & 0xff);

                pimg.pixels[i] = (buff.get(offset + 2) & 0xFF) << 16
                        | (buff.get(offset + 1) & 0xFF) << 8
                        | (buff.get(offset) & 0xFF);


            }
            pimg.updatePixels();


        }


        if (type == 3) {
//            ShortBuffer buff = iimg.getShortBuffer();
            ByteBuffer buff = iimg.getByteBuffer();

            //  PImage ret = new PImage(img.w(), img.h(), PApplet.RGB);
            pimg.loadPixels();



            for (int x = 0; x
                    < iimg.width(); x++) {
                for (int y = 0; y
                        < iimg.height(); y++) {
//            for (int i = 0; i < iimg.width() * iimg.height(); i++) {
                    int offset = y + x * iimg.height();

                    pimg.pixels[offset] = buff.get(offset) & 0xFF;

//            ret.pixels[i] = applet.color(buff.get(offset + 0) & 0xff, buff.get(offset + 1) & 0xFF, buff.get(offset + 2) & 0xff);
//
//                pimg.pixels[i] = (buff.get(offset + 2) & 0xFF) << 16
//                        | (buff.get(offset + 1) & 0xFF) << 8
//                        | (buff.get(offset) & 0xFF);


                }
            }
            pimg.updatePixels();


        }
    }
    private boolean running = true;

    public void end() {
        running = false;


    }

    public PImage frame() {
        return pimg;


    }

    @Override
    public void run() {

        while (running) {
//            applet.println("Running ?");
            try {
                iimg = grabber.grab();
                IplImageToPImage(
                        imgType);



            } catch (Exception e) {
                PApplet.println(e);

            }

//            try {
//                Thread.sleep(2);
//            } catch (InterruptedException e) {
//                // e.printStackTrace();
//            }
        }
//        try {
//        // call the method with this object as the argument!
//        frameEventMethod.invoke(p5parent, new Object[] { this });
//
//        } catch (Exception e) {
//        err("Could not invoke the \"frameEvent()\" method for some reason.");
//        e.printStackTrace();
//        frameEventMethod = null;
//        }
    }
}
