
import static com.googlecode.javacpp.Loader.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import com.googlecode.javacv.cpp.opencv_core.*;
import com.googlecode.javacv.cpp.opencv_imgproc.*;


class AnimationImage{

    IplImage cameraView = null;
    IplImage backgrIm = null;
    IplImage differenceIm, threshIm, finalIm;
    GLTexture displayTex = null;
    TrackedView trackedView;

    boolean captureBackground = false;
    boolean hasImage = false;
    String name;

    PImage imgDebug;

    public AnimationImage(TrackedView trackedView, String name){
	this.trackedView = trackedView;
	this.name = name;
	imgDebug = createImage((int) viewSize.x, (int) viewSize.y, RGB);

	// CvSize s = new CvSize((int) viewSize.x, (int) viewSize.y);
	// threshIm  = IplImage.create( s, opencv_core.IPL_DEPTH_8U, 1 ); 
	// finalIm   = IplImage.create( s, IPL_DEPTH_8U, 1 ); 

    }


    public void captureBackground(){
	this.captureBackground = true;
    }


    public void updateImage(PApplet parent, Camera cam){

	cameraView  = cam.getView(trackedView);
	if(cameraView == null){
	    println("Frame lost ? " + name);
	    return;
	}

	// Create the texture if not done already.
	if(displayTex == null)
	    displayTex = Utils.createTextureFrom(parent, cameraView);
	
	// send the Image to OpenGL
	Utils.updateTexture(cameraView, displayTex);
	
	if(this.captureBackground){
	    this.captureBackground = false;

	    // save a copy of the image
	    backgrIm = cameraView.clone();
	    differenceIm = backgrIm.clone();
	    threshIm = backgrIm.clone();
	    finalIm = backgrIm.clone();
	}
    }

    
    PImage computeDiff(){

	if(backgrIm == null){
	    System.err.println("AnimationImage Error: No background set." + name);
	    return null;
	}

	cvAbsDiff(cameraView , backgrIm , differenceIm); 
	cvThreshold(differenceIm, threshIm, 40, 255,CV_THRESH_BINARY);

	// minor erosion
	//	cvErode(threshIm,threshIm, null, 2); 

	// Invert the colors in the original image
	//cvNot(cameraView, cameraView);

	// Select the valid area
	cvAnd(cameraView, threshIm, finalIm, null);

	//	cvEqualizeHist(finalIm, finalIm);

	Utils.IplImageToPImage(finalIm, false, imgDebug);
	hasImage = true;

	return imgDebug;

	//	Utils.updateTexture(cameraView, displayTex);
	//	return displayTex;
    }

    boolean hasImage(){
    	return hasImage;
    }

    PImage getImage(){
	return imgDebug;
    }

    IplImage getIplImage(){
	return threshIm;
    }

    boolean hasBackground(){
	return backgrIm != null;
    }


    void saveImage(){

	if(backgrIm != null){

	    println("Saving...");
	    cvSaveImage(sketchPath + "/" +name + "-background.jpg", backgrIm);
	    cvSaveImage(sketchPath + "/" +name + "-image.jpg", finalIm);
	    println("Saved...");

	}

        // displayTex.updateTexture();
	// displayTex.save("capture1.png");
	// println("Image saved");
    }

    void loadBackground(){


    }

    void loadImage(){


    }

}

