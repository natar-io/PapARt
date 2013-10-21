package saito.objloader;

import java.util.ArrayList;
import java.util.Hashtable;
import javax.media.opengl.GL;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.opengl.PGraphicsOpenGL;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author jeremy
 */
public class OBJModelLite {

    private SegmentLite[] segmentsLite;
    private Hashtable<String, MaterialLite> materialsLite;
    private int drawMode = PConstants.TRIANGLES; // render mode (ex. POLYGON,
    // POINTS ..)
    private PApplet parent;
    private GL gl;

    public OBJModelLite(ArrayList<Segment> segments, Hashtable<String, Material> materials, int drawMode, PApplet parent) {
        this.drawMode = drawMode;
        this.parent = parent;
        segmentsLite = new SegmentLite[segments.size()];

        // make all segments lite
        for (int i = 0; i < segments.size(); i++) {
            Segment segment = segments.get(i);
            segmentsLite[i] = new SegmentLite(segment);
        }

        //  make all materials lite
        materialsLite = new Hashtable<String, MaterialLite>();
        for (Material mat : materials.values()) {
            materialsLite.put(mat.mtlName, new MaterialLite(mat));
        }

    }

    public void drawGL() {
        boolean fill = parent.g.fill;
        boolean stroke = parent.g.stroke;

        parent.fill(255);
        parent.stroke(255);
        parent.noFill();
        parent.noStroke();

        gl = ((PGraphicsOpenGL) parent.g).beginGL();

//        saveGLState();

        SegmentLite tmpModelSegment;
        MaterialLite mtl;

        for (int i = 0; i < segmentsLite.length; i++) {

            tmpModelSegment = segmentsLite[i];


            if (tmpModelSegment.valid) {
                mtl = materialsLite.get(tmpModelSegment.materialName);

//            mtl.beginDrawGL(gl, useMaterial, useTexture);
                mtl.beginDrawGL(gl, true, true);

                switch (drawMode) {

                    case (PConstants.POINTS):
                        tmpModelSegment.drawGL(gl, GL.GL_POINTS);
                        break;

                    case (PConstants.LINES):
                        tmpModelSegment.drawGL(gl, GL.GL_LINES);
                        break;

                    case (PConstants.TRIANGLES):
                        tmpModelSegment.drawGL(gl, GL.GL_TRIANGLES);
                        break;

                    case (PConstants.TRIANGLE_STRIP):
                        tmpModelSegment.drawGL(gl, GL.GL_TRIANGLE_STRIP);
                        break;

                    case (PConstants.QUADS):
                        tmpModelSegment.drawGL(gl, GL.GL_QUADS);
                        break;

                    case (PConstants.QUAD_STRIP):
                        tmpModelSegment.drawGL(gl, GL.GL_QUAD_STRIP);
                        break;

                    case (PConstants.POLYGON):
                        tmpModelSegment.drawGL(gl, GL.GL_POLYGON);
                        break;

                }

//            mtl.endDrawGL(gl, useMaterial, useTexture);
                mtl.endDrawGL(gl, true, true);

            }
        }

        revertGLState();
        ((PGraphicsOpenGL) parent.g).endGL();

        parent.g.fill = fill;
        parent.g.stroke = stroke;

    }

    /**
     * Called at the start of drawOPENGL.<br>
     * </br> This saves the current state ready so it doesn't get hammered from
     * the objModel.<br>
     * NOTE: this method is on the way out. It'll be removed in the near future.
     * </br>
     */
    private void saveGLState() {
        gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
    }

    /**
     * Returns back to original opengl state that Processing was in.<br>
     * </br>
     * NOTE: this method is on the way out. It'll be removed in the near future.
     */
    private void revertGLState() {
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glPopAttrib();
    }
}
