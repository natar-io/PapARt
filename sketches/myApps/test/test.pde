// PapARt library
import fr.inria.papart.procam.*;

public void setup(){
    size(1280, 1024, OPENGL);
    Papart papart = new Papart(this);

    papart.initCamera("0", Camera.Type.FLY_CAPTURE);
    papart.loadSketches();
    papart.startTracking();
}

void draw(){

}
