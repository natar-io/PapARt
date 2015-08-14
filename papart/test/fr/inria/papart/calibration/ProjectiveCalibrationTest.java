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

import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.calibration.Calibration;
import fr.inria.papart.Sketch;
import org.junit.Test;
import static org.junit.Assert.*;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.data.XML;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class ProjectiveCalibrationTest {

    ProjectiveDeviceCalibration instance;
    PApplet sketch;

    @Test
    public void CreateValues() {
        sketch = new Sketch();         String[] args = new String[]{"--present", "test.fr.inria.papart.calibration.ProjectiveCalibrationTest"};         PApplet.runSketch(args, sketch);

        ProjectiveDeviceCalibration calibNoExtr;
        calibNoExtr = new ProjectiveDeviceCalibration();
        calibNoExtr.setIntrinsics(createDummyValues());
        calibNoExtr.setExtrinsics(identity());
        calibNoExtr.saveTo(sketch, Common.currentPath + Common.ProjectiveCalibrationNoExtrinsics);

        ProjectiveDeviceCalibration calibExtr;
        calibExtr = new ProjectiveDeviceCalibration();
        calibExtr.setIntrinsics(createDummyValues());
        calibExtr.setExtrinsics(createDummyValues());
        calibExtr.saveTo(sketch, Common.currentPath + Common.ProjectiveCalibrationExtrinsics);
    }

    PMatrix3D identity() {
        PMatrix3D mat = new PMatrix3D();
        mat.reset();
        return mat;
    }

    PMatrix3D dummyMat() {
        return new PMatrix3D(0, 1, 2, 3,
                4, 5, 6, 7,
                8, 9, 10, 11,
                12, 13, 14, 15);
    }

    @Test
    public void checkLoad() {
        sketch = new Sketch();
        String[] args = new String[]{"--present", "test.fr.inria.papart.calibration.ProjectiveCalibrationTest"};
        PApplet.runSketch(args, sketch);
        
        instance = new ProjectiveDeviceCalibration();
        instance.loadFrom(sketch, Common.currentPath + Common.ProjectiveCalibrationNoExtrinsics);

        assertFalse(instance.hasExtrinsics());
        checkValuesOf(instance.getIntrinsics());

        instance.loadFrom(sketch, Common.currentPath + Common.ProjectiveCalibrationExtrinsics);

        assertTrue(instance.hasExtrinsics());
        checkValuesOf(instance.getIntrinsics());
        checkValuesOf(instance.getExtrinsics());
    }

    public static PMatrix3D createDummyValues() {
        float[] dummyValues = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        PMatrix3D mat = new PMatrix3D();
        mat.set(dummyValues);
        return mat;
    }

    public static void checkValuesOf(PMatrix3D homograpy) {
        float[] dummyValues = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        float[] actualValues = new float[16];
        homograpy.get(actualValues);
        assertArrayEquals(dummyValues, actualValues, Float.MIN_VALUE);
    }

//    @Test
//    public void checkReplaceIn() {
//        sketch = new Sketch();         String[] args = new String[]{"--present", "test.fr.inria.papart.calibration.ProjectiveCalibrationTest"};         PApplet.runSketch(args, sketch);
//        instance = new HomographyCalibration();
//        setFakeMatrix();
//        instance.replaceIn(sketch, Common.currentPath + Common.HomographyCalibrationCopy);
//
//        HomographyCalibration instance2 = new HomographyCalibration();
//        instance2.loadFrom(sketch,
//                Common.currentPath + Common.HomographyCalibrationCopy);
//
//        float[] valuesInstance1 = new float[16];
//        float[] valuesInstance2 = new float[16];
//
//        instance.getHomography().get(valuesInstance1);
//        instance2.getHomography().get(valuesInstance2);
//
//        assertArrayEquals(valuesInstance1, valuesInstance2, Float.MIN_VALUE);
//    }
//
//    private void setFakeMatrix() {
//        instance.setMatrix(new PMatrix3D());
//    }
//    @Test
//    public void checkXML() {
//        sketch = new Sketch();         String[] args = new String[]{"--present", "test.fr.inria.papart.calibration.ProjectiveCalibrationTest"};         PApplet.runSketch(args, sketch);
//        XML root = sketch.loadXML(Common.currentPath + Common.HomographyCalibration);
//        assertTrue(root.getName() == Calibration.CALIBRATION_XML_NAME);
//
//        XML homographyNode = root.getChild(HomographyCalibration.HOMOGRAPHY_XML_NAME);
//        assertNotNull(homographyNode);
//    }
}
