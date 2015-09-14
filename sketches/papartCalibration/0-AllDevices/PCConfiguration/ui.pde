Textfield cameraIdText, posXText, posYText;
RadioButton screenChooser,  cameraType ;

Button startCameraButton, saveCameraAsButton, saveDefaultCameraButton;
Button initButton, saveScreenAsButton, saveDefaultScreenButton;

PFont myFont = createFont("arial",20);
ControlFont cFont;
CColor cColor;
CColor cColorToggle;

PImage testCameraImg;

void initUI(){
    skatolo = new Skatolo(this);

   // skatolo.setColorForeground(color(200,0));

   // skatolo.setColorLabel(color(0,0,0,0));
  //  skatolo.setColorValue(color(0,0,0,0));
  //  skatolo.setColorActive(color(161,4,4,0));

   // skatolo.setColorBackground(color(200,0));

    PFont pfont = loadFont("data/Serif.plain-13.vlw"); // use true/false for smooth/no-smooth
    cFont = new ControlFont(pfont,13);

    cColor = new CColor(color(134, 171, 242),
	       color(51),
	       color(71),
	       color(255),
	       color(255));


    cColorToggle = new CColor(color(134, 171, 242),
	       color(120),
	       color(219),
	       color(255),
	       color(255));


    initScreenUI();
    initCameraUI();

    updateStyles();

}

void initScreenUI(){
    screenChooser = skatolo.addRadioButton("screenChooserRadio")
	.setPosition(50,100)
	.setLabel("Screen Chooser")
	.toUpperCase(false)
	.setItemWidth(20)
	.setItemHeight(20)
	.setColorLabel(color(255))
	.activate(0)
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

    posXText = skatolo.addTextfield("PosX")
	.setPosition(417, 100)
	.setSize(80,20)
	.setFont(myFont)
	.setLabel("Position X")
	.setText(Integer.toString(screenConfig.getProjectionScreenOffsetX()))
	;

    posYText = skatolo.addTextfield("PosY")
	.setPosition(417, 150)
	.setSize(80,20)
	.setFont(myFont)
	.setLabel("Position Y")
	.setText(Integer.toString(screenConfig.getProjectionScreenOffsetY()))
	;

    initButton = skatolo.addButton("initSecondApplet")
	.setPosition(611, 102)
	.setLabel("Test Projection")
	.setSize(110, 20)
	;

    saveDefaultScreenButton = skatolo.addButton("saveDefaultScreen")
	.setPosition(611, 143)
	.setLabel("Save as default")
	.setSize(110, 20)
	;

    saveScreenAsButton = skatolo.addButton("saveScreenAs")
	.setPosition(611, 183)
	.setLabel("Save screen as...")
	.setSize(110, 20)
	;


}

void initCameraUI(){


    cameraType = skatolo.addRadioButton("cameraTypeChooser")
	.setPosition(50, 365)
	.setItemWidth(20)
	.setItemHeight(20)
	.addItem("OpenCV", 0)
	.addItem("Processing", 1)
	.addItem("OpenKinect", 2)
	.addItem("FlyCapture", 3)
	.setColorLabel(color(255))
	.activate(cameraConfig.getCameraType().ordinal())
	;


    cameraIdText = skatolo.addTextfield("CameraId")
	.setPosition(250 ,370)
	.setSize(200,20)
	.setFont(myFont)
	.setLabel("")
	.setLabelVisible(false)
	.setText(cameraConfig.getCameraName())
	.setFocus(true)
	;



    testCameraImg = loadImage("data/testCamera.png");

    startCameraButton = skatolo.addButton("testCameraButton")
	.setPosition(611, 372)
	.setLabel("Test the camera")
	.setSize(110, 20)
	;

    saveDefaultCameraButton = skatolo.addButton("saveDefaultCamera")
	.setPosition(611, 413)
	.setLabel("Save as default")
	.setSize(110, 20)
	;

    saveCameraAsButton = skatolo.addButton("saveCameraAs")
	.setPosition(611, 453)
	.setLabel("Save camera as.")
	.setSize(110, 20)
	;

}



void updateStyles(){

    setStyle(screenChooser);
    setStyle(posXText);
    setStyle(posYText);
    setStyle(initButton);
    setStyle(saveScreenAsButton);
    setStyle(saveDefaultScreenButton);

    setStyle(cameraType);
    setStyle(cameraIdText);
    setStyle(startCameraButton);
    setStyle(saveDefaultCameraButton);
    setStyle(saveCameraAsButton);
}

void setStyle(Controller controller){
    controller.getCaptionLabel().updateFont(cFont).toUpperCase(false);
    controller.setColor(cColor);
}


void setStyle(RadioButton controller){
    for(Toggle toggle : controller.getItems()){
	toggle.getCaptionLabel().updateFont(cFont).toUpperCase(false);
	toggle.setColor(cColorToggle);
    }
}
