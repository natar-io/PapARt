
import processing.sound.*;

SoundFile[] cris;
SoundFile[] grognement;

void initSound()
{

  String folder = "sons/";
  String crisS = "cri";
  String gro = "grognement";
  String extension = ".wav";
  int nbCris = 10;
  int nbGrognement = 2;


  // load BD.wav from the data folder, with a 512 sample buffer
  int bsize = 2048;

  cris = new SoundFile[nbCris];
  for(int i= 1; i <= nbCris; i++){
      cris[i-1] = new SoundFile(this, folder + crisS + i + extension);
  }

  grognement = new SoundFile[nbGrognement];
  for(int i= 1; i <= nbGrognement; i++){
      grognement[i-1] = new SoundFile(this, folder + gro + i + extension);
  }


  /* kick = new SoundFile("BD.wav", bsize); */
  /* // load SD.wav from the data folder */
  /* snare = new SoundFile("SD.wav", bsize); */
  /* // load CHH.wav from the data folder */
  /* hat = new SoundFile("CHH.wav", bsize); */

}


void  cri(){
  int sample = (int) random(cris.length);
  cris[sample].play();

  if(debug){
    println("cri "+ sample);
  }
}


void  grognement(){

  int sample = (int) random(grognement.length);
  
  println("Playing grognement : " + sample);
  grognement[sample].play();

  if(debug){
    println("cri "+ sample);
  }
}



void stopSound()
{
  // close the SoundFiles before we exit
  for(int i= 1; i <= cris.length; i++){
    cris[i-1].stop();
  }

  for(int i= 1; i <= grognement.length; i++){
    grognement[i-1].stop();
  }
}
