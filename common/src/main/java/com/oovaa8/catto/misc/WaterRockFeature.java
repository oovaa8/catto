package com.oovaa8.catto.misc;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class WaterRockFeature extends Feature<BlockStateConfiguration> {
    public WaterRockFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        BlockPos blockPos = context.origin();
        WorldGenLevel worldGenLevel = context.level();
        RandomSource randomSource = context.random();
        BlockStateConfiguration blockStateConfiguration = context.config();

        while (blockPos.getY() > worldGenLevel.getMinBuildHeight() + 3) {
            if (!worldGenLevel.isEmptyBlock(blockPos.below())) {
                BlockState blockState = worldGenLevel.getBlockState(blockPos.below());
                if (blockState.isSolid()) {
                    break;
                }
            }

            blockPos = blockPos.below();
        }

        if (blockPos.getY() <= worldGenLevel.getMinBuildHeight() + 3) {
            return false;
        }

        for (int i = 0; i < 3; i++) {
            int j = randomSource.nextInt(2);
            int k = randomSource.nextInt(2);
            int l = randomSource.nextInt(2);
            float f = (j + k + l) * 0.333F + 0.5F;

            for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-j, -k, -l), blockPos.offset(j, k, l))) {
                if (blockPos2.distSqr(blockPos) <= f * f) {
                    worldGenLevel.setBlock(blockPos2, blockStateConfiguration.state, 3);
                }
            }

            blockPos = blockPos.offset(-1 + randomSource.nextInt(2), -randomSource.nextInt(2), -1 + randomSource.nextInt(2));
        }

        return true;
    }
}
