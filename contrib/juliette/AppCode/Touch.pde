public class MyApp extends PaperTouchScreen { //<>// //<>//

  // La balle de pong
  float balleHaut;
  float balleGauche;
  float mouvementBalleVert = 0;
  float mouvementBalleHoriz = 0;
  float diametreBalle;
  boolean balleLancee = false;

  // la distance entre les blocs et les bords du jeu
  float epaisseurBords;

  // le bloc de gauche, joué par la personne
  float hauteurBG;
  float largeurBG;

  // le bloc de droite, joué par l'ordinateur
  float hauteurBD;
  float largeurBD;

  float tailleBlocs;

  // l'accélération de la balle à chaque rebond
  float coeffRebond;

  // la distance réelle que fait le jeu (en mm ici)
  float heightProj;
  float widthProj;

  // la hauteur à laquelle le joueur touche
  float my;

  public void settings() {
    setDrawOnPaper();
    setDrawingSize(297, 210);
    loadMarkerBoard(Papart.markerFolder + "A3-small1.svg", 297, 210);
  }

  public void setup() {

    coeffRebond = -1.01;

    ellipseMode(CORNER);

    // initialisation des distances du jeu réelles
    widthProj = getDrawingSize().x;
    heightProj = getDrawingSize().y;

    // initialisation des autres paramètres 
    // en fonction des distances du vidéoProj, pour garder
    // toujours tout à la même échelle
    epaisseurBords = widthProj/30;
    diametreBalle = widthProj/20;

    hauteurBG = heightProj/4;
    largeurBG = widthProj/20;

    hauteurBD = heightProj/4;
    largeurBD = widthProj/20;

    balleGauche = widthProj/2;
    balleHaut = heightProj/2;

    tailleBlocs = heightProj/4;
  }

  public void drawOnPaper() {

    setLocation(63, 45, 0);
    background(200);

    manageTouchs();

    deplacerObjets();

    if (balleDepasse())     // si la balle touche le côté gauche on a perdu
      perdu();

    if (balleToucheBlocDroit() || balleToucheBlocGauche())     // si la balle touche un bloc, elle rebondit
      mouvementBalleHoriz *= coeffRebond;

    if (balleToucheBordHorizontal())
      mouvementBalleVert *= coeffRebond;

    dessinerObjets();    // on dessine les objets, qui se sont déplacés
  }

  void dessinerObjets() {
    fill(255, 108, 109);
    ellipse(balleGauche, balleHaut, diametreBalle, diametreBalle);

    fill(20, 135, 255);
    rect(epaisseurBords, hauteurBG, largeurBG, tailleBlocs);

    fill(75, 255, 94);
    rect(widthProj-epaisseurBords-largeurBD, hauteurBD, largeurBD, tailleBlocs);
  }

  void lancerBalle() {

    delay(1000);
    balleHaut = heightProj/2;
    balleGauche = widthProj/2;

    mouvementBalleHoriz = int(random(0, 2)) == 0 ? -3 : 3;
    mouvementBalleVert = int(random(0, 2)) == 0 ? -3 : 3;

    balleLancee = true;
  }

  void perdu() {
    fill(0);
    text("Perdu !", widthProj/3, 2*heightProj/5);
    text("Touchez la balle pour recommencer.", epaisseurBords *4, 3*heightProj/5);
    balleLancee = false;
  }

  void manageTouchs() {
    for (Touch t : touchList.get2DTouchs()) {

      // si on veut lancer la balle, il faut la toucher
      if (!balleLancee && toucheBalle(t.position)) 
        lancerBalle();

      my = t.position.y;
    }
  }

  // on déplace les objets
  // il faut s'assurer que les objets ont la bonne taille
  // ou ne dépassent pas de l'écran
  void deplacerObjets() {

    // position du bloc de gauche
    hauteurBG = constrain(my  - heightProj/8, 0, 3*heightProj/4);

    // position bloc de droite
    hauteurBD = constrain(balleHaut  - heightProj/8, 0, 3*heightProj/4);

    // on s'assure que la balle ne sort pas de la fenêtre
    balleGauche = constrain(balleGauche, 0, widthProj - diametreBalle);
    balleHaut = constrain(balleHaut, 0, heightProj - diametreBalle);

    if (balleLancee) {
      balleHaut += mouvementBalleVert;
      balleGauche += mouvementBalleHoriz;
    }
  }

  boolean balleDepasse() {
    return balleGauche <= 0 || balleGauche + diametreBalle >= widthProj;
  }

  boolean balleToucheBlocDroit() {
    return balleGauche + diametreBalle/2 >= widthProj-epaisseurBords-largeurBD // la balle touche le bloc sur les X
      && balleHaut >= hauteurBD // la balle est plus basse que le haut d'un bloc
      && balleHaut + diametreBalle <= hauteurBD + tailleBlocs; // la balle est plus basse que le bas d'un bloc
  }

  boolean balleToucheBlocGauche() {
    return balleGauche <= largeurBG + epaisseurBords + diametreBalle/2// la balle touche le bloc sur les X
      && balleHaut - diametreBalle/2 >= hauteurBG // la balle 
      && balleHaut + diametreBalle <= hauteurBG + tailleBlocs;
  }

  boolean balleToucheBordHorizontal() {
    return balleHaut <= diametreBalle / 2 || balleHaut + diametreBalle >= heightProj;  // la balle est au dessus de la zone de jeu, ou en dessous
  }

  boolean toucheBalle(PVector touch) {
    if (touch.x >= balleGauche && touch.x <= balleGauche + diametreBalle
      && touch.y >= balleHaut && touch.y <= balleHaut + diametreBalle)
      println("Touche balle !");
    return touch.x >= balleGauche && touch.x <= balleGauche + diametreBalle
      && touch.y >= balleHaut && touch.y <= balleHaut + diametreBalle;
  }
}