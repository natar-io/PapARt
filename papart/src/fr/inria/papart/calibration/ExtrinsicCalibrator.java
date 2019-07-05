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

import tech.lity.rea.nectar.calibration.HomographyCalibration;
import tech.lity.rea.nectar.calibration.PlaneAndProjectionCalibration;
import tech.lity.rea.nectar.calibration.PlaneCalibration;
import tech.lity.rea.nectar.depthcam.DepthCameraDevice;
import fr.inria.papart.multitouch.DepthTouchInput;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.camera.ProjectorAsCamera;
import tech.lity.rea.nectar.camera.Camera;
import fr.inria.papart.procam.display.ProjectorDisplay;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import tech.lity.rea.nectar.calibration.HomographyCreator;
import tech.lity.rea.nectar.camera.CameraNectar;
import tech.lity.rea.nectar.camera.CameraRGBIRDepth;
import tech.lity.rea.nectar.camera.SubCamera;
import toxi.geom.Plane;
import toxi.geom.Vec3D;

/**
 *
 * @author Jérémy Laviole - laviole@rea.lity.tech
 */
public class ExtrinsicCalibrator {

    private final PApplet parent;

    // TODO: find a way to tweak this. 
    private static final float OPEN_KINECT_Z_OFFSET = -25f;
    private static final float REALSENSE_Z_OFFSET = -15f;
    // Cameras
    private ProjectorDisplay projector;
    private final PMatrix3D depthCameraExtrinsics = new PMatrix3D();
    private final Papart papart;

    private DepthCameraDevice depthCameraDevice;
    private ProjectorAsCamera projectorAsCamera;
    private Camera mainColorCamera;

    public ExtrinsicCalibrator(PApplet parent) {
        this.parent = parent;
        papart = Papart.getPapart();
    }

    public void setProjector(ProjectorDisplay projector) {
        this.projector = projector;
    }

    public void setProjectorAsCamera(ProjectorAsCamera projectorAsCamera) {
        this.projectorAsCamera = projectorAsCamera;
    }

    public void setDepthCamera(DepthCameraDevice device) {
        this.depthCameraDevice = device;
    }

    public void setMainColorCamera(Camera cam) {
        this.mainColorCamera = cam;
    }

    public PMatrix3D getDepthCamExtrinsics() {
        return this.depthCameraExtrinsics;
    }

    public PMatrix3D computeProjectorCameraExtrinsics(ArrayList<ExtrinsicSnapshot> snapshots) {
        PMatrix3D sum = new PMatrix3D(0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0);
        PMatrix3D sum2 = new PMatrix3D(0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0);

        for (ExtrinsicSnapshot snapshot : snapshots) {
            PMatrix3D extr = computeExtrinsics(snapshot.mainCameraPaper,
                    snapshot.projectorPaper);

//            System.out.println("Extrinsics: ");
//            extr.print();
            Utils.addMatrices(sum, extr);
        }
        Utils.multMatrix(sum, 1f / (float) snapshots.size());

        System.out.println("Extrinsics average: ");
        sum.print();
        PVector sumPos = Utils.posFromMatrix(sum);

        // Second pass - remove the outliers  (distant from X mm)
        int k = 0;
        for (ExtrinsicSnapshot snapshot : snapshots) {
            PMatrix3D extr = computeExtrinsics(snapshot.mainCameraPaper,
                    snapshot.projectorPaper);

            float dist = Utils.posFromMatrix(extr).dist(sumPos);
            if (dist < 10f) { // 2 cm !
                Utils.addMatrices(sum2, extr);
                k++;
                System.out.println("Extrinsics TAKEN: ");
                extr.print();
            }
        }
        Utils.multMatrix(sum2, 1f / (float) k);

        sum.print();
        sum2.print();

        if (k == 0) {
            saveProCamExtrinsics(sum);
        } else {
            saveProCamExtrinsics(sum2);
        }
        return sum2;
    }

    public void saveProCamExtrinsics(PMatrix3D extr) {
        HomographyCalibration hc = new HomographyCalibration();
        hc.setMatrix(extr);

        projectorAsCamera.saveExtrinsics(hc);
//        papart.saveCalibration(Papart.cameraProjExtrinsics, extr);

        projector.setExtrinsics(extr);
    }

    public void calibrateDepthCamera(ArrayList<ExtrinsicSnapshot> snapshots, boolean useExternal) {
        if (depthCameraDevice == null) {
            return;
        }
        if (useExternal) {
            calibrateDepthAndExternalCam(snapshots);
        } else {
            calibrateProjectorDepthCam(snapshots);
        }
    }

    /**
     * Simple normal case, One depthcamera (color - Depth) and a projector.
     *
     * @param snapshots
     */
    protected void calibrateProjectorDepthCam(ArrayList<ExtrinsicSnapshot> snapshots) {
        PMatrix3D kinectExtr = depthCameraDevice.getStereoCalibration().get();
        kinectExtr.invert();

        PlaneCalibration planeCalibCam = computeAveragePlaneTrackingCam(snapshots);
        planeCalibCam.flipNormal();

        // identity - no external camera for ProCam calibration
        PMatrix3D kinectCameraExtrinsics = new PMatrix3D();
        // Depth -> Color calibration.
        kinectCameraExtrinsics.set(kinectExtr);

        HomographyCalibration homography = ExtrinsicCalibrator.computeScreenPaperIntersection(projector, planeCalibCam, kinectCameraExtrinsics);

        if (homography == HomographyCalibration.INVALID) {
            System.err.println("No intersection");
            return;
        }

        // TODO: not sure here... ?
        movePlaneAlongOffset(planeCalibCam);

        saveDepthPlaneCalibration(planeCalibCam, homography);
        saveDepthCameraExtrinsics(depthCameraExtrinsics);
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

    /**
     * More complex case: One camera for tracking, One depth camera (color +
     * depth) and a projector.
     *
     * @param snapshots
     */
    protected void calibrateDepthAndExternalCam(ArrayList<ExtrinsicSnapshot> snapshots) {
        calibrateDepthToExternalExtr(snapshots);
        calibrateDepthCamPlane(snapshots);
    }

    /**
     * Color camera to Depth cameara (color).
     *
     * @param snapshots
     */
    protected void calibrateDepthToExternalExtr(ArrayList<ExtrinsicSnapshot> snapshots) {
        // Depth -> color  extrinsics
        PMatrix3D kinectExtr = depthCameraDevice.getStereoCalibration().get();

        // color -> depth  extrinsics
        kinectExtr.invert();

        // depth -> tracking
        PMatrix3D depthCamToMainCamExtr = computeKinectCamExtrinsics(snapshots, kinectExtr);

        // // tracking -> depth
        depthCamToMainCamExtr.invert();

        this.depthCameraExtrinsics.set(depthCamToMainCamExtr);

        // depthCamera (color, extrinsics)
        saveDepthCameraExtrinsics(depthCamToMainCamExtr);
    }

    @Deprecated
    public boolean calibrateDepthCamPlaneOnly(ArrayList<ExtrinsicSnapshot> snapshots) {
        // Depth -> color  extrinsics
        PMatrix3D stereoExtrinsics = depthCameraDevice.getStereoCalibration().get();

        // color -> depth  extrinsics
        stereoExtrinsics.invert();

        PlaneCalibration planeCalibCam = computeAveragePlaneTrackingCam(snapshots);
        PlaneCalibration planeCalibKinect = computeAveragePlaneDepthCamera(snapshots, stereoExtrinsics);
        planeCalibCam.flipNormal();

        // Tracking --> depth
        // Camera extrinsics should be used here...
        HomographyCalibration homography = ExtrinsicCalibrator.computeScreenPaperIntersection(projector,
                planeCalibCam,
                stereoExtrinsics);
        if (homography == HomographyCalibration.INVALID) {
            System.err.println("No intersection");
            return false;
        }

        // move the plane up a little.
        planeCalibKinect.flipNormal();
        movePlaneAlongOffset(planeCalibKinect);

        saveDepthPlaneCalibration(planeCalibKinect, homography);
        return true;
    }

    private PMatrix3D computeKinectCamExtrinsics(ArrayList<ExtrinsicSnapshot> snapshots, PMatrix3D stereoExtr) {
        PMatrix3D sum = new PMatrix3D(0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0);

        int nbCalib = 0;
        for (ExtrinsicSnapshot snapshot : snapshots) {
            if (snapshot.depthCamPaper == null) {
                continue;
            }

            // Color -> Paper
            PMatrix3D boardFromDepth = snapshot.depthCamPaper.get();

            /// depth -> color -> color -> Paper
            boardFromDepth.preApply(stereoExtr);

            PMatrix3D extr = computeExtrinsics(boardFromDepth, snapshot.mainCameraPaper);

            Utils.addMatrices(sum, extr);
            nbCalib++;
        }

        Utils.multMatrix(sum, 1f / (float) nbCalib);
        return sum;
    }

    private PlaneCalibration computeAveragePlaneDepthCamera(ArrayList<ExtrinsicSnapshot> snapshots, PMatrix3D stereoExtr) {
        PVector paperSize = new PVector(297, 210);

        Plane sumDepthCam = new Plane(new Vec3D(0, 0, 0),
                new Vec3D(0, 0, 0));

        int nbCalib = 0;
        for (ExtrinsicSnapshot snapshot : snapshots) {
            if (snapshot.depthCamPaper == null) {
                continue;
            }

            //  color -> paper
            PMatrix3D boardFromDepth = snapshot.depthCamPaper.get();

            // Depth -> color -> color -> paper
            boardFromDepth.preApply(stereoExtr);

            PlaneCalibration planeCalibKinect
                    = PlaneCalibration.CreatePlaneCalibrationFrom(boardFromDepth, paperSize);
            Utils.sumPlane(sumDepthCam, planeCalibKinect.getPlane());
            nbCalib++;
        }

        Utils.averagePlane(sumDepthCam, 1f / nbCalib);

        PlaneCalibration calibration = new PlaneCalibration();
        calibration.setPlane(sumDepthCam);
        calibration.setHeight(PlaneCalibration.DEFAULT_PLANE_HEIGHT);

//        System.out.println("Plane viewed by the kinect");
//        println(sumKinect);
        return calibration;
    }

    public PlaneCalibration computeAveragePlaneTrackingCam(ArrayList<ExtrinsicSnapshot> snapshots) {
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

    /**
     * Compute 3D plane from depth Camera using depth camera extrinsics and
     * stereo calibration. TO UPDATE
     *
     * @param snapshots
     * @return
     */
    public boolean calibrateDepthCamPlane(ArrayList<ExtrinsicSnapshot> snapshots) {
        // Depth -> color  extrinsics
        PMatrix3D stereoExtrinsics = depthCameraDevice.getStereoCalibration().get();

        // color -> depth  extrinsics
        stereoExtrinsics.invert();

        PlaneCalibration planeCalibCam = computeAveragePlaneTrackingCam(snapshots);
        PlaneCalibration planeCalibKinect = computeAveragePlaneDepthCamera(snapshots, stereoExtrinsics);
        planeCalibCam.flipNormal();

        // Tracking --> depth
        PMatrix3D kinectCameraExtr = depthCameraDevice.getColorCamera().getExtrinsics();
        // papart.loadCalibration(Papart.kinectTrackingCalib);

        HomographyCalibration homography = ExtrinsicCalibrator.computeScreenPaperIntersection(projector,
                planeCalibCam,
                kinectCameraExtr);
        if (homography == HomographyCalibration.INVALID) {
            System.err.println("No intersection");
            return false;
        }

        // move the plane up a little.
        planeCalibKinect.flipNormal();
        movePlaneAlongOffset(planeCalibKinect);

        saveDepthPlaneCalibration(planeCalibKinect, homography);
        return true;
    }

    /**
     * To test !
     *
     * @param kinectCameraExtrinsics
     */
    public void saveDepthCameraExtrinsics(PMatrix3D kinectCameraExtrinsics) {

        depthCameraDevice.getColorCamera().setExtrinsics(depthCameraExtrinsics);

        HomographyCalibration hc = new HomographyCalibration();
        hc.setMatrix(kinectCameraExtrinsics);

        CameraRGBIRDepth mainCam = depthCameraDevice.getMainCamera();
        if (mainCam instanceof CameraNectar) {
            CameraNectar cam = (CameraNectar) mainCam;
            cam.setExtrinsics(depthCameraExtrinsics);
            cam.saveExtrinsics("cameraTracking");
        }

        // Old save to papart
//        papart.saveCalibration(Papart.kinectTrackingCalib, kinectCameraExtrinsics);
    }

    public void saveDepthPlaneCalibration(PlaneCalibration planeCalib, HomographyCalibration homography) {
        PlaneAndProjectionCalibration planeProjCalib = new PlaneAndProjectionCalibration();
        planeProjCalib.setPlane(planeCalib);
        planeProjCalib.setHomography(homography);

        CameraRGBIRDepth mainCam = depthCameraDevice.getMainCamera();
        if (mainCam instanceof CameraNectar) {
            CameraNectar cam = (CameraNectar) mainCam;
            planeCalib.saveToXML(cam.getRedisClient(), cam.getCameraDescription() + ":plane");
            homography.saveToXML(cam.getRedisClient(), cam.getCameraDescription() + ":planeHomography");
        }

//        planeProjCalib.saveTo(parent, Papart.planeAndProjectionCalib);
        ((DepthTouchInput) papart.getTouchInput()).setPlaneAndProjCalibration(planeProjCalib);
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
