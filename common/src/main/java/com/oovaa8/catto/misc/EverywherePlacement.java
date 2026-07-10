package com.oovaa8.catto.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.stream.Stream;

//Returns every position in the chunk
public abstract class EverywherePlacement extends PlacementModifier {
    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        return Stream.iterate(0, i -> i + 1).limit(256).map(i -> new BlockPos(pos.getX() + (i & 15), pos.getY(), pos.getZ() + (i >> 4)));
    }
}