package com.oovaa8.catto.density_functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.storage.WorldData;

public record Continent() implements DensityFunction {
    private static final MapCodec<Continent> DATA_CODEC = MapCodec.unit(new Continent());

    public static final KeyDispatchDataCodec<Continent> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

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
        return random(seed);
    }

    private static float vRandom2Y(int seed) {
        return random(seed ^ 0x9e3779b9);
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

    public record TerrainType(TerrainId id, int avgHeight, int heightDifference) {}

    public static final TerrainType BEACH = new TerrainType(TerrainId.BEACH, 64, 16);

    public static final TerrainType OCEAN_PLAIN = new TerrainType(TerrainId.OCEAN_PLAIN, -64, 16);
    public static final TerrainType OCEAN_HILL = new TerrainType(TerrainId.OCEAN_HILL, -128, 126);
    public static final TerrainType DEEP_OCEAN_PLAIN = new TerrainType(TerrainId.DEEP_OCEAN_PLAIN, -128, 16);
    public static final TerrainType DEEP_OCEAN_HILL = new TerrainType(TerrainId.DEEP_OCEAN_HILL, -128, 128);
    public static final TerrainType ABYSSAL_PLAIN = new TerrainType(TerrainId.ABYSSAL_PLAIN, -300, 0);

    public static final TerrainType PLAINS_LOW = new TerrainType(TerrainId.PLAINS_LOW, 128, 16);
    public static final TerrainType HILLS_LOW = new TerrainType(TerrainId.HILLS_LOW, 192, 128);
    public static final TerrainType PLAINS_MID = new TerrainType(TerrainId.PLAINS_MID, 512, 16);
    public static final TerrainType HILLS_MID = new TerrainType(TerrainId.HILLS_MID, 512, 128);
    public static final TerrainType PLAINS_HIGH = new TerrainType(TerrainId.PLAINS_HIGH, 1024, 16);
    public static final TerrainType HILLS_HIGH = new TerrainType(TerrainId.HILLS_HIGH, 1024, 128);

    public static final TerrainType MOUNTAINS_LOW = new TerrainType(TerrainId.MOUNTAINS_LOW, 1512, 512);
    public static final TerrainType MOUNTAINS_HIGH = new TerrainType(TerrainId.MOUNTAINS_HIGH, 1900, 1024);

    // private static final float CONTINENT_SCALE = 0.05f;
    public static final int BLOCKS_PER_TILE = 256;

    public static TerrainType terrainPicker(float height) {
        int rand = (int) (fRandom(height) * 100.0f);
        if (height >= 0.0f) {
            if (height <= 0.3f) {
                return switch (rand % 3) {
                    case 0 -> OCEAN_PLAIN;
                    default -> BEACH;
                };
            }
            if (height <= 0.6f) {
                return switch (rand % 3) {
                    case 0 -> HILLS_LOW;
                    default -> PLAINS_LOW;
                };
            }
            if (height >= 0.9f) return MOUNTAINS_LOW;
            return switch (rand % 4) {
                case 0, 1 -> HILLS_LOW;
                default -> MOUNTAINS_LOW;
            };
        }
        if (height >= -3.0f) return OCEAN_PLAIN;
        if (height <= -15.0f) return ABYSSAL_PLAIN;
        return DEEP_OCEAN_PLAIN;
    }

    // ------------------------------------------------------------------
    // Voronoi / continent height
    // ------------------------------------------------------------------

    /** Returns {closestDist, closestCellX, closestCellY}. */
    public static float[] vronoiNoise(float px, float py) {
        float closestDist = Float.MAX_VALUE;
        float closestX = 0.0f;
        float closestY = 0.0f;
        float baseX = floor(px);
        float baseY = floor(py);
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                float sx = baseX + i;
                float sy = baseY + j;
                int seed = cellSeed(sx, sy);
                float cx = sx + vRandom2X(seed);
                float cy = sy + vRandom2Y(seed);
                float dist = length(cx - px, cy - py);
                if (dist < closestDist) {
                    closestDist = dist;
                    closestX = sx;
                    closestY = sy;
                }
            }
        }
        return new float[] { closestDist, closestX, closestY };
    }

    public static float heightFromCell(float cellX, float cellY) {
        float anchorNoise = (ContinentNoise.snoise(cellX * 0.0125f + 0.72f, cellY * 0.0125f + 0.15f) + 1.0f) / 1.0f;

        float anchorPosX = cellX * 0.05f + 0.3f + anchorNoise * 3.5f;
        float anchorPosY = cellY * 0.05f + 0.7f + anchorNoise * 3.5f;
        float anchorSqX = floor(anchorPosX);
        float anchorSqY = floor(anchorPosY);

        float anchorDist = Float.MAX_VALUE;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                float ax = anchorSqX + i;
                float ay = anchorSqY + j;
                int seed = cellSeed(ax, ay);
                float centerX = ax + vRandom2X(seed) + anchorNoise;
                float centerY = ay + vRandom2Y(seed) + anchorNoise;
                float dist = length(centerX - anchorPosX, centerY - anchorPosY) + anchorNoise * 0.25f;
                if (dist < anchorDist) {
                    anchorDist = dist;
                }
            }
        }
        return (ContinentNoise.snoise(cellX, cellY) + 4.0f) / 4.0f - (anchorDist * 1.4f) * (anchorDist * 3.5f);
    }

    public static TerrainType mixT(TerrainType t1, TerrainType t2, float p) {
        return new TerrainType(
                p < 0.5f ? t1.id() : t2.id(),
                (int) mix((float) t1.avgHeight(), (float) t2.avgHeight(), p),
                (int) mix((float) t1.heightDifference(), (float) t2.heightDifference(), p));
    }

    public static TerrainType blendedTerrain(float px, float py) {
        float baseX = floor(px);
        float baseY = floor(py);
        TerrainType output = BEACH;
        float weightSum = 0.0f;
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                float cellX = baseX + i;
                float cellY = baseY + j;
                int seed = cellSeed(cellX, cellY);
                float centerX = cellX + vRandom2X(seed);
                float centerY = cellY + vRandom2Y(seed);
                float dist = length(centerX - px, centerY - py);
                TerrainType terrain = terrainPicker(heightFromCell(cellX, cellY));
                float weight = 1.0f - smoothstep(0.0f, 1.5f, dist);
                if (weight > 0.0f) {
                    if (weightSum == 0.0f) {
                        output = terrain;
                    } else {
                        output = mixT(output, terrain, weight / (weightSum + weight));
                    }
                    weightSum += weight;
                }
            }
        }
        return output;
    }

    public static TerrainType sharpTerrain(float px, float py) {
        float[] v = vronoiNoise(px, py);
        return terrainPicker(heightFromCell(v[1], v[2]));
    }

    public static float worldTo01(int worldHeight) {
        return 2.0f * ((float)(worldHeight) + 320.0f) / 2352.0f - 1.0f;
    }

    public static float getContinentHeight(int x, int z) {
        float px = (float) x / (float) BLOCKS_PER_TILE;
        float pz = (float) z / (float) BLOCKS_PER_TILE;
        TerrainType terrain = blendedTerrain(px, pz);
        return worldTo01(terrain.avgHeight());
    }

    @Override
    public double compute(FunctionContext context) {
        return getContinentHeight(context.blockX(), context.blockZ());
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new Continent();
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
