// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

import fr.inria.papart.procam.camera.*;
import fr.inria.papart.procam.display.*;


TrackedView boardView;
Papart papart;
ARDisplay cameraDisplay;
Camera camera;

PVector[] corners = new PVector[4];


public void settings(){
    size(200, 200, P3D);
}

public void setup(){
    Papart papart = Papart.seeThrough(this);


    papart =  Papart.getPapart();
    cameraDisplay = papart.getARDisplay();
    cameraDisplay.manualMode();

    camera = papart.getCameraTracking();

    boardView = new TrackedView();
    //    boardView.setCaptureSizeMM(new PVector(1280, 800));
    boardView.setImageWidthPx(1280);
    boardView.setImageHeightPx(800);
    boardView.init();

    corners[0] = new PVector(100, 100);
    corners[1] = new PVector(200, 100);
    corners[2] = new PVector(200, 200);
    corners[3] = new PVector(100, 200);

    cursor(CROSS);
}

void draw(){

  PImage img = camera.getImage();
    if(img == null)
	return;

    background(0);
    image(img, 0, 0, width, height);


  fill(255, 100);
  quad(corners[0].x, corners[0].y,
       corners[1].x, corners[1].y,
       corners[2].x, corners[2].y,
       corners[3].x, corners[3].y);


  if(view != null){
      image(view, 0, 0, 100, 100);
  }

}

boolean test = false;
int currentPt=0;

void mouseDragged() {
  corners[currentPt] = new PVector(mouseX, mouseY);
}



PImage view = null;

void keyPressed() {

  if (key == '1')
    currentPt = 0;

  if (key == '2')
    currentPt = 1;

  if (key == '3')
    currentPt = 2;

  if (key == '4')
    currentPt = 3;

  if (key == 't')
    test = !test;

  if (key == 's') {

      boardView.setCorners(corners);
      view = boardView.getViewOf(camera);
      //      papart.saveCalibration("cameraPaperForTouch.xml", mat);
      view.save(sketchPath("view.bmp"));
      println("Saved");
  }
}
