/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.apps;

import fr.inria.papart.procam.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.multitouch.*;
import java.util.ArrayList;

import org.bytedeco.javacv.*;
import processing.core.PApplet;
import processing.core.PVector;
import toxi.geom.*;

import processing.opengl.*;

public class MultiTouch extends PApplet {

    KinectTouchInput touchInput;

    public static void main(String args[]) {
        PApplet.main(new String[]{"--present", "fr.inria.papart.apps.MultiTouch"});
    }

    public void settings(){
        fullScreen(P3D, 1);
    }
    
    public void setup() {
        Papart papart = Papart.projection2D(this);

        // arguments are 2D and 3D precision.
        papart.loadTouchInputKinectOnly();
        touchInput = (KinectTouchInput) papart.getTouchInput();
        frameRate(200);
    }

    public void draw() {

        background(100);

        fill(50, 50, 255);

        fill(255, 0, 0);
        ArrayList<TouchPoint> touchs3D = new ArrayList<TouchPoint>(touchInput.getTouchPoints3D());
        for (TouchPoint tp : touchs3D) {

            PVector pos = tp.getPosition();
            ellipse(pos.x * width,
                    pos.y * height, 40, 40);
        }

        // Get a copy, as the arrayList is constantly modified
        ArrayList<TouchPoint> touchs2D = new ArrayList<TouchPoint>(touchInput.getTouchPoints2D());
        for (TouchPoint tp : touchs2D) {
            fill(50, 50, 255);
            PVector pos = tp.getPosition();
            ellipse(pos.x * width,
                    pos.y * height, 20, 20);
        }

    }
}
