/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.scanner;

import fr.inria.papart.calibration.PlaneCalibration;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.display.ProjectorDisplay;
import java.util.ArrayList;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Ray3D;
import toxi.geom.ReadonlyVec3D;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class ExtrinsicCalibrator {

    private final DecodedCode decodedCode;
    private final Camera cameraTracking;
    private final PMatrix3D cameraPaperTransform;
    private final ProjectorDisplay projector;

    private ProjectiveDeviceP projectorDevice, cameraDevice;

    private PlaneCalibration planeCalibCam;

    private ArrayList<CalibrationPoint> pointList;
    private int nbValidPoints;

    private final PVector paperSize = new PVector(100, 100); // does not matter...

    public ExtrinsicCalibrator(PMatrix3D cameraPaperTransform, DecodedCode code,
            ProjectorDisplay projector, Camera cameraTracking) {
        this.decodedCode = code;
        this.cameraPaperTransform = cameraPaperTransform;
        this.projector = projector;
        this.cameraTracking = cameraTracking;

    }

    public PMatrix3D compute() {

        planeCalibCam = PlaneCalibration.CreatePlaneCalibrationFrom(cameraPaperTransform, paperSize);
        planeCalibCam.flipNormal();

        cameraDevice = cameraTracking.getProjectiveDevice();
        projectorDevice = projector.getProjectiveDeviceP();

        createPoints();

        // Now we have Camera Points, and Projector Points.
        // Projector Points are OK, for Camera we can reproject the plane
        // found from the Tracking... 
        projectCameraPoints();

        // Now we have the couples object/image.
        PVector imagePoints[] = new PVector[nbValidPoints];
        PVector objectPoints[] = new PVector[nbValidPoints];

        int k = 0;
        for (CalibrationPoint cp : pointList) {
            if (!cp.isValid) {
                continue;
            }

            imagePoints[k] = cp.imageProj;
            //	imagePoints[k] = cp.imageCam;
            objectPoints[k] = cp.object;
            k++;
        }
        if (k > 100) {
            PMatrix3D orientation = projectorDevice.estimateOrientationRansac(objectPoints, imagePoints);

            orientation.print();
            return orientation;
        } else {
            if (k > 3) {
                PMatrix3D orientation = projectorDevice.estimateOrientation(objectPoints, imagePoints);
                orientation.print();
                return orientation;
            }
        }

        return new PMatrix3D();
    }

    void createPoints() {

        boolean[] validPoints = decodedCode.getMask();
        int[] decodedX = decodedCode.getDecodedX();
        int[] decodedY = decodedCode.getDecodedY();

        pointList = new ArrayList<>();

        // check all the image for valid Points to create Pairs
        // iteration in cameraPoints. 
        int offset = 0;
        for (boolean isValid : validPoints) {
            offset++;
            if (!isValid) {
                continue;
            }

            int x = offset % cameraTracking.width();
            int y = offset / cameraTracking.width();

            CalibrationPoint cp = new CalibrationPoint();

            cp.imageCam.set(x, y);
            cp.imageProj.set(decodedX[offset], decodedY[offset]);

            //	println(cp.imageCam + " " + cp.imageProj);
            pointList.add(cp);
        }
    }

    void projectCameraPoints() {

        // frameWidth = projectorDevice.getWidth();
        // frameHeight = projectorDevice.getHeight();
        nbValidPoints = 0;

        for (CalibrationPoint cp : pointList) {

            PVector intersection = computeIntersection((int) cp.imageCam.x, (int) cp.imageCam.y);
            if (intersection == null) {
                continue;
            }

            nbValidPoints++;
            cp.object.set(intersection);
            cp.isValid = true;
        }
    }

// px and py in pixels... not homogeneous coordinates.
    PVector computeIntersection(int px, int py) {

        // Create a ray from the Camera, to intersect with the paper found.     
        PVector origin = new PVector(0, 0, 0);
        PVector viewedPt = cameraDevice.pixelToWorldNormP(px, py);

        Ray3D ray
                = new Ray3D(new Vec3D(origin.x,
                                origin.y,
                                origin.z),
                        new Vec3D(viewedPt.x,
                                viewedPt.y,
                                viewedPt.z));

        // Intersect ray with Plane 
        ReadonlyVec3D inter = planeCalibCam.getPlane().getIntersectionWithRay(ray);

        if (inter == null) {
            // println("No intersection :( check stuff");
            return null;
        }

        return new PVector(inter.x(), inter.y(), inter.z());
    }

    class CalibrationPoint {

        public PVector imageProj = new PVector();
        public PVector imageCam = new PVector();
        public PVector object = new PVector();
        public boolean isValid = false;

        public CalibrationPoint() {
        }
    }

}
