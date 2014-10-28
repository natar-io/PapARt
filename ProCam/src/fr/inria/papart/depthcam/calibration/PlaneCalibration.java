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
import processing.data.XML;
import toxi.geom.Plane;
import toxi.geom.Triangle3D;
import toxi.geom.Vec3D;
import toxi.math.MathUtils;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class PlaneCalibration extends Calibration {

    static final String PLANE_XML_NAME = "Plane";
    static final String PLANE_POS_XML_NAME = "Position";
    static final String PLANE_NORMAL_XML_NAME = "Normal";
    static final String PLANE_HEIGHT_XML_NAME = "Height";
    static final String X_XML_NAME = "x";
    static final String Y_XML_NAME = "y";
    static final String Z_XML_NAME = "z";

    private float planeHeight = 25.00f;

    private Plane plane;
    private Vec3D[] points;
    private int currentPointPlane;
    private boolean valid;

    public PlaneCalibration(PApplet parent, String fileName) {
        this.loadFrom(parent, fileName);
    }

    public PlaneCalibration() {
        initPlane();
    }

    protected void initPlane() {
        currentPointPlane = 0;
        points = new Vec3D[3];
        setValid(false);
    }

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
        this.setPlane(new Plane(tri));
        setValid(true);
    }

    public boolean orientation(Vec3D point, float value) {
        return plane.classifyPoint(point, 0.05f) == Plane.Classifier.BACK;
    }

    public boolean orientation(Vec3D p) {
        float d = plane.sub(p).dot(plane.normal);
        if (d < -MathUtils.EPS) {
            return false;
        } else if (d > MathUtils.EPS) {
            return true;
        }
        return true; //ON_PLANE;
    }

    public boolean hasGoodOrientationAndDistance(Vec3D point) {
        return orientation(point) && plane.getDistanceToPoint(point) <= planeHeight;
    }

    public boolean hasGoodDistance(Vec3D point) {
        return plane.getDistanceToPoint(point) <= planeHeight;
    }

    public boolean hasGoodOrientation(Vec3D point) {
        return orientation(point);
    }

    public float distanceTo(Vec3D point) {
        return plane.getDistanceToPoint(point);
    }

    public void flipNormal() {
        plane.normal = plane.normal.invert();
    }

    public void moveAlongNormal(float value) {
        plane.x = plane.x + value * plane.normal.x;
        plane.y = plane.y + value * plane.normal.y;
        plane.z = plane.z + value * plane.normal.z;
    }

    public float getPlaneHeight() {
        return planeHeight;
    }

    public void setPlaneHeight(float planeHeight) {
        this.planeHeight = planeHeight;
    }

    protected void setPlane(Plane plane) {
        this.plane = plane;
    }

    public Plane getPlane() {
        assert (isValid());
        return plane;
    }

    public boolean isValid() {
        return valid;
    }

    protected void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public void replaceIn(XML xml) {
        XML planeNode = xml.getChild(PLANE_XML_NAME);
        setVectorIn(planeNode.getChild(PLANE_POS_XML_NAME), (Vec3D) plane);
        setVectorIn(planeNode.getChild(PLANE_NORMAL_XML_NAME), plane.normal);
        planeNode.getChild(PLANE_HEIGHT_XML_NAME).setFloat(PLANE_HEIGHT_XML_NAME, planeHeight);
    }

    @Override
    public void addTo(XML xml) {
        XML root = new XML(PLANE_XML_NAME);
        XML pos = createXML(PLANE_POS_XML_NAME, (Vec3D) plane);
        XML normal = createXML(PLANE_NORMAL_XML_NAME, plane.normal);
        XML height = new XML(PLANE_HEIGHT_XML_NAME);
        height.setFloat(PLANE_HEIGHT_XML_NAME, planeHeight);

        root.addChild(pos);
        root.addChild(normal);
        root.addChild(height);
        xml.addChild(root);
    }

    private XML createXML(String name, Vec3D v) {
        XML node = new XML(name);
        this.setVectorIn(node, v);
        return node;
    }

    private void setVectorIn(XML node, Vec3D v) {
        node.setFloat(X_XML_NAME, v.x);
        node.setFloat(Y_XML_NAME, v.y);
        node.setFloat(Z_XML_NAME, v.z);
    }

    private Vec3D getVectorFrom(XML node) {
        Vec3D v = new Vec3D();
        v.x = node.getFloat(X_XML_NAME);
        v.y = node.getFloat(Y_XML_NAME);
        v.z = node.getFloat(Z_XML_NAME);
        return v;
    }

    @Override
    public void loadFrom(PApplet parent, String fileName) {
        XML root = parent.loadXML(fileName);
        XML planeNode = root.getChild(PLANE_XML_NAME);
        XML posNode = planeNode.getChild(PLANE_POS_XML_NAME);
        XML normalNode = planeNode.getChild(PLANE_NORMAL_XML_NAME);
        XML heightNode = planeNode.getChild(PLANE_HEIGHT_XML_NAME);

        Vec3D position = getVectorFrom(posNode);
        Vec3D normal = getVectorFrom(normalNode);
        float height = heightNode.getFloat(PLANE_HEIGHT_XML_NAME);

        this.plane = new Plane();
        plane.set(position);
        plane.normal.set(normal);
        setPlaneHeight(height);
        this.setValid(true);
    }
    
    @Override
    public String toString(){
        return this.plane.toString() + " " + this.planeHeight;
    }

}
