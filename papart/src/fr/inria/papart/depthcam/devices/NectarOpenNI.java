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
import processing.core.PApplet;
import tech.lity.rea.nectar.camera.Camera;
import tech.lity.rea.nectar.camera.CameraNectar;
import tech.lity.rea.nectar.camera.CannotCreateCameraException;

/**
 *
 * @author Jeremy Laviole
 */
public final class NectarOpenNI extends DepthCameraDevice {

    private final CameraNectar cameraNectar;

    public NectarOpenNI(PApplet parent, Camera anotherCam) throws CannotCreateCameraException {
        super(parent);

        if (anotherCam instanceof CameraNectar) {
            this.camera = (CameraNectar) anotherCam;
            this.camera.setUseDepth(true);
        } else {
            initDefaultCamera();
            this.anotherCamera = anotherCam;
        }

        if (this.anotherCamera == null) {
            this.anotherCamera = getColorCamera();
        }

        cameraNectar = (CameraNectar) camera;

        camera.getDepthCamera().setCalibration(Papart.AstraSDepthCalib);
        camera.getColorCamera().setCalibration(Papart.AstraSRGBCalib);
        setStereoCalibration(Papart.AstraSStereoCalib);

//        setStereoCalibration(Papart.kinectStereoCalib);
        // TODO: Hacks to try to handle the SR300 distorsions
//        camera.getDepthCamera().setCalibration(Papart.SR300IRCalib);
//        camera.getIRCamera().setCalibration(Papart.SR300IRCalib);
    }

    @Override
    public CameraNectar getMainCamera() {
        return cameraNectar;
    }

    @Override
    public int rawDepthSize() {
        return getDepthCamera().width() * getDepthCamera().height() * 2;
    }

    @Override
    public Camera.Type type() {
        return Camera.Type.NECTAR;
    }

    @Override
    public void loadDataFromDevice() {
//        setStereoCalibration(cameraNI.getHardwareExtrinsics());
    }

    @Override
    public DepthAnalysis.DepthComputation createDepthComputation() {
        return new OpenNIDepth();
    }
}
