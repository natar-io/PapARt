

PVector drawingBoardSize = new PVector(297, 210);   //  21 * 29.7 cm
PVector drawingBoardSizeHalf = new PVector(297 / 2f, 210 /2f);   

PVector secondaryBoardSize = new PVector(190, 65);

Screen drawingScreen, placementScreen, addLayerScreen, editLayerScreen, filterScreen;

MarkerBoard drawingBoard, placementBoard, addLayerBoard, editLayerBoard, filterBoard;
MarkerBoard[] boards;

TouchElement drawingTouch, addLayerTouch, editLayerTouch, filterTouch, placementTouch;

void initMarkerBoards(){


    String markerBoardFile;

    /////////// Drawing canvas //////////
    drawingBoard = new MarkerBoard(sketchPath + "/data/markers/a3/small/A3-small1.cfg",
				   "drawing", 
				   (int) drawingBoardSize.x, (int)drawingBoardSize.y); 
    
    
    /////////// addLayer board ///////////
    addLayerBoard = new MarkerBoard(sketchPath + "/data/markers/nouveaux/2310.cfg",
				  "drawing", 
				  (int) secondaryBoardSize.x, (int)secondaryBoardSize.y); 

    /////////// EditLayer board ///////////
    editLayerBoard = new MarkerBoard(sketchPath + "/data/markers/nouveaux/2300.cfg",
				  "drawing", 
				  (int) secondaryBoardSize.x, (int)secondaryBoardSize.y); 


    /////////// filter Board ///////////
    filterBoard = new MarkerBoard(sketchPath + "/data/markers/nouveaux/2290.cfg",
				  "drawing", 
				  (int) secondaryBoardSize.x, (int)secondaryBoardSize.y); 

    /////////// placement Board ///////////
    // placementBoard = new MarkerBoard(sketchPath + "/data/markers/nouveaux/2285.cfg",
    // 				     "drawing", 
    // 				     (int) secondaryBoardSize.x, (int)secondaryBoardSize.y); 


    /////////// Active Element Board //////////
    boards = new MarkerBoard[4];
    boards[0] = drawingBoard;
    boards[1] = editLayerBoard;
    boards[2] = addLayerBoard;
    boards[3] = filterBoard;
    //    boards[4] = placementBoard;

}

void setBoardsFiltering(Camera cam){

    // TODO: add filtering to all these boards...

    drawingBoard.setDrawingMode(cameraTracking, true, 8);
    drawingBoard.setFiltering(cameraTracking, 30, 8);

    // editLayerBoard.setDrawingMode(cameraTracking, true, 8);
    // editLayerBoard.setFiltering(cameraTracking, 30, 8);
}

void initScreens(){

    drawingScreen = new Screen(this, drawingBoardSize, screenResolution);   
    drawingScreen.setAutoUpdatePos(cameraTracking, drawingBoard);
    projector.addScreen(drawingScreen);


    // editLayerScreen = new Screen(this, secondaryBoardSize, screenResolution);   
    // editLayerScreen.setAutoUpdatePos(cameraTracking, editLayerBoard);
    // projector.addScreen(editLayerScreen);

    // filterScreen = new Screen(this, secondaryBoardSize, screenResolution);   
    // filterScreen.setAutoUpdatePos(cameraTracking, filterBoard);
    // projector.addScreen(filterScreen);

    // placementScreen = new Screen(this, secondaryBoardSize, screenResolution);   
    // placementScreen.setAutoUpdatePos(cameraTracking, placementBoard);
    // projector.addScreen(placementScreen);

}



void updateScreens(){
    drawingScreen.updatePos();
    // editLayerScreen.updatePos();
    // filterScreen.updatePos();
    // placementScreen.updatePos();
}


void updateTouch(){

    drawingScreen.computeScreenPosTransform();
    drawingTouch = touchInput.projectTouchToScreen(drawingScreen, projector, 
						   true, true); 
    

    // editLayerScreen.computeScreenPosTransform();
    // editLayerTouch = touchInput.projectTouchToScreen(editLayerScreen, projector, 
    // 						  true, true); 


    // filterScreen.computeScreenPosTransform();
    // filterTouch = touchInput.projectTouchToScreen(filterScreen, projector, 
    // 						   true, true); 

    // placementScreen.computeScreenPosTransform();
    // placementTouch = touchInput.projectTouchToScreen(placementScreen, projector, 
    // 						   true, true); 
    

}
