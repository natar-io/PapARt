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

        int cornerSize = 5;
        ellipseMode(CENTER);

        translate(0, 0);
        drawEllipses();

        translate(width, 0);
        drawEllipses();

        translate(0, height);
        drawEllipses();

        translate(-width, 0);
        drawEllipses();

    }

    void drawEllipses(){
        noStroke();
        fill(120);
        ellipse(0, 0, 50, 50);

        fill(160);
        ellipse(0, 0, 30, 30);

        fill(200);
        ellipse(0, 0, 2, 2);
    }

}
