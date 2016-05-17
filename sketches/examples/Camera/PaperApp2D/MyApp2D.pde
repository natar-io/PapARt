public class MyApp extends PaperScreen {

    Particle[] particles;
    int am = 1000;

    void settings(){
        setDrawingSize(1000, 500);
        loadMarkerBoard(Papart.markerFolder + "tableauInterieur.jpg", 990, 490);

        // setDrawingSize(297, 210);
        // loadMarkerBoard(Papart.markerFolder + "A3-small1.svg", 297, 210);
        setDrawOnPaper();
    }

    void setup() {
        setDrawingFilter(100);

        particles = new Particle[am];
        for (int i = 0; i < am; i++) {
            particles[i] = new Particle(new PVector(0, 0), i);
        }

    }

    void drawOnPaper(){
        background(mouseX);

        fill(0, 100, 0);
        rect(0, 0, 100, 100);

        try{
        translate(350, 110);
        for (int i = 0; i < am; i++) {
            particles[i].draw(getGraphics());
        }
        
         for (int i = 0; i < am; i++) {
             particles[i].drawMid(getGraphics());
             particles[i].move();
         }

        }catch(Exception e){
            e.printStackTrace();
        }


// rect(0, 0, drawingSize.x /2, drawingSize.y/2);
        // fill(200, 100, 20);
        // translate(0, 0, 1);
        // rect(10, 10, 100, 30);
    }


    class Particle {
        PVector loc, vel;
        float a, s, radius;

        Particle(PVector l, float i) {
            loc = l;
            a = i;
            s = random(200, 250);
        }

        void draw(PGraphicsOpenGL g) {
            //Change the size of the particle in relation to its distance from the center.
            radius = map(dist(loc.x, loc.y, 0, 0), 0, 150, 30, 1);
             g.fill(0, 150, 255);
             g.ellipse(loc.x, loc.y, radius, radius);
        }

        void drawMid(PGraphicsOpenGL g) {
            g.fill(40);
            g.ellipse(loc.x, loc.y, radius-4, radius-4);
        }

        void move() {
            float r = sin(radians(frameCount*(s/200)));
            vel = new PVector(sin(radians(a))*r, cos(radians(a))*r);
            vel.mult(1.2);
            loc.add(vel);
        }
    }


}