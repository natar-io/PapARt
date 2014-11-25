// Used for shadow lookup
varying vec4 coord, leftCoord, rightCoord;

varying vec4 diffuse,ambient;
varying vec3 normal,lightDir,halfVector;

varying vec4 leftShift, rightShift, pos1, diff;

void main()
{
  normal = normalize(gl_NormalMatrix * gl_Normal);
  lightDir = normalize(vec3(gl_LightSource[0].position));
  halfVector = normalize(gl_LightSource[0].halfVector.xyz);
  
  diffuse = gl_FrontMaterial.diffuse * gl_LightSource[0].diffuse;
  ambient = gl_FrontMaterial.ambient * gl_LightSource[0].ambient;
  ambient += gl_LightModel.ambient * gl_FrontMaterial.ambient;
  
  // Texture handling
  gl_TexCoord[0] = gl_MultiTexCoord0;
  
  leftCoord = gl_TextureMatrix[5] * gl_Vertex ;
  //  rightCoord = gl_TextureMatrix[6] * gl_Vertex ;
  coord = gl_Vertex * gl_ModelViewProjectionMatrix;


  leftShift = gl_TextureMatrix[4] * gl_Vertex;
  leftShift.x /= (297. * 6.);
  leftShift.y /= (210. * 6.);
  leftShift.z = leftShift.z / leftShift.w;
 
  rightShift = gl_TextureMatrix[6] * gl_Vertex ;
  rightShift.x /= (297. * 6.);
  rightShift.y /= (210. * 6.);
  rightShift.z = rightShift.z / rightShift.w;

  pos1 = coord;
  pos1.x /= (297. * 6.);
  pos1.y /= (210. * 6.);
  pos1.z = pos1.z / pos1.w;
  //  pos1 = pos1 / pos1.z;

  leftShift.x = leftShift.x - pos1.x;
  leftShift.y = leftShift.y - pos1.y;


  diff = leftShift - rightShift;
  /* diff = diff / diff.b; */

  gl_Position = ftransform();
}
