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
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.indexer.ShortIndexer;
import org.bytedeco.libfreenect.global.freenect;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.bytedeco.opencv.opencv_core.Mat;

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
      // System.out.println("LOADing Grabber CV DEPTH 6, param:  " + cameraNo);
      this.systemNumber = cameraNo;
      System.out.println("Passed NO in Depth: " + cameraNo);
      grabber = new OpenCVFrameGrabber(cameraNo);
      depthCamera = new SubDepthCamera(this, SubCamera.Type.DEPTH);
      //converter = new OpenCVFrameConverter.ToIplImage();
   }

   @Override
   public void internalStart() throws FrameGrabber.Exception {

      this.isConnected = true;
      //grabber.setImageWidth(640);
      //grabber.setImageHeight(480);

      //if (this.captureFormat != null && this.captureFormat.length() > 0) {
      //    grabber.setFormat(this.captureFormat);
      //}
      System.out.println("InternalStart CameraOpencVDepth " + grabber + " " + this.systemNumber);
      grabber.start();

      System.out.println("Grabber started ! ");
      // Frame f = grabber.grab();
      // System.out.println("Grabbed: " + f);
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
     int offset = 300 + 480*200;

     frame.imageDepth = Frame.DEPTH_USHORT; 
     frame.imageChannels = 1; 

     // "public Mat(short[] s, boolean signed) { this(s.length, 1, signed ? CV_16SC1 : CV_16UC1); new ShortPointer(data()).put(s); }\n"
     // Mat depthmat = new Mat(640, 480, opencv_core.CV_16UC1);
     System.out.println("image size: " + frame.imageWidth + " " + frame.imageHeight);
     System.out.println("nb images: " + frame.image.length);
     System.out.println("nb Depth, C, Str: " + frame.imageDepth + " " +
                         frame.imageChannels + " " + frame.imageStride);

     // CV_16UC1 2
     Mat rawMat = new Mat(frame.imageHeight, frame.imageWidth, 2, 
                          new Pointer(frame.image[0].position(0)));

     ShortBuffer buff = rawMat.createBuffer(); 
     
     ///ShortBuffer b = rawMat.createBuffer();
     //System.out.println("raw: " + b.get(offset));


    //  IplImage out4 = IplImage.create(frame.imageWidth, frame.imageHeight, 64, 1,
    //  new Pointer(frame.image[0].position(0)));
    //  FloatBuffer buff4 = (FloatBuffer) out4.createBuffer();
    //  System.out.println("Buffer data: " +  buff4.get(offset + 0) + " " +  buff4.get(offset + 1) +  " " +  buff4.get(offset + 2) +"  " + buff4.get(offset + 3));
     

    //  IplImage out3 = IplImage.create(frame.imageWidth, frame.imageHeight, 32, 1,
    //  new Pointer(frame.image[0].position(0)));
    //  FloatBuffer buff3 = (FloatBuffer) out3.createBuffer();

    //  System.out.println("Buffer data: " +  buff3.get(offset + 0) + " " +  buff3.get(offset + 1) +  " " +  buff3.get(offset + 2) +"  " + buff3.get(offset + 3));
    
    //int depthC = (int) (buff3.get(offset) & 0xFF >> 2 & buff3.get(offset+1) & 0xFF );
    //System.out.println("depthA: " + depthA);



     // IPL_DEPTH_16U == Frame.DEPTH_USHORT
    // IplImage out2 = IplImage.create(frame.imageWidth, frame.imageHeight, 8, 2,
    //                                new Pointer(frame.image[0].position(0)));
    // ByteBuffer buff2 = (ByteBuffer) out2.createBuffer();

    // byte[] array = new byte[640*480*2]; 
    //byte[] arr = new byte[buff2.remaining()];
    // System.out.println("READING: " + arr.length);
    // buff2.get(arr);
    // System.out.println("Buffer ARR: " +  arr[offset + 0] + " " +  arr[offset + 1] +  " " +  arr[offset + 2] +"  " + arr[offset + 3]);
    

    // int depthA = (int) (buff2.get(offset) & 0xFF >> 2 & buff2.get(offset+1) & 0xFF );
    // System.out.println("depthA: " + depthA);
    
    // System.out.println("Buffer data: " +  buff2.get(offset + 0) + " " +  buff2.get(offset + 1) +  " " +  buff2.get(offset + 2) +"  " + buff2.get(offset + 3));
    // System.out.println("Buffer data: " +  buff2.get(offset + 4) + " " +  buff2.get(offset + 5) +  " " +  buff2.get(offset + 6) +"  " + buff2.get(offset + 7));
    
    // int depthA1 = (int) (buff2.get(offset) & 0xFF >> 2 & (buff2.get(offset+1) & 0xFF));
    // System.out.println("depthA1: " + depthA1);
        
     // ShortBuffer buff = (ShortBuffer) frame.image[0];

    int a = (int) buff.get(offset) & 0xFFFF; 
    int b = (int) buff.get(offset+1) & 0xFFFF; 
    int c = (int) buff.get(offset+2) & 0xFFFF; 
    int d = (int) buff.get(offset+3) & 0xFFFF; 

    System.out.println("abcd " + a + " " + b + " " + c + " " + d); 

    IplImage out = IplImage.create(frame.imageWidth, frame.imageHeight,
                                   Frame.DEPTH_USHORT, 1, new Pointer(frame.image[0].position(0)));
 // ByteBuffer buff = out.getByteBuffer();

    // ShortBuffer buff = (ShortBuffer) out.createBuffer();


    // System.out.println("Buffer order " + buff.order()+ " remain: " + buff2.remaining());

    // short[] arr = new short[buff2.remaining()];
    // System.out.println("READING: " + arr.length);
    // buff.get(arr, 0, 640*480 *2);
    // System.out.println("Buffer ARR: " +  arr[offset + 0] + " " +  arr[offset + 1] +  " " +  arr[offset + 2] +"  " + arr[offset + 3]);
    

    // System.out.println("Buffer data: " +  buff.get(offset + 0) + " " +  buff.get(offset + 1) +  " " +  buff.get(offset + 2) +"  " + buff.get(offset + 3));
    // int d0 = buff.get(offset);
    // int depth = (int) (buff.get(offset +0) & 0xFFFF);
    // int depth2 = (int) (buff.get(offset +1) & 0xFFFF);
    // int depth3 = (int) (buff.get(offset +2) & 0xFFFF);
    // int depth4 = (int) (buff.get(offset +3) & 0xFFFF);
    // System.out.println("d0: " + d0 + " d: " + depth + " d2: " + depth2 + " d3: " + depth3 + " d4: " + depth4);
  
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
    System.out.println("Grabdepth: 111");
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
           depthCamera.setPixelFormat(PixelFormat.BGR);
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
