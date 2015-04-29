public class Place{
    Location loc;
    String name;
    PImage img;

    Place(){
    }

    Place(Location _loc, String _name, PImage _img){
	loc = _loc;
	name = _name;
	img = _img;
    }

    public PImage getImage(){
	return img;
    }

    public Location getLocation(){
	return loc;
    }

    public String getName(){
	return name;
    }

    public ScreenPosition getScreenPosition(UnfoldingMap umap){
	return umap.getScreenPosition(loc);
    }

    public ScreenPosition getScreenPosition(UnfoldingMap umap, int resolution){
	ScreenPosition result = umap.getScreenPosition(loc);
	result.div(resolution);
	return result;
    }

    public ScreenPosition getScreenPosition(MapFactory umaps){
	return umaps.getScreenPosition(loc);
    }

    public ScreenPosition getScreenPosition(MapFactory umaps, int resolution){
	return umaps.getScreenPosition(loc, resolution);
    }


}
