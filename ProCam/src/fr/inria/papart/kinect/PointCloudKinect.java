
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

import codeanticode.glgraphics.GLModel;
import java.util.ArrayList;
import java.util.Arrays;
import javax.media.opengl.GL;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;
import toxi.geom.Triangle3D;
import toxi.geom.Vec3D;
import toxi.geom.mesh.OBJWriter;

/**
 *
 * @author jeremy
 */
public class PointCloudKinect {

    protected GLModel model;
    private Kinect kinect;
    private PApplet parent;
    protected int nbToDraw = 0;
    protected GLModel lastModel = model;
    private GLModel triangleModel = null;
    private int[] indicesMap;
    private int[] indices;
    private float[] normals;
    private boolean[] valid;
    private Vec3D[] points;
    private PImage colors;
    private int skip;

    public PointCloudKinect(PApplet parent, Kinect kinect) {
        this.kinect = kinect;
        this.parent = parent;

        // TODO: try pointSprites ? -- not working on MacBook
        model = new GLModel(parent, Kinect.KINECT_SIZE, GLModel.POINTS, GLModel.STREAM);

//        model = new GLModel(parent, Kinect.size, GLModel.POINT_SPRITES, GLModel.STREAM);
//        model.setSpriteSize(80, 400);

        model.initColors();
    }

    public GLModel getModel() {
        return this.model;
    }

    public void updateColorsProcessing() {

        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        PImage colors = kinect.getDepthColor();
        lastModel = model;
        model.beginUpdateVertices();
        nbToDraw = 0;
        for (int i = 0; i < Kinect.KINECT_SIZE; i++) {

            if (valid[i]) {
                Vec3D p = points[i];
                model.updateVertex(nbToDraw++, p.x, p.y, -p.z);
            }
        }
        model.endUpdateVertices();

        if (colors != null) {
            colors.loadPixels();
            model.beginUpdateColors();
            int k = 0;
            for (int i = 0; i < Kinect.KINECT_SIZE; i++) {
                if (valid[i]) {
                    int c = colors.pixels[i];

                    model.updateColor(k++,
                            (c >> 16) & 0xFF,
                            (c >> 8) & 0xFF,
                            c & 0xFF);
                }
            }
            model.endUpdateColors();
        }

    }

    private void initTriangleModel() {
        triangleModel = new GLModel(parent, Kinect.KINECT_SIZE, GLModel.TRIANGLES, GLModel.STREAM);
        triangleModel.initIndices(Kinect.KINECT_SIZE * 6, GLModel.STREAM);
        triangleModel.initNormals();
        triangleModel.initColors();
        indicesMap = new int[Kinect.KINECT_SIZE];
        indices = new int[Kinect.KINECT_SIZE * 6];
        normals = new float[Kinect.KINECT_SIZE * 4]; // 1 normal per vertex ?
    }

    public void updateTrianglesColorsProcessing() {

        if (triangleModel == null) {
            initTriangleModel();
        } else {
//            Arrays.fill(indicesMap, 0);
//            Arrays.fill(indices, 0);
        }

        lastModel = triangleModel;

        valid = kinect.getValidPoints();
        points = kinect.getDepthPoints();
        colors = kinect.getDepthColor();

        triangleModel.beginUpdateVertices();
        nbToDraw = 0;

        skip = kinect.getCurrentSkip();

        ///////////////  Vertices

        nbToDraw = 0;
        for (int i = 0; i < Kinect.KINECT_SIZE; i++) {

            if (valid[i]) {
                Vec3D p = points[i];
//                PVector p2 = new PVector(p.x, p.y, p.z);
//                transfo.mult(p2, p2);

                indicesMap[i] = nbToDraw;
                triangleModel.updateVertex(nbToDraw++, p.x, p.y, -p.z);

//                triangleModel.updateVertex(nbToDraw++, p2.x, p2.y, -p2.z);

            }
        }
        triangleModel.endUpdateVertices();


        ///////////////  Triangles  ////////////////
        int currentIndex = 0;

        for (int y = skip; y < Kinect.KINECT_HEIGHT; y += skip) {
            for (int x = skip; x < Kinect.KINECT_WIDTH; x += skip) {

                int offset = y * Kinect.KINECT_WIDTH + x;

                if (valid[offset]) {
                    currentIndex = checkAndCreateTriangle(x, y, currentIndex);
                }
            }
        }

//        triangleModel.beginUpdateIndices();
        triangleModel.updateIndices(indices, currentIndex);
//        triangleModel.endUpdateIndices();

        triangleModel.updateNormals(normals);
        
        nbToDraw = currentIndex;


        ///////////////  Colors 
        if (colors != null) {
            colors.loadPixels();
            triangleModel.beginUpdateColors();
            int k = 0;
            for (int i = 0; i < Kinect.KINECT_SIZE; i++) {
                if (valid[i]) {
                    int c = colors.pixels[i];

                    triangleModel.updateColor(k++,
                            (c >> 16) & 0xFF,
                            (c >> 8) & 0xFF,
                            c & 0xFF);

//                    triangleModel.updateColor(k++, 0, 255, 0);
                }
            }
            triangleModel.endUpdateColors();
        }
    }

    private int checkAndCreateTriangle(int x, int y, int currentIndex) {

        // Triangles indices this way. A is current
        // D B 
        // C A

        final float maxDist = 10.0f;

        int offsetB = ((y - skip) * Kinect.KINECT_WIDTH) + x;
        int offsetA = (y * Kinect.KINECT_WIDTH) + x;
        int offsetC = offsetA - skip;
        int offsetD = offsetB - skip;

        if (valid[offsetA]
                && valid[offsetB]
                && valid[offsetC]
                && valid[offsetD]) {

            if (points[offsetA].distanceTo(points[offsetB]) < maxDist
                    && points[offsetA].distanceTo(points[offsetC]) < maxDist
                    && points[offsetA].distanceTo(points[offsetD]) < maxDist) {


                // Get the normal !
                Triangle3D triangle1 = new Triangle3D(points[offsetB], points[offsetD], points[offsetC]);
                Triangle3D triangle2 = new Triangle3D(points[offsetA], points[offsetB], points[offsetC]);

                triangle1.computeNormal();
                triangle2.computeNormal();

                int vertexIndex = indicesMap[offsetB];
                normals[vertexIndex * 4 + 0] = triangle1.normal.x;
                normals[vertexIndex * 4 + 1] = triangle1.normal.y;
                normals[vertexIndex * 4  + 2] = triangle1.normal.z;  
                normals[vertexIndex * 4  + 3] = 0;  
                
                vertexIndex = indicesMap[offsetC];
                normals[vertexIndex * 4  + 0] = triangle1.normal.x;
                normals[vertexIndex * 4  + 1] = triangle1.normal.y;
                normals[vertexIndex * 4  + 2] = triangle1.normal.z;      
                normals[vertexIndex * 4  + 3] = 0;      
                
                vertexIndex = indicesMap[offsetD];
                normals[vertexIndex * 4  + 0] = triangle1.normal.x;
                normals[vertexIndex * 4  + 1] = triangle1.normal.y;
                normals[vertexIndex * 4  + 2] = triangle1.normal.z;  
                normals[vertexIndex * 4  + 3] = 0;  
                
                vertexIndex = indicesMap[offsetA];
                normals[vertexIndex * 4  + 0] = triangle2.normal.x;
                normals[vertexIndex * 4  + 1] = triangle2.normal.y;
                normals[vertexIndex * 4  + 2] = triangle2.normal.z;  
                normals[vertexIndex * 4  + 3] = 0;  

                indices[currentIndex++] = indicesMap[offsetB];
                indices[currentIndex++] = indicesMap[offsetD];
                indices[currentIndex++] = indicesMap[offsetC];
                indices[currentIndex++] = indicesMap[offsetA];
                indices[currentIndex++] = indicesMap[offsetB];
                indices[currentIndex++] = indicesMap[offsetC];
//                
            }
//            model.updateIndices(indicesMap);
        }

        return currentIndex;
    }

    public void update() {

        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        PImage colors = kinect.getDepthColor();
        lastModel = model;
        model.beginUpdateVertices();
        nbToDraw = 0;
        for (int i = 0; i < Kinect.KINECT_SIZE; i++) {

            if (valid[i]) {
                Vec3D p = points[i];
//                if (plane.orientation(p)) {
//                if (calib.plane().hasGoodOrientationAndDistance(p)) {
//                    if (isInside(calib.project(p), 0.f, 1.f, 0.05f)) {
                model.updateVertex(nbToDraw++, p.x, p.y, -p.z);
//                    }

//                } else {
//                    valid[i] = false;
//                }
            }
        }

        model.endUpdateVertices();

        if (colors != null) {
            colors.loadPixels();
            model.beginUpdateColors();
            int k = 0;
            for (int i = 0; i < Kinect.KINECT_SIZE; i++) {
                if (valid[i]) {
                    int c = colors.pixels[i];

                    model.updateColor(k++,
                            (c >> 16) & 0xFF,
                            (c >> 8) & 0xFF,
                            c & 0xFF);
                }
            }
            model.endUpdateColors();
        }
    }

    public void updateMultiTouch() {

        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        PImage colors = kinect.getDepthColor();
        lastModel = model;
        model.beginUpdateVertices();
        nbToDraw = 0;
        for (int i = 0; i < Kinect.KINECT_SIZE; i++) {

            if (valid[i]) {
                Vec3D p = points[i];
                model.updateVertex(nbToDraw++, p.x, p.y, -p.z);
            }
        }
        model.endUpdateVertices();

        colors.loadPixels();
        model.beginUpdateColors();
        int k = 0;
        for (int i = 0; i < Kinect.KINECT_SIZE; i++) {
            if (valid[i]) {
                int c = colors.pixels[i];

                model.updateColor(k++,
                        (c >> 16) & 0xFF,
                        (c >> 8) & 0xFF,
                        c & 0xFF);
            }
        }
        model.endUpdateColors();
    }

    public void updateMultiTouch(Vec3D[] projectedPoints) {

        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        lastModel = model;
        model.beginUpdateVertices();
        nbToDraw = 0;
        for (int i = 0; i < Kinect.KINECT_SIZE; i++) {

            if (valid[i]) {
                Vec3D p = points[i];
                model.updateVertex(nbToDraw++, p.x, p.y, -p.z);
            }
        }
        model.endUpdateVertices();

        model.beginUpdateColors();
        int k = 0;
        for (int i = 0; i < Kinect.KINECT_SIZE; i++) {
            if (valid[i]) {

                int c = parent.color(255, 255, 255);

                if (Kinect.connectedComponent[i] > 0) {
                    switch (Kinect.connectedComponent[i]) {
                        case 1:
                            c = parent.color(100, 200, 100);
                            break;
                        case 2:
                            c = parent.color(0, 200, 100);
                            break;
                        case 3:
                            c = parent.color(200, 200, 100);
                            break;
                        case 4:
                            c = parent.color(0, 0, 200);
                            break;
                        case 5:
                            c = parent.color(0, 100, 200);
                            break;
                        default:
                    }
                }

                model.updateColor(k++,
                        (c >> 16) & 0xFF,
                        (c >> 8) & 0xFF,
                        c & 0xFF);
            }
        }
        model.endUpdateColors();

    }

    public void drawSelf(PGraphicsOpenGL graphics) {
//        System.out.println("Trying to draw " + nbToDraw);
        lastModel.render(0, nbToDraw);
//        lastModel.render();
    }

    public void exportToObj(String fileName) {
        OBJWriter writer = new OBJWriter();
        writer.beginSave(fileName);

        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
//        PImage colors = kinect.getDepthColor();

        for (int i = 0; i < Kinect.KINECT_SIZE; i++) {
            if (valid[i]) {
                Vec3D p = points[i];
                writer.vertex(p);
            }
        }

//        if (colors != null) {
//            colors.loadPixels();
//            model.beginUpdateColors();
//            int k = 0;
//            for (int i = 0; i < Kinect.size; i++) {
//                if (valid[i]) {
//                    int c = colors.pixels[i];
//
//                    model.updateColor(k++,
//                            (c >> 16) & 0xFF,
//                            (c >> 8) & 0xFF,
//                            c & 0xFF);
//                }
//            }
//            model.endUpdateColors();
//        }

        writer.endSave();
    }
    private OBJWriter writer = null;

    public void startExportObj(String fileName) {
        writer = new OBJWriter();
        writer.beginSave(fileName);
    }

    public void exportObj() {
        assert (writer != null);
        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        for (int i = 0; i < Kinect.KINECT_SIZE; i++) {
            if (valid[i]) {
                Vec3D p = points[i];
                writer.vertex(p);
            }
        }
    }

    public void endExportObj() {
        writer.endSave();
    }

    public static boolean isInside(Vec3D v, float min, float max, float sideError) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }
}
