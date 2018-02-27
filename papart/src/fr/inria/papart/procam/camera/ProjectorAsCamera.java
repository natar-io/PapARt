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

import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.utils.ARToolkitPlusUtils;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.utils.ImageUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import org.bytedeco.javacpp.opencv_core.IplImage;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class ProjectorAsCamera extends Camera {

    private final TrackedView projectorView;
    private final Camera cameraTracking;
    private IplImage grayImage;
    private final ProjectorDisplay projector;
    private PImage pimage;
    
    public ProjectorAsCamera(ProjectorDisplay projector, Camera cameraTracking, TrackedView view) {
        this.projectorView = view;
        this.cameraTracking = cameraTracking;
        this.projector = projector;
    }

    public void setImage(IplImage image) {
        this.currentImage = image;
    }

    @Override
    public void setCalibration(String fileName) {
        try {
            this.calibrationFile = fileName;
            pdp = ProjectiveDeviceP.loadProjectorDevice(parent, fileName);
            camIntrinsicsP3D = pdp.getIntrinsics();
            this.width = pdp.getWidth();
            this.height = pdp.getHeight();
            this.undistort = pdp.handleDistorsions();
        } catch (Exception e) {
            e.printStackTrace();

            System.err.println("Camera: error reading the calibration " + pdp
                    + "file" + fileName + " \n" + e);
        }

//        System.out.println("Calibration : ");
//        camIntrinsicsP3D.print();
    }

    static public void convertARProjParams(PApplet parent, String calibrationFile,
            String calibrationARtoolkit) {
        try {
            ARToolkitPlusUtils.convertProjParam(parent, calibrationFile, calibrationARtoolkit);
        } catch (Exception ex) {
            System.out.println("Error converting projector to ARToolkit "
                    + calibrationFile + " " + calibrationARtoolkit
                    + ex);
        }
    }

    @Override
    public void start() {
        return;
    }

    /**
     * Not implemented.
     *
     * @return
     */
    @Override
    public PImage getPImage() {
        
        if(currentImage != null){
            if(pimage == null){
                pimage = parent.createImage(currentImage.width(), currentImage.height(), RGB);
            }
            ImageUtils.IplImageToPImage(currentImage, true, pimage);
            return pimage;
        }
        return null;
    }

    /**
     * Grab an image from the camera associated. 
     * Not used.
     */
    @Override
    public void grab() {
        IplImage img = projectorView.getIplViewOf(cameraTracking);
        if (img != null) {
            currentImage = img;
            if (this.format == PixelFormat.GRAY) {
                currentImage = greyProjectorImage(img);
            }
        }
    }

    /**
     * Grab a fake (given image). 
     * @param source given image.
     */
    public void grab(IplImage source) {
        IplImage img = projectorView.getIplViewOf(cameraTracking, source);
        if (img != null) {
            currentImage = img;
            if (this.format == PixelFormat.GRAY) {
                currentImage = greyProjectorImage(img);
            }
        }
    }

    private opencv_core.IplImage greyProjectorImage(opencv_core.IplImage projImage) {

        if (projImage.nChannels() == 1) {
            grayImage = projImage;
            return grayImage;
        }

        if (grayImage == null) {
            grayImage = opencv_core.IplImage.create(projector.getWidth(),
                    projector.getHeight(),
                    IPL_DEPTH_8U, 1);
        }

        cvCvtColor(projImage, grayImage, CV_BGR2GRAY);
        // if(test){
        //     cvSaveImage( sketchPath() + "/data/projImage.jpg", grayImage);
        //     cvSaveImage( sketchPath() + "/data/camImage.jpg", camera.getIplImage());
        // }
        return grayImage;
    }

    /**
     * Not used.
     */
    @Override
    public void close() {
    }

}
