/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL2;
import processing.core.PApplet;
import static processing.core.PConstants.TRIANGLES;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PShader;
import toxi.geom.Triangle3D;
import toxi.geom.Vec3D;
import toxi.geom.mesh.OBJWriter;

/**
 *
 * @author jiii
 */
public class PointCloud {

    private final PApplet parentApplet;
    protected int nbColors = 0;
    protected int nbVertices = 0;
    // OpenGL values  -- Point rendering.
    private final int nbPoints;
    private final int vboUsage = GL2.GL_STREAM_COPY;
    private final int vertexMode = GL2.GL_POINTS;
    // openGL shader program    
    PShader myShader;
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

    public PointCloud(PApplet parent, int size) {
        this.parentApplet = parent;
        this.nbPoints = size;
        initPointCloud();
    }

    private void initPointCloud() {
        PGL pgl = ((PGraphicsOpenGL) parentApplet.g).pgl;

        // TODO: lookt at the shaders... 
        myShader = parentApplet.loadShader("shaders/kinect/kinect1.frag", "shaders/kinect/kinect1.vert");

        myShader.bind();
        shaderProgram = myShader.glProgram;
        vertLoc = pgl.getAttribLocation(shaderProgram, "vertex");
        colorsLoc = pgl.getAttribLocation(shaderProgram, "color");
        transformLoc = pgl.getUniformLocation(shaderProgram, "transform");

        myShader.unbind();

        System.out.println("Shader program " + shaderProgram + " vertex loc " + vertLoc + " transform loc " + transformLoc + " colors " + colorsLoc);
        // Todo allocate this intbuffer...

        // Allocate the buffer in central memory (native),  then java, then OpenGL 
        // Native memory         
        int bytes = nbPoints * 4 * 4; // 4 : SizeOf Float   -> ? SIZEOF_FLOAT
        vertices = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder()).
                asFloatBuffer();
        colors = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder()).asIntBuffer();
//        colors = ByteBuffer.allocateDirect(bytes).asIntBuffer();

        // Java memory 
        verticesTmp = new float[nbPoints * 4];
        colorsTmp = new int[nbPoints];

        // OpenGL memory
        // Not necessary as data are streamed... ??
        // IntBuffer intBuffer = allocateIntBuffer(1);
        //  pgl.genBuffers(1, intBuffer);
//        glVertBuff = intBuffer.get(0);
//
        //pgl.genBuffers(1, intBuffer);
        // glColorsBuff = intBuffer.get(0);
        // Check the use of these allocations ?
//        pgl.bindBuffer(PGL.ARRAY_BUFFER, glVertBuff);
//        pgl.bufferData(PGL.ARRAY_BUFFER, nbPoints * 4 * SIZEOF_FLOAT, vertices, PGL.STREAM_DRAW);
//        
//       pgl.bindBuffer(PGL.ARRAY_BUFFER, glColorsBuff);
//        pgl.bufferData(PGL.ARRAY_BUFFER, nbPoints * 4 * SIZEOF_BYTE, colors, PGL.STREAM_DRAW);
//        pgl.bufferData(PGL.ARRAY_BUFFER, nbPoints * 4 * SIZEOF_BYTE, colors, PGL.DYNAMIC_DRAW);
        System.out.println("Buffer vertex object: " + glVertBuff);

        // unbind the buffer.
        pgl.bindBuffer(PGL.ARRAY_BUFFER, 0);
    }

    public void update(PointCloudElement[] pce, boolean[] mask) {
        nbVertices = 0;
        nbColors = 0;

        assert (pce.length <= nbPoints);
        for (int i = 0; i < pce.length; i++) {
            if (mask[i]) {
                addPoint(pce[i]);
            }
        }
        vertices.rewind();
        vertices.put(verticesTmp, 0, nbVertices);

        colors.rewind();
        colors.put(colorsTmp, 0, nbColors);
    }

    public void update(PointCloudElement[] pce) {
        nbVertices = 0;
        nbColors = 0;
        assert (pce.length <= nbPoints);

        for (int i = 0; i < pce.length; i++) {
            // TODO: parallel ?
            addPoint(pce[i]);
        }
        vertices.rewind();
        vertices.put(verticesTmp, 0, nbVertices);

        colors.rewind();
        colors.put(colorsTmp, 0, nbColors);
    }


    public void updateCheck(PointCloudElement[] pce, int w, int h) {
        nbVertices = 0;
        nbColors = 0;

        System.out.println("Compute connexity...");
        Connexity connexity = new Connexity(pce, w, h);
        connexity.computeAll();

        System.out.println("Compute connexity OK");
        byte[] connexSum = connexity.getSum();

        assert (pce.length <= nbPoints);

        for (int i = 0; i < pce.length; i++) {
            // TODO: parallel ?
            if (pce[i] == null
                    || pce[i].point == null
                    || connexSum[i] < 3) {
                continue;
            }

            addPoint(pce[i]);
        }
        vertices.rewind();
        vertices.put(verticesTmp, 0, nbVertices);

        colors.rewind();
        colors.put(colorsTmp, 0, nbColors);
    }

    private void addPoint(PointCloudElement pce) {

        if (pce == null || pce.point == null) {
            return;
        }

        PVector p = pce.point;

//                float[] vert = vertices[nbToDraw];
        verticesTmp[nbVertices++] = p.x;
        verticesTmp[nbVertices++] = p.y;
        verticesTmp[nbVertices++] = p.z;
        verticesTmp[nbVertices++] = 1;

        int c = pce.ptColor;
        int c2 = javaToNativeARGB(c);
        colorsTmp[nbColors++] = c2;
    }

    public void drawSelf(PGraphicsOpenGL g) {

        drawPoints(g);

        // Some would say that... 
//            boolean[] valid = kinect.getValidPoints();
//            Vec3D[] points = kinect.getDepthPoints();
//            PImage colorsImg = kinect.getDepthColor();
//
//            nbVertices = 0;
//            nbColors = 0;
//            parentApplet.beginShape(POINTS);
//            for (int i = 0; i < nbPoints; i++) {
//                if (valid[i]) {
//                    Vec3D p = points[i];
//                    int c = colorsImg.pixels[i];
//                    parentApplet.fill(c);
//                    parentApplet.vertex(p.x, p.y, -p.z);
//                }
//            }
//
//            parentApplet.endShape();
        ///// Triangulation ...  TODO without KINECT
//            boolean[] valid = kinect.getValidPoints();
//            Vec3D[] points = kinect.getDepthPoints();
//            PImage colorsImg = kinect.getDepthColor();
//
//            nbVertices = 0;
//            nbColors = 0;
//            parentApplet.beginShape(TRIANGLES);
//            parentApplet.noStroke();
//
//            skip = kinect.getCurrentSkip();
//
//            for (int y = skip; y < Kinect.KINECT_HEIGHT - skip; y += skip) {
//                for (int x = skip; x < Kinect.KINECT_WIDTH - skip; x += skip) {
//
//                    int offset = y * Kinect.KINECT_WIDTH + x;
//
//                    if (valid[offset]) {
//
//                        Vec3D p = points[offset];
//                        int c = colorsImg.pixels[offset];
//
////                    parentApplet.fill(c);
////                    parentApplet.vertex(p.x, p.y, -p.z);
////                    parentApplet.normal(0, 0, 1);
//                        checkAndCreateTriangle2(x, y, offset, c);
//                    }
//                }
//            }
//            parentApplet.endShape();
    }

    private void drawPoints(PGraphicsOpenGL g) {

        // get cache object using g.
        PGL pgl = g.pgl;

        // Use the shader program
        myShader.bind();

        // enable the vertice array
        pgl.enableVertexAttribArray(vertLoc);
        pgl.enableVertexAttribArray(colorsLoc);

        // load the transformation matrix
        pgl.uniformMatrix4fv(transformLoc, 1, false, toOpenGL(g.projmodelview));

        // Making sure that no VBO is bound at this point.
        pgl.bindBuffer(GL2.GL_ARRAY_BUFFER, 0);

        // check the positions -> useless ? !
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

        myShader.unbind();

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

//
//    public void drawSelf(PGraphicsOpenGL graphics) {
////        System.out.println("Trying to draw " + nbToDraw);
////        lastModel.render(0, nbToDraw);
////        lastModel.render();
//    }
    //// Work for Kinect only for now...
//    public void exportToObj(String fileName) {
//        OBJWriter writer = new OBJWriter();
//        writer.beginSave(fileName);
//
//        boolean[] valid = kinect.getValidPoints();
//        Vec3D[] points = kinect.getDepthPoints();
////        PImage colors = kinect.getDepthColor();
//
//        for (int i = 0; i < nbPoints; i++) {
//            if (valid[i]) {
//                Vec3D p = points[i];
//                writer.vertex(p);
//            }
//        }
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
//        writer.endSave();
//    }
//    private OBJWriter writer = null;
//    public void startExportObj(String fileName) {
//        writer = new OBJWriter();
//        writer.beginSave(fileName);
//    }
//
//    public void exportObj() {
//        assert (writer != null);
//        boolean[] valid = kinect.getValidPoints();
//        Vec3D[] points = kinect.getDepthPoints();
//        for (int i = 0; i < nbPoints; i++) {
//            if (valid[i]) {
//                Vec3D p = points[i];
//                writer.vertex(p);
//            }
//        }
//    }
//
//    public void endExportObj() {
//        writer.endSave();
//    }
    public static boolean isInside(Vec3D v, float min, float max, float sideError) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }
}
