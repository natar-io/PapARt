uniform sampler2D leftTex;
uniform sampler2D rightTex;

void main()
{
  // RECOPIE
  // gl_FragColor = vec4(0.4, 0.2, 0.8, 0.3);

  vec4 leftColor  = texture2D(leftTex, gl_TexCoord[0].st);
  vec4 rightColor = texture2D(rightTex, gl_TexCoord[0].st);

  gl_FragColor = normalize(leftColor + rightColor); 

}

