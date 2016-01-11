import fr.inria.papart.procam.*;
import fr.inria.papart.multitouch.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import processing.video.*;
import TUIO.*;
import toxi.geom.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.procam.display.*;
import fr.inria.skatolo.Skatolo;
import fr.inria.guimodes.Mode;

boolean useProjector = true;
float renderQuality = 1.5f;
Papart papart;

KinectTouchInput touchInput;

boolean noCamera = false;


void settings(){
    fullScreen(P3D);
}

void setup(){


    if(noCamera) {

	Papart papart = new Papart(this);
	papart.initNoCamera(1);
	papart.loadTouchInputTUIO();
	papart.loadSketches();
	return;
    }


    if(useProjector){
	papart = Papart.projection(this);
	papart.loadTouchInput();
    } else {

	papart = new Papart(this);

	papart.initKinectCamera(renderQuality);
	papart.loadTouchInputKinectOnly();
	BaseDisplay display = papart.getDisplay();
	display.setDrawingSize(width, height);
    }

    touchInput = (KinectTouchInput) papart.getTouchInput();

    papart.loadSketches();
    papart.startTracking();
}


void draw(){
  //  println("FrameRate " + frameRate);


}

boolean test = false;
boolean saveDiceColors = false;
boolean saveOrangeColor = false;


void keyPressed() {

    println("key " + key);

    if(key == 'd'){
	saveDiceColors = true;
	println("SaveDiceColor");
    }
    if(key == 'o'){
	saveOrangeColor = true;
	println("SaveOrangeColor");
    }
    if(key == 'B')
	board.saveLocationTo("board.xml");
    if(key == 'b')
	board.loadLocationFrom("board.xml");

    if(key == 'K')
	counter.saveLocationTo("counter.xml");
    if(key == 'k')
	counter.loadLocationFrom("counter.xml");

    if(key == 'S')
	myStatus.saveLocationTo("myStatus.xml");
    if(key == 's')
	myStatus.loadLocationFrom("myStatus.xml");


    if(key == 't')
	test = !test;

    if(key == 'r')
	removeLastToken();

    if(key == 'n')
	nextPlayer();


    if (key == CODED) {
	if (keyCode == LEFT) {
	    undo();
	}
    }

    if(key == ' ')
	action();

}
