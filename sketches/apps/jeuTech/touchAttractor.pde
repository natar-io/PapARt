class TouchAttractor extends DyingObject{

    PVector position;

    public TouchAttractor(float x, float y){
	this.position = new PVector(x, y);
	lifeTime = 1500;
    }

}
