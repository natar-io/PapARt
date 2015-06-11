
static int NB_PLAYERS = 0;
static int MAX_PLAYERS = 5;
static Player currentPlayer;
static ArrayList<Player> playerList = new ArrayList<Player>();
static ArrayList<Token> allTokens = new ArrayList<Token>();

int minTokenDist = 30;

class Token extends PVector {
    public Player owner;
    public Token(float x, float y){
	super(x,y);
    }
}

class SpecialAttack extends PVector {

}


void actionPressed(){
    if(Mode.is("PlaceDice")){
	resolveHealAttack();
	Mode.set("ChooseAction");
	return;
    }

    if(Mode.is("ChooseAction")){
	// todo : check action...
    }

}

void resolveHealAttack(){

    int nbHearts = heartCounter.size();
    int nbAttack = attackCounter.size();

    heartCounter.clear();
    attackCounter.clear();
    
    currentPlayer.HP += nbHearts * 5;

     for(Player player : playerList){
	 if(player != currentPlayer)
	     player.HP -= nbAttack * 2;
    }
     
    


}


void nextPlayer(){

    println("Next player");
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
    
    Token token = allTokens.get(allTokens.size() - 1);
    token.owner.tokens.remove(token);
    allTokens.remove(token);
    
}


public class Player {

    ArrayList<Token> tokens = new ArrayList<Token>();
    int drawingColor;
    int drawingTempColor;

    int id;
    int HP = 40;
    int nbPoints = 0;
    
    public Player(){
	createID();
	this.drawingColor = getColor();
	playerList.add(this);
    }
    
    public boolean tryAddToken(PVector pos){

	for(Token v : allTokens){
	    if(v.dist(pos) < minTokenDist)
		return false;
	}

	println("Adding a token, total " + allTokens.size());

	Token token = new Token(pos.x, pos.y);
	token.owner = this;
	allTokens.add(token);
	tokens.add(token);
	return true;
    }

    public int getColor(){
	if(this.id == 0)
	    return #2856D1; // bleus. 

	if(this.id == 1)
	    return #3FB75A; // verts

	if(this.id == 2)
	    return #E81014; // rouges
	
	if(this.id == 3)
	    return #E0E039; // jaunes

	if(this.id == 4)
	    return #DE8C33; // oranges

	return #479D81;
    }


    public int getTempColor(){
	if(this.id == 0)
	    return #507EF7; // bleus. 

	if(this.id == 1)
	    return #5EE37B; // verts

	if(this.id == 2)
	    return #FA5356; // rouges
	
	if(this.id == 3)
	    return #EAEA63; // jaunes

	if(this.id == 4)
	    return #F5AC5E; // oranges

	return #479D81;
    }
    
    private void createID(){
	this.id = NB_PLAYERS++;
    }

}
