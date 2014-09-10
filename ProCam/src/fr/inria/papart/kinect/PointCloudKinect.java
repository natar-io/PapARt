
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

import static fr.inria.papart.kinect.PointCloud.javaToNativeARGB;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL2;
import processing.core.PApplet;
import processing.core.PConstants;
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
 * @author jeremy  From GLGraphics...
 */
public class PointCloudKinect extends PointCloud implements PConstants {

    private final KinectProcessing kinect;

    public PointCloudKinect(PApplet parent, KinectProcessing kinect, int skip) {
        super(parent, Kinect.KINECT_SIZE / (skip * skip));
        this.kinect = kinect;
    }

    public void autoUpdate() {
        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        PImage colorsImg = kinect.getDepthColor();

        nbVertices = 0;
        nbColors = 0;
        
        int k = 0;
        
        for (int i = 0; i < Kinect.KINECT_SIZE; i++) {

            if (valid[i]) {
                Vec3D p = points[i];
                int c = colorsImg.pixels[i];

//                float[] vert = vertices[nbToDraw];
                verticesJava[k++] = p.x;
                verticesJava[k++] = p.y;
                verticesJava[k++] = -p.z;
                verticesJava[k++] = 1;
                
                int c2 = javaToNativeARGB(c);

                nbVertices++;
                
                colorsJava[nbColors++] = c2;
                // Think about dividing the color intensity by 255 in the shader...
            }
        }
        verticesNative.rewind();
        verticesNative.put(verticesJava, 0, nbVertices * 4);

        colorsNative.rewind();
        colorsNative.put(colorsJava, 0, nbColors);
    }

}
