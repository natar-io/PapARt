class Surface {
  private ArrayList<Vec2> points;
  private ChainShape chain;
  private Vec2[] vertices;
  private Body body;
  private Boolean isAlive;
  int touchID;
  
  Surface(int ID) {
    points  = new ArrayList<Vec2>();
    chain   = new ChainShape();
    isAlive = false;
    touchID = ID;
  }

  void addPoint(float x, float y){
    if(!checkClose(x,y)){
      points.add(new Vec2(x,y));
      vertices = new Vec2[points.size()];
      for(int i = 0; i < vertices.length; i++) {
        vertices[i] = box2d.coordPixelsToWorld(points.get(i));
      }
    }
  }
  
  // We don't add vertices that are too close
  boolean checkClose(float x, float y){
   boolean tooClose = false;
   if(points.size()>0){
     float dx = x - points.get(points.size()-1).x;
     float dy = y - points.get(points.size()-1).y;
     if(sqrt(dx*dx+dy*dy)<5 || sqrt(dx*dx+dy*dy)>300){
       tooClose = true;
      }
   }
   return tooClose;
  }
  
  void updateChain(){
    if(points.size()>1){
      if(body!=null){
        box2d.destroyBody(body);
      }
      chain = new ChainShape();
      chain.createChain(vertices, vertices.length);
      BodyDef bd = new BodyDef();
      bd.type = BodyType.STATIC;
      body = box2d.world.createBody(bd);
      body.createFixture(chain, 1);
      isAlive = true;
    } 
  }
  void kill(){
     box2d.destroyBody(body);
     BodyDef bd = new BodyDef();
     bd.type = BodyType.KINEMATIC;
     body = box2d.world.createBody(bd);
     isAlive = false;
  }
  
  void display() {
    strokeWeight(3);
    stroke(255,120,120);
    noFill();
    beginShape();
    for (Vec2 v: points) {
      vertex(v.x,v.y);
    }
    endShape();
  }
  
  int getID(){
   return touchID; 
  }
  
  boolean isAlive(){
    return isAlive; 
  }
}