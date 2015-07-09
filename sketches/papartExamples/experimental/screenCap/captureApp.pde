import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.image.WritableRaster;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

public class CaptureApp  extends PaperTouchScreen {

    PGraphicsOpenGL pg;
    Robot robot;
    PImage capturedImage;
    Rectangle rectangle;

    public CaptureApp(PApplet parent,
	       MarkerBoard board,
	       PVector size,
	       float resolution,
	       Camera cam,
	       ARDisplay proj,
	       TouchInput touchInput) {
	super(parent, board, size,
	      resolution,
	      cam, proj, touchInput);
	
	try{
	    robot = new Robot();
	    rectangle = new Rectangle(1948, 78, 1024, 600);
	    
	    BufferedImage im  = robot.createScreenCapture(rectangle);
	    capturedImage = new PImage(im);
	} catch(Exception e){
	    println(e);
	}


    }


    public void draw(){
	
	setLocation(150, 0, 0);
	pg = beginDraw2D();
	    
	pg.background(0);
	BufferedImage bi = robot.createScreenCapture(rectangle);
	int w = bi.getWidth();
	int h = bi.getHeight();
      
	capturedImage.loadPixels();
	WritableRaster raster = bi.getRaster();
	raster.getDataElements(0, 0, w, h, capturedImage.pixels);
	capturedImage.updatePixels();

	pg.image(capturedImage, 0, 0, getDrawingSize().x, getDrawingSize().y);

	for(Touch touch : getTouchList()){

	    if(touch.is3D) 
		continue ;
	    PVector p = touch.p;

	    if(p.x < 0 || p.x > 1 || p.y < 0 || p.y > 1) 
		continue;

	    float x = rectangle.x + p.x * rectangle.width ;
	    float y = rectangle.y + p.y * rectangle.height;

	    robot.mouseMove((int) x, (int) y);
	    robot.mousePress(InputEvent.BUTTON1_MASK);
	    robot.delay(100);
	    robot.mouseRelease(InputEvent.BUTTON1_MASK);
	}

	pg.endDraw();

    }

}
