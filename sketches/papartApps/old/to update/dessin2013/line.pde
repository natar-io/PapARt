int startAnim = 0;

float snapDist = 5;  // 1mm
float indirectPrecisionFactor = 1f/4f;

abstract class Line {

    // TODO: id, undoable etc...
    boolean isActive;

    boolean isHidden = false;
    boolean isStandard = false;


    PVector[] snapPoints = null;


    int w = 2;


    Line(){

	// // TODO: deprecated
	// this.isStandard = true;
	// tanL = l;
	// tanR = r;
    }

    void hide(){
	this.isHidden = true;
    }

    void show(){
	this.isHidden = false;
    }


    boolean isStandard(){
    	return this.isStandard;
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
	    if(close != null){
		return close;
	    }
	}


	if(isGridOn && gridSnapPoints != null) {

	    println("grid + snap OK"); 

	    // for each snap points of the grid
	    for(PVector s : gridSnapPoints){

		println("dist " + s.dist(p));

		// check the snap... 
		if(s.dist(p) < snapDist){
		    return s;	

		}
	    }
	}




	return p;
    }


    PVector isCloseTo(PVector p, Line line){

	if(snapPoints != null)
	    for(PVector snapP : line.snapPoints)
		if(snapP.dist(p) < snapDist)
		    return snapP;
		
	return null;
    }

    abstract void drawSelf(GLGraphicsOffScreen g);

    abstract void setActive(PVector p);

}



class StandardLine extends Line{

    // For left and right segment
    PVector l;
    PVector r;

    public StandardLine(){

	this.isStandard = true;

	l = new PVector(0.4 * drawSize.x, 0.5 * drawSize.y);
	r = new PVector(0.6 * drawSize.x, 0.5 * drawSize.y);

	snapPoints = new PVector[2];
	snapPoints[0] = l;
	snapPoints[1] = r;

    }
    
    void setActive(PVector p){

	setStdLineMode();

	if(p == l)
	    lineMode = EDIT_LEFT;

	if(p == r)
	    lineMode = EDIT_RIGHT;

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

    // PVector isCloseTo(PVector p, Line line){
	
    // 	if(line.l.dist(p) < snapDist){
    // 	    return line.l;
    // 	}
    // 	if(line.r.dist(p) < snapDist){
    // 	    return line.r;
    // 	}
	
    // 	return null;
    // }
    



    void drawSelf(GLGraphicsOffScreen g){

	isActive = activeLine == this;

	if(isActive){

	    g.pushStyle();
	    g.noFill();
	    
	    g.stroke(255, 20, 20);
	    g.ellipse(l.x, l.y, 9, 9);
	    
	    if(lineMode == EDIT_LEFT){
		float s = getSizeAnimated();
		g.ellipse(l.x, l.y, s, s);
	    }	    

	    g.stroke(20, 20, 255);
	    g.ellipse(r.x, r.y, 9, 9);
	    if(lineMode == EDIT_RIGHT){
		float s = getSizeAnimated();
		g.ellipse(r.x, r.y, s, s);
	    }	    

	    g.popStyle();

	    g.stroke(193, 160, 23);
	}
	else {

	    if(isHidden) {
		g.stroke(60);
		g.strokeWeight(1);
	    } else {
		g.stroke(150);
		g.strokeWeight(2);
	    }

	}

	// Set the weight and draw the line.
	g.strokeWeight(w);


	g.line(l.x, 
	       l.y,
	       r.x,
	       r.y);

	if(isSelecting){
	    int id = lines.indexOf(this);
	
	    g.pushMatrix();

	    g.translate((l.x + r.x) / 2f , 
			(l.y + r.y) / 2f + 10, 
			0);
	    g.scale(1, -1, 1);
	    
	    g.textFont(numPadFont, idFondSize);
	    g.text(id, 0, 0);
	    g.popMatrix();
	}
    }

}


class PointLine extends Line {

    // For horizon and Point
    PVector pos;

    boolean isHorizon = false;
    boolean isVert = false;

    public PointLine(){

	this.isStandard = false;

	pos = new PVector(0.5 * drawSize.x, 0.5 * drawSize.y);
	snapPoints = new PVector[1];
	snapPoints[0] = pos;

    }


    private final int NO_LINE = 0;
    private final int HORIZON_ONLY = 1;
    private final int VERTICAL_ONLY = 2;
    private final int HORIZON_VERTICAL = 3;
    private final int NB_CYCLE = 4;

    private int currentCycle = 0;

    // TODO: cycle ... 
    void switchHorizonVert(){

	currentCycle = (currentCycle + 1) % NB_CYCLE;
	
	setHorizon(currentCycle == HORIZON_ONLY || 
		   currentCycle == HORIZON_VERTICAL);

	setVert(currentCycle == VERTICAL_ONLY || 
		currentCycle == HORIZON_VERTICAL);

    }




    void setHorizon(boolean h){
	this.isHorizon = h;
    }

    void setVert(boolean v){
	this.isVert = v;
    }

    void movePosTo(PVector p){
	pos.set(checkSnap(p));
    }

    void movePosBy(PVector p){
	p.mult(indirectPrecisionFactor);
	PVector r2 = PVector.add(p, pos);
	pos.set(checkSnap(r2));
    }

   void setActive(PVector p){

       setPointMode();
       
       if(p == pos)
	   lineMode = EDIT_LINE_POS;

    }
    
    // PVector isCloseTo(PVector p, Line line){

    // 	if(line.pos.dist(p) < snapDist){
    // 	    return line.pos;
    // 	}

    // 	return null;
    // }
    

    void drawSelf(GLGraphicsOffScreen g){

	isActive = activeLine == this;

	if(isActive){

	    g.pushStyle();
	    g.noFill();
	    

	    g.stroke(20, 255, 20);
	    g.ellipse(pos.x, pos.y, 9, 9);
	    
	    if(lineMode == EDIT_LINE_POS){
		float s = getSizeAnimated();
		g.ellipse(pos.x, pos.y, s, s);
	    }
	    
	    g.popStyle();

	    g.stroke(193, 160, 23);
	}
	else {

	    if(isHidden) {
		g.stroke(60);
		g.strokeWeight(1);
	    } else {
		g.stroke(150);
		g.strokeWeight(2);
	    }

	}

	// Set the weight and draw the line.
	g.strokeWeight(w);


	if(isHorizon) {

	    g.line(0, 
		   pos.y,
		   drawSize.x,
		   pos.y);

	}

	if(isVert){

	    g.line(pos.x, 
		   0,
		   pos.x,
		   drawSize.y);

	}



	if(!isActive && (isHorizon || isVert))
	    g.ellipse(pos.x, pos.y, 9, 9);
	
	g.ellipse(pos.x, pos.y, 1, 1);
	

	if(isSelecting){
	    int id = lines.indexOf(this);
	
	    g.pushMatrix();
	    g.translate(pos.x , pos.y + 10, 0);
	    g.scale(1, -1, 1);

	    g.textFont(numPadFont, idFondSize);	    
	    g.text(id, 0, 0);
	    g.popMatrix();
	}

    }

}
