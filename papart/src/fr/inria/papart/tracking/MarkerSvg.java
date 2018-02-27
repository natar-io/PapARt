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
    private PVector[] corners = new PVector[4];
    private boolean cornersSet = false;

    public MarkerSvg(int id, PMatrix2D matrix, PVector size) {
        this.id = id;
        this.size = size.get();
        this.matrix = matrix.get();
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
        return new PVector((float) x, (float)y);
    }

    /**
     * Check pixel resolution - TODO
     *
     * @return
     */
    public static float pixelToMm() {
//        return 25.4f / 96.0f;
        return 25.4f / 90.0f;
    }

    public static float mmToPixel() {
        return 1.0f / pixelToMm();
    }

    static private void findMarkers(PShapeSVGExtended shape, ArrayList<PShape> markers) {
        try {
            for (PShape child : shape.getChildren()) {
                findMarkers((PShapeSVGExtended) child, markers);
            }
        } catch (NullPointerException npe) {
            // Sometimes no child causes a null pointer exception.
        }

        if (shape.getKind() == RECT && shape.getName().startsWith("marker")) {
            markers.add(shape);
        }
    }

    static private float computeSize(String heightText) {

        if (heightText.endsWith("mm")) {
            String value = heightText.substring(0, heightText.indexOf("mm"));
            return Float.parseFloat(value) * mmToPixel();
        } else {
            return Float.parseFloat(heightText);
        }

    }

    static public MarkerList getMarkersFromSVG(XML xml) {

        float pageHeight = computeSize(xml.getString("height"));
//        System.out.println("Height : " + pageHeight);

        PShape svg = new PShapeSVGExtended(xml);
        ArrayList<PShape> markersSVG = new ArrayList<>();
        findMarkers((PShapeSVGExtended) svg, markersSVG);

//        ArrayList<MarkerSvg> markers = new ArrayList<>();
        MarkerList markers = new MarkerList();

        float sheetWidth = computeSize(xml.getString("width")) * pixelToMm();
        float sheetHeight = computeSize(xml.getString("height")) * pixelToMm();
        markers.setSheetSize(sheetWidth, sheetHeight);

        for (PShape markerSvg : markersSVG) {

            int id = Integer.parseInt(markerSvg.getName().substring(6));

            float[] params = markerSvg.getParams();

            PVector size = new PVector(params[2], params[3]);

            // SVG standard has a going down Y axis. 
            PMatrix2D matrix = (PMatrix2D) getMatrix(markerSvg);
            matrix.scale(1, -1);
//            matrix.translate(0, -size.y);

            matrix.m02 = matrix.m02 * pixelToMm();
            matrix.m12 = matrix.m12 * pixelToMm();
//            matrix.m12 = (pageHeight - matrix.m12) * pixelToMm();

            size.x = size.x * pixelToMm();
            size.y = size.y * pixelToMm();
            MarkerSvg marker = new MarkerSvg(id, matrix.get(), size);

            marker.corners[0] = new PVector(matrix.m02, matrix.m12);
            matrix.translate(size.x, 0);
            marker.corners[1] = new PVector(matrix.m02, matrix.m12);
            matrix.translate(0, -size.y);
            marker.corners[2] = new PVector(matrix.m02, matrix.m12);
            matrix.translate(-size.x, 0);
            marker.corners[3] = new PVector(matrix.m02, matrix.m12);
            marker.cornersSet = true;

            markers.put(id, marker);

        }
        return markers;
    }

    private static PMatrix getMatrix(PShape shape) {

        PMatrix matrix = ((PShapeSVGExtended) shape).getMatrix();

        boolean useParams = true;
        float[] params = null;

        try {
            params = shape.getParams();
        } catch (NullPointerException npe) {
            useParams = false;
        }

        if (matrix == null) {
            matrix = new PMatrix2D();

        }
        if (useParams) {
            matrix.translate(params[0], params[1]);
        }

        // is root.
        if (shape.getParent() == null) {
            return matrix;
        }

        PMatrix2D parentMat = (PMatrix2D) getMatrix(shape.getParent());
        matrix.preApply(parentMat);

        return matrix;
    }
}
