
static int NB_PLAYERS = 0;
static int MAX_PLAYERS = 5;
static Player currentPlayer;
static ArrayList<Player> playerList = new ArrayList<Player>();
static ArrayList<PVector> allTokens = new ArrayList<PVector>();

int minTokenDist = 30;

void nextPlayer(){

    println("Next player");


    currentPlayer.HP += nbHearts * 5;

     for(Player player : playerList){
	 if(player != currentPlayer)
	     player.HP -= nbAttack * 2;
    }
     
    
    if(currentPlayer.id == NB_PLAYERS -1){
	currentPlayer = playerList.get(0);
    } else {
	currentPlayer = playerList.get(currentPlayer.id + 1);
    }
    
    println("Current player " + currentPlayer.id);
}



void removeLastToken(){

    if(allTokens.isEmpty())
	return;
    
    PVector token = allTokens.get(allTokens.size() - 1);
    for(Player player : playerList){
	if(player.tokens.contains(token))
	    player.tokens.remove(token);
    }
    allTokens.remove(token);
    
}


public class Player {

    ArrayList<PVector> tokens = new ArrayList<PVector>();
    int drawingColor;
    int id;
    int HP = 40;
    int nbPoints = 0;
    
    public Player(){
	createID();
	this.drawingColor = getColor();
	playerList.add(this);
    }
    
    public boolean tryAddToken(PVector pos){

	for(PVector v : allTokens){
	    if(v.dist(pos) < minTokenDist)
		return false;
	}

	println("Adding a token, total " + allTokens.size());

	PVector tokenPos = pos.get();
	allTokens.add(tokenPos);
	tokens.add(tokenPos);
	return true;
    }

    public int getColor(){
	if(this.id == 0)
	    return #366AFF; // bleus. 

	if(this.id == 1)
	    return #14FA2C; // verts

	if(this.id == 2)
	    return #ED241D; // rouges
	
	if(this.id == 3)
	    return #D1BE54;

	return #479D81;
    }
    
    private void createID(){
	this.id = NB_PLAYERS++;
    }

}
