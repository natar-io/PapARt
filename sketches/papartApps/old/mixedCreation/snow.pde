
boolean useSnow = true;

class Snow extends SubSketch {

    int numberBalls = 400;
    int numberSnow = 800;
    
    //this creates an empty array called myBalls that can contain numberBalls objects of the class Ball
    Ball[] myBalls = new Ball[numberBalls];
    Ball[] backsnow = new Ball[numberSnow];


  public void setup(PApplet parent){
	
	// size in Pixels 
	this.width = 500;
	this.height = 500;

	// display size in millimeters 
	this.displayWidth = 200;
	this.displayHeight = 200;

	// initalize the sketch
	this.initSketch(parent);


	//////////// Std processing code ////////
	//population
	
	for(int i = 0; i<numberSnow; i++)
	    {
		backsnow[i] = new Ball();
		backsnow[i].myDiameter = 5;
		backsnow[i].posX = random(0, width);
		backsnow[i].posY = random(0, height);
		backsnow[i].speedX = random(0, .5);
		backsnow[i].speedY = random(1, 2);
		backsnow[i].red = random(60, 140);
	    }
	
	for(int i =0; i<numberBalls; i++)
	    {
		myBalls[i] = new Ball();
		myBalls[i].myDiameter = 8;
		myBalls[i].posX = random(0, width);
		myBalls[i].posY = random(0, height);
		myBalls[i].speedX = random(0, 1);
		myBalls[i].speedY = random(1, 3);
		myBalls[i].red = random(140, 255);
		myBalls[i].alpha = 200;
	    }

    }

    public void draw(){
	
	//	background(20);

	clear(0, 0);

	// if(!useSnow){
	//     return;
	// }

	// println("Snow");
	// clear(20, 10);
	
	//	scale(1, -1, 1);
	noStroke();

	if(useSnow)
	for(int i =0; i<numberBalls; i++){
	    myBalls[i].update();
	    backsnow[i].update();
	}

    }



    class Ball //this does not exist until you call it
    {
	//these are properties of the class
	int myDiameter = 10;
	float red = 255;
	float alpha = 110;
	float posX = 250; //these are the default properties of the class
	float posY = 250;
	float speedX = 3; //three pixels at a time
	float speedY = 2;
	//
   
	//this is the method
	//and it is a function inside the class, it can be whatever name you decide
	void update()
	{
	    noStroke();
	    fill(100 + red, alpha);
	    ellipse(posX, posY, myDiameter, myDiameter);
	    posX+= speedX;
	    posY+= speedY;
   
	    if(posX > width) //when you have only one instruction in your block of instruction, you don't have to use the curly brackets
		posX = 0;
   
	    if(posX < 0)
		posX= width;
   
	    if(posY > height)
		posY = 0;
   
	    if(posY < 0)
		posY = height;
   
	    if(speedY < 2)
		{
		    myDiameter = 7;
		    red = 130;
		}
   
	    if(speedY < 1.6)
		{
		    myDiameter = 5;
		    red = 100;
		}
   
	    if(speedY < 1.2)
		{
		    myDiameter = 3;
		    red = 70;
		}
   
	}
    }


}
