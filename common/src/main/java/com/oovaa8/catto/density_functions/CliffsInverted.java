package com.oovaa8.catto.density_functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record CliffsInverted(

        DensityFunction height,
        DensityFunction cliffs,
        DensityFunction raw_height,
        DensityFunction length,
        DensityFunction flatness,
        DensityFunction steepness,
        DensityFunction noise,
        float ocean,
        int worldHeight,
        int min_y

) implements DensityFunction {
    private static final MapCodec<CliffsInverted> DATA_CODEC = RecordCodecBuilder.mapCodec(
            kind -> kind.group( //16 argument limit TwT
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("height").forGetter(CliffsInverted::height),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("raw_height").forGetter(CliffsInverted::raw_height),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("cliffs").forGetter(CliffsInverted::cliffs),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("length").forGetter(CliffsInverted::length),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("flatness").forGetter(CliffsInverted::length),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("steepness").forGetter(CliffsInverted::length),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("noise").forGetter(CliffsInverted::noise),
                            Codec.FLOAT.fieldOf("sea_level").forGetter(CliffsInverted::ocean),
                            Codec.INT.fieldOf("world_height").forGetter(CliffsInverted::worldHeight),
                            Codec.INT.fieldOf("min_y").forGetter(CliffsInverted::worldHeight)
                    )
                    .apply(kind, CliffsInverted::new)
    );

    public static final KeyDispatchDataCodec<CliffsInverted> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    private float remap(float value){
        return 2*(value - (float)this.min_y) / ((float)this.worldHeight) - 1;
    }

    @Override
    public double compute(FunctionContext context) {
        float height_map = (float)this.height.compute(context);
        float h = height_map - this.remap(context.blockY());
        float s = (float)this.steepness.compute(context); // -1 to 1
        if(s >= 0) return h;
        float f = (float)this.flatness.compute(context); // 0 to 1
        float c = (float)this.cliffs.compute(context);
        float t = 0.15f * -s * Math.min(f * 20, 1);
        if(c>= 0 && c < t){
            float d = (float)this.length.compute(context) / (float)this.worldHeight;
            float hro = (float)raw_height.compute(context) + d/2 - (float)this.ocean;
            int i = (int) Math.floor(hro/d);
            float w = ((float)this.noise.compute(context) + 1) * 0.25f + 0.5f;
            float gap = (float)Steepness.heightNoise(i,w);
            float l = 2*(h / (d*f*gap))-1 - 0.05f;
            if(l<-1 || l>1) return h;
            float cn=c/t;
            return (cn*cn+l*l-0.9) >= 0 ? h : -h;
        }
        return h;
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new CliffsInverted(this.height.mapAll(visitor), this.raw_height.mapAll(visitor), this.cliffs.mapAll(visitor), this.length.mapAll(visitor), this.flatness.mapAll(visitor), this.steepness.mapAll(visitor), this.noise.mapAll(visitor), this.ocean, this.worldHeight, this.min_y);
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
        return CliffsInverted.CODEC;
    }
}
