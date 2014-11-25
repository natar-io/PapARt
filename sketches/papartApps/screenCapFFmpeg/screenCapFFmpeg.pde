
// PapARt library
import fr.inria.papart.procam.*;
import fr.inria.papart.multitouchKinect.*;


Papart papart;

// Frame location. 
int framePosX = 0;
int framePosY = 200;

boolean useProjector;


// Undecorated frame 
public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify(); 
  super.init();
}


void setup(){

    useProjector = true;
    int frameSizeX = 1280;
    int frameSizeY = 800;

    if(!useProjector) {
	frameSizeX = 640 * 2;
	frameSizeY = 480 * 2;
    }


    size(frameSizeX, frameSizeY, OPENGL);
    papart = new Papart(this);

    if(useProjector){
	papart.initProjectorCamera(1, "0", Camera.OPENCV_VIDEO);
	papart.loadTouchInput(2, 5);
    } else {
	papart.initKinectCamera(2);
	papart.loadTouchInputKinectOnly(2, 5);
    }

    TouchInput touchInput = papart.getTouchInput();
    ARDisplay display = papart.getDisplay();
    Camera cameraTracking = papart.getCameraTracking();
    display.setDisplaySize(width, height);

    PVector boardSize = new PVector(297, 210);   //  21 * 29.7 cm
    float boardResolution = 5;  // 3 pixels / mm


    // --------- CaptureApp ----------------    
    // TODO: moon.cfg...
    String markerFile = sketchPath + "/data/markers/moon/moon.cfg";
    MarkerBoard markerBoard =  new MarkerBoard(markerFile, "CaptureApp Board",
    					       (int) boardSize.x, (int)boardSize.y);
    CaptureApp moon = new CaptureApp(this, markerBoard,
    			    boardSize, boardResolution,
    			    cameraTracking, display, touchInput);
    
    // Start the tracking.
    cameraTracking.trackSheets(true);
}


void draw(){
}


boolean test = false;
void keyPressed() {

  if(key == 't')
    test = !test;

  // Placed here, bug if it is placed in setup().
  if(key == ' ')
    frame.setLocation(framePosX, framePosY);
}


