uniform sampler2D sceneTex;

void main()
{
  
  gl_FragColor =  vec4(texture2D(sceneTex, gl_TexCoord[0].st).rgb, 0.0);
}


