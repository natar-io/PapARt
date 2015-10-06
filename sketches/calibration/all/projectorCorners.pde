import java.awt.Frame;
import com.jogamp.newt.opengl.GLWindow;

public class ProjectorCorners extends PApplet {

    PImage cameraImage;

    public ProjectorCorners() {
	super();
	PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }

    // TODO: choose the screen.
    public void settings() {
	size(screenWidth, screenHeight, P3D);

    }

    public void setSize(){
        GLWindow window = (GLWindow) this.getSurface().getNative();
        window.setUndecorated(true);
        window.setSize(screenWidth, screenHeight);
        window.setPosition(screenOffsetX, screenOffsetY);
    }

    public void setup() {
        frameRate(2);
        setSize();
    }

    public void draw() {
        background(0);
        projectCornersImage();
    }

    void projectCornersImage(){
        fill(255);
        rect(0,0, 5, 5);

        fill(255);
        rect(width-5,0, 5, 5);

        fill(255);
        rect(width-5,height-5, 5, 5);

        fill(255);
        rect(0, height-5, 5, 5);
    }

}
