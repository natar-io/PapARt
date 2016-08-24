import fr.inria.skatolo.*;
import fr.inria.skatolo.events.*;
import fr.inria.skatolo.gui.controllers.*;
import fr.inria.skatolo.gui.group.*;

RadioButton r;
Button b;

void initButtons() {
  skatolo.getMousePointer().disable();
  skatolo.setAutoDraw(false);
  r = skatolo.addRadioButton("radioButtons")
    .setNoneSelectedAllowed(false)
    .setPosition(5, height-105)
    .setSize(100, 100)
    .setColorBackground(color(100, 120, 180))
    .setColorForeground(color(100, 120, 180))
    .setColorActive(color(140, 180, 255))
    .setColorLabel(color(0))
    .setItemsPerRow(2)
    .setSpacingColumn(10)
    .addItem(" ", 1)
    .addItem(".", 2)
    ;
  b = skatolo.addButton("resetWalls")
    .setPosition(225, height-105)
    .setColorBackground(color(100, 120, 180))
    .setColorForeground(color(100, 120, 180))
    .setColorActive(color(140, 180, 255))
    .setSize(100, 100)
    .addCallback(  
    new CallbackListener() {
    public void controlEvent(CallbackEvent theEvent) {
      int a = theEvent.getAction(); 
      if (a==skatolo.ACTION_PRESSED) {
        resetWalls();
      }
    }
  }
  );
  b.setLabel(" ");
  Toggle t = r.getItem(0);
  t.activate();
  isWall = false;
  isMagnet = true;
}

void controlEvent(ControlEvent theEvent) {
  println(theEvent.getGroup().id());
  if (theEvent.isFrom(r)) {
    for (int i=0; i<theEvent.getGroup().getArrayValue().length; i++) {
      if (int(theEvent.getGroup().getArrayValue()[i])==01) {
        if (theEvent.getValue()==2.0) {
          isWall = true;
          isMagnet = false;
        } else if (theEvent.getValue()==1.0) {
          isWall = false;
          isMagnet = true;
        }
      }
    }
  }
}

void resetWalls() {
  // Clear all button
  for (Surface s : surfaces) {
    if (s.isAlive()) {
      s.points.clear();
      s.kill();
    }
  }
  surfaces = new Vector<Surface>();
}


void displayLabels() {
  pushMatrix();
  fill(255);
  textSize(20);
  translate(205, height-130);
  rotate(PI);
  text("Walls", 0, 0);
  popMatrix();
  pushMatrix();
  translate(105, height-130);
  rotate(PI);
  text("Magnet", 0, 0);
  popMatrix();
  pushMatrix();
  translate(320, height-130);
  rotate(PI);
  text("Clear", 0, 0);
  popMatrix();
}