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

import tech.lity.rea.nectar.calibration.HomographyCalibration;
import fr.inria.papart.calibration.files.Calibration;
import fr.inria.papart.Sketch;
import org.junit.Test;
import static org.junit.Assert.*;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.data.XML;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class HomographyCalibrationTest {

    static final int NB_POINTS = 10;
    static final int DIMS = 3;

    HomographyCalibration instance;
    PApplet sketch;

    @Test
    public void CreateValues() {
        sketch = new Sketch();         String[] args = new String[]{"--present", "test.fr.inria.papart.calibration.ProjectiveCalibrationTest"};         PApplet.runSketch(args, sketch);
        instance = new HomographyCalibration();
        instance.setMatrix(createDummyValues());
        instance.saveTo(sketch, Common.currentPath + Common.HomographyCalibration);
    }
    
    @Test
    public void checkLoadAndSave() {
        sketch = new Sketch();         String[] args = new String[]{"--present", "test.fr.inria.papart.calibration.ProjectiveCalibrationTest"};         PApplet.runSketch(args, sketch);
        instance = new HomographyCalibration();
        instance.loadFrom(sketch, Common.currentPath + Common.HomographyCalibration);
        PMatrix3D homograpy = instance.getHomography();

        checkValuesOf(homograpy);

        instance.saveTo(sketch, Common.currentPath + Common.HomographyCalibrationCopy);
        instance.loadFrom(sketch, Common.currentPath + Common.HomographyCalibrationCopy);
        homograpy = instance.getHomography();
        checkValuesOf(homograpy);
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

    @Test
    public void checkReplaceIn() {
        sketch = new Sketch();         String[] args = new String[]{"--present", "test.fr.inria.papart.calibration.ProjectiveCalibrationTest"};         PApplet.runSketch(args, sketch);
        instance = new HomographyCalibration();
        setFakeMatrix();
        instance.replaceIn(sketch, Common.currentPath + Common.HomographyCalibrationCopy);

        HomographyCalibration instance2 = new HomographyCalibration();
        instance2.loadFrom(sketch,
                Common.currentPath + Common.HomographyCalibrationCopy);

        float[] valuesInstance1 = new float[16];
        float[] valuesInstance2 = new float[16];

        instance.getHomography().get(valuesInstance1);
        instance2.getHomography().get(valuesInstance2);

        assertArrayEquals(valuesInstance1, valuesInstance2, Float.MIN_VALUE);
    }

    private void setFakeMatrix() {
        instance.setMatrix(new PMatrix3D());
    }

    @Test
    public void checkXML() {
        sketch = new Sketch();         String[] args = new String[]{"--present", "test.fr.inria.papart.calibration.ProjectiveCalibrationTest"};         PApplet.runSketch(args, sketch);
        XML root = sketch.loadXML(Common.currentPath + Common.HomographyCalibration);
        assertTrue(root.getName() == Calibration.CALIBRATION_XML_NAME);

        XML homographyNode = root.getChild(HomographyCalibration.HOMOGRAPHY_XML_NAME);
        assertNotNull(homographyNode);
    }

}
