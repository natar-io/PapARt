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


PImage cameraImg;
IplImage cameraImgIpl;


PlaneCalibration planeCalibCam;

// size does not matter, I think.
PVector paperSize = new PVector(297, 210);


ExtrinsicCalibrator calibrator;

void settings(){
    size(800, 600, P3D);
}

public void setup(){

    papart = Papart.projection(this);

    // cameraTracking = CameraFactory.createCamera(papart.cameraConfiguration.getCameraType(),
    //     					       papart.cameraConfiguration.getCameraName());
    // cameraTracking.setParent(this);
    // cameraTracking.setCalibration(papart.cameraCalib);

    // cameraTracking.convertARParams(this, papart.cameraCalib, papart.camCalibARtoolkit);
    // cameraTracking.initMarkerDetection(papart.camCalibARtoolkit);


    // TODO: make this work without starting the camera...

    // cameraTracking.start();
    // cameraTracking.setThread();


    projector = papart.getProjectorDisplay();
    cameraTracking = papart.getCameraTracking();

    // markerBoard = new MarkerBoard(Papart.markerFolder + "big.cfg",
    //                               paperSize.x, paperSize.y); // the size does not matter here.
    // cameraTracking.trackMarkerBoard(markerBoard);

    //   projector = new ProjectorDisplay(this, papart.projectorCalib);
    decodedCode = DecodedCode.loadFrom(this, "../capture/scan0");
    calibrator = new ExtrinsicCalibrator(decodedCode,
                                         projector,
                                         cameraTracking);

    myApp = new MyApp();

    papart.startTracking();
    // TODO: working with ref ?
    // cameraImg = decodedCode.getRefImage();
    // cameraImgIpl = decodedCode.getRefImageIpl();
}

MyApp myApp;



void draw(){
    cameraImg = cameraTracking.getPImage();
    cameraImgIpl = cameraTracking.getIplImage();

    image(cameraImg, 0, 0, width, height);

}

void compute(){

    cameraPaperTransform = myApp.getLocation();

//    markerBoard.updatePosition(cameraTracking, cameraImgIpl);
//    cameraPaperTransform = markerBoard.getTransfoMat(cameraTracking);

    println("Camera Paper...");
    cameraPaperTransform.print();
    calibrator.setTransform(cameraPaperTransform);

    PMatrix3D output = calibrator.compute();
    papart.saveCalibration(Papart.cameraProjExtrinsics, output);
}

public class MyApp extends PaperScreen {
    void setup() {
        setDrawingSize(297, 210);
        loadMarkerBoard(Papart.markerFolder + "A3-small1.cfg", 297, 210);
    }
    void draw() {
        beginDraw2D();
        background(100, 100, 100);
        endDraw();
    }
}


void keyPressed(){
    compute();
}
