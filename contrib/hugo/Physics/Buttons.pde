import fr.inria.skatolo.*;
import fr.inria.skatolo.events.*;
import fr.inria.skatolo.gui.controllers.*;
import fr.inria.skatolo.gui.Pointer;

public class Buttons extends PaperTouchScreen {

  PVector paperSize = new PVector(160, 160);
  Skatolo skatolo;
  ArrayList<Integer> pointers = new ArrayList();


  public void settings() {
    setDrawingSize((int) paperSize.x, (int) paperSize.y);
    loadMarkerBoard(sketchPath() + "/data/physicsInterface.svg", 
      paperSize.x, paperSize.y);
  }

  public void setup() {
    Mode.add("walls");
    Mode.add("clear");
    Mode.add("magnet");
    createButtons();
  }

  void createButtons() {
    int button2PosX = 87;
    int button2PosY = 100;
    int buttonSize = 64;
    skatolo = new Skatolo(this.parent, this);
    skatolo.getMousePointer().disable();
    skatolo.setAutoDraw(false);

    skatolo.addHoverButton("lock")
      .setPosition(0, button2PosY)
      .setSize(buttonSize, buttonSize)
      ;

    skatolo.addHoverButton("walls")
      .setPosition(button2PosX, button2PosY)
      .setSize(buttonSize, buttonSize)
      .hide()
      ;

    skatolo.addHoverButton("magnet")
      .setPosition(0, button2PosY)
      .setSize(buttonSize, buttonSize)
      .hide()
      ;

    skatolo.addHoverButton("clear")
      .setPosition(button2PosX, 0)
      .setSize(buttonSize, buttonSize)
      .hide()
      ;
  }

  public void lock() {
    skatolo.get("lock").hide();
    skatolo.get("magnet").show();
    skatolo.get("clear").show();
    skatolo.get("walls").show();   
    play.activatePhysics();
}

  public void magnet() {
    isWall = false;
    isMagnet = true;
  }

  public void walls() {
    isWall = true;
    isMagnet = false;
  }

  public void clear() {
    for (Surface s : surfaces) {
      if (s.isAlive()) {
        s.getPoints().clear();
        s.kill();
      }
    }
    surfaces = new Vector<Surface>();
  }

  public void drawOnPaper() {
    setLocation(10, 10, 0);
    try {
      background(0);
      updateTouch();
      updateButtons();
      noStroke();
    } 
    catch(Exception e) {
      println("eexception " +e );
      e.printStackTrace();
    }
  }

  public void updateButtons() {
    skatolo.draw();
    SkatoloLink.updateTouch(touchList, skatolo);
  }
}