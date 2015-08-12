import java.awt.Frame;


SecondApplet s;

void initSecondApplet(){
    
    try{
	screenConfig.setProjectionScreenOffsetX(Integer.parseInt(posXText.getText()));
	screenConfig.setProjectionScreenOffsetY(Integer.parseInt(posYText.getText()));

	SecondApplet app = new SecondApplet(); 

    }catch(java.lang.NumberFormatException e){
	println("Invalid Position");
    }
}

public class SecondApplet extends PApplet {

    private boolean first = true;
    
    public SecondApplet(){
	super();
	PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }

    public void settings() {
	size(screenConfig.getProjectionScreenWidth(),
	     screenConfig.getProjectionScreenHeight(), P3D);
	fullScreen();
    }

    public void setup() {

    }

    public void post(){

	if(first){
	    println("Post");
	    frame.setLocation(screenConfig.getProjectionScreenOffsetX(),
			      screenConfig.getProjectionScreenOffsetY());
	    // getSurface().setSize(screenConfig.getProjectionScreenWidth(),
	    // 			  screenConfig.getProjectionScreenHeight());

	    frame.setUndecorated(true); 
	}
    }

    public void draw() {
	background(100);
	rect(0, 0, rectSize, rectSize);
	rect(width-rectSize, 0, rectSize, rectSize);
	rect(width-rectSize, height-rectSize, rectSize, rectSize);
	rect(0, height-rectSize, rectSize, rectSize);

    }
}
