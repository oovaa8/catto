package com.oovaa8.catto.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.BasaltColumnsFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BasaltColumnsFeature.class)
public class BasalColumnsWaterMixin {
    @Inject(method = "isAirOrLavaOcean", at = @At("HEAD"), cancellable = true)
    private static void addWater(LevelAccessor level, int seaLevel, BlockPos pos, CallbackInfoReturnable<Boolean> cir){
        BlockState b = level.getBlockState(pos);
        if (b.is(Blocks.WATER)){
            cir.setReturnValue(true);
        }
    }
}
