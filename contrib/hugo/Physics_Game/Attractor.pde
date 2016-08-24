class Attractor {
  private Body body;
  private boolean isAlive;
  private float x,y;
  float r;

  Attractor(float r_, float x_, float y_) {
    r = r_;
    x = x_;
    y = y_;
    BodyDef bd = new BodyDef();
    bd.type = BodyType.KINEMATIC;
    bd.position = box2d.coordPixelsToWorld(x,y);
    body = box2d.world.createBody(bd);
    CircleShape cs = new CircleShape();
    cs.m_radius = box2d.scalarPixelsToWorld(r);
    body.createFixture(cs,1);
    isAlive = true;
  }

  Vec2 attract(Box b) {
    float G = 400; 
    Vec2 pos = body.getWorldCenter();    
    Vec2 moverPos = b.body.getWorldCenter();
    Vec2 force = pos.sub(moverPos);
    float distance = force.length();
    distance = constrain(distance,5,7);
    force.normalize();
    // Note the attractor's mass is 0 because it's fixed so can't use that
    float strength = (G * 1 * b.body.m_mass) / (distance * distance); // Calculate gravitional force magnitude
    force.mulLocal(strength);         // Get force vector --> magnitude * direction
    return force;
  }
  
  BodyDef move(float x, float y){
    this.x = x;
    this.y = y;
    BodyDef bd = new BodyDef();
    bd.position = box2d.coordPixelsToWorld(x,y);
    return bd;
  }
  
  void create(BodyDef bd){
    body = box2d.world.createBody(bd);   
    isAlive = true;
  }
  
  void kill(){
     box2d.destroyBody(body);
     isAlive = false;
  }
  boolean isAlive(){
   return isAlive; 
  }

  
  PVector getXY() {
    PVector xy = new PVector(x, y);
    return xy;
  }


}