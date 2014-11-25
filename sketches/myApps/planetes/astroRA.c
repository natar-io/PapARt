/*------------------------------------------------------------------------
Copyright 2014 Stéphanie Fleck et Gilles Simon, Université de Lorraine

This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
------------------------------------------------------------------------*/

#ifdef _WIN32
#include <windows.h>
#endif
#include <stdio.h>
#include <stdlib.h>
#ifndef __APPLE__
#include <GL/gl.h>
#include <GL/glut.h>
#else
#include <OpenGL/gl.h>
#include <GLUT/glut.h>
#endif
#include <AR/gsub.h>
#include <AR/video.h>
#include <AR/param.h>
#include <AR/ar.h>
#include <math.h>
#include <string.h>

#define IMPORT                  0

#define REVOLUTION_ORBITE		29.5
#define REVOLUTION_SATELLITE	29.5
#define PI						3.14159265
#define DISTANCE_SOLEIL			500.
#define DISTANCE_LUNE			160.
#define RAYON_SOLEIL            (38./310.*DISTANCE_SOLEIL)
#define RAYON_SOLEIL_MANIP      50.
#define RAYON_TERRE	            40.
#define RAYON_LUNE	            16.
#define DEMILONGUEUR_AXE		(RAYON_TERRE+20.)
#define INCLINAISON_TERRE		0.//23.
#define TAILLE_TIRETS			5.
#define ZOOM					1.5
#define LATITUDE                48.6928 
#define LONGITUDE               6.18361-90
#define TOLERANCE_PHASE			20
#define TOLERANCE_COSPHASE		0.1

GLfloat			light_position[] = {-DISTANCE_SOLEIL,0.0,0.0,0.0};
GLfloat			light_position_soleil[] = {0.0,0.0,0.0,1.0};
GLfloat			diffuse_light[]  = {1., 1., 1.};
GLfloat			ambient_light[]    = {0.5, 0.5, 0.5};
/*
GLfloat			ambient_terre[] = {0.02, 0.02, 0.02};
*/
GLfloat			ambient_terre[] = {0.0, 0.0, 0.0};
GLfloat			diffuse_terre[]  = {0.22, 0.42, 0.95};
GLfloat			ambient_soleil[] = {1., 1., 1.};
GLfloat			diffuse_soleil[] = {1., 1., 1.};
GLfloat			ambient_texture[]= {0.2, 0.2, 0.2};
GLfloat			ambient_texture_sombre[]= {0.02, 0.02, 0.02};
GLfloat			diffuse_texture[]= {1., 1., 1.};
GLfloat			color_sunray[] = {1., 1., 1.};
GLfloat			color_orbit[] = {1., 1., 1.};
GLfloat			color_earthaxis[] = {1., 1., 1.};

FILE           *fstatsT,*fstatsL,*fstatsS;
//
// Camera configuration.
//
#ifdef _WIN32
char			*vconf = "Data\\WDM_camera_flipV.xml";
#else
char			*vconf = "";
#endif

int             xsize, ysize;
int             thresh = 100;
int             count = 0;
char           *cparam_name    = "Data/camera_para.dat";
ARParam         cparam;
float			hour			=0.0;
double			tmpMatrix[3][4];

typedef enum {RIEN, SOLEIL, SOLEIL_MANIP, TERRE, ATERRE, LUNE, ETOILES, CIEL, TAILLE} ID_PLANETE;
int texture_planete[TAILLE];
typedef enum {WOMAN, MAN} ID_SEXE;
typedef enum {P_TERRE, P_LUNE, P_SOLEIL, P_ETE, P_AUTOMNE, P_HIVER, P_PRINTEMPS, P_ORBITE, NB_MARKERS} ID_PATTERN;

/* Image type - contains height, width, and data */
struct Image {
    unsigned long sizeX;
    unsigned long sizeY;
    char *data;
};

typedef struct Image Image;

int texture_planete[TAILLE];
static Image im[TAILLE];
static int bpp[TAILLE];

typedef struct PatternStruct {
	char name[128];
	int id;
	double width;
	double center[2];
	double trans[3][4];
	int visible;
}Pattern;

static Pattern patt[NB_MARKERS];

float			jour, angleOrbite, angleSatellite;
int				wire			= 0;
int				swap			= 0;
int				showTraits	    = 1;
int				showSysteme	    = 0;
int				showVignette    = 1;
int				showMen         = 1;
float           inclinaison     = 0.;
int             numImage        = 0;
int             next[NB_MARKERS];
int             numImageMarqueur[NB_MARKERS];

//14.5;
float			scale			= 1.2;//0.6;
int				saison			= 0;
int             newSaison[]     = {0,0,0,0};
float           distance        = 0.;
float           latitude        = LATITUDE;
float			longitude       = LONGITUDE;
GLfloat         lightposLL[4]   = {0,0,0,0}; 		
GLfloat         lightposLT[4]   = {0,0,0,0};
GLfloat			lightposTT[4]   = {0,0,0,0};       
GLfloat			sunposLL[3]   = {0,0,0};       
GLfloat			sunposLT[3]   = {0,0,0};       
GLfloat			sunposTT[3]   = {0,0,0};   
int			    soleilVisible = 0;

static void   init(void);
static void   cleanup(void);
static void   keyEvent( unsigned char key, int x, int y);
static void   mainLoop(void);
static void   draw( void );
static void	  creerPlanete(int nom, GLfloat rayon);
static void	  drawPlanete(int nom);
static void   drawMan(int sexe);
static void   init_texture(void);
static void   drawDashedLine(GLfloat orig[], GLfloat dest[], GLfloat color[], float length_tirets);
static void   drawDashedCircle(float ray, GLfloat color[], float angle_tirets);

int main(int argc, char **argv)
{
	glutInit(&argc, argv);
	init();

    arVideoCapStart();
    argMainLoop( NULL, keyEvent, mainLoop );
	return (0);
}

static void   keyEvent( unsigned char key, int x, int y)
{
    /* quit if the ESC key is pressed */
    if( key == 0x1b ) {
        printf("*** %f (frame/sec)\n", (double)count/arUtilTimer());
        cleanup();
        exit(0);
    }
	
	if ( key == 'b' || key == 'B') {
		showMen = (showMen+1)%2;
	}
	
	if ( key == 'o' || key == 'O') {
		showTraits = (showTraits+1)%2;
	}

	if ( key == 'e' || key == 'E') {
		longitude -= 1;
	}

	if ( key == 'r' || key == 'R') {
		longitude += 1;
	}

	if ( key == 't' || key == 'T') {
		latitude -= 1;
	}

	if ( key == 'y' || key == 'Y') {
		latitude += 1;
	}

	if ( key == ' ' || key == ' ' ) {
		swap = (swap+1)%2;
	}
	
	if ( key == 'v' || key == 'V' ) {
		showMen = showVignette = (showVignette+1)%2;
	}

	if ( key == 'a' || key == 'A') {
		hour = hour + 0.5 ;
	}
	
	if ( key == 'z' || key == 'Z') {
		hour = hour - 0.5 ;
	}
	
	if (key == 'w' || key == 'W') {
		wire = (wire+1)%2;
	}
	
	if (key == 'd' || key == 'D') {
		inclinaison = inclinaison + 1;
	}
	
	if (key == 'f' || key == 'F') {
		inclinaison = inclinaison - 1;
	}

	if (key == 'x' || key == 'X') {
		showVignette = showMen = showSysteme = (showSysteme+1)%2;
		
	}
	
	if (key == 's' || key == 'S') {
		scale -= 0.1;
	}
	
	if (key == 'q' || key == 'Q') {
		scale += 0.1;
	}
}

/* main loop */
static void mainLoop(void)
{
    ARUint8         *dataPtr;
    ARMarkerInfo    *marker_info;
    int             marker_num;
    int             i, j, k;

    /* grab a vide frame */
    if( (dataPtr = (ARUint8 *)arVideoGetImage()) == NULL ) {
        arUtilSleep(2);
        return;
    }
    if( count == 0 ) arUtilTimerReset();
    count++;

    argDrawMode2D();
    argDispImage( dataPtr, 0,0 );

    /* detect the markers in the video frame */
    if( arDetectMarker(dataPtr, thresh, &marker_info, &marker_num) < 0 ) {
        cleanup();
        exit(0);
    }

    arVideoCapNext();
	numImage++;

	if (IMPORT) {
		double trash;

		printf("%d : ", numImage);
		if (next[P_TERRE]) {
			fscanf(fstatsT, "%d ", &numImageMarqueur[P_TERRE]);
			for (i = 0; i < 3; i++)
				for (j = 0; j < 4; j++)
					fscanf(fstatsT, "%lf ", &patt[P_TERRE].trans[i][j]);
			for (i = 0; i < 3; i++)
				for (j = 0; j < 3; j++)
					fscanf(fstatsT, "%lf ", &trash);
		}
		if (numImage == numImageMarqueur[P_TERRE]) {
			patt[P_TERRE].visible = 1;
			next[P_TERRE] = 1;
			printf("TERRE ");
		}
		else {
			patt[P_TERRE].visible = 0;
			next[P_TERRE] = 0;
			printf("%d ", numImageMarqueur[P_TERRE]);
		}
		if (next[P_LUNE]) {
			fscanf(fstatsL, "%d ", &numImageMarqueur[P_LUNE]);
			for (i = 0; i < 3; i++)
				for (j = 0; j < 4; j++)
					fscanf(fstatsL, "%lf ", &patt[P_LUNE].trans[i][j]);
			for (i = 0; i < 3; i++)
				for (j = 0; j < 3; j++)
					fscanf(fstatsL, "%lf ", &trash);
		}
		if (numImage == numImageMarqueur[P_LUNE]) {
			patt[P_LUNE].visible = 1;
			next[P_LUNE] = 1;
			printf("LUNE ");
		}
		else {
			patt[P_LUNE].visible = 0;
			next[P_LUNE] = 0;
			printf("%d ", numImageMarqueur[P_LUNE]);
		}

		if (next[P_SOLEIL]) {
			fscanf(fstatsS, "%d ", &numImageMarqueur[P_SOLEIL]);
			for (i = 0; i < 3; i++)
				for (j = 0; j < 4; j++)
					fscanf(fstatsS, "%lf ", &patt[P_SOLEIL].trans[i][j]);
			for (i = 0; i < 3; i++)
				for (j = 0; j < 3; j++)
					fscanf(fstatsS, "%lf ", &trash);
		}
		if (numImage == numImageMarqueur[P_SOLEIL]) {
			patt[P_SOLEIL].visible = 1;
			next[P_SOLEIL] = 1;
			printf("SOLEIL ");
		}
		else {
			patt[P_SOLEIL].visible = 0;
			next[P_SOLEIL] = 0;
			printf("%d ", numImageMarqueur[P_SOLEIL]);
		}
		
		printf("\n");
	}
	else {
		for (i = 0 ; i < NB_MARKERS ; i++ ) {
			/* check for object visibility */
			k = -1;
			for( j = 0; j < marker_num; j++ ) {
				if( patt[i].id == marker_info[j].id ) {
					if( k == -1 ) k = j;
					else if( marker_info[k].cf < marker_info[j].cf ) k = j;
				}
			}
		
			if ( k == -1) patt[i].visible = 0 ;
			if ( k > -1) patt[i].visible = 1 ;
		
			/* get the transformation between the marker and the real camera */
		
			//if (k > -1 )
			arGetTransMat(&marker_info[k], patt[i].center, patt[i].width, patt[i].trans);
		}
		/*
		if( patt[P_TERRE].visible == 0 ) {
        argSwapBuffers();
        return;
		}
		*/
		for (i = P_ETE; i <= P_PRINTEMPS; i++)
			if( patt[i].visible ) {
				newSaison[i]++;
				if (newSaison[i]>5) {
					saison = i-P_ETE;
					newSaison[i] = 0;
				}
			}
	
		if( (patt[P_TERRE].visible == 1) && (patt[P_ORBITE].visible == 1 ) ) {
			/*
	        float t0x = patt[P_TERRE].trans[0][3];
	        float t0y = patt[P_TERRE].trans[1][3];
		    float t0z = patt[P_TERRE].trans[2][3];
	        float t1x = patt[P_ORBITE].trans[0][3];
		    float t1y = patt[P_ORBITE].trans[1][3];
		    float t1z = patt[P_ORBITE].trans[2][3];
			float v0x = patt[P_TERRE].trans[0][2];
			float v0y = patt[P_TERRE].trans[1][2];
			float v0z = patt[P_TERRE].trans[2][2];
			float v1x = patt[P_ORBITE].trans[0][2];
			float v1y = patt[P_ORBITE].trans[1][2];
			float v1z = patt[P_ORBITE].trans[2][2];
			float scal = v0x*v1x+v0y*v1y+v0z*v1z;
			float angle = acos(scal)/PI*180.;
			float distOld = distance;
			float diff;
			distance = sqrt((t1x-t0x)*(t1x-t0x)+(t1y-t0y)*(t1y-t0y)+(t1z-t0z)*(t1z-t0z));
			diff = fabs(distance-distOld);
			if (diff < 1.) diff = 1.;
			//hour = (distance-120.)/130.*24.*REVOLUTION_ORBITE;	
			hour += 0.25*diff;
			*/
			hour += 0.25;
		}

		if (patt[P_TERRE].visible == 1) {
			fprintf(fstatsT, "%d ", numImage);
			for (i = 0; i < 3; i++)
				for (j = 0; j < 4; j++)
					fprintf(fstatsT, "%lf ", patt[P_TERRE].trans[i][j]);
			fprintf(fstatsT, "%lf %lf %lf ", patt[P_TERRE].trans[0][3], patt[P_TERRE].trans[1][3], patt[P_TERRE].trans[2][3]);
			fprintf(fstatsT, "%lf %lf %lf ", patt[P_TERRE].trans[0][3]+patt[P_TERRE].trans[0][0]*patt[P_TERRE].width/2., patt[P_TERRE].trans[1][3]+patt[P_TERRE].trans[1][0]*patt[P_TERRE].width/2., patt[P_TERRE].trans[2][3]+patt[P_TERRE].trans[2][0]*patt[P_TERRE].width/2.);
			fprintf(fstatsT, "%lf %lf %lf ", patt[P_TERRE].trans[0][3]+patt[P_TERRE].trans[0][1]*patt[P_TERRE].width/2., patt[P_TERRE].trans[1][3]+patt[P_TERRE].trans[1][1]*patt[P_TERRE].width/2., patt[P_TERRE].trans[2][3]+patt[P_TERRE].trans[2][1]*patt[P_TERRE].width/2.);
			fprintf(fstatsT, "\n");
		}

		if (patt[P_LUNE].visible == 1) {
			fprintf(fstatsL, "%d ", numImage);
			for (i = 0; i < 3; i++)
				for (j = 0; j < 4; j++)
					fprintf(fstatsL, "%lf ", patt[P_LUNE].trans[i][j]);
			fprintf(fstatsL, "%lf %lf %lf ", patt[P_LUNE].trans[0][3], patt[P_LUNE].trans[1][3], patt[P_LUNE].trans[2][3]);
			fprintf(fstatsL, "%lf %lf %lf ", patt[P_LUNE].trans[0][3]+patt[P_LUNE].trans[0][0]*patt[P_LUNE].width/2., patt[P_LUNE].trans[1][3]+patt[P_LUNE].trans[1][0]*patt[P_LUNE].width/2., patt[P_LUNE].trans[2][3]+patt[P_LUNE].trans[2][0]*patt[P_LUNE].width/2.);
			fprintf(fstatsL, "%lf %lf %lf ", patt[P_LUNE].trans[0][3]+patt[P_LUNE].trans[0][1]*patt[P_LUNE].width/2., patt[P_LUNE].trans[1][3]+patt[P_LUNE].trans[1][1]*patt[P_LUNE].width/2., patt[P_LUNE].trans[2][3]+patt[P_LUNE].trans[2][1]*patt[P_LUNE].width/2.);
			fprintf(fstatsL, "\n");
		}

		if (patt[P_SOLEIL].visible == 1) {
			fprintf(fstatsS, "%d ", numImage);
			for (i = 0; i < 3; i++)
				for (j = 0; j < 4; j++)
					fprintf(fstatsS, "%lf ", patt[P_SOLEIL].trans[i][j]);
			fprintf(fstatsS, "%lf %lf %lf ", patt[P_SOLEIL].trans[0][3], patt[P_SOLEIL].trans[1][3], patt[P_SOLEIL].trans[2][3]);
			fprintf(fstatsS, "%lf %lf %lf ", patt[P_SOLEIL].trans[0][3]+patt[P_SOLEIL].trans[0][0]*patt[P_SOLEIL].width/2., patt[P_SOLEIL].trans[1][3]+patt[P_SOLEIL].trans[1][0]*patt[P_SOLEIL].width/2., patt[P_SOLEIL].trans[2][3]+patt[P_SOLEIL].trans[2][0]*patt[P_SOLEIL].width/2.);
			fprintf(fstatsS, "%lf %lf %lf ", patt[P_SOLEIL].trans[0][3]+patt[P_SOLEIL].trans[0][1]*patt[P_SOLEIL].width/2., patt[P_SOLEIL].trans[1][3]+patt[P_SOLEIL].trans[1][1]*patt[P_SOLEIL].width/2., patt[P_SOLEIL].trans[2][3]+patt[P_SOLEIL].trans[2][1]*patt[P_SOLEIL].width/2.);
			fprintf(fstatsS, "\n");
		}
	}

    draw();

	argSwapBuffers();
}

static void init( void )
{
	int	i;
    ARParam  wparam;
	char buffer[256];
    time_t timestamp = time(NULL);

	strcpy(patt[P_TERRE].name, "Data/patt.terre");
	patt[P_TERRE].id = P_TERRE;
	strcpy(patt[P_ORBITE].name, "Data/patt.orbite");
	patt[P_ORBITE].id = P_ORBITE;
	strcpy(patt[P_SOLEIL].name, "Data/patt.soleil");
	patt[P_SOLEIL].id = P_SOLEIL;
	strcpy(patt[P_PRINTEMPS].name, "Data/patt.printemps");
	patt[P_PRINTEMPS].id = P_PRINTEMPS;
	strcpy(patt[P_ETE].name, "Data/patt.ete");
	patt[P_ETE].id = P_ETE;
	strcpy(patt[P_AUTOMNE].name, "Data/patt.automne");
	patt[P_AUTOMNE].id = P_AUTOMNE;
	strcpy(patt[P_HIVER].name, "Data/patt.hiver");
	patt[P_HIVER].id = P_HIVER;
	strcpy(patt[P_LUNE].name, "Data/patt.lune");
	patt[P_LUNE].id = P_LUNE;
	
	for ( i = 0 ; i < NB_MARKERS ; i++ ) {
		patt[i].center[0] = 0.0; patt[i].center[1] = 0.0;
		patt[i].width = 40.0;
		patt[i].visible = 0;
	}
	
    /* open the video path */
    if( arVideoOpen( vconf ) < 0 ) exit(0);
    /* find the size of the window */
    if( arVideoInqSize(&xsize, &ysize) < 0 ) exit(0);
    printf("Image size (x,y) = (%d,%d)\n", xsize, ysize);

    /* set the initial camera parameters */
    if( arParamLoad(cparam_name, 1, &wparam) < 0 ) {
        printf("Camera parameter load error !!\n");
        exit(0);
    }
    arParamChangeSize( &wparam, xsize, ysize, &cparam );
    arInitCparam( &cparam );
    printf("*** Camera Parameter ***\n");
    arParamDisp( &cparam );


	//load the markers
	for (i = 0 ; i < NB_MARKERS ; i++ )
		if ( (patt[i].id = arLoadPatt(patt[i].name)) < 0 ) {
			printf("pattern load error !!\n");
			exit(0);
		}	
	
    /* open the graphics window */
    argInit( &cparam, ZOOM, 0, 0, 0, 0 );

	glEnable(GL_LIGHTING);
	glEnable(GL_LIGHT0);
	glLightfv(GL_LIGHT0, GL_POSITION, light_position);
	glLightfv(GL_LIGHT0, GL_DIFFUSE, diffuse_light);
	glDisable(GL_LIGHT0);
		
	init_texture();
	creerPlanete(LUNE, RAYON_LUNE);
	creerPlanete(TERRE, RAYON_TERRE);
	creerPlanete(SOLEIL, RAYON_SOLEIL);
	creerPlanete(SOLEIL_MANIP, RAYON_SOLEIL_MANIP);

	if (IMPORT) {
		int annee,jour,heure,minutes;
		char mois[1024];

		printf("Annee (ex:2012)? ");
		scanf("%d", &annee);
		printf("Mois (ex:May) ? ");
		scanf("%s", mois);
		printf("Jour (ex:9)? ");
		scanf("%d", &jour);
		printf("Heure (ex:13)? ");
		scanf("%d", &heure);
		printf("Minutes (ex:37)? ");
		scanf("%d", &minutes);
		sprintf(buffer,"statsT_%d-%s-%02d-%02dh%02d.txt", annee, mois, jour, heure, minutes);
		printf("Ouverture du fichier %s\n", buffer);
		if (fopen_s(&fstatsT, buffer, "r" )>0) {
			printf("\nLe fichier %s n'existe pas dans le repertoire bin !\nAPPUYEZ SUR LA TOUCHE ENTREE", buffer);
			scanf("%s", mois);
			exit(0);
		}
		sprintf(buffer,"statsL_%d-%s-%02d-%02dh%02d.txt", annee, mois, jour, heure, minutes);
		printf("Ouverture du fichier %s\n", buffer);
		if (fopen_s(&fstatsL, buffer, "r" )>0) {
			printf("\nLe fichier %s n'existe pas dans le repertoire bin !\nAPPUYEZ SUR LA TOUCHE ENTREE", buffer);
			scanf("%s", mois);
			exit(0);
		}
		sprintf(buffer,"statsS_%d-%s-%02d-%02dh%02d.txt", annee, mois, jour, heure, minutes);
		printf("Ouverture du fichier %s\n", buffer);
		if (fopen_s(&fstatsS, buffer, "r" )>0) {
			printf("\nLe fichier %s n'existe pas dans le repertoire bin !\nAPPUYEZ SUR LA TOUCHE ENTREE", buffer);
			scanf("%s", mois);
			exit(0);
		}
		fopen_s(&fstatsS, buffer, "r" );
		for (i = 0; i < NB_MARKERS; i++)
			next[i] = 1;
	}
	else {
		strftime(buffer, sizeof(buffer), "%Y-%b-%d-%H:%M", localtime(&timestamp));
		fprintf(stderr, "EXPERIENCE %s\n", buffer);
	
		strftime(buffer, sizeof(buffer), "statsT_%Y-%b-%d-%Hh%M.txt", localtime(&timestamp));
		fopen_s(&fstatsT, buffer, "w+" );
		strftime(buffer, sizeof(buffer), "statsL_%Y-%b-%d-%Hh%M.txt", localtime(&timestamp));
		fopen_s(&fstatsL, buffer, "w+" );
		strftime(buffer, sizeof(buffer), "statsS_%Y-%b-%d-%Hh%M.txt", localtime(&timestamp));
		fopen_s(&fstatsS, buffer, "w+" );
	}
}

/* cleanup function called when program exits */
static void cleanup(void)
{
    arVideoCapStop();
    arVideoClose();
    argCleanup();
}

static void drawOrbiteEtLune(GLfloat ambient[], int drawOrbit, int drawMen) {
	glPushMatrix();
		glEnable(GL_LIGHTING);
		glEnable(GL_LIGHT0);

		//Orbite de la lune
		if (drawOrbit) {
			glPushMatrix();
				glTranslatef(0, 0, RAYON_TERRE);
				drawDashedCircle(DISTANCE_LUNE, color_orbit, asin(TAILLE_TIRETS/DISTANCE_LUNE));
			glPopMatrix();
		}
		//Lune
		glPushMatrix();
			glTranslatef(0, 0, RAYON_TERRE);
			// inclinaison du plan orbital
			glTranslatef(DISTANCE_LUNE*cos(angleOrbite),DISTANCE_LUNE*sin(angleOrbite), 0.);
			glRotatef(angleSatellite, 0.0, 0.0, 1.0);
			glMaterialfv(GL_FRONT, GL_AMBIENT, ambient);
			glMaterialfv(GL_FRONT, GL_DIFFUSE, diffuse_texture);
			drawPlanete(LUNE);
			if (drawMen) {
				glTranslatef(-RAYON_LUNE, 0., 0.);
				glScalef(0.03, 0.03, 0.03);
				glRotatef(-90, 0.0, 1.0, 0.0);
				glRotatef(90, 0.0, 0.0, 1.0);
				drawMan(MAN);
			}
		glPopMatrix();
	glPopMatrix();
	glDisable(GL_LIGHTING);
}

static void drawSun(int nom) {
	glPushMatrix();
		glEnable(GL_LIGHTING);
		glDisable(GL_LIGHT0);
		glEnable(GL_LIGHT1);
		glLightfv(GL_LIGHT1, GL_POSITION, light_position_soleil);
		glLightfv(GL_LIGHT1, GL_DIFFUSE, diffuse_light);
		glLightfv(GL_LIGHT1, GL_AMBIENT, diffuse_light);
		glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, ambient_soleil);
		glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, diffuse_soleil);
		drawPlanete(nom);
		glDisable(GL_LIGHT1);
		glEnable(GL_LIGHT0);
	glPopMatrix();	
}

static void drawSysteme() {
	GLfloat	light_position2[]  = {light_position[0],light_position[1],light_position[2]-RAYON_TERRE,0.0};
    double gl_para[16];

	/* dessin du soleil en fonction de la saison */
	glPushMatrix();
		glMatrixMode(GL_MODELVIEW);
		argConvGlpara(patt[P_TERRE].trans, gl_para);
		glLoadMatrixd( gl_para );
		if (swap) glRotatef(-90., 1., 0., 0.);
		glScalef(scale, scale, scale);
		glTranslatef( 0.0, 0.0, RAYON_TERRE );
		glRotatef(-inclinaison, 0.0, 1.0, 0.0 );
		glTranslatef(cos((float)saison*PI/2.)*DISTANCE_SOLEIL, sin((float)saison*PI/2.)*DISTANCE_SOLEIL, 0.);
		drawSun(SOLEIL);
	glPopMatrix();

	/* dessin de la lune et de son orbite */

	glMatrixMode(GL_MODELVIEW);
	argConvGlpara(patt[P_TERRE].trans, gl_para);
	glLoadMatrixd( gl_para );	

	glPushMatrix(); 
		if (swap) glRotatef(-90., 1., 0., 0.);
		glScalef(scale, scale, scale);
			//drawOrbiteEtLune(ambient_texture, showTraits, showMen);
		drawOrbiteEtLune(ambient_texture_sombre, showTraits, showMen);
	glPopMatrix();

	//glPopMatrix();
}

static void drawTerre() {
	GLfloat	  orig[3], dest[3];

	glPushMatrix(); 
		if (swap && showSysteme) glRotatef(-90., 1., 0., 0.);
		glScalef(scale, scale, scale);
		glTranslatef(0, 0, RAYON_TERRE);
		//glMaterialfv(GL_FRONT, GL_AMBIENT, ambient_texture);
		glMaterialfv(GL_FRONT, GL_AMBIENT, ambient_texture_sombre);
		glMaterialfv(GL_FRONT, GL_DIFFUSE, diffuse_texture);							
		glPushMatrix();
			glRotatef(-inclinaison, 0.0, 1.0, 0.0 );
			glRotatef(INCLINAISON_TERRE, 0., 1., 0.);
			//glRotatef(((int)hour%24)/24.*360., 0.0, 0.0, 1.0 );
			glRotatef(hour/24.*360., 0.0, 0.0, 1.0 );
			if (wire) glutWireSphere(RAYON_TERRE+0.2, 24, 11);
			drawPlanete(TERRE);
			orig[0]=orig[1]=dest[0]=dest[1]=0.;
			orig[2]=-DEMILONGUEUR_AXE;
			dest[2]=DEMILONGUEUR_AXE;
			if (showTraits)	drawDashedLine(orig, dest, color_earthaxis, TAILLE_TIRETS);
		glPopMatrix();
		if (showMen) {
			glPushMatrix();
				//glRotatef(angleOrbite/PI*180., 0., 0., 1.);
				glRotatef(longitude+hour/24.*360., 0.0, 0.0, 1.0 );
				glRotatef(-latitude, 0., 1., 0.);
				glTranslatef(RAYON_TERRE, 0., 0.);
				glScalef(0.05, 0.05, 0.05);
				glRotatef(90, 0.0, 1.0, 0.0);
				glRotatef(90, 0.0, 0.0, 1.0);
				drawMan(WOMAN);
			glPopMatrix();
		}
		glDisable( GL_LIGHTING );
	glPopMatrix();
}

static void draw( void )
{
    double    gl_para[16];


    argDrawMode3D();
    argDraw3dCamera( 0, 0 );
    glClearDepth( 1.0 );
    glClear(GL_DEPTH_BUFFER_BIT);
    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LEQUAL);
	
	jour = hour/24.0;
	angleOrbite=2.0*PI*jour/REVOLUTION_ORBITE;
	angleSatellite=360.0*jour/REVOLUTION_SATELLITE;

	if (IMPORT && patt[P_TERRE].visible) {
		glMatrixMode(GL_MODELVIEW);
		argConvGlpara(patt[P_TERRE].trans, gl_para);
		glLoadMatrixd( gl_para );
		glBegin(GL_POLYGON);
			glVertex3f(-patt[P_TERRE].width/2.,-patt[P_TERRE].width/2.,0.);
			glVertex3f(-patt[P_TERRE].width/2.,patt[P_TERRE].width/2.,0.);
			glVertex3f(patt[P_TERRE].width/2.,patt[P_TERRE].width/2.,0.);
			glVertex3f(patt[P_TERRE].width/2.,-patt[P_TERRE].width/2.,0.);
		glEnd();
	}

	if (IMPORT && patt[P_LUNE].visible) {
		glMatrixMode(GL_MODELVIEW);
		argConvGlpara(patt[P_LUNE].trans, gl_para);
		glLoadMatrixd( gl_para );
		glBegin(GL_POLYGON);
			glVertex3f(-patt[P_LUNE].width/2.,-patt[P_LUNE].width/2.,0.);
			glVertex3f(-patt[P_LUNE].width/2.,patt[P_LUNE].width/2.,0.);
			glVertex3f(patt[P_LUNE].width/2.,patt[P_LUNE].width/2.,0.);
			glVertex3f(patt[P_LUNE].width/2.,-patt[P_LUNE].width/2.,0.);
		glEnd();
	}

	if (IMPORT && patt[P_SOLEIL].visible) {
		glMatrixMode(GL_MODELVIEW);
		argConvGlpara(patt[P_SOLEIL].trans, gl_para);
		glLoadMatrixd( gl_para );
		glBegin(GL_POLYGON);
			glVertex3f(-patt[P_SOLEIL].width/2.,-patt[P_SOLEIL].width/2.,0.);
			glVertex3f(-patt[P_SOLEIL].width/2.,patt[P_SOLEIL].width/2.,0.);
			glVertex3f(patt[P_SOLEIL].width/2.,patt[P_SOLEIL].width/2.,0.);
			glVertex3f(patt[P_SOLEIL].width/2.,-patt[P_SOLEIL].width/2.,0.);
		glEnd();
	}

	if (!showSysteme) {

		/* dessin du soleil si le marqueur est visible */

		if (patt[P_SOLEIL].visible) {
			glMatrixMode(GL_MODELVIEW);
			argConvGlpara(patt[P_SOLEIL].trans, gl_para);
			glLoadMatrixd( gl_para );
			glScalef(scale, scale, scale);
			glTranslatef( 0.0, 0.0, RAYON_SOLEIL_MANIP );
			drawSun(SOLEIL_MANIP);
			soleilVisible = 1;
		}

		/* dessin de la lune si le marqueur est visible */

		if (patt[P_LUNE].visible) {
			GLfloat	  orig[3], dest[3];

			orig[0]= patt[P_LUNE].trans[0][2]*scale*(RAYON_LUNE+RAYON_SOLEIL_MANIP/2.)+patt[P_LUNE].trans[0][3];
			orig[1]= patt[P_LUNE].trans[1][2]*scale*(RAYON_LUNE+RAYON_SOLEIL_MANIP/2.)+patt[P_LUNE].trans[1][3];
			orig[2]= patt[P_LUNE].trans[2][2]*scale*(RAYON_LUNE+RAYON_SOLEIL_MANIP/2.)+patt[P_LUNE].trans[2][3];

			if (patt[P_SOLEIL].visible) {
				double pattLTransInverted[3][4];
				GLfloat l_position[4];

				arUtilMatInv(patt[P_LUNE].trans, pattLTransInverted);
				dest[0]= patt[P_SOLEIL].trans[0][2]*scale*RAYON_SOLEIL_MANIP+patt[P_SOLEIL].trans[0][3];
				dest[1]= patt[P_SOLEIL].trans[1][2]*scale*RAYON_SOLEIL_MANIP+patt[P_SOLEIL].trans[1][3];
				dest[2]= patt[P_SOLEIL].trans[2][2]*scale*RAYON_SOLEIL_MANIP+patt[P_SOLEIL].trans[2][3];
				l_position[0] = dest[0]-orig[0];
				l_position[1] = dest[1]-orig[1];
				l_position[2] = dest[2]-orig[2];
				l_position[3] = 0.0;
				lightposLL[0] = pattLTransInverted[0][0]*l_position[0] + pattLTransInverted[0][1]*l_position[1] + pattLTransInverted[0][2]*l_position[2];
				lightposLL[1] = pattLTransInverted[1][0]*l_position[0] + pattLTransInverted[1][1]*l_position[1] + pattLTransInverted[1][2]*l_position[2];
				lightposLL[2] = pattLTransInverted[2][0]*l_position[0] + pattLTransInverted[2][1]*l_position[1] + pattLTransInverted[2][2]*l_position[2];
				lightposLL[3] = 0.;
				sunposLL[0] = pattLTransInverted[0][0]*dest[0] + pattLTransInverted[0][1]*dest[1] + pattLTransInverted[0][2]*dest[2] + pattLTransInverted[0][3];
				sunposLL[1] = pattLTransInverted[1][0]*dest[0] + pattLTransInverted[1][1]*dest[1] + pattLTransInverted[1][2]*dest[2] + pattLTransInverted[1][3];
				sunposLL[2] = pattLTransInverted[2][0]*dest[0] + pattLTransInverted[2][1]*dest[1] + pattLTransInverted[2][2]*dest[2] + pattLTransInverted[2][3];
			}
			else if (soleilVisible) {
				dest[0] = patt[P_LUNE].trans[0][0]*sunposLL[0] + patt[P_LUNE].trans[0][1]*sunposLL[1] + patt[P_LUNE].trans[0][2]*sunposLL[2] + patt[P_LUNE].trans[0][3];
				dest[1] = patt[P_LUNE].trans[1][0]*sunposLL[0] + patt[P_LUNE].trans[1][1]*sunposLL[1] + patt[P_LUNE].trans[1][2]*sunposLL[2] + patt[P_LUNE].trans[1][3];
				dest[2] = patt[P_LUNE].trans[2][0]*sunposLL[0] + patt[P_LUNE].trans[2][1]*sunposLL[1] + patt[P_LUNE].trans[2][2]*sunposLL[2] + patt[P_LUNE].trans[2][3];

				glMatrixMode(GL_MODELVIEW);
				argConvGlpara(patt[P_LUNE].trans, gl_para);
				glLoadMatrixd( gl_para );

				glTranslatef( sunposLL[0], sunposLL[1], sunposLL[2] );
				glScalef(scale, scale, scale);
				drawSun(SOLEIL_MANIP);
			}

			if (patt[P_TERRE].visible && soleilVisible) {
				double patt0TransInverted[3][4];
				GLfloat l_position[4];
				GLfloat	  orig2[3];

				arUtilMatInv(patt[P_TERRE].trans, patt0TransInverted);

				l_position[0] = dest[0]-orig[0];
				l_position[1] = dest[1]-orig[1];
				l_position[2] = dest[2]-orig[2];
				lightposLT[0] = patt0TransInverted[0][0]*l_position[0] + patt0TransInverted[0][1]*l_position[1] + patt0TransInverted[0][2]*l_position[2];
				lightposLT[1] = patt0TransInverted[1][0]*l_position[0] + patt0TransInverted[1][1]*l_position[1] + patt0TransInverted[1][2]*l_position[2];
				lightposLT[2] = patt0TransInverted[2][0]*l_position[0] + patt0TransInverted[2][1]*l_position[1] + patt0TransInverted[2][2]*l_position[2];
				sunposLT[0] = patt0TransInverted[0][0]*dest[0] + patt0TransInverted[0][1]*dest[1] + patt0TransInverted[0][2]*dest[2] + patt0TransInverted[0][3];
				sunposLT[1] = patt0TransInverted[1][0]*dest[0] + patt0TransInverted[1][1]*dest[1] + patt0TransInverted[1][2]*dest[2] + patt0TransInverted[1][3];
				sunposLT[2] = patt0TransInverted[2][0]*dest[0] + patt0TransInverted[2][1]*dest[1] + patt0TransInverted[2][2]*dest[2] + patt0TransInverted[2][3];

				orig2[0]= patt[P_TERRE].trans[0][2]*scale*RAYON_TERRE+patt[P_TERRE].trans[0][3];
				orig2[1]= patt[P_TERRE].trans[1][2]*scale*RAYON_TERRE+patt[P_TERRE].trans[1][3];
				orig2[2]= patt[P_TERRE].trans[2][2]*scale*RAYON_TERRE+patt[P_TERRE].trans[2][3];
				l_position[0] = dest[0]-orig2[0];
				l_position[1] = dest[1]-orig2[1];
				l_position[2] = dest[2]-orig2[2];
				l_position[3] = 0.0;
				lightposTT[0] = patt0TransInverted[0][0]*l_position[0] + patt0TransInverted[0][1]*l_position[1] + patt0TransInverted[0][2]*l_position[2];
				lightposTT[1] = patt0TransInverted[1][0]*l_position[0] + patt0TransInverted[1][1]*l_position[1] + patt0TransInverted[1][2]*l_position[2];
				lightposTT[2] = patt0TransInverted[2][0]*l_position[0] + patt0TransInverted[2][1]*l_position[1] + patt0TransInverted[2][2]*l_position[2];
				sunposTT[0] = patt0TransInverted[0][0]*dest[0] + patt0TransInverted[0][1]*dest[1] + patt0TransInverted[0][2]*dest[2] + patt0TransInverted[0][3];
				sunposTT[1] = patt0TransInverted[1][0]*dest[0] + patt0TransInverted[1][1]*dest[1] + patt0TransInverted[1][2]*dest[2] + patt0TransInverted[1][3];
				sunposTT[2] = patt0TransInverted[2][0]*dest[0] + patt0TransInverted[2][1]*dest[1] + patt0TransInverted[2][2]*dest[2] + patt0TransInverted[2][3];
			}

			if (showTraits && soleilVisible) {
				glMatrixMode(GL_MODELVIEW);
				glLoadIdentity();
				glPushMatrix(); 
					//if (swap) glRotatef(-90., 1., 0., 0.);
					drawDashedLine(orig, dest, color_sunray, TAILLE_TIRETS);
				glPopMatrix(); 
			}

			glMatrixMode(GL_MODELVIEW);
			argConvGlpara(patt[P_LUNE].trans, gl_para);
			glLoadMatrixd( gl_para );

			glLightfv(GL_LIGHT0, GL_POSITION, lightposLL);
			glLightfv(GL_LIGHT0, GL_DIFFUSE, diffuse_light);
			glLightfv(GL_LIGHT0, GL_AMBIENT, ambient_light);

			glMatrixMode(GL_MODELVIEW);
			argConvGlpara(patt[P_LUNE].trans, gl_para);
			glLoadMatrixd( gl_para );	
			glPushMatrix();
				//if (swap) glRotatef(-90., 1., 0., 0.);
				glScalef(scale, scale, scale);
				glEnable(GL_LIGHTING);
				glEnable(GL_LIGHT0);
				//glMaterialfv(GL_FRONT, GL_AMBIENT, ambient_texture);
				glMaterialfv(GL_FRONT, GL_AMBIENT, ambient_texture_sombre);
				glMaterialfv(GL_FRONT, GL_DIFFUSE, diffuse_texture);
				glTranslatef( 0.0, 0.0, RAYON_LUNE+RAYON_SOLEIL_MANIP/2. );
				drawPlanete(LUNE);
				glDisable( GL_LIGHTING );
			glPopMatrix();
		}

		/* dessin de la terre si le marqueur est visible */

		if (patt[P_TERRE].visible) {
			GLfloat	  orig[3], dest[3];
			double patt0TransInverted[3][4];

			arUtilMatInv(patt[P_TERRE].trans, patt0TransInverted);
			orig[0]= patt[P_TERRE].trans[0][2]*scale*RAYON_TERRE+patt[P_TERRE].trans[0][3];
			orig[1]= patt[P_TERRE].trans[1][2]*scale*RAYON_TERRE+patt[P_TERRE].trans[1][3];
			orig[2]= patt[P_TERRE].trans[2][2]*scale*RAYON_TERRE+patt[P_TERRE].trans[2][3];

			if (patt[P_SOLEIL].visible) {
				dest[0]= patt[P_SOLEIL].trans[0][2]*scale*RAYON_SOLEIL_MANIP+patt[P_SOLEIL].trans[0][3];
				dest[1]= patt[P_SOLEIL].trans[1][2]*scale*RAYON_SOLEIL_MANIP+patt[P_SOLEIL].trans[1][3];
				dest[2]= patt[P_SOLEIL].trans[2][2]*scale*RAYON_SOLEIL_MANIP+patt[P_SOLEIL].trans[2][3];
				light_position[0] = dest[0]-orig[0];
				light_position[1] = dest[1]-orig[1];
				light_position[2] = dest[2]-orig[2];
				light_position[3] = 0.0;			
				lightposTT[0] = patt0TransInverted[0][0]*light_position[0] + patt0TransInverted[0][1]*light_position[1] + patt0TransInverted[0][2]*light_position[2];
				lightposTT[1] = patt0TransInverted[1][0]*light_position[0] + patt0TransInverted[1][1]*light_position[1] + patt0TransInverted[1][2]*light_position[2];
				lightposTT[2] = patt0TransInverted[2][0]*light_position[0] + patt0TransInverted[2][1]*light_position[1] + patt0TransInverted[2][2]*light_position[2];
				lightposTT[3] = 0.;
			}
			else if (patt[P_LUNE].visible && soleilVisible) {
				dest[0] = patt[P_TERRE].trans[0][0]*sunposTT[0] + patt[P_TERRE].trans[0][1]*sunposTT[1] + patt[P_TERRE].trans[0][2]*sunposTT[2] + patt[P_TERRE].trans[0][3];
				dest[1] = patt[P_TERRE].trans[1][0]*sunposTT[0] + patt[P_TERRE].trans[1][1]*sunposTT[1] + patt[P_TERRE].trans[1][2]*sunposTT[2] + patt[P_TERRE].trans[1][3];
				dest[2] = patt[P_TERRE].trans[2][0]*sunposTT[0] + patt[P_TERRE].trans[2][1]*sunposTT[1] + patt[P_TERRE].trans[2][2]*sunposTT[2] + patt[P_TERRE].trans[2][3];
			}
			else 
				lightposTT[0] = lightposTT[1] = lightposTT[2] = 0.;
			
			if (showTraits && (patt[P_SOLEIL].visible || (patt[P_LUNE].visible && soleilVisible))) {
				glMatrixMode(GL_MODELVIEW);
				glLoadIdentity();
				glPushMatrix(); 
					//if (swap) glRotatef(-90., 1., 0., 0.);
					drawDashedLine(orig, dest, color_sunray, TAILLE_TIRETS);
				glPopMatrix(); 
			}

			glMatrixMode(GL_MODELVIEW);
			argConvGlpara(patt[P_TERRE].trans, gl_para);
			glLoadMatrixd( gl_para );	

			glEnable(GL_LIGHTING);
			glEnable(GL_LIGHT0);
			glLightfv(GL_LIGHT0, GL_POSITION, lightposTT);
			glLightfv(GL_LIGHT0, GL_DIFFUSE, diffuse_light);
			glLightfv(GL_LIGHT0, GL_AMBIENT, ambient_light);
			glMaterialfv(GL_FRONT, GL_AMBIENT, ambient_soleil);
			glMaterialfv(GL_FRONT, GL_DIFFUSE, diffuse_soleil);

			drawTerre(); 
		}
	}
	else if (patt[P_TERRE].visible) {
		float alpha = inclinaison/180.*PI;
		GLfloat	  orig[3], dest[3];
		
		/* Dessin du systeme Terre - Lune - Soleil */

		glMatrixMode(GL_MODELVIEW);
		argConvGlpara(patt[P_TERRE].trans, gl_para);
		glLoadMatrixd( gl_para );	

		light_position[0] = cos((float)saison*PI/2.)*cos(alpha)*DISTANCE_SOLEIL;
		light_position[1] = sin((float)saison*PI/2.)*cos(alpha)*DISTANCE_SOLEIL;
		light_position[2] = cos((float)saison*PI/2.)*sin(alpha)*DISTANCE_SOLEIL;
		light_position[3] = 0.0;
		orig[0] = 0.;
		orig[1] = 0.;
		orig[2] = scale*RAYON_TERRE;
		dest[0] = cos((float)saison*PI/2.)*cos(alpha)*scale*DISTANCE_SOLEIL;
		dest[1] = sin((float)saison*PI/2.)*cos(alpha)*scale*DISTANCE_SOLEIL;
		dest[2] = cos((float)saison*PI/2.)*sin(alpha)*scale*DISTANCE_SOLEIL+scale*RAYON_TERRE;

		if (showTraits) {
			glPushMatrix(); 
				if (swap) glRotatef(-90., 1., 0., 0.);
				drawDashedLine(orig, dest, color_sunray, TAILLE_TIRETS);
			glPopMatrix(); 
		}

		glEnable(GL_LIGHTING);
		glEnable(GL_LIGHT0);
		//if (swap) glRotatef(-90., 1., 0., 0.);
		glLightfv(GL_LIGHT0, GL_POSITION, light_position);
		glLightfv(GL_LIGHT0, GL_DIFFUSE, diffuse_light);
		glLightfv(GL_LIGHT0, GL_AMBIENT, ambient_light);
		glMaterialfv(GL_FRONT, GL_AMBIENT, ambient_soleil);
		glMaterialfv(GL_FRONT, GL_DIFFUSE, diffuse_soleil);

		drawTerre();

		drawSysteme();
	}

	/* Vignette montrant la lune et le soleil depuis le point de vue observateur */

	if (showVignette && patt[P_TERRE].visible && (patt[P_LUNE].visible || showSysteme)) { 
		float normal[3];
		float posMan[3];
		float posLune[3];
		float X[3], Y[3], Z[3];
		float norme;
		int i;
		double trans[3][4];
		double transInv[3][4];
		int nuit;
		int luneVisible;
		double patt0TransInverted[3][4];

		arUtilMatInv(patt[P_TERRE].trans, patt0TransInverted);

		glViewport(ZOOM*(xsize-xsize/4.-5.),ZOOM*5,ZOOM*xsize/4.,ZOOM*xsize/4.*480./640.);
		//glPushMatrix();
		normal[0] = cos((longitude+hour/24.*360.)/180.*PI)*cos(latitude/180.*PI);
		normal[1] = sin((longitude+hour/24.*360.)/180.*PI)*cos(latitude/180.*PI);
		normal[2] = sin(latitude/180.*PI);
		posMan[0] = scale*RAYON_TERRE*cos((longitude+hour/24.*360.)/180.*PI)*cos(latitude/180.*PI);
		posMan[1] = scale*RAYON_TERRE*sin((longitude+hour/24.*360.)/180.*PI)*cos(latitude/180.*PI);
		posMan[2] = scale*RAYON_TERRE*sin(latitude/180.*PI)+scale*RAYON_TERRE;

		if (!showSysteme) {
			float posLuneView[3];

			posLuneView[0] = patt[P_LUNE].trans[0][2]*scale*(RAYON_LUNE+RAYON_SOLEIL_MANIP/2.)+patt[P_LUNE].trans[0][3];
			posLuneView[1] = patt[P_LUNE].trans[1][2]*scale*(RAYON_LUNE+RAYON_SOLEIL_MANIP/2.)+patt[P_LUNE].trans[1][3];
			posLuneView[2] = patt[P_LUNE].trans[2][2]*scale*(RAYON_LUNE+RAYON_SOLEIL_MANIP/2.)+patt[P_LUNE].trans[2][3];
			
			posLune[0] = patt0TransInverted[0][0]*posLuneView[0] + patt0TransInverted[0][1]*posLuneView[1] + patt0TransInverted[0][2]*posLuneView[2] + patt0TransInverted[0][3];
			posLune[1] = patt0TransInverted[1][0]*posLuneView[0] + patt0TransInverted[1][1]*posLuneView[1] + patt0TransInverted[1][2]*posLuneView[2] + patt0TransInverted[1][3];
			posLune[2] = patt0TransInverted[2][0]*posLuneView[0] + patt0TransInverted[2][1]*posLuneView[1] + patt0TransInverted[2][2]*posLuneView[2] + patt0TransInverted[2][3];
		}
		else {
			posLune[0] = scale*DISTANCE_LUNE*cos(angleOrbite);
			posLune[1] = scale*DISTANCE_LUNE*sin(angleOrbite);
			posLune[2] = scale*RAYON_TERRE;
		}
	
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		//glPushMatrix();	
		if (!showSysteme)
			nuit = ((sunposTT[0]-posMan[0])*normal[0]+(sunposTT[1]-posMan[1])*normal[1]+(sunposTT[2]-posMan[2])*normal[2] < 0.);
		else {
			float alpha = inclinaison/180.*PI;
			double solTerre[3];

			solTerre[0] = cos((float)saison*PI/2.)*cos(alpha)*scale*DISTANCE_SOLEIL;
			solTerre[1] = sin((float)saison*PI/2.)*cos(alpha)*scale*DISTANCE_SOLEIL;
			solTerre[2] = cos((float)saison*PI/2.)*sin(alpha)*scale*DISTANCE_SOLEIL+scale*RAYON_TERRE;

			nuit = ((solTerre[0]-posMan[0])*normal[0]+(solTerre[1]-posMan[1])*normal[1]+(solTerre[2]-posMan[2])*normal[2] < 0.);
		}

		glScalef(scale, scale, scale);

		if (nuit) {
			glEnable(GL_TEXTURE_2D);								
			glTexEnvi(GL_TEXTURE_ENV,  GL_TEXTURE_ENV_MODE, GL_REPLACE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,  GL_REPEAT); 
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,  GL_REPEAT); 
			if(bpp[ETOILES]==24)
					glTexImage2D(GL_TEXTURE_2D,0,3,
					im[ETOILES].sizeX,im[ETOILES].sizeY,
					0,GL_RGB,GL_UNSIGNED_BYTE,im[ETOILES].data);
			else if(bpp[ETOILES]==32)
					glTexImage2D(GL_TEXTURE_2D,0,4,
					im[ETOILES].sizeX,im[ETOILES].sizeY,
					0,GL_RGBA,GL_UNSIGNED_BYTE,im[ETOILES].data);
			glMaterialfv(GL_FRONT, GL_DIFFUSE, diffuse_terre);
			glMaterialfv(GL_FRONT, GL_AMBIENT, ambient_terre);
			glEnable(GL_LIGHTING);
			glEnable(GL_LIGHT0);
			glBegin(GL_POLYGON);
				glNormal3d(0.0,0.0,1.0);
				glTexCoord2d(0,0);
				glVertex3f(-scale*1000.,-scale*1000.,scale*1000.);
				glTexCoord2d(0,1400);
				glVertex3f(-scale*1000.,scale*1000.,scale*1000.);
				glTexCoord2d(1400,1400);
				glVertex3f(scale*1000.,scale*1000.,scale*1000.);
				glTexCoord2d(1400,0);
				glVertex3f(scale*1000.,-scale*1000.,scale*1000.);
			glEnd();
			glDisable(GL_LIGHTING);
			glDisable(GL_TEXTURE_2D);
		}
		else {
			Z[0] = -normal[0];
			Z[1] = -normal[1];
			Z[2] = -normal[2];
			X[0] = normal[1];
			X[1] = -normal[0];
			X[2] = 0.;
			Y[0] = Z[1]*X[2]-Z[2]*X[1];
			Y[1] = Z[2]*X[0]-Z[0]*X[2];
			Y[2] = Z[0]*X[1]-Z[1]*X[0];
			trans[0][0] = X[0];
			trans[1][0] = X[1];
			trans[2][0] = X[2];
			trans[0][1] = Y[0];
			trans[1][1] = Y[1];
			trans[2][1] = Y[2];
			trans[0][2] = Z[0];
			trans[1][2] = Z[1];
			trans[2][2] = Z[2];
				
			trans[0][3] = 1000.*(posMan[0]+10.*normal[0]);
			trans[1][3] = 1000.*(posMan[1]+10.*normal[1]);
			trans[2][3] = 1000.*(posMan[2]+10.*normal[2]);
			arUtilMatInv(trans, transInv);
			argConvGlpara(transInv, gl_para);
			glMatrixMode(GL_MODELVIEW);
			glLoadMatrixd( gl_para );

			glMaterialfv(GL_FRONT, GL_DIFFUSE, diffuse_terre);
			glMaterialfv(GL_FRONT, GL_AMBIENT, ambient_terre);
			glEnable(GL_LIGHTING);
			glEnable(GL_LIGHT0);
			if (!showSysteme) {
				glLightfv(GL_LIGHT0, GL_POSITION, lightposTT);
			}
			else {
				glLightfv(GL_LIGHT0, GL_POSITION, light_position);
			}
			glTranslatef(0.,0.,1000.*scale*RAYON_TERRE);
			glutSolidSphere(1000.*scale*RAYON_TERRE, 20.0, 20.0);
		}

		Z[0] = posLune[0]-posMan[0];
		Z[1] = posLune[1]-posMan[1];
		Z[2] = posLune[2]-posMan[2];
		norme = sqrt(Z[0]*Z[0]+Z[1]*Z[1]+Z[2]*Z[2]);
		Z[0]/=norme;Z[1]/=norme;Z[2]/=norme;
		X[0] = normal[1]*Z[2]-normal[2]*Z[1];
		X[1] = normal[2]*Z[0]-normal[0]*Z[2];
		X[2] = normal[0]*Z[1]-normal[1]*Z[0];
		Y[0] = Z[1]*X[2]-Z[2]*X[1];
		Y[1] = Z[2]*X[0]-Z[0]*X[2];
		Y[2] = Z[0]*X[1]-Z[1]*X[0];
		if (normal[0]*Y[0]+normal[1]*Y[1]+normal[2]*Y[2]>0)
			for (i = 0; i < 3; i++) {
				X[i] = -X[i];
				Y[i] = -Y[i];
			}		
		trans[0][0] = X[0];
		trans[1][0] = X[1];
		trans[2][0] = X[2];
		trans[0][1] = Y[0];
		trans[1][1] = Y[1];
		trans[2][1] = Y[2];
		trans[0][2] = Z[0];
		trans[1][2] = Z[1];
		trans[2][2] = Z[2];
		trans[0][3] = posMan[0];
		trans[1][3] = posMan[1];
		trans[2][3] = posMan[2];
				
		arUtilMatInv(trans, transInv);
		argConvGlpara(transInv, gl_para);
		glMatrixMode(GL_MODELVIEW);
		glLoadMatrixd( gl_para );

		luneVisible = (Z[0]*normal[0]+Z[1]*normal[1]+Z[2]*normal[2] > 0.);

		if (!showSysteme && luneVisible) {
			double gl_paratmp[16];
			double cosphase, sinphase, anglephase, V[3];

			glEnable(GL_LIGHTING);
			glEnable(GL_LIGHT0);
			glLightfv(GL_LIGHT0, GL_POSITION, lightposLT);
			glPushMatrix();
				//arUtilMatInv(patt[P_TERRE].trans, patt0TransInverted);
				argConvGlpara(patt0TransInverted, gl_paratmp);
				glMultMatrixd( gl_paratmp );
				argConvGlpara(patt[P_LUNE].trans, gl_paratmp);
				glMultMatrixd( gl_paratmp );
				glScalef(scale, scale, scale);
				//glMaterialfv(GL_FRONT, GL_AMBIENT, ambient_texture);
				glMaterialfv(GL_FRONT, GL_AMBIENT, ambient_texture_sombre);
				glMaterialfv(GL_FRONT, GL_DIFFUSE, diffuse_texture);
				glTranslatef( 0.0, 0.0, RAYON_LUNE + RAYON_SOLEIL_MANIP/2. );
				drawPlanete(LUNE);
				glDisable( GL_LIGHTING );
			glPopMatrix();

			norme = sqrt(lightposLT[0]*lightposLT[0]+lightposLT[1]*lightposLT[1]+lightposLT[2]*lightposLT[2]);
			V[0] = lightposLT[0]/norme;
			V[1] = lightposLT[1]/norme;
			V[2] = lightposLT[2]/norme;
			cosphase = -(Z[0]*V[0]+Z[1]*V[1]+Z[2]*V[2]);
			sinphase = -(X[0]*V[0]+X[1]*V[1]+X[2]*V[2]);
			//anglephase = acos(cosphase)/PI*180.;
			//if (sinphase < 0) anglephase = -anglephase;
			printf("%f\n", cosphase);
			/*
			if (fabs(anglephase) < TOLERANCE_PHASE) printf("PLEINE LUNE\n");
			if (fabs(anglephase-45) < TOLERANCE_PHASE) printf("LUNE GIBBEUSE\n");
			if (fabs(anglephase-90) < TOLERANCE_PHASE) printf("QUARTIER DE LUNE\n");
			if (fabs(anglephase-135) < TOLERANCE_PHASE) printf("CROISSANT DE LUNE\n");
			if (fabs(anglephase-180) < TOLERANCE_PHASE) printf("NOUVELLE LUNE\n");
			*/
			if (fabs(cosphase-1) < TOLERANCE_COSPHASE) printf("PLEINE LUNE\n");
			if (fabs(cosphase-0.5) < TOLERANCE_COSPHASE) printf("LUNE GIBBEUSE\n");
			if (fabs(cosphase) < TOLERANCE_COSPHASE && sinphase>0) printf("DERNIER QUARTIER DE LUNE\n");
			if (fabs(cosphase+0.5) < TOLERANCE_COSPHASE && sinphase>0) printf("DERNIER CROISSANT DE LUNE\n");
			if (fabs(cosphase+1) < TOLERANCE_COSPHASE) printf("NOUVELLE LUNE\n");
			if (fabs(cosphase) < TOLERANCE_COSPHASE && sinphase<0) printf("PREMIER QUARTIER DE LUNE\n");
			if (fabs(cosphase+0.5) < TOLERANCE_COSPHASE && sinphase<0) printf("PREMIER CROISSANT DE LUNE\n");
			
		}
		else if (showSysteme && luneVisible) {
			glEnable(GL_LIGHTING);
			glEnable(GL_LIGHT0);
			glLightfv(GL_LIGHT0, GL_POSITION, light_position);
			glPushMatrix();
				glScalef(scale, scale, scale);
				drawOrbiteEtLune(ambient_texture_sombre,0, 0);
			glPopMatrix();	
		}

		if (!showSysteme && !nuit) {
			double gl_paratmp[16];
			//double patt0TransInverted[3][4]

			if (patt[P_SOLEIL].visible) {
				glPushMatrix();
					//arUtilMatInv(patt[P_TERRE].trans, patt0TransInverted);
					argConvGlpara(patt0TransInverted, gl_paratmp);
					glMultMatrixd( gl_paratmp );
					argConvGlpara(patt[P_SOLEIL].trans, gl_paratmp);
					glMultMatrixd( gl_paratmp );
					glScalef(scale, scale, scale);
					glTranslatef( 0.0, 0.0, RAYON_SOLEIL_MANIP );
					drawSun(SOLEIL_MANIP);
				glPopMatrix();
			}
			else if (patt[P_LUNE].visible && soleilVisible) {
				glPushMatrix();
					glScalef(scale, scale, scale);
					glTranslatef( sunposLT[0], sunposLT[1], sunposLT[2] );
					drawSun(SOLEIL_MANIP);
				glPopMatrix();
			}
		}
		else if (showSysteme && !nuit) {
			glPushMatrix();
				glScalef(scale, scale, scale);
				glTranslatef( 0.0, 0.0, RAYON_TERRE );
				glRotatef(-inclinaison, 0.0, 1.0, 0.0 );
				glTranslatef(cos((float)saison*PI/2.)*DISTANCE_SOLEIL, sin((float)saison*PI/2.)*DISTANCE_SOLEIL, 0.);//glTranslatef(-DISTANCE_SOLEIL, 0., 0.);
				drawSun(SOLEIL);
			glPopMatrix();
		}
	}

    glDisable( GL_DEPTH_TEST );
}

static void drawDashedLine(GLfloat orig[], GLfloat dest[], GLfloat color[], float length) {
	float tx = dest[0]-orig[0];
	float ty = dest[1]-orig[1];
	float tz = dest[2]-orig[2];
	float norme = sqrt(tx*tx+ty*ty+tz*tz);
	int nbSeg = norme/(1.5*length), i;

	glDisable(GL_LIGHTING);
	glEnable(GL_DEPTH_TEST);
	tx /= norme; ty/= norme; tz/=norme;
	glPushMatrix();
		glColor4f(color[0], color[1], color[2], 1.);
		glBegin(GL_LINES);
		for (i = 0; i < nbSeg; i++) {
			glVertex3f(orig[0]+i*1.5*length*tx,orig[1]+i*1.5*length*ty,orig[2]+i*1.5*length*tz);
			glVertex3f(orig[0]+(i*1.5+1.)*length*tx,orig[1]+(i*1.5+1.)*length*ty,orig[2]+(i*1.5+1.)*length*tz);
		}
		glEnd();
		glColor4f(1., 1., 1., 1.);
	glPopMatrix();	
	glEnable(GL_LIGHTING);
}

static void drawDashedCircle(float ray, GLfloat color[], float angle) {
	int nbSeg = 2*PI/(1.5*angle), i;

	glPushMatrix();
		glColor4f(color[0], color[1], color[2], 1.);
		glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE, color);
		glBegin(GL_LINES);
		for (i = 0; i <= nbSeg; i++) {
			glVertex3f(ray*cos(i*1.5*angle), ray*sin(i*1.5*angle), 0.);
			glVertex3f(ray*cos((i*1.5+1.)*angle), ray*sin((i*1.5+1.)*angle), 0.);
		}
		glEnd();
		glColor4f(1., 1., 1., 1.);
	glPopMatrix();	
}

static void creerPlanete(int nom, GLfloat rayon) {
	GLUquadricObj* sph1 = gluNewQuadric();

	glNewList(nom,GL_COMPILE);
		gluQuadricDrawStyle(sph1,GLU_FILL);
		gluQuadricNormals(sph1,GLU_SMOOTH);
		gluQuadricTexture(sph1,GL_TRUE);
		gluSphere(sph1,rayon,30,30);
	glEndList();
}

static void drawPlanete(int nom) {
	glPushMatrix();
 		glEnable(GL_TEXTURE_2D);
		glTexEnvi(GL_TEXTURE_ENV,  GL_TEXTURE_ENV_MODE, GL_MODULATE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		if(bpp[nom]==24)
			glTexImage2D(GL_TEXTURE_2D,0,3,
				im[nom].sizeX,im[nom].sizeY,
				0,GL_RGB,GL_UNSIGNED_BYTE,im[nom].data);
		else if(bpp[nom]==32)
			glTexImage2D(GL_TEXTURE_2D,0,4,
				im[nom].sizeX,im[nom].sizeY,
				0,GL_RGBA,GL_UNSIGNED_BYTE,im[nom].data);
		glCallList(nom);
		glDisable(GL_TEXTURE_2D);
	glPopMatrix();
}

static void drawMan(int sexe)
{
	GLUquadric* cylindre;

	glPushMatrix();

	//cylindre corps
	glPushMatrix();
	glTranslatef( 0.0, 0.0, 70.0 );
	glRotatef(0.0, 1, 0, 0);	
	cylindre = gluNewQuadric();
	gluCylinder(cylindre, 20.0, 20.0, 60.0, 30.0, 30.0);
	glPopMatrix();
	
	//cylindre bras gauche
	glPushMatrix();
	glTranslatef( 27.0, 0.0, 90.0 );
	gluCylinder(cylindre, 7.0, 7.0, 30.0, 30.0, 30.0);
	glPopMatrix();
	
	//cylindre bras droit
	glPushMatrix();
	glTranslatef( -27.0, 0.0, 90.0 );	
	gluCylinder(cylindre, 7.0, 7.0, 30.0, 30.0, 30.0);
	glPopMatrix();
	
	//tete
	glPushMatrix();
	glTranslatef(0.0 , 0.0 , 150.0);
	glutSolidSphere(20.00, 10.0, 10.0);
	glPopMatrix();
	
	//cylindre jambe droit
	glPushMatrix();
	glTranslatef( -10.0, 0.0, 0.0 );	
	gluCylinder(cylindre, 5.0, 5.0, 70.0, 30.0, 30.0);
	glPopMatrix();
	
	//cylindre jambe gauche
	glPushMatrix();
	glTranslatef( 10.0, 0.0, 0.0 );	
	gluCylinder(cylindre, 5.0, 5.0, 70.0, 30.0, 30.0);
	glPopMatrix();
	
	//jupe
	if (sexe == WOMAN) {
		glPushMatrix();
		glTranslatef( 0.0, 0.0, 50.0 );	
		gluCylinder(cylindre, 50.0, 20.0, 30.0, 30.0, 30.0);
		glPopMatrix();
	}
	gluDeleteQuadric(cylindre);

	glPopMatrix();
}

/* BMP RGB 24 bpp uniquement */
int ImageLoad(char *filename, Image *image) {
    FILE *file;
    unsigned long size;
    unsigned long i;
    unsigned short int planes;
    unsigned short int bpp;
    char temp;

    if ((file = fopen(filename, "rb"))==NULL)
    {
        printf("File Not Found : %s\n",filename);
        return 0;
    }
    
    fseek(file, 18, SEEK_CUR);

    if ((i = fread(&image->sizeX, 4, 1, file)) != 1) {
        printf("Error reading width from %s.\n", filename);
        return 0;
    }
    
    if ((i = fread(&image->sizeY, 4, 1, file)) != 1) {
        printf("Error reading height from %s.\n", filename);
        return 0;
    }
    
    if ((fread(&planes, 2, 1, file)) != 1) {
        printf("Error reading planes from %s.\n", filename);
        return 0;
    }
    if (planes != 1) {
        printf("Planes from %s is not 1: %u\n", filename, planes);
        return 0;
    }

    if ((i = fread(&bpp, 2, 1, file)) != 1) {
        printf("Error reading bpp from %s.\n", filename);
        return 0;
    }
    if ((bpp != 24) && (bpp!=32)) {
        printf("Bpp from %s is not 24 or 32: %u\n", filename, bpp);
        return 0;
    }
    
    size = image->sizeX * image->sizeY * (bpp/8);

    fseek(file, 24, SEEK_CUR);

    image->data = (char *) malloc(size);
    if (image->data == NULL) {
        printf("Error allocating memory for color-corrected image data");
        return 0;       
    }

    if ((i = fread(image->data, size, 1, file)) != 1) {
        printf("Error reading image data from %s.\n", filename);
        return 0;
    }

    for (i=0;i<size;i+=(bpp/8)) {
        temp = image->data[i];
        image->data[i] = image->data[i+2];
        image->data[i+2] = temp;
    }

    return bpp;
}

void LoadTexture(char* filename, int* ident)
 {
  Image im;
  int bpp;

  glEnable(GL_TEXTURE_2D);

  if(bpp=ImageLoad(filename, &im))
   {
     glTexEnvi(GL_TEXTURE_ENV,  GL_TEXTURE_ENV_MODE, GL_MODULATE);
     glGenTextures(1, (GLuint*)ident);
     glBindTexture(GL_TEXTURE_2D, *ident);
     glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
     glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
 	 //glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,  GL_REPEAT); 
	 //glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,  GL_REPEAT); 
     if(bpp==24)
        glTexImage2D(GL_TEXTURE_2D,0,3,
		    im.sizeX,im.sizeY,
		    0,GL_RGB,GL_UNSIGNED_BYTE,im.data);
      else if(bpp==32)
        glTexImage2D(GL_TEXTURE_2D,0,4,
	   	    im.sizeX,im.sizeY,
		    0,GL_RGBA,GL_UNSIGNED_BYTE,im.data);
   }

   glDisable(GL_TEXTURE_2D);

 }


/***************************************************************************
  Initialisation textures
***************************************************************************/
void init_texture(void)
{
  glEnable(GL_TEXTURE_2D);

  printf("Chargement des textures....\n");
  
  bpp[SOLEIL]=ImageLoad("Data/sun.bmp", &im[SOLEIL]);
  printf("\tSoleil\n");
  
  bpp[SOLEIL_MANIP]=ImageLoad("Data/sun.bmp", &im[SOLEIL_MANIP]);
  printf("\tSoleil manip\n");
  
  bpp[TERRE]=ImageLoad("Data/earth.bmp", &im[TERRE]);
  printf("\tTerre\n");

  bpp[LUNE]=ImageLoad("Data/moon.bmp", &im[LUNE]);
  printf("\tLune\n");

  bpp[ETOILES]=ImageLoad("Data/etoiles.bmp", &im[ETOILES]);
  printf("\tEtoiles\n");

}



