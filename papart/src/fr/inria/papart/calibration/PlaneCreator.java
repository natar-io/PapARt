/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.calibration;

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
