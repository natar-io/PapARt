import java.awt.Frame;
import shiffman.box2d.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.joints.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.*;


import fr.inria.skatolo.*;
import fr.inria.skatolo.events.*;
import fr.inria.skatolo.gui.controllers.*;

private Skatolo skatolo;
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
  cf = new ControlFrame();
}



// Step is done in Game.pde

int TOWER = 0;
int MISSILE_LAUNCHER = 10;
int ENNEMI = 11;
int PUSH_UP = 1;
int PUSH_DOWN = 2;
int POWERUP = 3;


public float drawingDistance;
public float attractorDistance;
public float attractorPower= 140;

public float acceleration = 10;

public float density = 1.0;
public float friction = 0.01;
public float restitution = 0.3;


public float towerRateRatio;

public boolean isAttractor(Touch t){
    return t.touchPoint.attachedValue == PUSH_DOWN || t.touchPoint.attachedValue == PUSH_UP;
}

public boolean isTower(Touch t){
    return t.touchPoint.attachedValue == TOWER;
}

public boolean isPushUp(Touch t){
    return t.touchPoint.attachedValue == PUSH_UP;
}

public boolean isEnnemi(Touch t){
    return t.touchPoint.attachedValue == ENNEMI;
}


public void push(Touch t, Missile m){
    float G = attractorPower;

    boolean up = false;
    if(noCameraMode)
	; // TODO: do something...
    else
	up = t.touchPoint.attachedValue == PUSH_DOWN;

    // Stronger missiles are less pushed
    G = G * m.level * 1.5f;

    Vec2 force = new Vec2(0, up ? G : -G);
    m.applyForce(force);
}

public void pushUpDown(Missile m, boolean up){
    float G = attractorPower;

    // Stronger missiles are less pushed
    G = G * m.level * 1.5f;

    Vec2 force = new Vec2(0, up ? G : -G);
    m.applyForce(force);
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



// the ControlFrame class extends PApplet, so we
// are creating a new processing applet inside a
// new frame with a skatolo object loaded
public class ControlFrame extends PApplet {
    int w, h;
    Skatolo skatolo;

    public ControlFrame() {
	super();
	PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }

    public void settings() {
	size(400, 400, P2D);
    }


  public void setup() {
    frameRate(25);
    skatolo = new Skatolo(this);

    skatolo.addSlider("attractorPower").plugTo(mainApplet,"attractorPower")
	.setRange(0, 10)
	.setValue(8)
	.setPosition(20,20);

    skatolo.addSlider("attractorDistance").plugTo(mainApplet,"attractorDistance")
	.setRange(10, 200)
	.setValue(60)
	.setPosition(20,40);

    skatolo.addSlider("drawingDistance").plugTo(mainApplet,"drawingDistance")
	.setRange(1, 20)
	.setValue(5)
	.setPosition(20,50);


    skatolo.addSlider("acceleration").plugTo(mainApplet,"acceleration")
	.setRange(-100, 100)
	.setPosition(20,60);


    float y = 60;

    y+= 20;
    skatolo.addSlider("colorDistDrawing").plugTo(mainApplet,"colorDistDrawing")
	.setRange(0, 255)
	.setValue(40)
	.setPosition(20, y);

    y+= 20;
    skatolo.addSlider("colorDistObject").plugTo(mainApplet,"colorDistObject")
	.setRange(0, 50)
	.setValue(15)
	.setPosition(20,y);

    y+= 20;
    skatolo.addSlider("colorNbObject").plugTo(mainApplet,"colorNbObject")
	.setRange(0, 30)
	.setValue(5)
	.setPosition(20, y);

    y+= 20;
    skatolo.addSlider("towerPowerUpThreshold").plugTo(mainApplet,"towerPowerUpThreshold")
	.setRange(5, 150)
	.setValue(75)
	.setPosition(20, y);

    y+= 20;
    skatolo.addSlider("levelPixelRatio").plugTo(mainApplet,"levelPixelRatio")
	.setRange(0, 30)
	.setValue(17.80f)
	.setPosition(20, y);

    y+= 20;
    skatolo.addSlider("towerRateRatio").plugTo(mainApplet,"towerRateRatio")
	.setRange(0, 100)
	.setValue(30)
	.setPosition(20, y);

  }

  public void draw() {
      background(100);
  }


  public Skatolo control() {
    return skatolo;
  }
}
