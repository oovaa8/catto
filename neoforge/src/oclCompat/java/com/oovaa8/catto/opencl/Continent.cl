// None of this should be in the final code
float f_random(float f) {return 0.0f;}

float2 v_random2(float2 cell) {return (float2)0.0;}

float f_random2(float2 cell) {return 0.0f;}

float snoise(float2 v) {return 0.0f;}

float snoise01(float2 pos){return 0.0f;}
////////////////////////////////////////////////////////
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
__constant static Terrain_Type OCEAN_HILL = { T_OCEAN_HILL, -128, 12};
__constant static Terrain_Type DEEP_OCEAN_PLAIN = { T_DEEP_OCEAN_PLAIN, -128, 16};
__constant static Terrain_Type DEEP_OCEAN_HILL = { T_DEEP_OCEAN_HILL, -128, 128};
__constant static Terrain_Type ABYSSAL_PLAIN = { T_ABYSSAL_PLAIN, -300, 0};

__constant static Terrain_Type PLAINS_LOW = { T_PLAINS_LOW, 128, 16};
__constant static Terrain_Type HILLS_LOW = { T_HILLS_LOW, 192, 128};
__constant static Terrain_Type PLAINS_MID = { T_PLAINS_MID, 512, 16};
__constant static Terrain_Type HILLS_MID = { T_HILLS_MID, 512, 128};
__constant static Terrain_Type PLAINS_HIGH = { T_PLAINS_HIGH, 1024, 16};
__constant static Terrain_Type HILLS_HIGH = { T_HILLS_HIGH, 1024, 128};

__constant static Terrain_Type MOUNTAINS_LOW = { T_MOUNTAINS_LOW, 1512, 156};
__constant static Terrain_Type MOUNTAINS_HIGH = { T_MOUNTAINS_HIGH, 1900, 156};

#define CONTINENT_SCALE (0.1f)
#define BLOCKS_PER_TILE 256
#define TILE_INV (1.0f / (float)BLOCKS_PER_TILE)

// Blend radius in cells. The 5x5 scan in the blend functions is valid for radius <= 2.
#define BLEND_RADIUS 1.5f
#define BLEND_R2 (BLEND_RADIUS * BLEND_RADIUS)

#define BLEND_PRUNE_LIMIT (BLEND_R2 + 0.05f)
#define VORONOI_SLACK (0.01f)

static Terrain_Type __attribute__((pure)) terrain_picker(float height){
    if(height >= 0.0f){
        // only hash when we actually need the random roll
        int r = (int)(f_random(height) * 100.0f);
        if(height <= 0.3f){
            if (r % 3 == 0) return OCEAN_PLAIN;
            return BEACH;
        }
        if(height <= 0.6f){
            if (r % 3 == 0) return HILLS_LOW;
            return PLAINS_LOW;
        }
        if(height >= 0.9f) return MOUNTAINS_LOW;
        if (r % 4 < 2) return HILLS_LOW;
        return MOUNTAINS_LOW;
    }
    if(height>=-3.0f) return OCEAN_PLAIN;
    if(height<=-15.0f) return ABYSSAL_PLAIN;
    return DEEP_OCEAN_PLAIN;
}

// Squared distance from p to the unit box [lo, lo+1)^2.
// Every jittered feature point of a cell lies inside its box, so this is a lower bound
// on the distance to that point and lets us skip the hash entirely for far cells.
static float __attribute__((pure)) box_d2(float2 lo, float2 p) {
    float2 d = fmax(fmax(lo - p, p - (lo + 1.0f)), 0.0f);
    return dot(d, d);
}

// Nearest jittered point (cell + v_random2(cell) + shift) among the 3x3 cells around floor(p).
// Returns (winning cell.xy, winning (point - p).xy). Callers apply length() only if they need it.
// Visits the home cell first so the box test can prune most of the other eight.
static float4 __attribute__((pure)) nearest3x3(float2 p, float2 shift) {
    const int ox[9] = {0, -1, 1, 0, 0, -1, 1, -1, 1};
    const int oy[9] = {0, 0, 0, -1, 1, -1, -1, 1, 1};
    const float2 base = floor(p);
    float best = MAXFLOAT;
    float4 res = (float4)(0.0f);
    #pragma unroll
    for (int k = 0; k < 9; k++) {
        float2 cell = base + (float2)((float)ox[k], (float)oy[k]);
        if (box_d2(cell + shift, p) - VORONOI_SLACK >= best) continue;
        float2 d = cell + v_random2(cell) + shift - p;
        float d2 = dot(d, d);
        if (d2 < best) {
            best = d2;
            res = (float4)(cell, d);
        }
    }
    return res;
}

static float3 __attribute__((pure)) vronoi_noise(float2 pos) {
    float4 r = nearest3x3(pos, (float2)(0.0f));
    return (float3)(length(r.zw), r.xy);
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

static Terrain_Type __attribute__((pure)) mix_t(Terrain_Type t1, Terrain_Type t2, float p) {
    Terrain_Type result;
    result.terrain_id = p < 0.5f ? t1.terrain_id : t2.terrain_id;
    result.avg_height = mix(t1.avg_height, t2.avg_height, p);
    result.height_difference = mix(t1.height_difference, t2.height_difference, p);
    return result;
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
static Terrain_Type __attribute__((pure)) sharp_terrain(float2 pos)
{
    // only the winning cell matters here, no sqrt needed
    float4 r = nearest3x3(pos, (float2)(0.0f));
    return terrain_picker(height_from_cell(r.xy));
}

static float __attribute__((pure)) world_to_01(float world_height){
    return 2.0f * (world_height + 320.0f) / 2352.0f - 1.0f;
}

static float __attribute__((pure)) get_continent_height(int x, int z, float noise, int seed){
    float2 pos = (float2)((float)x * TILE_INV, (float)z * TILE_INV);
    Terrain_Type terrain = blended_terrain(pos);
    return world_to_01(terrain.avg_height + terrain.height_difference * noise);
}