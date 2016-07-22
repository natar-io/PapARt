import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.multitouch.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.calibration.*;
import fr.inria.papart.procam.Utils;

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

import java.io.*;
import java.nio.channels.FileChannel;


Skatolo skatolo;

CameraConfiguration cameraConfig;
CameraConfiguration kinectConfig;
ScreenConfiguration screenConfig;

int nbScreens = 1;
PImage backgroundImage;

public void settings() {
    size(800, 840);
}

TestView testView;

void setup(){

    cameraConfig = new CameraConfiguration();
    kinectConfig = new CameraConfiguration();
    screenConfig = new ScreenConfiguration();

    cameraConfig.loadFrom(this, Papart.cameraConfig);
    kinectConfig.loadFrom(this, Papart.cameraKinectConfig);
    screenConfig.loadFrom(this, Papart.screenConfig);

    initUI();
    backgroundImage = loadImage("data/background.png");
    tryLoadCameraCalibration();
    tryLoadProjectorCalibration();

    // test subApplet.
    testView = new TestView();
}


int cameraWidth, cameraHeight;
int projectorWidth, projectorHeight;
boolean cameraCalibrationOk = false;
boolean projectorCalibrationOk = false;

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

void tryLoadProjectorCalibration(){
    try{
	String calibrationYAML = Papart.projectorCalib;
	ProjectiveDeviceP pdp = ProjectiveDeviceP.loadProjectorDevice(this, calibrationYAML);
	projectorWidth = pdp.getWidth();
        projectorHeight = pdp.getHeight();
        projectorCalibrationOk = true;
	//println(cameraWidth + " " + cameraHeight);
    } catch(Exception e){

    }
}

void testProjection(boolean value){
    if(value){
	testView.testProjector();
    }
}

void testCameraButton(boolean value){
    println("Start pressed " + value);

    if(value){
	testView.testCamera();
    }

}


void cameraTypeChooser(int value){
    if(value >= 0){
        cameraConfig.setCameraType(Camera.Type.values()[value]);
    }
}

void kinectTypeChooser(int value){
    if(value >= 0){
        kinectConfig.setCameraType(Camera.Type.values()[value]);
    }
}

void testKinectButton(boolean value){

    if(value){
	testView.testKinect();
    }

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

public void switchToCalibration(boolean value){
    println("Switch !");
    Utils.runExample("calibration/all", true);

    try{
         Thread.sleep(12000);
     }catch(Exception e){}
    exit();
}


int rectSize = 30;

void draw(){

        // cColor = new CColor(color(49,51,50),
    // 	       color(51),
    // 	       color(71),
    // 	       color(255),
    // 	       color(255));

     // initCameraUI();
     // updateStyles();

    image(backgroundImage, 0, 0);

    if(cameraCalibrationOk){
        text("Resolution : " + cameraWidth + "x" + cameraHeight , 400, 415);
    }

    if(projectorCalibrationOk){
        text("Resolution : " + projectorWidth + "x" + projectorHeight ,350, 207);
    }


}



void keyPressed() {
    if (key == 27) {
	//The ASCII code for esc is 27, so therefore: 27
     //insert your function here
        println("Yoo");
    }
    if (key == ESC)
        key=0;
}



// Todo: custom file chooser.
void saveCameraAs(){
    selectOutput("Select a file to write to:", "fileSelectedSaveCamera");
}

void fileSelectedSaveCamera(File selection) {
    saveCamera(selection.getAbsolutePath());
}


void loadCalibration(){
    File defaultCameraCalibration = new File(Papart.cameraCalib);
    selectOutput("Choose the calibration YAML file:", "fileSelectedLoadCalibration", defaultCameraCalibration);
}

// TODO custom file chooser...
void fileSelectedLoadCalibration(File selection){

    if(selection == null){
        return;
    }

    if(selection.getName().endsWith(".yaml")){
        File defaultCameraCalibration = new File(Papart.cameraCalib);
        try{
            copy(selection, defaultCameraCalibration);

            tryLoadCameraCalibration();
            println("New calibration set! ");

        } catch(Exception e){
            println("Error while copying file " + e);
        }
    }else {
        println("You should select a YAML calibration file");
    }
}


public void copy(File src, File dst) throws IOException {
    FileInputStream inStream = new FileInputStream(src);
    FileOutputStream outStream = new FileOutputStream(dst);
    FileChannel inChannel = inStream.getChannel();
    FileChannel outChannel = outStream.getChannel();
    inChannel.transferTo(0, inChannel.size(), outChannel);
    inStream.close();
    outStream.close();
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
void saveKinectAs(){
    selectOutput("Select a file to write to:", "fileSelectedSaveKinect");
}

void fileSelectedSaveKinect(File selection) {
    saveKinect(selection.getAbsolutePath());
}

void saveDefaultKinect(){
    saveKinect(Papart.cameraKinectConfig);
}

void saveKinect(String fileName){
    kinectConfig.setCameraName(kinectIdText.getText());
    kinectConfig.saveTo(this, fileName);
    println("Kinect saved.");
}



void loadProjectorCalibration(){
    File defaultProjectorCalibration = new File(Papart.projectorCalib);
    selectOutput("Choose the projectorCalibration YAML file:",
                 "fileSelectedLoadProjectorCalibration",
                 defaultProjectorCalibration);
}

// TODO custom file chooser...
void fileSelectedLoadProjectorCalibration(File selection){

    if(selection == null){
        return;
    }
    if(selection.getName().endsWith(".yaml")){
        File defaultProjectorCalibration = new File(Papart.projectorCalib);
        try{
            copy(selection, defaultProjectorCalibration);
            tryLoadProjectorCalibration();
            println("New projector calibration set! ");
        } catch(Exception e){
            println("Error while copying file " + e);
        }
    }else {
        println("You should select a YAML projector calibration file");
    }
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

void updateScreenConfig(){
    try{
	screenConfig.setProjectionScreenOffsetX(Integer.parseInt(posXText.getText()));
	screenConfig.setProjectionScreenOffsetY(Integer.parseInt(posYText.getText()));
    }catch(java.lang.NumberFormatException e){
	println("Invalid Position");
    }

}

void saveScreen(String fileName){

    updateScreenConfig();

    screenConfig.saveTo(this, fileName);
    println("Default screen saved.");
}
