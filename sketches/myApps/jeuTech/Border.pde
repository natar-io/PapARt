
class Border{

    // We need to keep track of a Body and a width and height
    Body body;
    float w;
    float h;
    
    // Constructor
    Border(float x, float y, int width, int height) {
	w = width;
	h = height;
	// Add the box to the box2d world
	makeBody(new Vec2(x, y), w, h);
	body.setUserData(this);
    }

    // This function removes the particle from the box2d world
    void killBody() {
	box2d.destroyBody(body);
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
	g.fill(175);
	g.noStroke();
	g.rect(0, 0, w, h);
	
	g.rectMode(CORNER);
	g.popMatrix();
    }
    
    // This function adds the rectangle to the box2d world
    void makeBody(Vec2 center, float w_, float h_) {
	
	// Define a polygon (this is what we use for a rectangle)
	PolygonShape sd = new PolygonShape();
	float box2dW = box2d.scalarPixelsToWorld(w_/2);
	float box2dH = box2d.scalarPixelsToWorld(h_/2);
	sd.setAsBox(box2dW, box2dH);
	
	// Define a fixture
	FixtureDef fd = new FixtureDef();
	fd.shape = sd;
	// Parameters that affect physics
	fd.density = 1;
	fd.friction = 0.3;
	fd.restitution = 0.5;
	
	// Define the body and make it from the shape
	BodyDef bd = new BodyDef();
	bd.type = BodyType.STATIC;
	bd.position.set(box2d.coordPixelsToWorld(center));
	
	body = box2d.createBody(bd);
	body.createFixture(fd);
	
    }
}
