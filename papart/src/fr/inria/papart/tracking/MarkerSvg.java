/*
 */
package fr.inria.papart.tracking;

import java.util.ArrayList;
import java.util.HashMap;
import static processing.core.PApplet.println;
import static processing.core.PConstants.RECT;
import processing.core.PMatrix;
import processing.core.PMatrix2D;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PShapeSVG;
import processing.core.PVector;
import processing.data.XML;

public class MarkerSvg implements Cloneable {

    private final int id;
    private final PMatrix2D matrix = new PMatrix2D();
    private final PVector size = new PVector();
    private PVector[] corners = new PVector[4];
    private boolean cornersSet = false;

    public MarkerSvg(int id, PMatrix2D matrix, PVector size) {
        this.id = id;
        this.size.set(size);
        this.matrix.set(matrix);
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

    public static float pixelToMm() {
        return 25.4f / 90.0f;
    }

    public static float mmToPixel() {
        return 1.0f / pixelToMm();
    }

    static private void findMarkers(PShapeSVG shape, ArrayList<PShape> markers) {

        for (PShape child : shape.getChildren()) {
            findMarkers((PShapeSVG) child, markers);
        }

        if (shape.getKind() == RECT && shape.getName().startsWith("marker")) {
            markers.add(shape);
        }
    }

    static public HashMap<Integer, MarkerSvg> getMarkersFromSVG(XML xml) {

        float pageHeight = xml.getFloat("height");
//        System.out.println("Height : " + pageHeight);

        PShape svg = new PShapeSVG(xml);
        ArrayList<PShape> markersSVG = new ArrayList<>();
        findMarkers((PShapeSVG) svg, markersSVG);

//        ArrayList<MarkerSvg> markers = new ArrayList<>();
        HashMap<Integer, MarkerSvg> markers = new HashMap<>();

        for (PShape markerSvg : markersSVG) {

            int id = Integer.parseInt(markerSvg.getName().substring(6));

            float[] params = markerSvg.getParams();
            PVector size = new PVector(params[2], params[3]);

            PMatrix2D matrix = (PMatrix2D) getMatrix(markerSvg);

            matrix.m02 = matrix.m02 * pixelToMm();
            matrix.m12 = (pageHeight - matrix.m12) * pixelToMm();

            size.x = size.x * pixelToMm();
            size.y = size.y * pixelToMm();
            MarkerSvg marker = new MarkerSvg(id, matrix, size);
            markers.put(id, marker);
        }
        return markers;
    }

    private static PMatrix getMatrix(PShape shape) {

        PMatrix matrix = shape.getMatrix();

        boolean useParams = true;
        float[] params = null;

        try {
            params = shape.getParams();
        } catch (NullPointerException npe) {
            useParams = false;
        }

        if (matrix == null) {
            matrix = new PMatrix2D();
            if (useParams) {
                matrix.translate(params[0], params[1]);
            }
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
