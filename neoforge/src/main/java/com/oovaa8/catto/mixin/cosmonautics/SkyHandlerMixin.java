package com.oovaa8.catto.mixin.cosmonautics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "dev.devce.rocketnautics.client.SkyHandler")
public abstract class SkyHandlerMixin {

    @ModifyConstant(
            method = "onRenderLevelStage",
            constant = @Constant(doubleValue = 1000.0D)
    )
    private static double rocketnautics$modifyVisibilityStartHeight(double original) {
        return 2100.0D;
    }
}