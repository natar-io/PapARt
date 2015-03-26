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

ProjectorDisplay projector;

int frameSizeX = 1280;
int frameSizeY = 720;

int framePosX = 0;
int framePosY = 200;

public void setup(){

    size(frameSizeX, frameSizeY, OPENGL);
    
    //    String procamCalib = sketchPath + "/data/calibration/calibration-p1.yaml";
    projector = new ProjectorDisplay(this, Papart.proCamCalib);
    projector.init();

    object = new PVector[4];
    image = new PVector[4]; 
    
    object[0] = new PVector(0, 0, 0);
    object[1] = new PVector(100, 0, 0);
    object[2] = new PVector(100, 100, 0);
    object[3] = new PVector(0, 100, 0);
    
    image[0] = new PVector(100, 100);
    image[1] = new PVector(200,  100);
    image[2] = new PVector(200,  200);
    image[3] = new PVector(100,  200);
}

PMatrix3D mat, pos;
PVector object[];
PVector image[];

void draw(){
 
    if(set)
	image[currentPt] = new PVector(mouseX, mouseY);
    
    
    ProjectiveDeviceP pdp = projector.getProjectiveDeviceP();
    mat = pdp.estimateOrientation(object, image);
    //    mat.print();
    
    PGraphicsOpenGL g1 = projector.beginDraw();  
    g1.background(50, 20, 20);


    g1.modelview.apply(mat);
    
    g1.fill(50, 50, 200);
    g1.translate(-10, -10, 0);
    g1.rect(0, 0, 120, 120);
    
    g1.translate(200, 200, 0);

    g1.fill(0, 200, 100);
    g1.rect(0, 0, 50, 50);

    projector.endDraw();
    
    DrawUtils.drawImage((PGraphicsOpenGL) g, 
			projector.render(),
			0, 0, frameSizeX, frameSizeY);
    
    quad(image[0].x, image[0].y, 
	 image[1].x, image[1].y, 
	 image[2].x, image[2].y, 
	 image[3].x, image[3].y);
    
    if(test){ 
	mat.print();
	test = false;
    }
    
}


boolean test = true;
int currentPt = 0;
boolean set = false;

void keyPressed() {

  if(key == '0')
    currentPt = 0;
  
  if(key == '1')
    currentPt = 1;
    
  if(key == '2')
    currentPt = 2;
  
  if(key == '3')
    currentPt = 3;
    
  if(key == 's'){
     set = !set;
  }  
      
    
  
  if(key == 't')
    test = !test;

  // Placed here, bug if it is placed in setup().
  if(key == ' ')
    frame.setLocation(framePosX, framePosY);

}
