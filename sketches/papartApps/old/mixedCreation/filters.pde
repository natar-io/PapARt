import fr.inria.papart.graphics2D.*;


class Filters extends PaperInterface{
    

    static final int NO_MODE = 0;
    static final int ADD_MODE = 1;
    static final int EDIT_MODE = 2;
    static final int EDIT_FILTER_MODE = 3;

    int currentMode = NO_MODE;

    ArrayList<LayerFilter> allFilters = new ArrayList<LayerFilter>();
    ArrayList<Button> currentFilterButtons = new ArrayList<Button>();    

    Button[] filterButtons;
    Button leftButton, rightButton, clearButton, fullButton, editButton;
    Button addButton, backButton, validateButton;

    Map<Layer, FilterLayerButtons> filterMap = new HashMap<Layer, FilterLayerButtons>();
    FilterLayerButtons currentButtons = null;

    //////// edit button variables /////////
    FilterLayerArea selectedFla;

    public Filters(PApplet parent, MarkerBoard board, PVector size, Camera cam, float resolution, Projector proj){
	super(parent, board, size, cam, resolution, proj);
    }
    
    
    BlurLF blurLF;
    AlphaLF alphaLF;
    OldFilmLF oldFilmLF;
    
    public void init(){
	
	blurLF = new BlurLF(parent, sketchPath+ "/data/shaders/Filters/");
	alphaLF = new AlphaLF(parent, sketchPath+ "/data/shaders/Filters/");
	oldFilmLF = new OldFilmLF(parent, sketchPath+ "/data/shaders/Filters/");
	
	allFilters.add(blurLF);
	allFilters.add(alphaLF);
	allFilters.add(oldFilmLF);


	leftButton = new Button("Left", 40, 90, 35, 20);
	rightButton = new Button("Right", 70 , 90, 35, 20);
	clearButton = new Button("Clear", 110, 90, 35, 20);
	fullButton = new Button("Full", 150, 90, 35, 20);
	editButton = new Button("Edit", 180, 90, 35, 20);

	addButton = new Button("Add\nFilter", 100, 30, 35, 33);
	backButton = new Button("Back", 70 + 25, 65 / 2, 25, 30);
	validateButton = new Button("Validate", 70 + 65, 65 / 2, 40, 30);

	buttons.add(leftButton);
	buttons.add(rightButton);
	buttons.add(clearButton);
	buttons.add(fullButton);
	buttons.add(editButton);

	buttons.add(addButton);
	buttons.add(backButton);
	buttons.add(validateButton);

	addButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    addMode();
		}
		public void ButtonReleased(){
		}
	    });
	
	validateButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    validatePressed();
		    println("filters :: validate pressed");
		}
		public void ButtonReleased(){
		    
		}
	    });
	
	backButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    backButtonPressed();
		    println("filters :: Back pressed");
		}
		public void ButtonReleased(){
		}
	    });


	
	clearButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    clearButtonPressed();
		    println("filters :: clear pressed");
		}
		public void ButtonReleased(){
		}
	    });


	
	fullButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    fullButtonPressed();
		    println("filters :: full pressed");
		}
		public void ButtonReleased(){
		}
	    });


	editButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    editButtonPressed();
		    println("filters :: edit pressed");
		}
		public void ButtonReleased(){
		}
	    });


	filterButtons = new Button[allFilters.size()];

	// TODO: cleaner for more filters...
	int k = 0;
	for(LayerFilter f : allFilters){
	    Button b = new Button(f.name, 30 + k * 30, 90, 35, 20);
	    filterButtons[k] = b;
	    buttons.add(b);
	    b.attachedObject = f;
	    k++;
	    
	}

	noMode();
    }
    
    

    private void addMode(){

	currentMode = ADD_MODE;
	addButton.hide();
	backButton.show();
	backButton.reset();
	
	//	validateButton.hide();
	showFilterButtons();
	
    }

    private void validatePressed(){

    }

    private void backButtonPressed(){

	if(currentMode == ADD_MODE){
	    noMode();
	    addButton.reset();
	}
    }

    private void clearButtonPressed(){
	if(selectedFla != null){
	    selectedFla.clear(false);
	}

    }

    private void fullButtonPressed(){
	if(selectedFla != null){
	    selectedFla.clear(true);
	}

    }

    private void editButtonPressed(){


	if(currentMode == EDIT_MODE){
	    currentMode = EDIT_FILTER_MODE;
	    leftButton.hide();
	    rightButton.hide();
	    clearButton.hide();
	    fullButton.hide();
	}
	else{
	    currentMode = EDIT_MODE;
	    leftButton.show();
	    rightButton.show();
	    clearButton.show();
	    fullButton.show();
	}
    }

    public FilterLayerArea getEditableArea(){
	if(currentMode == EDIT_FILTER_MODE)
	    return selectedFla;
	else 
	    return null;

    }

    private void noMode(){

	currentMode = NO_MODE;

	backButton.hide();
	validateButton.hide();
	hideEditButtons();
	hideFilterButtons();

	addButton.show();
	showCurrentFilterButtons();
    }

    private void editMode(Button b){

	currentMode = EDIT_MODE;

	addButton.hide();
	hideCurrentFilterButtons();

	selectedFla = (FilterLayerArea) b.attachedObject;
	
	b.show();
	showEditButtons();

    }

    public void updateButtonsLayer(Layer layer){

	FilterLayerButtons flb; 

	// Does not exist, create the flb
	if(!filterMap.containsKey(layer)){

	    flb = new FilterLayerButtons();
	    flb.layer = layer;
	    filterMap.put(layer, flb);

	} else {

	    flb = filterMap.get(layer);

	    if(currentButtons != null)
		for(Button b : flb.filterButtons)
		    b.show();
	}


	if(currentButtons != null)
	    for(Button b : currentButtons.filterButtons)
		b.hide();

	currentButtons = flb;
    }
    
    public void draw(){

	if(currentButtons.filterButtons != null){

	    // TODO: place in Update ?
	    if(currentMode == ADD_MODE){
		for(Button b : this.filterButtons){
		    // A filter is selected		
		    if(b.isActive){
			Layer l = editLayer.getCurrentLayer();
			LayerFilter lf = (LayerFilter) b.attachedObject;
			FilterLayerArea fla = l.addFilter(parent, lf);
			fla.clear(true);
			
			int pos = currentButtons.filterButtons.size() * 40  + 130;
			Button filterButton = new Button(lf.name, pos, 30, 30, 30);
			filterButton.attachedObject = fla;
			buttons.add(filterButton);

			currentButtons.filterButtons.add(filterButton);
			currentButtons.filterLayerArea.add(fla);
			currentButtons.layerFilter.add(lf);

			// currentButtons 
			
			b.reset();
			noMode();
		    }
		}
	    }
	    
	    if(currentMode == NO_MODE){
		for(Button b : currentButtons.filterButtons){
		    // A filter is selected		
		    if(b.isActive){
			editMode(b);
		    }
		}
	    }
	    
	    if(currentMode == EDIT_MODE){
		for(Button b : currentButtons.filterButtons){
		    // A filter is selected		
		    if(!b.isActive){
			noMode();
		    }
		}
	    }
	    
	}

	GLGraphicsOffScreen g = screen.getGraphics();
	g.beginDraw();
	g.clear(0,0);
	g.scale(resolution);
	
	for(Button b : buttons){
	    b.drawSelf(g);
	}

	g.endDraw();

    }




    protected void showFilterButtons(){
	for(Button b : filterButtons)
	    b.show();
    }

    protected void hideFilterButtons(){
	for(Button b : filterButtons)
	    b.hide();
    }


    protected void showCurrentFilterButtons(){
	if(currentButtons != null)	for(Button b : currentButtons.filterButtons)
	    b.show();
    }

    protected void hideCurrentFilterButtons(){
	if(currentButtons != null)	
	    for(Button b : currentButtons.filterButtons)
		b.hide();
    }


    protected void showEditButtons(){
	leftButton.show();
	rightButton.show();
	clearButton.show();
	fullButton.show();
	editButton.show();
    }

    protected void hideEditButtons(){
	leftButton.hide();
	rightButton.hide();
	clearButton.hide();
	fullButton.hide();
	editButton.hide();
    }


}


class FilterLayerButtons{

    Layer layer;
    ArrayList<LayerFilter> layerFilter = new  ArrayList<LayerFilter>();
    ArrayList<FilterLayerArea> filterLayerArea = new ArrayList<FilterLayerArea>();
    ArrayList<Button> filterButtons = new ArrayList<Button>();

}






