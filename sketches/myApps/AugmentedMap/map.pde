import java.util.Vector;
import java.util.Iterator;
import java.util.Stack; 
import org.bytedeco.javacpp.ARToolKitPlus;
import org.bytedeco.javacpp.ARToolKitPlus.TrackerMultiMarker;
import de.fhpotsdam.unfolding.mapdisplay.*;
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
import de.fhpotsdam.unfolding.events.*;
import de.fhpotsdam.utils.*;
import de.fhpotsdam.unfolding.providers.*;

public class DrawingMap extends PaperTouchScreen {

    //Images for the different filters
    PImage map1;
    PImage map2;
    PImage map3;

    //Images for the different objects
    PImage wine;

    //Precision thresholds
    int timeStep = 500000;
    int spaceStep = 300;

    //Added objects
    Vector<PVector> positions = new Vector<PVector>();
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
    Button captButton;

    //Menu
    int menuWidth = 20;

    //Maps
    UnfoldingMap currentMap;
    UnfoldingMap umap1;
    UnfoldingMap umap2;
    UnfoldingMap umap3;
    PGraphics mapOuter1;
    PGraphics mapOuter2;
    PGraphics mapOuter3;
    //MapTiles map = new MapTiles();
    Screen curScreen;

    //Informations for capture
    TrackedView boardView;
    // 5cm  ->  50 x 50 pixels 
    PVector captureSize = new PVector(A4BoardSize.x - menuWidth, A4BoardSize.y);
    PVector origin = new PVector(0, 0);
    //int picSizeX = (int) A4BoardSize.x - menuWidth; // Works better with power  of 2 (initially 64)
    //int picSizeY = (int) A4BoardSize.y; // Works better with power  of 2 (initially 64)
    int picSizeX = 288;
    int picSizeY = 224;
    //For capture
    boolean captured = false;
    int timeCounter = 0;
    PImage lastCapture = null;

    //For locations
    Vector<Place> places = new Vector<Place>();
    int currentPlace;
    boolean curPlaceChanged = false;
    int currentPlaceSelection = 0;
    int currentPlaceSelectionCounter = 15;

    //For the touch
    PVector currentTouch = new PVector(-1, -1);


/**************************************************************************************************************/
    void setup(){
	setDrawingSize((int) A4BoardSize.x, (int) A4BoardSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/drawing.cfg",
		    (int) A4BoardSize.x, (int) A4BoardSize.y);

	//Initialize images
	//map1 = loadImage(sketchPath + "/data/images/bordeauxmap.png");
	//map2 = loadImage(sketchPath + "/data/images/bordeauxsat.png");
	//map3 = loadImage(sketchPath + "/data/images/bordeauxtram.png");
	//wine = loadImage(sketchPath + "/data/images/wine.jpg");
	//background(0);
	//image(map, 0, 0, drawingSize.x, drawingSize.y);

	//Initialize maps
	//umap1 = new UnfoldingMap(parent);
	umap1 = new UnfoldingMap(parent, 0, 0, A4BoardSize.x, A4BoardSize.y, new Google.GoogleMapProvider());
	//umap1 = new UnfoldingMap(parent, 0, 0, A4BoardSize.x, A4BoardSize.y, new Microsoft.RoadProvider());
	//umap1 = new UnfoldingMap(parent, 0, 0, A4BoardSize.x, A4BoardSize.y, new OpenStreetMap.OpenStreetMapProvider());
	umap2 = new UnfoldingMap(parent, 0, 0, A4BoardSize.x, A4BoardSize.y, new Microsoft.AerialProvider());
	umap3 = new UnfoldingMap(parent, 0, 0, A4BoardSize.x, A4BoardSize.y, new StamenMapProvider.WaterColor());
	//map4 = new UnfoldingMap(this, new Yahoo.HybridProvider());

	Location bordeauxLoc = new Location(44.840362f, -0.581678f);// Hotel de ville
	Place bordeauxPlace = new Place(bordeauxLoc, "Bordeaux", loadImage(sketchPath + "/data/images/bordeaux.jpg"));
	places.add(bordeauxPlace);
	currentPlace = 0;
	umap1.zoomAndPanTo(bordeauxLoc, 12);
	umap2.zoomAndPanTo(bordeauxLoc, 12);
	umap3.zoomAndPanTo(bordeauxLoc, 12);
	//map4.zoomAndPanTo(bordeauxLocation, 12);
	float maxPanningDistance = 5; // in km
	umap1.setPanningRestriction(bordeauxLoc, maxPanningDistance);
	umap2.setPanningRestriction(bordeauxLoc, maxPanningDistance);
	umap3.setPanningRestriction(bordeauxLoc, maxPanningDistance);
	//map4.setPanningRestriction(bordeauxLocation, maxPanningDistance);

	//Initialize places
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
    
	//currentMap = umap1;
	//MapUtils.createDefaultEventDispatcher(parent, umap1);
	MapUtils.createDefaultEventDispatcher(parent, umap1, umap2, umap3);

	//umap1 = new UnfoldingMap(parent);

	//Add view for the tracking
	boardView = new TrackedView(markerBoard, 
				    origin,
				    captureSize,
				    picSizeX, picSizeY);

	// Register this view with the camera.
	cameraTracking.addTrackedView(boardView);

	//Initialize buttons
	captButton = new Button("", 
				(int) A4BoardSize.x - 10, 15, 
				16, 16);
	/*PImage captImage = loadImage(sketchPath + "/data/images/capt.png");
	  captButton = new Button(wine, (int) A4BoardSize.x - 8, 8);*/

	captButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    mode = "Capture";
		}
		public void ButtonReleased(){
		    mode = "displayFilter";
		}
	    });
	captButton.setButtonFontSize(5);

	buttons.add(captButton);
    }

/**************************************************************************************************************/
    public void draw(){
	findCurrentTouch();
	umap1.draw();
	OpenGLMapDisplay curMapDisplay = (OpenGLMapDisplay) umap1.mapDisplay;
	mapOuter1 = curMapDisplay.getOuterPG();
	umap2.draw();
	curMapDisplay = (OpenGLMapDisplay) umap2.mapDisplay;
	mapOuter2 = curMapDisplay.getOuterPG();
	umap3.draw();
	curMapDisplay = (OpenGLMapDisplay) umap3.mapDisplay;
	mapOuter3 = curMapDisplay.getOuterPG();

	beginDraw2D();
	if(!refInit){
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


	//umap1.resize(A4BoardSize.x, A4BoardSize.y);
	//OpenGLMapDisplay mapDisplay1 = (OpenGLMapDisplay) umap1.mapDisplay;
	//System.out.println(mapDisplay1.getHeight());
	//System.out.println(mapDisplay1.getWidth());
	//PGraphics inner =  mapDisplay1.getInnerPG();
	//PGraphics outer = mapDisplay1.getOuterPG();
	
	//umap1.draw();
	//image(outer, 0, 0, A4BoardSize.x, A4BoardSize.y);

	PMatrix3D transform = new PMatrix3D();
	
	//Get the current position of the sheet of paper
	//Screen curScreen = getScreen();
	PVector[] cornersPos = curScreen.getCornerPos();
	curCenter = cornersPos[0];
	curCenter.add(cornersPos[2]);
	curCenter.div(2);
	
	if(mode == "displayFilter"){
	    dispFilters();
	    drawCurrentTouch(color(255, 0, 105));
	    if(currentPlace == 0){
		if(curPlaceChanged){
		    Location curLocation = places.elementAt(0).getLocation();
		    umap1.zoomAndPanTo(curLocation, 12);
		    umap2.zoomAndPanTo(curLocation, 12);
		    umap3.zoomAndPanTo(curLocation, 12);
		    //map4.zoomAndPanTo(bordeauxLocation, 12);
		    float maxPanningDistance = 5; // in km
		    umap1.setPanningRestriction(curLocation, maxPanningDistance);
		    umap2.setPanningRestriction(curLocation, maxPanningDistance);
		    umap3.setPanningRestriction(curLocation, maxPanningDistance);
		    curPlaceChanged = false;
		}
		dispPlaces();
	    }
	    if((currentPlace != 0) && (curPlaceChanged)){
		Location curLocation = places.elementAt(currentPlace).getLocation();
		umap1.zoomAndPanTo(curLocation, 15);
		umap2.zoomAndPanTo(curLocation, 15);
		umap3.zoomAndPanTo(curLocation, 15);
		//map4.zoomAndPanTo(bordeauxLocation, 12);
		float maxPanningDistance = 5; // in km
		umap1.setPanningRestriction(curLocation, maxPanningDistance);
		umap2.setPanningRestriction(curLocation, maxPanningDistance);
		umap3.setPanningRestriction(curLocation, maxPanningDistance);
		curPlaceChanged = false;
	    }
	    selectPlace();
	    //singleTouch();
	}
	if(mode == "Capture"){
	    timeCounter++;
	    if((!captured) && (timeCounter == 10)){
		lastCapture = capture();
		//System.out.println("Capture");
		//System.out.println(lastCapture.width);
		//System.out.println(lastCapture.height);
		captured = true;
	    }
	    if(timeCounter == 20){
		mode = "displayFilter";
		captured = false;
		timeCounter = 0;
		//captButton.setNotTouched();
		captButton.reset();
		//System.out.println("Back to filtering");
		lastCapture = detectPostIts(lastCapture);// TODO remove this
		//detectLines(lastCapture); // TO REMOVE
		lastCapture = getConnectedComponents(lastCapture);
	    }
	}


	//Display an indicator for the reference position
	/*color refColor = color(2*refPos.x, 2*refPos.y, refPos.z);
	fill(refColor);
	rect(0,0,15,15);*/
	

	/*drawTouch(touchBuffer, color(255, 255, 0));
	Vector<PVector> centroids = clusterTouch();*/
	touchBuffer.removeAllElements();
	//System.out.println(centroids.size());
	//drawTouch(centroids, color(0, 255, 0)):
	noStroke();
	//rect(10,10,10,10);
	//rect(20,20,5,5);
	//rect(30,30,3,3);
	stroke(0, 0, 0);
	drawMenu();
	endDraw();
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
    void drawMenu(){
	tint(255, 255);
	fill(255, 255, 255);
	noStroke();
	rectMode(CORNER);
	rect(A4BoardSize.x - menuWidth, 0, menuWidth, A4BoardSize.y);
	PImage capt = loadImage(sketchPath + "/data/images/capt.png");
	image(capt, A4BoardSize.x - 18, 7, 16, 16);
	drawButtons();

	if(currentPlace != 0){
	    //Draw a button to come back at the main view
	    Place mainPlace = places.elementAt(0);
	    image(mainPlace.getImage(), A4BoardSize.x - 18, 28, 16, 16);
	}
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
	image(mapOuter1, 0, 0, drawingSize.x, drawingSize.y);

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
	    image(mapOuter2, 0, 0, drawingSize.x, drawingSize.y);
	}
	else if (displacement.x > threshMax){
	    tint(255, (int) opacity);
	    //currentMap = umap3;
	    //image(map3, 0, 0, drawingSize.x, drawingSize.y);
	    image(mapOuter3, 0, 0, drawingSize.x, drawingSize.y);
	}

	if(lastCapture != null){
	    //image(lastCapture, 0, 0, A4BoardSize.x/10, A4BoardSize.y/10);
	    image(lastCapture, 0, 0, A4BoardSize.x - menuWidth, A4BoardSize.y);
	}

	fill(155, 45, 107);
	textSize(18);
	textAlign(LEFT);
	text(places.elementAt(currentPlace).getName(), 10, 15);
    }

/**************************************************************************************************************/
    void dispPlaces(){
	tint(255,255);
	textSize(5);
	textAlign(CENTER);
	fill(155, 45, 107);
	imageMode(CENTER);
	rectMode(CENTER);
	noStroke();
	for(int curPlaceIdx = 1; curPlaceIdx < places.size(); curPlaceIdx++){
	    Place curPlace = places.elementAt(curPlaceIdx);
	    ScreenPosition curScreenPos = curPlace.getScreenPosition(umap1);
	    rect(curScreenPos.x, curScreenPos.y, 18, 18);
	    image(curPlace.getImage(), curScreenPos.x, curScreenPos.y, 15, 15);
	}
	/*for(int curPlaceIdx = 1; curPlaceIdx < places.size(); curPlaceIdx++){
	    Place curPlace = places.elementAt(curPlaceIdx);
	    ScreenPosition curScreenPos = curPlace.getScreenPosition(umap1);
	    text(curPlace.getName(), curScreenPos.x, curScreenPos.y - 10);
	}*/
	imageMode(CORNER);
	rectMode(CORNER);
    }

/**************************************************************************************************************/
    void selectPlace(){
	if(touchList.get2DTouchs().isEmpty()){
	    return;
	}
	if(currentPlace != 0){
	    PVector buttonPosition = new PVector(A4BoardSize.x - 10, 36);
	    buttonPosition.sub(currentTouch);
	    if(buttonPosition.mag() < 15){
		currentPlace = 0;
		curPlaceChanged = true;
	    }
	    return;
	}
	boolean found = false;
	int foundPlace = 0;
	for(int placeIdx = 1; (placeIdx < places.size()) && (!found); placeIdx++){
	    Place curPlace = places.elementAt(placeIdx);
	    ScreenPosition curScreenPosition = curPlace.getScreenPosition(umap1);
	    curScreenPosition.sub(currentTouch);
	    if(curScreenPosition.mag() < 15){//TODO put the parameter somewhere
		fill(105, 0, 255);
		ellipse(currentTouch.x, currentTouch.y, currentPlaceSelectionCounter, currentPlaceSelectionCounter);
		foundPlace = placeIdx;
		found = true;
		if(currentPlaceSelection == 0){
		    currentPlaceSelection = placeIdx;
		}
		//System.out.println("Selected a place");
	    }
	    //TODO complete that
	}
	if(foundPlace == currentPlaceSelection){
	    currentPlaceSelectionCounter--;
	}
	else{
	    currentPlaceSelection = foundPlace;
	    currentPlaceSelectionCounter = 15;
	}
	if(currentPlaceSelectionCounter == 0){
	    currentPlace = foundPlace;
	    curPlaceChanged = true;
	    currentPlaceSelection = 0;
	    currentPlaceSelectionCounter = 15;
	}
    }

/**************************************************************************************************************/
    void singleTouch(){
	// Consider only the touch with the min y
	PVector minYVector = new PVector(A4BoardSize.x,A4BoardSize.y);
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
	   
	if((!empty) && (minYVector.x < A4BoardSize.x - menuWidth)){//If there is at least one touch store its position (if it's not too close (in time or space) from the last recorded one)
	    if(positions.isEmpty() && timers.isEmpty()){
		positions.add(minYVector);
		timers.add(millis());
	    }
	    else{
		PVector pPrev = positions.lastElement();
		//float distance = pow(pPrev.x - minYVector.x,2);
		float distance = pow(pPrev.x - minYVector.x,2) + pow(pPrev.y - minYVector.y,2);
		float timeDistance = pow(float(timers.lastElement()) - float(millis()), 2);
		if((distance > spaceStep) && (timeDistance > timeStep)){
		    positions.add(minYVector);
		    //System.out.println("Detected a touch");
		    timers.add(millis());
		}
	    }
	}


	color currentColor = color(2*curCenter.x, 2*curCenter.y, curCenter.z);
	fill(currentColor);
	stroke(0, 0, 0);
	strokeWeight(1);
	strokeJoin(ROUND);
	
	//Display the position of the validates touches
        Iterator<PVector> itr = positions.iterator();
        while(itr.hasNext()){
	    PVector curPos = itr.next();
	    rectMode(CENTER);
	    rect(curPos.x, curPos.y, 15, 15);
            //image(wine, curPos.x, curPos.y - 20, 15, 15);
	}


    }

/**************************************************************************************************************/
    PImage capture(){
	PImage out = cameraTracking.getPView(boardView);
	return out;
    }

/**************************************************************************************************************/

    void wait(int n){//n is the time to wait in millis seconds
	long t0, t1;
	t0 = System.currentTimeMillis();

	do{
	    t1 = System.currentTimeMillis();
	}
	while ((t1 - t0) < n);
    }

/**************************************************************************************************************/
    Vector<PVector> clusterTouch(){
	int nbSteps = 5;
	float threshDist = 10;
	int nbTouches = touchBuffer.size();
	Vector<PVector> centroids = new Vector<PVector>(touchBuffer);
	int[] clusters = new int[nbTouches];
	for(int i = 0; i < nbTouches; i++){
	    clusters[i] = i;
	}

	for(int step = 0; step < nbSteps; step++){
	    //Assign points to their cluster
	    for(int pointIdx = 0; pointIdx < nbTouches; pointIdx++){//For every point in the buffer
		for(int cluster = 0; cluster < centroids.size(); cluster++){//For every cluster
		    if(touchBuffer.elementAt(pointIdx).dist(centroids.elementAt(cluster)) < threshDist){//If the point is closer to the centroid than the threshDist, we assign it to that cluster
			clusters[pointIdx] = cluster;
		    }
		}
	    }
	    //Compute new centroids
	    Vector<PVector> newCentroids = new Vector<PVector>();
	    for(int cluster = 0; cluster < centroids.size(); cluster++){//For all existing clusters
		PVector curCentroid = new PVector(0, 0, 0);
		int nbPointsInCluster = 0;
		for(int pointIdx = 0; pointIdx < nbTouches; pointIdx++){//Find all the points in that cluster
		    if(clusters[pointIdx] == cluster){//If the point is in the cluster
			curCentroid.add(touchBuffer.elementAt(pointIdx));
			nbPointsInCluster++;
		    }
		}
		if(nbPointsInCluster > 0){
		    curCentroid.div(nbPointsInCluster);
		    newCentroids.add(curCentroid);
		}
	    }
	    centroids.removeAllElements();
	    centroids.addAll(newCentroids);
	    //System.out.println(newCentroids.size());
	}

	return centroids;
	//Remember to empty the buffer after using this function
    }


/**************************************************************************************************************/
    PImage detectPostIts(PImage toAnalyze){
	Vector<Color> colorsInTheImage = new Vector<Color>();// Colors found in the image (without redundancy)
	Vector<Color> colorsForDisplay = new Vector<Color>();// Colors used to display the result of the detection

	int imWidth = toAnalyze.width;
	int imHeight = toAnalyze.height;
	PImage result = createImage(imWidth, imHeight, RGB);

	for(int w = 0; w < imWidth; w++){
	    for(int h = 0; h < imHeight; h++){
		boolean found = false;
		color curPixel = toAnalyze.get(w, h);
		for(int cIdx = 0; (cIdx < colorsInTheImage.size()) && (!found); cIdx++){
		    Color curColor = colorsInTheImage.elementAt(cIdx);
		    if(curColor.dist(curPixel) < 70){
			found = true;
			result.set(w, h, colorsForDisplay.elementAt(cIdx).getColor());
		    }
		}
		if(!found){
		    colorsInTheImage.add(new Color(curPixel));
		    color newColor = randomColor();
		    colorsForDisplay.add(new Color(newColor));
		    result.set(w, h, newColor);
		}
	    }
	}

	//Reassign the values of the pixels to the closest color
	for(int w = 0; w < imWidth; w++){
	    for(int h = 0; h < imHeight; h++){
		color curPixel = toAnalyze.get(w, h);
		Color curMinColor = colorsInTheImage.elementAt(0);
		int minIdx = -1;
		for(int cIdx = 0; cIdx < colorsInTheImage.size(); cIdx++){
		    Color curColor = colorsInTheImage.elementAt(cIdx);
		    if(curColor.dist(curPixel) < curMinColor.dist(curPixel)){
			curMinColor = new Color(curColor.getColor());
			minIdx = cIdx;
		    }
		}
		if(minIdx > 0){
		    result.set(w, h, curMinColor.getColor());
		}
	    }
	}


	return result;
    }

/**************************************************************************************************************/
    PImage getConnectedComponents(PImage toAnalyze){
	//TODO find a better technique to find the background of the image (using the count of pixels maybe)
	int sizeNbhd = 5;//5 pixels in every direction => 11*11 neighbourhood
	int imW = toAnalyze.width;
	int imH = toAnalyze.height;
	PImage result = createImage(imW, imH, RGB);
	boolean checkedPixels[][] = new boolean[imW][imH];
	//Initialize array
	for(int x = 0; x < toAnalyze.width; x++){
	    for(int y = 0; y < toAnalyze.height; y++){
		checkedPixels[x][y] = false;
	    }
	}


	/*checkedPixels[0][0] = true;
	result.set(0, 0, color(255));

	color backgroundColor = toAnalyze.get(0,0);*/
	
	//Find background color
	Vector<Color> imageColors = new Vector<Color>();
	Vector<Integer> counts = new Vector<Integer>();
	int maxIdx = -1;
	boolean foundMax = false;
	for(int imX = 0; (imX < imW) && (!foundMax); imX++){
	    for(int imY = 0; (imY < imH) && (!foundMax); imY++){
		color curColor = toAnalyze.get(imX, imY);
		boolean foundColor = false;
		for(int cIdx = 0; (cIdx < imageColors.size()) && (!foundColor); cIdx++){
		    //System.out.println(cIdx);
		    if(imageColors.elementAt(cIdx).dist(curColor) < 2){
			foundColor = true;
			int newCount = counts.elementAt(cIdx).intValue() + 1;
			counts.set(cIdx, new Integer(newCount));
			if(newCount > (imX * imY) / 2){
			    foundMax = true;
			    maxIdx = cIdx;
			}
		    }
		}
		if(!foundColor){
		    imageColors.add(new Color(curColor));
		    counts.add(new Integer(1));
		}
	    }
	}
	
	if(!foundMax){
	    maxIdx = -1;
	    int maxValue = 0;
	    for(int cIdx = 0; cIdx < counts.size(); cIdx++){
		//System.out.println(counts.elementAt(cIdx).intValue());
		if(counts.elementAt(cIdx).intValue() > maxValue){
		    maxIdx = cIdx;
		    maxValue = counts.elementAt(cIdx).intValue();
		}
	    }
	}
	
	color backgroundColor = imageColors.elementAt(maxIdx).getColor();


	for(int imX = 0; imX < imW; imX++){
	    for(int imY = 0; imY < imH; imY++){
		if(!checkedPixels[imX][imY]){
		    checkedPixels[imX][imY] = true;
		    if(colorDistance(backgroundColor, toAnalyze.get(imX, imY)) < 2){//If the pixel is part of the background
			result.set(imX, imY, color(255));
		    }
		    else{//We found a new component
			//Define a color for this new component
			color componentColor = toAnalyze.get(imX, imY);
			color displayColor = randomColor();
			//Find the component
		        Stack pixelsToProcessX = new Stack();
		        Stack pixelsToProcessY = new Stack();
			//pixelsToProcess.push(new PVector(imX, imY));
			pixelsToProcessX.push(new Integer(imX));
			pixelsToProcessY.push(new Integer(imY));
			while(!pixelsToProcessY.empty()){
			    /*PVector currentPixel = pixelsToProcess.pop();
			    int curX = currentPixel.x;
			    int curY = currentPixel.y;*/
			    Integer curX_ = (Integer) pixelsToProcessX.pop();
			    int curX = curX_.intValue();
			    Integer curY_ = (Integer) pixelsToProcessY.pop();
			    int curY = curY_.intValue();
			    /*int curX = (Integer) pixelsToProcessX.pop().intValue();
			      int curY = (Integer) pixelsToProcessY.pop().intValue();*/
			    result.set(curX, curY, displayColor);
			    //Check the neibourhood of this pixel and add the pixels that should be processed
			    for(int nbhdX = max(0, curX - sizeNbhd); nbhdX <= min(imW - 1, curX + sizeNbhd); nbhdX++){
				for(int nbhdY = max(0, curY - sizeNbhd); nbhdY <= min(imH - 1, curY + sizeNbhd); nbhdY++){
				    if(!checkedPixels[nbhdX][nbhdY]){
					if(colorDistance(backgroundColor, toAnalyze.get(nbhdX, nbhdY)) < 2){//If it's of the color of the background
					    checkedPixels[nbhdX][nbhdY] = true;
					    result.set(nbhdX, nbhdY, color(255));
					}
					else{
					    //else if(colorDistance(componentColor, toAnalyze.get(nbhdX, nbhdY)) < 2){//If it's of the color of the current connected component
					    checkedPixels[nbhdX][nbhdY] = true;
					    //pixelsToProcess.push(new PVector(nbhdX, nbhdY));
					    pixelsToProcessX.push(new Integer(nbhdX));
					    pixelsToProcessY.push(new Integer(nbhdY));
					}
				    }
				}
			    }
			}
		    }
		   
		}
	    }
	}

	return result;
    }

/**************************************************************************************************************/
    Vector<Vector<PVector>> detectLines(PImage toAnalyze){//To call on a segmented+connectectedcomponents picture
	//Uncomment the followin section if you have a good capture resolution
	/*int disSize = 3;//Size of the chunks of pixels used for detecting a line. This size should be the size of the pen used to draw the line. Here, a chunk will be 3 * 3 = 9 pixels.
	int proportionThreshold = 50;//If more than 50% of the pixels of the chunk are of the color, we consider the chunk as being of that color

	//Generate average image
	//System.out.println(toAnalyze.width);
	//System.out.println(toAnalyze.height);

	int avWidth = floor(toAnalyze.width/disSize);
	int avHeight = floor(toAnalyze.height/disSize);

	//System.out.println(avWidth);
	//System.out.println(avHeight);
	PImage avImg = createImage(avWidth, avHeight, RGB);
	for(int avX = 0; avX < avWidth; avX++){// x in the average image
	    for(int avY = 0; avY < avHeight; avY++){// y in the average image
		Vector<Color> colors = new Vector<Color>();
		Vector<Integer> counts = new Vector<Integer>();
		//float avRed = 0;
		//float avGreen = 0;
		//float avBlue = 0;
		for(int blocX = disSize * avX; blocX < disSize * (avX + 1); blocX++){
		    for(int blocY = disSize * avY; blocY < disSize * (avY + 1); blocY++){
		        Color curColor = new Color(toAnalyze.get(blocX, blocY));
			boolean foundColor = false;
			for(int colIdx = 0; (colIdx < colors.size()) && (!foundColor); colIdx++){
			    if(colors.elementAt(colIdx).dist(curColor) < 2){
				foundColor = true;
				int curCount = counts.elementAt(colIdx);
				counts.set(colIdx, curCount + 1);
			    }
			}
			if(!foundColor){
			    colors.add(curColor);
			    counts.add(1);
			}
			//avRed += red(curColor);
			//avGreen += green(curColor);
			//avBlue += blue(curColor);
		    }
		}
		//Find the most used color (ie max of counts)
		int maxCounts = 0;
		int maxIdx = -1;
		for(int colIdx = 0; colIdx < colors.size(); colIdx++){
		    int curElement = counts.elementAt(colIdx);
		    if(curElement > maxCounts){
			maxCounts = curElement;
			maxIdx = colIdx;
		    }
		}

		//avRed /= disSize * disSize;
		//avGreen /= disSize * disSize;
		//avBlue /= disSize * disSize;
		//avImg.set(avX, avY, color(avRed, avGreen, avBlue));
		avImg.set(avX, avY, colors.elementAt(maxIdx).getColor());
	    }
	}
	image(avImg, 0, 0, avWidth, avHeight);
	lastCapture = avImg;// TO REMOVE*/

	/*for(int imX = 0; imX < toAnalyze.width; imX++){
	    for(int imY = 0; imY < toAnalyze.height; imY++){

	    }
	}*/

	Vector<Vector<PVector>> paths = new Vector<Vector<PVector>>();
	
	int sizeNbhd = 5;

	int imW = toAnalyze.width;
	int imH = toAnalyze.height;

	boolean checkedPixels[][] = new boolean[imW][imH];
	
	for(int imX = 0; imX < imW; imX++){
	    for(int imY = 0; imY < imH; imY++){
		color curColor = toAnalyze.get(imX, imY);
		if((colorDistance(curColor, color(255)) > 2) && (!checkedPixels[imX][imY])){//We found a new path
		    
		}
	    }
	}

	return paths;
    }

/**************************************************************************************************************/
 

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

/**************************************************************************************************************/
public class Color{
    float cRed;
    float cGreen;
    float cBlue;

    Color(){
	cRed = 0;
	cGreen = 0;
	cBlue = 0;
    }
    Color(color _color){
        cRed = red(_color);
	cGreen = green(_color);
	cBlue = blue(_color);
    }
    public float getRed(){
	return cRed;
    }
    public float getGreen(){
	return cGreen;
    }
    public float getBlue(){
	return cBlue;
    }
    public color getColor(){
	return color(cRed, cGreen, cBlue);
    }
    public float dist(color _color){
	float res = sq(cRed - red(_color));
	res += sq(cGreen - green(_color));
	res += sq(cBlue - blue(_color));
	res = sqrt(res);
	return res;
    }
    public float dist(Color _color){
	return dist(_color.getColor());
    }

    public void add(Color _color){
	cRed += _color.getRed();
	cGreen += _color.getGreen();
	cBlue += _color.getBlue();
    }
}

/**************************************************************************************************************/

public class Place{
    Location loc;
    String name;
    PImage img;

    Place(){
    }

    Place(Location _loc, String _name, PImage _img){
	loc = _loc;
	name = _name;
	img = _img;
    }

    public PImage getImage(){
	return img;
    }

    public Location getLocation(){
	return loc;
    }

    public String getName(){
	return name;
    }

    public ScreenPosition getScreenPosition(UnfoldingMap umap){
	return umap.getScreenPosition(loc);
    }
}


/**************************************************************************************************************/
/*public class Neighbourhood{
    PImage toAnalyze;
    PVector curPixel;
    int sizeNbhd;

    int idxTL;
    int idxTR;
    int idxBR;
    int idxBL;

    int left;
    int right;
    int top;
    int bottom;

    Neighbourhood(PImage _toAnalyze, PVector _curPixel, int _sizeNbhd){
	toAnalyze = _toAnalyze;
	curPixel = _curPixel;
	sizeNbhd = _sizeNbhd;

	idxTL = 0;
	idxTR = idxTL + (getRight() - getLeft());
	idxBR = idxTR + (getBottom() - getTop());
	idxBL = idxBR + (getRight() - getLeft());

	left = getLeft();
	right = getRight();
	top = getTop();
	bottom = getBottom();
    }

    public int getLeft(){
	return max(curPixel.x - sizeNbhd, 0);
    }

    public int getRight(){
	return min(curPixel.x + sizeNbhd, toAnalyze.width - 1);
    }

    public int getTop(){
	return max(curPixel.y - sizeNbhd, 0);
    }

    public int getBottom(){
	return min(curPixel.y + sizeNbhd, toAnalyze.height - 1);
    }

    public int getNbElements(){
	return idxBL + (bottom - top);
    }

    public int getIdxFromPixel(PVector pxl){
	int idx = -1;
	if(pxl.x == left){
	    if(pxl.y == top){
		idx = idxTL;
	    }
	    else{
		idx = idxBL + (bottom - pxl.y);
	    }
	}
	else if(pxl.x = right){
	    idx = idxTR + (pxl.y - top);
	}
	else if(pxl.y = top){
	    idx = idxTL + (pxl.x - left);
	}
	else if(pxl.y = bottom){
	    idx = idxBR + (right - pxl.x);
	}
	return idx;
    }

    public PVector getPixelFromIdx(int idx){
	PVector pxl = new PVector(0, 0, 0);
	
	if((idxTL <= idx) && (idx <= idxTR)){
	    int pxlX = left + idx - idxTL;
	    int pxlY = top;
	    pxl.set(pxlX, pxlY, 0);
	}
	else if((idxTR <= idx) && (idx <= idxBR)){
	    int pxlX = right;
	    int pxlY = top + idx - idxTR;
	    pxl.set(pxlX, pxlY, 0);
	}
	else if((idxBR <= idx) && (idx <= idxBL)){
	    int pxlX = right + idxBR - idx;
	    int pxlY = bottom;
	    pxl.set(pxlX, pxlY, 0);
	}
	else if((idxBl <= idx) && (idx < getNbElements())){
	    int pxlX = left;
	    int pxlY = bottom + idxBL - idx;
	    pxl.set(pxlX, pxlY, 0);
	}
	return pxl;
    }

    public color colorFromIdx(int idx){
	PVector pxl = getPixelFromIdx(idx);
	return toAnalyze.get(pxl.x, pxl.y);
    }

    public color colorFromPixel(PVector pxl){
	return toAnalyze.get(pxl.x, pxl.y);
    }

    public int getOppositeIdx(int idxOrigin){
	int nbElements = getNbElements();
	int idxOpposite = 0;
	if(idxOrigin >= nbElements/2){
	    idxOpposite = idxOrigin - nbElements/2;
	}
	else{
	    idxOpposite = idxOrigin + nbElements/2;
	}
	return idxOpposite;
    }

    public int getNextIdx(int idx){//Loops if the number of elements is reached: be careful to not loop forever
	int nextIdx = idx + 1;
	if(idx + 1 >= getNbElements()){
	    nextIdx = 0;
	}
	return nextIdx;
    }

    public int getPrevIdx(int idx){//Loops if the index gets < 0: be careful to not loop forever
	int prevIdx = idx - 1;
	if(prevIdx < 0){
	    prevIdx = getNbElements() - 1;
	}
	return prevIdx;
    }

    Vector<PVector> exploreBorder(PVector origin){
	color curColor = toAnalyze.get(curPixel.x, curPixel.y);
	//Define neighbourhood
	
	int idxOrigin = getIdxFromPixel(origin);

	if(idxOrigin < 0){
	    idxOrigin = idxTL;//This case shouldn't happend
	}

	int idxOpposite = getOppositeIdx(idxOrigin);

	Vector<PVector> results = new Vector<PVector>();

	//Check if the opposite pixel is of the right color
	PVector pxlOpposite = getPixelFromIdx(idxOpposite);
	if(colorDistance(curColor, getColorFromPixel(pxlOpposite)) < 2){
	    results.add(pxlOpposite);
	}

	int branchPrev = getPrevIdx(idxOpposite);
	int branchNext = getNextIdx(idxOpposite);

	int nbSteps = (getNbElements() / 2) - 2;
	
	for(int step = 0; step < nbSteps; step++){
	    //Check pixel in Prev Branch
	    PVector pxlPrev = getPixelFromIdx(branchPrev);
	    if(colorDistance(curColor, getColorFromPixel(pxlPrev)) < 2){
		results.add(pxlPrev);
	    }
	    branchPrev = getPrevIdx(branchPrev);


	    //Check pixel in Next Branch
	    PVector pxlNext = getPixelFromIdx(branchNext);
	    if(colorDistance(curColor, getColorFromPixel(pxlNext)) < 2){
		results.add(pxlNext);
	    }
	    branchNext = getNextIdx(branchNext);
	}

	return results;
    }

    }*/
