package com.oovaa8.catto.misc;

import com.mojang.serialization.Codec;
import com.oovaa8.catto.Constants;
import com.oovaa8.catto.MainMod;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

@Deprecated
public class BrinePoolsFeature extends Feature<NoneFeatureConfiguration> {
    public BrinePoolsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        RandomSource random = context.random();
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();

        BlockState rim = Blocks.GRAVEL.defaultBlockState();
        BlockState fill = MainMod.TOXIC_BRINE_BLOCK.defaultBlockState();

        while (level.isWaterAt(pos) && pos.getY() > level.getMinBuildHeight() + 2) {
            pos = pos.below();
        }

        if(pos.getY() <= level.getMinBuildHeight() + 2) return false;

        double rot = random.nextDouble() * 2 * Math.PI;
        int a = random.nextInt(21) + 10;
        int b = random.nextInt(21) + 10;

        double sin = Math.sin(rot);
        double cos = Math.cos(rot);

        BlockPos c1 = pos.offset((int) (a*sin), 0 , (int) (a*cos));
        BlockPos c2 = pos.offset((int) -(a*sin), 0 , (int) -(a*cos));
        BlockPos c3 = pos.offset((int) (b*cos), 0 , (int) -(b*sin));
        BlockPos c4 = pos.offset((int) -(b*cos), 0 , (int) (b*sin));
        if(level.isWaterAt(c1)){
            return false;
        }else if(level.isWaterAt(c2)){
            return false;
        }else if(level.isWaterAt(c3)){
            return false;
        }else if(level.isWaterAt(c4)){
            return false;
        }

        int radius = Math.max(a, b);
        for (int x = -radius; x < radius; x++) {
            for (int z = -radius; z < radius; z++) {
                BlockPos currentPos = pos.offset(x,0,z);
                if (!level.hasChunkAt(currentPos)) {
                    Constants.LOG.warn("Tried generating brine pool in ungenerated chunk");
                    continue;
                }
                double m = (sin*x + cos*z);
                double n = (cos*x + sin*z);

                double ellipseBig = (m * m) / (a * a) + (n * n) / (b * b);
                double ellipseSmall = (m*m)/((a-1.1)*(a-1.1)) + (n*n)/((b-1.1)*(b-1.1));
                if(ellipseSmall <= 1) {
                    level.setBlock(currentPos, fill, 3);
                    if(level.isWaterAt(currentPos.below())){
                        level.setBlock(currentPos.below(), Blocks.STONE.defaultBlockState(), 3);
                    }
                } else if (ellipseBig <= 1) {
                    if(level.isWaterAt(currentPos)){
                        level.setBlock(currentPos, rim, 3);
                    }
                }

            }
        }

        return true;
    }
}