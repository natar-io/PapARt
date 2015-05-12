import java.awt.Frame;

// http://forum.processing.org/one/topic/popup-how-to-open-a-new-window.html

CamPFrame cameraPFrame;
camApplet cameraApplet;



void testCamera(){

    cameraConfig.setCameraName(cameraIdText.getText());

    cameraPFrame = new CamPFrame();
}


class CamPFrame extends Frame {
    
    public CamPFrame() {
        setBounds(100, 100, 640, 480);
	
        cameraApplet = new camApplet();
        add(cameraApplet);
        cameraApplet.init();
        show();
    }
}

Camera camera;

class camApplet extends PApplet {

  
    public void setup() {

	size(640, 480, P3D);

	camera = cameraConfig.createCamera();
	camera.setParent(this);
	camera.setSize(640, 480);
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
}
