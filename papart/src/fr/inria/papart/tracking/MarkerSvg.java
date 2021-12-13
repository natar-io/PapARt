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
package fr.inria.papart.tracking;

import java.util.ArrayList;
import java.util.HashMap;
import org.bytedeco.javacv.Marker;
import static processing.core.PApplet.println;
import static processing.core.PConstants.RECT;
import processing.core.PMatrix;
import processing.core.PMatrix2D;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PVector;
import processing.data.XML;
import tech.lity.rea.svgextended.PShapeSVGExtended;

public class MarkerSvg implements Cloneable {

    private final int id;
    private final PMatrix2D matrix;
    private final PVector size;
    protected PVector[] corners = new PVector[4];
    protected boolean cornersSet = false;

    public MarkerSvg(int id, PMatrix2D matrix, PVector size) {
        this.id = id;
        this.size = size.get();
        this.matrix = matrix.get();
    }

    public String toString() {
        return "mid: " + id + " corners: "
                + corners[0].x + " " + corners[0].y + "\n"
                + corners[1].x + " " + corners[1].y + "\n"
                + corners[2].x + " " + corners[2].y + "\n"
                + corners[3].x + " " + corners[3].y + "\n";
    }

    public Marker copyAsMarker() {
        double[] corners = new double[this.corners.length * 4];
        int k = 0;
        for (int i = 0; i < this.corners.length; i++) {
            corners[k++] = this.corners[i].x;
            corners[k++] = this.corners[i].y;
        }
        return new org.bytedeco.javacv.Marker(id, corners, 1.0);
    }

    public int getId() {
        return id;
    }

    public PMatrix2D getMatrix() {
        return matrix;
    }

    public PVector getSize() {
        return size;
    }

    public void computeCorners() {
        if (!cornersSet) {
            PVector c1 = new PVector(0, 0);
            PVector c2 = new PVector(size.x, 0);
            PVector c3 = new PVector(size.x, -size.y);
            PVector c4 = new PVector(0, -size.y);

//            PVector pos = new PVector(matrix.m02, matrix.m12);
//            PVector c1 = PVector.add(pos, new PVector(0, 0));
//            PVector c2 = PVector.add(pos, new PVector(size.x, 0));
//            PVector c3 = PVector.add(pos, new PVector(size.x, size.y));
//            PVector c4 = PVector.add(pos, new PVector(0, size.y));
            PVector c1T = new PVector();
            PVector c2T = new PVector();
            PVector c3T = new PVector();
            PVector c4T = new PVector();

            matrix.mult(c1, c1T);
            matrix.mult(c2, c2T);
            matrix.mult(c3, c3T);
            matrix.mult(c4, c4T);
            corners[0] = c1T;
            corners[1] = c2T;
            corners[2] = c3T;
            corners[3] = c4T;
        }
    }

    public PVector[] getCorners() {
        computeCorners();
        return corners;
    }

    public PVector getCenter() {
        double x = 0, y = 0;
        computeCorners();
        for (int i = 0; i < 4; i++) {
            x += corners[i].x;
            y += corners[i].y;
        }
        x /= 4;
        y /= 4;
        return new PVector((float) x, (float) y);
    }

}
