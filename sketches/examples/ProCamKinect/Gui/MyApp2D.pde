import fr.inria.skatolo.*;
import fr.inria.skatolo.events.*;
import fr.inria.skatolo.gui.controllers.*;

import java.util.ArrayList;
import toxi.geom.Vec3D;
import fr.inria.papart.multitouch.*;


public class MyApp  extends PaperTouchScreen {

    Skatolo skatolo;

    int hello = 10;

    void settings() {

        setDrawingSize(297, 210);
	loadMarkerBoard(Papart.markerFolder + "A3-small1.cfg", 297, 210);

    }

    void setup() {

	skatolo = new Skatolo(this.parent, this);

	skatolo.getMousePointer().disable();
	skatolo.setAutoDraw(false);

	skatolo.addSlider("Sliiiide")
	    .setPosition(0, 0)
	    .setSize(130, 10)
	    ;

	skatolo.addButton("button")
	    .setPosition(130, 80)
	    .setSize(60, 60)
	    ;

	skatolo.addToggle("toggle")
	    .setPosition(130, 120)
	    .setSize(60, 60)
	    ;


	Slider2D s = skatolo.addSlider2D("wave")
	    .setPosition(30,40)
	    .setSize(100,100)
	    .setArrayValue(new float[] {50, 50})
	    //.disableCrosshair()
	    ;

	// skatolo.addNumberbox("numberbox")
	//     .setPosition(100,160)
	//     .setSize(100,14)
	//     .setScrollSensitivity(0.1)
	//     .setValue(50)
	//     ;
    }

    // TODO: Full integration !
    // TODO: use the pointerList ?
    ArrayList<Integer> pointers = new ArrayList();

    void drawOnPaper(){

        background(150);

        skatolo.draw();

	for (Touch t : touchList.get2DTouchs()) {

	    // draw the touch.
	    PVector p = t.position;

	    colorMode(HSB, 30, 100, 100);
	    fill(t.id % 30, 100, 100);

	    if(t.id != TouchPoint.NO_ID){

		if(!pointers.contains(t.id)){
		    ellipse(p.x, p.y, 100, 100);
		    skatolo.addPointer(t.id);
		    pointers.add(t.id);
		    skatolo.updatePointerPress(t.id, true);
		} else {
		    skatolo.updatePointer(t.id, (int) p.x, (int) p.y);
		}

		skatolo.updatePointerPress(t.id, true);
		ellipse(p.x, p.y, 10, 10);
	    }

	    //	    println("Setting Pointer " + p);

	    // skatolo.getPointer().set((int) p.x, (int)p.y);
	    // skatolo.getPointer().pressed();


	    // pushMatrix();
	    // translate(skatolo.getPointer().getX(), skatolo.getPointer().getY());
	    // stroke(255);
	    // line(-10,0,10,0);
	    // line(0,-10,0,10);
	    // popMatrix();

	    //	     	println(skatolo.isMouseOver());
	}

	ArrayList<Integer> currentTouchIds = touchList.get2DTouchs().getIds();

	ArrayList<Integer> toDelete = (ArrayList<Integer>)pointers.clone();
	toDelete.removeAll(currentTouchIds);

	for(Integer pointerId : toDelete){
	    // skatolo.updatePointerPress(pointerId, false);
	    // skatolo.updatePointer(pointerId, 0, 0);
	    skatolo.removePointer(pointerId);
	    pointers.remove(pointerId);
	}


	fill(200, 100, 20);
	rect(10, 10, 100, 30);
    }

}
