import java.awt.Frame;

/// TO DELETE...

public class CameraView extends PApplet {

    PImage cameraImage;

    public CameraView() {
	super();
	PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }

    public void settings() {
	size(camera.width(), camera.height(), P3D);
    }

    public void setup() {
        frameRate(30);
        cameraImage = createImage(camera.width(), camera.height(), RGB);


        // it will fail if the calibration file is not present.
        try{
            loadCorners();
        } catch(Exception e){};
    }

    String cornersFileName = "data/corners.json";

    private void saveCorners(){
        JSONArray values = new JSONArray();
        for(int i = 0; i < corners.length; i++){
            JSONObject cornerJSON = new JSONObject();
            cornerJSON.setFloat("x", corners[i].x);
            cornerJSON.setFloat("y", corners[i].y);
            values.setJSONObject(i, cornerJSON);
        }
        saveJSONArray(values, cornersFileName);
    }

    private void loadCorners(){
        JSONArray values = loadJSONArray(cornersFileName);
        for (int i = 0; i < values.size(); i++) {
            JSONObject cornerJSON = values.getJSONObject(i);
            corners[i].set(cornerJSON.getFloat("x"),
                           cornerJSON.getFloat("y"));
        }
    }


    int lastUpdate = 0;
    int updateTimeOut = 1000;

    public void draw() {

        if(camera == null){
            background(100, 0, 0);
            return;
        }

        if(millis() > lastUpdate + updateTimeOut){
            camera.getPImageCopyTo(cameraImage);
            lastUpdate = millis();
        }

        if(cameraImage == null)
            return;
        image(cameraImage, 0, 0, width, height);

        fill(0, 180,0, 100);
        quad(corners[0].x, corners[0].y,
             corners[1].x, corners[1].y,
             corners[2].x, corners[2].y,
             corners[3].x, corners[3].y);
    }

    void mouseDragged() {
        corners[currentCorner].set(mouseX, mouseY);
    }

    void keyPressed(){
        if(key == 'l'){
            loadCorners();
        }
        if(key == 's'){
            saveCorners();
        }
    }

}
