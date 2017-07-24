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

import fr.inria.papart.calibration.files.HomographyCalibration;
import fr.inria.papart.calibration.files.PlaneCalibration;
import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.Sketch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import processing.core.PApplet;
import processing.core.PMatrix3D;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */
public class PlaneAndProjectionCalibrationTest {

    HomographyCalibration instance;
    PApplet sketch;

    public PlaneAndProjectionCalibrationTest() {
    }

    @Test
    public void testCreation() {
        sketch = new Sketch();         String[] args = new String[]{"--present", "test.fr.inria.papart.calibration.ProjectiveCalibrationTest"};         PApplet.runSketch(args, sketch);

        PlaneCalibration planeCalibration = new PlaneCalibration();
        planeCalibration.loadFrom(sketch, Common.currentPath + Common.PlaneCalibration);

        HomographyCalibration homographyCalibration = new HomographyCalibration();
        homographyCalibration.setMatrix(HomographyCalibrationTest.createDummyValues());

        PlaneAndProjectionCalibration papc = new PlaneAndProjectionCalibration();
        papc.setHomography(homographyCalibration);
        papc.setPlane(planeCalibration);

        papc.saveTo(sketch, Common.currentPath + Common.PlaneProjectionCalibration);
    }

    @Test
    public void testLoading() {
        sketch = new Sketch();         String[] args = new String[]{"--present", "test.fr.inria.papart.calibration.ProjectiveCalibrationTest"};         PApplet.runSketch(args, sketch);
        PlaneAndProjectionCalibration papc = new PlaneAndProjectionCalibration();
        papc.loadFrom(sketch, Common.currentPath + Common.PlaneProjectionCalibration);
        PlaneCalibrationTest.checkPlane(papc.getPlaneCalibration());
        HomographyCalibrationTest.checkValuesOf(papc.getHomography());
    }

}
