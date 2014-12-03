
public class MyApp  extends PaperScreen {

    PShapeOpenGL headModel;

    PShader texlightShader;
    PShader capShader;

    

    void setup(){
	setDrawingSize(297, 210);
	loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);

	//	headModel =  (PShapeOpenGL) loadShape("sphere2/earth.obj");
	headModel =  (PShapeOpenGL) loadShape("sphere/SphereTopUV.obj");

	loadImages();
	loadShaders();
    }


    PImage white;
    PImage rawSignal, meditationMask; 

    void loadImages(){
	rawSignal = loadImage("teegi_caps/scalp_tex_raw.jpg");
	meditationMask = loadImage("teegi_caps/mask_meditation_BW.png");
	white = createImage(1, 1, RGB);
	white.loadPixels();
	white.pixels[0] = color(255);
	white.updatePixels();
    }

    void loadShaders(){
	texlightShader = loadShader("shaders/texlightfrag.glsl", "shaders/texlightvert.glsl");
	capShader = loadShader("shaders/capFrag.glsl", "shaders/capVert.glsl");
	
	//	capShader.set("mask", meditationMask);
    }

    void draw(){
	// background(0);

	beginDraw3D();

	// if(test){
	//     capShader.set("mask", white);
	// } else {
	//     capShader.set("mask", meditationMask);
	// }


	pushMatrix();
	//	shader(capShader);
	translate(drawingSize.x /2,
		  drawingSize.y /2, 
		  170f);

	// 11 cm 
       	scale(105f / 2f);

	//	rotateX(HALF_PI);
	// headModel.drawWithTexture(getGraphics(), rawSignal);
		headModel.draw(getGraphics());
	popMatrix();
	
	endDraw();
    }

}
    
