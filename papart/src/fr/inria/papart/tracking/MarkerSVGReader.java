/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.tracking;

import java.util.ArrayList;
import static processing.core.PConstants.RECT;
import processing.core.PMatrix;
import processing.core.PMatrix2D;
import processing.core.PShape;
import processing.core.PVector;
import processing.data.XML;
import tech.lity.rea.svgextended.PShapeSVGExtended;

/**
 *
 * @author realitytech
 */
public class MarkerSVGReader {

    private XML xml;

    // Two variables for sizes: do we use millimeters. 
    // If not what is the DPI. 
    // Set to default legacy values, which changed with inkscape 0.92
    private boolean millimeterMode = false;
    private float dpi = 90.0f;
    float sheetWidthMm, sheetHeightMm;

    private float unitsToMM = PX_TO_MM_96_DPI;

    private static final float INCH_TO_MM = 25.4f;
    private static final float PX_TO_MM_96_DPI = INCH_TO_MM / 96f; // PX to MM  default inkscape
    private static final float PX_TO_MM_90_DPI = INCH_TO_MM / 90f; // PX to MM  default inkscape 0.91

//    private float pixelToMm() {
//        return INCH_TO_MM / dpi;
//    }
//
//    private float mmToPixel() {
//        return 1.0f / pixelToMm();
//    }

    public MarkerSVGReader(XML in) {
        xml = in;

        // All sizes are in millimeters now in inkscape ?
        checkMmOrPixelsSizes();
//        if (!this.millimeterMode) {
        checkDPI();
//        } else {
//            System.out.println("Markerboard: Millimeter mode.");
//        }

        sheetWidthMm = computeSizeMM(xml.getString("width"));
        sheetHeightMm = computeSizeMM(xml.getString("height"));
        
        System.out.println("Size of the markerboard: " + sheetWidthMm  + " " + sheetHeightMm);

        // Find if it uses any weird scales
        XML view = xml.getChild("sodipodi:namedview");
        if (view != null) {
            String units = view.getString("units");

            if (units == null) {
                units = view.getString("inkscape:document-units");
            }
            if (units != null && units.equals("px")) {
                String scale = view.getString("scale-x");
                if (scale == null) {
                    unitsToMM = PX_TO_MM_96_DPI; // default inkscape value...
                } else {
                    unitsToMM = Float.parseFloat(scale);
                }
            }
            if (units != null && units.equals("mm")) {
                unitsToMM = 1f;
            }
        }
    }

    private void checkMmOrPixelsSizes() {
        this.millimeterMode = xml.getString("width").endsWith("mm")
                && xml.getString("height").endsWith("mm");

    }

    private void checkDPI() {
        try {
            String v1[] = xml.getString("inkscape:version").split(" ");

            int main = 0;
            int second = 92;
            String v2;
            if (v1.length > 0) {
                v2 = v1[0];
            } else {
                v2 = xml.getString("inkscape:version");
            }
            String inkscapeVersion[] = v2.split("\\.");
            if (inkscapeVersion.length > 0) {
                main = Integer.parseInt(inkscapeVersion[0]);
                second = Integer.parseInt(inkscapeVersion[1]);
            }
            if (second <= 91) {
                // 96 DPI
                dpi = 96;
            }
            if (second >= 92 || main > 0) {
                // 90 DPI
                dpi = 90;
                System.out.println("Markerboard: 90 DPI inkscape file in pixels.");
            }
        } catch (Exception e) {
            System.err.println("Cannot guess version of SVG file." + e);
        }
    }

    public MarkerList getList() {
        PShape svg = new PShapeSVGExtended(xml);
        ArrayList<PShape> markersSVG = new ArrayList<>();
        findMarkers((PShapeSVGExtended) svg, markersSVG);

        MarkerList markers = new MarkerList();
        markers.setSheetSize(sheetWidthMm, sheetHeightMm);

        for (PShape markerSvg : markersSVG) {
            int id = Integer.parseInt(markerSvg.getName().substring(6));

            MarkerSvg marker = createMarker(markerSvg, id);

            markers.put(id, marker);
        }
        return markers;
    }

    private MarkerSvg createMarker(PShape markerSvg, int id) {
        float[] params = markerSvg.getParams();

        PVector size = new PVector(params[2], params[3]);

        // SVG standard has a going down Y axis. (unlike inkscape)
        PMatrix2D matrix = (PMatrix2D) getMatrix(markerSvg);
        matrix.scale(1, -1);
//            matrix.translate(0, -size.y);
//        if (!millimeterMode) {
        matrix.m02 = matrix.m02 * unitsToMM;
        matrix.m12 = matrix.m12 * unitsToMM;
//            matrix.m12 = (pageHeight - matrix.m12) * pixelToMm();

        size.x = size.x * unitsToMM;
        size.y = size.y * unitsToMM;
//        }
        System.out.println("marker found:  id: " + id + " , size: " + size);

        matrix.print();
        MarkerSvg marker = new MarkerSvg(id, matrix.get(), size);

        marker.corners[0] = new PVector(matrix.m02, matrix.m12);
        matrix.translate(size.x, 0);
        marker.corners[1] = new PVector(matrix.m02, matrix.m12);
        matrix.translate(0, -size.y);
        marker.corners[2] = new PVector(matrix.m02, matrix.m12);
        matrix.translate(-size.x, 0);
        marker.corners[3] = new PVector(matrix.m02, matrix.m12);
        marker.cornersSet = true;
        return marker;
    }

    private PMatrix getMatrix(PShape shape) {

        boolean useParams = true;
        float[] params = null;

        try {
            params = shape.getParams();
        } catch (NullPointerException npe) {
            useParams = false;
        }

        PMatrix matrix = ((PShapeSVGExtended) shape).getMatrix();
        if (matrix == null) {
            matrix = new PMatrix2D();
        } else {
            System.out.println("Matrix found, for  " + shape);
            // use a copy
            matrix = matrix.get();
        }
        
        if (useParams) {
            System.out.println("Translate " + params[0] + " " + params[1]);
            matrix.translate(params[0], params[1]);
        }

        // is root.
        if (shape.getParent() == null) {
            System.out.println("Current matrix: " + shape);
            ((PMatrix2D) matrix).print();
            return matrix;
        }

        PMatrix2D parentMat = (PMatrix2D) getMatrix(shape.getParent());

        System.out.println("parent matrix: ");
        parentMat.print();
        matrix.preApply(parentMat);

        return matrix;
    }

    void findMarkers(PShapeSVGExtended shape, ArrayList<PShape> markers) {
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

//    private float computeSizePx(String heightText) {
//        if (heightText.endsWith("mm")) {
//            String value = heightText.substring(0, heightText.indexOf("mm"));
//            return Float.parseFloat(value) * mmToPixel();
//        } else {
//            return Float.parseFloat(heightText);
//        }
//    }

    private float computeSizeMM(String heightText) {
        if (heightText.endsWith("mm")) {
            String value = heightText.substring(0, heightText.indexOf("mm"));
            return Float.parseFloat(value);
        } else {
            return Float.parseFloat(heightText)  * unitsToMM;
        }
    }

}
