static int NB_PLAYERS = 0;
static int MAX_PLAYERS = 5;
static Player currentPlayer;
static ArrayList<Player> playerList = new ArrayList<Player>();
static ArrayList<Token> allTokens = new ArrayList<Token>();
static ArrayList<SpecialAttack> allAttacks = new ArrayList<SpecialAttack>();


int minTokenDist = 30;

class Token extends PVector {
    public Player owner;
    public Token(float x, float y){
	super(x,y);
    }
}

class SpecialAttack extends PVector {
    public Player owner;
    public SpecialAttack(float x, float y){
	super(x,y);


    }
}

// Ration between size of  Tower and special attack
float attackHPRatio = 1.5f;
int heartHPUp = 5;
int attackHPDown = 2;

boolean actionPressed = false;

boolean tryAddElement = false;

void action(){

    println("Action !");

    if(Mode.is("PlaceDice")){
	nextPlayer();
    }

    if(Mode.is("SpecialAttack") || Mode.is("AddTower")){
	tryAddElement = true;
    	return;
    }
}

// To be done again
void undo(){
}

void addTower(){
    nextPlayer();
}

void addSpecialAttack(){
    nextPlayer();
}


void resolveHealAttack(){
    savePlayersHP();
    currentPlayer.heal();

     for(Player player : playerList){
	 if(player != currentPlayer)
	     player.receiveDamage();

    }
}

void savePlayersHP(){
    for(Player player : playerList){
	player.saveHP();
    }
}

void undoPlayersHP(){
    for(Player player : playerList){
	player.restoreHP();
    }
}

    // TODO: display something for nextplayer, like a glow on colour ?
float lastNextPlayer = 0;

void nextPlayer(){

    resolveHealAttack();

    println("Next player");
    if(currentPlayer.id == NB_PLAYERS -1){
	currentPlayer = playerList.get(0);
    } else {
	currentPlayer = playerList.get(currentPlayer.id + 1);
    }

    println("Current player " + currentPlayer.id);
    lastNextPlayer = millis();
    Mode.set("PlaceDice");
}

void prevPlayer(){

    // TODO: display something for nextplayer, like a glow on colour ?
    println("Prev player");

    if(currentPlayer.id == 0){
	currentPlayer = playerList.get(NB_PLAYERS -1);
    } else {
	currentPlayer = playerList.get(currentPlayer.id - 1);
    }

    println("Current player " + currentPlayer.id);
    Mode.set("PlaceDice");
}




void removeLastToken(){

    if(allTokens.isEmpty())
	return;

    Token token = allTokens.get(allTokens.size() - 1);
    token.owner.tokens.remove(token);
    allTokens.remove(token);

}

boolean isCloseToAToken(PVector pos){
    for(Token v : allTokens){
	if(v.dist(pos) < minTokenDist)
	    return true;
    }
    return false;
}


public float damageAmount(){
    return nbAttack * attackHPDown;
}

public float healAmount(){
    return nbHearts * heartHPUp;
}

public class Player {

    ArrayList<Token> tokens = new ArrayList<Token>();
    ArrayList<SpecialAttack> attacks = new ArrayList<SpecialAttack>();
    int drawingColor;
    int drawingTempColor;

    int id;
    int HP = 40;
    int lastHP = 40;
    int nbPoints = 0;

    public Player(){
	createID();
	this.drawingColor = getColor();
	playerList.add(this);
    }

    public void heal(){
	HP += healAmount();
    }

    public void receiveDamage(){
	HP -= damageAmount();
    }

    public void saveHP(){
	lastHP = HP;
    }

    public void restoreHP(){
	HP = lastHP;
    }


    public void addToken(PVector pos){

	Token token = new Token(pos.x, pos.y);
	token.owner = this;
	allTokens.add(token);
	tokens.add(token);

	println("Adding a token, total " + allTokens.size());
    }



    public void addAttack(PVector pos){

	SpecialAttack attack = new SpecialAttack(pos.x, pos.y);
	attack.owner = this;
	allAttacks.add(attack);
	attacks.add(attack);

	println("Adding a specialattack, total " + allAttacks.size());
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
