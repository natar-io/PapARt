uniform sampler2D sceneTex;
uniform sampler2D mask;


void main()
{

  vec4 maskColor = texture2D(mask, gl_TexCoord[0].st);
  vec4 texC = texture2D(sceneTex, gl_TexCoord[0].st);

  float a = 1. - maskColor.r;

  gl_FragColor =  vec4(texC.rgb * a, a);

}


