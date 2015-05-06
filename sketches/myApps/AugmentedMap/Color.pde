public class Color{
    float cRed;
    float cGreen;
    float cBlue;

    Color(){
	cRed = 0;
	cGreen = 0;
	cBlue = 0;
    }
    Color(color _color){
        cRed = red(_color);
	cGreen = green(_color);
	cBlue = blue(_color);
    }
    Color(float _cRed, float _cGreen, float _cBlue){
	cRed = _cRed;
	cGreen = _cGreen;
	cBlue = _cBlue;
    }
    public float getRed(){
	return cRed;
    }
    public float getGreen(){
	return cGreen;
    }
    public float getBlue(){
	return cBlue;
    }
    public color getColor(){
	return color(cRed, cGreen, cBlue);
    }
    public float dist(color _color){
	float res = sq(cRed - red(_color));
	res += sq(cGreen - green(_color));
	res += sq(cBlue - blue(_color));
	res = sqrt(res);
	return res;
    }
    public float dist(Color _color){
	return dist(_color.getColor());
    }

    public void add(Color _color){
	cRed += _color.getRed();
	cGreen += _color.getGreen();
	cBlue += _color.getBlue();
    }

    public void set(Color _color){
	cRed = _color.getRed();
	cGreen = _color.getGreen();
	cBlue = _color.getBlue();
    }

    PImage getAverageColor(PImage img, int minX, int maxX, int minY, int maxY){
	int w = img.width;
	int h = img.height;
	minX = constrain(minX, 0, w);
	minY = constrain(minY, 0, h);
	maxX = constrain(maxX, 0, w);
	maxY = constrain(maxY, 0, h);

	/*System.out.println("w: " + w);
	System.out.println("h: " + h);
	System.out.println("minX: " + minX);
	System.out.println("minY: " + minY);
	System.out.println("maxX: " + maxX);
	System.out.println("maxY: " + maxY);*/

	w = maxX - minX;
	h = maxY - minY;

	PImage piece = createImage(w, h, RGB);

	cRed = 0;
	cGreen = 0;
	cBlue = 0;

	for(int x = minX; x < maxX; x++){
	    for(int y = minY; y < maxY; y++){
		color col = img.get(x, y);
		piece.set(x - minX, y - minY, col);
		cRed += red(col);
		cGreen += green(col);
		cBlue += blue(col);
	    }
	}

	cRed /= w * h;
	cGreen /= w * h;
	cBlue /= w * h;

	return piece;

    }
    
    void print(){
	System.out.println("Red: " + cRed);
	System.out.println("Green: " + cGreen);
	System.out.println("Blue: " + cBlue);
    }
}
