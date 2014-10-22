/* 
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.inria.papart.depthcam;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class ScreenSpaceCalibration implements PConstants {

    private PlaneThreshold planeThreshold;

    protected int currentPoint;

    private final String outputFileName;
    private final PApplet pa;
    private final int nbPoints;
    private final Homography homography;
    private PVector[] screenPoints = null;

    
    public ScreenSpaceCalibration(PApplet parent, int nPoints,
            String outputFile,
            PVector screenSize) {

        this.pa = parent;
        this.outputFileName = outputFile;

        float speed = 0.5f;
        int nbPointsSpeed = (int) ((1 + 1f / speed) * (1 + 1f / speed));

        nbPoints = nPoints + nbPointsSpeed + 3;
        homography = new fr.inria.papart.depthcam.Homography(parent, 3, 2, nbPoints);

        // First 3 points for the plane
        homography.setPoint(false, 0, new PVector(0, 0));
        homography.setPoint(false, 1, new PVector(0, 1));
        homography.setPoint(false, 2, new PVector(1, 0));

        if (screenSize != null) {
            screenPoints = new PVector[nbPoints];
            screenPoints[0] = new PVector(0, 0);
            screenPoints[1] = new PVector(0, screenSize.y);
            screenPoints[2] = new PVector(screenSize.x, 0);
        }

        int k = 3;

        for (float i = 0; i <= 1.0; i += speed) {
            for (float j = 0; j <= 1.0; j += speed, k++) {
                PVector pt = new PVector(i, j);

                if (screenSize != null) {
                    screenPoints[k] = new PVector(screenSize.x * i, 
                                screenSize.y * j);
                }
                homography.setPoint(false, k, pt);
            }
        }

//	// TODO ?
//        // Generate random points, avoid the corners.
//        for (int i = nbPointsSpeed + 3; i < nbPoints; i++) {
//
//            PVector v = new PVector(random(frameSizeX),
//                    random(frameSizeY));
//
//            PVector vNorm = new PVector(v.x / (float) frameSizeX,
//                    v.y / (float) frameSizeY);
//
//            homography.setPoint(false, i, vNorm);
//        }
        currentPoint = 0;
        planeThreshold = new PlaneThreshold();
    }

    /**
     * Draw the current point
     * @param pg current graphics context
     * @param ellipseSize point size
     */
    public void drawPoint(PGraphics pg, float ellipseSize) {
        pg.ellipseMode(CENTER);
        pg.ellipse(screenPoints[currentPoint].x,
                screenPoints[currentPoint].y,
                ellipseSize, ellipseSize);
    }

    /**
     * 
     * @param point the 3D position of the current point. 
     * @return  true when the calibration ended. False while there are 
     * still points to add. 
     */
    public boolean pointFound(Vec3D point) {

        assert (currentPoint < nbPoints);

        // The first three points are for the plane thresold.
        if (currentPoint < 3) {
            planeThreshold.addPoint(point);
        }

        homography.setPoint(true, currentPoint, new PVector(point.x, point.y, point.z));
        currentPoint++;

        // if the current Point is 3 -> compute the plane
        if (currentPoint == 3) {
            computePlane();
            return false;
        }

        // All the points are selected
        if (currentPoint == nbPoints) {
            computeHomograpy();
            return true;
        }

        return false;

    }

    private void computeHomograpy() {
        homography.findHomography();
        KinectScreenCalibration.save(pa, outputFileName, homography, planeThreshold);
    }

    private void computePlane() {
        if (planeThreshold.computePlane()) {
            planeThreshold.setHeight(10);
            planeThreshold.moveUpDown(-11f);
        }
    }

}
