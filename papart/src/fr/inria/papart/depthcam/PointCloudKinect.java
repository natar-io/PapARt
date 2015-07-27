/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.depthcam;

import static fr.inria.papart.depthcam.PointCloud.javaToNativeARGB;
import fr.inria.papart.multitouch.TouchPoint;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import toxi.geom.Vec3D;

/**
 *
 */
public class PointCloudKinect extends PointCloud implements PConstants {

    public PointCloudKinect(PApplet parent, int skip) {
        super(parent, KinectDepthAnalysis.getKinectSize() / (skip * skip));
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
        for (int i = 0; i < KinectDepthAnalysis.getKinectSize(); i++) {

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
    
    
    public void updateWith(KinectProcessing kinect, ArrayList<TouchPoint> touchs){
        
        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        PImage colorsImg = kinect.getColouredDepthImage();
        
        nbVertices = 0;
        nbColors = 0;
        int k = 0;

        int id = 0;
        for(TouchPoint touch : touchs){
            
            int c =  (((id +1) % 5) * (255 / 5) & 0xFF) << 16
                | (255 - (id % 10 *255)  & 0xFF) << 8
                | (100 & 0xFF);

            id++;
            int c2 = javaToNativeARGB(c);
            
            for(DepthDataElementKinect dde : touch.getDepthDataElements()){
                Vec3D p = dde.depthPoint;
                verticesJava[k++] = p.x;
                verticesJava[k++] = p.y;
                verticesJava[k++] = -p.z;
                verticesJava[k++] = 1;

                nbVertices++;
                
                colorsJava[nbColors++] = c2;
                 
            }            
        }
        
     
        verticesNative.rewind();
        verticesNative.put(verticesJava, 0, nbVertices * 4);

        colorsNative.rewind();
        colorsNative.put(colorsJava, 0, nbColors);
    }

}
