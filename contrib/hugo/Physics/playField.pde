public class PlayField extends PaperTouchScreen {

  PVector paperSize = new PVector(420, 297);
  TrackedView boardView;
  ColorDetection colorDetectionPaper;
  ColorDetection colorDetectionInk;
  int inkColor = 230;
  //PVector captureSize = new PVector(150, (int) paperSize.y);
  //PVector origin = new PVector(200, (int) paperSize.y/2);
  PVector captureSize = new PVector((int) paperSize.x-50, (int) paperSize.y - 50);
  PVector origin = new PVector(50, 50);
  int picSize = 128;
  boolean isActivated = false;
  int count = 0;

  public void settings() {
    setDrawingSize((int) paperSize.x, (int) paperSize.y);
    loadMarkerBoard(Papart.markerFolder + "A3-small1.svg", (int) paperSize.x, (int) paperSize.y);
  }

  public void setup() {
    // Hardware setup
    play = this;
    pointers = new ArrayList();
    skatolo = new Skatolo(this.parent, this);
    // Physical elements
    boxes = new Vector<Box>();
    walls = new Vector<Boundary>();
    attractors = new Vector<Attractor>();
    surfaces = new Vector<Surface>();
    drawnWalls = new Vector<Boundary>();
    // Color Detection
    boardView = new TrackedView(this);
    boardView.setCaptureSizeMM(captureSize);
    boardView.setImageWidthPx(picSize);
    boardView.setImageHeightPx(picSize);
    boardView.setTopLeftCorner(origin);
    boardView.init();
  }


  public void activatePhysics() {
    // Lock Paper
    lockPosition();
    // Create Walls
    walls.addElement(new Boundary((int) paperSize.x/2, 0, (int) paperSize.x-10, 5, 255, 0));
    walls.addElement(new Boundary((int) paperSize.x/2, (int) paperSize.y, (int) paperSize.x-10, 5, 255, 0));
    walls.addElement(new Boundary(0, (int) paperSize.y/2, 5, (int) paperSize.y-10, 255, 0));
    //walls.addElement(new Boundary((int) paperSize.x, (int) paperSize.y/2, 5, (int) paperSize.y, 255, 0));

    d = new Destructor((int) paperSize.x-50, (int) paperSize.y/2, 50, 50, color(255, 120, 120), 0);

    //Attractors
    for (int i = 0; i<maxPointsPerTouch; i++) {
      Attractor a = new Attractor(10, 0, 0);
      a.kill();
      attractors.addElement(a);
    }
    isActivated = true;
  }

  public void lockPosition() {
    play.saveLocationTo("loc.xml");
    println("Position saved");
    play.getLocation().print();
    play.loadLocationFrom("loc.xml");
  }


  public boolean isCorrectHSB(int colorToCompare, int colorFixed, int threshold) {
    float hueC = hue(colorToCompare);
    float hueF = hue(colorFixed);
    if (hueC > hueF + threshold) return false;
    if (hueC < hueF - threshold) return false;
    if (saturation(colorToCompare) < 45) return false;
    if (brightness(colorToCompare) < 15) return false;
    return true;
  }


  public void drawOnPaper() {   
    background(0);
    if (!isActivated) {
      // Wait for user to lock paper
      rectMode(CENTER);
      rect((int) paperSize.x/2, 0, (int) paperSize.x, 5);
      rect((int) paperSize.x/2, (int) paperSize.y, (int) paperSize.x, 5);
      rect(0, (int) paperSize.y/2, 5, (int) paperSize.y);
      rect((int) paperSize.x, (int) paperSize.y/2, 5, (int) paperSize.y);
    } else {
      // The game starts
      count ++;
      box2d.step();

      // Color Detection
      if (count > 25) {
        for (Boundary b : drawnWalls) {
          b.kill();
        }
        drawnWalls = new Vector<Boundary>();
        stroke(100);
        fill(100, 180, 255, 20);
        strokeWeight(1);
        PImage out = boardView.getViewOf(cameraTracking);
        ellipseMode(CENTER);
        noStroke();
        fill(255, 0, 0);
        colorMode(HSB, 360, 100, 100);
        out.loadPixels();
        for (int x=0; x<out.width-1; x++) {
          for (int y=0; y<out.height-1; y++) {
            int loc = x + y * out.width;
            if (isCorrectHSB(out.pixels[loc], inkColor, 20)) {
              float newx = origin.x + (float(x)/float(out.width))*captureSize.x;
              float newy = origin.y + (float(y)/float(out.height))*captureSize.y;
              Boundary b = new Boundary(newx, newy, 7, 7, 180, 0);
              drawnWalls.addElement(b);
              println(newx, newy);
            }
          }
        }
        out.updatePixels();
        colorMode(RGB, 255, 255, 255);
        count = 0;
      }
      // Add new boxes
      if (count%5==0) {
        boxes.addElement(new Box(10, paperSize.y/2, random(15)+3, random(15)+3, 255, 0));
      }
      //setLocation(63, 45, 0);
      updateTouch();
      touchs2D = touchList.get2DTouchs();
      nTouchs = touchs2D.size();

      //---- Display boxes, walls, attractors and surface ----
      for (Surface s : surfaces) {
        strokeWeight(3);
        stroke(255, 120, 120);
        noFill();
        beginShape();
        for (Vec2 v : s.getPoints()) {
          vertex(v.x, v.y);
        }
        endShape();
      }

      fill(d.fill_c);
      stroke(d.stroke_c);
      rectMode(CENTER);
      rect(d.getXY().x, d.getXY().y, d.getWH().x, d.getWH().y);

      for (Box b : boxes) {
        for (Attractor a : attractors) {
          if (a.isAlive) {
            if (isMagnet) {
              Vec2 force = a.attract(b); 
              b.applyForce(force);
            }
          }
          if (b.isAlive) {
            fill(b.fill_c);
            strokeWeight(1);
            stroke(b.stroke_c);
            rectMode(CENTER);
            Vec2 pos = box2d.getBodyPixelCoord(b.body);
            float angle = b.body.getAngle();
            pushMatrix();
            translate(pos.x, pos.y);
            rotate(-angle);
            rect(0, 0, b.getWH().x, b.getWH().y);
            popMatrix();
          }
        }
        // Kill boxes that are out of the frame
        if (b.isAlive && (b.getXY().y > paperSize.y || b.getXY().x > paperSize.x || b.getXY().x < 0 || b.getXY().y < 0)) {
          b.kill();
        }
      }
      for (Boundary b : walls) {
        fill(b.fill_c);
        stroke(b.stroke_c);
        rectMode(CENTER);
        rect(b.getXY().x, b.getXY().y, b.getWH().x, b.getWH().y);
      }
      skatolo.draw(); 
      drawPen();
      rectMode(CENTER);

      fill(180, 100);
      rect((int) paperSize.x, (int) paperSize.y/2, 5, (int) paperSize.y);
      // ----------------------------
    }
  }


  // Generates Magnets/Walls with finger touch
  void drawPen() {
    stroke(255);
    fill(255);
    touchIDs = new ArrayList();

    if (touchs2D.size()>0) {
      reset_attractors = true;
      for (Touch tp : touchs2D) {
        PVector pos = tp.position;
        for (int i=0; i < attractors.size(); i++) {
          BodyDef bd = attractors.get(i).move(pos.x, pos.y);
          attractors.get(i).create(bd);
          //Display attractors
          Vec2 xy = box2d.getBodyPixelCoord(attractors.get(i).body);
          float a = attractors.get(i).body.getAngle();
          pushMatrix();
          translate(xy.x, xy.y);
          rotate(a);
          if (isMagnet) {
            fill(90, 180, 255, 150);
          } else {
            fill(255, 150, 120, 150);
          }
          strokeWeight(0);
          ellipse(0, 0, attractors.get(i).r*2, attractors.get(i).r*2);
          popMatrix();
        }
        // Create Surfaces
        if (surfaces.size()>0) {
          boolean foundID = false; // Search for a surface with same touch ID
          for (Surface s : surfaces) { 
            if (s.getID() == tp.id && isWall) {
              s.addPoint(pos.x, pos.y);
              foundID = true;
            }
            s.updateChain();
          }
          if (!foundID && isWall) { // New Surface
            PVector p = new PVector(tp.id, pos.x, pos.y);
            touchIDs.add(p); // Register point for further surfaces extension
          }
        } else if (isWall) { // We add the first surface
          Surface new_s = new Surface(tp.id);
          new_s.addPoint(pos.x, pos.y);
          surfaces.addElement(new_s);
        }
        // Update Skatolo
        if (!pointers.contains(tp.id)) {
          skatolo.addPointer(tp.id);
          skatolo.updatePointerPress(tp.id, true);
          pointers.add(tp.id);
        } else {
          skatolo.updatePointer(tp.id, (int)(pos.x), (int)(pos.y));
        }
        skatolo.updatePointerPress(tp.id, true);
      }
    }

    //Reset attractors on touch release
    else if (reset_attractors) {
      for (Attractor a : attractors) {
        if (a.isAlive) a.kill();
      }
      reset_attractors = false;
      // Remove old Skatolo pointers
      for (Integer pointerId : pointers) {
        skatolo.removePointer(pointerId);
      }
      pointers.clear();
    }

    for (int i = 0; i < touchIDs.size(); i++) {
      PVector p = touchIDs.get(i);
      Surface new_s = new Surface(int(p.x));
      new_s.addPoint(p.y, p.z);
      surfaces.addElement(new_s);
    }
  }
}