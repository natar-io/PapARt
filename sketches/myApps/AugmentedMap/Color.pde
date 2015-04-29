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
}
