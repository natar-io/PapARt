public class Box{
  private float x, y, w, h;
  private int fill_c, stroke_c;
  private Body body;
  
  Box(float x_, float y_, float w_, float h_, int f_, int s_){
    x = x_;
    y = y_;
    w = w_;
    h = h_;
    fill_c = f_;
    stroke_c = s_;

    BodyDef bd = new BodyDef();
    bd.type = BodyType.DYNAMIC;
    bd.position.set(box2d.coordPixelsToWorld(x,y));
    //bd.bullet = true;

    PolygonShape ps = new PolygonShape();
    float box2dW = box2d.scalarPixelsToWorld(w/2);
    float box2dH = box2d.scalarPixelsToWorld(h/2);
    ps.setAsBox(box2dW, box2dH);
    
    FixtureDef fd = new FixtureDef();
    fd.shape = ps;
    fd.density = 10;
    fd.friction = 0.3;
    fd.restitution = 0.25;
    body = box2d.createBody(bd);
    body.createFixture(fd);
  }
  
  
  void applyForce(Vec2 force) {
    body.applyForce(force, body.getWorldCenter());
  }
  
    
  void display(){
    fill(fill_c);
    strokeWeight(1);
    stroke(stroke_c);
    rectMode(CENTER);
    Vec2 pos = box2d.getBodyPixelCoord(body);
    float a = body.getAngle();
    pushMatrix();
    translate(pos.x,pos.y);
    rotate(-a);
    rect(0,0,w,h);
    popMatrix();
  }  
}