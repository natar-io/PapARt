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
package fr.inria.papart.depthcam.calibration;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class ScreenSpaceCalibration extends PlaneCalibrationLegacy {

    private Homography homography;
    private PVector[] screenPoints = null;
    protected int nbPoints;

    public ScreenSpaceCalibration(PApplet parent, String outputFile,
            PVector screenSize) {
        super(parent, outputFile);
        initHomography();
    }

    public void initHomography() {

        float step = 0.5f;
        int nbPointsSpeed = (int) ((1 + 1f / step) * (1 + 1f / step));
        nbPoints = nbPointsSpeed;
        screenPoints = new PVector[nbPoints];
        
        homography = new fr.inria.papart.depthcam.calibration.Homography(parent, 3, 2, nbPoints);

        int k = 0;
        for (float i = 0; i <= 1.0; i += step) {
            for (float j = 0; j <= 1.0; j += step, k++) {
                PVector pt = new PVector(i, j);
                screenPoints[k] = new PVector(i, j);
                homography.setPoint(false, k, pt);
            }
        }

    }

    public void drawPoint(PGraphics pg, PVector screenSize, float ellipseSize) {

        if (!this.planeSet) {

            switch (currentPoint) {
                case 0:   pg.ellipse(0, 0, ellipseSize, ellipseSize);
                    break;
                case 1:   pg.ellipse(0, screenSize.y, ellipseSize, ellipseSize);
                    break;
                case 2:   pg.ellipse(screenSize.x, screenSize.y, ellipseSize, ellipseSize);
                    break;
            }
        } else {
            pg.ellipseMode(CENTER);
            pg.ellipse(screenPoints[currentPoint].x * screenSize.x,
                    screenPoints[currentPoint].y * screenSize.y,
                    ellipseSize, ellipseSize);
        }
    }

    /**
     *
     * @param point the 3D position of the current point.
     * @return true when the calibration ended. False while there are still
     * points to add.
     */
    public boolean pointFound(Vec3D point) {

        assert (currentPoint < nbPoints);

        if (!planeSet) {
            addPlanePoint(point);
            return false;
        } else {
            return addHomographyPoint(point);
        }
    }

    private void addPlanePoint(Vec3D point) {
        // The first three points are for the plane thresold.
        assert (currentPoint < 3);
        planeThreshold.addPoint(point);
        currentPoint++;

        // if the current Point is 3 -> compute the plane
        if (currentPoint == 3) {
            computePlane();
            currentPoint = 0;
            this.planeSet = true;
        }
    }

    private boolean addHomographyPoint(Vec3D point) {
        homography.setPoint(true, currentPoint, new PVector(point.x, point.y, point.z));
        currentPoint++;

        // All the points are selected
        if (currentPoint == nbPoints) {
            computeHomograpy();
            return true;
        }
        return false;
    }

    private void computeHomograpy() {
        homography.findHomography();
        KinectScreenCalibration.save(parent, outputFileName, homography, planeThreshold);
    }

    private void computePlane() {
        if (planeThreshold.computePlane()) {
            planeThreshold.setHeight(10);
            planeThreshold.moveUpDown(-11f);
        }
    }

}
