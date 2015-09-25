import processing.sound.*;

class Sine{

  int nbSine = 3;

  //  SinOsc sine;
  SinOsc[] sine;

  TouchPoint tp;
  
  Sine(TouchPoint tp){
    this.tp = tp;
    sine = new SinOsc[nbSine];
    for(int i=0; i < nbSine; i++){
	SinOsc sine1 = new SinOsc(applet);
	sine1.freq(440);
	sine[i] = sine1;
    }

    //    sine = new SinOsc(440, 0.5, out.sampleRate());
    //    sine.portamento(200);
    update();

    for(int i=0; i < nbSine; i++){
	sine[i].play();
    }
  }
  
  void update(){
      if(tp.isToDelete()){
      for(int i=0; i < nbSine ; i++)
	  sine[i].stop();
      sineToDelete.add(this);
    }

    PVector pos = tp.getPosition();
    
    float amp = (1 - pos.z);
   if(pos.y > 0.5)
	amp = amp / 2f;
    if(pos.y > 0.8)
	amp = amp / 3f;
    float freq = map(pos.x, 0, 1, 40, 800);
    
    if(pos.x < 0 || pos.y < 0 ||
       pos.x > 1 || pos.y > 1)
	amp = 0;
    
    sine[0].amp(amp);
    sine[0].freq(freq);
    

    // if(pos.y < 0.5){
    //   sine[1].setAmp(amp);
    //   sine[1].setFreq(freq / 2f);
    // } else{
    //   sine[1].setAmp(0);
    // }

    // if(pos.y < 0.8){
    //   sine[2].setAmp(amp);
    //   sine[2].setFreq( 2 * freq);
    // } else{
    //   sine[2].setAmp(0);
    // }


    /* if(pos.y < 0.6){ */
    /*   sine[3].setAmp(1 - pos.z); */
    /*   sine[3].setFreq( freq / 4f); */
    /* } else{ */
    /*   sine[3].setAmp(0); */
    /* } */

    /* if(pos.y < 0.8){ */
    /*   sine[4].setAmp(1 - pos.z); */
    /*   sine[4].setFreq(freq / 5f); */
    /* } else{ */
    /*   sine[4].setAmp(0); */
    /* } */
    //    sine.setPan(map(pos.y, 0, 1, 1500, 60));
    //    sine.setFreq( map(pos.x, 0, 1, 200, 12000));


 }



}
