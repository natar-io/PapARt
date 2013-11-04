
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

import static fr.inria.papart.kinect.Kinect.KINECT_WIDTH;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import processing.core.PApplet;
import processing.core.PConstants;
import static processing.core.PConstants.GROUP;
import static processing.core.PConstants.TRIANGLES;
import static processing.core.PConstants.X;
import static processing.core.PConstants.Y;
import static processing.core.PConstants.Z;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PShape;
import static processing.core.PShape.GEOMETRY;
import static processing.core.PShape.PATH;
import static processing.core.PShape.PRIMITIVE;
import processing.core.PVector;
import processing.opengl.PGL;
import static processing.opengl.PGL.ARRAY_BUFFER;
import static processing.opengl.PGL.DEPTH_TEST;
import static processing.opengl.PGL.FLOAT;
import static processing.opengl.PGL.TEXTURE_2D;
import processing.opengl.PGraphicsOpenGL;
import static processing.opengl.PGraphicsOpenGL.pgl;
import processing.opengl.PShader;
import processing.opengl.PShapeOpenGL;
import toxi.geom.Triangle3D;
import toxi.geom.Vec3D;
import toxi.geom.mesh.OBJWriter;

/**
 *
 * @author jeremy  From GLGraphics...
 */
public class PointCloudKinect implements PConstants {

    private Kinect kinect;
    private PApplet parentApplet;
    protected int nbColors = 0;
    protected int nbVertices = 0;
    // OpenGL values  -- Point rendering.
    private int vboUsage = GL2.GL_STREAM_COPY;
    private int vertexMode = GL2.GL_POINTS;
    // openGL shader program    
    PShader kinectShader;
    int shaderProgram;
    // locations associated
    private int vertLoc;
    private int glVertBuff;
    private int colorsLoc;
    private int glColorsBuff;
    private int transformLoc;
    // Local buffers 
    private float[] verticesTmp;
    private int[] colorsTmp;
    // openGL Internal buffers
    private FloatBuffer vertices;
    private IntBuffer colors;
    protected static final int SIZEOF_SHORT = Short.SIZE / 8;
    protected static final int SIZEOF_INT = Integer.SIZE / 8;
    protected static final int SIZEOF_FLOAT = Float.SIZE / 8;
    protected static final int SIZEOF_BYTE = Byte.SIZE / 8;
    protected boolean usePoints;
    private int skip = 1;

    public PointCloudKinect(PApplet parent, Kinect kinect) {
        this(parent, kinect, true);
    }

    public PointCloudKinect(PApplet parent, Kinect kinect, boolean points) {
        this.kinect = kinect;
        this.parentApplet = parent;

        if (points) {
            usePoints = true;
            initPointCloud();
        } else {
            usePoints = false;
            initTriangles();
        }

    }

    private void initPointCloud() {
        PGL pgl = PGraphicsOpenGL.pgl;

        kinectShader = parentApplet.loadShader("shaders/kinect/kinect1.frag", "shaders/kinect/kinect1.vert");

        kinectShader.bind();
        shaderProgram = kinectShader.glProgram;
        vertLoc = pgl.getAttribLocation(shaderProgram, "vertex");
        colorsLoc = pgl.getAttribLocation(shaderProgram, "color");
        transformLoc = pgl.getUniformLocation(shaderProgram, "transform");

        kinectShader.unbind();

        System.out.println("Shader program " + shaderProgram + " vertex loc " + vertLoc + " transform loc " + transformLoc + " colors " + colorsLoc);
        // Todo allocate this intbuffer...

        // Allocate the buffer in central memory (native),  then java, then OpenGL 
        // Native memory         
        int bytes = Kinect.KINECT_SIZE * 4 * 4; // 4 : SizeOf Float
        vertices = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder()).
                asFloatBuffer();
        colors = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder()).asIntBuffer();
//        colors = ByteBuffer.allocateDirect(bytes).asIntBuffer();

        // Java memory 
        verticesTmp = new float[Kinect.KINECT_SIZE * 4];
        colorsTmp = new int[Kinect.KINECT_SIZE];

        // OpenGL memory
        // Not necessary as data are streamed... ??
        IntBuffer intBuffer = allocateIntBuffer(1);
        pgl.genBuffers(1, intBuffer);
//        glVertBuff = intBuffer.get(0);
//
        pgl.genBuffers(1, intBuffer);
        glColorsBuff = intBuffer.get(0);

//        pgl.bindBuffer(PGL.ARRAY_BUFFER, glVertBuff);
//        pgl.bufferData(PGL.ARRAY_BUFFER, Kinect.KINECT_SIZE * 4 * SIZEOF_FLOAT, vertices, PGL.STREAM_DRAW);
//        
        pgl.bindBuffer(PGL.ARRAY_BUFFER, glColorsBuff);
        pgl.bufferData(PGL.ARRAY_BUFFER, Kinect.KINECT_SIZE * 4 * SIZEOF_BYTE, colors, PGL.STREAM_DRAW);

        System.out.println("Buffer vertex object: " + glVertBuff);

        // unbind the buffer.
        pgl.bindBuffer(PGL.ARRAY_BUFFER, 0);
    }
    PShape currentShape;

    private void initTriangles() {
//        currentShape = parentApplet.createShape(TRIANGLES);
    }

    public void updateColorsProcessing() {

        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        PImage colorsImg = kinect.getDepthColor();

        nbVertices = 0;
        nbColors = 0;

        if (usePoints) {
            for (int i = 0; i < Kinect.KINECT_SIZE; i++) {

                if (valid[i]) {
                    Vec3D p = points[i];
                    int c = colorsImg.pixels[i];

//                float[] vert = vertices[nbToDraw];
                    verticesTmp[nbVertices++] = p.x;
                    verticesTmp[nbVertices++] = p.y;
                    verticesTmp[nbVertices++] = -p.z;
                    verticesTmp[nbVertices++] = 1;

                    int c2 = javaToNativeARGB(c);

                    colorsTmp[nbColors++] = c2;
                    // Think about dividing the color intensity by 255 in the shader...
                }
            }
            vertices.rewind();
            vertices.put(verticesTmp, 0, nbVertices);

            colors.rewind();
            colors.put(colorsTmp, 0, nbColors);
        } else {
        }

    }

    public void drawSelf(PGraphicsOpenGL g) {

        if (usePoints) {
            drawPoints(g);

            // Some would say that... 
//            boolean[] valid = kinect.getValidPoints();
//            Vec3D[] points = kinect.getDepthPoints();
//            PImage colorsImg = kinect.getDepthColor();
//
//            nbVertices = 0;
//            nbColors = 0;
//            parentApplet.beginShape(POINTS);
//            for (int i = 0; i < Kinect.KINECT_SIZE; i++) {
//                if (valid[i]) {
//                    Vec3D p = points[i];
//                    int c = colorsImg.pixels[i];
//                    parentApplet.fill(c);
//                    parentApplet.vertex(p.x, p.y, -p.z);
//                }
//            }
//
//            parentApplet.endShape();
        } else {

            boolean[] valid = kinect.getValidPoints();
            Vec3D[] points = kinect.getDepthPoints();
            PImage colorsImg = kinect.getDepthColor();

            nbVertices = 0;
            nbColors = 0;
            parentApplet.beginShape(TRIANGLES);
            parentApplet.noStroke();

            skip = kinect.getCurrentSkip();

            for (int y = 0; y < kinect.KINECT_HEIGHT ; y += skip) {
                for (int x = 0; x < kinect.KINECT_WIDTH ; x += skip) {

                    int offset = y * kinect.KINECT_WIDTH + x;

                    if (valid[offset]) {

                        Vec3D p = points[offset];
                        int c = colorsImg.pixels[offset];

//                    parentApplet.fill(c);
//                    parentApplet.vertex(p.x, p.y, -p.z);
//                    parentApplet.normal(0, 0, 1);
                        checkAndCreateTriangle2(x, y, offset, c);
                    }
                }
            }

//            for (int i = 0; i < Kinect.KINECT_SIZE; i++) {
//                if (valid[i]) {
//                    Vec3D p = points[i];
//                    int c = colorsImg.pixels[i];
//                    checkAndCreateTriangle2(i % kinect.KINECT_WIDTH, i / kinect.KINECT_HEIGHT, i, c);
//
//                }
//            }
            parentApplet.endShape();
        }
    }

    private void drawPoints(PGraphicsOpenGL g) {

        // get cache object using g.
        PGL pgl = PGraphicsOpenGL.pgl;

        // Use the shader program
        kinectShader.bind();

        // enable the vertice array
        pgl.enableVertexAttribArray(vertLoc);
        pgl.enableVertexAttribArray(colorsLoc);

        // load the transformation matrix
        pgl.uniformMatrix4fv(transformLoc, 1, false, toOpenGL(g.projmodelview));

        // Making sure that no VBO is bound at this point.
        pgl.bindBuffer(GL2.GL_ARRAY_BUFFER, 0);

        // check the positions -> uselesse ? !
        vertices.position(0); // start at 0
        colors.position(0); // start at 0

        // Say where vertices are
        // Version 1
        pgl.vertexAttribPointer(vertLoc, 4, PGL.FLOAT, false, 4 * SIZEOF_FLOAT, vertices);

        // Version 2
//        pgl.bindBuffer(PGL.ARRAY_BUFFER, glVertBuff);
//        pgl.bufferData(PGL.ARRAY_BUFFER, nbVertices * 4 * SIZEOF_FLOAT, vertices, PGL.STREAM_DRAW);
//        gl.glVertexAttribPointer(vertLoc, 4, PGL.FLOAT, false, 4 * SIZEOF_FLOAT, 0);
        // Version 1 
//        pgl.bindBuffer(GL2.GL_ARRAY_BUFFER, 0);
        // Say where colors are
        pgl.vertexAttribPointer(colorsLoc, 4, PGL.UNSIGNED_BYTE, false, 4 * SIZEOF_BYTE, colors);

        // Version 2 
//        pgl.bindBuffer(PGL.ARRAY_BUFFER, glColorsBuff);
//        pgl.bufferData(PGL.ARRAY_BUFFER, nbVertices * 4 * SIZEOF_BYTE, colors, PGL.STREAM_DRAW);
//        gl.glVertexAttribPointer(colorsLoc, 4, PGL.UNSIGNED_BYTE, false, 4 * SIZEOF_BYTE, 0);
        // Draw the array nbToDraw elements.
        pgl.drawArrays(vertexMode, 0, nbVertices);

        pgl.disableVertexAttribArray(vertLoc);
        pgl.disableVertexAttribArray(colorsLoc);

        kinectShader.unbind();

        // stop shader
        pgl.useProgram(0);

    }
    float[] glProjmodelview = null;
    FloatBuffer glProjBuff = null;

    public FloatBuffer toOpenGL(PMatrix3D projmodelview) {

        if (glProjmodelview == null) {
            glProjmodelview = new float[16];
        }

        glProjmodelview[0] = projmodelview.m00;
        glProjmodelview[1] = projmodelview.m10;
        glProjmodelview[2] = projmodelview.m20;
        glProjmodelview[3] = projmodelview.m30;

        glProjmodelview[4] = projmodelview.m01;
        glProjmodelview[5] = projmodelview.m11;
        glProjmodelview[6] = projmodelview.m21;
        glProjmodelview[7] = projmodelview.m31;

        glProjmodelview[8] = projmodelview.m02;
        glProjmodelview[9] = projmodelview.m12;
        glProjmodelview[10] = projmodelview.m22;
        glProjmodelview[11] = projmodelview.m32;

        glProjmodelview[12] = projmodelview.m03;
        glProjmodelview[13] = projmodelview.m13;
        glProjmodelview[14] = projmodelview.m23;
        glProjmodelview[15] = projmodelview.m33;

        if (glProjBuff == null || glProjBuff.capacity() < glProjmodelview.length) {
            glProjBuff = FloatBuffer.allocate(glProjmodelview.length);
        }
        glProjBuff.position(0);
        glProjBuff.put(glProjmodelview);
        glProjBuff.rewind();

        return glProjBuff;
    }
    protected static boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    protected static int javaToNativeARGB(int color) {
        if (BIG_ENDIAN) { // ARGB to RGBA
            return ((color >> 24) & 0xFF) | ((color << 8) & 0xFFFFFF00);
        } else { // ARGB to ABGR
            return (color & 0xFF000000) | ((color << 16) & 0xFF0000)
                    | (color & 0xFF00) | ((color >> 16) & 0xFF);
        }
    }

    protected static IntBuffer allocateDirectIntBuffer(int size) {
        int bytes = PApplet.max(MIN_DIRECT_BUFFER_SIZE, size) * SIZEOF_INT;
        return ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder()).
                asIntBuffer();
    }

    protected static IntBuffer allocateIntBuffer(int size) {
        if (USE_DIRECT_BUFFERS) {
            return allocateDirectIntBuffer(size);
        } else {
            return IntBuffer.allocate(size);
        }
    }
    /**
     * Switches between the use of regular and direct buffers.
     */
    protected static final boolean USE_DIRECT_BUFFERS = true;
    protected static final int MIN_DIRECT_BUFFER_SIZE = 1;

    private int checkAndCreateTriangle2(int x, int y, int currentIndex, int color) {

        int[] connexity = kinect.getConnexity();
        Vec3D[] points = kinect.getDepthPoints();

        // Triangles indices this way. A is current
        // D B 
        // C A
        // Triangles indices this way. A is current
        // 0   1   2 
        //   a   b
        // 3   x   4
        //   c   d 
        // 5   6   7
        int offset1 = ((y - skip) * Kinect.KINECT_WIDTH) + x;
        int offsetx = (y * Kinect.KINECT_WIDTH) + x;
        int offset3 = offsetx - skip;
        int offset0 = offset1 - skip;

        int c = connexity[offsetx];
        int[] offsets = new int[8];

        for (int y1 = y - skip, connNo = 0; y1 <= y + skip;  y1 = y1 + skip) {
            for (int x1 = x - skip; x1 <= x + skip; x1 = x1 + skip) {

                // Do not try the current point
                if (x1 == x && y1 == y) {
                    continue;
                }
                
                offsets[connNo] = -1;
                
                // If the point is not in image
                if (y1 >= Kinect.KINECT_HEIGHT || y1 < 0 || x1 < 0 || x1 >= Kinect.KINECT_WIDTH) {
                    connNo++;
                    continue; 
                }

                int offset = y1 * Kinect.KINECT_WIDTH + x1;

                if ((c & (1 << connNo)) == 1 << connNo) {
                    offsets[connNo] = offset;
                }

                if(offsets[connNo] != -1){
                    if(points[offsets[connNo]] == null){
                        System.out.println("Bug dans la Matrice. L'élu est là.");
                    }
                }

                connNo++;
            }
        }

        // x 1  3  
        boolean tra = offsets[1] != -1 && offsets[3] != -1;

        // x 4 1
        boolean trb = offsets[1] != -1 && offsets[4] != -1;

        // x 6 4
        boolean trc = offsets[4] != -1 && offsets[6] != -1;

        // x 3 6
        boolean trd = offsets[3] != -1 && offsets[6] != -1;

        Vec3D normal = new Vec3D(0, 0, 0);

//        System.out.println("Offsets " + offsets[1] + " " + offsetx + " " + offsets[3]);
        if (tra) {
            // X 1 3 
//            System.out.println("point " + points[offsetx] + " " + points[offsets[1]] + " " + points[offsets[3]]);

            Triangle3D trianglea = new Triangle3D(points[offsetx], points[offsets[1]], points[offsets[3]]);
            trianglea.computeNormal();

            normal = normal.add(trianglea.normal);

            parentApplet.fill(c);
            parentApplet.vertex(points[offsetx].x, points[offsetx].y, -points[offsetx].z);
            parentApplet.normal(normal.x, normal.y, -normal.z);

            parentApplet.fill(c);
            parentApplet.vertex(points[offsets[1]].x, points[offsets[1]].y, -points[offsets[1]].z);
            parentApplet.normal(normal.x, normal.y, -normal.z);

            parentApplet.fill(c);
            parentApplet.vertex(points[offsets[3]].x, points[offsets[3]].y, -points[offsets[3]].z);
            parentApplet.normal(normal.x, normal.y, -normal.z);

//            indices[currentIndex++] = indicesMap[offsetx];
//            indices[currentIndex++] = indicesMap[offsets[1]];
//            indices[currentIndex++] = indicesMap[offsets[3]];
//
//            int vertexIndex = indicesMap[offsetx];
//            normals[vertexIndex * 4 + 0] = trianglea.normal.x;
//            normals[vertexIndex * 4 + 1] = trianglea.normal.y;
//            normals[vertexIndex * 4 + 2] = trianglea.normal.z;
//            normals[vertexIndex * 4 + 3] = 0;
        }

        if (trb) {
            // X 1 4
//            System.out.println("Offets " + offsetx + offsets[4] + " " + offsets[1]);

            Triangle3D triangleb = new Triangle3D(points[offsetx], points[offsets[4]], points[offsets[1]]);
            triangleb.computeNormal();
            normal = normal.add(triangleb.normal);
//
            parentApplet.fill(c);
            parentApplet.vertex(points[offsetx].x, points[offsetx].y, -points[offsetx].z);
            parentApplet.normal(normal.x, normal.y, -normal.z);

            parentApplet.fill(c);
            parentApplet.vertex(points[offsets[4]].x, points[offsets[4]].y, -points[offsets[4]].z);
            parentApplet.normal(normal.x, normal.y, -normal.z);

            parentApplet.fill(c);
            parentApplet.vertex(points[offsets[1]].x, points[offsets[1]].y, -points[offsets[1]].z);
            parentApplet.normal(normal.x, normal.y, -normal.z);

//            indices[currentIndex++] = indicesMap[offsetx];
//            indices[currentIndex++] = indicesMap[offsets[1]];
//            indices[currentIndex++] = indicesMap[offsets[4]];
        }

        if (trc) {
            // X 6 4
            Triangle3D triangleb = new Triangle3D(points[offsetx], points[offsets[6]], points[offsets[4]]);
            triangleb.computeNormal();
            normal = normal.add(triangleb.normal);

            //
            parentApplet.fill(c);
            parentApplet.vertex(points[offsetx].x, points[offsetx].y, -points[offsetx].z);
            parentApplet.normal(normal.x, normal.y, -normal.z);

            parentApplet.fill(c);
            parentApplet.vertex(points[offsets[6]].x, points[offsets[6]].y, -points[offsets[6]].z);
            parentApplet.normal(normal.x, normal.y, -normal.z);

            parentApplet.fill(c);
            parentApplet.vertex(points[offsets[4]].x, points[offsets[4]].y, -points[offsets[4]].z);
            parentApplet.normal(normal.x, normal.y, -normal.z);

//            indices[currentIndex++] = indicesMap[offsetx];
//            indices[currentIndex++] = indicesMap[offsets[6]];
//            indices[currentIndex++] = indicesMap[offsets[4]];
        }
        if (trd) {
            // X 3 6
            Triangle3D triangleb = new Triangle3D(points[offsetx], points[offsets[3]], points[offsets[6]]);
            triangleb.computeNormal();
            normal = normal.add(triangleb.normal);

            parentApplet.fill(c);
            parentApplet.vertex(points[offsetx].x, points[offsetx].y, -points[offsetx].z);
            parentApplet.normal(normal.x, normal.y, -normal.z);

            parentApplet.fill(c);
            parentApplet.vertex(points[offsets[6]].x, points[offsets[6]].y, -points[offsets[6]].z);
            parentApplet.normal(normal.x, normal.y, -normal.z);

            parentApplet.fill(c);
            parentApplet.vertex(points[offsets[6]].x, points[offsets[6]].y, -points[offsets[6]].z);
            parentApplet.normal(normal.x, normal.y, -normal.z);

//            indices[currentIndex++] = indicesMap[offsetx];
//            indices[currentIndex++] = indicesMap[offsets[3]];
//            indices[currentIndex++] = indicesMap[offsets[6]];
        }

//        normal.normalize();
//        int vertexIndex = indicesMap[offsetx];
//        normals[vertexIndex * 4 + 0] = normal.x;
//        normals[vertexIndex * 4 + 1] = normal.y;
//        normals[vertexIndex * 4 + 2] = normal.z;
//        normals[vertexIndex * 4 + 3] = 0;
        return currentIndex;

    }
//
//    // Old version...
//    private int checkAndCreateTriangle(int x, int y, int currentIndex) {
//
//        // Triangles indices this way. A is current
//        // D B 
//        // C A
//
//        final float maxDist = 10.0f;
//
//        int offsetB = ((y - skip) * Kinect.KINECT_WIDTH) + x;
//        int offsetA = (y * Kinect.KINECT_WIDTH) + x;
//        int offsetC = offsetA - skip;
//        int offsetD = offsetB - skip;
//
//        if (valid[offsetA]
//                && valid[offsetB]
//                && valid[offsetC]
//                && valid[offsetD]) {
//
//            if (points[offsetA].distanceTo(points[offsetB]) < maxDist
//                    && points[offsetA].distanceTo(points[offsetC]) < maxDist
//                    && points[offsetA].distanceTo(points[offsetD]) < maxDist) {
//
//
//                // Get the normal !
//                Triangle3D triangle1 = new Triangle3D(points[offsetB], points[offsetD], points[offsetC]);
//                Triangle3D triangle2 = new Triangle3D(points[offsetA], points[offsetB], points[offsetC]);
//
//                triangle1.computeNormal();
//                triangle2.computeNormal();
//
//                int vertexIndex = indicesMap[offsetB];
//                normals[vertexIndex * 4 + 0] = triangle1.normal.x;
//                normals[vertexIndex * 4 + 1] = triangle1.normal.y;
//                normals[vertexIndex * 4 + 2] = triangle1.normal.z;
//                normals[vertexIndex * 4 + 3] = 0;
//
//                vertexIndex = indicesMap[offsetC];
//                normals[vertexIndex * 4 + 0] = triangle1.normal.x;
//                normals[vertexIndex * 4 + 1] = triangle1.normal.y;
//                normals[vertexIndex * 4 + 2] = triangle1.normal.z;
//                normals[vertexIndex * 4 + 3] = 0;
//
//                vertexIndex = indicesMap[offsetD];
//                normals[vertexIndex * 4 + 0] = triangle1.normal.x;
//                normals[vertexIndex * 4 + 1] = triangle1.normal.y;
//                normals[vertexIndex * 4 + 2] = triangle1.normal.z;
//                normals[vertexIndex * 4 + 3] = 0;
//
//                vertexIndex = indicesMap[offsetA];
//                normals[vertexIndex * 4 + 0] = triangle2.normal.x;
//                normals[vertexIndex * 4 + 1] = triangle2.normal.y;
//                normals[vertexIndex * 4 + 2] = triangle2.normal.z;
//                normals[vertexIndex * 4 + 3] = 0;
//
//                indices[currentIndex++] = indicesMap[offsetB];
//                indices[currentIndex++] = indicesMap[offsetD];
//                indices[currentIndex++] = indicesMap[offsetC];
//                indices[currentIndex++] = indicesMap[offsetA];
//                indices[currentIndex++] = indicesMap[offsetB];
//                indices[currentIndex++] = indicesMap[offsetC];
////                
//            }
////            model.updateIndices(indicesMap);
//        }
//
//        return currentIndex;
//    }
//    public void update() {
//
//        boolean[] valid = kinect.getValidPoints();
//        Vec3D[] points = kinect.getDepthPoints();
//        PImage colors = kinect.getDepthColor();
//        lastModel = model;
//        model.beginUpdateVertices();
//        nbToDraw = 0;
//        for (int i = 0; i < Kinect.KINECT_SIZE; i++) {
//
//            if (valid[i]) {
//                Vec3D p = points[i];
////                if (plane.orientation(p)) {
////                if (calib.plane().hasGoodOrientationAndDistance(p)) {
////                    if (isInside(calib.project(p), 0.f, 1.f, 0.05f)) {
//                model.updateVertex(nbToDraw++, p.x, p.y, -p.z);
////                    }
//
////                } else {
////                    valid[i] = false;
////                }
//            }
//        }
//
//        model.endUpdateVertices();
//
//        if (colors != null) {
//            colors.loadPixels();
//            model.beginUpdateColors();
//            int k = 0;
//            for (int i = 0; i < Kinect.KINECT_SIZE; i++) {
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
//    }
//    public void updateMultiTouch() {
//        boolean[] valid = kinect.getValidPoints();
//        Vec3D[] points = kinect.getDepthPoints();
//        PImage colors = kinect.getDepthColor();
//        lastModel = model;
//        model.beginUpdateVertices();
//        nbToDraw = 0;
//        for (int i = 0; i < Kinect.KINECT_SIZE; i++) {
//
//            if (valid[i]) {
//                Vec3D p = points[i];
//                model.updateVertex(nbToDraw++, p.x, p.y, -p.z);
//            }
//        }
//        model.endUpdateVertices();
//
//        colors.loadPixels();
//        model.beginUpdateColors();
//        int k = 0;
//        for (int i = 0; i < Kinect.KINECT_SIZE; i++) {
//            if (valid[i]) {
//                int c = colors.pixels[i];
//
//                model.updateColor(k++,
//                        (c >> 16) & 0xFF,
//                        (c >> 8) & 0xFF,
//                        c & 0xFF);
//            }
//        }
//        model.endUpdateColors();
//    }
//
//    public void updateMultiTouch(Vec3D[] projectedPoints) {
//
//        boolean[] valid = kinect.getValidPoints();
//        Vec3D[] points = kinect.getDepthPoints();
//        lastModel = model;
//        model.beginUpdateVertices();
//        nbToDraw = 0;
//        for (int i = 0; i < Kinect.KINECT_SIZE; i++) {
//
//            if (valid[i]) {
//                Vec3D p = points[i];
//                model.updateVertex(nbToDraw++, p.x, p.y, -p.z);
//            }
//        }
//        model.endUpdateVertices();
//
//        model.beginUpdateColors();
//        int k = 0;
//        for (int i = 0; i < Kinect.KINECT_SIZE; i++) {
//            if (valid[i]) {
//
//                int c = parentApplet.color(255, 255, 255);
//
//                if (Kinect.connectedComponent[i] > 0) {
//                    switch (Kinect.connectedComponent[i]) {
//                        case 1:
//                            c = parentApplet.color(100, 200, 100);
//                            break;
//                        case 2:
//                            c = parentApplet.color(0, 200, 100);
//                            break;
//                        case 3:
//                            c = parentApplet.color(200, 200, 100);
//                            break;
//                        case 4:
//                            c = parentApplet.color(0, 0, 200);
//                            break;
//                        case 5:
//                            c = parentApplet.color(0, 100, 200);
//                            break;
//                        default:
//                    }
//                }
//
//                model.updateColor(k++,
//                        (c >> 16) & 0xFF,
//                        (c >> 8) & 0xFF,
//                        c & 0xFF);
//            }
//        }
//        model.endUpdateColors();
//
//    }
//    public void drawSelf(PGraphicsOpenGL graphics) {
////        System.out.println("Trying to draw " + nbToDraw);
////        lastModel.render(0, nbToDraw);
////        lastModel.render();
//    }

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
