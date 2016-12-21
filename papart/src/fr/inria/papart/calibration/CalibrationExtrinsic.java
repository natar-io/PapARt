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

import fr.inria.papart.depthcam.devices.DepthCameraDevice;
import fr.inria.papart.multitouch.KinectTouchInput;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.display.ProjectorDisplay;
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
public class CalibrationExtrinsic {

    private final PApplet parent;

    private static final float OPEN_KINECT_Z_OFFSET = -15f;
    private static final float REALSENSE_Z_OFFSET = -52f;
    // Cameras
    private ProjectorDisplay projector;
    private final PMatrix3D kinectCameraExtrinsics = new PMatrix3D();
    private final Papart papart;

    // Kinect
    private DepthCameraDevice depthCameraDevice;

    public CalibrationExtrinsic(PApplet parent) {
        this.parent = parent;
        papart = Papart.getPapart();
    }

    void setProjector(ProjectorDisplay projector) {
        this.projector = projector;
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

    public void computeProjectorCameraExtrinsics(ArrayList<CalibrationSnapshot> snapshots) {
        PMatrix3D sum = new PMatrix3D(0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0);

        for (CalibrationSnapshot snapshot : snapshots) {
            PMatrix3D extr = computeExtrinsics(snapshot.mainCameraPaper,
                    snapshot.projectorPaper);
            Utils.addMatrices(sum, extr);
        }
        Utils.multMatrix(sum, 1f / (float) snapshots.size());

        saveProCamExtrinsics(sum);
    }

    public void saveProCamExtrinsics(PMatrix3D extr) {
        papart.saveCalibration(Papart.cameraProjExtrinsics, extr);
        projector.setExtrinsics(extr);
    }

    public void calibrateKinect(ArrayList<CalibrationSnapshot> snapshots, boolean useExternal) {
        if (depthCameraDevice == null) {
            return;
        }
        if (useExternal) {
            calibrateDepthAndExternalCam(snapshots);
        } else {
            calibrateProjectorDepthCam(snapshots);
        }
    }

    protected void calibrateProjectorDepthCam(ArrayList<CalibrationSnapshot> snapshots) {
        PMatrix3D kinectExtr = depthCameraDevice.getStereoCalibration().get();
        kinectExtr.invert();

        PlaneCalibration planeCalibCam = computeAveragePlaneCam(snapshots);
        planeCalibCam.flipNormal();

        // identity - no external camera for ProCam calibration
        PMatrix3D kinectCameraExtrinsics = new PMatrix3D();
        // Depth -> Color calibration.
        kinectCameraExtrinsics.set(kinectExtr);

        HomographyCalibration homography = CalibrationExtrinsic.computeScreenPaperIntersection(projector, planeCalibCam, kinectCameraExtrinsics);

        if (homography == HomographyCalibration.INVALID) {
            System.err.println("No intersection");
            return;
        }

        // TODO: not sure here... ?
        movePlaneAlongOffset(planeCalibCam);

        saveKinectPlaneCalibration(planeCalibCam, homography);
        saveKinectCameraExtrinsics(kinectCameraExtrinsics);
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

    protected void calibrateDepthAndExternalCam(ArrayList<CalibrationSnapshot> snapshots) {
        calibrateDepthToExternalExtr(snapshots);
        calibrateDepthCamPlane(snapshots);
    }

    protected void calibrateDepthToExternalExtr(ArrayList<CalibrationSnapshot> snapshots) {
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

    public boolean calibrateDepthCamPlane(ArrayList<CalibrationSnapshot> snapshots) {
        // Depth -> color  extrinsics
        PMatrix3D kinectExtr = depthCameraDevice.getStereoCalibration().get();

        // color -> depth  extrinsics
        kinectExtr.invert();

        PlaneCalibration planeCalibCam = computeAveragePlaneCam(snapshots);
        PlaneCalibration planeCalibKinect = computeAveragePlaneKinect(snapshots, kinectExtr);
        planeCalibCam.flipNormal();

        // Tracking --> depth
        PMatrix3D kinectCameraExtr = papart.loadCalibration(Papart.kinectTrackingCalib);

        HomographyCalibration homography = CalibrationExtrinsic.computeScreenPaperIntersection(projector,
                planeCalibCam,
                kinectCameraExtr);
        if (homography == HomographyCalibration.INVALID) {
            System.err.println("No intersection");
            return false;
        }

        // move the plane up a little.
        planeCalibKinect.flipNormal();
        movePlaneAlongOffset(planeCalibKinect);

        saveKinectPlaneCalibration(planeCalibKinect, homography);
        return true;
    }

    public boolean calibrateDepthCamPlaneOnly(ArrayList<CalibrationSnapshot> snapshots) {
        // Depth -> color  extrinsics
        PMatrix3D kinectExtr = depthCameraDevice.getStereoCalibration().get();

        // color -> depth  extrinsics
        kinectExtr.invert();

        PlaneCalibration planeCalibCam = computeAveragePlaneCam(snapshots);
        PlaneCalibration planeCalibKinect = computeAveragePlaneKinect(snapshots, kinectExtr);
        planeCalibCam.flipNormal();

        // Tracking --> depth
        PMatrix3D kinectCameraExtr = papart.loadCalibration(Papart.kinectTrackingCalib);

        HomographyCalibration homography = CalibrationExtrinsic.computeScreenPaperIntersection(projector,
                planeCalibCam,
                kinectCameraExtr);
        if (homography == HomographyCalibration.INVALID) {
            System.err.println("No intersection");
            return false;
        }

        // move the plane up a little.
        planeCalibKinect.flipNormal();
        movePlaneAlongOffset(planeCalibKinect);

        saveKinectPlaneCalibration(planeCalibKinect, homography);
        return true;
    }

    private PMatrix3D computeKinectCamExtrinsics(ArrayList<CalibrationSnapshot> snapshots, PMatrix3D stereoExtr) {
        PMatrix3D sum = new PMatrix3D(0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0);

        int nbCalib = 0;
        for (CalibrationSnapshot snapshot : snapshots) {
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

    private PlaneCalibration computeAveragePlaneKinect(ArrayList<CalibrationSnapshot> snapshots, PMatrix3D stereoExtr) {
        PVector paperSize = new PVector(297, 210);

        Plane sumKinect = new Plane(new Vec3D(0, 0, 0),
                new Vec3D(0, 0, 0));

        int nbCalib = 0;
        for (CalibrationSnapshot snapshot : snapshots) {
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

    private PlaneCalibration computeAveragePlaneCam(ArrayList<CalibrationSnapshot> snapshots) {
        PVector paperSize = new PVector(297, 210);

        Plane sumCam = new Plane(new Vec3D(0, 0, 0),
                new Vec3D(0, 0, 0));

        int nbPlanes = 0;
        for (CalibrationSnapshot snapshot : snapshots) {

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

        ((KinectTouchInput) papart.getTouchInput()).setPlaneAndProjCalibration(planeProjCalib);
    }

    public static PMatrix3D computeExtrinsics(PMatrix3D camPaper, PMatrix3D projPaper) {
        PMatrix3D extr = projPaper.get();
        extr.invert();
        extr.preApply(camPaper);
        extr.invert();
        return extr;
    }

    /**
     * Computes the intersection of the corners of the projector viewed by a
     * camera
     *
     * @param projector
     * @param planeCalibCam
     * @param kinectCameraExtrinsics
     * @return
     */
    public static HomographyCalibration computeScreenPaperIntersection(ProjectorDisplay projector, PlaneCalibration planeCalibCam, PMatrix3D kinectCameraExtrinsics) {
        // generate coordinates...
        float step = 0.5f;
        int nbPoints = (int) ((1 + 1.0F / step) * (1 + 1.0F / step));
        HomographyCreator homographyCreator = new HomographyCreator(3, 2, nbPoints);

        // Creates 3D points on the corner of the screen
        for (float i = 0; i <= 1.0; i += step) {
            for (float j = 0; j <= 1.0; j += step) {
                PVector screenPoint = new PVector(i, j);
                PVector kinectPoint = new PVector();

                PVector inter = projector.getProjectedPointOnPlane(planeCalibCam, i, j);

                if (inter == null) {
                    return HomographyCalibration.INVALID;
                }

                // get the point from the Kinect's point of view. 
                kinectCameraExtrinsics.mult(inter, kinectPoint);

                homographyCreator.addPoint(kinectPoint, screenPoint);
            }
        }
        return homographyCreator.getHomography();
    }

}
