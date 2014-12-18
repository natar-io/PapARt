import fr.inria.openvibelink.read.*;
import processing.net.*; 


final String TCPServerIP = "localhost";
final int TCPInServerPort = 5678;


public class AmbientFeedback  extends PaperScreen {

  ReadAnalog myClient; 
  PShader pixelize, waves, white_noise;
  int noiseLevel = 0;
  PGraphics feedback, scene;

  int ambientWidth = 350;
  int ambientHeight = 250;

  void setup() {
    setDrawingSize(ambientWidth, ambientHeight);
    loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", ambientWidth, ambientHeight);

    initShaders();
    loadSecondMode();
    initNetwork();

    // we need a dedicated renderer for the waves in order to apply pixelize and noise only on it
    feedback = createGraphics(ambientWidth, ambientHeight, P2D); 
    scene = createGraphics(ambientWidth, ambientHeight, P2D);
  }

  void initNetwork() {
    // init client, first attempt to connect
    myClient = new ReadAnalog(parent, TCPServerIP, TCPInServerPort);
  }



  void initShaders() {
    pixelize =    loadShader("shaders/pixelize.glsl");
    waves =       loadShader("shaders/waves.glsl");
    white_noise = loadShader("shaders/white_noise.glsl"); 

    pixelize.set(   "iResolution", float(ambientWidth), float(ambientHeight)); 
    waves.set(      "iResolution", float(ambientWidth), float(ambientHeight));
    white_noise.set("iResolution", float(ambientWidth), float(ambientHeight));
  }

  void loadSecondMode() {
    SecondMode.add("clear");
    SecondMode.add("waves");
    SecondMode.add("pixelate");
    SecondMode.add("noise");
    SecondMode.set("waves");
  }

  void draw() {

    if (SecondMode.is("clear")) {
      return;
    }

    if (noCameraMode) {
      setLocation(50, 300, 0 );
    }

  //  updateNetwork();

    beginDraw3D();
    updateShaders();
    drawFeedback();
    drawScene();

    image(scene, 0, 0);

    endDraw();
  }

  void updateNetwork() {
    double[][] data = myClient.read();
    if (data == null) {
      //  println("Waiting for data...");
    }
    // nice output to stdout
    else {
      int nbChans = myClient.getNbChans();
      int chunkSize = myClient.getChunkSize();
      println("Read " + chunkSize + " samples from " + nbChans + " channels:");
      // I can fill... can you fill it?
      for (int i=0; i < nbChans; i++) {
        for (int j=0; j < chunkSize; j++) {
          print(data[i][j] + "\t");
          // Do something with this data !!!!  
          //  like ?    SecondMode.set("...");
        }
        println();
      }
    }
  }


  void updateShaders() {
    waves.set("iGlobalTime", millis()/1000.0);
    white_noise.set("iGlobalTime", millis()/1000.0);
  }

  void drawFeedback() {

    // waves as muscular feedback
    feedback.beginDraw();
    feedback.filter(waves);

    if (SecondMode.is("pixelate")) {
      feedback.filter(pixelize);
    } else {

      if (SecondMode.is("noise")) {
        feedback.filter(white_noise);
        feedback.filter(pixelize);
      }
    }

    feedback.endDraw();
  }

  void drawScene() {
    // moving shape in the foreground
    scene.beginDraw();
    scene.background(0, 0, 0);
    scene.rect(23, 40, 100, 150);
    // the waves will show up on everything that's placed before (even white colors)
    scene.image(feedback, 0, 0);
    scene.ellipse(mouseX, mouseY, 100, 100);
    scene.endDraw();
  }
}
