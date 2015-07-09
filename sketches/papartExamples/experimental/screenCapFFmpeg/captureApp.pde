import java.awt.Rectangle;
import java.awt.image.WritableRaster;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.awt.Robot;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacpp.opencv_core.IplImage;


public class CaptureApp  extends PaperTouchScreen {

    PGraphicsOpenGL pg;
    CamImage capturedImage;
    Rectangle rectangle;
    FFmpegFrameGrabber grabber;
    Robot robot;

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

	    int x = 1948;
	    int y = 78;
	    int w = 1024;
	    int h = 600;
	    rectangle = new Rectangle(x, y, 1024, 600);
	    robot = new Robot();
	    
	    grabber = new FFmpegFrameGrabber(":0.0+" + x + "," + y);
	    capturedImage = new CamImage(parent, rectangle.width, rectangle.height);

	    grabber.setFormat("x11grab");
	    grabber.setImageWidth(w);
	    grabber.setImageHeight(h);
	    grabber.start();

	} catch(Exception e){
	    println(e);
	}


    }


    public void draw(){
	
	setLocation(150, 0, 0);
	pg = beginDraw2D();
	    
	pg.background(0);

	try{

	    capturedImage.update(grabber.grab());
	    pg.image(capturedImage,0, 0, getDrawingSize().x, getDrawingSize().y);

	}catch (Exception e){	    
	    println("Frame lost ? " + e);
	}

	for(Touch touch : getTouchList()){

	    if(touch.is3D) 
		continue ;
	    PVector p = touch.p;

	    if(p.x < 0 || p.x > 1 || p.y < 0 || p.y > 1) 
		continue;

	    float x = rectangle.x + p.x * rectangle.width ;
	    float y = rectangle.y + p.y * rectangle.height;

	    robot.mouseMove((int) x, (int) y);
	    //	    robot.mousePress(InputEvent.BUTTON1_MASK);
	    // robot.delay(100);
	    // robot.mouseRelease(InputEvent.BUTTON1_MASK);
	}

	pg.endDraw();

    }

}
