// try out TCPWriteAnalog with a nice GUI
import fr.inria.openvibelink.write.*;
import processing.net.*;



final int TCPOutServerPort = 12345;
// how may channels do you want challenge openvibe with?
final int matrixSize = 4 * 4;
int nbChans = matrixSize + 2;   // 2 is for primary & secondary mode. 
// How many chunks are sent each second ; at least 128 to make openvibe happy (128 / 256 / ... / 4096 )
final int sampleRate = 128;

public class MyApp  extends PaperScreen {

  // TCP server
  WriteAnalog server;
  float[] matrixValues = new float[matrixSize];
  float[] valuesToSend = new float[nbChans];

  PShapeOpenGL headModel;

  PShader texlightShader;
  PShader capShader;

  int visionColor1 = #FF2C2C;
  int visionColor2 = #213DE8;
    
  int rawColor1 = #FF2C2C;
  int rawColor2 = #213DE8;

  int relaxColor1 = #EFF01B;
  int relaxColor2 = #32F01B;

  void setup() {
    setDrawingSize(298, 210);
    loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);

    //	headModel =  (PShapeOpenGL) loadShape("sphere2/earth.obj");
    headModel = (PShapeOpenGL) loadShape("sphere/SphereTopUV.obj");

    loadImages();
    createWhiteMask();
    loadShaders();
    initNetwork();
  }

  void initNetwork() {
    server = new WriteAnalog(parent, TCPOutServerPort, nbChans, sampleRate);
  }

  void draw() {
    // background(0);
    if (noCameraMode) {
      setLocation(390, 85, 0 );
    }
    beginDraw3D();
    updateTexture();
    drawTeegi();

    sendToNetwork();
    endDraw();
  }

  void sendToNetwork() {
    PMatrix3D loc = this.getLocation(); 
    // fill the array to send. 
    loc.get(matrixValues);

    System.arraycopy(matrixValues, 0, valuesToSend, 0, matrixSize);
    valuesToSend[matrixSize] = Mode.asInt();
    valuesToSend[matrixSize + 1] = SecondMode.asInt();

    // send to the server. 
    server.write(matrixValues);
  }

  void drawTeegi() {
    pushMatrix();
    shader(capShader);

    translate(drawingSize.x /2, 
	      drawingSize.y /2, 
	      170f);
    // 11 cm 
    scale(105f / 2f);

    //	rotateX(HALF_PI);
    headModel.drawWithTexture(getGraphics(), rawSignal);
    //		headModel.draw(getGraphics());
    resetShader();
    popMatrix();
  }

  void loadTextureFromFile() {
  }


  PImage whiteMask;
  PImage rawSignal, relaxMask, motorMask, visionMask; 

  void loadImages() {
    rawSignal = loadImage("teegi_caps/scalp_tex_raw-crop.jpg");
    relaxMask = loadImage("teegi_caps/mask_meditation_BW.png");
    motorMask = loadImage("teegi_caps/mask_motor_BW.png");
    visionMask = loadImage("teegi_caps/mask_close_BW.png");
  }

  void createWhiteMask() {
    whiteMask = createImage(1, 1, RGB);
    whiteMask.loadPixels();
    colorMode(RGB, 255);
    whiteMask.pixels[0] = color(255);
    whiteMask.updatePixels();
  }


  void loadShaders() {
    texlightShader = loadShader("shaders/texlightfrag.glsl", "shaders/texlightvert.glsl");
    capShader = loadShader("shaders/capFrag.glsl", "shaders/capVert.glsl");

    colorMode(RGB, 1.0);
    capShader.set("color1", red(visionColor1), green(visionColor1), blue(visionColor1));
    capShader.set("color2", red(visionColor2), green(visionColor2), blue(visionColor2));
    colorMode(RGB, 255);
  }


  void updateTexture() {
    pushStyle();
    if (Mode.is("vision")) {
      setVisionTexture();
    }
    if (Mode.is("relax")) {
      setRelaxTexture();
    }
    if (Mode.is("raw")) {
      setRawTexture();
    }
    popStyle();
  }

  void setVisionTexture() {
    colorMode(RGB, 1.0);

    capShader.set("isRaw", false);
    capShader.set("isWhiteMiddle", true);
    capShader.set("mask", visionMask);
    capShader.set("color1", red(visionColor1), green(visionColor1), blue(visionColor1));
    capShader.set("color2", red(visionColor2), green(visionColor2), blue(visionColor2));
  }

  void setRelaxTexture() {
    colorMode(RGB, 1.0);

    capShader.set("isRaw", false);
    capShader.set("isWhiteMiddle", false);
    capShader.set("mask", relaxMask);
    capShader.set("color1", red(relaxColor1), green(relaxColor1), blue(relaxColor1));
    capShader.set("color2", red(relaxColor2), green(relaxColor2), blue(relaxColor2));
  }

  void setRawTexture() {
    colorMode(RGB, 1.0);

    capShader.set("isRaw", true);
    capShader.set("isWhiteMiddle", true);
    capShader.set("mask", whiteMask);
    capShader.set("color1", red(rawColor1), green(rawColor1), blue(rawColor1));
    capShader.set("color2", red(rawColor2), green(rawColor2), blue(rawColor2));
  }
}
