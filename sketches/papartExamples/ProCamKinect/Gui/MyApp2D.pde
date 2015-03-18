import fr.inria.controlP5.*;
import fr.inria.controlP5.events.*;
import fr.inria.controlP5.gui.controllers.*;

import java.util.ArrayList;
import toxi.geom.Vec3D;
import fr.inria.papart.multitouch.*;


public class MyApp  extends PaperTouchScreen {

    ControlP5 cp5;
    
    int hello = 10;

    void setup() {

	setDrawingSize(297, 210);
	loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);

	// init() to create the screen...
	init();

	// set the graphics context
	currentGraphics = screen.getGraphics();

	// now the graphics are OK. 
	cp5 = new ControlP5(this.parent, this);

	cp5.getMousePointer().disable();
	cp5.setAutoDraw(false);

	cp5.addSlider("hello", 0, 100, 50, 40, 40, 100, 20);
	

	// enable the pointer (and disable the mouse as input) 
	// cp5.getPointer().enable();
	// cp5.getPointer().set(297, 210);
    }

    // TODO: Full integration !
    // TODO: use the pointerList ?
    ArrayList<Integer> pointers = new ArrayList();

    void draw() {

	beginDraw2D();

	background(150);

	//	background(cp5.get("hello").getValue());
	// println("Hello " + hello);
	// background(hello);
	// first draw controlP5

	cp5.draw();
	//	cp5.draw(getGraphics());

	for (Touch t : touchList.get2DTouchs()) {

	    // draw the touch. 
	    PVector p = t.position;

	    colorMode(HSB, 30, 100, 100);
	    fill(t.id % 30, 100, 100);

	    if(t.id != TouchPoint.NO_ID){

		if(!pointers.contains(t.id)){
		    ellipse(p.x, p.y, 100, 100);
		    cp5.addPointer(t.id);
		    pointers.add(t.id);
		    cp5.updatePointerPress(t.id, true);
		} else {
		    cp5.updatePointer(t.id, (int) p.x, (int) p.y);
		}

		cp5.updatePointerPress(t.id, true);
		ellipse(p.x, p.y, 10, 10);
	    }

	    //	    println("Setting Pointer " + p);

	    // cp5.getPointer().set((int) p.x, (int)p.y);
	    // cp5.getPointer().pressed();


	    // pushMatrix();
	    // translate(cp5.getPointer().getX(), cp5.getPointer().getY());
	    // stroke(255);
	    // line(-10,0,10,0);
	    // line(0,-10,0,10);
	    // popMatrix();

	    //	     	println(cp5.isMouseOver());
	}

	ArrayList<Integer> currentTouchIds = touchList.get2DTouchs().getIds();


	ArrayList<Integer> toDelete = (ArrayList<Integer>)pointers.clone();
	toDelete.removeAll(currentTouchIds);

	for(Integer pointerId : toDelete){
	    cp5.updatePointerPress(pointerId, false);
	    cp5.updatePointer(pointerId, 0, 0);
	    cp5.removePointer(pointerId);
	    pointers.remove(pointerId);
	}


	fill(200, 100, 20);
	rect(10, 10, 100, 30);
	endDraw();
  }

}
