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
        super(parent, Kinect.SIZE / (skip * skip));
    }
    
    public PointCloudKinect(PApplet parent) {
        this(parent, 1);
    }
    
    public void updateWith(KinectProcessing kinect){
        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        PImage colorsImg = kinect.getColouredDepthImage();
        
        nbVertices = 0;
        nbColors = 0;
        
        int k = 0;
        for (int i = 0; i < Kinect.SIZE; i++) {

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
