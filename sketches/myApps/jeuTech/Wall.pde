class DyingObject{
    int creationTime;
    int lifeTime = 2000;

    DyingObject(){
	creationTime = millis();
    }

    boolean isTooOld(){
	return creationTime + lifeTime < millis();
    }
}


class Wall extends DyingObject{

    // We need to keep track of a Body and a width and height
    Body body;
    float w;
    float h;
    
    // Constructor
    Wall(float x, float y, int size) {
	super();
	lifeTime = 200;
	w = size;
	h = size;
	// Add the box to the box2d world
	makeBody(new Vec2(x, y), w, h);
	body.setUserData(this);
    }

  // This function removes the particle from the box2d world
  void killBody() {
    box2d.destroyBody(body);
  }

  // Is the particle ready for deletion?
  boolean done() {
    if(isTooOld()){
	killBody();
	return true;
    }
    return false;
  }

  // Drawing the box
  void display(PGraphicsOpenGL g) {
    // We look at each body and get its screen position
    Vec2 pos = box2d.getBodyPixelCoord(body);
    // Get its angle of rotation
    float a = body.getAngle();

    g.pushMatrix();
    g.rectMode(CENTER);
    g.translate(pos.x, pos.y);
    g.rotate(-a);
    g.fill(60);
    g.noStroke();
    //    g.rect(0, 0, w, h);
    g.ellipse(0, 0, w *2, w * 2);

    g.rectMode(CORNER);
    g.popMatrix();
  }

  // This function adds the rectangle to the box2d world
  void makeBody(Vec2 center, float w_, float h_) {

      // Make the body's shape a circle
      CircleShape cs = new CircleShape();
      cs.m_radius = box2d.scalarPixelsToWorld(w_);
      
      FixtureDef fd = new FixtureDef();
      fd.shape = cs;
      // Parameters that affect physics
      fd.density = density;
      fd.friction = friction;
      fd.restitution = restitution;
      
      // Define the body and make it from the shape
      BodyDef bd = new BodyDef();
      bd.type = BodyType.STATIC;
      bd.position.set(box2d.coordPixelsToWorld(center));
      
      body = box2d.createBody(bd);
      body.createFixture(fd);
      
  }


  // // This function adds the rectangle to the box2d world
  // void makeBody(Vec2 center, float w_, float h_) {

  //   // Define a polygon (this is what we use for a rectangle)
  //   PolygonShape sd = new PolygonShape();
  //   float box2dW = box2d.scalarPixelsToWorld(w_/2);
  //   float box2dH = box2d.scalarPixelsToWorld(h_/2);
  //   sd.setAsBox(box2dW, box2dH);

  //   // Define a fixture
  //   FixtureDef fd = new FixtureDef();
  //   fd.shape = sd;
  //   // Parameters that affect physics
  //   fd.density = 1;
  //   fd.friction = 0.3;
  //   fd.restitution = 0.5;

  //   // Define the body and make it from the shape
  //   BodyDef bd = new BodyDef();
  //   bd.type = BodyType.STATIC;
  //   bd.position.set(box2d.coordPixelsToWorld(center));

  //   body = box2d.createBody(bd);
  //   body.createFixture(fd);

  // }

}
