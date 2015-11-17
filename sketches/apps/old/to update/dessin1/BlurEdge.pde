
int BLUR_MODE = 1152;


int blurMode = MODE_NONE;

int BLUR_BLUR = 1;
int BLUR_EDGE = 2;
int BLUR_INTENS = 3;
int BLUR_REMOVE = 4;


GLTexture sourceTex, maskTex, outputTex;
GLTextureFilter blurFilter; 


GLGraphicsOffScreen maskOffScreen;

GLTexture[] inputTex;

int blurSceneLoc, blurMaskLoc, blurWHLoc;
float[] wh;
PVector imgSize;

float ellipseSize = 60;

void initBlurMode(){

    sourceTex = new GLTexture(this, sketchPath + "/source.png");
    outputTex = new GLTexture(this, sourceTex.width, sourceTex.height, ARGB);

    //    maskTex = new GLTexture(this, sourceTex.width, sourceTex.height, ARGB);
    maskOffScreen = new GLGraphicsOffScreen(this, sourceTex.width, sourceTex.height);

    imgSize = new PVector(sourceTex.width, sourceTex.height);
    
    // 3 cm ellipse.

    inputTex = new GLTexture[2];
    inputTex[0] = sourceTex;
    //    inputTex[1] = maskTex;
    inputTex[1] = maskOffScreen.getTexture();

    
    blurFilter = new GLTextureFilter(this, sketchPath + "/data/shaders/blur.xml");
    //    blurShader = new GLSLShader(this, "blur.vert", "blur.frag");

    wh = new float[2];
    wh[0] = 1f / sourceTex.width;
    wh[1] = 1f / sourceTex.height;

    blurFilter.setParameterValue("wh", wh);

  // sobelFilter.setParameterValue("wh", pixelSize);
  // sobelFilter.apply(myTexture, dest);

    // blurShader.start();
    // blurSceneLoc = blurShader.getUniformLocation("sceneTex");
    // blurMaskLoc = blurShader.getUniformLocation("mask");
    // blurWHLoc = blurShader.getUniformLocation("wh");
    // blurShader.stop();

}

void enterBlurMode(){
    resetFullNumPad();

}

void leaveBlurMode(){

}


int blurIntens = 200;
int derivSize = 4;
float derivT = 0.5;
float derivTSpeed = 0.05;

void keyBlur(){

    if(key == '.'){
	blurMode = BLUR_REMOVE;
    }

    if(key == '0'){
	blurMode = MODE_NONE;
    }

    if(key == '1'){
	blurMode = BLUR_BLUR;
    }

    if(key == '2'){
	blurMode = BLUR_EDGE;
    }

   if(key == '3'){
       blurMode = BLUR_INTENS;
    }

   if(key == '4'){

       maskOffScreen.beginDraw();
       maskOffScreen.noStroke();
       maskOffScreen.background(0);
       maskOffScreen.endDraw();

   }

    if(blurMode == BLUR_EDGE){
	
	if(key == '+'){
	    derivSize += 1;
	}
	
	if(key == '-'){
	    derivSize -= 1;
	}

	derivSize = constrain(derivSize, 1, 8);
	blurFilter.setParameterValue("derivSize", derivSize);
    }

    if(blurMode == BLUR_BLUR){

	if(key == '+'){
	    derivT += derivTSpeed;
	}
	
	if(key == '-'){
	    derivT -= derivTSpeed;
	}
	println(derivT);
	derivT = constrain(derivT, 0, 1);
	blurFilter.setParameterValue("intensT", derivT);

    }


    if(blurMode == MODE_NONE){
	if(key == '+'){
	    blurIntens += 10;
	}
	
	if(key == '-'){
	    blurIntens -= 10;
	}
    }

}


void touchBlur(TouchElement te){

    if(te.position2D.size() >= 1){
	PVector p0 = te.position2D.get(0);
	p0 = PVector.mult(p0, imgSize);
	maskOffScreen.beginDraw();
	maskOffScreen.noStroke();

	// Red -> Blur
	if(blurMode == BLUR_BLUR){
	    maskOffScreen.fill(255, 0, 0);
	}
	
	// Green -> Edge detection
	if(blurMode == BLUR_EDGE){
	    maskOffScreen.fill(0, 255, 0);
	}


	// Blue -> High intensity
	if(blurMode == BLUR_INTENS){
	    maskOffScreen.fill(0, 0, 255);
	}

	// Black -> no filter
	if(blurMode == BLUR_REMOVE){
	    maskOffScreen.fill(0, 0, 0);	
	}

	maskOffScreen.ellipse(p0.x, imgSize.y -p0.y -30,
			      ellipseSize, ellipseSize);

	maskOffScreen.endDraw();
    }



}


void drawBlur(GLGraphicsOffScreen g){


       if(globalMode == BLUR_MODE){



	g.pushMatrix();
	  g.translate(drawSize.x / 2, drawSize.y / 2);
	  g.scale(1, -1, 1);


	  blurFilter.setParameterValue("wh", wh);	  
	  blurFilter.apply(inputTex, outputTex);

	  g.tint(blurIntens, blurIntens, blurIntens);
	  g.image(outputTex, 0, 0, drawSize.x, drawSize.y);
	  //	  	  g.image(sourceTex, 0, 0, drawSize.x, drawSize.y);
	  
	g.popMatrix();
       }
	//    }

}
