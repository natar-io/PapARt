import shiffman.box2d.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.joints.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.*;

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
}

// Step is done in Game.pde
