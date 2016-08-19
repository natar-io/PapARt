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

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.RealSense;
import org.bytedeco.javacpp.RealSense.context;
import org.bytedeco.javacpp.RealSense.device;
import org.bytedeco.javacpp.indexer.UShortBufferIndexer;
import org.bytedeco.javacpp.indexer.UShortIndexer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraRealSense extends Camera {

        public static void main(String[] args) {

        context context = new context();

        System.out.println("Devices found: " + context.get_device_count());

        device device = context.get_device(0);

        System.out.println("Using device 0, an " + device.get_name());
        System.out.println(" Serial number: " + device.get_serial());
        
        device.enable_stream(RealSense.depth, 640, 480, RealSense.z16, 60);
        device.enable_stream(RealSense.color, 640, 480, RealSense.rgb8, 60);
        
        device.start();
        
        while(true){
            device.wait_for_frames();
            
            System.out.println("Depth scale " + device.get_depth_scale());
//            glPixelTransferf(GL_RED_SCALE, 0xFFFF * dev->get_depth_scale() / 2.0f);

            float scale = device.get_depth_scale();
            Pointer data = (Pointer) device.get_frame_data(RealSense.depth);
            ShortBuffer bb = data.position(0).limit(640 * 480 * 2).asByteBuffer().asShortBuffer();
            
            System.out.println("Capacity :" + bb.capacity());
//            UShortIndexer indexer = new UShortBufferIndexer(_frame_data.asByteBuffer().asShortBuffer());
            
            for(int i = 0 ; i < bb.capacity(); i++){
                float value = bb.get(i);
                if(value != 0){
                    value = value * scale;
                    System.out.print(value + " ");
                }
            }
//System.out.println("Data : "+ bb.get(100));
        }
        
        }
//        try{
//            
//        }catch(Exception e){
//            System.out.println("Exception: " + e);
//            e.printStackTrace();
//        }
//    // Configure all streams to run at VGA resolution at 60 frames per second
//    dev->enable_stream(rs::stream::depth, 640, 480, rs::format::z16, 60);
//    dev->enable_stream(rs::stream::color, 640, 480, rs::format::rgb8, 60);
//    dev->enable_stream(rs::stream::infrared, 640, 480, rs::format::y8, 60);
//    try { dev->enable_stream(rs::stream::infrared2, 640, 480, rs::format::y8, 60); }
//    catch(...) { printf("Device does not provide infrared2 stream.\n"); }
//    dev->start();
//
//    // Open a GLFW window to display our output
//    glfwInit();
//    GLFWwindow * win = glfwCreateWindow(1280, 960, "librealsense tutorial #2", nullptr, nullptr);
//    glfwMakeContextCurrent(win);
//    while(!glfwWindowShouldClose(win))
//    {
//        // Wait for new frame data
//        glfwPollEvents();
//        dev->wait_for_frames();
//
//        glClear(GL_COLOR_BUFFER_BIT);
//        glPixelZoom(1, -1);
//
//        // Display depth data by linearly mapping depth between 0 and 2 meters to the red channel
//        glRasterPos2f(-1, 1);
//        glPixelTransferf(GL_RED_SCALE, 0xFFFF * dev->get_depth_scale() / 2.0f);
//        glDrawPixels(640, 480, GL_RED, GL_UNSIGNED_SHORT, dev->get_frame_data(rs::stream::depth));
//        glPixelTransferf(GL_RED_SCALE, 1.0f);
//
//        // Display color image as RGB triples
//        glRasterPos2f(0, 1);
//        glDrawPixels(640, 480, GL_RGB, GL_UNSIGNED_BYTE, dev->get_frame_data(rs::stream::color));
//
//        // Display infrared image by mapping IR intensity to visible luminance
//        glRasterPos2f(-1, 0);
//        glDrawPixels(640, 480, GL_LUMINANCE, GL_UNSIGNED_BYTE, dev->get_frame_data(rs::stream::infrared));
//
//        // Display second infrared image by mapping IR intensity to visible luminance
//        if(dev->is_stream_enabled(rs::stream::infrared2))
//        {
//            glRasterPos2f(0, 0);
//            glDrawPixels(640, 480, GL_LUMINANCE, GL_UNSIGNED_BYTE, dev->get_frame_data(rs::stream::infrared2));
//        }
    
    
    private OpenCVFrameGrabber grabber;
    private final OpenCVFrameConverter.ToIplImage converter;

    protected CameraRealSense(int cameraNo) {
//        this.systemNumber = cameraNo;
//        this.setPixelFormat(PixelFormat.BGR);
//        converter = new OpenCVFrameConverter.ToIplImage();
    }

    @Override
    public void start() {
//        OpenCVFrameGrabber grabberCV = new OpenCVFrameGrabber(this.systemNumber);
//        grabberCV.setImageWidth(width());
//        grabberCV.setImageHeight(height());
////        grabberCV.setFrameRate(60);
//        grabberCV.setImageMode(FrameGrabber.ImageMode.COLOR);
// 
//        try {
//            grabberCV.start();
//            this.grabber = grabberCV;
//            this.isConnected = true;
//        } catch (Exception e) {
//            System.err.println("Could not start frameGrabber... " + e);
//
//            System.err.println("Could not camera start frameGrabber... " + e);
//            System.err.println("Camera ID " + this.systemNumber + " could not start.");
//            System.err.println("Check cable connection, ID and resolution asked.");
//
//            this.grabber = null;
//        }
    }

    @Override
    public void grab() {

//        if (this.isClosing()) {
//            return;
//        }
//        try {
//            IplImage img = converter.convertToIplImage(grabber.grab());
//            if (img != null) {
//                this.updateCurrentImage(img);
//            }
//        } catch (Exception e) {
//            System.err.println("Camera: OpenCV Grab() Error ! " + e);
//        }
    }

    @Override
    public PImage getPImage() {

//        if (currentImage != null) {
//            this.checkCamImage();
//            camImage.update(currentImage);
//            return camImage;
//        }
//        // TODO: exceptions !!!
        return null;
    }

    @Override
    public void close() {
        this.setClosing();
//        if (grabber != null) {
//            try {
//                grabber.stop();
//                System.out.println("Stopping grabber (OpencV)");
//               
//            } catch (Exception e) {
//                System.out.println("Impossible to close " + e);
//            }
//        }
    }

}
