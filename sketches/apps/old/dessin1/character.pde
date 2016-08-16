
int CHARACTER_MODE = 71219;


import java.text.DecimalFormat;

ArrayList<Character> characters;

Character activeCharacter;
int characterWeight = 2;
int characterIntens = 130;

PFont ageFont;
int fontSize = 20;
DecimalFormat decimalFormat;

PImage charImg, posImg, sizeImg, ageImg, strengthImg;

void initCharacterMode(){
    characters = new ArrayList<Character>();

    // TODO: temporary

    // Images : age / strength / size / pos

    charImg = loadImage(sketchPath + "/images/char.png");
    ageImg = loadImage(sketchPath + "/images/age.png");
    strengthImg = loadImage(sketchPath + "/images/strength.png");
    sizeImg = loadImage(sketchPath + "/images/size.png");
    posImg = loadImage(sketchPath + "/images/pos.png");


    ageFont = loadFont(sketchPath + "/data/Font/DejaVuSerif-130.vlw");
    decimalFormat = new DecimalFormat("#.##");


    activeCharacter =new Character(10, new PVector(40, 40), 7.5, 20); 
    characters.add(activeCharacter);
}


void enterCharacterMode(){

    resetFullNumPad();
}


void leaveCharacterMode(){



}



int CHARACTER_POS = 1;
int CHARACTER_SIZE = 2;
int CHARACTER_AGE = 3;
int CHARACTER_STRENGTH = 4;

int characterMode = NONE;


void leaveCurrentCharacterMode(){

    characterMode = NONE;

    int[] keys = {0, 1, 2, 3};

    // if(l == null)
    // 	setNotActiveKeys(keys);
    // else
	setActiveKeys(keys);

}


void keyCharacter(){

    keyIndirect();

    if(key == '1'){
	if(characterMode == NONE){
	    characterMode = CHARACTER_POS;
	    
	    int[] keys = {0, 8};
	    setActiveKeys(keys);
	}
	else 
	    leaveCurrentCharacterMode();
    }

    if(key == '2'){
	if(characterMode == NONE){
	    characterMode = CHARACTER_SIZE;
	    setActiveKey(1);
	}
	else 
	    leaveCurrentCharacterMode();
    }


    if(key == '3'){
	if(characterMode == NONE){
	    characterMode = CHARACTER_AGE;
	    setActiveKey(2);
	}
	else 
	    leaveCurrentCharacterMode();
    }

    if(key == '4'){
	if(characterMode == NONE){
	    characterMode = CHARACTER_STRENGTH;
	    setActiveKey(3);
	}
	else 
	    leaveCurrentCharacterMode();
    }


}


void touchCharacter(TouchElement te){

    
    if(te.position2D.size() >= 1){
	
	PVector p0 = te.position2D.get(0);
	p0 = PVector.mult(p0, drawSize);
	p0.y += yTouchOffset;
	
	
	if(characterMode == CHARACTER_POS && !isIndirectOn)
	    activeCharacter.setPos(p0);
	
	
	}

    
    if(te.speed2D.size() >= 1){
	
	PVector p0 = te.speed2D.get(0);
	
	if(characterMode == CHARACTER_AGE)
	    activeCharacter.addToAge(p0.x * 5);
	
	
	if(characterMode == CHARACTER_POS && isIndirectOn)
	    activeCharacter.addToPos(PVector.mult(p0, drawSize));
	
	if(characterMode == CHARACTER_STRENGTH)
	    activeCharacter.addToStrength(p0.x * 5);  

	if(characterMode == CHARACTER_SIZE) 
	    activeCharacter.addToSize(p0.x * 10);  


				 
	//	activeCharacter.strength += p0.x;
	//	activeCharacter.headSize += p0.y;

	// if(characterMode == NONE){

// int CHARACTER_POS = 1;
// int CHARACTER_SIZE = 2;
// int CHARACTER_AGE = 3;CHARACTER_AGE = 3;
// int CHARACTER_STRENGTH = 4;


    }

}



void drawCharacter(GLGraphicsOffScreen g){

    if(globalMode == CHARACTER_MODE){
	numPadImages[0] = posImg;
	numPadImages[1] = sizeImg;
	numPadImages[2] = ageImg;
	numPadImages[3] = strengthImg;

	if(characterMode == CHARACTER_POS){
	    numPadIndirect();
	}else {
	    numPadImages[8] = tmpImage;
	}


	for(Character c  : characters){
	    c.drawSelf(g);
	}

    }


}




class Character{

    // p112 - Cours de dessin facile - Hachette Collections
    float headSize;
    float strength;

    PVector pos;
    float age;
    

    public Character(float headSize, PVector pos, float strength, float age){
	this.pos = pos;
	this.headSize = headSize;
	this.strength = strength;
	this.age = age;
    }

    // http://www.idrawdigital.com/2009/01/tutorial-adult-child-proportions/

    void addToAge(float plus){
	this.age += plus;
	this.age = constrain(this.age, 1, 20);	
    }

    void addToStrength(float plus){
	this.strength += plus;
	this.strength = constrain(this.strength, 6.5, 9);
    }



    void setPos(PVector pos){
	this.pos = pos;
    }

    void addToPos(PVector v){
	v.mult(indirectPrecisionFactor);
	this.pos.add(v);
    }

    void addToSize(float v){
	this.headSize = constrain(this.headSize + v, 5, 100);
	
    }


    public void drawSelf(GLGraphicsOffScreen g){

	//	g.translate(pos.x, pos.y);

	g.pushStyle();
	g.noFill ();
	g.strokeWeight(characterWeight);
	g.stroke(characterIntens);




	if(age >= 20){

	    /////// Adult drawing  /////// 
	    float numHeadsHeight = floor(strength);
	    float lastLineHeight = strength - numHeadsHeight;

	    // default is two heads. 
	    float headRatio = (2 + (strength - 7.5) / 3f);
	    for(float y = pos.y + strength  * headSize;
		y >= pos.y ;
		y -= headSize){
	    
		g.line(pos.x, y, pos.x + headRatio * headSize, y);
	    }

	    float y = pos.y;
	    g.line(pos.x, y, pos.x + headRatio * headSize, y);


	    for(float x = pos.x;
		x <=  pos.x + headRatio * headSize;
		x += headRatio * headSize / 2f){
	    
		g.line(x, pos.y, x, pos.y +  strength  * headSize);
	    }
	}
	else{ 
	    //////// child drawing  ///////


	    float headWidth = map(age, 1, 20, 1.2, 2); 
	    float numHeads = map(age, 1, 20, 4, 7.5);


	    //////// Horizontal Lines
	    for(float y = pos.y + numHeads * headSize;
		y >= pos.y ;
		y -= headSize){
	    
		g.line(pos.x, y, pos.x + headWidth * headSize, y);
	    }

	    float y = pos.y;
	    g.line(pos.x, y, pos.x + headWidth * headSize, y);

	    /////// Vertical Lines

	    // No middle if too small
	    if(headWidth < 1.5){
		float x = pos.x;
		g.line(x, pos.y, x, pos.y +  numHeads * headSize);

		x = pos.x + headWidth * headSize;
		g.line(x, pos.y, x, pos.y +   numHeads  * headSize);

	    }else{

		for(float x = pos.x;
		    x <=  pos.x + headWidth * headSize;
		    x += headWidth / 2f * headSize){
		    g.line(x, pos.y, x, pos.y +  numHeads  * headSize);
		}
	    }


	}

	
	if(characterMode == CHARACTER_AGE){
	    g.pushMatrix();
	    
	    g.scale(1, -1, 1);
	    g.textFont(ageFont, fontSize);
	    
	    //	  DecimalFormat decimalFormat = new DecimalFormat("#.##");
	    String ageText = decimalFormat.format(this.age);
	    g.text("age " + ageText, pos.x, -(pos.y - fontSize - 4));
	    
	    g.popMatrix();
	}


	if(characterMode == CHARACTER_STRENGTH){
	    g.pushMatrix();
	    
	    g.scale(1, -1, 1);
	    g.textFont(ageFont, fontSize);
	    
	    //	  DecimalFormat decimalFormat = new DecimalFormat("#.##");
	    String strengthText = decimalFormat.format(this.strength);
	    g.text("stength " + strengthText, pos.x, -(pos.y - fontSize - 4));
	    
	    g.popMatrix();
	}


	g.popStyle();
    }

}
