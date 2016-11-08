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

import fr.inria.papart.multitouch.KinectTouchInput;
import fr.inria.papart.procam.Utils;
import org.bytedeco.javacpp.freenect;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraOpenKinectDepth extends Camera implements WithTouchInput{

    private CameraOpenKinect parent;
    private int depthFormat = freenect.FREENECT_DEPTH_MM;
    // other possibility freenect.FREENECT_DEPTH_10_BIT -> Obselete;

    private IplImage depthImage;
    private KinectTouchInput touchInput;
    private PImage camImageDepth = null;

    protected CameraOpenKinectDepth(CameraOpenKinect parent) {
        this.parent = parent;
        this.setPixelFormat(PixelFormat.DEPTH_KINECT_MM);
    }

    // Nothing, this is virtual...
    @Override
    public void start() {
        parent.grabber.setDepthFormat(depthFormat);
    }

    @Override
    public void grab() {
        try {
            IplImage img = parent.grabber.grabDepth();

            this.currentImage = img;
            if (touchInput != null) {
                touchInput.lock();
                touchInput.update();
                touchInput.getTouch2DColors(parent.currentImage);
                touchInput.unlock();
            } else {
                if (touchInput != null) {
                    System.err.println("Error, the TouchInput is set, but no DepthImg is grabbed.");
                }
            }
        } catch (Exception e) {
            System.err.println("Camera: Kinect Grab depth Error ! " + e);
            e.printStackTrace();
        }

    }

    @Override
    public PImage getPImage() {
        if (camImageDepth == null) {
            camImageDepth = parent.parent.createImage(width, height, PApplet.ALPHA);
        }

        if (depthImage != null) {
            Utils.IplImageToPImageKinect(depthImage, false, camImageDepth);
        }
        return camImageDepth;
    }

    @Override
    public void close() {
        setClosing();
    }

    @Override
    public KinectTouchInput getTouchInput() {
        return touchInput;
    }
    
    @Override
    public void setTouchInput(KinectTouchInput touchInput) {
        this.touchInput = touchInput;
    }

    public int getDepthFormat() {
        return depthFormat;
    }

    public void setDepthFormat(int depthFormat) {
        this.depthFormat = depthFormat;
    }

    @Override
    public void newTouchImage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void newTouchImageWithColor(IplImage image) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
