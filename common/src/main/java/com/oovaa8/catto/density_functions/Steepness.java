package com.oovaa8.catto.density_functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record Steepness(

        DensityFunction height,
        DensityFunction steepness,
        DensityFunction flatness,
        DensityFunction length,
        DensityFunction noise,
        int worldHeight,
        float ocean

) implements DensityFunction {
    private static final MapCodec<Steepness> DATA_CODEC = RecordCodecBuilder.mapCodec(
            kind -> kind.group( //16 argument limit TwT
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("height").forGetter(Steepness::height),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("steepness").forGetter(Steepness::steepness),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("flatness").forGetter(Steepness::flatness),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("length").forGetter(Steepness::length),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("noise").forGetter(Steepness::noise),
                            Codec.INT.fieldOf("world_height").forGetter(Steepness::worldHeight),
                            Codec.FLOAT.fieldOf("sea_level").forGetter(Steepness::ocean)
                    )
                    .apply(kind, Steepness::new)
    );

    public static final KeyDispatchDataCodec<Steepness> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(DensityFunction.FunctionContext context) {
        float h = (float)this.height.compute(context); // -1 to 1
        float f = (float)this.flatness.compute(context); // 0 to 1
        if(f <= 0) return h;
        float s = Math.max((float)this.steepness.compute(context), 0.00001f); // 0 to 1
        float w = ((float)(this.noise.compute(context) + 1) * 0.25f + 0.5f);
        float d = (float)this.length.compute(context) / this.worldHeight; // in blocks
        float r = d / (float) 2;

        float hro = h+r-(float)this.ocean;//55/2352+0.00001
        int i = (int) Math.floor(hro / d); //0

        float gap = d * (float)heightNoise(i,w); //110/2352 * 1
        float height = (float)accumulatedHeight(i,d,w); //0

        float sm = smooth(d, r, s, hro); //0.5
        return h + f * (height + sm * gap - hro); //h+1*(0 + 0 * 0.0264103924 - 55/2352 + 0.00001) = h - 55/2352 + 0.00001; 36.5
                                                  //h+1*(0 + 1 * 110/2352 - 55/2352 - 0.00001); h + 2 * 55/2352 * 0.5647... - 55/2352 - 0.000001; h + 55/2352(2*0.5647... - 1) - 0.00001; 67
    }


    static double accumulatedHeight(int index, double d, double w) {
        double h = 0;
        if(index > 0){
            for (int k = 0; k < index; k++) h += d* heightNoise(k,w);
        }else{
            for (int k = index; k < 0; k++) h -= d* heightNoise(k,w);
        }
        return h;
    }

    static double heightNoise(int j, double w) {
        return 1.0 + (hash(j) * 2.0 - 1.0) * w;
    }

    public static double hash(int n) {
        if(n==0) return 0.5; //ocean cliff should always be the same length as it can disappear otherwise
        n = (((n >>> 16) ^ n) * 0x45d9f3b);
        n = (((n >>> 16) ^ n) * 0x45d9f3b);
        n = ((n >>> 16) ^ n);
        return (double) ((n & 0x7fffffff) / 0x7fffffff); // Sets first bit to 0 than divides by 01111111111111111111111111111111 (max value)
    }

    float smooth(float d, float r, float s, float hro){
        float ds = d*s/2;
        return smoothstep(r - ds, r+ds, Steepness.mod(hro,d));
    }

    float smoothstep(float a,float b,float k){
        float t = Math.clamp((k-a)/(b-a),0,1f);
        return t*t*(3-2*t);
    }

    public static float mod(float a, float b){
        return ((a % b) + b) % b;
    }

    @Override
    public void fillArray(double[] array, DensityFunction.ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @Override
    public DensityFunction mapAll(DensityFunction.Visitor visitor) {
        return new Steepness(this.height.mapAll(visitor), this.steepness.mapAll(visitor), this.flatness.mapAll(visitor), this.length.mapAll(visitor), this.noise.mapAll(visitor), this.worldHeight, this.ocean);
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
        return Steepness.CODEC;
    }
}
