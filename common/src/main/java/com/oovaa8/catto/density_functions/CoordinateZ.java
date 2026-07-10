package com.oovaa8.catto.density_functions;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record CoordinateZ() implements DensityFunction {
    private static final MapCodec<CoordinateZ> DATA_CODEC = MapCodec.unit(new CoordinateZ());

    public static final KeyDispatchDataCodec<CoordinateZ> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        return context.blockZ();
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new CoordinateZ();
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
        return CoordinateZ.CODEC;
    }
}
