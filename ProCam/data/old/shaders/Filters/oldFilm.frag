/// http://devmaster.net/posts/2989/shader-effects-old-film


/// <summary>
/// Uniform variables.
/// <summary>
uniform vec2 ImageSize;
uniform vec2 TexelSize;
uniform vec4 Colour;
uniform sampler2D Sample0;

uniform float SepiaValue;
uniform float NoiseValue;
uniform float ScratchValue;
uniform float InnerVignetting;
uniform float OuterVignetting;
uniform float RandomValue;
uniform float TimeLapse;


/// <summary>
/// Varying variables.
/// <summary>
varying vec2 vUv;


/// <summary>
/// Computes the overlay between the source and destination colours.
/// <summary>
vec3 Overlay (vec3 src, vec3 dst)
{
    // if (dst <= Ω) then: 2 * src * dst
    // if (dst > Ω) then: 1 - 2 * (1 - dst) * (1 - src)
    return vec3((dst.x <= 0.5) ? (2.0 * src.x * dst.x) : (1.0 - 2.0 * (1.0 - dst.x) * (1.0 - src.x)),
                (dst.y <= 0.5) ? (2.0 * src.y * dst.y) : (1.0 - 2.0 * (1.0 - dst.y) * (1.0 - src.y)),
                (dst.z <= 0.5) ? (2.0 * src.z * dst.z) : (1.0 - 2.0 * (1.0 - dst.z) * (1.0 - src.z)));
}


/// <summary>
/// 2D Noise by Ian McEwan, Ashima Arts.
/// <summary>
vec3 mod289(vec3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec2 mod289(vec2 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec3 permute(vec3 x) { return mod289(((x*34.0)+1.0)*x); }
float snoise (vec2 v)
{
    const vec4 C = vec4(0.211324865405187,  // (3.0-sqrt(3.0))/6.0
                        0.366025403784439,  // 0.5*(sqrt(3.0)-1.0)
                        -0.577350269189626, // -1.0 + 2.0 * C.x
                        0.024390243902439); // 1.0 / 41.0

    // First corner
    vec2 i  = floor(v + dot(v, C.yy) );
    vec2 x0 = v -   i + dot(i, C.xx);

    // Other corners
    vec2 i1;
    i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
    vec4 x12 = x0.xyxy + C.xxzz;
    x12.xy -= i1;

    // Permutations
    i = mod289(i); // Avoid truncation effects in permutation
    vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 ))
        + i.x + vec3(0.0, i1.x, 1.0 ));

    vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
    m = m*m ;
    m = m*m ;

    // Gradients: 41 points uniformly over a line, mapped onto a diamond.
    // The ring size 17*17 = 289 is close to a multiple of 41 (41*7 = 287)

    vec3 x = 2.0 * fract(p * C.www) - 1.0;
    vec3 h = abs(x) - 0.5;
    vec3 ox = floor(x + 0.5);
    vec3 a0 = x - ox;

    // Normalise gradients implicitly by scaling m
    // Approximation of: m *= inversesqrt( a0*a0 + h*h );
    m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );

    // Compute final noise value at P
    vec3 g;
    g.x  = a0.x  * x0.x  + h.x  * x0.y;
    g.yz = a0.yz * x12.xz + h.yz * x12.yw;
    return 130.0 * dot(m, g);
}


/// <summary>
/// Fragment shader entry.
/// <summary>
void main ()
{
    // Sepia RGB value
    vec3 sepia = vec3(112.0 / 255.0, 66.0 / 255.0, 20.0 / 255.0);

    // Step 1: Convert to grayscale
    vec3 colour = texture2D(Sample0, vUv).xyz;
    float gray = (colour.x + colour.y + colour.z) / 3.0;
    vec3 grayscale = vec3(gray);
    
    // Step 2: Appy sepia overlay
    vec3 finalColour = Overlay(sepia, grayscale);
    
    // Step 3: Lerp final sepia colour
    finalColour = grayscale + SepiaValue * (finalColour - grayscale);
    
    // Step 4: Add noise
    float noise = snoise(vUv * vec2(1024.0 + RandomValue * 512.0, 1024.0 + RandomValue * 512.0)) * 0.5;
    finalColour += noise * NoiseValue;
    
    // Optionally add noise as an overlay, simulating ISO on the camera
    //vec3 noiseOverlay = Overlay(finalColour, vec3(noise));
    //finalColour = finalColour + NoiseValue * (finalColour - noiseOverlay);
    
    // Step 5: Apply scratches
    if ( RandomValue < ScratchValue )
    {
        // Pick a random spot to show scratches
        float dist = 1.0 / ScratchValue;
        float d = distance(vUv, vec2(RandomValue * dist, RandomValue * dist));
        if ( d < 0.4 )
        {
            // Generate the scratch
            float xPeriod = 8.0;
            float yPeriod = 1.0;
            float pi = 3.141592;
            float phase = TimeLapse;
            float turbulence = snoise(vUv * 2.5);
            float vScratch = 0.5 + (sin(((vUv.x * xPeriod + vUv.y * yPeriod + turbulence)) * pi + phase) * 0.5);
            vScratch = clamp((vScratch * 10000.0) + 0.35, 0.0, 1.0);

            finalColour.xyz *= vScratch;
        }
    }
    
    // Step 6: Apply vignetting
    // Max distance from centre to corner is ~0.7. Scale that to 1.0.
    /* float d = distance(vec2(0.5, 0.5), vUv) * 1.414213; */
    /* float vignetting = clamp((OuterVignetting - d) / (OuterVignetting - InnerVignetting), 0.0, 1.0); */
    /* finalColour.xyz *= vignetting; */
    
    // Apply colour
    gl_FragColor.xyz = finalColour;
    gl_FragColor.w = 1.0;
}
