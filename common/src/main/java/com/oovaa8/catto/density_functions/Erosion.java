package com.oovaa8.catto.density_functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jetbrains.annotations.NotNull;

public class Erosion
        implements DensityFunction {

    public DensityFunction height;
    public DensityFunction strength;
    public DensityFunction scale;

    // The magnitude of the gullies as a weight value from 0 to 1.
    // A value of 0 can sharpen peaks and valleys but feature virtually no gullies.
    // A value of 1 produces full gullies but may leave peaks and valleys rounded.
    // Adjusting erosion gully weight while inversely adjusting erosion scale can be used to control the sharpness of peaks and valleys while leaving gully magnitudes largely untocuhed.
    //Double gullyWeight,

    public float detail;

    // Separate rounding control of ridges and creases.
    //  ridgeRounding: Rounding of ridges.
    //  creaseRounding: Rounding of creases.
    //  z: Multiplier applied to the initial height function.
    //     E.g. if the height function has noise of 5 times lower frequency
    //     than the largest gullies, a value of 0.2 can compensate for that.
    //  w: Multiplier applied to each subsequent gully octave after the first.
    //     Setting it to the same value as the erosion lacunarity will produce
    //     consistent rounding of all octaves.
    public DensityFunction ridgeRounding;
    public DensityFunction creaseRounding;
    public float rounding_z;
    public float rounding_w;


    // Control over how far away from ridges/creases the erosion takes effect.
    //  x: Onset used on the initial height function.
    //  y: Onset used on each gully octave.
    //  z: RidgeMap-specific onset used on the initial height function.
    //  w: RidgeMap-specific onset used on each gully octave.
    //Double onset_x,
    //Double onset_y,
    //Double onset_z,
    //Double onset_w,
    // can't use these as we can only have 16 args


    // Control over the assumed slope of the initial height function.
    // In practise, assuming a slope can work better than using the input slope,
    // since the final terrain can be shaped quite differently than the input.
    //  assumedSlope: An assumed slope value to override the actual slope.
    //  weight: The amount (from 0 to 1) to override the actual slope.
    public float assumedSlope;
    public float assumedSlope_weight;

    // Gullies are based on stripes within Voronoi-like cells in the Phacelle noise
    // function. The cell scale parameter controls the sizes of the cells relative
    // to the overall erosion scale, while keeping the stripe widths unaffected.
    //
    // Values close to 1 usually produce good results. Smaller values produce more
    // grainy gullies while larger values produce longer unbroken gullies, but too
    // large values produce chaotic curved gullies that are not aligned with the
    // slopes.
    //
    // Value changes can cause abrupt changes in output, especially far away from
    // the origin, so this parameter is not well suited for animation or for
    // modulation by other functions.
    //Double cellScale,

    // The degree of normalization applied in the Phacelle noise, between 0 and 1.
    // The erosion filter depends on a certain consistency in magnitude of the
    // Phacelle output. However, high values can create loopy results where ridges
    // and creases meet up at a point, which produces unnatural looking results.
    public float normalization;

    // Layer settings
    public int octaves;
    public float lacunarity;
    public DensityFunction gain;

    // Scaling settings
    public float slopeScale;

    public String mode;

    private static final MapCodec<Erosion> DATA_CODEC = RecordCodecBuilder.mapCodec(
            kind -> kind.group( //16 argument limit TwT
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("height").forGetter(Erosion::getHeight),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("strength").forGetter(Erosion::getStrength),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("scale").forGetter(Erosion::getScale),
                            Codec.FLOAT.fieldOf("detail").forGetter(Erosion::getDetail),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("ridge_rounding").forGetter(Erosion::getRidgeRounding),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("crease_rounding").forGetter(Erosion::getCreaseRounding),
                            Codec.FLOAT.fieldOf("rounding_z").forGetter(Erosion::getRounding_z),
                            Codec.FLOAT.fieldOf("rounding_w").forGetter(Erosion::getRounding_w),
                            Codec.FLOAT.fieldOf("assumed_slope").forGetter(Erosion::getAssumedSlope),
                            Codec.FLOAT.fieldOf("assumed_slope_weight").forGetter(Erosion::getAssumedSlope_weight),
                            Codec.FLOAT.fieldOf("normalization").forGetter(Erosion::getNormalization),
                            Codec.INT.fieldOf("octaves").forGetter(Erosion::getOctaves),
                            Codec.FLOAT.fieldOf("lacunarity").forGetter(Erosion::getLacunarity),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("gain").forGetter(Erosion::getGain),
                            Codec.FLOAT.fieldOf("slope_scale").forGetter(Erosion::getSlopeScale),
                            Codec.STRING.fieldOf("mode").forGetter(Erosion::getMode)
                    )
                    .apply(kind, Erosion::new)
    );

    public DensityFunction getHeight() {
        return height;
    }

    public DensityFunction getStrength() {
        return strength;
    }

    public DensityFunction getScale() {
        return scale;
    }

    public float getDetail() {
        return detail;
    }

    public DensityFunction getRidgeRounding() {
        return ridgeRounding;
    }

    public DensityFunction getCreaseRounding() {
        return creaseRounding;
    }

    public float getRounding_z() {
        return rounding_z;
    }

    public float getRounding_w() {
        return rounding_w;
    }

    public float getAssumedSlope() {
        return assumedSlope;
    }

    public float getAssumedSlope_weight() {
        return assumedSlope_weight;
    }

    public float getNormalization() {
        return normalization;
    }

    public int getOctaves() {
        return octaves;
    }

    public float getLacunarity() {
        return lacunarity;
    }

    public DensityFunction getGain() {
        return gain;
    }

    public float getSlopeScale() {
        return slopeScale;
    }

    public String getMode() {
        return mode;
    }

    public static final KeyDispatchDataCodec<Erosion> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    public Erosion(DensityFunction Height, DensityFunction Strength, DensityFunction Scale, float Detail, DensityFunction RidgeRounding, DensityFunction CreaseRounding, float RoundingZ, float RoundingW, float AssumedSlope, float AssumedSlopeWeight, float Normalization, int Octaves, float Lacunarity, DensityFunction Gain, float SlopeScale, String Mode) {
        height = Height;
        strength = Strength;
        scale = Scale;
        detail = Detail;
        ridgeRounding = RidgeRounding;
        creaseRounding = CreaseRounding;
        rounding_w = RoundingW;
        rounding_z = RoundingZ;
        assumedSlope = AssumedSlope;
        assumedSlope_weight = AssumedSlopeWeight;
        normalization = Normalization;
        octaves = Octaves;
        lacunarity = Lacunarity;
        gain = Gain;
        slopeScale = SlopeScale;
        mode = Mode;
    }

    public Erosion() {
    }

    int lastX = -9999;
    int lastZ = -9999;
    ErosionResult lastResult;

    @Override
    public double compute(FunctionContext context) {
        ErosionResult result;
        if(lastX == context.blockX() && lastZ == context.blockZ() && lastResult != null){
            result = lastResult;
        }else {
            result = eval(context);
            lastResult = result;
            lastX = context.blockX();
            lastZ = context.blockZ();
        }

        return switch (mode) {
            case "height" -> result.height;
            case "ridge" -> result.ridge;
            case "slope_x" -> result.slope_x;
            case "slope_z" -> result.slope_y;
            case "slope_length" -> length(result.slope_x, result.slope_y);
            default -> result.height;
        };
    }

    private long key(int x, int y) {
        return (((long)x) << 32) | (y & 0xffffffffL);
    }

    static int logCount = 0;

    ErosionResult eval(FunctionContext context){
        float initialHeight = (float)this.height.compute(context);

        float[] slope = derivative2(context, initialHeight);
        float slope_x_i = slope[0];
        float slope_x = slope_x_i * this.slopeScale;
        float slope_y_i = slope[1];
        float slope_y = slope_y_i * this.slopeScale;

        float fadeTarget = Math.clamp(initialHeight / 0.6f, -1.0f, 1.0f);

        //float baseHeight = Math.clamp(initialHeight * 0.5 + 0.5,0,1);

        float[] erosion = ErosionFilter.ErosionFilter(
                context.blockX(), context.blockZ(),
                initialHeight, slope_x, slope_y, fadeTarget,
                (float)scale.compute(context), (float)strength.compute(context), (float)ridgeRounding.compute(context), (float)creaseRounding.compute(context), (float)gain.compute(context),
                0.8f, 1.25f, 1.25f, 2.8f, 1.5f, 0.5f, assumedSlope, assumedSlope_weight, detail, lacunarity, rounding_z, rounding_w, octaves, normalization
        );

        return new ErosionResult(
                initialHeight + erosion[0] + erosion[3] * 0.75f,
                erosion[4],
                slope_x_i + erosion[1],
                slope_y_i + erosion[2]
        );
    }

    static float safe_normalize_x(float x, float y){
        float l = length(x, y);
        return Math.abs(l) > 1e-10 ? x / l : x;
    }

    static float safe_normalize_y(float x, float y){
        float l = length(x, y);
        return Math.abs(l) > 1e-10 ? y / l : y;
    }

    static float length(float x, float y){
        return (float)Math.sqrt(x*x + y*y);
    }

    static float dot(float x1, float y1, float x2, float y2){
        return (x1*x2) + (y1*y2);
    }

    static float dot_self(float x, float y){
        return (x*x) + (y*y);
    }

    static float fract(float d){
        return d - (float)Math.floor(d);
    }

    static float hash_x(int p_x, int p_y){
        float k_x = 0.3183099f;
        float k_y = 0.3678794f;
        float x_x = p_x * k_x + k_y;
        float x_y = p_y * k_y + k_x;
        return -1.0f + 2.0f * fract(k_x * 16f * fract(x_x * x_y * (x_x + x_y)));
    }

    static float hash_y(int p_x, int p_y){
        float k_x = 0.3183099f;
        float k_y = 0.3678794f;
        float x_x = p_x * k_x + k_y;
        float x_y = p_y * k_y + k_x;
        return -1.0f + 2.0f * fract(k_y * 16f * fract(x_x * x_y * (x_x + x_y)));
    }

    static float ease_out(float t){
        float v = 1.0f - Math.clamp(t,0f,1f);
        return 1.0f - v*v;
    }

    static float smooth_start(float t, float smoothing){
        if (t>=smoothing) return  t - 0.5f * smoothing;
        return  0.5f * t * t / smoothing;
    }

    static float pow_inv(float t, float power){
        return 1.0f - (float)Math.pow(1.0f-Math.clamp(t,0f,1f), power);
    }

    static float mix(float x, float y, float a){
        return x * (1.0f-a) + y*a;
    }

    public float[] derivative2(FunctionContext context, float height){
        int dx = 1;
        int x = context.blockX();
        int z = context.blockZ();

        float height_x = (float)this.height.compute(newContext(context, x + dx, z));
        float height_x_dx = (height_x - height) / (float) dx;

        float height_z = (float)this.height.compute(newContext(context, x, z + dx));
        float height_z_dx = (height_z - height) / (float) dx;

        return new float[]{height_x_dx, height_z_dx};
    }

    public FunctionContext newContext(FunctionContext oldContext, int x, int z){
        return new FunctionContext() {
            @Override
            public int blockX() {
                return x;
            }

            @Override
            public int blockY() {
                return oldContext.blockY();
            }

            @Override
            public int blockZ() {
                return z;
            }

            @Override
            public @NotNull Blender getBlender() {
                return oldContext.getBlender();
            }
        };
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new Erosion(this.height.mapAll(visitor), this.strength.mapAll(visitor), this.scale.mapAll(visitor), this.detail, this.ridgeRounding.mapAll(visitor), this.creaseRounding.mapAll(visitor),
        this.rounding_z, this.rounding_w, this.assumedSlope, this.assumedSlope_weight, this.normalization, this.octaves, this.lacunarity, this.gain.mapAll(visitor), this.slopeScale, this.mode);
    }

    @Override
    public double minValue() {
        return -1.0;
    }
    @Override
    public double maxValue() {
        return 1.0;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    public static class ErosionResult {
        public float height;
        public float ridge;

        public float slope_x;
        public float slope_y;

        public ErosionResult(float h, float r, float sx, float sy){
            height = h;
            ridge = r;
            slope_x = sx;
            slope_y = sy;
        }
    }
}