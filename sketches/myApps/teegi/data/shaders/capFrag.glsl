#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform sampler2D texRaw;
uniform sampler2D mask;

uniform bool isRaw;
uniform bool isWhiteMiddle;
uniform vec3 color1;
uniform vec3 color2;

varying vec4 vertColor;
varying vec4 vertTexCoord;




vec3 compose(vec3 value, vec3 color1, vec3 color2){
  vec3 compo1 = value * color1;
  vec3 compo2 = (vec3(1) - value) * color2;
  return compo1 + compo2;
}

vec3 composeWhite(vec3 value, vec3 color1, vec3 color3){

  vec3 color2 = vec3(1.0);
  value = value * 2;

  if(value.x <= 1){
    vec3 compo1 = value * color2;
    vec3 compo2 = (vec3(1) - value) * color1;
    return compo1 + compo2;
  } else {
    value = value - 1;
    vec3 compo2 = value * color3; 
    vec3 compo3 = (vec3(1) - value) * color2;
    return compo2 + compo3;
  }

}



void main() {

  vec3 tex = texture2D(texture, vertTexCoord.st);
  vec3 raw = texture2D(texture, vertTexCoord.st);
  vec4 mask = texture2D(mask, vertTexCoord.st);

  vec3 rawColor;
  vec3 secondColor;

  if(isRaw){
    rawColor = composeWhite(raw, color1, color2);
    secondColor = vec3(0);
  } else {

    float maskInv = 1 - mask;

    rawColor = raw * maskInv;

    if(isWhiteMiddle){
      secondColor = composeWhite(tex, color1, color2);
    } else {
      secondColor = compose(tex, color1, color2);
    }

    secondColor = secondColor * mask;
  }

  gl_FragColor =  vec4(rawColor + secondColor, 1.0);
}



