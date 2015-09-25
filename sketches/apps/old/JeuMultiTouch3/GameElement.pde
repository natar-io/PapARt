int STATE_NORMAL = 0;
/* int STATE_SURVOLE1 = 1; */
/* int STATE_SURVOLE2 = 2; */
int STATE_TOUCHE1 = 1;
int STATE_TOUCHE2 = 2;
int STATE_DYING = 3;
int STATE_DEAD = 4;
int STATE_TODELETE = 5;

int TYPE_CUTE = 0;
int TYPE_BAD = 1;

class GameElement{

  PImage image;
  int state;
  int type;
  ArrayList<TouchPoint> touchPoints;
  int creationTime;

  static final int nbPos = 2;
  static final int movementDuration = 3000;
  static final int movementSpeed = 120;

  PVector position;
  PVector[] directions = new PVector[nbPos];
  int[] directionCreation = new int[nbPos];
  int lastAddedDirection;
  PVector size;
  float ratio;
  int currentPos = 0;
  boolean toDelete = false;

  float pet = 0;
  float totalPet;

  public GameElement(PImage image, int creationTime, int type){
    this.image = image;
    this.state = STATE_NORMAL;
    this.creationTime = creationTime;
    this.type = type;

    this.ratio = (float)image.width / (float)image.height;
    int s = (int)random(30) + 100;
    size = new PVector(s * ratio, s);

    totalPet = s / (1 *150f);  // < 0.25

    if(type == TYPE_BAD){
      totalPet *= 3;
    }

    /* this.position = new PVector(100 + (int) random(width - 100), */
    /* 				100 + (int) random(height - 100)); */
    this.position = new PVector();
    lastAddedDirection = creationTime;


    addPos(creationTime);
    addPos(creationTime);
  }

  void addPos(int currentTime){
    currentPos++;

    PVector prev =  directions[(currentPos) % nbPos];
    PVector next =  directions[(currentPos+1) % nbPos];

    if(prev != null && next != null){
      
      next.x = constrain(prev.x + random(-movementSpeed, movementSpeed),
			 100, width - 100);
      next.y = constrain(prev.y + random(-movementSpeed, movementSpeed),
			 100, height - 100);
	}

    else{

      directions[(currentPos+1) % nbPos] = 
	new PVector(10 + (int) random(width - 10),
		    10 + (int) random(height - 10));
    }

    directionCreation[(currentPos+1) % nbPos] = currentTime;

    lastAddedDirection = currentTime;
  }


  public void tick(int currentTime){

    if(directions[0] != null && directions[1] != null) {

	PVector d1 = directions[currentPos % nbPos];
	PVector d2 = directions[(currentPos+1) % nbPos];
	float a =	(currentTime - (float)directionCreation[(currentPos+1) % nbPos]) / movementDuration;
	
	position.x = lerp(d1.x, d2.x, a);
	position.y = lerp(d1.y, d2.y, a);
	
    }


	//    if(!allTP.isEmpty()){
	//      for(TouchPoint tp : allTP){

	  // 2D touch...
    if(type == TYPE_CUTE){

	ArrayList<TouchPoint> touchs2D = new ArrayList<TouchPoint>(touchInput.getTouchPoints2D());
	for(TouchPoint tp : touchs2D){
	    
	    PVector pos = tp.getPosition();
	    float vx = pos.x * width;
	    float vy = pos.y * height;
	    
	    if(vx > position.x - size.x &&
	       vx < position.x + size.x &&
	       vy > position.y - size.y &&
	       vy < position.y + size.y ){
		
		if(tp.getPreviousPosition() != null){
		    float dist = pos.dist(tp.getPreviousPosition());
		    if(dist > 2){
			pet += 0.005;
			if(debug)
			    ellipse(vx, vy, dist * 10000, dist * 10000);
		    }
		}
	    }


	    boolean isSound = false;
	    
	    if(state == STATE_NORMAL && 
	       pet >=  1f/3f * totalPet){
		state = STATE_TOUCHE1;
		isSound = true;
	    }
	    
	    if(state == STATE_TOUCHE1 &&
	       pet >=  2f/3f * totalPet){
		state = STATE_TOUCHE2;
		isSound = true; 
	    }
	    
	    if(state == STATE_TOUCHE2 &&
	       pet >= totalPet){
		state = STATE_DEAD;
		isSound = true;
		toDelete = true;
	    }
	    
	    if(isSound){
		cri();
	    }
	}
    }


    if(type == TYPE_BAD){

	ArrayList<TouchPoint> touchs3D = new ArrayList<TouchPoint>(touchInput.getTouchPoints3D());
	for(TouchPoint tp : touchs3D){

	    PVector pos = tp.getPosition();
	    float vx = pos.x * width;
	    float vy = pos.y * height;
	    
	    float uncertainX = size.x * 1.3;
	    float uncertainY = size.y * 1.3;

	    // if(vx > position.x - size.x &&
	    //    vx < position.x + size.x &&
	    //    vy > position.y - size.y &&
	    //    vy < position.y + size.y ){
	    if(vx > position.x - uncertainX &&
	       vx < position.x + uncertainX &&
	       vy > position.y - uncertainY &&
	       vy < position.y + uncertainY ){
		
		float dist = pos.dist(tp.getPreviousPosition());
		float dz = pos.z - tp.getPreviousPosition().z;
		
		println("dz " + dz);
		if(dz > 2){
		    
		    println("dist " + dist + " total " + totalPet);
		    pet += 0.03;
		    //			pet += dist;
		    if(debug) 
			ellipse(vx, vy, dist * 10000, dist * 10000);
		} 
	    }
	    
	    
	    boolean isSound = false;
	    
	    if(state == STATE_NORMAL && 
	       pet >=  1f/3f * totalPet){
		state = STATE_TOUCHE1;
		isSound = true;
	    }
	    
	    if(state == STATE_TOUCHE1 &&
	       pet >=  2f/3f * totalPet){
		state = STATE_TOUCHE2;
		isSound = true; 
	    }
	    
	    if(state == STATE_TOUCHE2 &&
	       pet >= totalPet){
		state = STATE_DEAD;
		isSound = true;
		toDelete = true;
	    }
	    
	    if(isSound){
		grognement();
	    }
	}
    }
    
    if(currentTime - lastAddedDirection  > movementDuration ){
	addPos(currentTime);
    }    
  }



  public void drawElement(int currentTime){


    pushStyle();

    if(type == TYPE_CUTE){
      fill(#25E35C);
      rect( position.x, position.y, 
	   size.x * (0.5f + 1.8f *pet / totalPet),
	   size.y * (0.5f + 1.8f *pet / totalPet));
    }    

    image(image, position.x, position.y, 
	  size.x * (0.5f + 1.2f *pet / totalPet),
	  size.y * (0.5f + 1.2f *pet / totalPet));

    if(type == TYPE_BAD){

      tint(200, 50, 50,  20 + (pet / totalPet) * 200 );
      image(image, position.x, position.y, 
	    size.x * (0.5f + 1.2f *pet / totalPet),
	    size.y * (0.5f + 1.2f *pet / totalPet));

    }
      popStyle();  

  }


}
