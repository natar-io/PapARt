import fr.inria.papart.procam.*;
import fr.inria.papart.procam.display.*;
import fr.inria.papart.drawingapp.*;
import fr.inria.papart.tools.*;
import org.bytedeco.javacpp.*;
import TUIO.*;
import toxi.geom.*;

// Undecorated frame 
public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify(); 
  super.init();
}

Papart papart;
ProjectorDisplay projector;

public void setup() {

  Papart.projectionOnly(this);

  papart =  Papart.getPapart();
  projector = papart.getProjectorDisplay();
  projector.manualMode();

  object = new PVector[4];
  image = new PVector[4]; 

  // 10 cm de côté. 
  object[0] = new PVector(0, 0, 0);
  object[1] = new PVector(100, 0, 0);
  object[2] = new PVector(100, 100, 0);
  object[3] = new PVector(0, 100, 0);

  image[0] = new PVector(100, 100);
  image[1] = new PVector(200, 100);
  image[2] = new PVector(200, 200);
  image[3] = new PVector(100, 200);
}

PMatrix3D mat, pos;
PVector object[];
PVector image[];

void draw() {

  ProjectiveDeviceP pdp = projector.getProjectiveDeviceP();
  mat = pdp.estimateOrientation(object, image);
  //    mat.print();

  PGraphicsOpenGL g1 = projector.beginDraw();  
  g1.background(69, 145, 181);


  g1.modelview.apply(mat);

  g1.fill(50, 50, 200);
  // g1.translate(-10, -10, 0);
  g1.rect(-10, -10, 120, 120);

//  g1.translate(160, 90, 0);

  g1.fill(0, 191, 100);
  g1.rect(150, 80, 50, 50);

  projector.endDraw();

  DrawUtils.drawImage((PGraphicsOpenGL) g, 
  projector.render(), 
  0, 0, width, height);

  quad(image[0].x, image[0].y, 
  image[1].x, image[1].y, 
  image[2].x, image[2].y, 
  image[3].x, image[3].y);

  if (test) { 
    mat.print();
    test = false;
  }
}

void mouseDragged() {

  image[currentPt] = new PVector(mouseX, mouseY);
}


boolean test = true;
int currentPt = 0;
boolean set = false;

void keyPressed() {

  if (key == '0')
    currentPt = 0;

  if (key == '1')
    currentPt = 1;

  if (key == '2')
    currentPt = 2;

  if (key == '3')
    currentPt = 3;

  if (key == 't')
    test = !test;

  if (key == 's') {
      papart.setTableLocation(mat);
      println("Saved");
  }
}

