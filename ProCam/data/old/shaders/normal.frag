//
// Fragment shader for cartoon-style shading
//
// Author: Philip Rideout
//
// Copyright (c) 2004 3Dlabs Inc. Ltd.
//
// See 3Dlabs-License.txt for license information
//

varying vec3 Normal;

void main (void)
{
  gl_FragColor = vec4(Normal / 2.0 + vec3(0.5, 0.5, 0.5), 1.0); 
}
