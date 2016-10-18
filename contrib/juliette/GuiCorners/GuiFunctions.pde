boolean saved = false;

public PVector corners(int id) {
  return image[id];
}

public int currentCorner() {
  return currentPt;
}

void mouseDragged() {
  if (Mode.is("corners"))
    image[currentPt] = new PVector(mouseX, mouseY);
}

int currentPt = 0;

public void moveCornerUp(boolean isUp, float amount) {
  image[currentPt].y += isUp ? -amount : amount;
}

public void moveCornerLeft(boolean isLeft, float amount) {
  image[currentPt].x += isLeft ? -amount : amount;
}

void keyPressed() {

  if (Mode.is("corners")) {
    if (key == '1')
      currentPt = 0;

    if (key == '2')
      currentPt = 1;

    if (key == '3')
      currentPt = 2;

    if (key == '4')
      currentPt = 3;
  }

  if (key == TAB && Mode.is("changeSize")) {
    if (inputHeight.isFocus()) {
      inputWidth.setFocus(true);
      inputHeight.setFocus(false);
    } else if (inputWidth.isFocus()) {
      inputHeight.setFocus(true);
      inputWidth.setFocus(false);
    }
  }

  if (key == CODED) {
    if (keyCode == UP) 
      moveCornerUp(true, 1);

    if (keyCode == DOWN) 
      moveCornerUp(false, 1);

    if (keyCode == LEFT) 
      moveCornerLeft(true, 1);

    if (keyCode == RIGHT) 
      moveCornerLeft(false, 1);
  }
}

// called when the button change mode is pressed
public void confirm() {
  if (Mode.is("corners")) {
    setModeChangeSize();
  } else if (Mode.is("changeSize")) {
    setModeCorners();
  }
}

public void setModeChangeSize() {
  Mode.set("changeSize");
  showInputs();
}

public void setModeCorners() {
  Mode.set("corners");
  hideInputs();
  parseInputs();
}

// parse the textfields
public void parseInputs() {
  try {
    String textWidth = inputWidth.getText();
    if (!textWidth.equals(""))
      objectWidth = Float.parseFloat(textWidth);
  }
  catch(Exception e) {
    inputWidth.clear();
    println("Exception! " +e.getMessage());
  }
  try {
    String textHeight = inputHeight.getText();
    if (!textHeight.equals(""))
      objectHeight = Float.parseFloat(inputHeight.getText());
  }
  catch(Exception e) {
    inputHeight.clear();
    println("Exception! " +e.getMessage());
  }

  object[0].set(0, 0, 0);
  object[1].set(objectWidth, 0, 0);
  object[2].set(objectWidth, objectHeight, 0); 
  object[3].set(0, objectHeight, 0);
}

// reverse the X axis
public void oppositeX() {
  PVector tmp;

  tmp = image[0];
  image[0] = image[1];
  image[1] = tmp;

  tmp = image[3];
  image[3] = image[2];
  image[2] = tmp;
}

// reverse the Y axis
public void oppositeY() {
  PVector tmp;

  tmp = image[0];
  image[0] = image[3];
  image[3] = tmp;

  tmp = image[1];
  image[1] = image[2];
  image[2] = tmp;
}

public void save() {
  papart.saveCalibration("marker.xml", paperCameraTransform);
  println("Saved");
  saved = true;
}