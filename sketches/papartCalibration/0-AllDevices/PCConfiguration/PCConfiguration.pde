import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.multitouch.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.calibration.*;

import toxi.geom.*;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.freenect;
import org.bytedeco.javacpp.opencv_core.*;


import fr.inria.skatolo.*;
import fr.inria.skatolo.events.*;
import fr.inria.skatolo.gui.*;
import fr.inria.skatolo.gui.group.*;
import fr.inria.skatolo.gui.controllers.*;

import processing.video.*;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import org.bytedeco.javacv.CanvasFrame;

Skatolo skatolo;

CameraConfiguration cameraConfig;
ScreenConfiguration screenConfig;

int nbScreens = 1;
PImage backgroundImage;

public void settings() {
    size(800, 600, P3D);
}

void setup(){


    // Here camera, test it with defaultCameraTest
    // cc.setCameraName("1");
    // cc.setCameraType(Camera.Type.OPENCV);

    // Camera.Type.OPENCV, "2"
    // Camera.Type.PROCESSING, "/dev/video1"
    // Camera.Type.FLY_CAPTURE, 0

    // TODO: sesizable & movable. Save Size and location !

    // Here Screen resolution
    // cc.setProjectionScreenWidth(1280);
    // cc.setProjectionScreenHeight(800);

    // Screen offset, where is the projection screen, relative to the main screen.
    // cc.setProjectionScreenOffsetX(0);
    // cc.setProjectionScreenOffsetY(200);

    // Do not modify anything further.

    cameraConfig = new CameraConfiguration();
    screenConfig = new ScreenConfiguration();

    cameraConfig.loadFrom(this, Papart.cameraConfig);
    screenConfig.loadFrom(this, Papart.screenConfig);

    initUI();
    backgroundImage = loadImage("data/background.png");
    tryLoadCameraCalibration();
}


int cameraWidth, cameraHeight;
boolean cameraCalibrationOk = false;

void tryLoadCameraCalibration(){

    try{
	String calibrationYAML = Papart.cameraCalib;
	ProjectiveDeviceP pdp = ProjectiveDeviceP.loadCameraDevice(this, calibrationYAML);
	cameraWidth = pdp.getWidth();
	cameraHeight = pdp.getHeight();
	cameraCalibrationOk = true;
	println(cameraWidth + " " + cameraHeight);
    } catch(Exception e){

    }
}


void testCameraButton(boolean value){
    println("Start pressed " + value);

    cameraConfig.setCameraName(cameraIdText.getText());

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
	cameraConfig.setCameraType(Camera.Type.OPENCV);

    if(value == 1)
	cameraConfig.setCameraType(Camera.Type.PROCESSING);

    if(value == 2)
	cameraConfig.setCameraType(Camera.Type.OPEN_KINECT);

    if(value == 3)
	cameraConfig.setCameraType(Camera.Type.FLY_CAPTURE);

    if(value == 4)
	cameraConfig.setCameraType(Camera.Type.KINECT2_RGB);

    if(value == 5)
	cameraConfig.setCameraType(Camera.Type.KINECT2_IR);

}

void screenChooserRadio(int value){

    if(value > 0 && value < nbScreens){
	PVector resolution = getScreenResolution(value);
	screenConfig.setProjectionScreenWidth((int) resolution.x);
	screenConfig.setProjectionScreenHeight((int) resolution.y);
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


    // cColor = new CColor(color(49,51,50),
    // 	       color(51),
    // 	       color(71),
    // 	       color(255),
    // 	       color(255));

    // updateStyles();

    image(backgroundImage, 0, 0);

}



void keyPressed() {
    if (key == 27) {
	//The ASCII code for esc is 27, so therefore: 27
     //insert your function here
     }

}



// Todo: custom file chooser.
void saveCameraAs(){
    selectOutput("Select a file to write to:", "fileSelectedSaveCamera");
}

void fileSelectedSaveCamera(File selection) {
    saveCamera(selection.getAbsolutePath());
}

void saveDefaultCamera(){
    saveCamera(Papart.cameraConfig);
}

void saveCamera(String fileName){
    cameraConfig.setCameraName(cameraIdText.getText());
    cameraConfig.saveTo(this, fileName);
    println("Camera saved.");
}



// Todo: custom file chooser.
void saveScreenAs(){
    selectOutput("Select a file to write to:", "fileSelectedSaveScreen");
}

void fileSelectedSaveScreen(File selection) {
    saveScreen(selection.getAbsolutePath());
}

void saveDefaultScreen(){
    saveScreen(Papart.screenConfig);
}

void saveScreen(String fileName){
    try{
	screenConfig.setProjectionScreenOffsetX(Integer.parseInt(posXText.getText()));
	screenConfig.setProjectionScreenOffsetY(Integer.parseInt(posYText.getText()));
    }catch(java.lang.NumberFormatException e){
	println("Invalid Position");
    }

    screenConfig.saveTo(this, fileName);
    println("Default screen saved.");
}
