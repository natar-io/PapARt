import fr.inria.papart.depthcam.devices.*;
import fr.inria.papart.depthcam.analysis.*;

boolean isKinectOne = false;
boolean isKinect360 = false;

int frameWidth, frameHeight;

ProjectiveDeviceP projectorDevice, kinectProjectiveP;

KinectDevice kinectDevice;
KinectProcessing kinectAnalysis;
PointCloudVisualization pcv;

boolean isKinectOneActivated = false;
boolean isKinect360Activated = false;
PlaneAndProjectionCalibration planeProjCalib;
HomographyCalibration homographyCalibration;
PlaneCalibration planeCalibCam;

PMatrix3D stereoCalib;
float kinectStereoValueX, kinectStereoValueY;

ARDisplay arDisplayKinect;

// camera Kinect360
Camera cameraKinect;

void checkKinectVersion(){
    CameraConfiguration kinectConfiguration = Papart.getDefaultKinectConfiguration(this);
    isKinect360 = kinectConfiguration.getCameraType() == Camera.Type.OPEN_KINECT;
    isKinectOne = kinectConfiguration.getCameraType() == Camera.Type.KINECT2_RGB;

    stereoCalib = HomographyCalibration.getMatFrom(this, Papart.kinectStereoCalib);
}


void activateKinect(){

    if(isKinectOne)
        initKinectOne();
    if(isKinect360)
        initKinect360();
}


void initKinectOne(){
    if(isKinectOneActivated)
        return;

    kinectDevice = new KinectOne(this, camera);

    initCommonKinect();

    // Kinect camera is the main tracking Camera
    kinectProjectiveP = camera.getProjectiveDevice();

    isKinectOneActivated = true;
}


// To implement fully
void initKinect360(){

    kinectDevice = new Kinect360(this);
    cameraKinect = kinectDevice.getCameraRGB();

    String ARToolkitCalib = sketchPath() + "/data/Kinect.cal";
    cameraKinect.convertARParams(this, cameraKinect.getCalibrationFile(), ARToolkitCalib);
    cameraKinect.initMarkerDetection(ARToolkitCalib);

    arDisplayKinect = new ARDisplay(this, cameraKinect);
    arDisplayKinect.init();
    arDisplayKinect.manualMode();

    app.addDisplay(arDisplayKinect);

    // TODO: Find why it updates before camera starts?
    cameraKinect.trackSheets(true);


     kinectProjectiveP = cameraKinect.getProjectiveDevice();
     cameraKinect.setThread();

     initCommonKinect();
     isKinect360Activated = true;
}

void initCommonKinect(){

    kinectDevice.getCameraDepth().setThread();

    kinectAnalysis = new KinectProcessing(this, kinectDevice);
    planeProjCalib = new PlaneAndProjectionCalibration();
    homographyCalibration = new HomographyCalibration();
    // init is done later now.
// pcv = new PointCloudVisualization();
    projectorDevice = projector.getProjectiveDeviceP();
    frameWidth = projectorDevice.getWidth();
    frameHeight = projectorDevice.getHeight();
}

void kinectStereoX(float value){
    this.kinectStereoValueX = value;
    controlFrame.showSaveBangKinectStereo();
}

void kinectStereoY(float value){
    this.kinectStereoValueY = value;
    controlFrame.showSaveBangKinectStereo();
}

void saveStereoKinect(boolean pressed){
    HomographyCalibration.saveMatTo(this, stereoCalib, Papart.kinectStereoCalib);
    controlFrame.hideSaveBangKinectStereo();
}


boolean computeScreenPaperIntersection(PlaneCalibration planeCalibCam,
                                       PMatrix3D kinectCameraExtrinsics){

    // generate coordinates...
    float step = 0.5f;
    int nbPoints = (int) ((1 + 1f / step) * (1 + 1f / step));
    HomographyCreator homographyCreator = new HomographyCreator(3, 2, nbPoints);

    // Creates 3D points on the corner of the screen
    int k = 0;
    for (float i = 0; i <= 1.0; i += step) {
        for (float j = 0; j <= 1.0; j += step, k++) {

            PVector screenPoint = new PVector(i, j);
            PVector kinectPoint = new PVector();

            // where the point is on the table.
            PVector inter = computeIntersection(planeCalibCam, i, j);
            if(inter == null)
                return false;

            // inter is viewed from tracking.
            //
            kinectCameraExtrinsics.mult(inter, kinectPoint);

            homographyCreator.addPoint(kinectPoint, screenPoint);
        }
    }
    homographyCalibration = homographyCreator.getHomography();
    return true;
}



PVector computeIntersection(PlaneCalibration planeCalibCam,float px, float py){

    // Create ray from the projector (origin / viewed pixel)
    // Intersect this ray with the piece of paper.
    // Compute the Two points for the ray
    PVector originP = new PVector(0, 0, 0);
    PVector viewedPtP = projectorDevice.pixelToWorldNormP((int) (px * frameWidth), (int) (py * frameHeight));

    // Pass it to the camera point of view (origin)
    PMatrix3D extr = projector.getExtrinsicsInv();
    PVector originC = new PVector();
    PVector viewedPtC = new PVector();
    extr.mult(originP, originC);
    extr.mult(viewedPtP, viewedPtC);

    // Second argument is a direction
    viewedPtC.sub(originC);

    Ray3D ray
        = new Ray3D(new Vec3D(originC.x,
                              originC.y,
                              originC.z),
                    new Vec3D(viewedPtC.x,
                              viewedPtC.y,
                              viewedPtC.z));

    // Intersect ray with Plane
    ReadonlyVec3D inter = planeCalibCam.getPlane().getIntersectionWithRay(ray);

    if(inter == null){
        println("No intersection :( check stuff");
        return null;
    }

    return new PVector(inter.x(), inter.y(), inter.z());
}