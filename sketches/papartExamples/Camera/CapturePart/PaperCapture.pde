// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

public void setup(){
    Papart papart = Papart.seeThrough(this);
    papart.loadSketches();
    papart.startTracking();
}

void draw(){

}
