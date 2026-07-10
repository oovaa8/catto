package com.oovaa8.catto.misc;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.FossilFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class WaterFossilFeature extends Feature<FossilFeatureConfiguration> {
    public WaterFossilFeature(Codec<FossilFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FossilFeatureConfiguration> context) {
        RandomSource randomsource = context.random();
        WorldGenLevel worldgenlevel = context.level();
        BlockPos blockpos = context.origin();

        int move_attempts = randomsource.nextInt(4); // Move down just a lil

        int m = 0;

        while (blockpos.getY() > worldgenlevel.getMinBuildHeight() + 3) {
            if (!worldgenlevel.isEmptyBlock(blockpos.below())) {
                BlockState blockState = worldgenlevel.getBlockState(blockpos.below());
                if (blockState.isSolid()) {
                    if(m > move_attempts) break;
                    m++;
                }
            }

            blockpos = blockpos.below();
        }

        Rotation rotation = Rotation.getRandom(randomsource);
        FossilFeatureConfiguration fossilfeatureconfiguration = context.config();
        int i = randomsource.nextInt(fossilfeatureconfiguration.fossilStructures.size());
        StructureTemplateManager structuretemplatemanager = worldgenlevel.getLevel().getServer().getStructureManager();
        StructureTemplate structuretemplate = structuretemplatemanager.getOrCreate(fossilfeatureconfiguration.fossilStructures.get(i));
        ChunkPos chunkpos = new ChunkPos(blockpos);
        BoundingBox boundingbox = new BoundingBox(
                chunkpos.getMinBlockX() - 16,
                worldgenlevel.getMinBuildHeight(),
                chunkpos.getMinBlockZ() - 16,
                chunkpos.getMaxBlockX() + 16,
                worldgenlevel.getMaxBuildHeight(),
                chunkpos.getMaxBlockZ() + 16
        );
        StructurePlaceSettings structureplacesettings = new StructurePlaceSettings().setRotation(rotation).setBoundingBox(boundingbox).setRandom(randomsource);
        Vec3i vec3i = structuretemplate.getSize(rotation);
        BlockPos blockpos1 = blockpos.offset(-vec3i.getX() / 2, 0, -vec3i.getZ() / 2);

        BlockPos blockpos2 = structuretemplate.getZeroPositionWithTransform(blockpos1, Mirror.NONE, rotation);
        structureplacesettings.clearProcessors();
        fossilfeatureconfiguration.fossilProcessors.value().list().forEach(structureplacesettings::addProcessor);
        structuretemplate.placeInWorld(worldgenlevel, blockpos2, blockpos2, structureplacesettings, randomsource, 4);
        structureplacesettings.clearProcessors();
        return true;
    }
}
