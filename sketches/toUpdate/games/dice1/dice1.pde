 import fr.inria.papart.procam.*;
import fr.inria.papart.drawingapp.*;
import fr.inria.papart.tools.*;
import fr.inria.papart.kinect.*;
import fr.inria.papart.multitouchKinect.*;

import processing.video.*;
import fr.inria.papart.tools.* ;

import javax.media.opengl.GL;
import processing.opengl.*;


// Loading javaCV and javaCPP
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
//import com.googlecode.javacv.processing.*;

// The libraries are using Toxiclibs
import toxi.processing.*;
import toxi.geom.*;
import toxi.math.*;
import toxi.geom.mesh.*;


// Undecorated frame 
public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify(); 
  super.init();
}


MarkerBoard markerBoard;
PVector boardSize = new PVector(297, 210);   //  21 * 29.7 cm
float boardResolution = 3;  // 3 pixels / mm

PaperTouchScreen myApp;

public void setup(){

    size(frameSizeX, frameSizeY, OPENGL);

    initCamera();
    initKinect();


    //  String markerBoardFileLeft = sketchPath + "/data/my_markerboardmini.cfg";
    // String markerBoardFileLeft = sketchPath + "/data/markers/a3/A3p1.cfg";
    String markerFile = sketchPath + "/data/markers/a3/small/A3-small1.cfg";

    markerBoard =  new MarkerBoard(markerFile, "my Markerboard", 
				   (int) boardSize.x, (int)boardSize.y); 

    myApp = new MyApp(this, markerBoard, 
						boardSize, boardResolution, 
					   cameraTracking, projector, touchInput);

    myApp.init();
}


void draw(){

    myApp.draw();
    projector.drawScreens();

    DrawUtils.drawImage((PGraphicsOpenGL) g, 
    			projector.distort(test),
    			0, 0, frameSizeX, frameSizeY);
    

}


boolean test = true;

void keyPressed() {
  
  if(key == 't')
    test = !test;

  // Placed here, bug if it is placed in setup().
  if(key == ' ')
    frame.setLocation(framePosX, framePosY);

}
