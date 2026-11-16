package com.oovaa8.catto.density_functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.storage.WorldData;

import java.util.Arrays;

public record Continent(DensityFunction noise) implements DensityFunction {
    private static final MapCodec<Continent> DATA_CODEC = RecordCodecBuilder.mapCodec(
            kind -> kind.group(
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("noise").forGetter(Continent::noise)
                    )
                    .apply(kind, Continent::new));

    public static final KeyDispatchDataCodec<Continent> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public DensityFunction noise() {
        return noise;
    }

    //Translated by claude from the opencl code, I'm not spending hours translating this

    // ------------------------------------------------------------------
    // Small OpenCL builtins
    // ------------------------------------------------------------------

    public static float floor(float x) {
        return (float) Math.floor(x);
    }

    public static float length(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    public static float mix(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static float smoothstep(float edge0, float edge1, float x) {
        float t = (x - edge0) / (edge1 - edge0);
        t = Math.clamp(t, 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    // ------------------------------------------------------------------
    // Hashing / random  (uint -> int, >> -> >>>, as_uint/as_float -> bit casts)
    // ------------------------------------------------------------------

    private static int hash(int x) {
        x ^= x >>> 16;
        x *= 0x7feb352d;
        x ^= x >>> 15;
        x *= 0x846ca68b;
        x ^= x >>> 16;
        return x;
    }

    /** Uniform float in [0, 1). */
    private static float random(int seed) {
        final int mantissaMask = 0x007FFFFF;
        final int one = 0x3F800000;

        int h = hash(seed);
        h &= mantissaMask;
        h |= one;

        return Float.intBitsToFloat(h) - 1.0f;
    }

    private static float fRandom(float f) {
        return random(Float.floatToRawIntBits(f));
    }

    /** Seed shared by v_random2 / f_random2. */
    private static int cellSeed(float cx, float cy) {
        int x = Float.floatToRawIntBits(cx);
        int y = Float.floatToRawIntBits(cy);
        return hash(x ^ hash(y));
    }

    private static float vRandom2X(int seed) {
        return (seed & 0xFFFF) * (1.0f / 65536.0f);
    }

    private static float vRandom2Y(int seed) {
        return (seed >>> 16) * (1.0f / 65536.0f);
    }

    public static float fRandom2(float cx, float cy) {
        return random(cellSeed(cx, cy));
    }

    // ------------------------------------------------------------------
    // Terrain types
    // ------------------------------------------------------------------

    public enum TerrainId {
        BEACH,
        OCEAN_PLAIN, OCEAN_HILL, DEEP_OCEAN_PLAIN, DEEP_OCEAN_HILL, ABYSSAL_PLAIN,
        PLAINS_LOW, HILLS_LOW, PLAINS_MID, HILLS_MID, PLAINS_HIGH, HILLS_HIGH,
        MOUNTAINS_LOW, MOUNTAINS_HIGH
    }

    public record TerrainType(TerrainId id, float avgHeight, float heightDifference) {}

    public static final TerrainType BEACH = new TerrainType(TerrainId.BEACH, 64, 16);

    public static final TerrainType OCEAN_PLAIN = new TerrainType(TerrainId.OCEAN_PLAIN, -64, 16);
    public static final TerrainType OCEAN_HILL = new TerrainType(TerrainId.OCEAN_HILL, -64, 46);
    public static final TerrainType DEEP_OCEAN_PLAIN = new TerrainType(TerrainId.DEEP_OCEAN_PLAIN, -128, 16);
    public static final TerrainType DEEP_OCEAN_HILL = new TerrainType(TerrainId.DEEP_OCEAN_HILL, -128, 64);
    public static final TerrainType ABYSSAL_PLAIN = new TerrainType(TerrainId.ABYSSAL_PLAIN, -300, 0);

    public static final TerrainType PLAINS_LOW = new TerrainType(TerrainId.PLAINS_LOW, 128, 16);
    public static final TerrainType HILLS_LOW = new TerrainType(TerrainId.HILLS_LOW, 128, 56);

    public static final TerrainType PLAINS_LOW_2 = new TerrainType(TerrainId.PLAINS_LOW, 312, 16);
    public static final TerrainType HILLS_LOW_2 = new TerrainType(TerrainId.HILLS_LOW, 312, 64);

    public static final TerrainType PLAINS_MID = new TerrainType(TerrainId.PLAINS_MID, 512, 16);
    public static final TerrainType HILLS_MID = new TerrainType(TerrainId.HILLS_MID, 512, 86);

    public static final TerrainType PLAINS_MID_2 = new TerrainType(TerrainId.PLAINS_MID, 800, 24);
    public static final TerrainType HILLS_MID_2 = new TerrainType(TerrainId.HILLS_MID, 800, 96);

    public static final TerrainType PLAINS_HIGH = new TerrainType(TerrainId.PLAINS_HIGH, 1024, 24);
    public static final TerrainType HILLS_HIGH = new TerrainType(TerrainId.HILLS_HIGH, 1024, 112);

    public static final TerrainType PLAINS_HIGH_2 = new TerrainType(TerrainId.PLAINS_HIGH, 1300, 16);
    public static final TerrainType HILLS_HIGH_2 = new TerrainType(TerrainId.HILLS_HIGH, 1300, 64);

    public static final TerrainType MOUNTAINS_LOW = new TerrainType(TerrainId.MOUNTAINS_LOW, 1512, 16);
    public static final TerrainType MOUNTAINS_MID = new TerrainType(TerrainId.MOUNTAINS_LOW, 1700, 16);
    public static final TerrainType MOUNTAINS_HIGH = new TerrainType(TerrainId.MOUNTAINS_HIGH, 1964, 16);

    private static final float CONTINENT_SCALE = 0.065f;
    public static final int BLOCKS_PER_TILE = 256;

    // x * TILE_INV == x / BLOCKS_PER_TILE exactly while BLOCKS_PER_TILE is a power of two.
    private static final float TILE_INV = 1.0f / BLOCKS_PER_TILE;

    // Blend radius in cells. The 5x5 scan in the blend functions is valid for radius <= 2.
    private static final float BLEND_RADIUS = 1.5f;
    private static final float BLEND_R2 = BLEND_RADIUS * BLEND_RADIUS;
    // Slack on the box-bound reject. Float rounding of (cell + jitter) grows with coordinate magnitude,
    // so the cheap bound is deliberately a bit loose; the exact distance test below it does the real work.
    private static final float BLEND_PRUNE_LIMIT = BLEND_R2 + 0.05f;
    private static final float VORONOI_SLACK = 0.01f;

    // Visit the home cell first so the box bound can prune the other eight.
    private static final int[] OX = {0, -1, 1, 0, 0, -1, 1, -1, 1};
    private static final int[] OY = {0, 0, 0, -1, 1, -1, -1, 1, 1};

    public static TerrainType terrainPicker(float height) {
        int rand = (int) (fRandom(height) * 100.0f);
        if (height >= 0.0f) {
            if (height <= 0.16f) {
                if (rand % 6 < 2) return OCEAN_PLAIN;
                if (rand % 6 == 2) return HILLS_LOW;
                return BEACH;
            }
            if (height <= 0.24f) {
                if (rand % 3 == 0) return PLAINS_LOW;
                return HILLS_LOW;
            }
            if (height <= 0.32f){
                if (rand % 5 < 3) return PLAINS_LOW_2;
                if (rand % 5 == 3) return HILLS_LOW;
                return HILLS_LOW_2;
            }
            if (height <= 0.4f){
                if (rand % 5 < 3) return PLAINS_MID;
                if (rand % 5 == 3) return HILLS_LOW_2;
                return HILLS_MID;
            }
            if (height <= 0.48f){
                if (rand % 5 < 3) return PLAINS_MID_2;
                return HILLS_MID_2;
            }
            if (height <= 0.56f){
                if (rand % 5 < 3) return PLAINS_HIGH;
                return HILLS_HIGH;
            }
            if (height <= 0.68f){
                if (rand % 5 < 3) return PLAINS_HIGH_2;
                return HILLS_HIGH_2;
            }
            if (height <= 0.8) return  MOUNTAINS_LOW;
            if (height <= 0.92) return  MOUNTAINS_MID;
            return  MOUNTAINS_HIGH;
        }
        if (height >= -3.0f) {
            if (rand % 3 == 0) return OCEAN_HILL;
            return OCEAN_PLAIN;
        };
        if (height <= -13.0f) return ABYSSAL_PLAIN;
        if (rand % 3 == 0) return DEEP_OCEAN_HILL;
        return DEEP_OCEAN_PLAIN;
    }

    // ------------------------------------------------------------------
    // Voronoi / continent height
    // ------------------------------------------------------------------

    /**
     * Nearest jittered point among the 3x3 cells around floor(p), as the winning cell's coordinates
     * packed into a long (x float bits in the high half, y in the low half). No allocation, no sqrt.
     */
    private static long nearestCell(float px, float py) {
        float baseX = floor(px);
        float baseY = floor(py);
        float best = Float.MAX_VALUE;
        float bestX = 0.0f;
        float bestY = 0.0f;
        for (int k = 0; k < 9; k++) {
            float sx = baseX + OX[k];
            float sy = baseY + OY[k];
            // lower bound on the distance to any point jittered inside this cell's unit box
            float bx = Math.max(Math.max(sx - px, px - (sx + 1.0f)), 0.0f);
            float by = Math.max(Math.max(sy - py, py - (sy + 1.0f)), 0.0f);
            if (bx * bx + by * by - VORONOI_SLACK >= best) continue;
            int seed = cellSeed(sx, sy);
            float dx = sx + vRandom2X(seed) - px;
            float dy = sy + vRandom2Y(seed) - py;
            float d2 = dx * dx + dy * dy;
            if (d2 < best) {
                best = d2;
                bestX = sx;
                bestY = sy;
            }
        }
        return ((long) Float.floatToRawIntBits(bestX) << 32) | (Float.floatToRawIntBits(bestY) & 0xFFFFFFFFL);
    }

    private static float packedX(long packed) {
        return Float.intBitsToFloat((int) (packed >> 32));
    }

    private static float packedY(long packed) {
        return Float.intBitsToFloat((int) packed);
    }

    /** Returns {closestDist, closestCellX, closestCellY}. */
    public static float[] vronoiNoise(float px, float py) {
        long cell = nearestCell(px, py);
        float sx = packedX(cell);
        float sy = packedY(cell);
        int seed = cellSeed(sx, sy);
        float dist = length(sx + vRandom2X(seed) - px, sy + vRandom2Y(seed) - py);
        return new float[] { dist, sx, sy };
    }

    public static float heightFromCell(float cellX, float cellY) {
        cellX *= CONTINENT_SCALE;
        cellY *= CONTINENT_SCALE;
        float anchorNoise = 0.5f * (ContinentNoise.snoise(cellX * 0.0125f + 0.72f, cellY * 0.0125f + 0.15f) + 1.0f);

        float anchorPosX = cellX * 0.05f + 0.3f + anchorNoise * 3.5f;
        float anchorPosY = cellY * 0.05f + 0.7f + anchorNoise * 3.5f;
        float anchorSqX = floor(anchorPosX);
        float anchorSqY = floor(anchorPosY);

        // Same 3x3 search as before but on squared distance, pruned by the box bound. The "+ anchorNoise * 0.25f"
        // term is constant across candidates and sqrt is monotonic, so min(length(d) + c) == length(dmin) + c
        // exactly, which means a single sqrt at the end.
        float bestD2 = Float.MAX_VALUE;
        float bestDx = 0.0f;
        float bestDy = 0.0f;
        for (int k = 0; k < 9; k++) {
            float ax = anchorSqX + OX[k];
            float ay = anchorSqY + OY[k];
            // feature points of this anchor cell lie in [a + anchorNoise, a + anchorNoise + 1)
            float loX = ax + anchorNoise;
            float loY = ay + anchorNoise;
            float bx = Math.max(Math.max(loX - anchorPosX, anchorPosX - (loX + 1.0f)), 0.0f);
            float by = Math.max(Math.max(loY - anchorPosY, anchorPosY - (loY + 1.0f)), 0.0f);
            if (bx * bx + by * by - VORONOI_SLACK >= bestD2) continue;
            int seed = cellSeed(ax, ay);
            float dx = ax + vRandom2X(seed) + anchorNoise - anchorPosX;
            float dy = ay + vRandom2Y(seed) + anchorNoise - anchorPosY;
            float d2 = dx * dx + dy * dy;
            if (d2 < bestD2) {
                bestD2 = d2;
                bestDx = dx;
                bestDy = dy;
            }
        }
        float anchorDist = length(bestDx, bestDy) + anchorNoise * 0.25f;
        return (ContinentNoise.snoise(cellX, cellY) + 4.0f) * 0.25f - (anchorDist * 1.4f) * (anchorDist * 3.5f);
    }

    public static TerrainType mixT(TerrainType t1, TerrainType t2, float p) {
        return new TerrainType(
                p < 0.5f ? t1.id() : t2.id(),
                mix(t1.avgHeight(), t2.avgHeight(), p),
                mix(t1.heightDifference(), t2.heightDifference(), p));
    }

    /**
     * Full blend (id + avgHeight + heightDifference), uncached. Same result as before, but:
     *  - cells are rejected by a box bound, then by exact distance, before any noise is evaluated
     *    (about 7 of 25 cells survive)
     *  - the running mix lives in locals instead of allocating a TerrainType per step
     * Visit order (i outer, j inner) is unchanged because the mix is order dependent.
     */
    public static TerrainType blendedTerrain(float px, float py) {
        CellCache cache = CELL_CACHE.get();
        float baseX = floor(px);
        float baseY = floor(py);
        float fx = px - baseX; // exact
        float fy = py - baseY;
        TerrainId id = BEACH.id();
        float avg = BEACH.avgHeight();
        float diff = BEACH.heightDifference();
        float weightSum = 0.0f;
        for (int i = -2; i <= 2; i++) {
            float dxl = Math.max(Math.max(i - fx, fx - (i + 1)), 0.0f);
            float dx2 = dxl * dxl;
            if (dx2 >= BLEND_PRUNE_LIMIT) continue;
            float cellX = baseX + i;
            for (int j = -2; j <= 2; j++) {
                float dyl = Math.max(Math.max(j - fy, fy - (j + 1)), 0.0f);
                if (dx2 + dyl * dyl >= BLEND_PRUNE_LIMIT) continue;
                float cellY = baseY + j;

                int slot = cache.slot((int)cellX, (int)cellY);
                int seed = cellSeed(cellX, cellY);
                float dx = cellX + cache.jx[slot] - px;
                float dy = cellY + cache.jy[slot] - py;
                float d2 = dx * dx + dy * dy;
                if (d2 >= BLEND_R2) continue; // weight would be exactly 0
                float w = 1.0f - smoothstep(0.0f, BLEND_RADIUS, (float) Math.sqrt(d2));
                if (w <= 0.0f) continue;
                TerrainType t = cache.terrain(slot, (int) cellX, (int) cellY);
                if (weightSum == 0.0f) {
                    id = t.id();
                    avg = t.avgHeight();
                    diff = t.heightDifference();

                } else {
                    float p = w / (weightSum + w);
                    id = p < 0.5f ? id : t.id();
                    avg = mix(avg, t.avgHeight(), p);
                    diff = mix(diff, t.heightDifference(), p);
                }
                weightSum += w;
            }
        }
        if (weightSum == 0.0f) return BEACH;
        return new TerrainType(id, avg, diff);
    }

    // ------------------------------------------------------------------
    // Per-thread cell cache
    // ------------------------------------------------------------------
    // One cell covers 256x256 blocks, so uncached, the jitter hash plus heightFromCell (two simplex
    // evaluations and a Voronoi search) for the same cell is recomputed for every block sampled near it.
    // Both are pure functions of the cell coordinates, so we memoize them per thread in a small
    // direct-mapped table. No locking, no invalidation.
    //
    // NOTE: this is only valid because nothing here depends on the world seed. If you ever add a seed to
    // the hash or the noise, include it in the key (or make the cache per-instance).

    private static final int CACHE_BITS = 10;
    private static final int CACHE_SIZE = 1 << CACHE_BITS;
    private static final int CACHE_MASK = CACHE_SIZE - 1;
    private static final TerrainType UNSET = new TerrainType(TerrainId.BEACH, -1, -1);

    private static final class CellCache {
        final long[] keys = new long[CACHE_SIZE];
        final float[] jx = new float[CACHE_SIZE];
        final float[] jy = new float[CACHE_SIZE];
        final TerrainType[] terrain = new TerrainType[CACHE_SIZE];

        CellCache() {
            Arrays.fill(keys, Long.MIN_VALUE);
        }

        /**
         * Slot for cell (cx, cy), loading the jitter on a miss. Slot index is 31*cx + cy, so any two cells
         * within +-32 cells of each other map to different slots: a 5x5 neighbourhood never self-collides.
         */
        int slot(int cx, int cy) {
            long key = ((long) cx << 32) | (cy & 0xFFFFFFFFL);
            int s = (cx * 31 + cy) & CACHE_MASK;
            if (keys[s] != key) {
                int seed = cellSeed((float) cx, (float) cy);
                jx[s] = vRandom2X(seed);
                jy[s] = vRandom2Y(seed);
                terrain[s] = UNSET; // height is computed lazily, only for cells that actually contribute
                keys[s] = key;
            }
            return s;
        }

        TerrainType terrain(int s, int cx, int cy) {
            TerrainType t = terrain[s];
            if (t == UNSET) {
                t = terrainPicker(heightFromCell((float) cx, (float) cy));
                terrain[s] = t;
            }
            return t;
        }
    }

    private static final ThreadLocal<CellCache> CELL_CACHE = ThreadLocal.withInitial(CellCache::new);

    public static TerrainType sharpTerrain(float px, float py) {
        long cell = nearestCell(px, py);
        return terrainPicker(heightFromCell(packedX(cell), packedY(cell)));
    }

    public static float worldTo01(float worldHeight) {
        return 2.0f * ((worldHeight) + 320.0f) / 2352.0f - 1.0f;
    }

    public static float getContinentHeight(int x, int z, float noise) {
        float px = (float) x * TILE_INV;
        float pz = (float) z * TILE_INV;
        TerrainType terrain = blendedTerrain(px, pz);
        //float noise = ContinentNoise.snoise(px, pz); use a density function
        float noise_scaled = noise * terrain.heightDifference;
        float height = terrain.avgHeight + noise_scaled;
        return worldTo01(height);
    }

    @Override
    public double compute(FunctionContext context) {
        return getContinentHeight(context.blockX(), context.blockZ(), (float) noise.compute(context));
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new Continent(noise.mapAll(visitor));
    }

    @Override
    public double minValue() {
        return -1;
    }
    @Override
    public double maxValue() {
        return 1;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return Continent.CODEC;
    }
}