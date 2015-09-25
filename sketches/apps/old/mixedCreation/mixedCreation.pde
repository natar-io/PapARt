import fr.inria.papart.procam.*;
import fr.inria.papart.tools.*;
import fr.inria.papart.kinect.*;
import fr.inria.papart.multitouchKinect.*;
import fr.inria.papart.drawingapp.*;

// Loading javaCV and javaCPP
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
//import com.googlecode.javacv.processing.*;

import javax.media.opengl.GL;
import processing.opengl.*;

import codeanticode.glgraphics.*;
import codeanticode.gsvideo.*;


Camera cameraTracking, cameraCapture;
Projector projector;
float screenResolution = 3;

PVector viewSize = new PVector(600, 600);
// TrackedView boardView; -- legacy 

boolean useSecondCam = false;


// Undecorated frame 
public void init() {
    frame.removeNotify(); 
    frame.setUndecorated(true); 
    frame.addNotify(); 
    super.init();
}

PVector captureSize = new PVector(75, 75);


void setup(){

    // Used in button management for time
    DrawUtils.applet = this;

    size(frameSizeX, frameSizeY, GLConstants.GLGRAPHICS);
 
    String calibrationFile, calibrationARToolKitFile;

    ///////////////////////////////////////////////
    //////////////// Projector ////////////////////
    ///////////////////////////////////////////////

    calibrationFile = sketchPath + "/data/calibration/calibration-p1.yaml";
    projector = new Projector(this,  calibrationFile,
			      frameSizeX, frameSizeY, 200, 2000);
    



    ///////////////////////////////////////////////
    ///////////////// Load subsketches ////////////
    ///////////////////////////////////////////////
    // see subSketches.pde
    initSubSketches();



    //////////////////////////////////////////////
    /////////// Marker Board ////////////////////
    ////////////////////////////////////////////

    /////////// Drawing canvas //////////

    /// -> see boards.pde /// 
    initMarkerBoards();



    //////////////////////////////////////////////
    ////////////// Tracking camera ///////////////
    //////////////////////////////////////////////

    calibrationFile = sketchPath +  "/data/calibration/calibration-p1.yaml";
    calibrationARToolKitFile = sketchPath + "/data/calibration/p1-art.dat";
    Camera.convertARParams(this, calibrationFile, calibrationARToolKitFile, cameraX, cameraY);
    cameraTracking = new Camera(this, cameraNo, cameraX, cameraY, calibrationFile, cameraType);
    cameraTracking.initMarkerDetection(this, calibrationARToolKitFile, boards);
    // The camera view is handled in another thread;
    cameraTracking.setThread();
    // The boards will be detected at every frame.
    cameraTracking.setAutoUpdate(true);


    //////////////////////////////////////////////
    ///////////// Capture camera /////////////////
    //////////////////////////////////////////////

    if(useSecondCam){
	calibrationFile = sketchPath + "/data/calibration/calibration-1600.yaml";
	calibrationARToolKitFile = sketchPath + "/data/calibration/1600-art.dat";
	Camera.convertARParams(this, calibrationFile, calibrationARToolKitFile, camera2X, camera2Y);
	cameraCapture = new Camera(this, camera2No , camera2X, camera2Y, calibrationFile, camera2Type);
	cameraCapture.initMarkerDetection(this, calibrationARToolKitFile, boards);
	// The camera view is handled in another thread;
	cameraCapture.setThread(true);
	cameraCapture.setAutoUpdate(true);
    }

    //////// Set the tracked view camera /////// 
    //    boardView = new TrackedView(markerBoard, cameraCapture, capturePos, captureSize, camera2X, camera2Y);


    addLayer = new AddLayer(this, addLayerBoard, 
			    new PVector(300, 200),
			    cameraTracking, 
			    screenResolution, projector);
    editLayer = new EditLayer(this, editLayerBoard, secondaryBoardSize, cameraTracking, screenResolution, projector);
    filters = new Filters(this, filterBoard,
			  new PVector(300, 120), 
			  cameraTracking, screenResolution, projector);


    //    placementInterface = new PlacementInterface(this, placementBoard, secondaryBoardSize, cameraTracking, screenResolution, projector);

    paperInterfaces = new PaperInterface[3];
    paperInterfaces[0] = addLayer;
    paperInterfaces[1] = editLayer;
    paperInterfaces[2] = filters;
    //    paperInterfaces[3] = placementInterface;


    for(PaperInterface pi : paperInterfaces)
	pi.init();


    //////////////////////////////////////////////
    ////////////// Virtual Screen ////////////////
    //////////////////////////////////////////////

    ////// see boards.pde ////
    setBoardsFiltering(cameraTracking);
    initScreens();


    ///////////////////////////////////////////////
    ///////////// Kinect init /////////////////////
    ///////////////////////////////////////////////

    initKinect();
    kinectCalibFile = "data/calibration/KinectScreenCalibration.txt";
    touchInput = new TouchInput(this, kinectCalibFile, kinect, openKinectGrabber, false, 3, 5);
 
    ///////////////////////////////////////////////
    ///////////// Load Image filters //////////////
    ///////////////////////////////////////////////

    //    initFilters(); 


    // for(SubSketch s : subSketches){
    // 	s.setup(this);
    // }

    // movie = new GSMovie(this, sketchPath + "/videos/station.mov");
    // movie.loop();

    
    ////////////////////////////////////////////
    /////////// Init GUI ///////////////////////
    ////////////////////////////////////////////

    // initPlacement();
    // initEditLayerBoard();

    initDrawing();


    ////////// Set Button font //////////

    font = loadFont(sketchPath + "/data/Font/GentiumBookBasic-48.vlw");
    Button.setFont(font);
    Button.setFontSize(12);


}


PFont font;
PaperInterface[] paperInterfaces;
EditLayer editLayer;
AddLayer addLayer;
PlacementInterface placementInterface;
Filters filters;

// IplImage out = null;
// GLTexture tex = null;

void movieEvent(GSMovie movie) {
  movie.read();
}


boolean displayAugmentation = false;


void draw(){

    // see boards.pde //
    updateScreens();
    updateTouch();

    // update paper.
    for(PaperInterface pi : paperInterfaces)
	pi.update();
       
    // Draw Menus 
    for(PaperInterface pi : paperInterfaces)
	pi.draw();


    // Update subsketches
    for(SubSketch s : subSketches){
	s.startDraw();
	s.draw();
	s.endDraw();
    }


    // see drawing.pde
    drawDrawing();

    // // see editLayer.pde    
    // drawEditLayer();

    // see addLayer.pde
    // addLayer.draw();
    // drawAddLayer();

    // // see filters.pde
    // drawFilters();
    
    // // see Placement.pde
    // drawPlacement();

    projector.drawScreens();    

    // Still problems with the distosions
    image(projector.distort(true), 0, 0, frameSizeX, frameSizeY);

}


class MyMovie {
    
    String movieName;
    GSMovie movie = null;
    Layer layer = null;

    public MyMovie(String movieName){
	this.movieName = movieName;
    }

    private void init(PApplet parent){
	movie = new GSMovie(parent, movieName);
	movie.loop();

	// println("Loading movie..." + movie.width + " " + movie.height);
	// float imRatio = (float) movie.height / (float) movie.width ;
	// this.layer = new Layer(parent, movie, 
	// 		    new PVector(0, 0),
	// 		    new PVector(50, 50 * imRatio));

	float imRatio = 320f / 240f;
	this.layer = new Layer(parent, 320, 240, 
			    new PVector(0, 0),
			    new PVector(50 * imRatio, 50));


	editLayer.addLayer(this.layer);	
	println("Video " + movieName + " started ...");	    
    }

    public void update(PApplet parent){
	if(movie == null)
	    init(parent);

	// GLGraphicsOffScreen g = layer.getBuffer();

	// g.beginDraw();
	// g.background(255, 0, 0);
	// g.endDraw();
       
	layer.putImage(movie);
    }
}


boolean capture = false;
boolean project = true;
boolean changed = false;
boolean test = false;

int dx = 0;
int dy = 0;

void keyPressed(){
    if(key == 't')
	capture = !capture;

    if(key == 'x'){
	dx +=  1;
    }
    if(key == 'X'){
	dx -=  1;
    }

    if(key == 'y'){
	dy +=  1;
    }
    if(key == 'Y'){
	dy -=  1;
    }

    println("Dx et dy " + dx + " " + dy);

    if(key == 't')
	test = !test;


    println("Test " + test);

    // Placed here, bug if it is placed in setup().
    if(key == ' ')
	frame.setLocation(framePosX, framePosY);
}

