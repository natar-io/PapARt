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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_GRAY16BE;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraFFMPEG extends Camera {

    private FFmpegFrameGrabber grabber;
    private final OpenCVFrameConverter.ToIplImage converter;
    private String imageFormat;
    
    protected CameraFFMPEG(String description, String imFormat) {
        this.cameraDescription = description;
        this.setPixelFormat(Camera.PixelFormat.BGR);
        this.imageFormat = imFormat;
        converter = new OpenCVFrameConverter.ToIplImage();
    }

    @Override
    public void start() {
        FFmpegFrameGrabber grabberFF = new FFmpegFrameGrabber(this.cameraDescription);
        grabberFF.setImageWidth(width());
        grabberFF.setImageHeight(height());
//        grabberCV.setFrameRate(60);
        
//        grabberFF.setImageMode(FrameGrabber.ImageMode.COLOR);
        grabberFF.setImageMode(FrameGrabber.ImageMode.RAW);
        
        this.setPixelFormat(PixelFormat.RGB);
        
        grabberFF.setFormat(this.imageFormat);
        
        try {
            grabberFF.start();
            this.grabber = grabberFF;
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
        try {
            
            if(parent.millis() > 5000){
            Frame frame = grabber.grab();
            Buffer b = frame.image[0];
//            byte[] arr = ((ByteBuffer) b).array();
//            System.out.println("Data: " + arr[0]);
//            System.out.println("Data: " + arr[1]);
            }
//            IplImage img = converter.convertToIplImageDepth(grabber.grab());
//
//            
//            if (img != null) {
//            System.out.println(img.asByteBuffer().get(100));
//            System.out.println(img.asByteBuffer().get(101));
//            
//                System.out.println("ipl depth: " + img.depth());
//            System.out.println("ipl channels: " + img.nChannels());
//                
//                this.updateCurrentImage(img);
//            }
        } catch (Exception e) {
            System.err.println("Camera: OpenCV Grab() Error ! " + e);
            e.printStackTrace();
        }
    }

    @Override
    public PImage getPImage() {

        if (currentImage != null) {
            this.checkCamImage();
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
                System.out.println("Stopping grabber (OpencV)");

            } catch (Exception e) {
                System.out.println("Impossible to close " + e);
            }
        }
    }

}
