//
//ArrayList<InteractiveZone> interfaceZones = new ArrayList<InteractiveZone>();
//ArrayList<Drawable> interfaceDrawables = new ArrayList<Drawable>();
//
//PFont interfaceFont;
//
////////////////// INTERFACE MAIN BUTTONS ////////////////////
//static final int MODE_NONE = -1;
//static final int MODE_LETTRE = 0;
//static final int MODE_NOMBRE = 1;
//
//int currentInterfaceMode = MODE_NONE;
//
//float drawEndMinimum = 0;
//
//public void initInterface(){
//
//    DrawUtils.applet = this;
//    interfaceFont = loadFont("URWGothicL-Book-200.vlw");
//
//
//   for(Button b : buttons){
//	b.reset();
//	addInteractiveZone(b);
//    }
//
//    // Set to the *** mode at the beginning
//    // decouverteButton.isActive = true;
//    scene3DButton.isActive = true;
//    // copyrightButton.isActive = true;
//
//}
//
//boolean activeButton(Button b){
//    if(b.isActive){
//	hideAllButtons();
//	retourButton.show();
//	//	b.show();
//	b.isActive = false;
//	return true;
//    }
//    return false;
//}
//
//
//public void addInteractiveZone(InteractiveZone z) {
//    interfaceZones.add(z);
//    interfaceDrawables.add(z);
//}
//
//public void drawInterface(PGraphics3D graphics){
//
//
//
//    graphics.pushStyle();
//    graphics.imageMode(CENTER);
//    graphics.fill(200,0 ,150);
//    //    graphics.stroke(200, 0, 0);
//
//    // TODO: feedback sur les doigts ?
//
//    for (Drawable d : interfaceDrawables)
//	d.drawSelf(graphics);
//    graphics.popStyle();
//    graphics.noTint();
//}
//
//public void observeInput(float x, float y, TouchPoint tp) {
//    for (InteractiveZone z : interfaceZones) {
//	if (z.isSelected(x, y, tp)) {
//	    return;
//	}
//    }
//}
//
//public void unSelect(){
//  for (InteractiveZone z : interfaceZones) {
//      z.isSelected = false;
//  }
//}
