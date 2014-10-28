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

import java.io.FileNotFoundException;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import toxi.geom.Matrix4x4;
import toxi.geom.Plane;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class PlaneCalibrationLegacy implements PConstants {

    protected PlaneThreshold planeThreshold;
    protected int currentPoint = 0;
    protected final String outputFileName;
    protected final PApplet parent;
    protected boolean planeSet = false;

    protected static final int planeOffsetText = 15;

    public PlaneCalibrationLegacy(PApplet parent, String outputFile) {
        this.parent = parent;
        this.outputFileName = outputFile;
        currentPoint = 0;
        planeThreshold = new PlaneThreshold();
    }

    public static PlaneCalibrationLegacy loadFrom(PApplet parent, String fileName) throws FileNotFoundException {
        String[] lines = parent.loadStrings(fileName);

        if (lines == null) {
            throw new FileNotFoundException(fileName);
        }

        Vec3D pos = new Vec3D(Float.parseFloat(lines[planeOffsetText + 0]),
                Float.parseFloat(lines[planeOffsetText + 1]),
                Float.parseFloat(lines[planeOffsetText + 2]));
        Vec3D norm = new Vec3D(Float.parseFloat(lines[planeOffsetText + 3]),
                Float.parseFloat(lines[planeOffsetText + 4]),
                Float.parseFloat(lines[planeOffsetText + 5]));
        float planeHeight = Float.parseFloat(lines[planeOffsetText + 6]);

        PlaneThreshold planeThreshold = new PlaneThreshold(pos, norm, planeHeight);
        PlaneCalibrationLegacy planeCalibration = new PlaneCalibrationLegacy(parent, fileName);
        planeCalibration.setPlane(planeThreshold);
        return planeCalibration;
    }

    public void saveTo(String filename) {
        String[] lines = new String[16 + 7];

        Plane plane = planeThreshold.getPlane();
        float height = planeThreshold.getHeight();

        System.out.println("Saving plane " + plane);

//        String[] lines = new String[7];
        lines[planeOffsetText + 0] = "" + plane.x;
        lines[planeOffsetText + 1] = "" + plane.y;
        lines[planeOffsetText + 2] = "" + plane.z;
        lines[planeOffsetText + 3] = "" + plane.normal.x;
        lines[planeOffsetText + 4] = "" + plane.normal.y;
        lines[planeOffsetText + 5] = "" + plane.normal.z;
        lines[planeOffsetText + 6] = "" + height;

        parent.saveStrings(filename, lines);
    }

    public void setPlane(PlaneThreshold p) {
        this.planeThreshold = p;
    }

    public PlaneThreshold plane() {
        return this.planeThreshold;
    }

}
