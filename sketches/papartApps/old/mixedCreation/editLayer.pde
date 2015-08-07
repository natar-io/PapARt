class EditLayer extends PaperInterface{


    Button editLayerButton, unSelectButton, childrenButton;

    public final int NO_MODE = 0;
    public final int EDIT_MODE = 1;
    public final int CHILDREN_MODE = 2;
    

    int currentMode = NO_MODE;

    public Layer currentLayer = null;

    public EditLayer(PApplet parent, MarkerBoard board, PVector size, Camera cam, float resolution, Projector proj){
	super(parent, board, size, cam, resolution, proj);
    }

    public void init(){

	//    PImage img = loadImage(sketchPath + "/images/b1.png");
	unSelectButton = new Button("Back", 70 + 25, (int) (65f / 2f), 33, 30);
	editLayerButton = new Button("Edit", 70 + 70, (int) (65f / 2f), 35, 30);
	childrenButton = new Button("SubLayer", 70 + 110, (int) (65f / 2f), 25, 30);

	buttons.add(unSelectButton);
	buttons.add(editLayerButton);
	buttons.add(childrenButton);

	
	ButtonListener bl = new ButtonListener() {
		public void ButtonPressed(){
		    editMode();
		}
		public void ButtonReleased(){
		    noMode();
		}
	    };
	editLayerButton.addListener(bl);

	bl = new ButtonListener() {
		public void ButtonPressed(){
		    setParentLayer();
		    unSelectButton.reset();
		}
		public void ButtonReleased(){

		}
	    };
	unSelectButton.addListener(bl);


	bl = new ButtonListener() {
		public void ButtonPressed(){
		    childrenMode();
		}
		public void ButtonReleased(){
		    noMode();
		}
	    };
	childrenButton.addListener(bl);



    }



    void noMode(){
	currentMode = NO_MODE;
    }	

    void editMode(){
	currentMode = EDIT_MODE;
    }

    void childrenMode(){
	currentMode = CHILDREN_MODE;

	for(Layer l : currentLayer.getChildren()){
	    l.setDrawBorders(true, 2000);
	}

    }


    public boolean isEditMode(){
	return currentMode == EDIT_MODE;
    }

    public boolean isChildrenMode(){
	return currentMode == CHILDREN_MODE;
    }



    public void setParentLayer(){
	Layer l = currentLayer.getParent();

	if(l == null){
	    // Nothing to do... 
	}else {
	    setCurrentLayer(l);
	}
	
    }

    public void setCurrentLayer(Layer l){
	this.currentLayer = l;
	filters.updateButtonsLayer(l);
    }

    public void addLayer(Layer l){

	if(currentLayer == null)
	    currentLayer = rootLayer;

	currentLayer.addSubLayer(l);
	allLayers.add(l);

	setCurrentLayer(l);
    }

    public Layer getCurrentLayer(){
	return this.currentLayer;
    }


    public void draw(){

	if(currentLayer == null)
	    currentLayer = rootLayer;


	GLGraphicsOffScreen g = screen.getGraphics();
	g.beginDraw();
	g.clear(0, 0);
	g.scale(resolution);


	for(Button b : buttons){
	    b.drawSelf(g);
	}

	g.textFont(font, 20);

	if(currentMode == EDIT_MODE)
	    DrawUtils.drawText(g, "Edit Mode", font, 15, 80, 4);

	if(currentMode == CHILDREN_MODE)
	    DrawUtils.drawText(g, "Sub Mode", font, 15, 80, 4);


	if(currentLayer != rootLayer){
	    g.pushMatrix();
	    g.translate(100, 30 /2f, 0);
	    currentLayer.drawSelfPreview(g, 30, 30);
	    g.popMatrix();
	}	


	// for(Button b : buttons){
	//     b.drawSelf(g);
	// }
	
	g.noStroke();
	g.fill(255);
	this.drawTouch(g, 5);

	g.endDraw();

    }

}

