PImage someImage;
PShader dirBlur;
 
void setup() {
    someImage = loadImage("http" + "://forum.processing.org/two/uploads/imageupload/549/25QR206HDUK3.png");
    size(someImage.width, someImage.height, P2D);
    dirBlur = loadShader("blur.frag");
}
 
void draw(){
    image(someImage, 0, 0);
    float angle = atan2(mouseX - width / 2, height / 2 - mouseY);
    dirBlur.set("blurOffset", 1.0 / someImage.width * sin(angle), 1.0 / someImage.height * cos(angle));
    for(int n = 0; n < 8; n++)
        filter(dirBlur);
}
