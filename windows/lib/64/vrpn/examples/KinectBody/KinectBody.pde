

import peasy.*;

PeasyCam cam;

int frameSizeX = 800;
int frameSizeY = 600;

PVector[] body;

void setup(){

    size(frameSizeX, frameSizeY, OPENGL); 

    cam = new PeasyCam(this, 4);
    cam.setMinimumDistance(1);
    cam.setMaximumDistance(15);

    cam.setWheelScale(0.2);

    body = new PVector[24];
    for(int i = 0; i < body.length; i++){
	body[i] = new PVector();
    }

    initVrpn();
}


void draw(){

    background(0);

    // cam.lookAt(trackerPos.x, trackerPos.y, trackerPos.z);

    // align to 16	Left Hip

    pushMatrix();
    translate(0, 0, -  body[16].z);

    // pushStyle();
    // fill(80, 80);
    // box(100, 100, 0.1);
    // popStyle();


    float zNear = 1;
    float zFar = 200;
    float fov = PI/3.0;
    float cameraZ = (height/2.0) / tan(fov/2.0);
    perspective(fov, float(width)/float(height), 
		zNear, zFar);
		//		cameraZ/10.0, cameraZ*10.0);

    pushMatrix();
      translate(trackerPos.x, trackerPos.y, trackerPos.z);
      box(0.1);
      //      box(5);
    popMatrix();

    for(int i = 0; i < body.length; i++){
    	PVector v =  body[i];
    	pushMatrix();
    	  translate(v.x, v.y, v.z);
    	  box(0.05);
    	popMatrix();
    }

    pushStyle();
      strokeWeight(1);
      stroke(200);

      createLine(0, 1);
      createLine(1, 2);
      createLine(2, 3);
      createLine(3, 4);
      createLine(4, 5);
      createLine(5, 6);
      createLine(6, 7);
      createLine(7, 8);
      createLine(8, 9);

      createLine(10, 11);
      createLine(11, 12);
      createLine(12, 13);
      createLine(14, 15);

      createLine(3, 16);
      createLine(3, 20);

      createLine(16, 17);
      createLine(17, 18);
      createLine(18, 19);

      createLine(20, 21);
      createLine(21, 22);
      createLine(22, 23);


    popStyle();

    popMatrix();


}


void createLine(int a, int b){

    line(body[a].x,
	 body[a].y,
	 body[a].z,
	 body[b].x,
	 body[b].y,
	 body[b].z);

}

boolean test = false;

void keyPressed(){

    if(key == 't')
	test = !test;


    if(key == 's')
	save("skeleton.png");

}

