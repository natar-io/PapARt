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

import org.junit.Test;
import static org.junit.Assert.*;
import processing.core.PApplet;
import processing.core.PMatrix3D;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class PlaneAndProjectionCalibrationTest {

    HomographyCalibration instance;
    PApplet sketch;

    public PlaneAndProjectionCalibrationTest() {
    }

    @Test
    public void testCreation() {
        sketch = new Sketch();

        PlaneCalibration planeCalibration = new PlaneCalibration();
        planeCalibration.loadFrom(sketch, Common.currentPath + Common.PlaneCalibration);

        HomographyCalibration homographyCalibration = new HomographyCalibration();
        homographyCalibration.setMatrix(HomographyCalibrationTest.createDummyValues());

        PlaneAndProjectionCalibration papc = new PlaneAndProjectionCalibration();
        papc.setHomographyCalibration(homographyCalibration);
        papc.setPlaneCalibration(planeCalibration);

        papc.saveTo(sketch, Common.currentPath + Common.PlaneProjectionCalibration);
    }

    @Test
    public void testLoading() {
        sketch = new Sketch();
        PlaneAndProjectionCalibration papc = new PlaneAndProjectionCalibration();
        papc.loadFrom(sketch, Common.currentPath + Common.PlaneProjectionCalibration);
        PlaneCalibrationTest.checkPlane(papc.getPlaneCalibration());
        HomographyCalibrationTest.checkValuesOf(papc.getHomography());
    }

}
