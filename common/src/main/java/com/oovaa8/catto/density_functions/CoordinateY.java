package com.oovaa8.catto.density_functions;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record CoordinateY() implements DensityFunction {
    private static final MapCodec<CoordinateY> DATA_CODEC = MapCodec.unit(new CoordinateY());

    public static final KeyDispatchDataCodec<CoordinateY> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        return context.blockY();
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new CoordinateY();
    }

    @Override
    public double minValue() {
        return Double.MIN_VALUE;
    }
    @Override
    public double maxValue() {
        return Double.MAX_VALUE;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CoordinateY.CODEC;
    }
}
