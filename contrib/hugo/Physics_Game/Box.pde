public class Box {
  private float x, y, w, h;
  private int fill_c, stroke_c;
  private Body body;
  private boolean isAlive;


  Box(float x_, float y_, float w_, float h_, int f_, int s_) {
    x = x_;
    y = y_;
    w = w_;
    h = h_;
    fill_c = f_;
    stroke_c = s_;

    BodyDef bd = new BodyDef();
    bd.type = BodyType.DYNAMIC;
    bd.position.set(box2d.coordPixelsToWorld(x, y));
    PolygonShape ps = new PolygonShape();
    float box2dW = box2d.scalarPixelsToWorld(w/2);
    float box2dH = box2d.scalarPixelsToWorld(h/2);
    ps.setAsBox(box2dW, box2dH);

    FixtureDef fd = new FixtureDef();
    fd.shape = ps;
    fd.density = 10;
    fd.friction = 0.1;
    fd.restitution = 0.25;
    body = box2d.createBody(bd);
    body.setAngularVelocity(random(10)-5);
    body.createFixture(fd);
    body.setUserData(this);
    isAlive = true;
  }


  void applyForce(Vec2 force) {
    body.applyForce(force, body.getWorldCenter());
  }


  Vec2 getXY() {
    Vec2 xy = box2d.coordWorldToPixels(body.getPosition());
    return xy;
  }

  PVector getWH() {
    PVector wh = new PVector(w, h);
    return wh;
  } 

  void kill() {
    box2d.destroyBody(body);
    isAlive = false;
  }
}