#version 330 compatibility

uniform sampler2D sceneTex;
uniform vec2 offset;

uniform mat3 G[2] = mat3[]
(
	mat3( 1.0, 2.0, 1.0, 0.0, 0.0, 0.0, -1.0, -2.0, -1.0 ),
	mat3( 1.0, 0.0, -1.0, 2.0, 0.0, -2.0, 1.0, 0.0, -1.0 )
);

void main() 
{ 
    vec2 uv = gl_TexCoord[0].xy;
    vec3 tc = vec3(1.0, 0.0, 0.0);
  
    mat3 I;
    float cnv[2];
    vec3 sample;
    
    // fetch the 3x3 neighbourhood and use the RGB vector's length as intensity value
    for (int i=0; i<3; i++)
    {
      for (int j=0; j<3; j++) 
      {
        sample = texelFetch(sceneTex, ivec2(uv / offset) + ivec2(i-1,j-1), 0).rgb;
        I[i][j] = length(sample); 
      }
    }
    
    // calculate the convolution values for all the masks
    for (int i=0; i<2; i++) 
    {
      float dp3 = dot(G[i][0], I[0]) + dot(G[i][1], I[1]) + dot(G[i][2], I[2]);
      cnv[i] = dp3 * dp3; 
    }

    tc = vec3(0.5 * sqrt(cnv[0]*cnv[0]+cnv[1]*cnv[1]));
    
    gl_FragColor = vec4(tc, 1.0);
}    
