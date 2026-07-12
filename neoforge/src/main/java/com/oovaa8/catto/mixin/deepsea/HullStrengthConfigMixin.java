package com.oovaa8.catto.mixin.deepsea;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.maxenonyme.createsubmarine.submarine.config.HullStrengthConfig;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HullStrengthConfig.class)
public class HullStrengthConfigMixin {

    /**
     * Divides the auto-computed default maxWaterDepth by 5 for any block that
     * falls back to autoCompute() (i.e. has no explicit entry in the config
     * file or in buildStaticDefaults()). implosionChance from the original
     * computation is left untouched.
     */
    @ModifyReturnValue(method = "autoCompute", at = @At("RETURN"))
    private static HullStrengthConfig.HullProperty createsubmarine$reduceDefaultDepth(
            HullStrengthConfig.HullProperty original, BlockState state, net.minecraft.resources.ResourceLocation id) {
        int reducedDepth = Math.max(1, original.maxWaterDepth() * 5);
        return new HullStrengthConfig.HullProperty(reducedDepth, original.implosionChance());
    }
}

