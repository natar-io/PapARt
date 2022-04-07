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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.*;
import org.bytedeco.libfreenect.global.freenect;
import org.bytedeco.opencv.opencv_core.IplImage;

import static java.lang.Math.*;
import static org.bytedeco.libfreenect.global.freenect.*;

import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.OpenKinectFrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
/**
*
* @author Jeremy Laviole
*/
public class CameraOpenCVDepth extends CameraRGBIRDepth {

  private OpenCVFrameGrabber grabber;
  private final OpenCVFrameConverter.ToIplImage converter;

      /////////////////////////////
      ///////// Depth camera 
      ///////////////////////

   protected CameraOpenCVDepth(int cameraNo) {
       this.systemNumber = cameraNo;
       grabber = new OpenCVFrameGrabber(this.systemNumber);
       converter = new OpenCVFrameConverter.ToIplImage();
   }

   @Override
   public void internalStart() throws FrameGrabber.Exception {
       grabber.start();
   }

   public void setExternalColorCamera(Camera camera){
     this.colorCamera = camera;
   }

   @Override
   public void close() {
       setClosing();
       if (grabber != null) {
           try {
               System.out.println("Stopping CameraOpenCVDepth cam.");
               this.stopThread();
               grabber.stop();
               depthCamera.close();
           } catch (Exception e) {
           }
       }
   }

   @Override
   public void grabIR() {
     System.out.println("Not implemented for OpencvDepth camera");
      //  try {
      //      IRCamera.updateCurrentImage(grabber.grabIR());
      //  } catch (FrameGrabber.Exception ex) {
      //      Logger.getLogger(CameraOpenKinect.class.getName()).log(Level.SEVERE, null, ex);
      //  }
   }

   @Override
   public void grabDepth() {

    System.out.println("In grab Depth");
       try {
           // depthCamera.currentImage = grabber.grab() // .grabDepth();
           
           IplImage img = converter.convertToIplImage(grabber.grab());

           // TODO: Depth image comes HERE 
           if (img != null) {
               depthCamera.currentImage = img;
              //  this.updateCurrentImage(img);
           }

           ((WithTouchInput) depthCamera).newTouchImageWithColor(colorCamera.currentImage);
       } catch (FrameGrabber.Exception ex) {
           Logger.getLogger(CameraOpenKinect.class.getName()).log(Level.SEVERE, null, ex);
       }
   }

   @Override
   public void grabColor() {
    
    System.out.println("GrabColor in OpenCVDepth");
          // The other camera 
    colorCamera.updateCurrentImage(this.colorCamera.getIplImage());
          //  colorCamera.updateCurrentImage(grabber.grabVideo());
     
   }

   @Override
   public void setUseDepth(boolean use) {
       if (use) {
           // depthCamera.setPixelFormat(PixelFormat.DEPTH_KINECT_MM);
           depthCamera.type = SubCamera.Type.DEPTH;
           depthCamera.setSize(640, 480);
           // grabber.setDepthFormat(freenect.FREENECT_DEPTH_MM);
       }
       this.useDepth = use;
   }

   @Override
   public void setUseIR(boolean use) {
       if (use) {
         // Not supported yet 
         //  IRCamera.setPixelFormat(PixelFormat.GRAY);
         //  IRCamera.type = SubCamera.Type.IR;
         //  IRCamera.setSize(640, 480);
           // grabber.setvideoformat ?
       }
       this.useIR = use;
   }

   @Override
   public void setUseColor(boolean use) {
       if (use) {
        // The other camera handles the format 
       }
       this.useColor = use;
   }

  
   @Override
   protected void internalGrab() throws Exception {
   }

}
