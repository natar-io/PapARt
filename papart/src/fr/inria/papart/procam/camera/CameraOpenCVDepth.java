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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.*;
import org.bytedeco.libfreenect.global.freenect;
import org.bytedeco.opencv.opencv_core.IplImage;

import static java.lang.Math.*;
import static org.bytedeco.libfreenect.global.freenect.*;

import org.bytedeco.javacv.Frame;
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
  //private final OpenCVFrameConverter.ToIplImage converter;

      /////////////////////////////
      ///////// Depth camera 
      ///////////////////////

   protected CameraOpenCVDepth(int cameraNo) {
      
      // colorCamera = new SubCamera(this, SubCamera.Type.COLOR);
    // IRCamera = new SubCamera(this, SubCamera.Type.IR);

      this.systemNumber = cameraNo;
      grabber = new OpenCVFrameGrabber(this.systemNumber);

      depthCamera = new SubDepthCamera(this, SubCamera.Type.DEPTH);
      
      //converter = new OpenCVFrameConverter.ToIplImage();
   }

   @Override
   public void internalStart() throws FrameGrabber.Exception {

      this.isConnected = true;
      grabber.setImageWidth(640);
      grabber.setImageHeight(480);

      //if (this.captureFormat != null && this.captureFormat.length() > 0) {
      //    grabber.setFormat(this.captureFormat);
      //}
      System.out.println("InternalStart CameraOpencVDepth " + grabber);
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


   public IplImage convertToIplImage(Frame frame) {
     // IplImage img = null;
     // CV_16UC1, 


     // "public Mat(short[] s, boolean signed) { this(s.length, 1, signed ? CV_16SC1 : CV_16UC1); new ShortPointer(data()).put(s); }\n"
     // Mat depthmat = new Mat(640, 480, opencv_core.CV_16UC1);

     // IPL_DEPTH_16U == Frame.DEPTH_USHORT
     IplImage out = IplImage.create(frame.imageWidth, frame.imageHeight, Frame.DEPTH_USHORT, 1,
                                    new Pointer(frame.image[0].position(0)));

    
    ShortBuffer buff = (ShortBuffer) out.createBuffer();
    // ByteBuffer buff = out.getByteBuffer();
  
     System.out.println("Buffer data: " +  buff.get(0) + " " +  buff.get(1) +  " " +  buff.get(2) +"  " + buff.get(3));
     int depth = (int) (buff.get(300  + 480* 200) & 0xFF);
     int depth2 = (int) (buff.get(300  + 480* 200) & 0xFFFF);
     int depth3 = (int) (buff.get(300  + 480* 200) & 0xFFFFFF);
     System.out.println("d " + depth + " d2" + depth2 + " d3 " + depth3);
    
     return  out; 

      // if (frame == null || frame.image == null) {
      //     return null;
      // } else if (frame.opaque instanceof IplImage) {
      //     System.out.println("Image opaque (returned): " + frame.opaque);
      //     return (IplImage)frame.opaque;
      // }
      // //  else
      
      // // if (!OpenCVFrameConverter.ToIplImage.isEqual(frame, img)) {
      // //     int depth = OpenCVFrameConverter.ToIplImage.getIplImageDepth(frame.imageDepth);
      // //     if (img != null) {
      // //         img.releaseReference();
      // //     }
      // //     img = depth < 0 ? null : (IplImage)IplImage.create(frame.imageWidth, frame.imageHeight, depth, frame.imageChannels, new Pointer(frame.image[0].position(0)))
      // //             .widthStep(frame.imageStride * Math.abs(frame.imageDepth) / 8)
      // //             .imageSize(frame.image[0].capacity() * Math.abs(frame.imageDepth) / 8).retainReference();
      // // }
      // System.out.println("Return : " + img);
      // return img;
  }

   @Override
   public void grabDepth() {

       try {
           // depthCamera.currentImage = grabber.grab() // .grabDepth();
           
           System.out.println("CameraOpencVDepth  grab() " + grabber);
           IplImage img = convertToIplImage(grabber.grab());

           System.out.println("Image 1: " + img);

           // TODO: Depth image comes HERE 
           if (img != null) {
               // depthCamera.updateCurrentImage(img);
               depthCamera.currentImage = img;
               // System.out.println("Image: " + img);
               //  this.updateCurrentImage(img);
           }

           // TODO: pass the color image here for color in touch.
           // ((WithTouchInput) depthCamera).newTouchImageWithColor(colorCamera.currentImage);
           ((WithTouchInput) depthCamera).newTouchImage();

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
