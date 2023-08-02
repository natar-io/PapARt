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

import javassist.tools.reflect.CannotCreateException;
import org.bytedeco.opencv.opencv_core.*;
// import org.bytedeco.opencv.IplImage;
//import static org.bytedeco.javacpp.opencv_videoio.CAP_PROP_BUFFERSIZE;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.FrameGrabber.ImageMode;
import org.bytedeco.javacv.FrameGrabber.SampleMode;

import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraOpenCV extends Camera {

  private OpenCVFrameGrabber grabber;
  private final OpenCVFrameConverter.ToIplImage converter;
  private boolean useRawImage = false;
  private int bitsPerPixel = 8;

  protected CameraOpenCV(int cameraNo) {
    this.systemNumber = cameraNo;
    this.setPixelFormat(PixelFormat.BGR);
    converter = new OpenCVFrameConverter.ToIplImage();
  }

  public void setRawImage(boolean value){
    this.useRawImage = value;
  }
  public void setBitsPerPixel(int value){
    this.bitsPerPixel = value;
  }


  @Override
  public void start() {
    OpenCVFrameGrabber grabberCV = new OpenCVFrameGrabber(this.systemNumber);
    grabberCV.setImageWidth(width());
    grabberCV.setImageHeight(height());
    grabberCV.setFrameRate(frameRate);
    
    if(useRawImage){
      grabberCV.setBitsPerPixel(this.bitsPerPixel);
      grabberCV.setSampleMode(SampleMode.RAW);
      grabberCV.setImageMode(ImageMode.RAW);
    }

    if (this.captureFormat != null && this.captureFormat.length() > 0) {
      grabberCV.setFormat(this.captureFormat);
    }
    // grabberCV.setImageMode(FrameGrabber.ImageMode.RAW);

    try {
      grabberCV.start();
      this.grabber = grabberCV;
      this.isConnected = true;
    } catch (Exception e) {
      System.err.println("Could not start frameGrabber... " + e);

      System.err.println("Could not camera start frameGrabber... " + e);
      System.err.println("Camera ID " + this.systemNumber + " could not start.");
      System.err.println("Check cable connection, ID and resolution asked.");
      this.grabber = null;
      parent.die("Cannot start camera.", new CannotCreateCameraException("Cannot load " + this.toString()));
      System.exit(-1);
      // throw new RuntimeException("Cannot start camera.");

    }
  }

  @Override
  public void grab() {

    if (this.isClosing()) {
      return;
    }
    try {

      IplImage img = converter.convertToIplImage(grabber.grab());
      if (img != null) {
        this.updateCurrentImage(img);
      }
    } catch (Exception e) {
      System.err.println("Camera: OpenCV Grab() Error ! " + e);
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
