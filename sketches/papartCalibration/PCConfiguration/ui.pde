
Textfield cameraIdText, posXText, posYText;
RadioButton screenChooser;

Button startCameraButton;

PFont myFont = createFont("arial",20);

PImage testCameraImg;

void initUI(){
    cp5 = new ControlP5(this);

    initScreenUI();
    initCameraUI();

    cp5.addButton("save")
	.setPosition(320, 520)
	.setSize(40, 20)
	;
}

void initScreenUI(){
    screenChooser = cp5.addRadioButton("screenChooserRadio")
	.setPosition(57,100)
	.setLabel("Screen Chooser")
	.setItemWidth(20)
	.setItemHeight(20)
	.setColorLabel(color(255))
	.activate(0)
	;


    posXText = cp5.addTextfield("PosX")
	.setPosition(440, 100)
	.setSize(80,20)
	.setFont(myFont)
	.setLabel("Position X")
	.setText(Integer.toString(cc.getProjectionScreenOffsetX()))
	;


    posYText = cp5.addTextfield("PosY")
	.setPosition(440, 150)
	.setSize(80,20)
	.setFont(myFont)
	.setLabel("Position Y")
	.setText(Integer.toString(cc.getProjectionScreenOffsetY()))
	;


    cp5.addButton("initSecondApplet")
	.setPosition(610, 150)
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

void initCameraUI(){


    cp5.addRadioButton("cameraTypeChooser")
	.setPosition(100, 300)
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
	.setPosition(325 ,350)
	.setSize(200,20)
	.setFont(myFont)
	.setLabel("")
	.setLabelVisible(false)
	.setText(cc.getCameraName())
	.setFocus(true)
	;

    testCameraImg = loadImage("data/testCamera.png");

    startCameraButton = cp5.addButton("testCameraButton")
	.setPosition(608, 350)
	.setSize(170,43)
	;


}
