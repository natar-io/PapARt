import fr.inria.papart.procam.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.multitouch.*;
import fr.inria.papart.procam.camera.*;

import org.bytedeco.javacv.*;
import toxi.geom.*;
import peasy.*;
import java.util.Iterator;
import java.util.ArrayList;

Camera camera;


KinectTouchInput touchInput;

PFont font;
PGraphicsOpenGL bloodGraphics;

void settings(){
    fullScreen(P3D);
}


void setup(){

    Papart papart = Papart.projection2D(this);

    // arguments are 2D and 3D precision.
    papart.loadTouchInputKinectOnly();
    touchInput = (KinectTouchInput) papart.getTouchInput();

    initBlood();

    background(0);
    font = loadFont("WCRhesusBBta-48.vlw"); //load the font stored in the data file

}


ArrayList<TouchPoint> touchs2D;
int nTouchs;
void draw(){

    //    background(0, 0, 0);

    // Get a copy, as the arrayList is constantly modified
     touchs2D = new ArrayList<TouchPoint>(touchInput.getTouchPoints2D());
     nTouchs = touchs2D.size();

     drawBlood();

     image(bloodGraphics, 0, 0);
}


boolean perCentChance(float value){
    return random(1) <  (value / 100f);
}

void keyPressed() {

    // Placed here, bug if it is placed in setup().
    if(key == ' '){
	bloodGraphics.beginDraw();
	bloodGraphics.background(0);
	bloodGraphics.endDraw();
    }
}
