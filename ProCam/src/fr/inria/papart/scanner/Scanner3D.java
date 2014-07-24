/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.scanner;

import fr.inria.papart.procam.Camera;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.procam.Projector;
import processing.core.PConstants;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

/**
 *
 * @author jiii
 */
public class Scanner3D implements PConstants {

    int[] decodedX, decodedY;

    private final PMatrix3D extrinsics, extrinsicsInv;
    private final PVector projPos, camPos;
    private final ProjectiveDeviceP projDev, camDev;

    private float cx, cy, fx, fy;
    private final Projector projector;

    private PVector lastError = new PVector();

    // We suppose that the camera is loaded, and running.
    public Scanner3D(ProjectiveDeviceP camera, Projector projector) {
        this.projector = projector;

//        backgroundRemover = new BackgroundRemover(
//                camera.width(), camera.height());
        // Projective device 
        projDev = projector.getProjectiveDeviceP();
        camDev = camera;

        // Projector --> Camera (Origin)
        extrinsics = projector.getExtrinsics();

        // Camera (origin) --> Projector
        extrinsicsInv = extrinsics.get();
        extrinsicsInv.invert();

        // Camera is at the origin, easy
        camPos = new PVector();

        // Projector position is given by its extrinsics
        projPos = new PVector();
        extrinsicsInv.mult(camPos, projPos);

        //    fx = (projector.getIntrinsics().m00 + projector.getIntrinsics().m11) / 2f  ;
        fx = projector.getIntrinsics().m00;
        fy = projector.getIntrinsics().m11;
        cx = projector.getIntrinsics().m02;
        cy = projector.getIntrinsics().m12;
    }

    public float focalDistance() {
        return fx;
    }

    public PVector sceenTo3D(int x, int y) {
        return projDev.pixelToWorldNormP(x, y);
    }

    public PVector projector2DViewOf(PVector p) {
        PVector out = new PVector();
        extrinsics.mult(p, out);
        PVector projPixels = projDev.worldToPixel(out, false);
        return projPixels;
    }

    public PVector projector3DViewOf(PVector p) {
        PVector out = new PVector();
        extrinsics.mult(p, out);
        return out;
    }

    /**
     * *
     *
     * @param projectedPoint in 3D coordinate of the projector
     * @param detectedPoint in 2D coordianate of the camera
     * @return the 3D intersection or null if the error is too important.
     */
    public PVector compute3DPoint(PVector projectedPoint, PVector detectedPoint) {

        // Get the projected point relative to the camera coordinate system. 
        // So that we have a vector from the projector, to this point in the 
        // camera coordinate system. 
        PVector projectedPointCam = new PVector();

        // projectedPoint = projDev.pixelToWorldP((int) projectedPoint.x, (int) projectedPoint.y);
        extrinsicsInv.mult(projectedPoint, projectedPointCam);
        projectedPointCam.sub(projPos);

        //    PVector observedPoint = camDev.pixelToWorldP((int) detectedPoint.x, (int) detectedPoint.y);
        PVector observedPoint = camDev.pixelToWorldNormP((int) detectedPoint.x, (int) detectedPoint.y);

        PVector intersection = intersectLineWithLine3D(projPos, projectedPointCam,
                camPos, observedPoint);

        // We have the point, we can compute its error !
        //////// Error computation //////////
        // Intersection from the Projector's coordinate system. 
        PVector interProj = new PVector();
        extrinsics.mult(intersection, interProj);

        int w = projector.getWidth();
        int projCoord = projDev.worldToPixel(interProj);
        PVector projPixels = new PVector((projCoord % w), projCoord / w);

        // Need Lens distorsions ?!
        int projCoordOrig = projDev.worldToPixel(projectedPoint);
        PVector projPixelsOrig = new PVector((projCoordOrig % w), projCoordOrig / w);

        // TODO : specify error || or create a struct ?
        float errX = Math.abs(projPixelsOrig.x - projPixels.x);
        float errY = Math.abs(projPixelsOrig.y - projPixels.y);
        float error = PVector.dist(projPixels, projPixelsOrig);
        lastError.x = errX;
        lastError.y = errY;
        lastError.z = error;

        // Need Lens distorsions !
//    println("Projected " + projPixelsOrig);
//        if (error < 40) {
//            return intersection;
//        }
        return intersection;
    }

    public PVector lastError() {
        return lastError;
    }

    static public PVector intersectLineWithLine3D(PVector q1,
            PVector v1,
            PVector q2,
            PVector v2) {
        PVector q12 = new PVector();

        // Define intermediate quantities.
        float v1_dot_v1 = 0, v2_dot_v2 = 0, v1_dot_v2 = 0, q12_dot_v1 = 0, q12_dot_v2 = 0;

        q12.x = q1.x - q2.x;
        v1_dot_v1 += v1.x * v1.x;
        v2_dot_v2 += v2.x * v2.x;
        v1_dot_v2 += v1.x * v2.x;
        q12_dot_v1 += q12.x * v1.x;
        q12_dot_v2 += q12.x * v2.x;

        q12.y = q1.y - q2.y;
        v1_dot_v1 += v1.y * v1.y;
        v2_dot_v2 += v2.y * v2.y;
        v1_dot_v2 += v1.y * v2.y;
        q12_dot_v1 += q12.y * v1.y;
        q12_dot_v2 += q12.y * v2.y;

        q12.z = q1.z - q2.z;
        v1_dot_v1 += v1.z * v1.z;
        v2_dot_v2 += v2.z * v2.z;
        v1_dot_v2 += v1.z * v2.z;
        q12_dot_v1 += q12.z * v1.z;
        q12_dot_v2 += q12.z * v2.z;

        // Calculate scale factors.
        float s, t, denom;
        denom = v1_dot_v1 * v2_dot_v2 - v1_dot_v2 * v1_dot_v2;
        s = (v1_dot_v2 / denom) * q12_dot_v2 - (v2_dot_v2 / denom) * q12_dot_v1;
        t = -(v1_dot_v2 / denom) * q12_dot_v1 + (v1_dot_v1 / denom) * q12_dot_v2;

        // Evaluate closest point.
        PVector p = new PVector(
                ((q1.x + s * v1.x) + (q2.x + t * v2.x)) / 2,
                ((q1.y + s * v1.y) + (q2.y + t * v2.y)) / 2,
                ((q1.z + s * v1.z) + (q2.z + t * v2.z)) / 2);
        return p;
    }

}
