import fr.inria.papart.procam.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.multitouch.*;
import fr.inria.papart.procam.camera.*;
import java.util.Vector;

import org.bytedeco.javacv.*;
import toxi.geom.*;
import peasy.*;
import java.util.Iterator;
import java.util.ArrayList;
import shiffman.box2d.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;

//Hardware setup
Camera camera;
KinectTouchInput touchInput;
ArrayList<TouchPoint> touchs2D;
ArrayList<Integer> pointers;
ArrayList<PVector> touchIDs;
int nTouchs;
Papart papart;
Skatolo skatolo;

// Box2D
Box2DProcessing box2d;
PolygonShape bodyShape = new PolygonShape();
Vector<Box> boxes;
Vector<Boundary> walls;
Vector<Attractor> attractors;
Vector<Surface> surfaces;
boolean reset_attractors = true;
int maxPointsPerTouch = 1;
boolean isWall, isMagnet, awayFromMenu;
void settings() {
  fullScreen(P3D, 2);
}

void setup() {

  // Hardware setup
  Papart papart = Papart.projection2D(this);
  papart.loadTouchInputKinectOnly();
  touchInput = (KinectTouchInput) papart.getTouchInput();
  pointers = new ArrayList();
  skatolo = new Skatolo(this);
  initButtons();
  initPhysics();


  // Physical elements
  boxes = new Vector<Box>();
  walls = new Vector<Boundary>();
  attractors = new Vector<Attractor>();
  surfaces = new Vector<Surface>();

  // Create Boxes
  for (int i = 0; i<10; i++) {
    boxes.addElement(new Box(random(width/2)+width/4, random(height/2)+height/4, random(60)+10, random(60)+10, 255, 0));
  }

  // Create Walls
  walls.addElement(new Boundary(width/2, 0, width, 10, 255, 0));
  walls.addElement(new Boundary(width/2, height, width, 10, 255, 0));
  walls.addElement(new Boundary(0, height/2, 10, height, 255, 0));
  walls.addElement(new Boundary(width, height/2, 10, height, 255, 0));

  //Attractors
  for (int i = 0; i<maxPointsPerTouch; i++) {
    Attractor a = new Attractor(15, 0, 0);
    a.kill();
    attractors.addElement(a);
  }
  background(0);
}


void initPhysics() {
  box2d = new Box2DProcessing(this);
  box2d.createWorld();
  box2d.setGravity(0, 20);
  float box2Dw = box2d.scalarPixelsToWorld(150);
  float box2Dh = box2d.scalarPixelsToWorld(100);
  bodyShape.setAsBox(box2Dw, box2Dh);
}


void draw() {
  background(0);
  // Display Text
  displayLabels();
  // Touch Points
  touchs2D = new ArrayList<TouchPoint>(touchInput.getTouchPoints2D());
  nTouchs = touchs2D.size();

  //Move Forward in time
  box2d.step();

  //Display boxes, walls, attractors and surface
  for (Surface s : surfaces) {
    s.display();
  }
  for (Box b : boxes) {
    for (Attractor a : attractors) {
      if (a.isAlive) {
        if (isMagnet) {
          Vec2 force = a.attract(b); 
            b.applyForce(force);
        }
      }
      b.display();
    }
  }
  for (Boundary b : walls) {
    b.display();
  }
  rectMode(CORNERS); 
    fill(120, 150, 255, 50); 
    stroke(255); 
    strokeWeight(0.5); 
    rect(0, height, 350, height-250); 
    skatolo.draw(); 
    drawPen();
}