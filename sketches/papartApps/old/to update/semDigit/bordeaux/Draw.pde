////////// Shadow Map //////////
PVector lightPos = new PVector( -89.39859, -21.532402, 58.48344);
PVector lightPos1 = new PVector( -89.39859, -21.532402, 58.48344);
PVector lightDir = new PVector(0, 0, 0);


boolean debugKinect = false;
boolean isAnaglyph = true;
boolean useSM = false;


// To check ...
PVector lightColor = new PVector(200, 200, 200);

void draw3D(Screen screen, TouchElement te ){

    //    createShadowMap();

    // GLGraphicsOffScreen paperScreen = screen.initDraw(userPos, nearFar.x, nearFar.y);
    // GL gl = paperScreen.beginGL();
    
    // paperScreen.background(100);
    // //	drawScene(paperScreen, gl);
    // paperScreen.endGL();
    // paperScreen.endDraw();


    if(!useStereo){
	GLGraphicsOffScreen paperScreen = screen.initDraw(userPos, nearFar.x, nearFar.y);
	
	GL gl = paperScreen.beginGL();

	drawScene(paperScreen, gl);
	paperScreen.endGL();
	paperScreen.endDraw();
	
	
    } else {
	
	// Left view,  beginDraw in the Initdraw
	GLGraphicsOffScreen paperScreen = screen.initDraw(userPos, nearFar.x , nearFar.y , true, true, true); 
	//	paperScreen.scale(screen.getScale()); 
	GL gl = paperScreen.beginGL();

	// apply the mask
    	gl.glColorMask(false,true, true,true); 
	//    	gl.glColorMask(true,false,false,true); 

	drawScene(paperScreen, gl);

	// Right view, no BeginDraw in the initDraw (last boolean)
	paperScreen = screen.initDraw(userPos, nearFar.x , nearFar.y , true, false, false);
	//	paperScreen.scale(screen.getScale()); 
    	gl.glClear( GL.GL_DEPTH_BUFFER_BIT); 

	//	gl.glColorMask(false, true, true, true); 
	gl.glColorMask(true, false, false, true); 


	drawScene(paperScreen, gl);
    	gl.glColorMask(true, true, true, true);

    	paperScreen.endGL(); 
	paperScreen.endDraw();
    }




    // paperScreen.fill(200);
    // paperScreen.stroke(80, 100, 0);
    // paperScreen.ellipse(150, 100, 30, 20);

    //    paperScreen.box(80);

    
    // if(saveImage){
    // 	lastDrawnImage.copy(paperScreen.getTexture());
    // 	//    lastDrawnImage.setFlippedX(true);
    // 	lastDrawnImage.updateTexture();
    // 	lastDrawnImage.save("projectionDraw.png");
    // 	saveImage = false;
    // }
  
}



public void drawScene(GLGraphicsOffScreen paperScreen, GL gl){


    paperScreen.background(100);

    paperScreen.pushMatrix();
    paperScreen.directionalLight(lightColor.x, lightColor.y, lightColor.z,
				 lightPos.x, 
				 lightPos.y, 
				 lightPos.z);	

    setTransformations(paperScreen);

    PVector center = boundingBox.getCenter();

    if(test){
	paperScreen.translate(-center.x, -center.y, -center.z);
	println("z");
    }
    else
	paperScreen.translate(-center.x, -center.y, 0);
 
    lightShader.start();   	
    gl.glUniform1iARB(texLocation2, 3);
 
 
    // useless (texLocation2)

    model59_80.drawGL(gl, texLocation2);
    model60_80.drawGL(gl, texLocation2);

    lightShader.stop();

    paperScreen.popMatrix();
}


	// SHADOWS ------------------------------------------------

	// if(useSM){
	//   shadowMapShader.start();
	//   gl.glUniform1iARB(shadowLocation, 7);
	//   gl.glUniform1iARB(texLocation, 3);
	//   //	  gl.glUniform1iARB(usingTex, patioTexture == null ? 0 : 1);
	//   gl.glActiveTexture(GL.GL_TEXTURE7);
	//   gl.glBindTexture(GL.GL_TEXTURE_2D, depthScreen.getTexture().getTextureID());
	// }

	//	------------------------------------------------
	//patioModel.render(effect1);

	// TODO: add a floor, or something similar. 

	// if(horseModel != null)
	//   horseModel.drawGL(gl, texLocation);
	// else
	//     if(effect1 == null || effect1 == lightEffect){
	// 	deerModel.render();
	// 	//		System.out.println("Here");
	//     }
	//     else{
	// 	//	System.out.println("There");
	// 	deerModel.render(effect1);
	//     }

	// if(useSM){
	//   shadowMapShader.stop();
	// }











void createShadowMap(){
    GL gl;
    
    useSM = true;
    depthScreen.beginDraw();
    
    float fov = PI/4f;
    PVector camPos;

    if(test)
	camPos = lightPos;
    else
	camPos = lightPos1;

    PVector camDir = lightDir;
    depthScreen.camera(camPos.x, camPos.y, camPos.z + 100,
		       camDir.x, camDir.y, camDir.z,
		       0, 1, 0);
    depthScreen.perspective(fov, 1, 80, 300f);
	
    gl = depthScreen.beginGL();
    gl.glMatrixMode(GL.GL_TEXTURE);
    gl.glActiveTexture(GL.GL_TEXTURE7);

    PMatrix3D bias = new PMatrix3D(
				   0.5f, 0.0f, 0.0f, 0.5f,
				   0.0f, 0.5f, 0.0f, 0.5f,
				   0.0f, 0.0f, 0.5f, 0.5f,
				   0.0f, 0.0f, 0.0f, 1.0f);

    PMatrix3D mat = new PMatrix3D(bias);
    mat.apply(depthScreen.projection);
    mat.apply(depthScreen.modelview);
    mat.transpose();
    gl.glLoadIdentity();
    gl.glLoadMatrixf(mat.get(matF), 0);

    // apply the transformation in GL.GL_TEXTURE7 also

    // back to modelview 
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glCullFace(GL.GL_FRONT);

    drawSceneGeomOnly(depthScreen, gl);

    depthScreen.endGL();
    depthScreen.endDraw();
}



////// Shadow Maps ///////
void drawSceneGeomOnly(GLGraphicsOffScreen paperScreen, GL gl){

    // if(horseModel != null)
    // 	horseModel.drawGL(gl, texLocation2);
    // else
    // 	deerModel.render();
    
}




/////////////// Trash stereo rendering ////////////////  // 

// }else{
	
    // 	// Stereo scene
	
    // 	if(saveImage){
    // 	    paperScreen = screen.initDraw(userPos, nearFar.x * screen.getScale(), nearFar.y * screen.getScale(), true, true, true); 
    // 	    paperScreen.scale(screen.getScale()); 
    // 	    GL gl = paperScreen.beginGL();
	    
    // 	    // draw the scene
    // 	    currentScene.drawSelf(paperScreen, gl);	    
    // 	    paperScreen.endGL();
    // 	    paperScreen.endDraw();

    // 	    lastDrawnImage.copy(paperScreen.getTexture()); //
    // 	    lastDrawnImage.updateTexture();
    // 	    lastDrawnImage.save("projectionDraw-Left.png"); 

    // 	    paperScreen = screen.initDraw(userPos, nearFar.x * screen.getScale(), nearFar.y * screen.getScale(), true, false, true); 
    // 	    paperScreen.scale(screen.getScale()); 
	    
    // 	    // draw the scene
    // 	    gl = paperScreen.beginGL();
    // 	    currentScene.drawSelf(paperScreen, gl);	    
    // 	    paperScreen.endGL(); 
    // 	    paperScreen.endDraw();

    // 	    lastDrawnImage.copy(paperScreen.getTexture()); //
    // 	    lastDrawnImage.updateTexture();
    // 	    lastDrawnImage.save("projectionDraw-Right.png"); 

    // 	    println("Stereo images saved");

    // 	    saveImage = false;
    // 	}

    // 	////////////////////////////////////////////	
    // 	//////////////////// Stereo rendering    ///
    // 	////////////////////////////////////////////	
	
    // 	// Left view,  beginDraw in the Initdraw
    // 	paperScreen = screen.initDraw(userPos, nearFar.x * screen.getScale(), nearFar.y * screen.getScale(), true, true, true); 
    // 	paperScreen.scale(screen.getScale()); 
    // 	GL gl = paperScreen.beginGL();

    // 	// apply the mask
    // 	gl.glColorMask(true,false,false,true); 

    // 	// draw the scene
    // 	currentScene.drawSelf(paperScreen, gl);

    // 	// Right view, no BeginDraw in the initDraw (last boolean)
    // 	paperScreen = screen.initDraw(userPos, nearFar.x * screen.getScale(), nearFar.y * screen.getScale(), true, false, false);
    // 	paperScreen.scale(screen.getScale()); 
    // 	gl.glClear( GL.GL_DEPTH_BUFFER_BIT); 
    // 	gl.glColorMask(false, true, true, true); 

    // 	currentScene.drawSelf(paperScreen, gl);

    // 	gl.glColorMask(true, true, true, true);

    // 	paperScreen.endGL(); 
    // 	paperScreen.endDraw();
    // }

