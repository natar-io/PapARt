

// Undecorated frame 
public void init() {
    frame.removeNotify(); 
    frame.setUndecorated(true); 
    frame.addNotify(); 
    super.init();
}



int framePosX = 0;
int framePosY = 120;
int frameSizeX = 1280;
int frameSizeY = 800;

void setup(){
    size(frameSizeX, frameSizeY, OPENGL);

    background(0);
}


int ellipseSize = 40;

void draw(){

    float step = 0.5f;
    //    nbPoints = (int) ((1 + 1f / step) * (1 + 1f / step));
    
    int k = 0;
    for (float i = 0; i <= 1.0; i += step) {
	for (float j = 0; j <= 1.0; j += step, k++) {
	    fill(i * 150 + 100, j * 150 +  100, 100);
	    ellipse(i * width, j * height, ellipseSize, ellipseSize);
	}
    }

}


void keyPressed() {

    // Placed here, bug if it is placed in setup().
    if(key == ' ')
	frame.setLocation(framePosX, framePosY);
}



