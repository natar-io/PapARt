import java.awt.Frame;

// http://forum.processing.org/one/topic/popup-how-to-open-a-new-window.html

PFrame f;
secondApplet s;


void initSecondApplet(){
    
    try{
	cc.setProjectionScreenOffsetX(Integer.parseInt(posXText.getText()));
	cc.setProjectionScreenOffsetY(Integer.parseInt(posYText.getText()));
	PFrame f = new PFrame();

    }catch(java.lang.NumberFormatException e){
	println("Invalid Position");
    }
}



class PFrame extends Frame {
    
    public PFrame() {

	int posX = cc.getProjectionScreenOffsetX();
	int posY = cc.getProjectionScreenOffsetY();
	int width = cc.getProjectionScreenWidth();
	int height = cc.getProjectionScreenHeight();

        setBounds(posX, posY, width, height);

	
	setUndecorated(true); 
	
        s = new secondApplet();
        add(s);
        s.init();
        show();
    }
}

class secondApplet extends PApplet {


    public void setup() {

	int width = cc.getProjectionScreenWidth();
	int height = cc.getProjectionScreenHeight();
	size(width, height);
	
	rect(0, 0, rectSize, rectSize);
	rect(width-rectSize, 0, rectSize, rectSize);
	rect(width-rectSize, height-rectSize, rectSize, rectSize);
	rect(0, height-rectSize, rectSize, rectSize);
	
        noLoop();
    }
    public void draw() {
    }
}
