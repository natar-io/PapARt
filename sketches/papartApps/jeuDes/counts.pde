

int HEART = 0;
int ATTACK = 1;
int POWER = 2;

 Zone[] zones = new Zone[3];
MyCounter counter; 
public class MyCounter  extends PaperTouchScreen {

    int height = 250;
    int width = 120;
    
    void setup() {
	setDrawingSize(width, height);
	loadMarkerBoard(sketchPath + "/data/drawing.cfg", width, height);

	// init all the zones
	zoneWidth = width / 3;
	
	zones[HEART] = new Zone();
	zones[ATTACK] = new Zone();
	zones[POWER] = new Zone();
	counter = this; 
    }
    
    void draw() {
	beginDraw2D();
	background(100, 100, 100);

	updateTouch();
	// reasons ?
	
	for(Zone zone : zones){
	    drawZone(zone); 
	}	

	text("Heart: " + zones[HEART].intensity + ", Attack" + zones[ATTACK].intensity + ", Power " + zones[POWER].intensity,0 ,200);


	int offset = 40;
	int textHeight = 50;
	for(Player player : playerList){

	    text("Player " + player.id + ", " + player.nbPoints +" points",
		 0,
		 offset + player.id * textHeight); 
	}
	
	endDraw();
    }

    void drawZone(Zone zone){
	noStroke();
	fill(zone.zoneCol);
	rect(zone.id *zoneWidth , 0, zoneWidth, height);

	zone.intensity = 0;
	
	for (Touch t : touchList) {

	    PVector p = t.position;
	    fill(200);
	    ellipse(p.x, p.y, 30, 30);

	    if(p.x > zone.beginning() &&
	       p.x < zone.end() ){

		zone.intensity++;
	    }
	    
	}


	
    }

}

int nbZones = 0;
int zoneWidth = 50;

class Zone {

    int zoneCol;
    int id;
    int intensity;

    public Zone(){
	id = nbZones++;
	zoneCol = getColor();
    }

    public int beginning(){
	return id *zoneWidth;
    }
    
    public int end(){
	return (id+1) *zoneWidth;
    }

    
    public int getColor(){
	if(this.id == 0)
	    return #135F67; // teal

	if(this.id == 1)
	    return #571E7C; // purple 

	return #9BA514; // yellow
    }
    
    
}
