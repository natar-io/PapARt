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

import toxi.geom.Plane;
import toxi.geom.Triangle3D;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class PlaneCreator {

    private float height = PlaneCalibration.HEIGHT_NOT_SET;

    private Plane plane;
    private Vec3D[] points = new Vec3D[3];
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
