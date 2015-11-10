// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

void settings(){
    size(640, 480, OPENGL);
}

public void setup(){
    Papart papart = new Papart(this);

    papart.initDebug();
    papart.loadTouchInputTUIO();
    papart.loadSketches();
}

void draw(){

}
