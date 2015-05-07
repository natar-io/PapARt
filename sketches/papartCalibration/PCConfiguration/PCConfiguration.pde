import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.multitouch.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.calibration.*;

import toxi.geom.*;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.freenect;
import org.bytedeco.javacpp.opencv_core.*;


import fr.inria.controlP5.*;
import fr.inria.controlP5.events.*;
import fr.inria.controlP5.gui.group.*;
import fr.inria.controlP5.gui.controllers.*;

import processing.video.*;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import org.bytedeco.javacv.CanvasFrame;

ControlP5 cp5;

ComputerConfiguration cc;

int nbScreens = 1;
PImage backgroundImage;

void setup(){


    cc = new ComputerConfiguration();

    // Here camera, test it with defaultCameraTest
    cc.setCameraName("1");
    cc.setCameraType(Camera.Type.OPENCV);
    
    // Camera.Type.OPENCV, "2"    
    // Camera.Type.PROCESSING, "/dev/video1"
    // Camera.Type.FLY_CAPTURE, 0

    // TODO: sesizable & movable. Save Size and location !

    // Here Screen resolution
    cc.setProjectionScreenWidth(1280);
    cc.setProjectionScreenHeight(800);
    
    // Screen offset, where is the projection screen, relative to the main screen. 
    cc.setProjectionScreenOffsetX(0);
    cc.setProjectionScreenOffsetY(200);    


    // Do not modify anything further. 
    size(800, 600, P3D);
    

    initUI();
    backgroundImage = loadImage("data/background.png");

}


void testCameraButton(boolean value){
    println("Start pressed " + value);

    cc.setCameraName(cameraIdText.getText());

    if(value){
	testCamera();
    } else {
	if(camera != null){
	    camera.close();
	    camera = null;
	}
    }
}


void cameraTypeChooser(int value){
    
    if(value == 0)
	cc.setCameraType(Camera.Type.OPENCV);

    if(value == 1)
	cc.setCameraType(Camera.Type.PROCESSING);

    if(value == 2)
	cc.setCameraType(Camera.Type.OPEN_KINECT);

    if(value == 3)
	cc.setCameraType(Camera.Type.FLY_CAPTURE);
}

void screenChooserRadio(int value){

    if(value > 0 && value < nbScreens){
	PVector resolution = getScreenResolution(value);
	cc.setProjectionScreenWidth((int) resolution.x);
	cc.setProjectionScreenHeight((int) resolution.y);
	println("screen chooser radio");
    }
}

PVector getScreenResolution(int screenNo){
    DisplayMode displayMode = CanvasFrame.getDisplayMode(screenNo); 
    return new PVector(displayMode.getWidth(), displayMode.getHeight());
}



// TODO: test default camera in here. 

int rectSize = 30;

void draw(){
    
    int c = color(0,0,0,1);
    startCameraButton.setPosition(610, 401).setSize(142,32).setColorForeground(c).setColorBackground(c);

    image(backgroundImage, 0, 0);

}




void keyPressed() {

  // Placed here, bug if it is placed in setup().
  if(key == ' ')
      frame.setLocation(cc.getProjectionScreenOffsetX(),
			cc.getProjectionScreenOffsetY());
  
  if(key == 's')
      save();
}

void save(){
    cc.setCameraName(cameraIdText.getText());
    cc.saveTo(this, Papart.computerConfig);
    
    println("Saved to " + Papart.computerConfig);
}
