import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.drawingapp.*;
import fr.inria.papart.multitouch.*;
import fr.inria.papart.drawingapp.Button;
import fr.inria.guimodes.Mode;
import org.bytedeco.javacpp.*;
import processing.opengl.*;
import org.reflections.*;

import fr.inria.papart.tracking.*;
import fr.inria.skatolo.Skatolo;

import fr.inria.papart.tracking.*;
import fr.inria.skatolo.Skatolo;
import fr.inria.papart.procam.ColorDetection;


import toxi.geom.*;
import peasy.*;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Vector;

import shiffman.box2d.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.joints.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.*;


//Hardware setup
Papart papart;
MarkerBoard markerBoardDrawing ;
ArrayList<Integer> pointers;
ArrayList<PVector> touchIDs;
TouchList touchs2D;
Skatolo skatolo;
PlayField play;
int nTouchs;
boolean useProjector;
PVector A4BoardSize = new PVector(297, 210);   //  21 * 29.7 cm
PVector interfaceSize = new PVector(60, 40);   //  6 * 4 cm

// Box 2D
Box2DProcessing box2d;
PolygonShape bodyShape = new PolygonShape();
public Vector<Box> boxes;
Vector<Boundary> walls;
Vector<Attractor> attractors;
Vector<Surface> surfaces; // Hand drawn surfaces
Vector<Boundary> drawnWalls; // Pen drawn boundaries (color detection)

Destructor d;
boolean reset_attractors = true;
int maxPointsPerTouch = 1;
boolean isWall, isMagnet;

public void settings() {
  fullScreen(P3D);
}

public void setup() {
  smooth();
  papart = Papart.projection(this);
  papart.loadTouchInput();
  papart.loadSketches() ;
  papart.startTracking();
  initPhysics();
}

void draw() {
}

void initPhysics() {
  box2d = new Box2DProcessing(this);
  box2d.createWorld();
  box2d.listenForCollisions();
  box2d.setGravity(20, 0);
  float box2Dw = box2d.scalarPixelsToWorld(150);
  float box2Dh = box2d.scalarPixelsToWorld(100);
  bodyShape.setAsBox(box2Dw, box2Dh);
}




// Collision event functions!
void beginContact(Contact cp) {
  // Get both shapes
  Fixture f1 = cp.getFixtureA();
  Fixture f2 = cp.getFixtureB();
  // Get both bodies
  Body b1 = f1.getBody();
  Body b2 = f2.getBody();
  println("contact");
  // Get our objects that reference these bodies
  Object o1 = b1.getUserData();
  Object o2 = b2.getUserData();

  if (o1.getClass() == Box.class && o2.getClass() == Destructor.class) {
    Box b = (Box) o1;
    b.kill();
  }
}

// Objects stop touching each other
void endContact(Contact cp) {
}