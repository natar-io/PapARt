import java.util.Vector;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack; 
import org.bytedeco.javacpp.ARToolKitPlus;
import org.bytedeco.javacpp.ARToolKitPlus.TrackerMultiMarker;
/*import de.fhpotsdam.unfolding.mapdisplay.*;
import de.fhpotsdam.unfolding.utils.*;
import de.fhpotsdam.unfolding.marker.*;
import de.fhpotsdam.unfolding.tiles.*;
import de.fhpotsdam.unfolding.interactions.*;
import de.fhpotsdam.unfolding.ui.*;
import de.fhpotsdam.unfolding.*;
import de.fhpotsdam.unfolding.core.*;
import de.fhpotsdam.unfolding.mapdisplay.shaders.*;
import de.fhpotsdam.unfolding.data.*;
import de.fhpotsdam.unfolding.geo.*;
import de.fhpotsdam.unfolding.texture.*;
import de.fhpotsdam.unfolding.events.*;*/
import de.fhpotsdam.utils.*;
import de.fhpotsdam.unfolding.providers.*;

public class DrawingMap extends PaperTouchScreen {

    //For the position
    PVector magicalLocation = new PVector(62, 46, 0);

    //Precision thresholds
    int timeStep = 500000;
    int spaceStep = 300;


    //Palette
    color cText = color(208, 0, 73);
    color cSelection = color(62, 22, 29);
    color cPointer = color(0, 228, 255);
    color cPointerEffect = color(12, 103, 215);
    color cNo = color(243, 2, 98);
    color cYes = color(141, 215, 12);

    //For the touch
    PVector currentTouch = new PVector(-1, -1);

    //Added objects
    Vector<PVector> positions = new Vector<PVector>();// TODO check if those are still useful
    Vector<Integer> timers = new Vector<Integer>();

    //Position of the sheet of paper
    PVector lastPos = new PVector(0,0,0);
    PVector refPos = new PVector(0,0,0);
    boolean refInit = false;
    PVector curCenter = new PVector(0,0,0);

    //Position of the untreated touches
    Vector<PVector> touchBuffer = new Vector<PVector>();

    //For the double touch interaction
    PVector lastPosA = new PVector(0,0,0);//Last position of the finger "A"
    PVector lastPosB = new PVector(0,0,0);//Last position of the finger "B"
    boolean doubleTouch = false;//If double touch is activated

    //Mode
    String mode = "displayFilter";

    //Buttons
    //Button captButton;

    //Menu
    //int menuWidth = 20;

    //Maps
    MapFactory umaps;
 
    Screen curScreen;

    //Informations for capture
    TrackedView boardView;
    // 5cm  ->  50 x 50 pixels 
    PVector captureSize = new PVector(A4BoardSize.x, A4BoardSize.y);
    PVector origin = magicalLocation;
    //int picSizeX = (int) A4BoardSize.x - menuWidth; // Works better with power  of 2 (initially 64)
    //int picSizeY = (int) A4BoardSize.y; // Works better with power  of 2 (initially 64)
    int picSizeX = 312;
    int picSizeY = 224;
    //For capture
    boolean captured = false;
    //int timeCounter = 0;
    int startingTime = -1;
    PImage lastCapture = null;
    PImage modifiedCapture = null;


    //For color averaging
    PImage frameOld;
    PImage frameAvg;
    int frameCount = 0;

    //For locations
    int placeSize = 13;
    Vector<Place> places = new Vector<Place>();
    int currentPlace;
    boolean curPlaceChanged = false;
    int currentPlaceSelection = 0;
    int currentPlaceSelectionStartingTime = -1;

    
    public static final int resolution = 3;
    public static final int zoomLevelOut = 13;
    public static final int zoomLevelIn = 16;
    public static final float maxPanningDistance = 5; // in km


/**************************************************************************************************************/
    void setup(){
	setResolution(resolution);
	setDrawingSize((int) A4BoardSize.x, (int) A4BoardSize.y);
	//loadMarkerBoard(sketchPath + "/data/markers/drawing.cfg",
	//		 (int) A4BoardSize.x, (int) A4BoardSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/frame4.png",
			420, 297);



	initPlaces();

	umaps = new MapFactory(resolution, parent);

	Location bordeauxLoc = places.elementAt(0).getLocation();
	currentPlace = 0;
	umaps.zoomAndPanTo(bordeauxLoc, zoomLevelOut);
	umaps.setPanningRestriction(bordeauxLoc, maxPanningDistance);

	//Add view for the tracking
	boardView = new TrackedView(markerBoard, origin, captureSize, picSizeX, picSizeY);

	// Register this view with the camera.
	cameraTracking.addTrackedView(boardView);

    }



/**************************************************************************************************************/
    private void initPlaces(){
	Location bordeauxLoc = new Location(44.840362f, -0.581678f);// Hotel de ville
	Place bordeauxPlace = new Place(bordeauxLoc, "Bordeaux", loadImage(sketchPath + "/data/images/bordeaux.jpg"));
	places.add(bordeauxPlace);
	Location capSciencesLoc = new Location(44.859749, -0.554376);
	Place capSciencesPlace = new Place(capSciencesLoc, "Cap Sciences", loadImage(sketchPath + "/data/images/capsciences.jpg"));
	places.add(capSciencesPlace);
	Location peyBerlandLoc = new Location(44.838000, -0.577691);
	Place peyBerlandPlace = new Place(peyBerlandLoc, "Place Pey Berland", loadImage(sketchPath + "/data/images/peyberland.jpg"));
	places.add(peyBerlandPlace);
	Location gambettaLoc = new Location(44.841430, -0.580489);
	Place gambettaPlace = new Place(gambettaLoc, "Place Gambetta", loadImage(sketchPath + "/data/images/gambetta.jpg"));
	places.add(gambettaPlace);
	Location saintJeanLoc = new Location(44.825726, -0.556276);
	Place saintJeanPlace = new Place(saintJeanLoc, "Gare Saint Jean", loadImage(sketchPath + "/data/images/saintjean.jpg"));
	places.add(saintJeanPlace);
	Location pontDePierreLoc = new Location(44.838397, -0.562859);
	Place pontDePierrePlace = new Place(pontDePierreLoc, "Pont de Pierre", loadImage(sketchPath + "/data/images/pontdepierre.jpg"));
	places.add(pontDePierrePlace);
	Location placeBourseLoc = new Location(44.841540, -0.569570);
	Place placeBoursePlace = new Place(placeBourseLoc, "Place de la Bourse", loadImage(sketchPath + "/data/images/placedelabourse.jpg"));
	places.add(placeBoursePlace);
	Location jardinPublicLoc = new Location(44.849063, -0.578172);
	Place jardinPublicPlace = new Place(jardinPublicLoc, "Jardin Public", loadImage(sketchPath + "/data/images/jardinpublic.jpg"));
	places.add(jardinPublicPlace);
    }

/**************************************************************************************************************/
    public void draw(){
	setLocation(magicalLocation.x, magicalLocation.y, magicalLocation.z);
	findCurrentTouch();
	umaps.draw();

	beginDraw2D();
	if(!refInit){// TODO put this in a method
	    //Initialize screen
	    curScreen = getScreen();
	    //Initialize maps
	    
	    //umap1.draw();
	    //Initialize reference position
	    PVector[] cornersPos = curScreen.getCornerPos();
	    refPos = cornersPos[0];
	    refPos.add(cornersPos[2]);
	    refPos.div(2);
	    refInit = true;
	}

	background(0);

	PMatrix3D transform = new PMatrix3D();
	
	//Get the current position of the sheet of paper
	PVector[] cornersPos = curScreen.getCornerPos();
	curCenter = cornersPos[0];
	curCenter.add(cornersPos[2]);
	curCenter.div(2);
	
	if(mode == "displayFilter"){
	    //dispFilters();
	    fill(cText);
	    //textSize(18);
	    textFont(mainFont);
	    textSize(18);
	    textAlign(LEFT);
	    text(places.elementAt(currentPlace).getName(), 10, 15);
	    //drawMenu();
	    //drawCurrentTouch(cPointer);
	    //singleTouch();
	}

	drawObjectDetection();

	touchBuffer.removeAllElements(); // TODO see if this touch buffer thingy is usefu
	noStroke();
	stroke(0, 0, 0);
	endDraw();
    }


/**************************************************************************************************************/
    void drawObjectDetection(){
	PImage capt = capture();
	/*if(frameCount % 10 == 0){
	    
	}
	else{

	}*/
	//System.out.println("Found " + colors.size() + " colors");
	for(Touch t : touchList.get2DTouchs()){
	    PVector p = t.position;
	    p.add(new PVector(8, 12, 0));// Depends on callibration: TODO find a way to avoid those random numbers
	    Color avg = new Color();
	    int minCol = -1;
	    float minDist = Float.MAX_VALUE;
	    PImage piece = avg.getAverageColor(capt, (int) (p.x - patchSize / 2) + 7, (int) (p.x + patchSize / 2) - 7, (int) (p.y - patchSize / 2) + 7, (int) (p.y + patchSize / 2) - 7);
	    for(int c = 0; c < colors.size(); c++){
		//System.out.println(colors.elementAt(c).dist(avg));
		//colors.elementAt(c).print();
		//avg.print();
		float curDist = colors.elementAt(c).dist(avg);
		System.out.println(curDist);
		if(curDist < minDist){
		    minDist = curDist;
		    minCol = c;
		}
		//fill(avg.getColor());
		//ellipse(p.x, p.y, 30, 30);
	    }
	    
	    //System.out.println("Found color");
	    if((minCol >= 0) && (minDist < 60)){
		//if((minCol >= 0)){
		fill(avg.getColor());
		ellipse(60, 60, 30, 30);
		ellipse((p.x - patchSize / 2) + 7, (int) (p.y - patchSize / 2) + 7, 5, 5);
		ellipse((p.x + patchSize / 2) - 7, (int) (p.y + patchSize / 2) - 7, 5, 5);
		fill(colors.elementAt(minCol).getColor());
		ellipse(0, 0, 30, 30);
		image(piece, 90, 90);
		System.out.println("Color detected: " + minCol);
	    }
	}
    }

/**************************************************************************************************************/
    void findCurrentTouch(){
	PVector minYVector = new PVector(A4BoardSize.x,A4BoardSize.y);//TODO implement a method "findTouchMinY" for this method and singleTouch method
	float minY = Float.MAX_VALUE;
	boolean empty = true;
	for (Touch t : touchList.get2DTouchs()) {
	    empty = false;
	    PVector p = t.position;
	    touchBuffer.add(p);//TEST ONLY, TO REMOVE
	    if(p.y < minY){
		minYVector = p;
		minY = p.y;
	    }
	}
	if(!empty){
	    currentTouch = minYVector;
	}
    }

/**************************************************************************************************************/
    void drawCurrentTouch(color c){
	if(!touchList.get2DTouchs().isEmpty()){
	    fill(c);
	    stroke(c);
	    ellipse(currentTouch.x, currentTouch.y, 12, 12);
	}
    }

/**************************************************************************************************************/
    void drawTouch(Vector<PVector> v, color c){
	for(int item = 0; item < v.size(); item++){
	    fill(c);
	    stroke(c);
	    ellipse(v.elementAt(item).x, v.elementAt(item).y, 15, 15);
	}
    }


/**************************************************************************************************************/
    void dispMainFilter(){
	//Display default map
	tint(255, 255);
	image(umaps.getMapOuter1(), 0, 0, drawingSize.x, drawingSize.y);
    }

/**************************************************************************************************************/
    void dispFilters(){
	//Get the displacement for the reference position
	PVector displacement = curCenter.get();
	displacement.sub(refPos);

	//Display default map
	tint(255, 255);
	//currentMap.draw();
	//image(map1, 0, 0, drawingSize.x, drawingSize.y);
	image(umaps.getMapOuter1(), 0, 0, drawingSize.x, drawingSize.y);

	//Normalize the displacement
	float dispNorm = displacement.y;
	float threshMax = 20;
	float threshMin = -30;
	//float threshMax = 50;
	//float threshMin = -50;
	if(dispNorm > threshMax){
	    dispNorm = threshMax;
	}
	if(dispNorm < threshMin){
	    dispNorm = threshMin;
	}

	//Compute the opacity corresponding to the displacement
	float b = 255 * threshMax / (threshMax - threshMin);
	float a = - 255 / (threshMax - threshMin);
	float opacity = a * dispNorm + b;

	//Display filters
	if(displacement.x < threshMin){
	    tint(255, (int) opacity);
	    //currentMap = umap2;
	    //image(map2, 0, 0, drawingSize.x, drawingSize.y);
	    image(umaps.getMapOuter2(), 0, 0, drawingSize.x, drawingSize.y);
	}
	else if (displacement.x > threshMax){
	    tint(255, (int) opacity);
	    //currentMap = umap3;
	    //image(map3, 0, 0, drawingSize.x, drawingSize.y);
	    image(umaps.getMapOuter3(), 0, 0, drawingSize.x, drawingSize.y);
	}

	tint(255, 255);
    }


/**************************************************************************************************************/
    PImage capture(){
	PImage out = cameraTracking.getPView(boardView);
	return out;
    }


/**************************************************************************************************************/
    color randomColor(){
	int red = (int) random(256);
	int green = (int) random(256);
	int blue = (int) random(256);
	return color(red, green, blue);
    }

/**************************************************************************************************************/
    float colorDistance(color color1, color color2){
	float dist = sq(red(color1) - red(color2));
	dist += sq(green(color1) - green(color2));
	dist += sq(blue(color1) - blue(color2));
	dist = sqrt(dist);
	return dist;
    }

}


