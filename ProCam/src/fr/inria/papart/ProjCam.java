package fr.inria.papart;


import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLSLShader;
import codeanticode.glgraphics.GLTexture;
import codeanticode.glgraphics.GLTextureFilter;
import com.googlecode.javacv.CameraDevice;
import com.googlecode.javacv.ProjectorDevice;
import com.googlecode.javacv.processing.ARTagDetector;
import javax.media.opengl.GL;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Matrix4x4;
import toxi.processing.ToxiclibsSupport;



class ProjCam{


    protected int posX, posY, frameWidth, frameHeight;
    protected int vw, vh;

    public GLGraphicsOffScreen graphics;
    public Screen Screen, userInterfaceScreen;
    public Screen[] screens;

    protected GLSLShader shader;
    protected GLTextureFilter lensFilter;
    protected GLTexture tex2;

    protected ToxiclibsSupport screenGFX;
    public ARTagDetector art;
    protected ProjectorDevice proj;
    protected CameraDevice cameraDevice;

    protected PMatrix3D projExtrinsicsP3D, projExtrinsicsP3DInv;
    protected PMatrix3D projIntrinsicsP3D, camIntrinsicsP3D;

    protected ARTThread artThread;

    public float[] projectionMatrixGL = new float[16];
    protected GLTexture myMap;


    protected Matrix4x4 transformationRender1Render2;
    protected PMatrix3D projectionInit, projectionInitCam;

    private GL gl = null;
    //    private PApplet parent;
    public PApplet parent;
    
    public float offscreenScale;
    PVector offscreenSize; 
    //    protected boolean useGSVideo;

    public ProjCam(PApplet parent, int camNo, 
		   int x, int y,
		   int width, int height, 
		   int vw, int vh,
		   String calibrationYAML, String calibrationData,
		   float offscreenScale, boolean gsvideo,
            boolean useAA, int antiAliasing, 
            String[] boards,
            int paperSheetWidth, 
            int paperSheetHeight,
            int interfaceSheetWidth,
            int interfaceSheetHeight){

	this.offscreenScale = offscreenScale;
	posX = x;
	posY = y;
	frameWidth = width;
	frameHeight = height;
	this.parent = parent;

	offscreenSize = new PVector(paperSheetWidth * offscreenScale, paperSheetHeight * offscreenScale);
	graphics = new GLGraphicsOffScreen(parent, width, height, useAA, antiAliasing);


	Screen    = new Screen(parent, this,
						   new PVector(paperSheetWidth, paperSheetHeight),
						   offscreenScale, useAA, antiAliasing);
	userInterfaceScreen = new Screen(parent, this,
						   new PVector(interfaceSheetWidth, interfaceSheetHeight),
						   offscreenScale, useAA, antiAliasing);
	screens = new Screen[2];
	screens[0] = Screen;
	screens[1] = userInterfaceScreen;

	screenGFX = new ToxiclibsSupport(parent, Screen.thisGraphics);

	// TODO: fix GSVideo
	//	this.useGSVideo = gsvideo;
	// if(gsvideo){ // GSTREAMER
	//     art = new ARTagDetector(vw, vh, calibrationYAML,
	// 			    calibrationData,
	// 			    calibrationBoard);	    
	//     video = new GSCapture(parent, vw, vh, 60);
	//     video.play();
	//     artThread = new ARTThread(art, this, video);
	// }
	// else{


	art = new ARTagDetector(camNo, vw, vh, 60, calibrationYAML,
				calibrationData,
				boards);
	//				new String[]() {calibrationBoard, calibrationBoard2});
	artThread = new ARTThread(art, this, screens);
	artThread.start();


	// TODO: new !!
	//	interfaceScreen = new new GLGraphicsOffScreen(parent, 400, 400, useAA, antiAliasing);


	lensFilter = new GLTextureFilter(parent, "projDistort.xml");
	tex2 = new GLTexture(parent, width, height);

	//////// Augmented Reality initialization ///////////
	try {
	    com.googlecode.javacv.processing.Utils.
		convertARParam(parent, calibrationYAML, calibrationData, vw, vh);
	    //    com.googlecode.javacv.processing.Utils.convertProjParam(parent, calibrationYAML, calibrationDATA1, 
	    // frameSizeX, frameHeight);
	}catch (Exception e){
	    System.out.println("Conversion error. " + e);
	}
	
	// Load the camera parameters. 
	try{
	    
	    CameraDevice[] camDev = CameraDevice.read(calibrationYAML);
	    if (camDev.length > 0) 
		cameraDevice = camDev[0];
	    ProjectorDevice[] p = ProjectorDevice.read(calibrationYAML);
	    if (p.length > 0) 
		proj = p[0];

	    double[] projMat = proj.cameraMatrix.get();
	    double[] projR = proj.R.get();
	    double[] projT = proj.T.get();
	    projIntrinsicsP3D = new PMatrix3D((float) projMat[0], (float) projMat[1], (float) projMat[2], 0,
					      (float) projMat[3], (float) projMat[4], (float) projMat[5], 0,
					      (float) projMat[6], (float) projMat[7], (float) projMat[8], 0,
					      0, 0, 0, 1);
	    projExtrinsicsP3D = new PMatrix3D((float) projR[0], (float) projR[1], (float) projR[2], (float) projT[0],
					      (float) projR[3], (float) projR[4], (float) projR[5], (float) projT[1], 
					      (float) projR[6], (float) projR[7], (float) projR[8], (float) projT[2],
					      0, 0, 0, 1);

	    projExtrinsicsP3DInv = projExtrinsicsP3D.get();
	    projExtrinsicsP3DInv.invert();

	    double[] camMat = cameraDevice.cameraMatrix.get();

	    camIntrinsicsP3D = new PMatrix3D((float) camMat[0], (float) camMat[1], (float) camMat[2], 0,
					      (float) camMat[3], (float) camMat[4], (float) camMat[5], 0,
					      (float) camMat[6], (float) camMat[7], (float) camMat[8], 0,
					      0, 0, 0, 1);

	}  catch(Exception e){ 
	    parent.die("Error reading the calibration file : " + calibrationYAML + " \n" + e);
	}
    


	float p00, p11, p02, p12;



	/////////////////////// Point projection parameters /////////////////////
	// ----------- OPENGL --------------
	// p00 = 2*projIntrinsicsP3D.m00 / vw ;
	// p11 = 2*projIntrinsicsP3D.m11 / vh ;
	// p02 = -(2*projIntrinsicsP3D.m02 / vw  -1);
	// p12 = -(2*projIntrinsicsP3D.m12 / vh -1);

	// graphics.beginDraw();

	// // frustum only for near and far...
	// graphics.frustum(0, 0, 0, 0, 200 , 2000);
	// //	graphics.frustum(-width/2, width, 0, height, -100, 200.0);
	// graphics.projection.m00 = p00;
	// graphics.projection.m11 = p11;
	// graphics.projection.m02 = p02;
	// graphics.projection.m12 = p12;

	// projectionInitCam = graphics.projection.get();


	/////////////// Rendering parameters /////////////////////



	// ----------- OPENGL --------------
	p00 = 2*projIntrinsicsP3D.m00 / frameWidth ;
	p11 = 2*projIntrinsicsP3D.m11 / frameHeight ;
	p02 = -(2*projIntrinsicsP3D.m02 / frameWidth  -1);
	p12 = -(2*projIntrinsicsP3D.m12 / frameHeight -1);

	graphics.beginDraw();

	// frustum only for near and far...
	graphics.frustum(0, 0, 0, 0, 200 , 2000);
	//	graphics.frustum(-width/2, width, 0, height, -100, 200.0);
	graphics.projection.m00 = p00;
	graphics.projection.m11 = p11;
	graphics.projection.m02 = p02;
	graphics.projection.m12 = p12;

	projectionInit = graphics.projection.get();


	graphics.projection.transpose();
	graphics.projection.get(projectionMatrixGL);
	graphics.projection.transpose();
	graphics.endDraw();

	// TODO: shader useless now ?
	shader = new GLSLShader(parent, "distortVert.glsl", "distortFrag.glsl");
	initDistortMap(proj);



//	println("ProjCam init OK");
    }

    /**
     * This function initializes the distorsion map used by the distorsion shader. 
     * The texture is of the size of the projector resolution.
     * @param proj 
     */
    private void initDistortMap(ProjectorDevice proj){
	myMap = new GLTexture(parent, frameWidth, frameHeight, GLTexture.FLOAT);
	float[] mapTmp = new float[ frameWidth *  frameHeight *3];
	int k =0;
	for(int y=0; y < frameHeight ; y++){
	    for(int x=0; x < frameWidth ; x++){
		
		double[] out = proj.undistort(x,y);
		mapTmp[k++] = (float) out[0] / frameWidth;
		mapTmp[k++] = (float) out[1] / frameHeight;
		mapTmp[k++] = 0;
	    }
	}
	myMap.putBuffer(mapTmp, 3);
    }
    
    
    // TODO: remove this from here ?
    public PVector getCamViewPoint(PVector pt){
      PVector tmp = new PVector();
      camIntrinsicsP3D.mult(new PVector(pt.x, pt.y, pt.z), tmp);
      return new PVector(tmp.x / tmp.z, tmp.y / tmp.z);
    }

    
    public boolean isReady(){
    	// if(useGSVideo){
    	//   if (!video.available()) {
    	//     return;
    	//   }
    	//   video.read();
    	//   pos3D = art.findMarkers(video);
    	// }else{
	return true;
	//    	return !(pos == null);
    }

    public void initProjection(){
	gl = graphics.beginGL();
	gl.glMatrixMode(GL.GL_PROJECTION);
	gl.glLoadMatrixf(projectionMatrixGL, 0);
	gl.glMatrixMode(GL.GL_MODELVIEW);
	graphics.endGL();
    }


    public void distortImageDraw(){
	GLTexture off = graphics.getTexture();

	////////  3D PROJECTION  //////////////
	graphics.beginDraw();
	graphics.clear(0);
	initProjection();
	graphics.resetMatrix();

	// Setting the camera
	graphics.scale(1, 1, -1);
	graphics.modelview.apply(projExtrinsicsP3D);
    
	// TODO: disable depth test ?
	// Setting the scene
	for(Screen screen: screens){
	    //	    GLTexture off2 = shadowMapScreen.getTexture();
	    GLTexture off2 = screen.getTexture();
	    graphics.pushMatrix();
	    graphics.modelview.apply(screen.pos); 
	    graphics.image(off2, 0, 0, screen.size.x, screen.size.y);	    
	    graphics.popMatrix();
	}

	// // Draw the paperSheet
	// OLD VERSION
	// graphics.modelview.apply(pos); 
	// graphics.image(off2, 0, 0, paperSheetWidth, paperSheetHeight);

	graphics.endDraw();

	// DISTORTION SHADER
	off = graphics.getTexture();
	lensFilter.apply(new GLTexture[]{off, myMap}, tex2);
	parent.image(tex2, posX, posY, frameWidth, frameHeight);
    }


    public PVector computePosOnPaper(PVector position){
	  graphics.pushMatrix();
	  PVector ret = new PVector();   
	  graphics.translate(position.x, position.y, position.z);
	  projExtrinsicsP3DInv.mult(new PVector(graphics.modelview.m03, 
						graphics.modelview.m13, 
						-graphics.modelview.m23),
				    ret);   
	  graphics.popMatrix();
	  return ret;
    }

   
    public PMatrix3D modelview1;
    public void prepare(){

	//	artThread.compute();
	////////  3D PROJECTION  //////////////
	graphics.beginDraw();
	graphics.clear(0);
	initProjection();
	graphics.resetMatrix();

	// Setting the camera
	graphics.scale(1, 1, -1);
	graphics.modelview.apply(projExtrinsicsP3D);
	modelview1 = graphics.modelview.get();

	for(Screen screen: screens){
	    screen.initTouch(this);
	}
	graphics.endDraw();
   }




    public PImage getCameraImage(){
	return art.getImage();
    }

    public void close(){
	artThread.stopThread();
	art.close();
    }

    ///////////// CUSTOM CALLS /////////////////


    // use  prepareGraphics before !!!
    public GLGraphicsOffScreen startPaperScreen(){
	Screen.initDraw();
	return Screen.thisGraphics;
    }

    public GLGraphicsOffScreen startPaperScreenLeft(){
	Screen.initDrawLeft();
	return Screen.thisGraphics;
    }
    public GLGraphicsOffScreen startPaperScreenRight(){
	Screen.initDrawRight();
	return Screen.thisGraphics;
    }

    public GLGraphicsOffScreen startPaperScreenRightOnly(){
	Screen.initDrawRightOnly();
	return Screen.thisGraphics;
    }

    public GLGraphicsOffScreen startInterfaceScreen(){
	userInterfaceScreen.initDraw();
	return userInterfaceScreen.thisGraphics;
    }
    public ToxiclibsSupport getToxi(){
	return screenGFX;
    }

    GLGraphicsOffScreen getGraphics() {
        return this.graphics;
    }

    PMatrix3D getModelview1() {
        return this.modelview1;
    }

    PMatrix3D getProjectionInit() {
        return this.projectionInit;
    }

    ProjectorDevice getProjectorDevice() {
        return this.proj;
    }


}
