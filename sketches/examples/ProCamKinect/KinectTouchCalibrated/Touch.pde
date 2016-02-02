import fr.inria.papart.depthcam.DepthData;
import java.util.ArrayList;
import toxi.geom.Vec3D;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.procam.display.*;
import fr.inria.papart.calibration.*;

public class MyApp extends PaperTouchScreen {

    PMatrix3D kinectProjector;
    PMatrix3D cameraProjector;
    PlaneCalibration tablePlane;

    void settings(){
        setDrawAroundPaper();
	setDrawingSize(297, 210);
	loadMarkerBoard(Papart.markerFolder + "A3-small1.svg", 297, 210);

    }

    void setup() {
	kinectProjector = papart.loadCalibration(Papart.kinectTrackingCalib);
	kinectProjector.invert();

	cameraProjector = ((ProjectorDisplay) getDisplay()).getExtrinsics().get();
	cameraProjector.invert();

	tablePlane = papart.getTablePlane();
    }

    PVector pointPos = new PVector();
    PVector posProj = new PVector();

    void drawAroundPaper(){

	// background(0);

	float ellipseSize = 5;

	// in draw3D Mode the graphics here are the projector's graphics.

	ProjectorDisplay projector = (ProjectorDisplay) getDisplay();
	ProjectiveDeviceP pdp = projector.getProjectiveDeviceP();

 	projector.loadModelView();
	applyMatrix(projector.getExtrinsics());

	// lights();
	// pointLight(0, 100, 0, 0, 100, 0);

	noStroke();
	fill(0);

	float focal = pdp.getIntrinsics().m00;
	float cx = pdp.getIntrinsics().m02;
	float cy = pdp.getIntrinsics().m12;

//println(cx + " " + cy + " " + focal);
//	translate(-782, -1383, 1000);

        // rectMode(CENTER);
//	rect(0, 0, 200, 200);
	// pushMatrix();

	// sphere(10);
	// popMatrix();



	for (Touch t : touchList) {

	    // draw the touch.
	    PVector p = t.position;
	    // fill(200);
	    // ellipse(p.x, p.y, ellipseSize, ellipseSize);

	    // draw the elements of the Touch

	    TouchPoint tp = t.touchPoint;
	    if(tp == null){
		println("TouchPoint null, this method only works with KinectTouchInput.");
		continue;
	    }

	    //	    Vec3D depthPoint = tp.getPositionKinect();

	    ArrayList<DepthDataElementKinect> depthDataElements = tp.getDepthDataElements();
	    for(DepthDataElementKinect dde : depthDataElements){

                Vec3D depthPoint = dde.depthPoint;

                kinectProjector.mult(new PVector(depthPoint.x,
                                                 depthPoint.y,
                                                 depthPoint.z),
                                     pointPos);


                // PVector out2D = pdp.worldToPixelCoord(pointPos);
                // println("Out " + out2D);
                // ellipse(out2D.x, out2D.y, 10, 10);

                // cameraProjector.mult(pointPos,
                // 			 posProj);

                float dist = tablePlane.distanceTo(pointPos);
                if(dist > 100)
                    continue;

                float col = 255 * ((100 - dist) / 100);

                pushMatrix();
                fill(col);
                translate(pointPos.x -2.2, pointPos.y -2.2 , pointPos.z);

                ellipse(0, 0, 3, 3);
                popMatrix();
	    }

	}
	endDraw();
    }
}
