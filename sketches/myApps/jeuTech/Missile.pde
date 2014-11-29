// The Nature of Code
// <http://www.shiffman.net/teaching/nature>
// Spring 2010
// Box2DProcessing example

// A circular particle

class Missile {

    // We need to keep track of a Body and a radius
    Body body;
    color col;
    
    Player1 faction;
    Player1 ennemi;

    float size = 3;
    int MAX_LIFETIME = 6000;
    int creationTime;

    Missile(Player1 faction, Player1 ennemi, PVector startPos){
	this.faction = faction;
	this.ennemi = ennemi;
	creationTime = millis();

	// // This function puts the particle in the Box2d world
	makeBody(startPos.x, startPos.y, size);
	body.setUserData(this);
	col = color(175);
    }

    // This function removes the particle from the box2d world
    void killBody() {
	box2d.destroyBody(body);
    }
    
    // Change color when hit
    void change() {
	col = color(255, 0, 0);
    }
    
    void setGoal(PVector goal, float speed){
	Vec2 pos = box2d.getBodyPixelCoord(body);
	Vec2 dir = new Vec2(goal.x - pos.x, goal.y - pos.y);
	dir.normalize();
	dir.mulLocal(speed);

	// Y axis is Inverted in Processing.
	dir.y = -dir.y;
	body.applyForceToCenter(dir);
    }

    void update(){
	setGoal(ennemi.getTargetLocation(), 5);
    }

    void setDirection(PVector direction){
	body.applyForceToCenter(new Vec2(direction.x, direction.y));
    }

  // Is the particle ready for deletion?
  boolean done(PVector destination) {
    // Let's find the screen position of the particle
    Vec2 pos = box2d.getBodyPixelCoord(body);

    PVector p = new PVector(pos.x, pos.y);
    float distance = p.dist(destination);
    
    if(distance < 30){
	killBody();
	return true;
    }


    if(isTooOld()){
	println("Removing old missile");
	return true;
    }
    return false;

  }

    boolean isTooOld(){
	return creationTime + MAX_LIFETIME < millis();
    }


  // 
  void display(PGraphicsOpenGL g) {
    // We look at each body and get its screen position
    Vec2 pos = box2d.getBodyPixelCoord(body);
    // Get its angle of rotation
    float a = body.getAngle();
    g.pushMatrix();
    g.translate(pos.x, pos.y);
    //    g.rotate(a);
    g.fill(col);
    //    g.stroke(0);
    //    g.strokeWeight(1);
    g.ellipse(0, 0, size*2, size*2);
    // Let's add a line so we can see the rotation
    g.popMatrix();
  }

  // Here's our function that adds the particle to the Box2D world
  void makeBody(float x, float y, float r) {
    // Define a body
    BodyDef bd = new BodyDef();
    // Set its position
    bd.position = box2d.coordPixelsToWorld(x, y);
    bd.type = BodyType.DYNAMIC;
    body = box2d.createBody(bd);

    // Make the body's shape a circle
    CircleShape cs = new CircleShape();
    cs.m_radius = box2d.scalarPixelsToWorld(r);

    FixtureDef fd = new FixtureDef();
    fd.shape = cs;
    // Parameters that affect physics
    fd.density = 1;
    fd.friction = 0.01;
    fd.restitution = 0.3;

    // Attach fixture to body
    body.createFixture(fd);

    body.setAngularVelocity(random(-10, 10));
  }
}
