package com.oovaa8.catto.opencl;

public class ContinentHelpers {
    public static String main = """
            typedef enum {
                T_BEACH,
                T_OCEAN_PLAIN, T_OCEAN_HILL, T_DEEP_OCEAN_PLAIN, T_DEEP_OCEAN_HILL, T_ABYSSAL_PLAIN,
                T_PLAINS_LOW, T_HILLS_LOW, T_PLAINS_MID, T_HILLS_MID, T_PLAINS_HIGH, T_HILLS_HIGH,
                T_MOUNTAINS_LOW, T_MOUNTAINS_HIGH
                } Terrain_ID;
            
            typedef struct {
                Terrain_ID terrain_id;
                float avg_height;
                float height_difference;
            } Terrain_Type;
            
            __constant static Terrain_Type BEACH = { T_BEACH, 64, 16};
            
            __constant static Terrain_Type OCEAN_PLAIN = { T_OCEAN_PLAIN, -64, 16};
            __constant static Terrain_Type OCEAN_HILL = { T_OCEAN_HILL, -64, 46};
            __constant static Terrain_Type DEEP_OCEAN_PLAIN = { T_DEEP_OCEAN_PLAIN, -128, 16};
            __constant static Terrain_Type DEEP_OCEAN_HILL = { T_DEEP_OCEAN_HILL, -128, 64};
            __constant static Terrain_Type ABYSSAL_PLAIN = { T_ABYSSAL_PLAIN, -300, 0};
            
            __constant static Terrain_Type PLAINS_LOW = { T_PLAINS_LOW, 128, 16};
            __constant static Terrain_Type HILLS_LOW = { T_HILLS_LOW, 128, 56};
            
            __constant static Terrain_Type PLAINS_LOW_2 = { T_PLAINS_LOW, 312, 16};
            __constant static Terrain_Type HILLS_LOW_2 = { T_HILLS_LOW, 312, 64};
            
            __constant static Terrain_Type PLAINS_MID = { T_PLAINS_MID, 512, 16};
            __constant static Terrain_Type HILLS_MID = { T_HILLS_MID, 512, 86};
            
            __constant static Terrain_Type PLAINS_MID_2 = { T_PLAINS_MID, 800, 24};
            __constant static Terrain_Type HILLS_MID_2 = { T_HILLS_MID, 800, 96};
            
            __constant static Terrain_Type PLAINS_HIGH = { T_PLAINS_HIGH, 1024, 24};
            __constant static Terrain_Type HILLS_HIGH = { T_HILLS_HIGH, 1024, 112};
            
            __constant static Terrain_Type PLAINS_HIGH_2 = { T_PLAINS_HIGH, 1300, 16};
            __constant static Terrain_Type HILLS_HIGH_2 = { T_HILLS_HIGH, 1300, 64};
            
            __constant static Terrain_Type MOUNTAINS_LOW = { T_MOUNTAINS_LOW, 1512, 16};
            __constant static Terrain_Type MOUNTAINS_MID = { T_MOUNTAINS_LOW, 1700, 16};
            __constant static Terrain_Type MOUNTAINS_HIGH = { T_MOUNTAINS_HIGH, 1964, 16};
            
            #define CONTINENT_SCALE (0.065f)
            #define BLOCKS_PER_TILE 256
            #define TILE_INV (1.0f / (float)BLOCKS_PER_TILE)
            
            // Blend radius in cells. The 5x5 scan in the blend functions is valid for radius <= 2.
            #define BLEND_RADIUS 1.5f
            #define BLEND_R2 (BLEND_RADIUS * BLEND_RADIUS)
            
            #define BLEND_PRUNE_LIMIT (BLEND_R2 + 0.05f)
            #define VORONOI_SLACK (0.01f)
            
            static Terrain_Type __attribute__((pure)) terrain_picker(float height){
                int rand = (int)(f_random(height) * 100.0f);
                if(height >= 0.0f){
                    if (height <= 0.16f) {
                        if (rand % 6 < 2) return OCEAN_PLAIN;
                        if (rand % 6 == 2) return HILLS_LOW;
                        return BEACH;
                    }
                    if (height <= 0.24f) {
                        if (rand % 3 == 0) return PLAINS_LOW;
                        return HILLS_LOW;
                    }
                    if (height <= 0.32f) {
                        if (rand % 5 < 3) return PLAINS_LOW_2;
                        if (rand % 5 == 3) return HILLS_LOW;
                        return HILLS_LOW_2;
                    }
                    if (height <= 0.4f) {
                        if (rand % 5 < 3) return PLAINS_MID;
                        if (rand % 5 == 3) return HILLS_LOW_2;
                        return HILLS_MID;
                    }
                    if (height <= 0.48f) {
                        if (rand % 5 < 3) return PLAINS_MID_2;
                        return HILLS_MID_2;
                    }
                    if (height <= 0.56f) {
                        if (rand % 5 < 3) return PLAINS_HIGH;
                        return HILLS_HIGH;
                    }
                    if (height <= 0.68f) {
                        if (rand % 5 < 3) return PLAINS_HIGH_2;
                        return HILLS_HIGH_2;
                    }
                    if (height <= 0.8f) return MOUNTAINS_LOW;
                    if (height <= 0.92f) return MOUNTAINS_MID;
                    return MOUNTAINS_HIGH;
                }
                if(height >= -3.0f) {
                    if (rand % 3 == 0) return OCEAN_HILL;
                    return OCEAN_PLAIN;
                }
                if(height <= -13.0f) return ABYSSAL_PLAIN;
                if (rand % 3 == 0) return DEEP_OCEAN_HILL;
                return DEEP_OCEAN_PLAIN;
            }
            
            static float __attribute__((pure)) height_from_cell(float2 cell_pos){
                const int ox[9] = {0, -1, 1, 0, 0, -1, 1, -1, 1};
                const int oy[9] = {0, 0, 0, -1, 1, -1, -1, 1, 1};
                cell_pos *= CONTINENT_SCALE;
            
                float anchor_noise = 0.5 * (snoise(cell_pos * 0.0125f + (float2)(0.72f, 0.15f)) + 1.0f);
            
                float2 anchor_pos = cell_pos * 0.05f + (float2)(0.3f, 0.7f) + anchor_noise * 3.5f;
                float2 anchor_square = floor(anchor_pos);
            
                float bestD2 = MAXFLOAT;
                float2 best_dist = (float2)(0.0f);
                #pragma unroll
                for(int k = 0; k < 9; k++){
                    float2 anchor_cell = anchor_square + (float2)((float)ox[k], (float)oy[k]);
                    // feature points of this anchor cell lie in [a + anchorNoise, a + anchorNoise + 1)
                    float2 lo = anchor_cell + anchor_noise;
                    float2 b = max(max(lo - anchor_pos, anchor_pos - (lo + 1.0f)), 0.0f);
                    if ((dot(b, b) - VORONOI_SLACK) >= bestD2) continue;
                    float2 dist = anchor_cell + v_random2(anchor_cell) + anchor_noise - anchor_pos;
                    float d2 = dot(dist, dist);
                    if (d2 < bestD2) {
                        bestD2 = d2;
                        best_dist = dist;
                    }
                }
                float anchor_dist = length(best_dist) + anchor_noise * 0.25f;
                return (snoise(cell_pos) + 4.0f) * 0.25f - (anchor_dist * 1.4f) * (anchor_dist * 3.5f);
            
            }
            
            static Terrain_Type __attribute__((pure)) blended_terrain(float2 pos)
            {
                const float2 base = floor(pos);
                float2 f = pos - base;
                float weight_sum = 0.0f;
            
                Terrain_ID terrain = BEACH.terrain_id;
                float avg = BEACH.avg_height;
                float diff = BEACH.height_difference;
                float weightSum = 0.0f;
            
                // don't unroll: the body inlines two snoise calls and would blow up code size 25x
                #pragma unroll 1
                for(int i = -2; i <= 2; i++){
                    float dxl = fmax(fmax(i - f.x, f.x - (i + 1)), 0.0f);
                    float dx2 = dxl * dxl;
                    if (dx2 >= BLEND_PRUNE_LIMIT) continue;
                    float cellX = base.x + (float)i;
                    #pragma unroll 1
                    for(int j = -2; j <= 2; j++){
                        float dyl = fmax(fmax(j - f.y, f.y - (j + 1)), 0.0f);
                        if (dx2 + dyl * dyl >= BLEND_PRUNE_LIMIT) continue;
            
                        const float2 cell = (float2)(cellX, base.y + (float)j);
                        float2 dist = cell + v_random2(cell) - pos;
                        float dist_2 = dot(dist, dist);
                        if (dist_2 >= BLEND_R2) continue; // weight would be exactly 0
            
                        float w = 1.0f - smoothstep(0.0f, BLEND_RADIUS, (float) sqrt(dist_2));
                        if (w <= 0.0f) continue;
                        Terrain_Type t = terrain_picker(height_from_cell(cell));
            
                        if (weightSum == 0.0f) {
                            terrain = t.terrain_id;
                            avg = t.avg_height;
                            diff = t.height_difference;
                        } else {
                            float p = w / (weightSum + w);
                            terrain = p < 0.5f ? terrain : t.terrain_id;
                            avg = mix(avg, t.avg_height, p);
                            diff = mix(diff, t.height_difference, p);
                        }
                        weightSum += w;
                    }
                }
                if (weightSum == 0.0f) {
                    // This should never happen, but if it does, return a default terrain type
                    return BEACH;
                }
                return (Terrain_Type){terrain, avg, diff};
            }
            
            static float __attribute__((pure)) world_to_01(float world_height){
                return 2.0f * (world_height + 320.0f) / 2352.0f - 1.0f;
            }
            
            static float __attribute__((pure)) get_continent_height(int x, int z, float noise, int seed){
                float2 pos = (float2)((float)x * TILE_INV, (float)z * TILE_INV);
                Terrain_Type terrain = blended_terrain(pos);
                return world_to_01(terrain.avg_height + terrain.height_difference * noise);
            }
            """;

    public static String simplex = """
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
            """;

    public static String random_hash = """
            static uint __attribute__((pure)) hash(uint x)
            {
                x ^= x >> 16;
                x *= 0x7feb352dU;
                x ^= x >> 15;
                x *= 0x846ca68bU;
                x ^= x >> 16;
                return x;
            }
            static float __attribute__((pure)) random(uint seed) {
                const uint mantissaMask = 0x007FFFFFu;
                const uint one          = 0x3F800000u;

                uint h = hash(seed);
                h &= mantissaMask;
                h |= one;

                float r2 = as_float(h);
                return r2 - 1.0f; // was 1.0 (double literal): double math on GPUs with fp64
            }

            static float __attribute__((pure)) f_random(float f) {
                return random(as_uint(f));
            }

            // Build with -D CATTO_FAST_HASH to use 2 hashes instead of 4 per call (16 bits of jitter per axis).
            // This CHANGES the generated world (different random values), so it is off by default.
            static float2 __attribute__((pure)) v_random2(float2 cell) {
                uint x = as_uint(cell.x);
                uint y = as_uint(cell.y);
                uint seed = hash(x ^ hash(y));
                return (float2)((float)(seed & 0xFFFFu), (float)(seed >> 16)) * (1.0f / 65536.0f);
            }

            static float __attribute__((pure)) f_random2(float2 cell) {
                uint x = as_uint(cell.x);
                uint y = as_uint(cell.y);
                uint seed = hash(x ^ hash(y));
                return random(seed);
            }
            """;
}