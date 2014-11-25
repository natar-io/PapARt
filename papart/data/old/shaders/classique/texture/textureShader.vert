// Used for shadow lookup
varying vec4 ShadowCoord;

varying vec4 diffuse,ambient;
varying vec3 normal,lightDir;

void main()
{
	normal = normalize(gl_NormalMatrix * gl_Normal);
	
	lightDir = normalize(vec3(gl_LightSource[0].position));
	//	halfVector = normalize(gl_LightSource[0].halfVector.xyz);

	diffuse = gl_FrontMaterial.diffuse * gl_LightSource[0].diffuse;
	ambient = gl_FrontMaterial.ambient * gl_LightSource[0].ambient;
	ambient += gl_LightModel.ambient * gl_FrontMaterial.ambient;

	// Texture handling
	gl_TexCoord[0] = gl_MultiTexCoord0;

      	ShadowCoord= gl_TextureMatrix[7] * gl_Vertex ;

	gl_Position = ftransform();

	//	gl_FrontColor = gl_Color;
}
