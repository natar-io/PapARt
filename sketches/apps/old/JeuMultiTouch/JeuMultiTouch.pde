import fr.inria.papart.procam.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.multitouch.*;

import org.bytedeco.javacv.*;
import toxi.geom.*;


/*
boolean lines determines whether our lines for particle relationships are drawn
toggle this by pressing any key, but 'g'
*/
boolean lines = false;
/*
gravity toggle
toggle this by pressing 'g'
*/
boolean gravity = true;
/* Particle count */
int particleCount = 1000;
/* Particle array */
Particle[] particles = new Particle[particleCount+1];



KinectTouchInput touchInput;

void settings(){
    fullScreen(P3D);
}

void setup(){
    Papart papart = Papart.projection2D(this);

    // arguments are 2D and 3D precision.
    papart.loadTouchInputKinectOnly();
    touchInput = (KinectTouchInput) papart.getTouchInput();

  /* RGB colormode, with range of 0 to 400 */
  colorMode(RGB, 400);
  /* Frame rate is 30 */
  fill (0);
  /* We loop through our particle count and initialize each */
  for (int x = particleCount; x >= 0; x--) {
    particles[x] = new Particle();
  }

}


ArrayList<PVector> pointers = new ArrayList<PVector>();

void draw() {

    background(0);

    pointers.clear();

    ArrayList<TouchPoint> touchs3D = new ArrayList<TouchPoint>(touchInput.getTouchPoints3D());
    for(TouchPoint tp : touchs3D){

	PVector pos = tp.getPosition();
	stroke(200);
	fill(#FFAAAA);
	hasPointer = true;
	pointerX = (int) (pos.x * width);
	pointerY = (int) (pos.y * height);
	pointerZ = (1-pos.z) ;
        pointers.add(new PVector(pointerX, pointerY, pointerZ));

	ellipse(pointerX, pointerY, 10, 10);

	// break;
    }


  noFill();
  //  popStyle();

  /* The stroke color is set to white for the border. */
  stroke (400);
  /* The border; it is 10 pixels from all sides */
  quad (10,10,10,height-10,width-10,height-10,width-10,10);
  /* All of our particles are looped through, and updated */
  for (int i = particleCount; i >= 0; i--) {
    Particle particle = (Particle) particles[i];
    particle.update(i);
  }


}

boolean hasPointer = false;
int pointerX;
int pointerY;
float pointerZ;
float gravityValue = -0.7f;

/* User presses a key */
void keyPressed() {
  /* if they press g */
  if (key == 'g' || key == 'G') {
    /* we toggle gravity */
    if (gravity) gravity = false;
    else gravity = true;
  }
  /* otherwise */

    /* we toggle line relationships */
  if(key == 'l')
    lines = !lines;

}
