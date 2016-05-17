// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import toxi.geom.*;

PImage projImg;

void setup(){
    Papart.projection2D(this);
    
    projImg = loadImage("ProjView.png");
}

void draw(){
    background(0);

    image(projImg, 0, 0, width, height);

}

void keyPressed() {
  
    if(key == 'l'){
	projImg = loadImage("ProjView.png");
    }
}
