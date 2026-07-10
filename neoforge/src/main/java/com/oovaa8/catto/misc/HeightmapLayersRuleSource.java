// I hate that this works with every fiber of my being
package com.oovaa8.catto.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.List;

public record HeightmapLayersRuleSource(List<Integer> offsets, List<BlockState> blocks)
        implements SurfaceRules.RuleSource {

    public static final KeyDispatchDataCodec<HeightmapLayersRuleSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.INT.listOf().fieldOf("offsets").forGetter(HeightmapLayersRuleSource::offsets),
                    BlockState.CODEC.listOf().fieldOf("blocks").forGetter(HeightmapLayersRuleSource::blocks)
            ).apply(instance, HeightmapLayersRuleSource::new))
    );

    public HeightmapLayersRuleSource {
        if (offsets.size() != blocks.size()) {
            throw new IllegalArgumentException("offsets and blocks must be the same length");
        }
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
        return CODEC;
    }

    public SurfaceRules.SurfaceRule apply(final SurfaceRules.Context context) {
        return (x, y, z) -> {
            int surfaceY = context.getMinSurfaceLevel() + 14; //We want to use preliminary here so cave entrances don't move it; + 14 to raise it back to real surface
            int depth = surfaceY - y;

            if (depth < 0) {
                return null;
            }

            BlockState result = null;
            int bestOffset = Integer.MIN_VALUE;
            for (int i = 0; i < this.offsets.size(); i++) {
                int off = this.offsets.get(i);
                if (off <= depth && off > bestOffset) {
                    bestOffset = off;
                    result = this.blocks.get(i);
                }
            }
            return result;
        };
    }
}