package com.oovaa8.catto.mixin;


import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Deprecated
@Mixin(OreFeature.class)
public class OreFeatureSurfaceCheckMixin {
    @Inject(method = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z", at = @At("HEAD"), cancellable = true)
    private void place(FeaturePlaceContext<OreConfiguration> context, CallbackInfoReturnable<Boolean> cir){
        int y = context.origin().getY();
        if (y <= -320) cir.setReturnValue(false);
        int heigth = context.level().getHeight(Heightmap.Types.OCEAN_FLOOR_WG, context.origin().getX(), context.origin().getZ());
        if(y>=heigth+40) cir.setReturnValue(false);
    }
}
