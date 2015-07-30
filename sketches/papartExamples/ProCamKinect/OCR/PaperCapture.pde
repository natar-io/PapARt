// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

void settings(){
    fullScreen(P3D);
}

public void setup(){
//    Papart papart = Papart.seeThrough(this);
        Papart papart = Papart.projection(this);
    papart.loadSketches();
    papart.startTracking();
}

void draw(){

}
