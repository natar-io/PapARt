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
package fr.inria.papart.scanner;

import fr.inria.papart.Sketch;
import org.junit.Test;
import static org.junit.Assert.*;
import processing.core.PApplet;
import static processing.core.PApplet.println;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */
public class DecodedCodeTest {

    static final String currentPath = "test/fr/inria/papart/scanner/";
    
    DecodedCode decodedCode;
    int width = 300;
    int height = 200;
    private Sketch sketch;
    String fileName = "decodedTest";
    
    public DecodedCodeTest() {

    }

    @Test
    public void createCode() {
        decodedCode = new DecodedCode(width, height);
        assertTrue(decodedCode.decodedX.length == width * height);
        assertTrue(decodedCode.decodedY.length == width * height);
        assertTrue(decodedCode.validMask.length == width * height);
    }

    
    int offset1 = 10;
    int value1 = 50;
    int value2 = 80;

    @Test
    public void testSaveLoad() {
        sketch = new Sketch();         String[] args = new String[]{"--present", "test.fr.inria.papart.calibration.ProjectiveCalibrationTest"};         PApplet.runSketch(args, sketch);
        decodedCode = new DecodedCode(10, 10);


        PImage ref = sketch.createImage(width, width, PApplet.RGB);
        decodedCode.setRefImage(ref);
        decodedCode.decodedX[offset1] = value1;
        decodedCode.decodedY[offset1] = value2;
        decodedCode.validMask[offset1] = true;
        decodedCode.validMask[0] = false;

        decodedCode.saveTo(sketch, currentPath + fileName);
        
        DecodedCode decodedCode2 = DecodedCode.loadFrom(sketch, currentPath + fileName);
        
        println(decodedCode2.decodedX[offset1]);
        
        assertTrue(decodedCode2.decodedX[offset1] == value1);
        assertTrue(decodedCode2.decodedY[offset1] == value2);
        assertTrue(decodedCode2.validMask[offset1] == true);
        assertTrue(decodedCode2.validMask[0] == false);
        assertTrue(decodedCode2.refImage.width == ref.width);
        assertTrue(decodedCode2.refImage.height == ref.height);
    }
    
    @Test
    public void testLoadFrom() {
    }


}
