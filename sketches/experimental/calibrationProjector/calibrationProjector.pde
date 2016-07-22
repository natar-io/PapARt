import fr.inria.papart.procam.*;
import fr.inria.papart.procam.display.*;
import fr.inria.papart.drawingapp.*;
import org.bytedeco.javacpp.*;
import TUIO.*;
import toxi.geom.*;


Papart papart;
// ProjectorDisplay ardisplay;
ARDisplay ardisplay;

float focal, cx, cy;
PMatrix3D projIntrinsics;

boolean useProjector = false;

PApplet parent;

void settings(){
    fullScreen(P3D);
    parent = this;
}

public void setup() {

    // Papart.projectorCalib = "calib.xml";

    if(useProjector)
	Papart.projectionOnly(this);
    else
	Papart.seeThrough(this);

  papart =  Papart.getPapart();
  ardisplay = papart.getARDisplay();
  ardisplay.manualMode();

  projIntrinsics = ardisplay.getIntrinsics();
  focal = projIntrinsics.m00;
  cx = projIntrinsics.m02;
  cy = projIntrinsics.m12;

  initGui();

  object = new PVector[4];
  image = new PVector[4];

  // 10 cm de côté.
  object[0] = new PVector(0, 0, 0);
  object[1] = new PVector(100, 0, 0);
  object[2] = new PVector(100, 100, 0);
  object[3] = new PVector(0, 100, 0);

  image[0] = new PVector(100, 100);
  image[1] = new PVector(200, 100);
  image[2] = new PVector(200, 200);
  image[3] = new PVector(100, 200);
}

PMatrix3D objectArdisplayTransfo, pos;
PVector object[];
PVector image[];

void draw() {


    line(width/2 - 100, height/2, width/2 + 100, height/2);
    line(width/2, height/2 - 100, width/2, height/2 + 100);

    projIntrinsics.m00 = focal;
    projIntrinsics.m11 = focal;
    projIntrinsics.m02 = cx;
    projIntrinsics.m12 = cy;

    // Update the rendering.
    ardisplay.updateIntrinsicsRendering();

    ProjectiveDeviceP pdp = ardisplay.getProjectiveDeviceP();

    // Update the estimation.
    pdp.updateFromIntrinsics();


    //    objectArdisplayTransfo = pdp.estimateOrientation(object, image);
    //    objectArdisplayTransfo.print();



    PGraphicsOpenGL g1 = ardisplay.beginDraw();

    if(useProjector)
	g1.background(69, 145, 181);
    else
	g1.clear();


    if(useProjector)
        g1.scale(1, 1, 1);
    else {
        g1.scale(1, -1, 1);
    }
    // g1.modelview.apply(objectArdisplayTransfo);

    g1.translate(0, 0, 810);

    g1.fill(50, 50, 200, 150);
    g1.rect(-10, -10, 120, 120);

    fill(200, 200);
    g1.rect(0, 0, 100, 100);

    g1.fill(0, 191, 100, 200);
    g1.rect(150, 80, 50, 50);

    ardisplay.endDraw();

    if(!useProjector)
	image(papart.getCameraTracking().getImage(), 0, 0, width, height);

    DrawUtils.drawImage((PGraphicsOpenGL) g,
			ardisplay.render(),
			0, 0, width, height);

    // quad(image[0].x, image[0].y,
    // 	 image[1].x, image[1].y,
    // 	 image[2].x, image[2].y,
    // 	 image[3].x, image[3].y);

    if (test) {
	//	objectArdisplayTransfo.print();
	test = false;
    }
}


boolean test = false;


void keyPressed() {

  if (key == 't')
    test = !test;

  if (key == 's') {

      ProjectiveDeviceP pdp = ardisplay.getProjectiveDeviceP();
      pdp.saveTo(this, "calib.xml");

      if(useProjector){
          pdp.saveProjectorTo(this, "calib.yaml");
      } else{
          pdp.saveCameraTo(this, "calib.yaml");
      }
      println("Saved");
  }
}
