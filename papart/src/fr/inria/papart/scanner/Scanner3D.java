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
package fr.inria.papart.scanner;

import fr.inria.papart.depthcam.DepthPoint;
import fr.inria.papart.depthcam.PointCloud;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.procam.display.ProjectorDisplay;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import processing.core.PApplet;
import processing.core.PConstants;
import static processing.core.PFont.list;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 *
 * @author Jeremy Laviole
 */
public class Scanner3D implements PConstants {

    private final PMatrix3D extrinsics, extrinsicsInv;
    private final PVector projPos, camPos;
    private final ProjectiveDeviceP projectorDevice, cameraDevice;

    private float cx, cy, fx, fy;
    private final ProjectorDisplay projector;

    private PVector lastError = new PVector();

    public Scanner3D(Camera camera, ProjectorDisplay projector) {
        this(camera.getProjectiveDevice(), projector);
    }

    // We suppose that the camera is loaded, and running.
    public Scanner3D(ProjectiveDeviceP camera, ProjectorDisplay projector) {
        this.projector = projector;

//        backgroundRemover = new BackgroundRemover(
//                camera.width(), camera.height());
        // Projective device 
        projectorDevice = projector.getProjectiveDeviceP();
        cameraDevice = camera;

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

    ArrayList<PVector> scannedPoints = new ArrayList<PVector>();
    ArrayList<PVector> scannedPointsColors = new ArrayList<PVector>();
    ArrayList<Integer> scannedPointsColorsInt = new ArrayList<>();

    public void compute3DPos(DecodedCode decodedCode, int scale, float errorXMax, float errorYMax) {

        int cameraX = decodedCode.getWidth();
        int cameraY = decodedCode.getHeight();

        boolean[] myMask = decodedCode.getMask();
        int[] decodedX = decodedCode.getDecodedX();
        int[] decodedY = decodedCode.getDecodedY();

        decodedCode.refImage.loadPixels();

        clear();

        // TODO: One point per observed Pixels. 
        // Solution ? List of valid points, and select the one with lowest error ?
        // Iterate in the camera image
        for (int y = 0; y < cameraY; y += scale) {
            for (int x = 0; x < cameraX; x += scale) {

                int offset = x + y * cameraX;

                // select only points where the data is retreived
                if (!myMask[offset]) {
                    continue;
                }

                PVector projectedPointProj = sceenTo3D(decodedX[offset], decodedY[offset]);
                PVector intersection = compute3DPoint(projectedPointProj,
                        new PVector(x, y));

                if (intersection == null) {
                    continue;
                }

                // TODO: Error in configuration file 
//                if (intersection.z < 200 || intersection.z > 2000) {
////		println("Intersection too far");
//                    continue;
//                }
                PVector error = lastError();
                float errX = error.x;
                float errY = error.y;
                PVector p2 = projector2DViewOf(intersection);

                if (errX > errorXMax || errY > errorYMax) {
                    continue;
                }

                scannedPoints.add(intersection);

                int c = decodedCode.refImage.pixels[offset];
                scannedPointsColors.add(new PVector(
                        c >> 16 & 0xFF,
                        c >> 8 & 0xFF,
                        c >> 0 & 0xFF));
                scannedPointsColorsInt.add(c);

                // Error as color. 
//                scannedPointsColors.add(new PVector(errX * 255f / 20f,
//                        errY * 255f / 20f,
//                        0));
                // noStroke();
                // fill(errX *2, errY*2, 100);
                // rect(p2.x, p2.y, sc, sc);
                // fill(errX *2, errY*2, 50);
                // rect(decodedX[offset], decodedY[offset], sc, sc);
            }

        }

//        System.out.println("3D points recovered : " + scannedPoints.size());
    }

    class DecodedPoint {

        public PVector intersection;
        public PVector error;
        public int projectorOffset;
        public int cameraOffset;
    }

    public void compute3DPosUniqueProj(DecodedCode decodedCode, int scale, float errorXMax, float errorYMax) {

        int cameraX = decodedCode.getWidth();
        int cameraY = decodedCode.getHeight();

        boolean[] myMask = decodedCode.getMask();
        int[] decodedX = decodedCode.getDecodedX();
        int[] decodedY = decodedCode.getDecodedY();

        int projPixels = projector.getWidth() * projector.getHeight();
        Map<Integer, ArrayList<DecodedPoint>> projectorPixels = new HashMap<Integer, ArrayList<DecodedPoint>>();
//        for (int i = 0; i < projPixels; i++) {
//            projectorPixels.put(i, new ArrayList<DecodedPoint>());
//        }

        decodedCode.refImage.loadPixels();

        clear();

        // TODO: One point per observed Pixels. 
        // Solution ? List of valid points, and select the one with lowest error ?
        // Iterate in the camera image
        for (int y = 0; y < cameraY; y += scale) {
            for (int x = 0; x < cameraX; x += scale) {

                int offsetCam = x + y * cameraX;

                // select only points where the data is retreived
                if (!myMask[offsetCam]) {
                    continue;
                }

                PVector projectedPointProj = sceenTo3D(decodedX[offsetCam], decodedY[offsetCam]);
                PVector intersection = compute3DPoint(projectedPointProj,
                        new PVector(x, y));

                if (intersection == null) {
                    continue;
                }

                int offsetProj = (int) decodedX[offsetCam] + (int) decodedY[offsetCam] * projector.getWidth();
                DecodedPoint decodedPoint = new DecodedPoint();
                decodedPoint.cameraOffset = offsetCam;
                decodedPoint.projectorOffset = offsetProj;
                decodedPoint.intersection = intersection;
                decodedPoint.error = lastError().copy();

                if (projectorPixels.get(offsetProj) == null) {
                    projectorPixels.put(offsetProj, new ArrayList<DecodedPoint>());
                }
                projectorPixels.get(offsetProj).add(decodedPoint);
            }

        }

        for (List<DecodedPoint> decodedPoints : projectorPixels.values()) {

            if (decodedPoints.isEmpty()) {
                continue;
            }

            Collections.sort(decodedPoints, new Comparator<DecodedPoint>() {
                @Override
                public int compare(DecodedPoint dp1, DecodedPoint dp2) {
                    if (dp1.error.mag() > dp2.error.mag()) {
                        return 1;
                    }
                    if (dp1.error.mag() < dp2.error.mag()) {
                        return -1;
                    }
                    return 0;
                }
            });

            // point with the lowest error !
            DecodedPoint first = decodedPoints.get(0);
            scannedPoints.add(first.intersection);
            int c = decodedCode.refImage.pixels[first.cameraOffset];
            scannedPointsColors.add(new PVector(
                    c >> 16 & 0xFF,
                    c >> 8 & 0xFF,
                    c >> 0 & 0xFF));
            scannedPointsColorsInt.add(c);
        }

//        System.out.println("3D points recovered : " + scannedPoints.size());
    }

    public void savePoints(PApplet applet, String name) {
        String[] vertices = new String[scannedPoints.size()];
        int k = 0;

        for (int i = 0; i < scannedPoints.size(); i++) {

            PVector v = scannedPoints.get(i);
            PVector c = scannedPointsColors.get(i);
            vertices[k++] = ("v " + v.x + " " + v.y + " " + v.z + " " + c.x + " " + c.y + " " + c.z);
            // for(PVector v : scannedPoints){
            // 	vertices[k++] = ("v " + v.x + " " + v.y + " " + v.z );
            // }
        }

        // Writes the strings to a file, each on a separate line
        applet.saveStrings(name, vertices);

    }

    public void clear() {
        scannedPoints.clear();
        scannedPointsColors.clear();
        scannedPointsColorsInt.clear();
    }

    public PointCloud asPointCloud(PApplet parent) {
        PointCloud cloud = new PointCloud(parent, scannedPoints.size());

        for (int i = 0; i < scannedPoints.size(); i++) {
            float x = scannedPoints.get(i).x;
            float y = scannedPoints.get(i).y;
            float z = scannedPoints.get(i).z;
            int c = scannedPointsColorsInt.get(i);
            DepthPoint pt = new DepthPoint(x, y, z, c);
            cloud.addPoint(pt);
        }

        cloud.loadVerticesToNative();

        return cloud;
    }

    public float focalDistance() {
        return fx;
    }

    public PVector sceenTo3D(int x, int y) {
        return projectorDevice.pixelToWorldNormP(x, y);
    }

    public PVector projector2DViewOf(PVector p) {
        PVector out = new PVector();
        extrinsics.mult(p, out);
        PVector projPixels = projectorDevice.worldToPixel(out, false);
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
     * @param detectedPoint in 2D coordinate of the camera
     * @return the 3D intersection or null if the error is too important.
     */
    public PVector compute3DPoint(PVector projectedPoint, PVector detectedPoint) {

        // We create two diffent "rays", so 4 different locations: 
        // Projector location,  point seen by the projector.  
        // Camera location, point seen by the camera. 
        // The origin is the camera as always.
        // point seen by the projector. 
        PVector projectedPointCam = new PVector();
        extrinsicsInv.mult(projectedPoint, projectedPointCam);
        projectedPointCam.sub(projPos);

        // point seen the camera.
        PVector observedPoint = cameraDevice.pixelToWorldNormP((int) detectedPoint.x, (int) detectedPoint.y);

        PVector intersection = intersectLineWithLine3D(projPos, projectedPointCam,
                camPos, observedPoint);

        // We have the point, we can compute its error !
        //////// Error computation ////////
        // Intersection from the Projector's coordinate system. 
        PVector interProj = new PVector();
        extrinsics.mult(intersection, interProj);

        int w = projector.getWidth();
//        int projCoord = projectorDevice.worldToPixel(interProj, true);
//        PVector projPixels = new PVector((projCoord % w), projCoord / w);

        PVector projPixels = projectorDevice.worldToPixel(interProj, true);

        // Need Lens distorsions ?!
//        int projCoordOrig = projectorDevice.worldToPixel(projectedPoint);
//        PVector projPixelsOrig = new PVector((projCoordOrig % w), projCoordOrig / w);
        PVector projPixelsOrig = projectorDevice.worldToPixel(projectedPoint, false);

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
