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

import fr.inria.papart.Sketch;
import fr.inria.papart.scanner.GrayCode;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class CameraProjectorSyncTest {

    int defaultDuration = 500;
    int defaultCaptureTime = 300;
    int defaultDelay = 100;
    int defaultDecode = GrayCode.DECODE_ABS;
    int defaultDecodeValue = 100;

    public CameraProjectorSyncTest() {
    }

    /**
     * Test of isValid method, of class CameraProjectorSync.
     */
    @Test
    public void testIsValid() {
        CameraProjectorSync cps = createInstance();
        assertTrue(cps.isValid());
        testDelay(cps);
        testCaptureTime(cps);
        testDisplayDuration(cps);
        testDecodeParams(cps);
    }

    private void testCaptureTime(CameraProjectorSync cps) {
        cps.setCaptureTime(-100);
        assertFalse(cps.isValid());
        cps.setCaptureTime(defaultCaptureTime);
        assertTrue(cps.isValid());
    }

    private void testDelay(CameraProjectorSync cps) {
        cps.setDelay(-100);
        assertFalse(cps.isValid());
        cps.setDelay(defaultDelay);
        assertTrue(cps.isValid());
    }

    private void testDisplayDuration(CameraProjectorSync cps) {
        cps.setDisplayDuration(0);
        assertFalse(cps.isValid());
        cps.setDisplayDuration(-10);
        assertFalse(cps.isValid());
        cps.setDisplayDuration(defaultDuration);
        assertTrue(cps.isValid());
    }

    private void testDecodeParams(CameraProjectorSync cps) {

        cps.setDecodeParameters(GrayCode.DECODE_ABS, 100);
        assertTrue(cps.isValid());
        cps.setDecodeParameters(GrayCode.DECODE_REF, 100);
        assertTrue(cps.isValid());
        cps.setDecodeParameters(GrayCode.DECODE_NOT_SET, 100);
        assertFalse(cps.isValid());

        cps.setDecodeParameters(GrayCode.DECODE_REF, -10);
        assertFalse(cps.isValid());

        cps.setDecodeParameters(GrayCode.DECODE_REF, 200);
        assertTrue(cps.isValid());
    }

    private CameraProjectorSync createInstance() {
        CameraProjectorSync cps
                = new CameraProjectorSync(defaultDuration,
                        defaultCaptureTime, defaultDelay);
        cps.setDecodeParameters(defaultDecode, defaultDecodeValue);
        return cps;
    }

    private CameraProjectorSync createInstance2() {
        CameraProjectorSync cps
                = new CameraProjectorSync(defaultDuration * 2,
                        defaultCaptureTime * 2, defaultDelay * 2);
         cps.setDecodeParameters(GrayCode.DECODE_REF, defaultDecodeValue*2);
        return cps;
    }

    /**
     * Test of addTo method, of class CameraProjectorSync.
     */
    @Test
    public void testAddTo() {
        CameraProjectorSync cps = createInstance();
        Sketch sketch = new Sketch();
        cps.saveTo(sketch, Common.currentPath + Common.CameraProjectorSync);

        CameraProjectorSync cps2 = new CameraProjectorSync(0, 0, 0);
        cps2.loadFrom(sketch, Common.currentPath + Common.CameraProjectorSync);

        assertEquals(cps2.getCaptureTime(), cps.getCaptureTime());
        assertEquals(cps2.getDelay(), cps.getDelay());
        assertEquals(cps2.getDisplayDuration(), cps.getDisplayDuration());
    }

    /**
     * Test of replaceIn method, of class CameraProjectorSync.
     */
    @Test
    public void testReplaceIn() {

        Sketch sketch = new Sketch();

        CameraProjectorSync cps = createInstance2();
        cps.replaceIn(sketch, Common.currentPath + Common.CameraProjectorSync);

        CameraProjectorSync cps2 = new CameraProjectorSync(0, 0, 0);
        cps2.loadFrom(sketch, Common.currentPath + Common.CameraProjectorSync);

        assertEquals(cps2.getCaptureTime(), cps.getCaptureTime());
        assertEquals(cps2.getDelay(), cps.getDelay());
        assertEquals(cps2.getDisplayDuration(), cps.getDisplayDuration());
    }

    /**
     * Test of loadFrom method, of class CameraProjectorSync.
     */
    @Test
    public void testLoadFrom() {

    }

}
