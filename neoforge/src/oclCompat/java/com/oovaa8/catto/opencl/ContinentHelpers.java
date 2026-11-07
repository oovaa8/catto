package com.oovaa8.catto.opencl;

public class ContinentHelpers {
    public static String main = """
            typedef enum {
             T_BEACH,
             T_OCEAN_PLAIN, T_OCEAN_HILL, T_DEEP_OCEAN_PLAIN, T_DEEP_OCEAN_HILL, T_ABYSSAL_PLAIN,\s
             T_PLAINS_LOW, T_HILLS_LOW, T_PLAINS_MID, T_HILLS_MID, T_PLAINS_HIGH, T_HILLS_HIGH,\s
             T_MOUNTAINS_LOW, T_MOUNTAINS_HIGH
             } Terrain_ID;
            
            typedef struct {
                Terrain_ID terrain_id;
                int avg_height;
                int height_difference;
            } Terrain_Type;
            
            __constant static Terrain_Type BEACH = { T_BEACH, 64, 16};
            
            __constant static Terrain_Type OCEAN_PLAIN = { T_OCEAN_PLAIN, -64, 16};
            __constant static Terrain_Type OCEAN_HILL = { T_OCEAN_HILL, -128, 126};
            __constant static Terrain_Type DEEP_OCEAN_PLAIN = { T_DEEP_OCEAN_PLAIN, -128, 16};
            __constant static Terrain_Type DEEP_OCEAN_HILL = { T_DEEP_OCEAN_HILL, -128, 128};
            __constant static Terrain_Type ABYSSAL_PLAIN = { T_ABYSSAL_PLAIN, -300, 0};
            
            __constant static Terrain_Type PLAINS_LOW = { T_PLAINS_LOW, 128, 16};
            __constant static Terrain_Type HILLS_LOW = { T_HILLS_LOW, 192, 128 };
            __constant static Terrain_Type PLAINS_MID = { T_PLAINS_MID, 512, 16};
            __constant static Terrain_Type HILLS_MID = { T_HILLS_MID, 512, 128 };
            __constant static Terrain_Type PLAINS_HIGH = { T_PLAINS_HIGH, 1024, 16};
            __constant static Terrain_Type HILLS_HIGH = { T_HILLS_HIGH, 1024, 128 };
            
            __constant static Terrain_Type MOUNTAINS_LOW = { T_MOUNTAINS_LOW, 1512, 512 };
            __constant static Terrain_Type MOUNTAINS_HIGH = { T_MOUNTAINS_HIGH, 1900, 1024 };
            
            //__constant static float CONTINENT_SCALE = 0.05f;
            __constant static int BLOCKS_PER_TILE = 256;
            
            static Terrain_Type __attribute__((pure)) terrain_picker(float height){
                int rand = (int)(f_random(height) * 100.0f);
                if(height >= 0.0f){
                    if(height <= 0.3f){
                        switch (rand % 3) {
                            case 0:
                                return OCEAN_PLAIN;
                            default:
                                return BEACH;
                                break;
                        }
                    }
                    if(height <= 0.6f){
                        switch (rand % 3) {
                            case 0:
                                return HILLS_LOW;
                                break;
                            default:
                                return PLAINS_LOW;
                                break;
                        }
                    }
                    if(height >= 0.9f) return MOUNTAINS_LOW;
                    switch (rand % 4) {
                        case 0:
                            return HILLS_LOW;
                            break;
                        case 1:
                            return HILLS_LOW;
                            break;
                        default:
                            return MOUNTAINS_LOW;
                            break;
                    }
                }
                if(height>=-3.0f) return OCEAN_PLAIN;
                if(height<=-15.0f) return ABYSSAL_PLAIN;
                return DEEP_OCEAN_PLAIN;
            }
            
            static float3 __attribute__((pure)) vronoi_noise(float2 pos) {
                float closest_dist = MAXFLOAT;
                float2 closest_cell = (float2)(0.0);
                float2 base_square = floor(pos);
                for(int i = -1; i <= 1; i++){
                    for(int j = -1; j <= 1; j++){
                        float2 sq = base_square + (float2)(i, j);
                        float2 center = sq + v_random2(sq);
                        float dist = length(center - pos);
                        if (dist < closest_dist) {
                            closest_dist = dist;
                            closest_cell = sq;
                        }
                    }
                }
                return (float3)(closest_dist, closest_cell);
            }
            
            static float __attribute__((pure)) height_from_cell(float2 cell_pos){
                float anchor_noise = (((snoise(cell_pos * 0.0125f + (float2)(0.72f, 0.15f)) + 1.0f)/1.0f)); //While c2me has it's own simplex function it is slower as it needs vanilla parity
                float2 anchor_pos = cell_pos * 0.05f + (float2)(0.3f, 0.7f) + anchor_noise * 3.5f;
                float2 anchor_square = floor(anchor_pos);
                float anchor_dist = MAXFLOAT;
                for(int i = -1; i <= 1; i++){
                    for(int j = -1; j <= 1; j++){
                        float2 anchor = anchor_square + (float2)(i, j);
                        float2 center = anchor + v_random2(anchor) + anchor_noise;
                        float dist = length(center - anchor_pos) + anchor_noise * 0.25f;
                        if (dist < anchor_dist) {
                            anchor_dist = dist;
                        }
                    }
                }
                float h = ((snoise(cell_pos) + 4.0f)/4.0f - (anchor_dist*1.4f)*(anchor_dist*3.5f));
                return h;
            }
            
            
            static Terrain_Type __attribute__((pure)) mix_t(Terrain_Type t1, Terrain_Type t2, float p) {
                Terrain_Type result;
                result.terrain_id = p < 0.5f ? t1.terrain_id : t2.terrain_id;
                result.avg_height = (int)mix((float)t1.avg_height, (float)t2.avg_height, p);
                result.height_difference = (int)mix((float)t1.height_difference, (float)t2.height_difference, p);
                return result;
            }
            
            static Terrain_Type __attribute__((pure)) blended_terrain(float2 pos)
            {
                float2 base_square = floor(pos);
                Terrain_Type output_terrain = BEACH;
                float weight_sum = 0.0f;
                for(int i = -2; i <= 2; i++){
                    for(int j = -2; j <= 2; j++){
                        float2 cell = base_square + (float2)(i, j);
                        float2 center = cell + v_random2(cell);
                        float dist = length(center - pos);
                        Terrain_Type terrain = terrain_picker(height_from_cell(cell));
                        //if (terrain.terrain_id == TERRAIN_MOUNTAIN) dist = dist - 0.5;
                        float weight = 1.0 - smoothstep(0.0f, 1.5f, dist);
                        if(weight > 0.0f){
                            if(weight_sum == 0.0f){
                                output_terrain = terrain;
                            } else {
                                output_terrain = mix_t(output_terrain, terrain, (weight / (weight_sum + weight)));
                            }
                            weight_sum += weight;
                        }
                    }
                }
                return output_terrain;
            }
            
            static Terrain_Type __attribute__((pure)) sharp_terrain(float2 pos)
            {
                float2 base_square = floor(pos);
                float3 v = vronoi_noise(pos);
                Terrain_Type terrain = terrain_picker(height_from_cell(v.yz));
                return terrain;
            }
            
            static float __attribute__((pure)) world_to_01(int world_height){
                return 2.0f * (float)(world_height + 320) / 2352.0f - 1.0f;
            }
            
            static float __attribute__((pure)) get_continent_height(int x, int z, int seed){
                float2 pos = (float2)((float)x / (float)BLOCKS_PER_TILE, (float)z / (float)BLOCKS_PER_TILE);
                Terrain_Type terrain = blended_terrain(pos);
                return world_to_01(terrain.avg_height);
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
                return r2 - 1.0;
            }
            
            static float __attribute__((pure)) f_random(float f) {
                return random(as_uint(f));
            }
            
            static float2 __attribute__((pure)) v_random2(float2 cell) {
                uint x = as_uint(cell.x);
                uint y = as_uint(cell.y);
                uint seed = hash(x ^ hash(y));
                return (float2)(random(seed), random(seed ^ 0x9e3779b9U));
            }
            
            static float __attribute__((pure)) f_random2(float2 cell) {
                uint x = as_uint(cell.x);
                uint y = as_uint(cell.y);
                uint seed = hash(x ^ hash(y));
                return random(seed);
            }
            """;
}
