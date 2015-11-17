import fr.inria.papart.procam.*;
import fr.inria.papart.multitouch.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;


void settings(){
    fullScreen(P3D);
}

void setup(){

    Papart papart = Papart.projection(this);
    papart.loadTouchInput();

    papart.loadSketches();
    papart.startTracking();
}


void draw(){

}


void keyPressed() {

}
