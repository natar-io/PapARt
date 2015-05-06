import de.fhpotsdam.unfolding.mapdisplay.*;
import de.fhpotsdam.unfolding.utils.*;
import de.fhpotsdam.unfolding.marker.*;
import de.fhpotsdam.unfolding.tiles.*;
import de.fhpotsdam.unfolding.interactions.*;
import de.fhpotsdam.unfolding.ui.*;
import de.fhpotsdam.unfolding.*;
import de.fhpotsdam.unfolding.core.*;
import de.fhpotsdam.unfolding.mapdisplay.shaders.*;
import de.fhpotsdam.unfolding.data.*;
import de.fhpotsdam.unfolding.geo.*;
import de.fhpotsdam.unfolding.texture.*;
import de.fhpotsdam.unfolding.events.*;

public class MapFactory{
    UnfoldingMap umap1;
    UnfoldingMap umap2;
    UnfoldingMap umap3;

    PGraphics mapOuter1;
    PGraphics mapOuter2;
    PGraphics mapOuter3;

    MapFactory(){
    }

    MapFactory(PApplet parent){
	setupFactory(1, parent);
    }

    MapFactory(int resolution, PApplet parent){
	setupFactory(resolution, parent);
    }

    public void setupFactory(int resolution, PApplet parent){
	umap1 = new UnfoldingMap(parent, 0, 0, resolution * A4BoardSize.x, resolution * A4BoardSize.y, new Google.GoogleMapProvider());
	umap2 = new UnfoldingMap(parent, 0, 0, resolution * A4BoardSize.x, resolution * A4BoardSize.y, new Microsoft.AerialProvider());
	umap3 = new UnfoldingMap(parent, 0, 0, resolution * A4BoardSize.x, resolution * A4BoardSize.y, new StamenMapProvider.WaterColor());	
	//umap3 = new UnfoldingMap(parent, 0, 0, resolution * A4BoardSize.x, resolution * A4BoardSize.y, new Microsoft.AerialProvider());
	createDefaultEventDispatcher(parent);
    }

    public void createDefaultEventDispatcher(PApplet parent){
	MapUtils.createDefaultEventDispatcher(parent, umap1, umap2, umap3);
    }

    public void zoomAndPanTo(Location loc, int zoomLvl){
	umap1.zoomAndPanTo(loc, zoomLvl);
	umap2.zoomAndPanTo(loc, zoomLvl);
	umap3.zoomAndPanTo(loc, zoomLvl);
    }

    public void setPanningRestriction(Location loc, float dist){
	umap1.setPanningRestriction(loc, dist);
	umap2.setPanningRestriction(loc, dist);
	umap3.setPanningRestriction(loc, dist);
    }

    public UnfoldingMap getMap1(){
	return umap1;
    }

    public UnfoldingMap getMap2(){
	return umap2;
    }

    public UnfoldingMap getMap3(){
	return umap3;
    }

    public PGraphics getMapOuter1(){
	return mapOuter1;
    }

    public PGraphics getMapOuter2(){
	return mapOuter2;
    }

    public PGraphics getMapOuter3(){
	return mapOuter3;
    }

    public Location getCenter(){
	return umap1.getCenter();
    }

    public int getZoomLevel(){
	return umap1.getZoomLevel();
    }

    public Location getLocation(ScreenPosition screenPos){
	return umap1.getLocation(screenPos);
    }

    public Location getLocation(PVector vecPos){
	return umap1.getLocation(new ScreenPosition(vecPos.x, vecPos.y));
    }

    public Location getLocation(PVector vecPos, int resolution){
	ScreenPosition screenPos = new ScreenPosition(vecPos.x, vecPos.y);
	screenPos.mult(resolution);
	return umap1.getLocation(screenPos);
    }

    public ScreenPosition getScreenPosition(Location loc){
	return umap1.getScreenPosition(loc);
    }

    public ScreenPosition getScreenPosition(Location loc, int resolution){
	ScreenPosition result = umap1.getScreenPosition(loc);
	result.div(resolution);
	return result;
    }

    public void draw(){
	umap1.draw();
	OpenGLMapDisplay curMapDisplay = (OpenGLMapDisplay) umap1.mapDisplay;
	mapOuter1 = curMapDisplay.getOuterPG();
	umap2.draw();
	curMapDisplay = (OpenGLMapDisplay) umap2.mapDisplay;
	mapOuter2 = curMapDisplay.getOuterPG();
	umap3.draw();
	curMapDisplay = (OpenGLMapDisplay) umap3.mapDisplay;
	mapOuter3 = curMapDisplay.getOuterPG();
    }

}
