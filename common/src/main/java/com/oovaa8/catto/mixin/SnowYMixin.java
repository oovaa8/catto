package com.oovaa8.catto.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public abstract class SnowYMixin {
    @Unique
    private static final float VANILLA_HEIGHT = 365.0F;
    @Unique
    private static final float WORLD_HEIGHT = 18000F;//2352.0F; while 2352 would be accurate to vanilla I hate snow biomes so...

    @Unique
    private static final float HEIGHT_SCALE = VANILLA_HEIGHT / WORLD_HEIGHT;
    @Unique
    private static final int SNOW_OFFSET = (int)(17 * (WORLD_HEIGHT / VANILLA_HEIGHT));

    @Shadow
    @Final
    private static PerlinSimplexNoise TEMPERATURE_NOISE;

    @Inject(method = "getHeightAdjustedTemperature", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;getY()I"), cancellable = true)
    private void getHeightAdjustedTemperature(CallbackInfoReturnable<Float> cir, @Local(argsOnly = true) BlockPos pos, @Local(ordinal = 0) float f) {
        int snowLevel = 63 + SNOW_OFFSET;
        if (pos.getY() > snowLevel) {
            float f1 = (float)(TEMPERATURE_NOISE.getValue((double)((float)pos.getX() / 8.0F), (double)((float)pos.getZ() / 8.0F), false) * 8.0);
            float noise = f1 * 0.05F / 40.0F;
            float height = (pos.getY() - snowLevel) * 0.05F / 40.0F * HEIGHT_SCALE;
            cir.setReturnValue(f - noise - height);
        } else {
            cir.setReturnValue(f);
        }
    }
}