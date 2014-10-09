
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.depthcam;

import static fr.inria.papart.depthcam.PointCloud.javaToNativeARGB;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import toxi.geom.Vec3D;

/**
 *
 */
public class PointCloudKinect extends PointCloud implements PConstants {

    public PointCloudKinect(PApplet parent, int skip) {
        super(parent, Kinect.KINECT_SIZE / (skip * skip));
    }
    
    public void updateWith(KinectProcessing kinect){
        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        PImage colorsImg = kinect.getColouredDepthImage();
        
        nbVertices = 0;
        nbColors = 0;
        
        int k = 0;
        for (int i = 0; i < Kinect.KINECT_SIZE; i++) {

            if (valid[i]) {
                Vec3D p = points[i];
                int c = colorsImg.pixels[i];

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
