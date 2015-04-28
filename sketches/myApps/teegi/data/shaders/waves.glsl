
// from https://www.shadertoy.com/view/Xsl3RX


#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;

uniform float iGlobalTime;
uniform float rings;
uniform float velocity;


varying vec4 vertColor;
varying vec4 vertTexCoord;

uniform vec2 iResolution;


void main()
{
  vec2 position = gl_FragCoord.xy/iResolution.xy;
  float aspect = iResolution.x/iResolution.y;
  position.x *= aspect;
  float dist = distance(position, vec2(aspect*0.5, 0.5));
  float offset=iGlobalTime*velocity;
  float conv=rings*4.;
  float v=dist*conv-offset;
  float ringr=floor(v);
  float wave_color=abs(dist- (ringr+float(fract(v)>0.5)+offset)/conv);
  // replaced 2 by 4 to get less rings, changed colors
  if(mod(ringr,4.)==1.)
    //color=1.-color;
    wave_color=1.;
  else
     wave_color=0.;
  // fadeout as arc goes further
  wave_color=wave_color-(dist*dist)*2.;
  // clamp values, we don't want background to be darker
  if (wave_color < 0.)
    wave_color = 0.;

  // get what's underneath
  vec4 col = texture2D(texture, vertTexCoord.xy);
  // apply wave on it
  col.rgb = col.rgb+wave_color;
  col.a = wave_color;
  // NB: in case you don't want white colors of the background to change, use
  // col.rgb =vec3(1.,1.,1.);

  gl_FragColor = col;
}

