import java.awt.Frame;
import shiffman.box2d.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.joints.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.*;

import controlP5.*;
private ControlP5 cp5;
ControlFrame cf;

// A reference to our box2d world
Box2DProcessing box2d;
ArrayList<Missile> missiles;
ArrayList<Wall> walls;

void initPhysics(){

    // Initialize box2d physics and create the world
  box2d = new Box2DProcessing(this);
  box2d.createWorld();

  // Add a listener to listen for collisions!
  box2d.world.setContactListener(new CustomListener());

  box2d.world.setGravity(new Vec2(0,0));

  // Create the empty list
  missiles = new ArrayList<Missile>();
  walls = new ArrayList<Wall>();
  
  // by calling function addControlFrame() a
  // new frame is created and an instance of class
  // ControlFrame is instanziated.
  cf = addControlFrame("physics", 200,200);
}



// Step is done in Game.pde

int TOWER = 0;
int MISSILE_LAUNCHER = 10;
int SPEEDUP = 1;
int ATTRACTOR = 2;
int POWERUP = 3;
int GOAL = 3;

public float attractorDistance = 140;
public float attractorPower= 140;
public float acceleration = 1.0;

public float density = 1.0;
public float friction = 0.01;
public float restitution = 0.3;


public boolean isAttractor(Touch t){
    return t.touchPoint.attachedValue == ATTRACTOR;
}

public boolean isSpeedUp(Touch t){
    return t.touchPoint.attachedValue == SPEEDUP;
}

public void attract(Touch t, Missile m){
    float G = attractorPower;

    Vec2 touchPos = box2d.coordPixelsToWorld(t.position.x, t.position.y);
    Vec2 missilePos = m.getPhysicsPos();

    Vec2 force = touchPos.sub(missilePos);
    float distance = force.length();
    // Keep force within bounds
    distance = constrain(distance,1,5);
    force.normalize();
    // Note the attractor's mass is 0 because it's fixed so can't use that
    // float strength = (G * 1 * m.body.m_mass) / (distance * distance); // Calculate gravitional force magnitude
    force.mulLocal(acceleration);         // Get force vector --> magnitude * direction

    m.applyForce(force);
}

public void speedUp(Touch t, Missile m){
    m.accelerate(acceleration);
}


// ----------- Physics Frame ----------


ControlFrame addControlFrame(String theName, int theWidth, int theHeight) {
  Frame f = new Frame(theName);
  ControlFrame p = new ControlFrame(this, theWidth, theHeight);
  f.add(p);
  p.init();
  f.setTitle(theName);
  f.setSize(p.w, p.h);
  f.setLocation(100, 100);
  f.setResizable(false);
  f.setVisible(true);
  return p;
}


// the ControlFrame class extends PApplet, so we 
// are creating a new processing applet inside a
// new frame with a controlP5 object loaded
public class ControlFrame extends PApplet {
  int w, h;
  ControlP5 cp5;
  Object parent;
  
  public void setup() {
    size(w, h);
    frameRate(25);
    cp5 = new ControlP5(this);

    cp5.addSlider("attractorPower").plugTo(parent,"attractorPower")
	.setRange(100, 2000)
	.setPosition(20,20);

    cp5.addSlider("attractorDistance").plugTo(parent,"attractorDistance")
	.setRange(10, 200)
	.setPosition(20,40);

    cp5.addSlider("acceleration").plugTo(parent,"acceleration")
	.setRange(-100, 100)
	.setPosition(20,60);


    float y = 60;

    y+= 20;
    cp5.addSlider("colorDistDrawing").plugTo(parent,"colorDistDrawing")
	.setRange(0, 255)
	.setValue(40)
	.setPosition(20, y);

    y+= 20;
    cp5.addSlider("colorDistObject").plugTo(parent,"colorDistObject")
	.setRange(0, 50)
	.setValue(15)
	.setPosition(20,y);

    y+= 20;
    cp5.addSlider("colorNbObject").plugTo(parent,"colorNbObject")
	.setRange(0, 30)
	.setValue(5)
	.setPosition(20, y);

    y+= 20;
    cp5.addSlider("towerPowerUpThreshold").plugTo(parent,"towerPowerUpThreshold")
	.setRange(5, 150)
	.setValue(70)
	.setPosition(20, y);

    y+= 20;
    cp5.addSlider("levelPixelRatio").plugTo(parent,"levelPixelRatio")
	.setRange(0, 30)
	.setValue(13.5f)
	.setPosition(20, y);


  }

  public void draw() {
      background(100);
  }
 
  private ControlFrame() {
  }

  public ControlFrame(Object theParent, int theWidth, int theHeight) {
    parent = theParent;
    w = theWidth;
    h = theHeight;
  }
  public ControlP5 control() {
    return cp5;
  }
}
