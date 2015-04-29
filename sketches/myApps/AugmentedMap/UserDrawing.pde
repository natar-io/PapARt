public class UserDrawing{
    PImage drawing;
    Location location;
    int zoomLevel;

    UserDrawing(PImage _drawing, Location _location, int _zoomLevel){
	drawing = _drawing;
	location = _location;
	zoomLevel = _zoomLevel;
    }

    UserDrawing(String line){//The string has to be of the shape <nameOfImageFile>/t<locationFirstCoordinate>/t<locationSecondCoordinate>/t<zoomLevel>
	String[] pieces = split(line, '\t');
	if(pieces.length >= 4){
	    drawing = loadImage(sketchPath + "/captures/capture" + pieces[0] + ".png");
	    location = new Location(float(pieces[1]), float(pieces[2]));
	    zoomLevel = int(pieces[3]);
	}
    }

    public PImage getDrawing(){
	return drawing;
    }

    public Location getLocation(){
	return location;
    }

    public int getZoomLevel(){
	return zoomLevel;
    }

    public ScreenPosition getScreenPosition(UnfoldingMap umap){
	return umap.getScreenPosition(location);
    }

    public ScreenPosition getScreenPosition(UnfoldingMap umap, int resolution){
	ScreenPosition result = umap.getScreenPosition(location);
	result.div(resolution);
	return result;
    }

    public ScreenPosition getScreenPosition(MapFactory umaps){
	return umaps.getScreenPosition(location);
    }

    public ScreenPosition getScreenPosition(MapFactory umaps, int resolution){
        return umaps.getScreenPosition(location, resolution);
    }

    /*public void draw(UnfoldingMap umap){
	if(umap.getZoomLevel() != zoomLevel){
	    System.out.println("Not same zoom level");
	    return;
	}
	System.out.println("Drawing");
	ScreenPosition screenPos = umap.getScreenPosition(location);
	//image(drawing, 0, 0);
	imageMode(CENTER);
	image(drawing, screenPos.x, screenPos.y);
	imageMode(CORNER);
	}*/

}
