package com.oovaa8.catto.mixin;


import net.minecraft.world.level.levelgen.VerticalAnchor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Deprecated
@Mixin(VerticalAnchor.class)
public interface AnchorHeightRemapMixin {
    @Inject(method = "absolute(I)Lnet/minecraft/world/level/levelgen/VerticalAnchor;", at = @At("HEAD"), cancellable = true)
    private static void absolute(int value, CallbackInfoReturnable<VerticalAnchor> cir){
        int new_value = -320 + (value + 64) * 2352 / 448;
        cir.setReturnValue(new VerticalAnchor.Absolute(new_value));
    }
}
