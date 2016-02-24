import fr.inria.papart.multitouch.*;

public class MyApp extends PaperTouchScreen {

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

        // fill(0, 100, 0);
        // rect(0, 0, 100, 100);

        // fill(#407DDB);
        // TouchList touchList = getTouchList().get2DTouchs();
        // for(Touch touch : touchList){
        //     ellipse(touch.position.x, touch.position.y, 50, 50);
        // }
        // http://www.openprocessing.org/sketch/301463
        pushStyle();
        pushMatrix();
        translate(600, 150);
        scale(0.4f);
        for(int i = 0; i < 360; i+=75){
            float x = sin(radians(i))*90;
            float y = cos(radians(i))*90;
            for(int q = 1; q < 5; q++){
                float s = q*45;
                strokeWeight(map(q, 1, 5, 5, 20));
                stroke(101, 9, 204, 60);
                arc(x, y, s, s, -radians(i+frameCount*2), -radians(i+frameCount*2)+PI);
                strokeWeight(1);
                stroke(255);
                arc(x, y, s, s, -radians(i+frameCount*2), -radians(i+frameCount*2)+PI);
            }
        }
        popStyle();

        popMatrix();

        pushMatrix();
        translate(380, 150);
        scale(0.7f);
        // http://www.openprocessing.org/sketch/270998
        for (int i = 0; i < am; i++) {
            particles[i].draw(getGraphics());
        }

         for (int i = 0; i < am; i++) {
             particles[i].drawMid(getGraphics());
             particles[i].move();
         }

         popMatrix();


         // http://www.openprocessing.org/sketch/41207


         pushStyle();
         stroke(1,111);
         fill(1,111);

         float wave = 75;

         float time = 0;
         int w = (int) 1000;
         int h = (int) 200;

         float x =0;
         pushMatrix();
         translate(0, drawingSize.y - 200);

         while (x<w){
             PVector raw = noisek(x/100,time);

             float k =wave *raw.z;
             // wave*noise(x/100,time);
             noStroke();
             ellipse(x, h /2 +(k),3,3);

             strokeWeight(1);

             stroke(0,0,255,164-(wave*raw.z));
             line(x, h/2 +(k),x-2,h);

             // stroke(1,wave*raw.z);
             // line(x,200+(k),x,h);
             x++;
         }
         time = time+.02;
         popMatrix();

         popStyle();
    }


    PVector noisek(float a,float b){
        PVector k = new PVector (a,b,0);
        float c = noise(a,b);
        PVector retval =new PVector (1,1,c);
        return retval;
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
            g.fill(#FFC6CF); // 0, 150, 255);
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
