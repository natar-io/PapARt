

PImage label;
PShape can;
float angle;

PShader texlightShader;
PImage backgroundImage, projectedImage;

PShape bgQuad, projQuad;

void setup(){

    size(800, 600, OPENGL);

    texlightShader = loadShader("pixLightFrag.glsl", "pixLightVert.glsl");
    backgroundImage = loadImage("background.jpg");
    projectedImage = loadImage("proj.jpg");

    bgQuad = createQuad(new PVector(0,0), new PVector(500, 500), backgroundImage);
    projQuad = createQuad(new PVector(0,0), new PVector(100, 100), projectedImage);
    
}


void draw(){

    background(0);

    shader(texlightShader);

    pointLight(255, 255, 255, width/2, height, 200);  

    translate(100, 100);
    shape(bgQuad);

}



PShape createQuad(PVector pos, PVector size, PImage tex){
    PShape sh = createShape();
    sh.beginShape(QUAD);
    sh.noStroke();
    sh.texture(tex);
    sh.vertex(pos.x,          pos.y, 0, 0);
    sh.vertex(pos.x + size.x, pos.y, tex.width, 0);
    sh.vertex(pos.x + size.x, pos.y + size.y, tex.width, tex.height);
    sh.vertex(pos.x ,         pos.y + size.y, 0, tex.height);
    
    //	sh.normal(x, 0, z);
    sh.endShape(); 
    return sh;
}
