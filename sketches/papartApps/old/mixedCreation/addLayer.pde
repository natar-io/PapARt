import java.lang.reflect.Constructor;

class AddLayer extends PaperInterface{

    Button addImageButton, addVideoButton, addAppButton, backButton;
    Button validateButton;

    Button activeButton = null;

    static final int SELECT_TYPE = 0;
    static final int ADD_IMAGE = 1;
    static final int ADD_VIDEO = 2;
    static final int ADD_APP = 3;

    int currentMode = SELECT_TYPE;

    String videoFolder;

    PImage[] availableImages;
    Button[] availableImagesButtons;

    String[] availableApps;
    Button[] availableAppsButtons;

    String[] availableVideos;
    Button[] availableVideosButtons;

    public AddLayer(PApplet parent, MarkerBoard board, PVector size, Camera cam, float resolution, Projector proj){
	super(parent, board, size, cam, resolution, proj);
    }

    public void init(){

	//    PImage img = loadImage(sketchPath + "/images/b1.png");
	addImageButton = new Button("Add\nImage", 70 + 25, (int) (65f / 2f), 35, 30);
	addVideoButton = new Button("Add\nVideo", 70 + 70, (int) (65f / 2f), 33, 30);
	addAppButton = new Button("Add\nApp", 70 + 110, (int) (65f / 2f), 25, 30);
	backButton = new Button("Back", 70 + 25, 65 / 2, 25, 30);
	validateButton = new Button("Validate", 70 + 65, 65 / 2, 40, 30);

	buttons.add(addImageButton);
	buttons.add(addVideoButton);
	buttons.add(addAppButton);
	buttons.add(backButton);
	buttons.add(validateButton);
	
	ButtonListener bl = new ButtonListener() {
		public void ButtonPressed(){
		    addImageMode();
		    println("Add Image pressed");
		}
		public void ButtonReleased(){
		    println("Add Image released");
		}
	    };
	addImageButton.addListener(bl);
	
	
	
	addVideoButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    addVideoMode();
		    println("Add Video pressed");
		}
		public void ButtonReleased(){
		    println("Add Video released");
		}
	    });
	    
	addAppButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    addAppMode();
		    println("Add App pressed");
		}
		public void ButtonReleased(){
		    println("Add App released");
		}
	    });
		
	
	
	
	validateButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    validatePressed();
		    println("addLayer :: validate pressed");
		}
		public void ButtonReleased(){
		    
		}
	    });
	
	backButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    backButtonPressed();
		    println("addLayer :: Back pressed");
		}
		public void ButtonReleased(){
		    
		}
	    });
	
	
	float imageSize = 30;
	int imageSizeY = 30;
	float imageDist = 5;

	///////////////////////////////////////////////////		
	//////////////// Available Images /////////////////	
	///////////////////////////////////////////////////		

	File imageFolder = new File(sketchPath + "/images/");
	String[] imageFileList = imageFolder.list();

	availableImages = new PImage[imageFileList.length];
	availableImagesButtons = new Button[imageFileList.length];

	int k = 0;
	for(float y = drawingSize.y - imageSize; 
	    y > 0 ;
	    y -= imageSize + imageDist){
		
	    for(float x = imageSize / 2 ; 
		x < drawingSize.x + imageDist ;
		x += imageSize + imageDist){
		    
		if(k >= availableImages.length)
		    break;
		
		println("Loading image " + imageFileList[k] + " ...");
		availableImages[k] = loadImage(sketchPath + "/images/"+ imageFileList[k]);
		availableImagesButtons[k] = new Button(availableImages[k], imageFileList[k], (int) x, (int) y, (int) imageSize, (int) imageSize);
		buttons.add(availableImagesButtons[k]);
		k++;
	    }
	    if(k >= availableImages.length)
		break;
	}


	///////////////////////////////////////////////////		
	//////////////// Available Videos /////////////////	
	///////////////////////////////////////////////////		


	imageSize = 80;

	videoFolder = sketchPath + "/videos/";
	File videoFolderFile = new File(videoFolder);
	String[] videoFileList = videoFolderFile.list();

	availableVideos = new String[videoFileList.length];
	availableVideosButtons = new Button[videoFileList.length];

	k = 0;
	for(float y = drawingSize.y - imageSizeY; 
	    y > 0 ;
	    y -= imageSizeY + imageDist){
		
	    for(float x = imageSize / 2 ; 
		x < drawingSize.x + imageDist ;
		x += imageSize + imageDist){
		    
		if(k >= availableVideos.length)
		    break;
		
		println("Loading Video " + videoFileList[k] + " ...");
		availableVideosButtons[k] = new Button(videoFileList[k], (int) x, (int) y, (int) imageSize, (int) imageSizeY);
		buttons.add(availableVideosButtons[k]);
		k++;
	    }
	    if(k >= availableVideos.length)
		break;
	}



	///////////////////////////////////////////////////		
	////////////////  Available Apps  /////////////////	
	///////////////////////////////////////////////////		

	// TODO:....

	imageSize = 90;
	imageSizeY = 14;

	availableApps = new String[subSketchList.size()];
	availableAppsButtons = new Button[subSketchList.size()];

	println("Subsketches to load : " + subSketchList.size());

	k = 0;
	for(float y = drawingSize.y - imageSizeY; 
	    y > 0 ;
	    y -= imageSizeY + imageDist){
		
	    for(float x = imageSize / 2 ; 
		x < drawingSize.x + imageDist ;
		x += imageSize + imageDist){
		    
		if(k >= availableApps.length)
		    break;
		
		Class sketch = subSketchList.get(k);
		availableApps[k] = sketch.getName();

		// Class<? extends SubSketch> subSketch = (Class<? extends SubSketch>) sketch;
		// String sketchName = subSketch.getField("sketchName");
		// println("Sketch name " + sketchName);


		println("Loading App" + availableApps[k] + " ...");
		String appName = 	availableApps[k].substring(14);

		availableAppsButtons[k] = new Button(appName, (int) x, (int) y, (int) imageSize, (int) imageSizeY);
		availableAppsButtons[k].attachedObject = subSketchList.get(k);
		buttons.add(availableAppsButtons[k]);

		availableAppsButtons[k].setButtonFontSize(12);

		k++;
	    }
	    if(k >= availableApps.length)
		break;
	}



	selectMode();
    }



    private void backButtonPressed(){

	println("addLayer :: Back Button pressed");

	if(currentMode == ADD_IMAGE){

	    // back to image mode (no more selection) 
	    if(activeButton != null){
		activeButton = null;
		backButton.reset();
		addImageMode();
		return;
	    }

	    // back to select mode
	    selectMode();
	    return;
	}


	if(currentMode == ADD_VIDEO){

	    // back to image mode (no more selection) 
	    if(activeButton != null){
		activeButton = null;
		backButton.reset();
		addVideoMode();
		return;
	    }

	    // back to select mode
	    selectMode();
	    return;
	}


	if(currentMode == ADD_APP){

	    // back to  mode (no more selection) 
	    if(activeButton != null){
		activeButton = null;
		backButton.reset();
		addAppMode();
		return;
	    }

	    // back to select mode
	    selectMode();
	    return;
	}


    }

    private void validatePressed(){

	println("addLayer :: Validate Button pressed");


	if(activeButton != null){


	    if(currentMode == ADD_IMAGE){

		PImage img = activeButton.getImage();
		
		float imRatio = (float) img.height / (float) img.width ;
		
		Layer l = new Layer(parent, img, 
				    new PVector(0, 0),
				    new PVector(50, 50 * imRatio));
		
		l.putImage(activeButton.getImage());
		editLayer.addLayer(l);
		
		hideImageButtons();
		activeButton = null;
		selectMode();
	    }

	    if(currentMode == ADD_VIDEO){

		// float imRatio = (float) movie.height / (float) movie.width ;
		// Layer l = new Layer(parent, movie, 
		// 		    new PVector(0, 0),
		// 		    new PVector(50, 50 * imRatio));
		
		MyMovie myMovie = new MyMovie(videoFolder + activeButton.getName());
		allMovies.add(myMovie);

		// l.putImage(activeButton.getImage());

		// editLayer.addLayer(l);
		// println("Video added ...");
		hideVideoButtons();		
		activeButton = null;
		selectMode();
	    }


	    if(currentMode == ADD_APP){

		
		//		Class<? extends SubSketch> sketch = (Class<? extends SubSketch>)activeButton.attachedObject;
		Class<?> sketch = (Class<?>)activeButton.attachedObject;

		try{

		    //		     Constructor<?>[] constructors = sketch.getConstructors();

		    Constructor<?>[] constructors = sketch.getDeclaredConstructors();
		     println("Constructors number " + constructors.length);
		     for(Constructor<?> c : constructors){
		    	 println(c);
		     }
		    
		    Class cls[] = new Class[] { mixedCreation.class };

		    Constructor c = sketch.getDeclaredConstructor(cls);
		     SubSketch subSketch = (SubSketch) c.newInstance(parent);
		    
		     //		    SubSketch subSketch = (SubSketch) sketch.newInstance();

		    subSketch.setup(parent);
		    subSketches.add(subSketch);
		    
		    
		    editLayer.addLayer(subSketch.layer);
		// } catch(InstantiationException ie){
		//     println("ADD LAYER APP INSTANTIATION ERROR " + ie);
		// } catch(IllegalAccessException iae){
		//     println("ADD LAYER APP ILLEGAL ACCESS ERROR " + iae);
	       	// } catch(NoSuchMethodException nsme){
		//     println("ADD LAYER APP NO SUCH METHOD ERROR " + nsme);
		} catch(Exception e){
		    println("ADD LAYER APP ERROR " + e);
		    e.printStackTrace();
		}

		println("Add App ...");

		hideVideoButtons();		
		activeButton = null;
		selectMode();
	    }

	}

    }

  
    public void addImageMode(){

	currentMode = ADD_IMAGE;
	hideSelectButtons();
	showImageButtons();
	backButton.show();
	validateButton.hide();

    }

    public void addVideoMode(){
	currentMode = ADD_VIDEO;
	hideSelectButtons();
	showVideoButtons();
	backButton.show();
	validateButton.hide();
    }

    public void addAppMode(){
	currentMode = ADD_APP;
	hideSelectButtons();
	showAppButtons();
	backButton.show();
	validateButton.hide();
    }


   public void selectMode(){

       currentMode = SELECT_TYPE;
       hideImageButtons();
       hideVideoButtons();		
       hideAppButtons();

       showSelectButtons();
       backButton.hide();
       
       addImageButton.reset();
       addVideoButton.reset();
       addAppButton.reset();


       println("Hide validate button");
       validateButton.hide();

       activeButton = null;
    }




    public void draw(){

	GLGraphicsOffScreen g = screen.getGraphics();
	g.beginDraw();
	g.clear(0, 0);
	g.scale(resolution);

	for(Button b : buttons){
	    b.drawSelf(g);
	}

	switch(currentMode){
	    
	case SELECT_TYPE :
	    break;
	case ADD_IMAGE :

	    if(activeButton == null){
		for(Button b : availableImagesButtons){
		    b.drawSelf(g);
		    
		    if(b.isActive){

			activeButton = b;
			activeButton.reset();

			hideImageButtons();
			validateButton.show();

			b.show();

			println("Button selected "  + b.getName());
			break;
		    }
		}
	    } else {
		activeButton.drawSelf(g);
	    }

	    break;
	case ADD_VIDEO :

	    if(activeButton == null){
		for(Button b : availableVideosButtons){
		    b.drawSelf(g);
		    
		    if(b.isActive){

			activeButton = b;
			activeButton.reset();

			hideVideoButtons();
			validateButton.show();

			b.show();

			println("Button selected "  + b.getName());
			break;
		    }
		}
	    } else {
		activeButton.drawSelf(g);
	    }

	    break;
	case ADD_APP :

	    if(activeButton == null){
		for(Button b : availableAppsButtons){
		    
		    if(b.isActive){

			activeButton = b;
			activeButton.reset();

			hideAppButtons();
			validateButton.show();

			b.show();

			println("Button selected "  + b.getName());
			break;
		    }
		}
	    } else {
		activeButton.drawSelf(g);
	    }


	    break;
	    
	}


	g.noStroke();
	g.fill(255);
	this.drawTouch(g, 5);

	g.endDraw();
    }


    private void showImageButtons(){
	for(Button b : availableImagesButtons)
	    b.show();
    }
    
    private void hideImageButtons(){
	for(Button b : availableImagesButtons)
	    b.hide();
    }

    private void showVideoButtons(){
	for(Button b : availableVideosButtons)
	    b.show();
    }
    
    private void hideVideoButtons(){
	for(Button b : availableVideosButtons)
	    b.hide();
    }

    private void showAppButtons(){
	for(Button b : availableAppsButtons)
	    b.show();
    }
    
    private void hideAppButtons(){
	for(Button b : availableAppsButtons)
	    b.hide();
    }


    private void hideSelectButtons(){
	addImageButton.hide();
	addVideoButton.hide();
	addAppButton.hide();
    }
    
    private void showSelectButtons(){
	addImageButton.show();
	addVideoButton.show();
	addAppButton.show();	
    }




}


