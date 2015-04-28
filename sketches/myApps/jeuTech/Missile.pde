// The Nature of Code
// <http://www.shiffman.net/teaching/nature>
// Spring 2010
// Box2DProcessing example

// A circular particle


class Missile extends DyingObject{

    // We need to keep track of a Body and a radius
    Body body;
    color col;
    
    Player1 faction;
    Player1 ennemi;


    int level;

    Missile(Player1 faction, Player1 ennemi, PVector startPos, int level){
	super();
	lifeTime = 15000;
	this.level = level;

	this.faction = faction;
	this.ennemi = ennemi;

	// // This function puts the particle in the Box2d world
	makeBody(startPos.x, startPos.y, size);
	body.setUserData(this);
	col = #4BE0B2;
    }
    
    PVector creationPos;
    void setCreationPos(PVector v){
	this.creationPos = v;
    }

    // This function removes the particle from the box2d world
    void killBody() {
	box2d.destroyBody(body);
    }

    Vec2 getPhysicsPos(){
	return body.getWorldCenter();
    }

    PVector getScreenPos(){
	Vec2 pos = box2d.getBodyPixelCoord(body);
	return new PVector(pos.x , pos.y);
    }

    void applyForce(Vec2 force){
	body.applyForce(force, body.getWorldCenter());
    }

    void accelerate(float amplitude){
	Vec2 vel = body.getLinearVelocity();
	vel.mulLocal(amplitude);
	body.setLinearVelocity(vel);
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

    void setInvDirection(PVector invDirection, float speedMult){
	Vec2 pos = box2d.getBodyPixelCoord(body);
	Vec2 dir = new Vec2(pos.x - invDirection.x, pos.y - invDirection.y);
	dir.normalize();
	dir.mulLocal(speedMult);
	dir.y = -dir.y;
	body.applyForceToCenter(dir);
    }

    void update(){
	//	setGoal(ennemi.getTargetLocation(), 5);
    }
    void setDirection(PVector direction){
	body.applyForceToCenter(new Vec2(direction.x, direction.y));
    }

    // TODO: Explosion Animation. 
    boolean expolsion = false;
    void hit(){

	level =  level -1;
	if(level == 0)
	    expolsion = true;
    }

    boolean hasExploded(){
	return expolsion;
    }

    boolean isOutOfBounds(){
	return game.checkBounds(this.getScreenPos());
    }

    // Is the particle ready for deletion?
    boolean done() {
	if(isTooOld() || hasExploded() || isOutOfBounds()){
	    faction.nbMissiles--;
	    killBody();
	    return true;
	}
	return false;
    }

    void forceDeath(){
	faction.nbMissiles--;
	killBody();
    }


    // 
    void display(PGraphicsOpenGL g) {
	// We look at each body and get its screen position
	Vec2 pos = box2d.getBodyPixelCoord(body);
	// Get its angle of rotation
	float a = body.getAngle();
	g.pushMatrix();
	g.translate(pos.x, pos.y);
	g.rotate(-a);
	g.fill(col);
	g.noStroke();
	

	float physicsScale = box2d.scaleFactor;

	for(PVector v : look){
	    g.ellipse(v.x * physicsScale, v.y *physicsScale, size*2, size*2);
	}
	// g.translate(0, 0);
	// g.ellipse(0, 0, size*2, size*2);

	// g.translate(10, -10);
	// g.ellipse(0, 0, size*2, size*2);

	// Let's add a line so we can see the rotation
	g.popMatrix();
    }
    
    ArrayList<PVector> look = new ArrayList<PVector>();

    // Here's our function that adds the particle to the Box2D world
    void makeBody(float x, float y, float r) {
	// Define a body
	BodyDef bd = new BodyDef();
	// Set its position
	bd.position = box2d.coordPixelsToWorld(x, y);
	bd.type = BodyType.DYNAMIC;
	body = box2d.createBody(bd);

	if(level == 1){
	    missileLevel1();
	}
	if(level == 2){
	    missileLevel2();
	}
	if(level == 3){
	    missileLevel3();
	}
	if(level == 4){
	    missileLevel4();
	}

	body.setAngularVelocity(random(-5, 5));
    }


    float size = 4.5f;
    PVector middle= new PVector(0,0);
    PVector left= new PVector(-size / 10, 0);
    PVector right= new PVector(size / 10, 0);
    float bulletSizePx = size / 10;

    void missileLevel1(){
	PVector middle= new PVector(0,0);
	addCircle(middle);
    }

    void missileLevel2(){
	PVector left= new PVector(0, -bulletSizePx);
	PVector right= new PVector(0, bulletSizePx);
    	addCircle(left);
	addCircle(right);
    }

    void missileLevel3(){
	PVector middle= new PVector(0, bulletSizePx );
	PVector left= new PVector( bulletSizePx,  -bulletSizePx);
	PVector right= new PVector(-bulletSizePx, -bulletSizePx);
	addCircle(middle);
	addCircle(left);
	addCircle(right);
    }

    void missileLevel4(){

	for(int i = 0; i < 2; i++){
	    PVector rand = new PVector(random(- bulletSizePx * 2, bulletSizePx * 2), 
					random(- bulletSizePx *2, bulletSizePx * 2));
	    addCircle(rand);
	    rand.mult(-1);
	    addCircle(rand);
	}
    }

    void addCircle(PVector pos){

	look.add(pos.get());

	// Make the body's shape a circle
	CircleShape cs = new CircleShape();
	cs.m_radius = box2d.scalarPixelsToWorld(size);
	cs.m_p.x = pos.x;
	cs.m_p.y = pos.y;

	FixtureDef fd = new FixtureDef();
	fd.shape = cs;
	// Parameters that affect physics
	fd.density = density;
	fd.friction = friction;
	fd.restitution = restitution;

	// Attach fixture to body
	body.createFixture(fd);
    }

}
