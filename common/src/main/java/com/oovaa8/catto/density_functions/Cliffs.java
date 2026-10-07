package com.oovaa8.catto.density_functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record Cliffs(

        DensityFunction height,
        DensityFunction length,
        int worldHeight,
        float ocean

) implements DensityFunction {
    private static final MapCodec<Cliffs> DATA_CODEC = RecordCodecBuilder.mapCodec(
            kind -> kind.group( //16 argument limit TwT
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("height").forGetter(Cliffs::height),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("length").forGetter(Cliffs::length),
                            Codec.INT.fieldOf("world_height").forGetter(Cliffs::worldHeight),
                            Codec.FLOAT.fieldOf("sea_level").forGetter(Cliffs::ocean)
                    )
                    .apply(kind, Cliffs::new)
    );

    public static final KeyDispatchDataCodec<Cliffs> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        float h = (float)this.height.compute(context); // -1 to 1
        float d = (float)this.length.compute(context) / this.worldHeight; // in blocks
        float r = d / (float) 2;
        return Steepness.mod(h-(float)this.ocean+r,d)/r - 1;
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new Cliffs(this.height.mapAll(visitor), this.length.mapAll(visitor), this.worldHeight, this.ocean);
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
        return Cliffs.CODEC;
    }
}
