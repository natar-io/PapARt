class Castle {

    float size = 55 /2f;
    float castleX = 120 + size;
    float castleY = 20 + size;

    PVector pos;

    // We need to keep track of a Body and a radius
    Body body;
    color col;
    
    Player1 faction;

    Castle(Player1 faction){
	this.faction = faction;

	col = faction.playerColor;
	pos = faction.gameCoord(new PVector(castleX, castleY));

	makeBody(pos.x, pos.y, size);
	body.setUserData(this);
    }

    // This function removes the particle from the box2d world
    void killBody() {
	box2d.destroyBody(body);
    }

    public PVector getPos(){
	return pos;
    }

    public void update(){
	pos = faction.gameCoord(new PVector(castleX, castleY));
	Vec2 vPhys = box2d.coordPixelsToWorld(pos.x, pos.y);
	body.setTransform(vPhys, 0);
    }
    
    float hp = 100;
    public void isHit(){
	hp -= 1;
    }

    // public float getCastleSize(){
    // 	float s = hp / 100 * castleSize;
    // 	if(s < 10) 
    // 	    s = 10;
    // 	return s;
    // }

  void display(PGraphicsOpenGL g) {
    // We look at each body and get its screen position
    Vec2 pos = box2d.getBodyPixelCoord(body);

    g.pushMatrix();
    g.translate(pos.x, pos.y);
    //    g.rotate(a);
    g.fill(col);
    g.ellipse(0, 0, size*2, size*2);
    g.popMatrix();
  }

  // Here's our function that adds the particle to the Box2D world
  void makeBody(float x, float y, float r) {
    // Define a body
    BodyDef bd = new BodyDef();
    // Set its position
    bd.position = box2d.coordPixelsToWorld(x, y);
    bd.type = BodyType.STATIC;
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
  }
}
