import fr.inria.papart.procam.*;
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

//PaperScreen paperScreenTest;
Projector projector;

int frameSizeX = 1280;
int frameSizeY = 720;

int framePosX = 0;
int framePosY = 200;

public void setup(){
    size(frameSizeX, frameSizeY, OPENGL);
    projector = new Projector(this, Papart.proCamCalib);
}

void draw(){
 
    
    PGraphicsOpenGL graphics = projector.beginDraw();  
    graphics.background(50, 20, 20);
    
    graphics.translate(-500, -600, 2000);
    
    graphics.fill(50, 50, 200);
    graphics.translate(-10, -10, 0);
    graphics.rect(0, 0, 120, 120);
    
    graphics.translate(200, 200, 0);

    graphics.fill(0, 200, 100);
    graphics.rect(0, 0, 50, 50);

    projector.endDraw();
    
    DrawUtils.drawImage((PGraphicsOpenGL) g, 
			projector.render(),
			0, 0, frameSizeX, frameSizeY);
    
}


void keyPressed() {

  // Placed here, bug if it is placed in setup().
  if(key == ' ')
    frame.setLocation(framePosX, framePosY);
}
