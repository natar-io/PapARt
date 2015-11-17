

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;

varying vec4 vertColor;
varying vec4 vertTexCoord;
uniform vec2 iResolution;

void main() {

  vec2 uv= vertTexCoord.xy;
  // pixelize with a good ratio, the higher the factor, the bigger the pixels
  // FIXME: weird results if factor is not a "good" divider
  vec2 pix = iResolution/8.;
  uv=floor(uv*pix)/pix;
  gl_FragColor = texture2D(texture, uv);
}

