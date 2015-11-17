import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.drawingapp.*;
import fr.inria.papart.multitouch.*;
import fr.inria.papart.drawingapp.Button; 
import org.bytedeco.javacpp.*;
import processing.opengl.*;
import org.reflections.*;


float boardResolution = 4;   //  pixels / mm

boolean useProjector;

// check
PVector A4BoardSize = new PVector(297, 210);   //  21 * 29.7 cm
PVector interfaceSize = new PVector(60, 40);   //  6 * 4 cm

MarkerBoard markerBoardDrawing ;

Papart papart; 

void settings(){
    fullScreen(P3D);
}

public void setup(){

    papart = Papart.projection(this); 
    papart.loadTouchInput();

    papart.loadSketches() ;
    papart.startTracking();
}

void draw(){
}

boolean test = false;
void keyPressed() {
    if(key == 't') 
	test = !test;

}
 
