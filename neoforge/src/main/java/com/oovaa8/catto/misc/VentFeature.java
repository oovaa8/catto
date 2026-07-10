package com.oovaa8.catto.misc;

import com.mojang.serialization.Codec;
import com.oovaa8.catto.MainMod;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VentFeature extends Feature<NoneFeatureConfiguration> {
    public VentFeature(Codec<NoneFeatureConfiguration> p_66003_) {
        super(p_66003_);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        WorldGenLevel level = context.level();

        while (level.isWaterAt(pos.below()) && pos.getY() > level.getMinBuildHeight() + 2) {
            pos = pos.below();
        }

        int h = random.nextInt(38) + 14; // Bound is exclusive

        int startRadiusPX = random.nextInt(6) + 2;
        int startRadiusNX = random.nextInt(6) + 2;
        int startRadiusPZ = random.nextInt(6) + 2;
        int startRadiusNZ = random.nextInt(6) + 2;

        int PXHeightOffset = random.nextInt(6) - random.nextInt(5);
        int NXHeightOffset = random.nextInt(6) - random.nextInt(5);
        int PZHeightOffset = random.nextInt(6) - random.nextInt(5);
        int NZHeightOffset = random.nextInt(6) - random.nextInt(5);

        for (int y = 0; y <= h; y++){
            if (y < h - 1 && y > 5) {
                this.setBlock(level, pos.offset(0, y, 0), Blocks.MAGMA_BLOCK.defaultBlockState());
            }else if (y <= 5){
                this.setBlock(level, pos.offset(0, y, 0), Blocks.LAVA.defaultBlockState());
            }
            else if (y == h - 1) {
                this.setBlock(level, pos.offset(0, y, 0), MainMod.ACTIVE_MAGMA.get().defaultBlockState());
            }

            double t = (double) (y) / (h); // 0 at bottom, 1 at top;
            double p = 0.3 + random.nextDouble() * 0.3;
            double falloff = 1.0 - Math.pow(t,p);

            int layerRadiusPX = y <= h + PXHeightOffset ? (int) Math.round(startRadiusPX * falloff) + 1 : 0;
            int layerRadiusNX = y <= h + NXHeightOffset ? (int) Math.round(startRadiusNX * falloff) + 1 : 0;
            int layerRadiusPZ = y <= h + PZHeightOffset ? (int) Math.round(startRadiusPZ * falloff) + 1 : 0;
            int layerRadiusNZ = y <= h + NZHeightOffset ? (int) Math.round(startRadiusNZ * falloff) + 1 : 0;

            for(int x = -layerRadiusNX; x <= layerRadiusPX; x++){
                for(int z = -layerRadiusNZ; z <= layerRadiusPZ; z++){
                    if(x != 0 || z != 0){
                        double radiusX = x >= 0 ? layerRadiusPX : layerRadiusNX;
                        double radiusZ = z >= 0 ? layerRadiusPZ : layerRadiusNZ;

                        double nx = x / radiusX;
                        double nz = z / radiusZ;
                        if (nx * nx + nz * nz <= 1.0) {
                            this.setBlock(level, pos.offset(x, y, z), Blocks.BASALT.defaultBlockState());
                        }
                    }
                }
            }
        }
        return true;
    }
}
