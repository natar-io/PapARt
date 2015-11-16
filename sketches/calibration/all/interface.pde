import fr.inria.skatolo.*;
import fr.inria.skatolo.gui.group.*;
import fr.inria.skatolo.gui.controllers.*;
import fr.inria.skatolo.gui.controllers.Slider;
import fr.inria.skatolo.gui.controllers.Button;

import fr.inria.papart.procam.Utils;


public class ControlFrame extends PApplet {

    boolean init = false;

    public ControlFrame() {
	super();
	PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }

    public void settings() {
	size(800, 800);
    }

    Skatolo skatolo;

    RadioButton corners, camRadio, projRadio, kinectRadio = null;
    Slider sliderObjectWidth, sliderObjectHeight;
    Bang saveCameraPaperBang, saveProjectorPaperBang, saveKinectPaperBang;
    Textarea textArea;
    Group cornersGroup;
    Textlabel cameraPaperLabel, projectorPaperLabel, kinectPaperLabel;

    Bang calibrateProCam, calibrateKinectCam, addProCamCalibration;
    Bang calibratePlaneTouch;

    Bang useProCamFromIntrinsics;

    Slider kinectStereoSliderX, kinectStereoSliderY;
    Bang kinectStereoBang;

    public void setText(String text){
        if(!init)
            return;
        textArea.setText(text);
    }

    public void setText(PMatrix3D matrix){
        if(!init)
            return;

        if(matrix == null){
            println("Null matrix, no display.");
            return;
        }
        textArea.setText(matToString(matrix));
    }

    public void hideCorners(){
        cornersGroup.hide();
//        corners.hide();
    }

    public void showCorners(){
        cornersGroup.show();
//        corners.show();
    }

    public void activateCornerNo(int nb){
        corners.activate(nb);
        // TODO: why is this not automatic ?
        activeCorner(nb);
    }

    public void hideObjectSize(){
        sliderObjectWidth.hide();
        sliderObjectHeight.hide();
    }

    public void showObjectSize(){
        sliderObjectWidth.show();
        sliderObjectHeight.show();
    }

    public void resetCamRadio(){
        camRadio.deactivateAll();
    }

    public void resetProjRadio(){
        projRadio.deactivateAll();
    }

    public void resetKinectRadio(){
        if(kinectRadio != null)
            kinectRadio.deactivateAll();
    }

    public void showSaveCameraButton(){
        saveCameraPaperBang.show();
    }

    public void hideSaveCameraButton(){
        saveCameraPaperBang.hide();
    }

    public void showSaveProjectorButton(){
        saveProjectorPaperBang.show();
    }

    public void hideSaveProjectorButton(){
        saveProjectorPaperBang.hide();
    }

    public void showSaveKinectButton(){
        saveKinectPaperBang.show();
    }

    public void hideSaveKinectButton(){
        if(saveKinectPaperBang != null)
            saveKinectPaperBang.hide();
    }


    public void setCameraPaperLabel(String text){
        cameraPaperLabel.setValue(text);
    }

    public void setProjectorPaperLabel(String text){
        projectorPaperLabel.setValue(text);
    }

    public void setKinectPaperLabel(String text){
        kinectPaperLabel.setValue(text);
    }

    public void showCalibrateProCam(){
        calibrateProCam.show();
    }

    public void hideCalibrateProCam(){
        calibrateProCam.hide();
    }

    public void showAddProCamCalibration(){
        addProCamCalibration.show();
    }

    public void hideAddProCamCalibration(){
        addProCamCalibration.hide();
    }

    public void showCalibrateKinectCam(){
        calibrateKinectCam.show();
    }

    public void hideCalibrateKinectCam(){
        calibrateKinectCam.hide();
    }

    public void hideKinectStereoSliders(){
        kinectStereoSliderX.hide();
        kinectStereoSliderY.hide();
        kinectStereoBang.hide();
    }

    public void showKinectStereoSliders(){
        kinectStereoSliderX.show();
        kinectStereoSliderY.show();
    }

    public void showSaveBangKinectStereo(){
        kinectStereoBang.show();
    }

    public void hideSaveBangKinectStereo(){
        kinectStereoBang.hide();
    }

    public void switchToPCConfiguration(){
        println("Switch !");
        Utils.runExample("calibration/PCConfiguration", true);

        try{
            Thread.sleep(8000);
        }catch(Exception e){}
        exit();
    }

    public void setup() {
        frameRate(20);
        skatolo = new Skatolo(this);
        initInterface();
        initCorners();

        hideObjectSize();
        hideCorners();

        initKinectInterface();
    }

    public void initCorners(){
        cornersGroup = skatolo.addGroup("CornersGroup")
            .setPosition(499,106)
            // .setWidth(300)
            // .setHeight(300)
            .activateEvent(true)
            .setLabel("Corners.")
            ;

        corners = skatolo.addRadioButton("Corners")
            .setPosition(1,73)
            .setItemWidth(20)
            .setItemHeight(20)
            .addItem("bottom Left", 0) // 0, y
            .addItem("bottom Right", 1) // x ,y
            .addItem("top right", 2)  // x, 0
            .addItem("Top Left ", 3)  // 0, 0
            .activate(0)
            .setGroup("CornersGroup")
            .plugTo(mainApplet, "activeCorner")
            ;

        skatolo.addBang("Save Corners")
            .setPosition(81, 10)
            .setSize(20, 20)
            .setGroup("CornersGroup")
            .plugTo(mainApplet, "saveCorners")
            ;

        skatolo.addBang("Load Corners")
            .setPosition(-2, 10)
            .setSize(20, 20)
            .setGroup("CornersGroup")
            .plugTo(mainApplet, "loadCorners")
            ;

        skatolo.addToggle("Show Zoom")
            .setPosition(80,73)
            .setSize(20, 20)
            .setGroup("CornersGroup")
            .plugTo(mainApplet, "showCornerZoom")
            ;

        sliderObjectWidth = skatolo.addSlider("ObjectWidth")
            .setPosition(-75, 183 )
            .setValue(objectWidth)
            .setRange(200, 500)
            .setSize(300, 12)
            .setGroup("CornersGroup")
        .plugTo(mainApplet, "objectWidth")
             ;

        sliderObjectHeight = skatolo.addSlider("ObjectHeight")
            .setPosition(-75,200 )
            .setValue(objectHeight)
            .setRange(200, 400)
            .setSize(200, 12)
            .setGroup("CornersGroup")
            .plugTo(mainApplet, "objectHeight")
            ;
    }

    public void initInterface(){

        skatolo.addBang("switchToPCConfiguration")
            .setLabel("Switch to PCConfiguration")
            .setPosition(649, 20)
            ;

        // add a horizontal sliders, the value of this slider will be linked
        // to variable 'sliderValue'
        calibrateProCam =  skatolo.addBang("calibrate ProCam")
            .plugTo(mainApplet, "calibrateProCam")
            .setPosition(300, 10)
            ;

        calibrateProCam.hide();

        addProCamCalibration =  skatolo.addBang("add ProCam locations")
            .plugTo(mainApplet, "addProCamCalibrationData")
            .setPosition(10, 10)
            ;

        addProCamCalibration.hide();


        camRadio = skatolo.addRadioButton("Camera calibration").plugTo(mainApplet, "camMode")
            .setPosition(12, 100)
            .setItemWidth(20)
            .setItemHeight(20)
            .addItem("CamView", 0)
            .addItem("CamMarker", 1)
            .addItem("CamManual", 2)
            .setColorLabel(color(255))
            ;

        saveCameraPaperBang = skatolo.addBang("Save Cam - Paper Location")
            .plugTo(mainApplet, "saveCameraPaper")
            .setPosition(214, 123)
            .setSize(20, 20)
            ;
        saveCameraPaperBang.hide();

        cameraPaperLabel = skatolo.addTextlabel("cameraPaperLabel",
                                                "Please set the calibration.",
                                                209,
                                                102);

        projectorPaperLabel = skatolo.addTextlabel("projectorPaperLabel",
                                                   "Please set the calibration.",
                                                   209,
                                                   216);


        saveProjectorPaperBang = skatolo.addBang("Save Proj - Paper Location")
            .plugTo(mainApplet, "saveProjectorPaper")
            .setPosition(214, 239)
            .setSize(20, 20)
            ;
        saveProjectorPaperBang.hide();




        projRadio = skatolo.addRadioButton("Projector calibration").plugTo(mainApplet, "projMode")
            .setPosition(12, 212)
            .setItemWidth(20)
            .setItemHeight(20)
            .addItem("ProjManual", 0)
            .addItem("ProjMarker", 1)
            .addItem("ProjView", 2)
            .setColorLabel(color(255))
            ;

        useProCamFromIntrinsics = skatolo.addBang("Use previous calibration").
            plugTo(mainApplet,"useLastExtrinsics")
            .setPosition(425, 52)
            .setSize(20, 20)
            ;


        skatolo.addBang("clear calibrations").
            plugTo(mainApplet,"clearCalibrations")
            .setPosition(415, 82)
            .setSize(20, 20)
            ;


        textArea = skatolo.addTextarea("txt")
            .setPosition(150,400)
            .setSize(450,200)
            .setFont(createFont("arial",12))
            .setLineHeight(14)
            //.setColor(color(128))
            ;
            // .setColorBackground(color(255,100))
            // .setColorForeground(color(255,100));


        // skatolo.addSlider("captureTime").plugTo(mainApplet, "captureTime")
        //     .setPosition(10, 40)
        //     .setRange(30, 1200)
        //     .setValue(captureTime)
        //     ;

        // skatolo.addSlider("delay").plugTo(mainApplet, "delay")
        //     .setPosition(10, 60)
        //     .setRange(0, 300)
        //     .setValue(delay)
        //     ;
        init = true;
    }

    public void initKinectInterface(){

        // Note : Modes should exist even when not activated.

        // If a Kinect is available
        if(isKinectOne || isKinect360){
            // activateKinectBang = skatolo.addBang("activateKinect")
            //     .setPosition(100,480)
            //     .setSize(20, 20)
            //     .plugTo(mainApplet, "activateKinect")
            //     ;

            kinectRadio = skatolo.addRadioButton("Kinect calibration").plugTo(mainApplet, "kinectMode")
                .setPosition(13, 318)
                .setItemWidth(20)
                .setItemHeight(20)
                .addItem("Kinect3D", 0)
                .setColorLabel(color(255))
                ;

            calibrateKinectCam =  skatolo.addBang("calibrate KinectCam")
                .plugTo(mainApplet, "calibrateKinectCam")
                .setPosition(320, 250)
                ;

            calibrateKinectCam.hide();


            calibratePlaneTouch = skatolo.addBang("calibrate KinectPlane")
                .plugTo(mainApplet, "calibrateKinect360Plane")
                .setPosition(320, 290);
                ;
// TODO: hide / show etc..

            kinectStereoBang = skatolo.addBang("saveStereo")
                .plugTo(mainApplet, "saveStereoKinect")
                .setPosition(205, 471)
                ;

             kinectStereoSliderX = skatolo.addSlider("translationX").plugTo(mainApplet, "kinectStereoX")
                .setPosition(38, 431)
                .setValue(stereoCalib.m03)
                .setRange(-80, 50)
                .setSize(400, 12);
            // Manual draw.

             kinectStereoSliderY = skatolo.addSlider("translationY").plugTo(mainApplet, "kinectStereoY")
                .setPosition(39, 448)
                .setValue(stereoCalib.m13)
                .setRange(-50, 50)
                .setSize(400, 12);

             hideKinectStereoSliders();

            if(isKinect360){

                kinectRadio.addItem("KinectManual", 1)
                    .addItem("KinectMarker", 2)
                    .setColorLabel(color(255))
                    ;

                saveKinectPaperBang = skatolo.addBang("Save Kinect - Paper Location")
                    .plugTo(mainApplet, "saveKinectPaper")
                    .setPosition(213, 343)
                    .setSize(20, 20)
                    ;
                saveKinectPaperBang.hide();

                kinectPaperLabel = skatolo.addTextlabel("kinectPaperLabel",
                                                        "Please save the calibration.",
                                                        212,
                                                        324);

            }

        }
    }

    public void draw() {
        background(100);

// initInterface();
   // initCorners();
 // initKinectInterface();

        fill(255);

        text("Camera : " + cameraName , 11, 89);
        text("Projector : " + screenName , 10, 203);
        text("Kinect : " + kinectName , 12, 306);


        pushMatrix();
        translate(300, 12);

        text("Calibration No : " + calibrationNumber , 0, 0);
        fill(0, 255, 0);
        for(int i = 0; i < calibrationNumber; i++){
            translate(10, 0);
            rect(0, 0, 10, 10);
        }
        popMatrix();

    }

    public Skatolo control() {
        return skatolo;
    }

    void keyPressed(){
        //   initInterface();
    }
}
