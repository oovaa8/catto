package com.oovaa8.catto.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.DeltaFeature;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DeltaFeature.class)
public class DeltaWaterMixin {

    @ModifyExpressionValue(method = "isClear", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private static boolean addWater(boolean original, @Local(argsOnly = true) LevelAccessor level, @Local(argsOnly = true) BlockPos pos, @Local() Direction direction, @Local DeltaFeatureConfiguration config){
        return original || (level.getBlockState(pos.relative(direction)).is(Blocks.WATER) && !config.contents().is(Blocks.WATER));
    }
}
