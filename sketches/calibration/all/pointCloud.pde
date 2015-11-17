import peasy.*;

PeasyCam cam;
KinectPointCloud pointCloud;
int precision = 2;

public class PointCloudVisualization extends PApplet {

    // TODO: When the main sketch closes, it
    // forces this one to close at the wrong time,
    // and everything crashes :(

    public PointCloudVisualization() {
        super();
        PApplet.runSketch(new String[]{this.getClass().getName()}, this);

    }

    public void settings() {
        // todo: sizeof kinect
        size(kinectDevice.getCameraDepth().width(),
             kinectDevice.getCameraDepth().height(),
             P3D);
    }

    public void setup() {
        // TODO: variable precision.  ?
        pointCloud = new KinectPointCloud(this, kinectAnalysis, precision);

// Set the virtual camera
        cam = new PeasyCam(this, 0, 0, -800, 800);
        cam.setMinimumDistance(0);
        cam.setMaximumDistance(1200);
        cam.setActive(true);
        this.getSurface().setVisible(false);
    }

  public void draw() {
      background(0);
      Camera kinectRGB = kinectDevice.getCameraRGB();
      Camera kinectDepth = kinectDevice.getCameraDepth();

      IplImage kinectImg = kinectRGB.getIplImage();
      IplImage kinectImgDepth = kinectDepth.getIplImage();

      if(kinectImgDepth == null || kinectImg == null){
          println("no Kinect image");
          return;
      }

      stereoCalib.m03 = kinectStereoValueX;
      stereoCalib.m13 = kinectStereoValueY;
      kinectDevice.setStereoCalibration(stereoCalib);

      if(isCalibrated){
          try{
          kinectAnalysis.update(kinectImgDepth, kinectImg, planeProjCalib, precision);
          }catch(Exception e){
              e.printStackTrace();
          }
      } else {
          kinectAnalysis.update(kinectImgDepth, kinectImg, precision);
      }

      pointCloud.updateWith(kinectAnalysis);
      pointCloud.drawSelf((PGraphicsOpenGL) g);
  }

}
