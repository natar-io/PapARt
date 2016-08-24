import fr.inria.papart.depthcam.DepthData;
import java.util.ArrayList;
import toxi.geom.Vec3D;
import processing.sound.*;

PVector boardSize = new PVector(297, 210);   //  21 * 29.7 cm
float boardResolution = 3;  // 3 pixels / mm
float ellipseSize = 3;
float red = 0, green = 0, blue = 0;
SinOsc sine;

public class MyApp extends PaperTouchScreen {

  public void settings() {
    setDrawAroundPaper();
    setDrawingSize(297, 210);
    loadMarkerBoard(Papart.markerFolder + "A3-small1.svg", 297, 210);
  }

  public void setup() {
    sine = new SinOsc(papart.getApplet());
    sine.play();
  }

  public void drawAroundPaper() {

    //PMatrix3D kinectCam = 

    //setLocation(63, 40, 0);
    for (Touch t : touchList) {

      TouchPoint tp = t.touchPoint;
      if (tp == null) {
        println("TouchPoint null, this method only works with Kinect.");
        continue;
      }

      PVector touch = t.position;
      println(touch.z);
    }

    /*
     ArrayList<DepthDataElementKinect> depthDataElements = tp.getDepthDataElements();
     
     for (DepthDataElementKinect dde : depthDataElements) {
     red = 0;
     green = 0;
     blue = 0;
     
     try {
     // Vec3D projPt = dde.projectedPoint; // point en espace normalisé sur la table (x, y) et z est en mm
     
     Vec3D depth = dde.depthPoint;
     
     float z = projPt.z;
     PVector v = touchInput.project(screen, getDisplay(), projPt.x, projPt.y); // (x, y) en espace normalisé sur la feuille de papier
     noStroke();
     
     float x = v.x * drawingSize.x;
     float y = (1-v.y) * drawingSize.y; // axe y inversé
     
     println(x + " " + y + " " + z );
     
     red = 255 * x / drawingSize.x;
     green = 255 * z / 150f;
     blue = 255 * y / drawingSize.y;
     
     fill(red, green, blue);     
     ellipse(x, y, ellipseSize, ellipseSize);
     }
     catch(Exception e) {
     println("Exception No Intersection " + e);
     }
     }
     }
     */
  }
}