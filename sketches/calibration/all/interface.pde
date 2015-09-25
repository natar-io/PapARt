import fr.inria.skatolo.*;
import fr.inria.skatolo.gui.group.*;
import fr.inria.skatolo.gui.controllers.*;
import fr.inria.skatolo.gui.controllers.Slider;
import fr.inria.skatolo.gui.controllers.Button;

public class ControlFrame extends PApplet {

    boolean init = false;

    public ControlFrame() {
	super();
	PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }

    public void settings() {
	size(800, 600, P3D);
    }

    Skatolo skatolo;

    RadioButton corners, camRadio, projRadio, kinectRadio = null;
    Slider sliderObjectWidth, sliderObjectHeight;
    Bang saveCameraPaperBang, saveProjectorPaperBang, saveKinectPaperBang;
    Textarea textArea;
    Group cornersGroup;
    Textlabel cameraPaperLabel, projectorPaperLabel, kinectPaperLabel;
    Bang calibrateProCam, calibrateKinectCam;

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

    public void showCalibrateKinectCam(){
        calibrateKinectCam.show();
    }

    public void hideCalibrateKinectCam(){
        calibrateKinectCam.hide();
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
        frameRate(25);
        skatolo = new Skatolo(this);


        skatolo.addBang("switchToPCConfiguration")
            .setLabel("Switch to PCConfiguration")
            .setPosition(400, 20)
            ;

        // add a horizontal sliders, the value of this slider will be linked
        // to variable 'sliderValue'
        calibrateProCam =  skatolo.addBang("calibrate ProCam").plugTo(mainApplet, "calibrateProCam")
            .setPosition(10, 10)
            ;

        calibrateProCam.hide();


        camRadio = skatolo.addRadioButton("Camera calibration").plugTo(mainApplet, "camMode")
            .setPosition(100, 100)
            .setItemWidth(20)
            .setItemHeight(20)
            .addItem("CamView", 0)
            .addItem("CamMarker", 1)
            .addItem("CamManual", 2)
            .setColorLabel(color(255))
            ;

        saveCameraPaperBang = skatolo.addBang("Save Cam - Paper Location")
            .plugTo(mainApplet, "saveCameraPaper")
            .setPosition(200, 100)
            .setSize(20, 20)
            ;
        saveCameraPaperBang.hide();

        cameraPaperLabel = skatolo.addTextlabel("cameraPaperLabel",
                                                "Please save the calibration.",
                                                200,
                                                90);

        projectorPaperLabel = skatolo.addTextlabel("projectorPaperLabel",
                                                   "Please save the calibration.",
                                                   200,
                                                   260);


        saveProjectorPaperBang = skatolo.addBang("Save Proj - Paper Location")
            .plugTo(mainApplet, "saveProjectorPaper")
            .setPosition(200, 250)
            .setSize(20, 20)
            ;
        saveProjectorPaperBang.hide();




        projRadio = skatolo.addRadioButton("Projector calibration").plugTo(mainApplet, "projMode")
            .setPosition(100, 250)
            .setItemWidth(20)
            .setItemHeight(20)
            .addItem("ProjManual", 0)
            .addItem("ProjMarker", 1)
            .addItem("ProjView", 2)
            .setColorLabel(color(255))
            ;



        cornersGroup = skatolo.addGroup("CornersGroup")
            .setPosition(400,50)
            // .setWidth(300)
            // .setHeight(300)
            .activateEvent(true)
            .setLabel("Corners.")
            ;

        corners = skatolo.addRadioButton("Corners")
            .setPosition(0,0)
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
            .setPosition(100, 100)
            .setSize(20, 20)
            .setGroup("CornersGroup")
            .plugTo(mainApplet, "saveCorners")
            ;

        skatolo.addBang("Load Corners")
            .setPosition(100, 50)
            .setSize(20, 20)
            .setGroup("CornersGroup")
            .plugTo(mainApplet, "loadCorners")
            ;

        sliderObjectWidth = skatolo.addSlider("ObjectWidth")
            .setPosition(400, 150 )
            .setValue(objectWidth)
            .setRange(200, 500)
            .setSize(300, 12)
            .setGroup("CornersGroup")
        .plugTo(mainApplet, "objectWidth")
             ;

        sliderObjectHeight = skatolo.addSlider("ObjectHeight")
            .setPosition(400,180 )
            .setValue(objectHeight)
            .setRange(200, 400)
            .setSize(200, 12)
            .setGroup("CornersGroup")
            .plugTo(mainApplet, "objectHeight")
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



        // Note : Modes should exist even when not activated.

        // If a Kinect is available
        if(isKinectOne || isKinect360){
            // activateKinectBang = skatolo.addBang("activateKinect")
            //     .setPosition(100,480)
            //     .setSize(20, 20)
            //     .plugTo(mainApplet, "activateKinect")
            //     ;



            kinectRadio = skatolo.addRadioButton("Kinect calibration").plugTo(mainApplet, "kinectMode")
                .setPosition(100, 450)
                .setItemWidth(20)
                .setItemHeight(20)
                .addItem("Kinect3D", 0)
                .setColorLabel(color(255))
                ;

            calibrateKinectCam =  skatolo.addBang("calibrate KinectCam").plugTo(mainApplet, "calibrateKinectCam")
                .setPosition(120, 10)
                ;

            calibrateKinectCam.hide();


            if(isKinect360){

                kinectRadio.addItem("KinectManual", 1)
                    .addItem("KinectMarker", 2)
                    .setColorLabel(color(255))
                    ;

                saveKinectPaperBang = skatolo.addBang("Save Kinect - Paper Location")
                    .plugTo(mainApplet, "saveKinectPaper")
                    .setPosition(200, 450)
                    .setSize(20, 20)
                    ;
                saveKinectPaperBang.hide();

                kinectPaperLabel = skatolo.addTextlabel("kinectPaperLabel",
                                                        "Please save the calibration.",
                                                        200,
                                                        460);

            }

        }


        hideCorners();
        hideObjectSize();

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

    public void draw() {
        background(100);

        text("Camera : " + cameraName , 100, 70);
        text("Projector : " + screenName , 100, 230);
        text("Kinect : " + kinectName , 100, 420);
    }

    public Skatolo control() {
        return skatolo;
    }
}
