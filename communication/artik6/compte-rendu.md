
# Réunion Artik t+6

## Présents 

> Porteur de projet, développeur(s), clients du projet (Potioc) et experts utiles (SED, STIP). 

Jérémy Laviole (JL) - Ingénieur Artik.
Hervé Mathieu (HM) - Directeur SED Bordeaux.
Martin Hachet (MH) - Initateur du projet, chef d'équipe. 
Anke Brock - Chargée de recherche.
Renaud Gervais - Doctorant 3ième année. 
Jérémy Frey (JF) - Doctorant 3ième année.
Sol Roo - Doctorant 1ière année.
Julia Chatain (JC) - Stagaire Master 2. 
Damien Clergeau - Doctorant 1ière année. 
Aurélien Masenet - Doctorant 1ière année. 


## Motif de la réunion
Réunion d'avancement de l'ADT Artik. Elle fait partie du suivi de projet dans le cadre des ADT Inria. Il y a une réunion au lancement, puis une tous les 6 mois.  

Cette réunion était  T0+6mois pour un projet de 2 ans. 

## Contenu de la réunion


### Introduction des ADT (HM)

Pour la suite ouvrez les [slides de la réunion](Artik-resentation-6.pdf).

### Objectifs de l'ADT (JL)

> Slide 2

Commentaires

> MH : ne pas perdre tout le travail fait durant la thèse de JL. 

### Fonctionnalités 

> Slide 3, puis [schéma fonctionnel](Artik-resentation-6.pdf) et [dépendances logicielles](archi-Mars-2015.svg). 


### Bilan t0+ 6 mois 

> Slides 4 et 5. 

**Retours JF** : 
- Installation difficile à faire soi-même. -> Un guide d'installation serait nécessaire. La gestion des différentes versions des bibliothèques dépendantes peut être aussi fastidieuse. -> L'élaboration d'un package indépendant de la version courante de Processing est a étudier. 
-  Compréhension difficile des types de rendus. Le rendu 2D limité à la feuille et le rendu 3D qui peut déborder. −> Une page détaillée d'explication serait nécessaire. 
-  Problème de rendu lié à l'odre des appels de fonctions. L'odre n'est pas maîtrisé lors des instanciations automatiques. 
- Problèmes avec les dépendances de PapARt : le moteur de rendu de Processing est peu performant. 

**Retours JC** : 
- Le nombre d'étapes de la calibration est trop important (4 étapes actuellement). Une intégration de toutes ces étapes dans un logiciel serait intéressant. Réponse JL : des étapes sont amenées à disparaître, c'est pourquoi l'intégration n'était pas une priorité. 
- Le numéro de caméra, résolution d'écran etc... sont à fixer à la main dans chaque application. -> Limitation connue, qui monte dans la liste des priorités. 
- Une auto-évaluation de la qualité de la calibration permettrait de savoir d'où proviennent certaines erreurs (code, ou qualité de la calibration). 

### Éléments à développer

> Résumé de la liste des choses à faire pour le dévelopement logiciel. Slides 6 et 7.  
> Le diagramme de gantt plus précis est inclus dans ce résumé à titre historique : papart.xml à ouvrir avec [GanttProject](http://www.ganttproject.biz/). 

### Objectifs des 6 prochains mois 

#### Objectifs équipe Potioc 

> Slide 8. 


- Showroom: installation physique à Inria, portable ou duplicable. 
- Vidéo(s) de présentation, dont une de format court. 
- Publication scientifique de «référence» style Emerging Tech, ou associée à une publication avec le projet de Julia. 
- Collaborations : Cap Sciences «villes intelligentes», autres Living Labs et à voir avec d'autres partenaires (Nadine Couture était intéressée pour l'ESTIA). 
- Donc dupliquer la démonstration. 

#### Objectifs transfert

> Slides 9 à 14. 

Création d'un kit facile à installer et à utiliser. 
Lancement d'une startup pour commercialiser le kit. 


### Réunions à venir 

- 15 Avril dépôt APP Papart. 
- Fin Avril - début mai.  Rencontre avec [l'incubateur d'aquitaine](http://www.incubateur-aquitaine.com/). 

Réunions décrites sur la slide 13. 
- Brainstorming  PapARt, ,courant mai à début juin.  
- Distribution de PapARt et propriété intellectuelle. 
 
