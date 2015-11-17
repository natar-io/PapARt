class Game{

  private static final String imageURI = "images/";
  private static final String mignon = "mignon.";
  private static final String moche = "moche";
  private static final String etoile = "etoile";

  private static final String ext = ".jpg";
  private static final int mignonNb = 42;
  private static final int etoilesNb = 4;
  private static final int mocheNb = 9;

  public static final int imagesNb = mignonNb + etoilesNb + mocheNb;

  private final PImage[] imagesMignon = new PImage[mignonNb];
  private final PImage[] imagesMoche = new PImage[mocheNb];
  private final PImage[] imagesEtoiles = new PImage[etoilesNb];


  private static final int totalGameElements = 20;
  private static final int addingTime = 4000;
  int lastAdd = millis();


  void init(){
    for(int i=0; i< mignonNb ; i++){
      int imageNo = i +1;
      String name = imageURI + mignon + imageNo + ext;
      imagesMignon[i] = loadImage(name);
    }

    for(int i=0; i< etoilesNb ; i++){
      int imageNo = i +1;
      String name = imageURI + etoile + imageNo + ext;
      imagesEtoiles[i] = loadImage(name);
    }

    for(int i=0; i< mocheNb ; i++){
      int imageNo = i +1;
      String name = imageURI + moche + imageNo + ext;
      imagesMoche[i] = loadImage(name);
    }


  }

  ArrayList<GameElement> gameElements = 
    new ArrayList<GameElement>();

  public Game(){

  }

  void addElement(){
 
    int type = (int) random(2);
    PImage img;

    if(type == TYPE_CUTE){
      img = imagesMignon[(int)random(mignonNb)];
    } else{
      img = imagesMoche[(int)random(mocheNb)];
    }

    gameElements.add(new GameElement(img,
				     millis(),
				     type));
  }


  ArrayList<GameElement> toDelete = new ArrayList<GameElement>();
  void update(){

    int time = millis();

    if(time - lastAdd > addingTime && 
       gameElements.size() < totalGameElements){
      if(((int) random(1)) == 0){
	addElement();
	lastAdd = time;
      }
    }
    
    for(GameElement ge : gameElements){
      ge.tick(time);
      if(ge.toDelete){
	toDelete.add(ge);
      }
    }

    if(!toDelete.isEmpty()){
      for(GameElement ge :toDelete){
	gameElements.remove(ge);
      }
      toDelete.clear();
    }

  }

  void drawGame(){
    int time = millis();
    for(GameElement ge : gameElements){
      ge.drawElement(time);
    }
  }
}

