void drawPen() {
  stroke(255);
  fill(255);
  touchIDs = new ArrayList();

  if (touchs2D.size()>0) {
    reset_attractors = true;
    awayFromMenu = false;
    for (TouchPoint tp : touchs2D) {
      PVector pos = tp.getPosition();
      // Check if manipulating menu
      if (pos.x*width <350 && pos.y*height > height - 250) {
        awayFromMenu = false;
      } else {
        awayFromMenu = true;
      }
      // Create Attractors
      if (awayFromMenu) {
        for (int i=0; i < attractors.size(); i++) {
          BodyDef bd = attractors.get(i).move(pos.x*width, pos.y*height);
          attractors.get(i).create(bd);
          attractors.get(i).display();
        }
      }
      // Create Surfaces
      if (surfaces.size()>0) {
        boolean foundID = false; // Search for a surface with same touch ID
        for (Surface s : surfaces) { 
          if (s.getID() == tp.getID() && isWall && awayFromMenu) {
            s.addPoint(pos.x*width, pos.y*height);
            foundID = true;
          }
          s.updateChain();
        }
        if (!foundID && isWall && awayFromMenu) { // New Surface
          PVector p = new PVector(tp.getID(), pos.x*width, pos.y*height);
          touchIDs.add(p); // Register point for further surfaces extension
        }
      } else if (isWall && awayFromMenu) { // We add the first surface
        Surface new_s = new Surface(tp.getID());
        new_s.addPoint(pos.x*width, pos.y*height);
        surfaces.addElement(new_s);
      }
      // Update Skatolo
      if (!pointers.contains(tp.getID())) {
        skatolo.addPointer(tp.getID());
        skatolo.updatePointerPress(tp.getID(), true);
        pointers.add(tp.getID());
      } else {
        skatolo.updatePointer(tp.getID(), (int)(pos.x*width), (int)(pos.y*height));
      }
      skatolo.updatePointerPress(tp.getID(), true);
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
    awayFromMenu = false;
  }

  for (int i = 0; i < touchIDs.size(); i++) {
    PVector p = touchIDs.get(i);
    Surface new_s = new Surface(int(p.x));
    new_s.addPoint(p.y, p.z);
    surfaces.addElement(new_s);
    //println(surfaces.size());
  }
}