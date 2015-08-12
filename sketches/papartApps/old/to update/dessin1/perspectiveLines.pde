
import toxi.geom.*;

// TODO: snapping with EVERY POINT !!!

int PERSPECTIVE_LINES_MODE = 12111;


int ONE_POINT_PERSP = 5;
int TWO_POINT_PERSP = 6;

int perspGlobalMode = TWO_POINT_PERSP;


int EDIT_POS = 1;
int EDIT_DL = 10;
int EDIT_DR = 11;
int EDIT_DUP = 12;

int EDIT_HORIZON = 0;
int EDIT_VP_LEFT = 4;
int EDIT_VP_RIGHT = 5;
int EDIT_VP_WEIGHT = 6;
int EDIT_VP_INTENS = 7;

// Secondary mode
int PERSP_GLOBAL_EDIT = 1;
int PERSP_SINGLE_EDIT = 2;


// EDIT = xxxx

int perspMode = MODE_NONE;
int perspSecondMode = PERSP_SINGLE_EDIT;

/////// Line activeLine = null;
/////// Line lastLine = null;

float horizon = 0.8 * drawSize.y;
float vanishingPointLeft = 0.3 * drawSize.x;
float vanishingPointRight = 0.7 * drawSize.x;


Vec2D horizonLeft = new Vec2D();
Vec2D horizonRight = new Vec2D();


PImage horizonUp, lineVanish, vanishLeft, vanishRight, vanishChoose, horizonOn, horizonOff;
PImage perspLeft, perspRight, perspUp; 

ArrayList<PerspectiveLine> perspLines;

int perspIntensity = 200;
int perspWeight = 2;
boolean isHorizonOn = false;

void initPerspLinesMode(){
    perspLines = new ArrayList<PerspectiveLine>();

    horizonUp = loadImage(sketchPath + "/images/horizon-up.png");
    lineVanish = loadImage(sketchPath + "/images/line-vanish.png");
    vanishLeft = loadImage(sketchPath + "/images/vanish-l.png");
    vanishRight = loadImage(sketchPath + "/images/vanish-r.png");
    vanishChoose = loadImage(sketchPath + "/images/vanish-lr.png");
    horizonOn = loadImage(sketchPath + "/images/horizonOn.png");
    horizonOff = loadImage(sketchPath + "/images/horizonOff.png");
    perspLeft = loadImage(sketchPath + "/images/persp-l.png");
    perspRight = loadImage(sketchPath + "/images/persp-r.png");
    perspUp = loadImage(sketchPath + "/images/persp-up.png");
}

void enterPersLinesMode(){
    backToEditPersp();
    perspSecondMode = PERSP_SINGLE_EDIT;
    setActiveLine(null);
    isHorizonOn = true;
    resetFullNumPad();
}

void leavePersLinesMode(){
    setActiveLine(null);
    lastLine = null;
    isHorizonOn = false;
    setNoActiveKey();
}


void updateVanishingPoints(){
    horizonLeft.y = horizon;
    horizonRight.y = horizon;
    
    horizonLeft.x = vanishingPointLeft;
    horizonRight.x = vanishingPointRight;
}

void createPerspLine(){
    PerspectiveLine pl = new PerspectiveLine();
    perspLines.add(pl);
    setActiveLine(pl);
    perspMode = EDIT;
}

void backToEditPersp(){
    perspMode = EDIT;
    setNoActiveKey();
}

void keyPerspLines(){
    
    keyAllLines();

    // Switch from one mode to the other.
    if(key == '7'){

	resetFullNumPad();

	if(perspSecondMode == PERSP_SINGLE_EDIT){
	    perspSecondMode = PERSP_GLOBAL_EDIT;
	    setAllActiveKeys(true);
	}
        else {
	    perspSecondMode = PERSP_SINGLE_EDIT;
	    setActiveLine(activeLine);
	}


    }

    if(perspSecondMode == PERSP_SINGLE_EDIT){
	keysPerspSingle();
    }

    if(perspSecondMode == PERSP_GLOBAL_EDIT){
	keysPerspGlobal();
    }


    if(activeLine == null && perspMode == EDIT_POS){
	backToEditPersp();	
    }


   if(keyCode == ENTER){
       lastLine = activeLine;
       setActiveLine(null);
       perspMode = MODE_NONE;
   }


}

void keysPerspSingle(){

    if(key == '0'){
	createPerspLine();
    }

    if(key == '.'){
	if(activeLine != null && perspLines.size() > 0) 
	    perspLines.remove(activeLine);
	setActiveLine(null);
    }


    if((perspMode == EDIT || perspMode == MODE_NONE) && !perspLines.isEmpty()){
       if(key == '+'){
	   int id = (perspLines.indexOf(activeLine) + 1) % perspLines.size();
	   setActiveLine(perspLines.get(id));
       }
       
       if(key == '-'){
	   int id = (perspLines.indexOf(activeLine) - 1) % perspLines.size();
	   if(id < 0)
	       id = perspLines.size() - 1;
	   setActiveLine(perspLines.get(id));
       }
   }


    if(key == '1'){
	if(perspMode == EDIT_POS) {
	    backToEditPersp();
	}else{
	    perspMode = EDIT_POS;
	    int[] keys = {0, 8, 10};
	    setActiveKeys(keys);   
	}
    }

    if(key == '2'){
	if(perspMode == EDIT_DL) {
	    backToEditPersp();
	}else{
	    perspMode = EDIT_DL;
	    setActiveKey(1);
	}
    }

    if(key == '3'){
	if(perspMode == EDIT_DR) {
	    backToEditPersp();
	}else{
	    perspMode = EDIT_DR;
	    setActiveKey(2);
	}
    }

    if(key == '4'){
	if(perspMode == EDIT_DUP) {
	    backToEditPersp();
	}else{
	    perspMode = EDIT_DUP;
	    setActiveKey(3);
	}
    }

}


void keysPerspGlobal(){

   if(key == '2'){
   	if(perspMode == EDIT_VP_WEIGHT)
	    setNoMode();
	else{
	    setActiveKey(1);
	    perspMode = EDIT_VP_WEIGHT;
	}
   }


   if(key == '3'){
   	if(perspMode == EDIT_VP_INTENS)
	    setNoMode();
	else{
	    setActiveKey(2);
	    perspMode = EDIT_VP_INTENS;
	}
   }


    if(key == '4'){
	if(perspMode == EDIT_HORIZON) {
	    backToEditPersp();
	}else{
	    perspMode = EDIT_HORIZON;
	    setActiveKey(3);
	    isHorizonOn = true;
	}
    }

   if(key == '5'){

	if(perspMode == EDIT_VP_LEFT) {
	    backToEditPersp();
	}else{
	    perspMode = EDIT_VP_LEFT;
	    	    setActiveKey(4);
	}
	
    }

   if(key == '6'){
	if(perspMode == EDIT_VP_RIGHT) {
	    backToEditPersp();
	}else{
	    perspMode = EDIT_VP_RIGHT;
	    setActiveKey(5);
	}
    }

   if(key == '1'){
       if(perspGlobalMode == ONE_POINT_PERSP){
   	   perspGlobalMode = TWO_POINT_PERSP;
       }
       else
   	   perspGlobalMode = ONE_POINT_PERSP;
    }



    if(key == '8'){
	isHorizonOn = !isHorizonOn;
    }


   if(perspMode == EDIT_VP_WEIGHT){
       if(key == '+'){
	   perspWeight ++;
       }

       if(key == '-'){
	   perspWeight --;
       }
       perspWeight = constrain(perspWeight, 1, 10);
   }

   if(perspMode == EDIT_VP_INTENS){
       if(key == '+'){
	   perspIntensity += 10;
       }

       if(key == '-'){
	   perspIntensity -= 10;
       }
       perspIntensity = constrain(perspIntensity, 1, 255);

   }


}


void touchPerspLines(TouchElement te){
    
    if(te.speed2D.size() >= 1){

	PVector p0 = te.speed2D.get(0);
	
	if(perspMode == EDIT_HORIZON){
	    horizon += p0.y * drawSize.y;

	    if(horizon > drawSize.y)
		horizon = drawSize.y;
	    if(horizon < 0)
		horizon = 0;
	}

	if(perspMode == EDIT_VP_LEFT){
	    vanishingPointLeft += p0.x * drawSize.x;
	}

	if(perspMode == EDIT_VP_RIGHT){
	    vanishingPointRight += p0.x * drawSize.x;
	}

	if(activeLine != null){

	    if(perspMode == EDIT_DL){
		((PerspectiveLine)activeLine).moveLeftDist(-p0.x);
	    }

	    if(perspMode == EDIT_DR){
		((PerspectiveLine)activeLine).moveRightDist(p0.x);
	    }

	    if(perspMode == EDIT_DUP){
		((PerspectiveLine)activeLine).moveUpDist(p0.y * drawSize.y / 5f);
	    }
	}

    }


    if(activeLine != null && perspMode == EDIT_POS){
	
	if(!isIndirectOn && te.position2D.size() >= 1){
	    PVector p0 = te.position2D.get(0);
	    p0 = PVector.mult(p0, drawSize);
	    p0.y += yTouchOffset;

	    activeLine.moveLeftTo(p0);
	}

	if(isIndirectOn && te.speed2D.size() >= 1){
	    PVector p0 = te.speed2D.get(0);
	    activeLine.moveLeftBy(PVector.mult(p0, drawSize));
	}
    }

}


void drawPerspLines(GLGraphicsOffScreen g){

    if(globalMode == PERSPECTIVE_LINES_MODE){

	if(perspSecondMode == PERSP_SINGLE_EDIT){
	    numPadImages[6] = vanishLeft;
	    numPadImages[0] = lineVanish;
	    numPadImages[1] = perspLeft;
	    numPadImages[2] = perspRight;
	    numPadImages[3] = perspUp;

	    numPadLines();
	}

	if(perspSecondMode == PERSP_GLOBAL_EDIT){
	    numPadImages[6] = lineVanish;

	    numPadImages[0] = vanishChoose;
	    numPadImages[1] = lineWeight;
	    numPadImages[2] = intensImg;
	    
	    if(isHorizonOn)
		numPadImages[7] = horizonOn;
	    else
		numPadImages[7] = horizonOff;
	    
	    numPadImages[3] = horizonUp;
	    numPadImages[4] = vanishLeft;
	    numPadImages[5] = vanishRight;

	}
    }


    updateVanishingPoints();


    g.stroke(255);

    if(isHorizonOn){
	g.strokeWeight(2);
	
	// Draw the horizon
	g.line(0,
	       horizon,
	       drawSize.x, 
	       horizon);
    }


    if(perspMode == EDIT_VP_LEFT){
	g.ellipse(vanishingPointLeft, horizon, 10, 10);
    }

    if(perspMode == EDIT_VP_RIGHT){
	g.ellipse(vanishingPointRight, horizon, 10, 10);
    }

    
    for(PerspectiveLine l : perspLines){
	l.drawSelf(g, drawSize);
    }

}



class PerspectiveLine extends Line{

    // Distance in % 
    float dL = 0.3f;  
    float dR = 0.3f;  

    // distance in mm.
    float dUp = 20f;  

    boolean isRect = false;
    boolean isCube = false;

    Vec2D point = new Vec2D();
    Vec2D pointL = new Vec2D();
    Vec2D pointR = new Vec2D();

    Vec2D intersectLine;
    Line2D lineR, lineL; 

    // for intersections
    Line2D lineRL, lineLR; 

    PerspectiveLine() {
	super();
	lineL = new Line2D(point, horizonLeft);
	lineR = new Line2D(point, horizonRight);

	lineLR = new Line2D(pointL, horizonRight);
	lineRL = new Line2D(pointR, horizonLeft);
    }

    void moveLeftTo(PVector p){
	l.set(checkSnap(p));
    }

    void moveLeftDist(float value){
	dL += value;
	if(dL < 0)
	    dL = 0;
	checkRect();
    }

    void moveRightDist(float value){
	dR += value;
	if(dR < 0)
	    dR = 0;
	checkRect();
    }


    void moveUpDist(float value){
	// checkCube !
	dUp += value;
	checkRect();
	checkCube();
    }

    void checkRect(){
	isRect = true;
    }

    void checkCube(){
	isCube = true;
    }

    void updateRect(){
	pointL.x = lerp(l.x, horizonLeft.x, dL);
	pointL.y = lerp(l.y, horizonLeft.y, dL);

	pointR.x = lerp(l.x, horizonRight.x, dR);
	pointR.y = lerp(l.y, horizonRight.y, dR);
	
	Line2D.LineIntersection inter = lineRL.intersectLine(lineLR);
	if(inter == null || inter.getType() == Line2D.LineIntersection.Type.NON_INTERSECTING )
	    intersectLine = null;  
	else
	    intersectLine = inter.getPos();
    }


    void updatePoint(){
	point.x = l.x;
	point.y = l.y;

	if(dL == 0 || dR == 0)
	    isRect = false;
    }


  void drawSelf(GLGraphicsOffScreen g, PVector scales){

      updatePoint();

      isActive = activeLine == this;

	// if rectangle in perspective

	// CHECK THIS SOMEWHERE
	//	if(perspGlobalMode == TWO_POINT_PERSP);

      g.strokeWeight(perspWeight);
    
      if(isRect){
	  
	  updateRect();

	  if(isActive){
	      if(perspMode == EDIT_DL){
		  g.pushStyle();
		  g.noFill();
		  g.stroke(255, 255, 20);
		  g.ellipse(pointL.x, pointL.y, 9, 9);
		  g.popStyle();
		}
	      if(perspMode == EDIT_DR){
		  g.pushStyle();
		  g.noFill();
		  g.stroke(255, 255, 20);
		  g.ellipse(pointR.x, pointR.y, 9, 9);
		  g.popStyle();
	      }
	    }

	  // perspective grey line
	  g.stroke(perspIntensity / 2);
	  
	  g.line(l.x, l.y,
		 horizonLeft.x, horizonLeft.y);
	  g.line(pointL.x, pointL.y, 
		 horizonRight.x, horizonRight.y);
	  g.line(l.x, l.y,
		 horizonRight.x, horizonRight.y);
	  g.line(pointR.x, pointR.y, 
		 horizonLeft.x, horizonLeft.y);
	  
	  
	  if(isActive)
	      g.stroke(193, 160, 23);
	  else 
	      g.stroke(perspIntensity);
	  
	  g.line(l.x, l.y,
		 pointL.x, pointL.y); 
	  
	  g.line(l.x, l.y,
		 pointR.x, pointR.y);

	  if(intersectLine != null){
	      g.line(pointL.x, pointL.y, 
		     intersectLine.x, intersectLine.y);
	      g.line(pointR.x, pointR.y, 
		     intersectLine.x, intersectLine.y);
	  }
	  

	  if(isCube){

	      g.line(l.x, l.y,
		     l.x, l.y + dUp);


	  }



	}
	
	else{

	    if(isActive){

		g.pushStyle();
		g.noFill();
		g.stroke(255, 255, 20);
		g.ellipse(l.x, l.y, 9, 9);
		if(perspMode == EDIT_POS)
		    g.ellipse(l.x, l.y, 18, 18);
		g.popStyle();
		
		g.stroke(193, 160, 23);
	    }else{
		g.stroke(perspIntensity);
	    }
	    
	    g.line(l.x, 
		   l.y,
		   horizonLeft.x , 
		   horizonLeft.y);
	    
	    if(perspGlobalMode == TWO_POINT_PERSP){
		
		g.line(l.x, 
		       l.y,
		       horizonRight.x , 
		       horizonRight.y);
	    }

	    
	}
	
	
  }
    
}


