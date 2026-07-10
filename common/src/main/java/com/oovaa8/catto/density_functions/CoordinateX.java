package com.oovaa8.catto.density_functions;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record CoordinateX() implements DensityFunction {
    private static final MapCodec<CoordinateX> DATA_CODEC = MapCodec.unit(new CoordinateX());

    public static final KeyDispatchDataCodec<CoordinateX> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        return context.blockX();
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new CoordinateX();
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
        return CoordinateX.CODEC;
    }
}
