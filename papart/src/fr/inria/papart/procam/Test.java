/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.procam;


import org.bytedeco.javacv.*;

public class Test {
    public static void main(String[] args) throws Exception {
        int x = 0, y = 0, w = 1024, h = 768; // specify the region of screen to grab
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(":0.0+" + x + "," + y);
        grabber.setFormat("x11grab");
        grabber.setImageWidth(w);
        grabber.setImageHeight(h);
        grabber.start();
        
        CanvasFrame frame = new CanvasFrame("Screen Capture");
        while (frame.isVisible()) {
            frame.showImage(grabber.grab());
        }
        frame.dispose();
        grabber.stop();
    }
}