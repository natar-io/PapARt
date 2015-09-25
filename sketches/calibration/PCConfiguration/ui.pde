Textfield cameraIdText, posXText, posYText;
Textfield kinectIdText;
Textfield cameraName;
Textfield kinectName;

RadioButton screenChooser, cameraType, kinectType;

Button startCameraButton, saveCameraAsButton, saveDefaultCameraButton;
Button startKinectButton, saveKinectAsButton, saveDefaultKinectButton;
Button initButton, saveScreenAsButton, saveDefaultScreenButton;
Toggle useCalibration;
Button loadCalibrationCamera, loadCalibrationProjector;

Button switchButton;

PFont myFont;
ControlFont cFont;
CColor cColor;
CColor cColorToggle;

PImage testCameraImg;

boolean useCameraCalibration;

void initUI() {

  myFont = createFont("arial", 20);
  skatolo = new Skatolo(this);

  // skatolo.setColorForeground(color(200,0));

  // skatolo.setColorLabel(color(0,0,0,0));
  //  skatolo.setColorValue(color(0,0,0,0));
  //  skatolo.setColorActive(color(161,4,4,0));

  // skatolo.setColorBackground(color(200,0));

  PFont pfont = loadFont("data/Serif.plain-13.vlw"); // use true/false for smooth/no-smooth
  cFont = new ControlFont(pfont, 13);

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
  initKinectUI();

  switchButton = skatolo.addButton("switchToCalibration")
      .setLabel("Switch to Calibration")
      .setSize(200, 30)
      .setPosition(400, 10)
      ;

  updateStyles();
}

void initScreenUI() {
  screenChooser = skatolo.addRadioButton("screenChooserRadio")
    .setPosition(50, 100)
    .setLabel("Screen Chooser")
    .toUpperCase(false)
    .setItemWidth(20)
    .setItemHeight(20)
    .setColorLabel(color(255))
    .activate(0)
    ;

  String[] descriptions = CanvasFrame.getScreenDescriptions();
  int k = 0;
  for (String description : descriptions) {
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
    .setSize(80, 20)
    .setFont(myFont)
    .setLabel("Position X")
    .setText(Integer.toString(screenConfig.getProjectionScreenOffsetX()))
    ;

  posYText = skatolo.addTextfield("PosY")
    .setPosition(417, 150)
    .setSize(80, 20)
    .setFont(myFont)
    .setLabel("Position Y")
    .setText(Integer.toString(screenConfig.getProjectionScreenOffsetY()))
    ;

  loadCalibrationProjector = skatolo.addButton("loadProjectorCalibration")
      .setLabel("Load Calibration")
      .setPosition(200, 197)
      .setSize(140,20)
      ;


  initButton = skatolo.addButton("testProjection")
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

void initCameraUI() {

    cameraType = skatolo.addRadioButton("cameraTypeChooser")
        .setPosition(50, 357)
        .setItemWidth(20)
        .setItemHeight(20)
        .addItem("OpenCV", Camera.Type.OPENCV.ordinal())
        .addItem("OpenCV Depth", Camera.Type.OPENCV_DEPTH.ordinal())
        .addItem("Processing", Camera.Type.PROCESSING.ordinal())
        .addItem("OpenKinect",Camera.Type.OPEN_KINECT.ordinal())
        .addItem("FlyCapture", Camera.Type.FLY_CAPTURE.ordinal())
        .addItem("Kinect2RGB", Camera.Type.KINECT2_RGB.ordinal())
        .addItem("Kinect2IR",Camera.Type.KINECT2_IR.ordinal())
        .activate(cameraConfig.getCameraType().ordinal())
        ;

    useCalibration = skatolo.addToggle("useCameraCalibration")
        .setLabel("use calibration")
        .setPosition(250, 457)
        // .setSize(130,50)
        .setState(true)
        ;

    loadCalibrationCamera = skatolo.addButton("loadCalibration")
        .setLabel("load calibration")
        .setPosition(250, 397)
        .setSize(140,20)
        ;


    cameraIdText = skatolo.addTextfield("CameraId")
    .setPosition(250, 358)
    .setSize(200, 20)
    .setFont(myFont)
    .setLabel("")
    .setLabelVisible(false)
    .setText(cameraConfig.getCameraName())
    .setFocus(true)
    ;

  testCameraImg = loadImage("data/testCamera.png");

  startCameraButton = skatolo.addButton("testCameraButton")
    .setPosition(611, 367)
    .setLabel("Test the camera")
    .setSize(110, 20)
    ;

  saveDefaultCameraButton = skatolo.addButton("saveDefaultCamera")
    .setPosition(611, 409)
    .setLabel("Save as default")
    .setSize(110, 20)
    ;

  saveCameraAsButton = skatolo.addButton("saveCameraAs")
    .setPosition(611, 450)
    .setLabel("Save camera as.")
    .setSize(110, 20)
    ;
}



void initKinectUI() {

    int currentType = 0;
    if(kinectConfig.getCameraType() == Camera.Type.OPEN_KINECT)
        currentType = 0;
    if(kinectConfig.getCameraType() == Camera.Type.KINECT2_RGB)
        currentType = 1;

    if(kinectConfig.getCameraType() == Camera.Type.FAKE)
        currentType = 2;

  kinectType = skatolo.addRadioButton("kinectTypeChooser")
    .setPosition(50, 652)
    .setItemWidth(20)
    .setItemHeight(20)
      .addItem("Kinect 360", Camera.Type.OPEN_KINECT.ordinal())
      .addItem("Kinect One", Camera.Type.KINECT2_RGB.ordinal())
      .addItem("No Kinect", Camera.Type.FAKE.ordinal())
    .setColorLabel(color(255))
      .activate(currentType)
    ;


  kinectIdText = skatolo.addTextfield("KinectId")
    .setPosition(250, 652)
    .setSize(200, 20)
    .setFont(myFont)
    .setLabel("")
    .setLabelVisible(false)
    .setText(kinectConfig.getCameraName())
    .setFocus(true)
    ;

  startKinectButton = skatolo.addButton("testKinectButton")
    .setPosition(611, 656)
    .setLabel("Test the kinect")
    .setSize(110, 20)
    ;

  saveDefaultKinectButton = skatolo.addButton("saveDefaultKinect")
    .setPosition(611, 696)
    .setLabel("Save as default")
    .setSize(110, 20)
    ;

  saveKinectAsButton = skatolo.addButton("saveKinectAs")
    .setPosition(611, 735)
    .setLabel("Save kinect as.")
    .setSize(110, 20)
    ;
}


void updateStyles() {

  setStyle(screenChooser);
  setStyle(posXText);
  setStyle(posYText);
  setStyle(initButton);
  setStyle(saveScreenAsButton);
  setStyle(saveDefaultScreenButton);
  setStyle(loadCalibrationProjector);

    setStyle(cameraType);
  setStyle(cameraIdText);
  setStyle(startCameraButton);
  setStyle(saveDefaultCameraButton);
  setStyle(saveCameraAsButton);
  setStyle(loadCalibrationCamera);

  setStyle(kinectType);
  setStyle(kinectIdText);
  setStyle(startKinectButton);
  setStyle(saveDefaultKinectButton);
  setStyle(saveKinectAsButton);

  setStyle(switchButton);
}

void setStyle(Controller controller) {
  controller.getCaptionLabel().updateFont(cFont).toUpperCase(false);
  controller.setColor(cColor);
}


void setStyle(RadioButton controller) {
  for (Toggle toggle : controller.getItems()) {
    toggle.getCaptionLabel().updateFont(cFont).toUpperCase(false);
    toggle.setColor(cColorToggle);
  }
}
