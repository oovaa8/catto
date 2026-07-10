package com.oovaa8.catto.mixin;

import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$Context")
public interface SurfaceRulesContextAccessor {
    @Accessor("chunk")
    ChunkAccess getChunk();

    @Invoker("getMinSurfaceLevel")
    int getMinSurfaceLevel();
}
