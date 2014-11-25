uniform sampler2D sceneTex;
uniform sampler2D mask;

uniform vec2 wh;
/* uniform int derivSize; */
/* uniform float intensT; */

vec4 computeBlur(){

  vec2 uv = gl_TexCoord[0].xy;
  vec4 c = texture2D(sceneTex, uv);

  c += texture2D(sceneTex, uv+0.001);
  c += texture2D(sceneTex, uv+0.003);
  c += texture2D(sceneTex, uv+0.005);
  c += texture2D(sceneTex, uv+0.007);
  c += texture2D(sceneTex, uv+0.009);
  c += texture2D(sceneTex, uv+0.011);

  c += texture2D(sceneTex, uv-0.001);
  c += texture2D(sceneTex, uv-0.003);
  c += texture2D(sceneTex, uv-0.005);
  c += texture2D(sceneTex, uv-0.007);
  c += texture2D(sceneTex, uv-0.009);
  c += texture2D(sceneTex, uv-0.011);

  c.rgb = vec3((c.r+c.g+c.b)/3.0);
  //  c = c / 9.5;

  c = c / 11.5;

  return c;
}

vec4 gaussianBlur(){

  float dx = wh.s;
  float dy = wh.t;
  vec2 st = gl_TexCoord[0].st;
  
  // Apply 3x3 gaussian filter
  vec4 color	 = 4.0 * texture2D(sceneTex, st);
  color		+= 2.0 * texture2D(sceneTex, st + vec2(+dx, 0.0));
  color		+= 2.0 * texture2D(sceneTex, st + vec2(-dx, 0.0));
  color		+= 2.0 * texture2D(sceneTex, st + vec2(0.0, +dy));
  color		+= 2.0 * texture2D(sceneTex, st + vec2(0.0, -dy));
  color		+= texture2D(sceneTex, st + vec2(+dx, +dy));
  color		+= texture2D(sceneTex, st + vec2(-dx, +dy));
  color		+= texture2D(sceneTex, st + vec2(-dx, -dy));
  color		+= texture2D(sceneTex, st + vec2(+dx, -dy));
  
  return color/ 16.0;
}


void main()
{
  // RECOPIE
  // gl_FragColor = vec4(0.4, 0.2, 0.8, 0.3);

  vec4 maskColor = texture2D(mask, gl_TexCoord[0].st);
  vec4 texC = texture2D(sceneTex, gl_TexCoord[0].st);

  float dx = wh.s;
  float dy = wh.t;
  vec2 st = gl_TexCoord[0].st;
  
  // Apply 3x3 gaussian filter
  vec4 color	 = 4.0 * texture2D(sceneTex, st);
  color		+= 2.0 * texture2D(sceneTex, st + vec2(+dx, 0.0));
  color		+= 2.0 * texture2D(sceneTex, st + vec2(-dx, 0.0));
  color		+= 2.0 * texture2D(sceneTex, st + vec2(0.0, +dy));
  color		+= 2.0 * texture2D(sceneTex, st + vec2(0.0, -dy));
  color		+= texture2D(sceneTex, st + vec2(+dx, +dy));
  color		+= texture2D(sceneTex, st + vec2(-dx, +dy));
  color		+= texture2D(sceneTex, st + vec2(-dx, -dy));
  color		+= texture2D(sceneTex, st + vec2(+dx, -dy));

  color = color / 16.0;
  vec4 color2 = texture2D(sceneTex, gl_TexCoord[0].st);


  gl_FragColor =  (maskColor.r * color) + (1. - maskColor.r) * color2;

  //  gl_FragColor =  color;


  //  gl_FragColor =  vec4(maskColor.r, color, color, 0.5);

}


