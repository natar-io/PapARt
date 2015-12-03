package fr.inria.papart.apps;

import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.opencv_core;
import org.reflections.*;
import TUIO.*;
import fr.inria.papart.apps.MyApp2D;
import fr.inria.papart.calibration.CalibrationPopup;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.tracking.DetectedMarker;
import fr.inria.papart.tracking.MarkerDetector;
import fr.inria.papart.tracking.MarkerSvg;
import java.util.ArrayList;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.awt.PShapeJava2D;
import processing.core.PApplet;
import processing.core.PMatrix;
import processing.core.PMatrix2D;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PShapeSVG;
import processing.core.PVector;
import processing.data.XML;
import toxi.geom.*;

import processing.video.*;

public class PaperApp2D extends PApplet {

    boolean useProjector = false;
    Papart papart;
    private Camera camera;
    private MarkerDetector markerDetector;

    @Override
    public void settings() {
        size(900, 600, P3D);
//        fullScreen(P3D, 2);
    }

    public static void main(String args[]) {
        PApplet.main(new String[]{"--present", "fr.inria.papart.apps.PaperApp2D"});
    }

    @Override
    public void setup() {

        papart = Papart.seeThrough(this);
//        papart.loadTouchInput();

        PaperScreen app = new MyApp2D();
        papart.startTracking();

//        markerDetector = new MarkerDetector();
        camera = papart.getCameraTracking();
//        papart.getARDisplay().manualMode();
//
//        XML xml = loadXML("/home/jiii/papart/markerGen/artoolkitPlus/A3-small1.svg");
//        ArrayList<MarkerSvg> markersFromSVG = MarkerSvg.getMarkersFromSVG(xml);
//        for(MarkerSvg m : markersFromSVG){
//            println(m.getId());
//            m.getMatrix().print();
//        }
        
    }



    public void draw() {
//        image(camera.getPImage(), 0, 0, camera.width(), camera.height());
//        IplImage img = camera.getIplImage();
//
//        DetectedMarker[] detectedMarkers = markerDetector.detect(img);
////        System.out.print("found: " + detectedMarkers.length);
//        for (DetectedMarker marker : detectedMarkers) {
////            System.out.print(" id : " + marker.id);
//
//            for (int i = 0; i < 8; i += 2) {
//                
//                fill(i * 50, 0, 0);
//                
//                float x = (float) marker.corners[i];
//                float y = (float) marker.corners[i + 1];
//                ellipse(x, y, 5, 5);
//            }
//
//        }
//        println("frameRate " + frameRate);
    }

//    public void keyPressed(){
//        if(key == 'c'){
//           papart.calibration();
//        }
//    }
}
