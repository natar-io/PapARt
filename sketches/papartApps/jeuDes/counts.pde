

int nbHearts = 0;
int nbAttack = 0;

MyCounter counter; 
public class MyCounter  extends PaperTouchScreen {

    int height = 250;
    int width = 120;
    
    void setup() {
	setDrawingSize(width, height);
	loadMarkerBoard(sketchPath + "/data/drawing.cfg", width, height);

	counter = this; 
    }
    
    void draw() {

	if(noCamera){
	    setLocation(600, 120,0);
	}

	beginDraw2D();
	background(100, 100, 100);

	updateTouch();
	// reasons ?

	drawTouch();
	
	// 	text("Heart: " + zones[HEART].intensity + ", Attack" + zones[ATTACK].intensity + ", Power " + zones[POWER].intensity,0 ,200);


	
	endDraw();
    }

    // void drawZone(Zone zone){
    // 	noStroke();
    // 	fill(zone.zoneCol);
    // 	rect(zone.id *zoneWidth , 0, zoneWidth, height);

    // 	zone.intensity = 0;

    // 	TouchList touch2D = touchList.get2DTouchs();
    // 	for (Touch t : touch2D) {

    // 	    PVector p = t.position;
    // 	    fill(200);
    // 	    ellipse(p.x, p.y, 30, 30);

    // 	    if(p.x > zone.beginning() &&
    // 	       p.x < zone.end() ){

    // 		zone.intensity++;
    // 	    }
	    
    // 	}


}

// int nbZones = 0;
// int zoneWidth = 50;

// class Zone {

//     int zoneCol;
//     int id;
//     int intensity;

//     public Zone(){
// 	id = nbZones++;
// 	zoneCol = getColor();
//     }

//     public int beginning(){
// 	return id *zoneWidth;
//     }
    
//     public int end(){
// 	return (id+1) *zoneWidth;
//     }

    
//     public int getColor(){
// 	if(this.id == 0)
// 	    return #135F67; // teal

// 	if(this.id == 1)
// 	    return #571E7C; // purple 

// 	return #9BA514; // yellow
//     }
    
    
// }
