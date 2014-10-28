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
import processing.core.PMatrix3D;
import processing.data.XML;
import toxi.geom.Plane;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class PlaneAndProjectionCalibration extends Calibration {

    private HomographyCalibration homographyCalibration = new HomographyCalibration();
    private PlaneCalibration planeCalibration = new PlaneCalibration();

    @Override
    public void loadFrom(PApplet parent, String fileName) {
        planeCalibration.loadFrom(parent, fileName);
        homographyCalibration.loadFrom(parent, fileName);
    }

    public Vec3D project(Vec3D point) {
        Vec3D projectedPoint = homographyCalibration.mat.applyTo(getPlane().getProjectedPoint(point));
        projectedPoint.x /= projectedPoint.z;
        projectedPoint.y /= projectedPoint.z;
        projectedPoint.z = getPlane().distanceTo(point);
        return projectedPoint;
    }

    @Override
    public boolean isValid() {
        return planeCalibration.isValid() && homographyCalibration.isValid();
    }

    @Override
    public void addTo(XML xml) {
        planeCalibration.addTo(xml);
        homographyCalibration.addTo(xml);
    }

    @Override
    public void replaceIn(XML xml) {
        planeCalibration.replaceIn(xml);
        homographyCalibration.replaceIn(xml);
    }

    public PlaneCalibration getPlaneCalibration() {
        return planeCalibration;
    }

    public HomographyCalibration getHomographyCalibration() {
        return homographyCalibration;
    }

    public void setPlaneCalibration(PlaneCalibration pc) {
        this.planeCalibration = pc;
    }

    public void setHomographyCalibration(HomographyCalibration hc) {
        this.homographyCalibration = hc;
    }

    ///////// Generated Delegation Methods ///////////////
    public PMatrix3D getHomography() {
        return homographyCalibration.getHomography();
    }

    public PMatrix3D getHomographyInv() {
        return homographyCalibration.getHomographyInv();
    }

    public boolean orientation(Vec3D point, float value) {
        return planeCalibration.orientation(point, value);
    }

    public boolean orientation(Vec3D p) {
        return planeCalibration.orientation(p);
    }

    public boolean hasGoodOrientationAndDistance(Vec3D point) {
        return planeCalibration.hasGoodOrientationAndDistance(point);
    }

    public boolean hasGoodDistance(Vec3D point) {
        return planeCalibration.hasGoodDistance(point);
    }

    public boolean hasGoodOrientation(Vec3D point) {
        return planeCalibration.hasGoodOrientation(point);
    }

    public float distanceTo(Vec3D point) {
        return planeCalibration.distanceTo(point);
    }

    public void moveAlongNormal(float value) {
        planeCalibration.moveAlongNormal(value);
    }

    public float getPlaneHeight() {
        return planeCalibration.getPlaneHeight();
    }

    public void setPlaneHeight(float planeHeight) {
        planeCalibration.setPlaneHeight(planeHeight);
    }

    public Plane getPlane() {
        return planeCalibration.getPlane();
    }

}
