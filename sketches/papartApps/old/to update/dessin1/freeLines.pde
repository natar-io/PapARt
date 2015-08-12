

int FREE_LINES_MODE = 2311;

// MODE_NONE is accessible for all modes.
int lineMode = MODE_NONE;

int CREATE_LINE = 0;
int EDIT_LEFT = 1;
int EDIT_RIGHT = 2;

int EDIT_TAN_LEFT = 4;
int EDIT_TAN_RIGHT = 5;

int EDIT_WEIGHT = 3;
// EDIT = ...

PImage lineL, lineR, lineSel, lineWeight, lineSnap, lineSnapOff, indirectImg, directImg;

ArrayList<Line> lines;
Line activeLine = null;
Line lastLine = null;

boolean isLineSnapOn = false;
boolean isIndirectOn = false;

float yTouchOffset = 20f;

void initFreeLinesMode(){

    lineL = loadImage(sketchPath + "/images/line-l.png");
    lineR = loadImage(sketchPath + "/images/line-r.png");
    lineSel = loadImage(sketchPath + "/images/line-s.png");
    lineWeight = loadImage(sketchPath + "/images/line-w.png");
    lineSnap = loadImage(sketchPath + "/images/line-snap.png");
    lineSnapOff = loadImage(sketchPath + "/images/line-snap2.png");
    indirectImg = loadImage(sketchPath + "/images/indirect.png");
    directImg = loadImage(sketchPath + "/images/direct.png");
    lines = new ArrayList<Line>();

}

void enterFreeLinesMode(){
    resetFullNumPad();
    setActiveLine(null);
}

void leaveFreeLinesMode(){
    setActiveLine(null);
    lastLine = null;
    setNoActiveKey();
}


void exitSelection(){
    lineMode = MODE_NONE;
    setNoActiveKey();
    lastLine = activeLine;

    setActiveLine(null);
    
}

void setActiveLine(Line l){
    int[] keys = {0, 1, 2, 3, 4, 8, 10};

    if(l == null)
	setNotActiveKeys(keys);
    else
	setActiveKeys(keys);

    activeLine = l;
}

void backToEditLine(){
    setNoActiveKey();
    lineMode = EDIT;
}

void keyAllLines(){

    if(key == '*'){
	isLineSnapOn = !isLineSnapOn;
    }

    keyIndirect();
}


void keyIndirect(){
   if(key == '9'){
	isIndirectOn = !isIndirectOn;
    }
}

void keyFreeLines(){

    keyAllLines();

    if(key == '0'){
	createLine();
	lineMode = EDIT;
    }

    if(key == '.'){
	if(activeLine != null && lines.size() > 0) 
	    lines.remove(activeLine);
	setActiveLine(null);
    }
    

    if((lineMode == EDIT || lineMode == MODE_NONE) && !lines.isEmpty()){
	if(key == '+'){
	    int id = (lines.indexOf(activeLine) + 1) % lines.size();
	    setActiveLine(lines.get(id));
	}

	if(key == '-'){
	    int id = (lines.indexOf(activeLine) - 1) % lines.size();
	    if(id < 0)
		id = lines.size() - 1;
	    setActiveLine(lines.get(id));
	}
    }


    if(keyCode == ENTER){
	exitSelection();
    }

    ////////// The next buttons require an active line ////////

    if(activeLine == null)
	return;


    if(key == '1'){
	if(lineMode == EDIT_LEFT)
	    backToEditLine();
	else{
	    lineMode = EDIT_LEFT;
	    setActiveKey(0);
	}
    }

    if(key == '2')
	if(lineMode == EDIT_RIGHT)
	    backToEditLine();

	else{
	    lineMode = EDIT_RIGHT;
	    setActiveKey(1);
	}

    if(key == '3'){
	if(lineMode == EDIT_WEIGHT)
	    backToEditLine();
	else{
	    lineMode = EDIT_WEIGHT;
	    setActiveKey(2);
	}
    }

    if(key == '4'){
	if(lineMode == EDIT_TAN_LEFT)
	    backToEditLine();
	else{
	    lineMode = EDIT_TAN_LEFT;
	    setActiveKey(3);
	}
    }

    if(key == '5'){
	if(lineMode == EDIT_TAN_RIGHT)
	    backToEditLine();
	else{
	    lineMode = EDIT_TAN_RIGHT;
	    setActiveKey(4);
	}
    }

    if(lineMode == EDIT_WEIGHT){

	if(activeLine != null){
	    if(key == '+'){
		activeLine.w++;
	    }
	   
	    if(key == '-'){
		activeLine.w--;
		if(activeLine.w <= 0)
		    activeLine.w = 1;
	    }
	}
    }


}

void createLine(){
    Line l = new Line();
    setActiveLine(l);
    lines.add(l);
}



void numPadLines(){

    if(isLineSnapOn)
	numPadImages[10] = lineSnap;
    else
	numPadImages[10] = lineSnapOff;

    numPadIndirect();
}

void numPadIndirect(){

   if(isIndirectOn)
	numPadImages[8] = indirectImg;
    else
	numPadImages[8] = directImg;
}


void touchFreeLines(TouchElement te){

    if(activeLine != null){
	if(!isIndirectOn && te.position2D.size() >= 1){
	    
	    PVector p0 = te.position2D.get(0);
	    p0 = PVector.mult(p0, drawSize);
	    p0.y += yTouchOffset;

	    if(lineMode == EDIT_LEFT){
		activeLine.moveLeftTo(p0);
	    }
	    
	    if(lineMode == EDIT_RIGHT){
		activeLine.moveRightTo(p0);
	    }

	    if(lineMode == EDIT_TAN_LEFT){
		activeLine.moveTanLeftTo(p0);
	    }
	    
	    if(lineMode == EDIT_TAN_RIGHT){
		activeLine.moveTanRightTo(p0);
	    }

	}


	if(isIndirectOn && te.speed2D.size() >= 1){
	    
	    PVector p0 = te.speed2D.get(0);

	    if(lineMode == EDIT_LEFT)
		activeLine.moveLeftBy(PVector.mult(p0, drawSize));
	    
	    if(lineMode == EDIT_RIGHT)
		activeLine.moveRightBy(PVector.mult(p0, drawSize));

	    if(lineMode == EDIT_TAN_LEFT)
		activeLine.moveTanLeftBy(PVector.mult(p0, drawSize));
	    
	    if(lineMode == EDIT_TAN_RIGHT)
		activeLine.moveTanRightBy(PVector.mult(p0, drawSize));

	}

    }

}

void drawFreeLines(GLGraphicsOffScreen g){

    if(globalMode == FREE_LINES_MODE){
	numPadImages[0] = lineL;
	numPadImages[1] = lineR;
	numPadImages[2] = lineWeight;

	//	numPadImages[3] = tanLImg;
	//	numPadImages[4] = tanRImg;
	numPadLines();
    }

    // // Draw inside the virtual screen.

    g.strokeWeight(2);
    g.stroke(255);

    g.noFill();


    for(Line l : lines){
	l.drawSelf(g);
    }


}


float snapDist = 5;  // 1mm
float indirectPrecisionFactor = 1f/4f;

class Line {

    // TODO: id, undoable etc...

    PVector l;
    PVector r;
    boolean isActive;

    PVector tanL, tanR;

    int w = 2;

    Line(){
	l = new PVector(0.4 * drawSize.x, 0.5 * drawSize.y);
	r = new PVector(0.6 * drawSize.x, 0.5 * drawSize.y);

	tanL = l;
	tanR = r;
    }
    
    void moveLeftTo(PVector p){
	l.set(checkSnap(p));
    }

    void moveRightTo(PVector p){
	r.set(checkSnap(p));
    }

    void moveLeftBy(PVector p){
	p.mult(indirectPrecisionFactor);
	PVector l2 = PVector.add(p, l);
	l.set(checkSnap(l2));
    }

    void moveRightBy(PVector p){
	p.mult(indirectPrecisionFactor);
	PVector r2 = PVector.add(p, r);
	r.set(checkSnap(r2));
    }

    void checkTanActive(){
	if(tanL == l){
	    tanL = new PVector(l.x, l.y);
	    tanR = new PVector(r.x, r.y);
	}

    }

    void deleteTan(){
	tanL = l;
	tanR = r;
    }

    void moveTanLeftTo(PVector p){
	checkTanActive();
	tanL.set(checkSnap(p));
    }

    void moveTanRightTo(PVector p){
	checkTanActive();
	tanR.set(checkSnap(p));
    }


    void moveTanLeftBy(PVector p){
	checkTanActive();
	p.mult(indirectPrecisionFactor);
	PVector l2 = PVector.add(p, tanL);
	tanL.set(checkSnap(l2));
    }

    void moveTanRightBy(PVector p){
	checkTanActive();
	p.mult(indirectPrecisionFactor);
	PVector r2 = PVector.add(p, tanR);
	tanR.set(checkSnap(r2));
    }


    PVector checkSnap(PVector p){
	if(!isLineSnapOn){
	    return p;
	}
	// // check all lines...

	for(Line line : lines){
	    if(line == this)
		continue;

	    PVector close = isCloseTo(p, line);
	    if(close != null)
		return close;
	}

	// ALL LINES ??!!
	for(Line line : perspLines){
	    if(line == this)
		continue;

	    PVector close = isCloseTo(p, line);
	    if(close != null)
		return close;
	}


	return p;
    }
    
    PVector isCloseTo(PVector p, Line line){
	if(line.l.dist(p) < snapDist){
	    return line.l;
	}
	if(line.r.dist(p) < snapDist){
	    return line.r;
	}
	return null;
    }
    

    void drawSelf(GLGraphicsOffScreen g){

	isActive = activeLine == this;

	if(isActive){

	    g.pushStyle();
	    g.noFill();

	    g.stroke(255, 20, 20);
	    g.ellipse(l.x, l.y, 9, 9);

	    if(lineMode == EDIT_LEFT)
		g.ellipse(l.x, l.y, 18, 18);

	    g.stroke(20, 20, 255);
	    g.ellipse(r.x, r.y, 9, 9);
	    if(lineMode == EDIT_RIGHT)
		g.ellipse(r.x, r.y, 18, 18);
	    
	    g.popStyle();


	    if(lineMode == EDIT_TAN_LEFT ||
	       lineMode == EDIT_TAN_RIGHT){
		g.pushStyle();
		g.noFill();
		g.stroke(20, 255, 20);
		g.ellipse(tanL.x, tanL.y, 9, 9);

		g.stroke(20, 255, 255);
		g.ellipse(tanR.x, tanR.y, 9, 9);
		g.popStyle();
	    }

	    // if(lineMode == EDIT_TAN_RIGHT){
	    // 	g.pushStyle();
  	    // 	  g.noFill();
	    // 	  g.stroke(20, 255, 255);
	    // 	  g.ellipse(tanR.x, tanR.y, 9, 9);
	    // 	g.popStyle();
	    // }


	    g.stroke(193, 160, 23);
	}
	else
	    g.stroke(255);

	// Set the weight and draw the line.
	g.strokeWeight(w);

	// g.line(l.x, 
	//        l.y,
	//        r.x,
	//        r.y);

	g.bezier(l.x, l.y, 
		 tanL.x, tanL.y,
		 tanR.x, tanR.y,		 
		 r.x, r.y);


    }


}

