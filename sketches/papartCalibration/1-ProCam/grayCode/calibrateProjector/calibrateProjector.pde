import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.procam.display.*;
import fr.inria.papart.drawingapp.*;
import fr.inria.papart.scanner.*;
import fr.inria.papart.calibration.*;
import fr.inria.guimodes.*;

import static org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

import fr.inria.guimodes.Mode;
import fr.inria.papart.scanner.GrayCode;


Papart papart;
DecodedCode decodedCode;
Camera cameraTracking;
MarkerBoard markerBoard;
PMatrix3D cameraPaperTransform;
ProjectorDisplay projector;
ProjectiveDeviceP projectorDevice, cameraDevice;


PImage cameraImg;
PImage cameraImg2;
IplImage cameraImgIpl;


PlaneCalibration planeCalibCam;

ArrayList<CalibrationPoint> pointList;

// size does not matter, I think.
PVector paperSize = new PVector(297, 210);


ExtrinsicCalibrator calibrator;

void settings(){
    size(800, 600, P3D);
}

public void setup(){

    papart = new Papart(this);

    cameraTracking = CameraFactory.createCamera(papart.cameraConfiguration.getCameraType(),
						       papart.cameraConfiguration.getCameraName());
    cameraTracking.setParent(this);
    cameraTracking.setCalibration(papart.proCamCalib);

    cameraTracking.convertARParams(this, papart.proCamCalib, papart.camCalibARtoolkit);
    cameraTracking.initMarkerDetection(papart.camCalibARtoolkit);

    markerBoard = new MarkerBoard(sketchPath + "/data/big.cfg", paperSize.x, paperSize.y); // the size does not matter here.
    cameraTracking.trackMarkerBoard(markerBoard);

    // TODO: make this work without starting the camera...

    cameraTracking.start();
    // grab a few images...
    for(int i = 0; i < 10; i++)
	cameraTracking.grab();

    // cameraImg = decodedCode.getRefImage();
    // cameraImgIpl = decodedCode.getRefImageIpl();

    cameraImg = cameraTracking.getPImage();
    cameraImgIpl = cameraTracking.getIplImage();

    markerBoard.updatePosition(cameraTracking, cameraImgIpl);
    cameraPaperTransform = markerBoard.getTransfoMat(cameraTracking);

    println("Camera Paper...");
    cameraPaperTransform.print();

    projector = new ProjectorDisplay(this, papart.proCamCalib);
    decodedCode = DecodedCode.loadFrom(this, "../capture/scan0");

    calibrator = new ExtrinsicCalibrator(cameraPaperTransform, decodedCode, projector, cameraTracking);

    PMatrix3D output = calibrator.compute();
    papart.saveCalibration("camProjExtrinsics.xml", output);

    image(cameraImg, 0, 0, width, height);

}


void draw(){

}
