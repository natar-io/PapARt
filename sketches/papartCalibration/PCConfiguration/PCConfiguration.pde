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
    
    cp5 = new ControlP5(this);
    PFont font = createFont("arial",20);

    cp5.addRadioButton("cameraTypeChooser")
	.setPosition(50,20)
	.setItemWidth(20)
	.setItemHeight(20)
	.addItem("OpenCV", 0)
	.addItem("Processing", 1)
	.addItem("OpenKinect", 2)
	.addItem("FlyCapture", 3)
	.setColorLabel(color(255))
	.activate(0)
	;


    cameraIdText = cp5.addTextfield("CameraId")
	.setPosition(200 ,50)
	.setSize(200,20)
	.setFont(font)
	.setLabel("Camera ID")
	.setText(cc.getCameraName())
	.setFocus(true)
	;

    startToggle = cp5.addToggle("startButton")
	.setPosition(430, 50)
	.setSize(20, 20)
	.setLabel("Start")
	;

    cp5.addButton("save")
	.setPosition(460, 50)
	.setSize(40, 20)
	;

    println("You should see three rectangles at each corner of the projection screen.");
   
    
    screenChooser = cp5.addRadioButton("screenChooserRadio")
	.setPosition(50,150)
	.setLabel("Screen Chooser")
	.setItemWidth(20)
	.setItemHeight(20)
	.setColorLabel(color(255))
	.activate(0)
	;

    posXText = cp5.addTextfield("PosX")
	.setPosition(50, 300)
	.setSize(80,20)
	.setFont(font)
	.setLabel("Position X")
	.setText(Integer.toString(cc.getProjectionScreenOffsetX()))
	;


    posYText = cp5.addTextfield("PosY")
	.setPosition(50, 340)
	.setSize(80,20)
	.setFont(font)
	.setLabel("Position Y")
	.setText(Integer.toString(cc.getProjectionScreenOffsetY()))
	;




    cp5.addButton("initSecondApplet")
	.setPosition(400, 150)
	.setLabel("Display on Projection Screen")
	.setSize(200, 20)
	;


    String[] descriptions = CanvasFrame.getScreenDescriptions();
    int k = 0;
    for(String description : descriptions){
	println(description);
	DisplayMode displayMode = CanvasFrame.getDisplayMode(k); 
	screenChooser.addItem("Screen " 
			      + description 
			      + " -- Resolution  " + displayMode.getWidth() 
			      + "x" + displayMode.getHeight(), k);

	k++;
    }
    nbScreens = k;

}

Textfield cameraIdText, posXText, posYText;
Toggle startToggle;
RadioButton screenChooser;


void startButton(boolean value){
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

    background(150);

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
