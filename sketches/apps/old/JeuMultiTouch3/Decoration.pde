class Decoration{

  float power;
  PVector pos;
  int creationTime;

  Decoration(int currentTime, PVector p, float power){
    creationTime = currentTime;
    this.pos = p;
    this.power = power;
    
  }

  void drawSelf(int currentTime){

  }

}
