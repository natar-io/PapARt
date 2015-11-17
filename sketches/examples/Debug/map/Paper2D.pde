import fr.inria.papart.multitouch.*;

import de.fhpotsdam.unfolding.*;
import de.fhpotsdam.unfolding.geo.*;
import de.fhpotsdam.unfolding.utils.*;
import de.fhpotsdam.unfolding.mapdisplay.*;


public class MyApp  extends PaperTouchScreen {


    UnfoldingMap map;

    void settings(){
        setDrawingSize(1000, 800);
	loadMarkerBoard(Papart.markerFolder + "A3-small1.cfg", 1000, 800);
    }

    void setup() {

	map = new UnfoldingMap(parent, 0, 0, 1000, 800);
       	map.zoomAndPanTo(new Location(52.5f, 13.4f), 10);
	MapUtils.createDefaultEventDispatcher(parent, map);

	map.switchTweening();
    }

    void draw() {

	// setLocation(mouseX, mouseY,0 );

	OpenGLMapDisplay mapDisplay = (OpenGLMapDisplay) map.mapDisplay;
	// mapDisplay.innerOffsetX = mouseX;
	// mapDisplay.innerOffsetY = mouseY;

	PGraphics inner =  mapDisplay.getInnerPG();
	PGraphics outer = mapDisplay.getOuterPG();

	map.draw();
	//	mapDisplay.draw();


	beginDraw2D();

	background(100);


       	image(inner, 0, 0, drawingSize.x, drawingSize.y);

	fill(255);
	translate(mouseX, mouseY);
	rect(0, 0, 100, 100);
	//	image(outer, width /2, 0, width / 2, height);



	endDraw();
    }
}
