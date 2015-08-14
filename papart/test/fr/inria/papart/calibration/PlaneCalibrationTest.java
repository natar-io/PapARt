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
package fr.inria.papart.calibration;

import fr.inria.papart.calibration.PlaneCalibration;
import fr.inria.papart.calibration.PlaneCreator;
import fr.inria.papart.calibration.Calibration;
import fr.inria.papart.Sketch;
import static fr.inria.papart.calibration.HomographyCalibrationTest.DIMS;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Test;
import processing.core.PApplet;
import processing.data.XML;
import toxi.geom.Plane;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class PlaneCalibrationTest {

    PlaneCalibration instance;
    Sketch sketch;

    static Vec3D pt1 = new Vec3D(0, 0, 0);
    static Vec3D pt2 = new Vec3D(1, 0, 0);
    static Vec3D pt3 = new Vec3D(1, 1, 0);

    public static Vec3D expectedOrigin = new Vec3D(0.6666667f, 0.33333334f, 0.0f);
    public static Vec3D expectedNorm = new Vec3D(-0.0f, 0.0f, -1.0f);

    static final float DEFAULT_PLANE_HEIGHT = 15f;

    @Test
    public void checkCreation() {
        instance = createPlane();
        checkPlane(instance);
    }

    @Test
    public void checkSave() {
        createInstanceAndSketch();
        instance.saveTo(sketch, Common.currentPath + Common.PlaneCalibration);
    }

    @Test
    public void checkLoad() {
        sketch = new Sketch();         String[] args = new String[]{"--present", "test.fr.inria.papart.calibration.ProjectiveCalibrationTest"};         PApplet.runSketch(args, sketch);
        instance = new PlaneCalibration();
        instance.loadFrom(sketch, Common.currentPath + Common.PlaneCalibration);
        checkPlane(instance);
    }

    @Test
    public void checkXML() {
        sketch = new Sketch();         String[] args = new String[]{"--present", "test.fr.inria.papart.calibration.ProjectiveCalibrationTest"};         PApplet.runSketch(args, sketch);
        instance = new PlaneCalibration();

        XML root = sketch.loadXML(Common.currentPath + Common.PlaneCalibration);
        assertTrue(root.getName() == Calibration.CALIBRATION_XML_NAME);

        XML planeNode = root.getChild(PlaneCalibration.PLANE_XML_NAME);
        assertNotNull(planeNode);

        XML posNode = planeNode.getChild(PlaneCalibration.PLANE_POS_XML_NAME);
        assertNotNull(posNode);
        XML normalNode = planeNode.getChild(PlaneCalibration.PLANE_NORMAL_XML_NAME);
        assertNotNull(normalNode);
        XML heightNode = planeNode.getChild(PlaneCalibration.PLANE_HEIGHT_XML_NAME);
        assertNotNull(heightNode);

        assertTrue(heightNode.getFloat(PlaneCalibration.PLANE_HEIGHT_XML_NAME) == DEFAULT_PLANE_HEIGHT);
    }

    private void createInstanceAndSketch() {
        sketch = new Sketch();         String[] args = new String[]{"--present", "test.fr.inria.papart.calibration.ProjectiveCalibrationTest"};         PApplet.runSketch(args, sketch);
        instance = createPlane();
    }

    public static PlaneCalibration createPlane() {
        PlaneCreator planeCreator = new PlaneCreator();

        planeCreator.addPoint(pt1);
        assertFalse(planeCreator.isComputed());
        planeCreator.addPoint(pt2);
        assertFalse(planeCreator.isComputed());
        planeCreator.addPoint(pt3);
        assertFalse(planeCreator.isComputed());
        planeCreator.setHeight(DEFAULT_PLANE_HEIGHT);
        assertTrue(planeCreator.isComputed());

        assertNotNull(planeCreator.getPlaneCalibration());
        return planeCreator.getPlaneCalibration();
    }

    static public void checkPlane(PlaneCalibration planeCalibration) {
        Plane plane = planeCalibration.getPlane();
        assertNotNull(plane);
        assertEquals(expectedOrigin.x, plane.x, Double.MIN_VALUE);
        assertEquals(expectedOrigin.y, plane.y, Double.MIN_VALUE);
        assertEquals(expectedOrigin.z, plane.z, Double.MIN_VALUE);

        assertEquals(expectedNorm.x, plane.normal.x, Double.MIN_VALUE);
        assertEquals(expectedNorm.y, plane.normal.y, Double.MIN_VALUE);
        assertEquals(expectedNorm.z, plane.normal.z, Double.MIN_VALUE);

        assertEquals(planeCalibration.getHeight(), DEFAULT_PLANE_HEIGHT, Double.MIN_VALUE);
    }

}
