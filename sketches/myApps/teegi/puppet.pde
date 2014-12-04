import fr.inria.guimodes.*;

public class MyApp  extends PaperScreen {

    PShapeOpenGL headModel;

    PShader texlightShader;
    PShader capShader;

    int visionColor1 = #4EF540;
    int visionColor2 = #224314;

    int rawColor1 = #FF2C2C;
    int rawColor2 = #213DE8;

    int relaxColor1 = #5323DB;
    int relaxColor2 = #A5D3F0;

    void setup(){
	setDrawingSize(297, 210);
	loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);

	//	headModel =  (PShapeOpenGL) loadShape("sphere2/earth.obj");
	headModel = (PShapeOpenGL) loadShape("sphere/SphereTopUV.obj");

	loadImages();
	loadShaders();
	
	Mode.add("vision");
	Mode.add("relax");
	Mode.add("raw");
	Mode.set("raw");
    }



    void draw(){
	// background(0);

	beginDraw3D();

	updateTexture();

	drawTeegi();
	
	endDraw();
    }

    void drawTeegi(){

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
	popMatrix();
    }



    PImage whiteMask;
    PImage rawSignal, relaxMask, motorMask, visionMask; 

    void loadImages(){
	rawSignal = loadImage("teegi_caps/scalp_tex_raw-crop.jpg");
	relaxMask = loadImage("teegi_caps/mask_meditation_BW.png");
	motorMask = loadImage("teegi_caps/mask_motor_BW.png");
	visionMask = loadImage("teegi_caps/mask_close_BW.png");

	whiteMask = createImage(1, 1, RGB);
	whiteMask.loadPixels();
	colorMode(RGB, 255);
	whiteMask.pixels[0] = color(255);
	whiteMask.updatePixels();
    }

    
    void loadShaders(){
	texlightShader = loadShader("shaders/texlightfrag.glsl", "shaders/texlightvert.glsl");
	capShader = loadShader("shaders/capFrag.glsl", "shaders/capVert.glsl");
	
	colorMode(RGB, 1.0);
	capShader.set("color1", red(visionColor1), green(visionColor1), blue(visionColor1));
	capShader.set("color2", red(visionColor2), green(visionColor2), blue(visionColor2));
    }


    void updateTexture(){

	if(Mode.is("vision")){
	    setVisionTexture();
	}
	if(Mode.is("relax")){
	    setRelaxTexture();
	}
	if(Mode.is("raw")){
	    setRawTexture();
	}
    }

    void setVisionTexture(){
	colorMode(RGB, 1.0);

	capShader.set("isRaw", false);
	capShader.set("mask", visionMask);
	capShader.set("color1", red(visionColor1), green(visionColor1), blue(visionColor1));
	capShader.set("color2", red(visionColor2), green(visionColor2), blue(visionColor2));
    }

    void setRelaxTexture(){
	colorMode(RGB, 1.0);

	capShader.set("isRaw", false);
	capShader.set("mask", relaxMask);
	capShader.set("color1", red(relaxColor1), green(relaxColor1), blue(relaxColor1));
	capShader.set("color2", red(relaxColor2), green(relaxColor2), blue(relaxColor2));
    }

    void setRawTexture(){
	colorMode(RGB, 1.0);

	capShader.set("isRaw", true);
	capShader.set("mask", whiteMask);
	capShader.set("color1", red(rawColor1), green(rawColor1), blue(rawColor1));
	capShader.set("color2", red(rawColor2), green(rawColor2), blue(rawColor2));
    }


}
    
