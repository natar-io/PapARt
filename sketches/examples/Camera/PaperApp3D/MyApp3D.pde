public class MyApp  extends PaperScreen {

    PShape rocketShape;


    void settings(){
        // setDrawAroundPaper(); // no drawing on a 2D virtual screen
        setDrawingSize(297, 210);
        loadMarkerBoard(Papart.markerFolder + "A3-small1.cfg", 297, 210);
    }

    void setup(){
        rocketShape = loadShape("data/rocket.obj");
    }

    void drawAroundPaper(){
        pushMatrix();
        scale(0.5f);
        rotateX(HALF_PI);
        rotateY((float) millis() / 1000f) ;
        try{
        shape(rocketShape);
        } catch(Exception e){
            println("exception " + e);
            e.printStackTrace();
        }
        popMatrix();

        translate(100, 10, 0);
        box(50);
    }
}
