

int GRID_MODE = 14113;



PImage gridImg, scaleImg, shiftImg, intensImg;

int gridMode = MODE_NONE;

int GRID_SHIFT = 3;
int GRID_SCALE = 4;
int GRID_WEIGHT = 5;
int GRID_INTENS = 6;

boolean isGridOn = false;


PVector shift = new PVector();  // no shift
float size = 20;  // 2cm 
int gridIntensity = 128;
int gridWeight = 2;

void initGridMode(){
    gridImg = loadImage(sketchPath + "/images/grid.png");
    scaleImg = loadImage(sketchPath + "/images/grid-size.png");
    shiftImg = loadImage(sketchPath + "/images/grid-shift.png");
    intensImg = loadImage(sketchPath + "/images/intens.png");

}

void leaveGridMode(){

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
    }



   if(key == '2'){
   	if(gridMode == GRID_WEIGHT)
	    setNoMode();
	else{
	    setActiveKey(1);
	    gridMode = GRID_WEIGHT;
	}
   }

   if(key == '3'){
   	if(gridMode == GRID_INTENS)
	    setNoMode();
	else{
	    setActiveKey(2);
	    gridMode = GRID_INTENS;
	}
   }

    if(key == '4'){
	if(gridMode == GRID_SCALE)
	    setNoMode();
	else{
	    setActiveKey(3);
	    gridMode = GRID_SCALE;
	}
    }


   if(key == '5'){
       isGridOn = !isGridOn;
    }


   if(gridMode == GRID_WEIGHT){
       if(key == '+'){
	   gridWeight ++;
       }

       if(key == '-'){
	   gridWeight --;
       }
       gridWeight = constrain(gridWeight, 1, 10);

   }


   if(gridMode == GRID_INTENS){
       if(key == '+'){
	   gridIntensity += 10;
       }

       if(key == '-'){
	   gridIntensity -= 10;
       }
       gridIntensity = constrain(gridIntensity, 1, 255);
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

	    if(size < 5)
		size = 5;

	    if(size > drawSize.y / 4f)
		size = drawSize.y / 4f;

	    // float shift = 0;  // no shift
	    // float size = 20;  // 2cm 
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

    }
}

void drawGrid(GLGraphicsOffScreen g){

    if(globalMode == GRID_MODE){
	numPadImages[3] = scaleImg;
	numPadImages[0] = shiftImg;
	numPadImages[4] = gridImg;
	numPadImages[1] = lineWeight;
	numPadImages[2] = intensImg;
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

}


