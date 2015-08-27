import org.bytedeco.javacpp.ARToolKitPlus;
import org.bytedeco.javacpp.ARToolKitPlus.TrackerMultiMarker;

import fr.inria.skatolo.*;
import fr.inria.skatolo.events.*;
import fr.inria.skatolo.gui.controllers.*;

public class Menu extends PaperTouchScreen {
    int buttonSize = 30;
    int buttonSpace = 10;

    //Palette
    color cPointer = color(141, 215, 12);
    color cPointerEffect = color(40, 172, 52);
    color cOverlay = color(225, 3, 196);
    color cSelected = color(162, 33, 174);


    //For the touch
    PVector currentTouch = new PVector(-1, -1);

    //For the capture
    TrackedView menuView;
    // 5cm  ->  50 x 50 pixels 
    PVector captureSize = new PVector(MenuSize.x, MenuSize.y);
    PVector origin = new PVector(0, 0, 0);
    //int picSizeX = (int) A4BoardSize.x - menuWidth; // Works better with power  of 2 (initially 64)
    //int picSizeY = (int) A4BoardSize.y; // Works better with power  of 2 (initially 64)
    int picSizeX = 128;
    int picSizeY = 128;
    
    void setup(){
	setDrawingSize((int) MenuSize.x, (int) MenuSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/menu.cfg",
			(int) MenuSize.x, (int) MenuSize.y);
	//loadMarkerBoard(sketchPath + "/data/markers/menu2.png",
	  //(int) MenuSize.x, (int) MenuSize.y);

	//Add view for the tracking
	menuView = new TrackedView(markerBoard, origin, captureSize, picSizeX, picSizeY);
	
    }


    void draw() {
	updateTouch();
	beginDraw2D();

	background(0);


	/*for (Touch t : touchList.get2DTouchs()) {

	    // draw the touch. 
	    PVector p = t.position;
	    fill(255, 0, 0);
	    ellipse(p.x, p.y, 12, 12);

	    }*/
	getColors();
	findCurrentTouch();
	//drawCurrentTouch(cPointer);
	endDraw();
    }

    void getColors(){
	colors.clear();
	PImage capt = capture();
	//image(capt, 0, 40, 130, 130);
	for(int i = 0; i < 3; i++){
	    for(int j = 0; j < 3; j++){
		int pixMinX = (patchSpace + patchSize) * i + patchSpace + 5;
		int pixMaxX = (patchSpace + patchSize) * (i + 1) - 5;
		int pixMinY = (patchSpace + patchSize) * j + patchSpace + 5;
		int pixMaxY = (patchSpace + patchSize) * (j + 1) - 5;
		for(Touch t : touchList.get2DTouchs()){
		    PVector p = t.position;
		    if(pixMinX < p.x && p.x < pixMaxX){
			if(pixMinY < p.y && p.y < pixMaxY){
			    Color avg = new Color();
			    avg.getAverageColor(capt, pixMinX, pixMaxX, pixMinY, pixMaxY);
			    colors.add(avg);
			    //avg.print();
			    fill(avg.getColor());
			    ellipse(pixMinX, pixMinY, 5, 5);
			    ellipse(pixMinX, pixMaxY, 5, 5);
			    ellipse(pixMaxX, pixMinY, 5, 5);
			    ellipse(pixMaxX, pixMaxY, 5, 5);
			    rect(pixMinX, pixMinY + 70, pixMaxX - pixMinX, pixMaxY - pixMinY);
			}
		    }
	        }
		/*Color avg = new Color();
		PImage piece = avg.getAverageColor(capt, pixMinX, pixMaxX, pixMinY, pixMaxY);
		colors.add(avg);
		avg.print();
		fill(avg.getColor());
		ellipse(pixMinX, pixMinY, 5, 5);
		ellipse(pixMinX, pixMaxY, 5, 5);
		ellipse(pixMaxX, pixMinY, 5, 5);
		ellipse(pixMaxX, pixMaxY, 5, 5);
		image(piece, pixMinX, pixMinY + 70);*/
	    }
	}
    }

    PImage capture(){
	PImage out = cameraTracking.getPView(menuView);
	return out;
    }


    void findCurrentTouch(){
	PVector minYVector = new PVector(A4BoardSize.x,A4BoardSize.y);//TODO implement a method "findTouchMinY" for this method and singleTouch method
	float minY = Float.MAX_VALUE;
	boolean empty = true;
	for (Touch t : touchList.get2DTouchs()) {
	    empty = false;
	    PVector p = t.position;
	    fill(255, 0, 0);
	    //ellipse(p.x, p.y, 12, 12);
	    if(p.y < minY){
		minYVector = p;
		minY = p.y;
	    }
	}
	if(!empty){
	    //System.out.println("Found touch");
	    currentTouch = minYVector;
	    //System.out.println(currentTouch.x);
	    //System.out.println(currentTouch.y);
	    //fill(255, 0, 0);
	    //ellipse(currentTouch.x, currentTouch.y, 12, 12);
	}
    }


    void drawCurrentTouch(color c){
	if(!touchList.get2DTouchs().isEmpty()){
	    fill(c);
	    stroke(c);
	    ellipse(currentTouch.x, currentTouch.y, 12, 12);
	}
    }

 
    
}

