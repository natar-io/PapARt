int minDropLength = 10;
int maxDropLength = 200;

int dropMinSize = 3;
int dropVaribaleSize = 20;

class Drop {
    int x, y, size,r,red,green,blue;
    boolean isMoving;
    Drop(int theX, int theY, int redC) {
	x = theX;
	y = theY;
	red = redC;
	
	r = y + int(random(minDropLength, maxDropLength));
	size = (int)( dropMinSize + random(dropVaribaleSize));

	isMoving=true;
    }
    
    void drip()  {
	if(size > 1 && perCentChance(30))
	    {
		size--;
	    }
	if(isMoving==true){
	    y++;
	}
    }
    
    void tryStop(){
	if(y == r){
	    isMoving=false;
	}
    }
    
    void show(PGraphicsOpenGL bloodGraphics) {
	bloodGraphics.stroke(255-red, 0, 0, 180);
	bloodGraphics.fill(  255-red, 0, 0, 180);

	// int splat = round(random(0,20));
	// bloodGraphics.textFont(font, size);
	// bloodGraphics.text(splat, x, y); 
	bloodGraphics.ellipse(x,y,size,size);
    }
}  
