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

import fr.inria.papart.calibration.files.PlaneCalibration;
import toxi.geom.Plane;
import toxi.geom.Triangle3D;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */
public class PlaneCreator {

    private float height = PlaneCalibration.HEIGHT_NOT_SET;

    private Plane plane;
    private final Vec3D[] points = new Vec3D[3];
    private int currentPointPlane = 0;

    public void addPoint(Vec3D point) {
        points[currentPointPlane++] = point;
        if (currentPointPlane == 3) {
            computePlane();
        }
    }

    private void computePlane() {
        Triangle3D tri = new Triangle3D(points[0],
                points[1],
                points[2]);
        this.plane = new Plane(tri);
    }

    public void setHeight(float planeHeight) {
        this.height = planeHeight;
    }

    public PlaneCalibration getPlaneCalibration() {
        assert (isComputed());
        PlaneCalibration planeCalibration = new PlaneCalibration();
        planeCalibration.setPlane(this.plane);
        planeCalibration.setHeight(height);
        return planeCalibration;
    }

    public boolean isComputed() {
        return this.plane != null && this.height != PlaneCalibration.HEIGHT_NOT_SET;
    }

    @Override
    public String toString() {
        return this.plane.toString() + " " + this.height;
    }

}
