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
package fr.inria.papart.calibration;

import fr.inria.papart.calibration.ExtrinsicSnapshot;
import fr.inria.papart.calibration.HomographyCreator;
import fr.inria.papart.calibration.Utils;
import fr.inria.papart.calibration.files.HomographyCalibration;
import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.files.PlaneCalibration;
import fr.inria.papart.depthcam.devices.DepthCameraDevice;
import fr.inria.papart.multitouch.DepthTouchInput;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.camera.Camera;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Plane;
import toxi.geom.Vec3D;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class ExtrinsicCalibrator {

    private final PApplet parent;

    // TODO: find a way to tweak this. 
    private static final float OPEN_KINECT_Z_OFFSET = -25f;
    private static final float REALSENSE_Z_OFFSET = -15f;
    // Cameras
    private final PMatrix3D kinectCameraExtrinsics = new PMatrix3D();
    private final Papart papart;

    // Kinect
    private DepthCameraDevice depthCameraDevice;

    public ExtrinsicCalibrator(PApplet parent) {
        this.parent = parent;
        papart = Papart.getPapart();
    }


    public void setDefaultDepthCamera() {
        this.depthCameraDevice = papart.getDepthCameraDevice();
    }

    public void setDepthCamera(DepthCameraDevice device) {
        this.depthCameraDevice = device;
    }

    public PMatrix3D getKinectCamExtrinsics() {
        return this.kinectCameraExtrinsics;
    }

    public void calibrateKinect(ArrayList<ExtrinsicSnapshot> snapshots, boolean useExternal) {
        if (depthCameraDevice == null) {
            return;
        }
        if (useExternal) {
            calibrateDepthAndExternalCam(snapshots);
        }
    }

    private void movePlaneAlongOffset(PlaneCalibration planeCalib) {

        if (depthCameraDevice.type() == Camera.Type.OPEN_KINECT) {
            System.out.println("Moving the plane along Z... " + OPEN_KINECT_Z_OFFSET);
            planeCalib.moveAlongNormal(OPEN_KINECT_Z_OFFSET);
        }
        if (depthCameraDevice.type() == Camera.Type.REALSENSE) {
            planeCalib.moveAlongNormal(REALSENSE_Z_OFFSET);
        }

    }

    protected void calibrateDepthAndExternalCam(ArrayList<ExtrinsicSnapshot> snapshots) {
        calibrateDepthToExternalExtr(snapshots);
    }

    protected void calibrateDepthToExternalExtr(ArrayList<ExtrinsicSnapshot> snapshots) {
        // Depth -> color  extrinsics
        PMatrix3D kinectExtr = depthCameraDevice.getStereoCalibration().get();

        // color -> depth  extrinsics
        kinectExtr.invert();

        // depth -> tracking
        PMatrix3D kinectCameraExtr = computeKinectCamExtrinsics(snapshots, kinectExtr);

        // // tracking -> depth
        kinectCameraExtr.invert();

        this.kinectCameraExtrinsics.set(kinectCameraExtr);
        saveKinectCameraExtrinsics(kinectCameraExtr);
    }

    private PMatrix3D computeKinectCamExtrinsics(ArrayList<ExtrinsicSnapshot> snapshots, PMatrix3D stereoExtr) {
        PMatrix3D sum = new PMatrix3D(0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0);

        int nbCalib = 0;
        for (ExtrinsicSnapshot snapshot : snapshots) {
            if (snapshot.kinectPaper == null) {
                continue;
            }

            // Color -> Paper
            PMatrix3D boardFromDepth = snapshot.kinectPaper.get();

            /// depth -> color -> color -> Paper
            boardFromDepth.preApply(stereoExtr);

            PMatrix3D extr = computeExtrinsics(boardFromDepth, snapshot.mainCameraPaper);

            Utils.addMatrices(sum, extr);
            nbCalib++;
        }

        Utils.multMatrix(sum, 1f / (float) nbCalib);
        return sum;
    }

    private PlaneCalibration computeAveragePlaneKinect(ArrayList<ExtrinsicSnapshot> snapshots, PMatrix3D stereoExtr) {
        PVector paperSize = new PVector(297, 210);

        Plane sumKinect = new Plane(new Vec3D(0, 0, 0),
                new Vec3D(0, 0, 0));

        int nbCalib = 0;
        for (ExtrinsicSnapshot snapshot : snapshots) {
            if (snapshot.kinectPaper == null) {
                continue;
            }

            //  color -> paper
            PMatrix3D boardFromDepth = snapshot.kinectPaper.get();

            // Depth -> color -> color -> paper
            boardFromDepth.preApply(stereoExtr);

            PlaneCalibration planeCalibKinect
                    = PlaneCalibration.CreatePlaneCalibrationFrom(boardFromDepth, paperSize);
            Utils.sumPlane(sumKinect, planeCalibKinect.getPlane());
            nbCalib++;
        }

        Utils.averagePlane(sumKinect, 1f / nbCalib);

        PlaneCalibration calibration = new PlaneCalibration();
        calibration.setPlane(sumKinect);
        calibration.setHeight(PlaneCalibration.DEFAULT_PLANE_HEIGHT);

//        System.out.println("Plane viewed by the kinect");
//        println(sumKinect);
        return calibration;
    }

    public PlaneCalibration computeAveragePlaneCam(ArrayList<ExtrinsicSnapshot> snapshots) {
        PVector paperSize = new PVector(297, 210);

        Plane sumCam = new Plane(new Vec3D(0, 0, 0),
                new Vec3D(0, 0, 0));

        int nbPlanes = 0;
        for (ExtrinsicSnapshot snapshot : snapshots) {

            if (snapshot.mainCameraPaper == null) {
                continue;
            }

            PlaneCalibration cam = PlaneCalibration.CreatePlaneCalibrationFrom(
                    snapshot.mainCameraPaper.get(), paperSize);

            Utils.sumPlane(sumCam, cam.getPlane());
            nbPlanes++;
        }
        Utils.averagePlane(sumCam, 1f / nbPlanes);

        PlaneCalibration calibration = new PlaneCalibration();
        calibration.setPlane(sumCam);
        calibration.setHeight(PlaneCalibration.DEFAULT_PLANE_HEIGHT);

        return calibration;
    }

    public void saveKinectCameraExtrinsics(PMatrix3D kinectCameraExtrinsics) {
        papart.saveCalibration(Papart.kinectTrackingCalib, kinectCameraExtrinsics);
    }

    public void saveKinectPlaneCalibration(PlaneCalibration planeCalib, HomographyCalibration homography) {
        PlaneAndProjectionCalibration planeProjCalib = new PlaneAndProjectionCalibration();
        planeProjCalib.setPlane(planeCalib);
        planeProjCalib.setHomography(homography);
        planeProjCalib.saveTo(parent, Papart.planeAndProjectionCalib);

        ((DepthTouchInput) papart.getTouchInput()).setPlaneAndProjCalibration(planeProjCalib);
    }

    public static PMatrix3D computeExtrinsics(PMatrix3D camPaper, PMatrix3D projPaper) {
        PMatrix3D extr = projPaper.get();
        extr.invert();
        extr.preApply(camPaper);
        extr.invert();
        return extr;
    }

  

}
