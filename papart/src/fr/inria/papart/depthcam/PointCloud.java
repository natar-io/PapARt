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
package fr.inria.papart.depthcam;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL2;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PShader;
import toxi.geom.Vec3D;

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
    private int colorsLoc;
    private int transformLoc;
    // Local buffers 
    protected float[] verticesJava;
    protected int[] colorsJava;
    // openGL Internal buffers
    protected FloatBuffer verticesNative;
    protected IntBuffer colorsNative;
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
        myShader = parentApplet.loadShader(PointCloud.class.getResource("points.frag").toString(),
                PointCloud.class.getResource("points.vert").toString());

        myShader.bind();
        shaderProgram = myShader.glProgram;
        vertLoc = pgl.getAttribLocation(shaderProgram, "vertex");
        colorsLoc = pgl.getAttribLocation(shaderProgram, "color");
        transformLoc = pgl.getUniformLocation(shaderProgram, "transform");

        myShader.unbind();

//         System.out.println("Shader program " + shaderProgram + " vertex loc " + vertLoc + " transform loc " + transformLoc + " colors " + colorsLoc);
        // Allocate the buffer in central memory (native),  then java, then OpenGL 
        // Native memory         
        int bytes = nbPoints * 4 * 4; // 4 : SizeOf Float   -> ? SIZEOF_FLOAT
        verticesNative = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder()).
                asFloatBuffer();
        colorsNative = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder()).asIntBuffer();

        // Java memory 
        verticesJava = new float[nbPoints * 4];
        colorsJava = new int[nbPoints];

//        System.out.println("Buffer vertex object: " + glVertBuff);
        // unbind the buffer.
        pgl.bindBuffer(PGL.ARRAY_BUFFER, 0);
    }

    private int currentVertNo = 0;

//    @Deprecated
//    public void update(PointCloudElement[] pce, boolean[] mask) {
//        nbColors = 0;
//        currentVertNo = 0;
//
//        assert (pce.length <= nbPoints);
//        for (int i = 0; i < pce.length; i++) {
//            if (mask[i]) {
//                addPoint(pce[i]);
//            }
//        }
//
//        loadVerticesToNative();
//    }
//
//    public void update(PointCloudElement[] pce) {
//        currentVertNo = 0;
//        nbColors = 0;
//        assert (pce.length <= nbPoints);
//
//        for (int i = 0; i < pce.length; i++) {
//            // TODO: parallel ?
//            addPoint(pce[i]);
//        }
//        loadVerticesToNative();
//    }

//    public void updateCheck(PointCloudElement[] pce, int w, int h) {
//        nbVertices = 0;
//        nbColors = 0;
//
//        System.out.println("Compute connexity...");
//        Connexity connexity = new Connexity(pce, w, h);
//        connexity.computeAll();
//
//        System.out.println("Compute connexity OK");
//        byte[] connexSum = connexity.getSum();
//
//        assert (pce.length <= nbPoints);
//
//        for (int i = 0; i < pce.length; i++) {
//            // TODO: parallel ?
//            if (pce[i] == null
//                    || pce[i].position == null
//                    || connexSum[i] < 3) {
//                continue;
//            }
//
//            addPoint(pce[i]);
//        }
//        loadVerticesToNative();
//    }

    private void addPoint(DepthPoint pce) {

        if (pce == null || pce.position == null) {
            return;
        }

        PVector p = pce.position;

//                float[] vert = vertices[nbToDraw];
        verticesJava[currentVertNo++] = p.x;
        verticesJava[currentVertNo++] = p.y;
        verticesJava[currentVertNo++] = p.z;
        verticesJava[currentVertNo++] = 1;

        int c = pce.colorPt;
        int c2 = javaToNativeARGB(c);
        colorsJava[nbColors++] = c2;
    }

    private void loadVerticesToNative() {
        nbVertices = currentVertNo / 4;
        verticesNative.rewind();
        verticesNative.put(verticesJava, 0, currentVertNo);

        colorsNative.rewind();
        colorsNative.put(colorsJava, 0, nbColors);
    }

    public void drawSelf(PGraphicsOpenGL g) {
        drawPoints(g);
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
        verticesNative.position(0); // start at 0
        colorsNative.position(0); // start at 0

        // Say where vertices are
        // Version 1
        pgl.vertexAttribPointer(vertLoc, 4, PGL.FLOAT, false, 4 * SIZEOF_FLOAT, verticesNative);

        // Version 2
//        pgl.bindBuffer(PGL.ARRAY_BUFFER, glVertBuff);
//        pgl.bufferData(PGL.ARRAY_BUFFER, nbVertices * 4 * SIZEOF_FLOAT, vertices, PGL.STREAM_DRAW);
//        gl.glVertexAttribPointer(vertLoc, 4, PGL.FLOAT, false, 4 * SIZEOF_FLOAT, 0);
        // Version 1 
//        pgl.bindBuffer(GL2.GL_ARRAY_BUFFER, 0);
        // Say where colors are
        // TEST
        pgl.vertexAttribPointer(colorsLoc, 4, PGL.UNSIGNED_BYTE, false, 4 * SIZEOF_BYTE, colorsNative);
//        pgl.vertexAttribPointer(colorsLoc, 4, PGL.UNSIGNED_INT, false, 4 * SIZEOF_INT, colorsNative);

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

    private FloatBuffer toOpenGL(PMatrix3D projmodelview) {

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

    public static boolean isInside(Vec3D v, float min, float max, float sideError) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }
}
