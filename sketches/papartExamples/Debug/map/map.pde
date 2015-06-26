// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

public void setup(){
    size(1200, 900, OPENGL);
    Papart papart = new Papart(this);

    papart.initDebug(1);
    papart.loadTouchInputTUIO();
    papart.loadSketches();
}

void draw(){

}
