/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 *
 * @author jeremylaviole
 */
public class TrackedView {
    
    
    PImage img = null;
    IplImage tmpImg = null;
    PMatrix3D pos;
    PVector[] cornerPos;
    PVector size;
    PVector[] screenP = new PVector[4];
    PVector[] outScreenP = new PVector[4];

    public TrackedView(float sheetW, float sheetH, int outWidth, int outHeight,
            int videoWidth, int videoHeight) {
        size = new PVector(sheetW, sheetH);

  
        img = new PImage(outWidth, outHeight, PApplet.RGB);
        cornerPos = new PVector[4];
        screenP = new PVector[4];
        outScreenP = new PVector[4];

        for (int i = 0; i < 4; i++) {
            cornerPos[i] = new PVector();
        }

        outScreenP[0] = new PVector(0, videoHeight);
        outScreenP[1] = new PVector(videoWidth, videoHeight);
        outScreenP[2] = new PVector(videoWidth, 0);
        outScreenP[3] = new PVector(0, 0);
    }

    public void setPos(float[] pos3D) {
        pos = new PMatrix3D(pos3D[0], pos3D[1], pos3D[2], pos3D[3],
                pos3D[4], pos3D[5], pos3D[6], pos3D[7],
                pos3D[8], pos3D[9], pos3D[10], pos3D[11],
                0, 0, 0, 1);

        //	println("Pos found ? : " );
        //	pos.print();
    }

    public void computeCorners(Camera cam) {
        PMatrix3D newPos = pos.get();

        cornerPos[0].x = newPos.m03;
        cornerPos[0].y = newPos.m13;
        cornerPos[0].z = newPos.m23;

        PMatrix3D tmp = new PMatrix3D();
        tmp.apply(pos);

        tmp.translate(size.x, 0, 0);
        cornerPos[1].x = tmp.m03;
        cornerPos[1].y = tmp.m13;
        cornerPos[1].z = tmp.m23;

        tmp.translate(0, size.y, 0);
        cornerPos[2].x = tmp.m03;
        cornerPos[2].y = tmp.m13;
        cornerPos[2].z = tmp.m23;

        tmp.translate(-size.x, 0, 0);
        cornerPos[3].x = tmp.m03;
        cornerPos[3].y = tmp.m13;
        cornerPos[3].z = tmp.m23;

        for (int i = 0; i < 4; i++) {
            screenP[i] = cam.getCamViewPoint(cornerPos[i]);
        }
    }

    public PImage getImage(IplImage iplImg) {
        if (tmpImg == null) {
            tmpImg = Utils.createImageFrom(iplImg, img);
        }

        Utils.remapImage(screenP, outScreenP, iplImg, tmpImg, img);
        return img;
    }
    
}
