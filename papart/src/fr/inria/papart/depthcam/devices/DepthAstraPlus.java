/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2016 Jérémy Laviole
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
package fr.inria.papart.depthcam.devices;

import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.CameraFactory;
import fr.inria.papart.procam.camera.CameraOpenCV;
import fr.inria.papart.procam.camera.CameraOpenCVDepth;
import fr.inria.papart.procam.camera.CameraOpenNI2;
import fr.inria.papart.procam.camera.CameraRGBIRDepth;
import fr.inria.papart.procam.camera.CannotCreateCameraException;
import processing.core.PApplet;

/**
 *
 * @author Jeremy Laviole
 */
public final class DepthAstraPlus extends DepthCameraDevice {

    // private final CameraOpenCVDepthAstra cameraNI;

    public DepthAstraPlus(PApplet parent, Camera anotherCam) throws CannotCreateCameraException {
        super(parent);

        // Pass the main color camera. 

        this.anotherCamera = anotherCam;

        // TODO: Adjust all of this

         initDefaultCamera();
         String id = Papart.getDefaultDepthCameraConfiguration(parent).getCameraName();

         camera = (CameraRGBIRDepth) CameraFactory.createCamera(type(), id);
         camera.setParent(parent);
         camera.setUseDepth(true);
         camera.setUseColor(true);

         ((CameraOpenCVDepth) camera).setExternalColorCamera(anotherCam);

         // camera.actAsColorCamera();


        // if (anotherCam instanceof CameraOpenCV) {
         //   this.camera = (CameraOpenNI2) anotherCam;
         //   this.camera.setUseDepth(true);
        //} else {
           // initDefaultCamera();
           // this.anotherCamera = anotherCam;
        //}

       // if (this.anotherCamera == null) {
       //     this.anotherCamera = getColorCamera();
       // }

        // cameraNI = (CameraOpenNI2) camera;

        camera.getDepthCamera().setCalibration(Papart.AstraSDepthCalib);
        // camera.getColorCamera().setCalibration(Papart.AstraSRGBCalib);
        
        //setStereoCalibration(Papart.AstraSStereoCalib);

    }

    @Override
    public CameraRGBIRDepth getMainCamera() {
        return this.camera;
    }

    @Override
    public int rawDepthSize() {
        return getDepthCamera().width() * getDepthCamera().height() * 2;
    }

    @Override
    public Camera.Type type() {
        return Camera.Type.OPENCV_DEPTH;
    }

    @Override
    public void loadDataFromDevice() {
//        setStereoCalibration(cameraNI.getHardwareExtrinsics());
    }

    @Override
    public DepthAnalysis.DepthComputation createDepthComputation() {
        return new OpenCVDepth();
    }
}
