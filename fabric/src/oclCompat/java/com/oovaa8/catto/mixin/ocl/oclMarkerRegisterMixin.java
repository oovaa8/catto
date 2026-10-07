//package com.oovaa8.catto.mixin.ocl;
/*
import com.ishland.c2me.base.common.util.MemoryUtil;
import com.ishland.c2me.opts.accel.opencl.common.compiler.GeneratedCLSource;
import com.ishland.c2me.opts.accel.opencl.common.compiler.emitters.misc.CLBlockStateMappings;
import com.ishland.c2me.opts.accel.opencl.common.gen.CLDataUtil;
import com.ishland.c2me.opts.accel.opencl.common.gen.cache.Stage1Cache;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.util.Mth;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;

import static com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen.MARKER_localOffsetTable;

@Mixin(value = CLDataUtil.class, remap = false)
public abstract class oclMarkerRegisterMixin {

    @Inject(method = "worldgen_data_root$createForFlatCacheOnly", at =
    @At(value = "INVOKE", target = "Lcom/ishland/flowsched/util/Assertions;assertTrue(Z)V", ordinal = 0))
    private static void addKey(ChunkPos base, int chunkSize, GeneratedCLSource generatedCLSource, double[] flatCachePrefilled, boolean extendByOne, CallbackInfoReturnable<ByteBuffer> cir, @Local(name = "index") int index, @Local(name = "key") LocalRef<Object> key, @Local(name = "currentTail") LocalIntRef currentTail){
        if(key.get() == ErosionEmitter.MARKER_cacheLike_erosion){
            key.set(MARKER_localOffsetTable);
        }
    }

    @Inject(method = "worldgen_data_root$createForArea", at =
    @At(value = "INVOKE", target = "Lcom/ishland/flowsched/util/Assertions;assertTrue(Z)V", ordinal = 1))
    private static void addKey(ChunkPos basePos, int horizontalChunkSize, StaticCache2D<ProtoChunk> regionArray, NoiseBasedChunkGenerator generator, RandomState noiseConfig, StaticCache2D<StructureManager> structureAccessors, CLBlockStateMappings mappings, GeneratedCLSource generatedCLSource, Stage1Cache.AreaCacheEntry stage1Cache,
                               CallbackInfoReturnable<ByteBuffer> cir, @Share("oset") LocalIntRef oset, @Local(name = "key") LocalRef<Object> key, @Local(name = "currentTail") LocalIntRef currentTail){
        oset.set(-1);
        if(key.get() == ErosionEmitter.MARKER_cacheLike_erosion){
            int offset = MemoryUtil.roundUp(currentTail.get(), 16);
            int bufSize = Mth.square(horizontalChunkSize * 16) * 16;
            currentTail.set(offset + bufSize);
            key.set(MARKER_localOffsetTable);
            oset.set(offset);
        }
    }

    @ModifyArg(
            method = "worldgen_data_root$createForArea",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/ishland/c2me/opts/accel/opencl/common/gen/CLDataUtil$2OffsetAndData;<init>(I[B)V",
                    ordinal = 0
            ), index = 0
    )
    private static int setOffset(int original, @Share("oset") LocalIntRef oset){
        int offset = oset.get();
        if(offset == -1) return 0;
        return offset;
    }
}*/