import fr.inria.papart.procam.*;
import fr.inria.papart.procam.display.*;
import org.bytedeco.javacpp.*;
import TUIO.*;
import toxi.geom.*;

Papart papart;
// ProjectorDisplay ardisplay;
ARDisplay ardisplay;

float focal, cx, cy;
PMatrix3D projIntrinsics; 


float paperDistance = 800; // in mm 

void settings(){
    size(200, 200, P3D);
}

public void setup() {

    Papart.cameraCalib = "calib.xml";
    
    Papart.seeThrough(this);
    
    papart =  Papart.getPapart();
    ardisplay = papart.getARDisplay();
    ardisplay.manualMode();
    
    projIntrinsics = ardisplay.getIntrinsics();
    focal = projIntrinsics.m00;
    cx = projIntrinsics.m02;
    cy = projIntrinsics.m12;
    
    initGui();
  
}

PMatrix3D objectArdisplayTransfo, pos;
PGraphicsOpenGL arGraphics;

void draw() {

    projIntrinsics.m00 = focal;
    projIntrinsics.m11 = focal;
    projIntrinsics.m02 = cx;
    projIntrinsics.m12 = cy;

    // Update the rendering.
    ardisplay.updateIntrinsicsRendering();
        
    ProjectiveDeviceP pdp = ardisplay.getProjectiveDeviceP();
    
    // Update the estimation.
    pdp.updateFromIntrinsics();

    arGraphics = ardisplay.beginDraw();  
    arGraphics.clear();
    arGraphics.scale(1, -1, 1);
    
    // set the distance of the paper (facing the camera)
    drawAt(paperDistance); // in mm 

    // draw the squares (printed from data/squares.svg)
    drawSquares();
    
    ardisplay.endDraw();

    // draw the image
    image(papart.getCameraTracking().getImage(), 0, 0, width, height);

    // draw the AR Layer
    DrawUtils.drawImage((PGraphicsOpenGL) g, 
			ardisplay.render(), 
			0, 0, width, height);
}

void drawAt(float distance){
    arGraphics.translate(0, 0, distance);
}

void drawSquares(){

    arGraphics.noFill();

    // thin 130mm square 
    arGraphics.rect(-10, -10, 120, 120);


    arGraphics.fill(50, 50, 200, 100);
    
    // filled 100mm square 
    arGraphics.rect(0, 0, 100, 100);


    // small green square 50mm
    arGraphics.fill(0, 191, 100, 100);
    arGraphics.rect(150, 80, 50, 50);
}

void keyPressed() {

  if (key == 's') {

      ProjectiveDeviceP pdp = ardisplay.getProjectiveDeviceP();
      pdp.saveTo(this, "calib.xml");
      println("Saved");
  }

  if (key == 'S') {
      ProjectiveDeviceP pdp = ardisplay.getProjectiveDeviceP();
      pdp.saveTo(this, Papart.calibrationFolder + "camera.xml");
      println("Saved as default");
  }

}
