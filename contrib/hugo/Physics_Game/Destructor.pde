public class Destructor {
  private float x, y, w, h;
  private float offset;
  private int fill_c, stroke_c;
  private Body body;

  Destructor(float x_, float y_, float w_, float h_, int f_, int s_) {
    x = x_;
    y = y_;
    w = w_;
    h = h_;
    offset = y_;
    fill_c = f_;
    stroke_c = s_;

    BodyDef bd = new BodyDef();
    bd.position.set(box2d.coordPixelsToWorld(x, y)); 
    bd.type = BodyType.KINEMATIC;
    body = box2d.createBody(bd);

    PolygonShape sd = new PolygonShape();
    float box2dW = box2d.scalarPixelsToWorld(w/2);
    float box2dH = box2d.scalarPixelsToWorld(h/2);
    sd.setAsBox(box2dW, box2dH);
    body.createFixture(sd, 1);
    body.setUserData(this);
  }

  void move(float x, float y) {
    this.x = x;
    this.y = y;
    box2d.destroyBody(body);
    BodyDef bd = new BodyDef();
    bd.position = box2d.coordPixelsToWorld(x, y);
    bd.type = BodyType.STATIC;
    body = box2d.createBody(bd);

    PolygonShape sd = new PolygonShape();
    float box2dW = box2d.scalarPixelsToWorld(w/2);
    float box2dH = box2d.scalarPixelsToWorld(h/2);
    sd.setAsBox(box2dW, box2dH);
    body.createFixture(sd, 1);
    body.setUserData(this);
  }

  PVector getXY() {
    PVector xy = new PVector(x, y);
    return xy;
  }

  PVector getWH() {
    PVector wh = new PVector(w, h);
    return wh;
  }

  float getOff() {
    return offset;
  }
}