public class YButton{
    boolean pressed;
    boolean visible;
    int lastContact;
    PImage img;

    PVector center;
    int w;
    int h;

    YButton(){
	pressed = false;
	visible = false;
	lastContact = -1;
	center = new PVector(0, 0, 0);
	w = 0;
	h = 0;
    }

    YButton(PImage _img){
	pressed = false;
	visible = true;
	lastContact = -1;
	img = _img;
	center = new PVector(0, 0, 0);
	w = img.width;
	h = img.height;
    }

    YButton(PImage _img, PVector _center){
	pressed = false;
	visible = true;
	lastContact = -1;
	img = _img;
	center = _center;
	w = img.width;
	h = img.height;
    }


    YButton(PImage _img, PVector _center, int _w, int _h){
	pressed = false;
	visible = true;
	lastContact = -1;
	img = _img;
	center = _center;
	w = _w;
	h = _h;
    }

    public boolean getPressed(){
	return pressed;
    }

    public boolean getVisible(){
	return visible;
    }

    public int getLastContact(){
	return lastContact;
    }

    public PImage getImage(){
	return img;
    }

    public PVector getCenter(){
	return center;
    }

    public int getWidth(){
	return w;
    }

    public int getHeight(){
	return h;
    }

    boolean isTouched(){
	return (lastContact >= 0);
    }

    public void setCenter(PVector _center){
	center = _center;
    }

    public void setVisible(boolean _visible){
	visible = _visible;
    }

    public void reset(){
	lastContact = -1;
	pressed = false;
    }

    public void update(PVector touch){
	if(visible){
	    boolean touchingButton = (touch.x >= center.x - w / 2) && (touch.x <= center.x + w / 2) && (touch.y >= center.y - h / 2) && (touch.y <= center.y + h / 2);
	    if(touchingButton){
		if(lastContact < 0){
		    lastContact = millis();
		}
		else{
		    /*System.out.println("Last contact: " + lastContact);
		    System.out.println("Millis: " + millis());*/
		    if(millis() - lastContact >= 600){
			pressed = true;
		    }
		}
	    }
	    else{
		this.reset();
	    }
	}
    }

}
