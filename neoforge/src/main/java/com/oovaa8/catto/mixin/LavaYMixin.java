package com.oovaa8.catto.mixin;

import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(NoiseBasedChunkGenerator.class)
public class LavaYMixin {

    @ModifyConstant(
            method = "createFluidPicker(Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;)Lnet/minecraft/world/level/levelgen/Aquifer$FluidPicker;",
            constant = @Constant(intValue = -54)
    )
    private static int modifyLavaFluidStatusY(int constant) {
        return -300;
    }

    @ModifyConstant(
            method = "lambda$createFluidPicker$4",
            constant = @Constant(intValue = -54)
    )
    private static int modifyLavaFluidStatusYLambda(int constant) {
        return -300;
    }
}