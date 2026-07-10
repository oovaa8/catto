package com.oovaa8.catto.mixin;

import com.oovaa8.catto.MainMod;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiquidBlockRenderer.class)
public class MixinLiquidBlockRenderer {
    @Inject(method = "isNeighborSameFluid", at = @At("HEAD"), cancellable = true)
    private static void brine(FluidState firstState, FluidState secondState, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(secondState.getType().isSame(MainMod.TOXIC_BRINE));
    }
}
