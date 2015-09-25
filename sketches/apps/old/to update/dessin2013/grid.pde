
import java.text.DecimalFormat;

int GRID_MODE = 14113;

PImage gridImg, scaleImg, shiftImg, intensImg, onImg, offImg;

int gridMode = MODE_NONE;

int GRID_SHIFT = 3;
int GRID_SCALE = 4;
int GRID_WEIGHT = 5;
int GRID_INTENS = 6;

boolean isGridOn = false;
ArrayList<PVector> gridSnapPoints = null;

PVector shift = new PVector();  // no shift
float size = 35;  // marker size. 
float minSize = 10;  // 1cm 
int gridIntensity = 70;
int gridWeight = 2;

// Use decimalFormat from earlier...
DecimalFormat decimalFormatGrid;

void initGridMode(){
    gridImg = loadImage(sketchPath + "/images/grid.png");
    scaleImg = loadImage(sketchPath + "/images/grid-size.png");
    shiftImg = loadImage(sketchPath + "/images/grid-shift.png");

    onImg = loadImage(sketchPath + "/images/on.png");
    offImg = loadImage(sketchPath + "/images/off.png");

    // TODO: remove ?
    intensImg = loadImage(sketchPath + "/images/intens.png");

    decimalFormatGrid = new DecimalFormat("##.#");
}

void enterGridMode(){
    resetFullNumPad();
    setNoMode();

    numPadSetOneLineText("Size : " +decimalFormatGrid.format(size), 12);
    //    numPadSetOneLineText("Grid mode", 12);
}

void leaveGridMode(){
    computeGridPoints();
    setNoMode();
}


void setNoMode(){

    gridMode = MODE_NONE;
    setNoActiveKey();
}

void keyGrid(){



    if(key == '1'){
	if(gridMode == GRID_SHIFT)
	    setNoMode();
	else{
	    setActiveKey(0);
	    gridMode = GRID_SHIFT;
	}

	gridActions++;
    }


    if(key == '2'){
	if(gridMode == GRID_SCALE)
	    setNoMode();
	else{
	    setActiveKey(1);
	    gridMode = GRID_SCALE;
	}

	gridActions++;
    }


   if(key == '3'){
       isGridOn = !isGridOn;

       gridActions++;
    }



}

void touchGrid(TouchElement te){
    
    if(te.speed2D.size() >= 1){

	PVector p0 = te.speed2D.get(0);
	PVector v = PVector.mult(p0, drawSize);
	    
	if(gridMode == GRID_SCALE){
	    
	    // On x for no reason.
	    float d = (v.x) / 8f;
	    size += d;

	    if(size < minSize)
		size = minSize;

	    if(size > drawSize.y / 4f)
		size = drawSize.y / 4f;

	    // float shift = 0;  // no shift
	    // float size = 20;  // 2cm 
	    numPadSetOneLineText("Size : " +decimalFormatGrid.format(size), 12);

	}

	if(gridMode == GRID_SHIFT){

	    v.mult(1f/8f);
	    shift.add(v);

	    if(shift.x < 0)
		shift.x += size;
	    if(shift.y < 0)
		shift.y += size;

	    if(shift.x >= size)
		shift.x -= size;
	    if(shift.y >= size)
		shift.y -= size;

	}

	computeGridPoints();
    }

}

void drawGrid(GLGraphicsOffScreen g){

    if(globalMode == GRID_MODE){

	numPadImages[0] = shiftImg;

	numPadImages[1] = scaleImg;
	numPadImages[2] = gridImg;
 	//	numPadImages[3] = scaleImg;
    }

    if(!isGridOn)
	return;

    g.noFill();
    g.stroke(gridIntensity);
    g.strokeWeight(gridWeight);

    float px = -size + shift.x;
    float py = -size + shift.y;

    // Vertical lines
    while(px < drawSize.x){
	g.line(px, 0, px, drawSize.y);
	px += size;
    }

    // Horizontal lines
    while(py < drawSize.y){
	g.line(0, py, drawSize.x, py);
	py += size;
    }



    if(isGridOn && gridSnapPoints != null) {
	for(PVector s : gridSnapPoints){
	    g.ellipse(s.x, s.y, 1, 1);
	}
    }

}

void computeGridPoints(){


    ArrayList<PVector> gridPoints = new ArrayList<PVector>();

    float px = -size + shift.x;
    while(px < drawSize.x){

	float py = -size + shift.y;
	while(py < drawSize.y){

	    if(px > 0 && py > 0){
		gridPoints.add(new PVector(px, py));
	    }
	    py += size;

	}
	px += size;
    }

    gridSnapPoints = gridPoints;
}
