package saito.objloader;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.nio.*;

import javax.media.opengl.GL;

import processing.core.*;
/**
 *
 * @author jeremy
 */
public class SegmentLite {

	public String materialName;
	public int indexIBCap;
        public boolean valid;
        public MaterialLite material;

	int[] glbuf;

        public SegmentLite(Segment seg){
            this.materialName = seg.materialName;
            this.indexIBCap = seg.indexIB.capacity();
            this.glbuf = seg.glbuf;
            this.valid = seg.getFaceCount() != 0;
        }


    	public void beginDrawGL(GL gl) {
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, glbuf[0]);

		gl.glEnableClientState(GL.GL_VERTEX_ARRAY); // Enable Vertex Arrays
		gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL.GL_NORMAL_ARRAY);

		// AAARRRGGGG Stride is in BYTES motherf-----
		gl.glVertexPointer(3, GL.GL_FLOAT, 32, 0);
		gl.glTexCoordPointer(2, GL.GL_FLOAT, 32, 12);
		gl.glNormalPointer(GL.GL_FLOAT, 32, 20);

		// turn on backface culling
		gl.glFrontFace(GL.GL_CCW);

		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);

		// gl.glPolygonMode(GL.GL_BACK, GL.GL_POINTS);
		// gl.glEnable(GL.GL_CULL_FACE);
		//
		// gl.glCullFace(GL.GL_BACK);
	}

	public void drawGL(GL gl, int GLTYPE) {

		beginDrawGL(gl);

		// once I get the indexing better I'll use glDrawElements
		// gl.glDrawElements(GLTYPE , indexIB.capacity(), GL.GL_UNSIGNED_INT,
		// indexIB);
		gl.glDrawArrays(GLTYPE, 0, indexIBCap);

		endDrawGL(gl);
	}

	public void endDrawGL(GL gl) {
		gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
	}
}
