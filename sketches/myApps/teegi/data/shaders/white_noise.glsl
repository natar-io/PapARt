

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;

varying vec4 vertColor;
varying vec4 vertTexCoord;

uniform vec2 iResolution;
uniform float iGlobalTime;

// from http://thndl.com/more-noise.html
float rand(vec2 p) {
    p+=.2127+p.x*.3713*p.y;
    vec2 r=4.789*sin(489.123*(p+iGlobalTime));
    return fract(r.x*r.y);
}


void main(void) {
  vec2 uv = vertTexCoord.xy;

  // used to make "blocks", the higher the factor the bigger the pixels size
  vec2  scale = iResolution/10.;
  float fuzz = rand(floor(scale*uv.xy));

  // get what's underneath
  vec4 col = texture2D(texture, vertTexCoord.xy);
  // apply the (weaken) noise
  col = col-fuzz/2.;
  gl_FragColor = col;
}

