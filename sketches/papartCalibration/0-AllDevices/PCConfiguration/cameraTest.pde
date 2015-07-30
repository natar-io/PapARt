import java.awt.Frame;

// http://forum.processing.org/one/topic/popup-how-to-open-a-new-window.html
camApplet cameraApplet;


void testCamera(){
    cameraConfig.setCameraName(cameraIdText.getText());

    cameraApplet = new camApplet();
}



Camera camera;

class camApplet extends PApplet {

    public camApplet() {
	super();
	PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }

    public void settings() {
	size(640, 480, P3D);
	smooth();
    }

    public void setup() {

	camera = cameraConfig.createCamera();
	camera.setParent(this);

	if(cameraConfig.getCameraType() == Camera.Type.FLY_CAPTURE &&
	   cameraCalibrationOk){
	    camera.setSize(cameraWidth, cameraHeight);
	} else {
	    camera.setSize(640, 480);
	}
	camera.start();
	camera.setThread();
    }
    public void draw() {

	if(camera== null)
	    return;
	PImage im = camera.getPImage();
	if(im != null)
	    image(im, 0, 0, 640, 480);
    }

    void mousePressed(){
	println("mouse : " + mouseX + " " + mouseY);
    }
}
