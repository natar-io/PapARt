import java.awt.Frame;

// http://forum.processing.org/one/topic/popup-how-to-open-a-new-window.html

PFrame f;
secondApplet s;


void initSecondApplet(){
    
    try{
	screenConfig.setProjectionScreenOffsetX(Integer.parseInt(posXText.getText()));
	screenConfig.setProjectionScreenOffsetY(Integer.parseInt(posYText.getText()));
	PFrame f = new PFrame();

    }catch(java.lang.NumberFormatException e){
	println("Invalid Position");
    }
}



class PFrame extends Frame {
    
    public PFrame() {

	int posX = screenConfig.getProjectionScreenOffsetX();
	int posY = screenConfig.getProjectionScreenOffsetY();
	int width = screenConfig.getProjectionScreenWidth();
	int height = screenConfig.getProjectionScreenHeight();

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

	int width = screenConfig.getProjectionScreenWidth();
	int height = screenConfig.getProjectionScreenHeight();
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
