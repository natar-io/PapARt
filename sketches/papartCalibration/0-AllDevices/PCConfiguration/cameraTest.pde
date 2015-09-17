import java.awt.Frame;
import fr.inria.papart.depthcam.devices.KinectOne;

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

        camera.setSize(640, 480);

	if(cameraConfig.getCameraType() == Camera.Type.FLY_CAPTURE &&
	   cameraCalibrationOk){
	    camera.setSize(cameraWidth, cameraHeight);
	} else {

            if(cameraConfig.getCameraType() == Camera.Type.KINECT2_RGB){
                camera.setSize(KinectOne.CAMERA_WIDTH_RGB, KinectOne.CAMERA_HEIGHT_RGB);
            }
            if(cameraConfig.getCameraType() == Camera.Type.KINECT2_IR){
                camera.setSize(KinectOne.CAMERA_WIDTH, KinectOne.CAMERA_HEIGHT);
            }
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

    public void close(){
        camera.close();
    }
}
