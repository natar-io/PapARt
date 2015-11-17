import fr.inria.papart.procam.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.multitouch.*;

import org.bytedeco.javacv.*;
import toxi.geom.*;



KinectTouchInput touchInput;

PApplet applet;

void settings(){
    fullScreen(P3D);
}

void setup(){
    Papart papart = Papart.projection2D(this);

    // arguments are 2D and 3D precision.
    papart.loadTouchInputKinectOnly();
    touchInput = (KinectTouchInput) papart.getTouchInput();
    applet = this;
}


ArrayList<Sine> allSine = new ArrayList<Sine>();
ArrayList<Sine> sineToDelete = new ArrayList<Sine>();


void draw() {

  background(0); 

  
  for(TouchPoint tp : touchInput.getTouchPoints2D()){
      if(tp.attachedObject == null){
	  Sine s = new Sine(tp);
	  tp.attachedObject = s;

	  allSine.add(s);
      }
  }
  
  println("There are " + allSine.size() + " active sine ");
  
  for(Sine s : allSine){
      s.update();
      ellipse(s.tp.getPosition().x * width, s.tp.getPosition().y *height, 20, 20); 
  }
  
  stroke(200);
  // for(int i = 0; i < out.bufferSize() - 1; i++)
  //     {
  // 	  float x1 = map(i, 0, out.bufferSize(), 0, width);
  // 	  float x2 = map(i+1, 0, out.bufferSize(), 0, width);
  // 	  line(x1, 50 + out.left.get(i)*50, x2, 50 + out.left.get(i+1)*50);
  // 	  line(x1, 150 + out.right.get(i)*50, x2, 150 + out.right.get(i+1)*50);
  //     }
  
  
  deleteSines();
}


void deleteSines(){

  if(sineToDelete.isEmpty()){
    return;
  }

  for(Sine s : sineToDelete){
    allSine.remove(s);
  }
  
  sineToDelete.clear();
}



void keyPressed() {
}

