
public class MyApp  extends PaperScreen {

    PShapeOpenGL headModel;

    PShader texlightShader;
    PShader capShader;

    int visionColor1 = #4EF540;
    int visionColor2 = #224314;

    void setup(){
	setDrawingSize(297, 210);
	loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);

	//	headModel =  (PShapeOpenGL) loadShape("sphere2/earth.obj");
	headModel =  (PShapeOpenGL) loadShape("sphere/SphereTopUV.obj");

	loadImages();
	loadShaders();
    }


    PImage white;
    PImage rawSignal, meditationMask, motorMask, eyeMask; 

    void loadImages(){
	rawSignal = loadImage("teegi_caps/scalp_tex_raw-crop.jpg");
	meditationMask = loadImage("teegi_caps/mask_meditation_BW.png");
	motorMask = loadImage("teegi_caps/mask_motor_BW.png");
	eyeMask = loadImage("teegi_caps/mask_close_BW.png");

	white = createImage(1, 1, RGB);
	white.loadPixels();
	colorMode(RGB, 255);
	white.pixels[0] = color(255);
	white.updatePixels();
    }

    void loadShaders(){
	texlightShader = loadShader("shaders/texlightfrag.glsl", "shaders/texlightvert.glsl");
	capShader = loadShader("shaders/capFrag.glsl", "shaders/capVert.glsl");
	

	capShader.set("mask", meditationMask);

	colorMode(RGB, 1.0);
	capShader.set("color1", red(visionColor1), green(visionColor1), blue(visionColor1));
	capShader.set("color2", red(visionColor2), green(visionColor2), blue(visionColor2));
    }

    void draw(){
	// background(0);

	beginDraw3D();

	//  Raw en fond 
	// plus vision ?

	if(test){
	    capShader.set("mask", white);
	} else {
	    capShader.set("mask", meditationMask);
	}


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
	
	endDraw();
    }

}
    
