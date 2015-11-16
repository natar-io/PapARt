package fr.inria.papart.apps;

import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.opencv_core;
import org.reflections.*;
import TUIO.*;
import fr.inria.papart.apps.MyApp2D;
import fr.inria.papart.calibration.CalibrationPopup;
import processing.core.PApplet;
import toxi.geom.*;

import processing.video.*;

public class PaperApp2D extends PApplet {

    boolean useProjector = false;
    Papart papart;

    public void settings() {
        fullScreen(P3D, 2);
    }

    public static void main(String args[]) {
        PApplet.main(new String[]{"--present", "fr.inria.papart.apps.PaperApp2D"});
    }

    public void setup() {

//        if (useProjector) {
        papart = Papart.projection(this);
//        } else {
//            papart = Papart.seeThrough(this);
//        }
//
//        papart.loadSketches();
        papart.loadTouchInput();
        
        MyApp2D app = new MyApp2D();
        papart.startTracking();
    }

    public void draw() {
//        println("frameRate " + frameRate);
    }
    
    public void keyPressed(){
        if(key == 'c'){
           papart.calibration();
        }
    }
}
