//
// Description : Array and textureless GLSL 2D simplex noise function.
//      Author : Ian McEwan, Ashima Arts.
//  Maintainer : stegu
//     Lastmod : 20110822 (ijm)
//     License : Copyright (C) 2011 Ashima Arts. All rights reserved.
//               Distributed under the MIT License. See LICENSE file.
//               https://github.com/ashima/webgl-noise
//               https://github.com/stegu/webgl-noise
// 

// Translated to OpenCL cause this project was originally a shadertoy

static float3 __attribute__((pure)) mod289_3(float3 x) {
  return x - floor(x * (1.0f / 289.0f)) * 289.0f;
}

static float2 __attribute__((pure)) mod289_2(float2 x) {
  return x - floor(x * (1.0f / 289.0f)) * 289.0f;
}

static float3 __attribute__((pure)) permute(float3 x) {
  return mod289_3(((x*34.0f)+10.0f)*x);
}

static float __attribute__((pure)) snoise(float2 v)
  {
  const float4 C = (float4)(0.211324865405187f,  // (3.0-sqrt(3.0))/6.0
                      0.366025403784439f,  // 0.5*(sqrt(3.0)-1.0)
                     -0.577350269189626f,  // -1.0 + 2.0 * C.x
                      0.024390243902439f); // 1.0 / 41.0
// First corner
  float2 i  = floor(v + dot(v, C.yy) );
  float2 x0 = v -   i + dot(i, C.xx);

// Other corners
  float2 i1;
  //i1.x = step( x0.y, x0.x ); // x0.x > x0.y ? 1.0 : 0.0
  //i1.y = 1.0 - i1.x;
  i1 = (x0.x > x0.y) ? (float2)(1.0f, 0.0f) : (float2)(0.0f, 1.0f);
  // x0 = x0 - 0.0 + 0.0 * C.xx ;
  // x1 = x0 - i1 + 1.0 * C.xx ;
  // x2 = x0 - 1.0 + 2.0 * C.xx ;
  float4 x12 = x0.xyxy + C.xxzz;
  x12.xy -= i1;

// Permutations
  i = mod289_2(i); // Avoid truncation effects in permutation
  float3 p = permute( permute( i.y + (float3)(0.0f, i1.y, 1.0f ))
		+ i.x + (float3)(0.0f, i1.x, 1.0f ));

  float3 m = max(0.5f - (float3)(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0f);
  m = m*m ;
  m = m*m ;

// Gradients: 41 points uniformly over a line, mapped onto a diamond.
// The ring size 17*17 = 289 is close to a multiple of 41 (41*7 = 287)

  float3 scaled = p * C.www;
  float3 x = 2.0f * (scaled - floor(scaled)) - 1.0f;
  float3 h = fabs(x) - 0.5f;
  float3 ox = floor(x + 0.5f);
  float3 a0 = x - ox;

// Normalise gradients implicitly by scaling m
// Approximation of: m *= inversesqrt( a0*a0 + h*h );
  m *= 1.79284291400159f - 0.85373472095314f * ( a0*a0 + h*h );

// Compute final noise value at P
  float3 g;
  g.x  = a0.x  * x0.x  + h.x  * x0.y;
  g.yz = a0.yz * x12.xz + h.yz * x12.yw;
  return 130.0f * dot(m, g);
}
static float __attribute__((pure)) snoise01(float2 pos){
    return (snoise(pos) + 1.0f)/2.0f;
}