//vertex shader

void main()
{
  //  gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * gl_Vertex;
  //  gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

  //  gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
  gl_TexCoord[0] = gl_MultiTexCoord0;
  gl_Position = ftransform();

}
