package com.oovaa8.catto.mixin.ocl;

import com.ishland.c2me.opts.accel.opencl.common.gen.CLDataUtil;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(value = CLDataUtil.class, remap = false)
public abstract class oclMarkerRegisterMixin {
/*
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
    }*/
}