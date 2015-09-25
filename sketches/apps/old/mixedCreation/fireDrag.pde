// http://www.openprocessing.org/sketch/30281

class FireDrag extends SubSketch {


/* OpenProcessing Tweak of *@*http://www.openprocessing.org/sketch/30281*@* */
/* !do not delete the line above, required for linking your tweak if you re-upload */
//  ========================================================
//
//  Fire Drag
//  arthurG, June 2011
//
//  questions, comments : arthurgraff -at- gmail
//  ========================================================



class CParticleFire {
  float x, y;
  float dirx,diry;
  float speed;
  boolean alive;
  float perlinPosX, perlinPosY;
  float angle;
  int amountA,amountR,amountG,amountB;

  CParticleFire (float x, float y) {
    this.x = x;
    this.y = y;
    this.speed = 0.7f+random(0.7f);
    this.angle = random(TWO_PI);

    amountA = 140+(int)random (80);
    amountR = 65+(int)random (65);
    amountG = 30+(int)random (30);
    amountB = 20+(int)random (20);

    perlinPosX = random (100);
    perlinPosY = random (100);


    this.dirx = random(-speed,speed);
    this.diry = random(-speed,speed);

    this.alive = true;
  }

  void update () {

    if (amountR>0)
      amountR--;
    if (amountG>0)
      amountG--;
    if (amountB>0)
      amountB--;
    if (amountA>0)
      amountA--;

    if (amountR==0 && amountG==0 && amountB==0 && amountA==0) {
      alive= false;
      return;
    }

    float dx = 2.0f*(0.5-noise (perlinPosX));
    float dy = 2.0f*(0.5-noise (perlinPosY));
    perlinPosX += scaleNoise*speed;
    perlinPosY += scaleNoise*speed;

    //this.direction.normalize ();
    float c = 0.0f;

    dirx = (dirx+dx*speed)/2.0f;
    diry = (diry+dy*speed)/2.0f;

    speed *= 0.99f;

    x += dirx;
    y += diry;

    if (x<0 || x>=width || y<0 || y>=height)
      alive = false;
  }

  void draw () {
    int id = (int)x+(int)y*width;
    bufferR [id] = min (255,(bufferR [id])+amountR);
    bufferG [id] = min (255,(bufferG [id])+amountG);
    bufferB [id] = min (255,(bufferB [id])+amountB);
    bufferA [id] = min (255,(bufferA [id])+amountA);
  }
}

//  ========================================================


class CSourceFire {

  float x, y;
  float lastx, lasty;

  ArrayList particles;
  int MAX_PARTICLES = 5000;

  CSourceFire (float x, float y) {
    this.x = x;
    this.y = y;

    this.lastx = this.x;
    this.lasty = this.y;

    particles = new ArrayList();
  }

  void update (float x, float y, int n) {

    this.lastx = this.x;
    this.lasty = this.y;
    this.x = x;
    this.y = y;

    float coef;
    for (int i=0; i<n && particles.size()<MAX_PARTICLES; i++) {
      coef=random(1);
      particles.add (new CParticleFire(this.x*coef+this.lastx*(1.0f-coef),this.y*coef+this.lasty*(1.0f-coef)));
    }

    for (int i=particles.size()-1; i>=0; i--) {
      CParticleFire p = (CParticleFire)particles.get(i);
      p.update ();
      if (!p.alive)
        particles.remove (i);
      else
        p.draw ();
    }



  }

  void getImage (PImage img) {
    img.loadPixels();
    int val;
    for (int i=width*height-1; i>=0; i--) {
      img.pixels[i] = bufferA[i]<<24 | bufferR[i]<<16 | bufferG[i]<<8 | bufferB[i];
    }
    img.updatePixels();
  }

}

//  ========================================================


PImage img;
int[] bufferR;
int[] bufferG;
int[] bufferB;
int[] bufferA;

CSourceFire source;

float scaleNoise = 0.01f;



// ----------------------------------------------------------
//
//  fastBlur :
//  Blur an array by avoiding a 2nd temporary array
//
//  A B C D E F
//  G H I J K L  -> array
//  M N O P Q R
//
//  1st pass, blur the cells A, C, E, H, J, L, M, O, Q
//  2nd pass, blur the cells B, D, F, G, I, K, N, P, R
//
//  i.e. (2nd line) :
//  1st pass : H = (H+(B+G+I+N)/4)/2
//             J = (J+(D+I+K+P)/4)/2
//  2nd pass : I = (I+(C+H+J+O)/4)/2
//             K = (K+(E+J+L+Q)/4)/2
//
// ----------------------------------------------------------
void fastBlur (int[] buf) {
  int id, k;
  int lim = width*(height-1);

  id = width+1;
  k=0;
  for (; id<lim; id+=1+k+k) {
    for (int i=1+k; i<width-1; i+=2, id+=2) {
      buf [id] >>= 1;
      buf [id] += (buf[id-1]+buf[id+1]+buf[id+width]+buf[id-width])>>3;
    }

    k = 1-k;
  }

  id = width+2;
  k=1;
  for (; id<lim; id+=1+k+k) {
    for (int i=1+k; i<width-1; i+=2, id+=2) {
      buf [id] >>= 1;
      buf [id] += (buf[id-1]+buf[id+1]+buf[id+width]+buf[id-width])>>3;
    }
    k = 1-k;
  }
}

void fastBigBlur (int[] buf) {
  int id, k;
  int lim = width*(height-1);

  id = width+1;
  k=0;
  for (; id<lim; id+=1+k+k) {
    for (int i=1+k; i<width-1; i+=2, id+=2) {
      buf [id] = (buf[id-1]+buf[id+1]+buf[id+width]+buf[id-width])>>2;
    }

    k = 1-k;
  }

  id = width+2;
  k=1;
  for (; id<lim; id+=1+k+k) {
    for (int i=1+k; i<width-1; i+=2, id+=2) {
      buf [id] = (buf[id-1]+buf[id+1]+buf[id+width]+buf[id-width])>>2;
    }
    k = 1-k;
  }
}

void cleanBorders (int[] buf) {
  int id,lim;

  lim = width-1;
  for (id=1; id<lim; id++)
    buf[id] = 0;

  lim = width*height-1;
  for (id=width*(height-1)+1; id<lim; id++)
    buf[id] = 0;

  lim = width*height;
  for (id=0; id<lim; id+=width)
    buf[id] = 0;

  lim = width*height;
  for (id=width-1; id<lim; id+=width)
    buf[id] = 0;
}




public void setup (PApplet parent) {

    // size in Pixels 
    this.width = 150;
    this.height = 150;
    
    // display size in millimeters 
    this.displayWidth = 100;
    this.displayHeight = 100;

    // initalize the sketch
    this.initSketch(parent);
    
    
    img = createImage (width,height,ARGB);
    bufferR = new int[width*height];
    bufferG = new int[width*height];
    bufferB = new int[width*height];
    bufferA = new int[width*height];
    
    source = new CSourceFire (mouseX,mouseY);

}


void draw () {

    //    updateInputTouch(drawingTouch, drawingBoardSize);
    updateInputHover(drawingTouch, drawingBoardSize);

    clear(0, 0);
    source.update (mouseX,mouseY,15);
    
    cleanBorders (bufferA);
    cleanBorders (bufferR);
    cleanBorders (bufferG);
    cleanBorders (bufferB);
    
    fastBlur (bufferR);
    fastBlur (bufferG);
    fastBlur (bufferB);
    
    fastBlur (bufferA);
    fastBlur (bufferA);
    fastBigBlur (bufferA);
    
    source.getImage(img);
    image (img,0,0);

}












}
