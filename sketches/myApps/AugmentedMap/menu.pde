import org.bytedeco.javacpp.ARToolKitPlus;
import org.bytedeco.javacpp.ARToolKitPlus.TrackerMultiMarker;

import fr.inria.skatolo.*;
import fr.inria.skatolo.events.*;
import fr.inria.skatolo.gui.controllers.*;

public class Menu extends PaperTouchScreen {
    TrackedView menuView;
    int buttonSize = 30;
    int buttonSpace = 10;

    //Palette
    color cPointer = color(141, 215, 12);
    color cPointerEffect = color(40, 172, 52);
    color cOverlay = color(225, 3, 196);
    color cSelected = color(162, 33, 174);


    //For the touch
    PVector currentTouch = new PVector(-1, -1);
    
    void setup(){
	setDrawingSize((int) MenuSize.x, (int) MenuSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/menu.cfg",
			(int) MenuSize.x, (int) MenuSize.y);//TODO change this size to put the size of an A5 paper
	//loadMarkerBoard(sketchPath + "/data/markers/menu2.png",
	  //(int) MenuSize.x, (int) MenuSize.y);
	
	//Button Capture
	PImage captImg = loadImage(sketchPath + "/data/images/capt_button.png");
	PVector captCenter = new PVector(buttonSpace + buttonSize / 2, buttonSpace + buttonSize / 2, 0);
	yButtons.add(new YButton(captImg, captCenter, buttonSize, buttonSize));

	//Button Home
	PImage homeImg = loadImage(sketchPath + "/data/images/Arms_of_Bordeaux.png");
	PVector homeCenter = new PVector(2 * buttonSpace + 3 * buttonSize / 2, buttonSpace + buttonSize / 2, 0);
	YButton homeButton = new YButton(homeImg, homeCenter, buttonSize, buttonSize);
	homeButton.setVisible(false);
	yButtons.add(homeButton);
    }


    void draw() {
	updateTouch();
	beginDraw2D();

	background(255);


	/*for (Touch t : touchList.get2DTouchs()) {

	    // draw the touch. 
	    PVector p = t.position;
	    fill(255, 0, 0);
	    ellipse(p.x, p.y, 12, 12);

	    }*/
	findCurrentTouch();
	updateYButtons();
	drawYButtons();
	drawCurrentTouch(cPointer);
	endDraw();
    }


    void findCurrentTouch(){
	PVector minYVector = new PVector(A4BoardSize.x,A4BoardSize.y);//TODO implement a method "findTouchMinY" for this method and singleTouch method
	float minY = Float.MAX_VALUE;
	boolean empty = true;
	for (Touch t : touchList.get2DTouchs()) {
	    empty = false;
	    PVector p = t.position;
	    fill(255, 0, 0);
	    ellipse(p.x, p.y, 12, 12);
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

    private void drawYButtons(){
	imageMode(CENTER);
	noFill();
	rectMode(CENTER);
	for(int b = 0; b < yButtons.size(); b++){
	    YButton curButton = yButtons.elementAt(b);
	    if(curButton.getVisible()){

		PImage curImg = curButton.getImage();
		PVector curCenter = curButton.getCenter();
		int curWidth = curButton.getWidth();
		int curHeight = curButton.getHeight();

		if(curButton.isTouched()){
		    stroke(cOverlay);
		    rect(curCenter.x, curCenter.y, curWidth, curHeight);
		    noStroke();
		}

		if(curButton.getPressed()){
		    stroke(cSelected);
		    rect(curCenter.x, curCenter.y, curWidth, curHeight);
		    noStroke();
		}

		image(curImg, curCenter.x, curCenter.y, curWidth, curHeight);


	    }
	}
	rectMode(CORNER);
	imageMode(CORNER);
    }

    private void updateYButtons(){
	for(int b = 0; b < yButtons.size(); b++){
	    if(touchList.get2DTouchs().isEmpty()){
		yButtons.elementAt(b).reset();
	    }
	    else{
		yButtons.elementAt(b).update(currentTouch);
	    }
	}
    }
}

