package com.oovaa8.catto.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.oovaa8.catto.density_functions.OceanMonumentHelper;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OceanMonumentStructure.class)
public class MonumentMixin{
    @Inject(method = "generatePieces", at = @At(value = "HEAD"))
    private static void generatePieces(StructurePiecesBuilder builder, Structure.GenerationContext context, CallbackInfo ci){
        int i = context.chunkPos().getMinBlockX() - 29;
        int j = context.chunkPos().getMinBlockZ() - 29;
        int height = context.chunkGenerator().getBaseHeight(context.chunkPos().getMiddleBlockX(), context.chunkPos().getMiddleBlockZ(), Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        OceanMonumentHelper.offset.put(OceanMonumentHelper.key(i, j), height - 29);
    }

    @Mixin(OceanMonumentPieces.MonumentBuilding.class)
    public static class MonumentPieceMixin{

        @ModifyConstant(method = "<init>(Lnet/minecraft/util/RandomSource;IILnet/minecraft/core/Direction;)V", constant = @Constant(intValue = 39))
        private static int monumentBuilding1(int constant, @Local(ordinal = 0) int x, @Local(ordinal = 1) int z){
            return constant + OceanMonumentHelper.offset.getOrDefault(OceanMonumentHelper.key(x,z), constant); // Sometimes the key wasn't there for whatever reason so be safe and use getOrDefault, same reason I swapped to a LinkedMap, really shouldn't affect much as it's not like there are monuments on every chunk
        }


        @ModifyConstant(method = "<init>(Lnet/minecraft/util/RandomSource;IILnet/minecraft/core/Direction;)V", constant = @Constant(intValue = 1, ordinal = 2))
        private static int monumentBuilding2(int constant, @Local(ordinal = 0) int x, @Local(ordinal = 1) int z){
            return constant + OceanMonumentHelper.offset.getOrDefault(OceanMonumentHelper.key(x,z), constant);
        }

        @ModifyConstant(method = "<init>(Lnet/minecraft/util/RandomSource;IILnet/minecraft/core/Direction;)V", constant = @Constant(intValue = 1, ordinal = 4))
        private static int monumentBuilding6(int constant, @Local(ordinal = 0) int x, @Local(ordinal = 1) int z){
            return constant + OceanMonumentHelper.offset.getOrDefault(OceanMonumentHelper.key(x,z), constant);
        }

        @ModifyConstant(method = "<init>(Lnet/minecraft/util/RandomSource;IILnet/minecraft/core/Direction;)V", constant = @Constant(intValue = 13))
        private static int monumentBuilding3(int constant, @Local(ordinal = 0) int x, @Local(ordinal = 1) int z){
            return constant + OceanMonumentHelper.offset.getOrDefault(OceanMonumentHelper.key(x,z), constant);
        }

        @ModifyConstant(method = "<init>(Lnet/minecraft/util/RandomSource;IILnet/minecraft/core/Direction;)V", constant = @Constant(intValue = 8))
        private static int monumentBuilding4(int constant, @Local(ordinal = 0) int x, @Local(ordinal = 1) int z){
            return constant + OceanMonumentHelper.offset.getOrDefault(OceanMonumentHelper.key(x,z), constant);
        }

        @ModifyConstant(method = "<init>(Lnet/minecraft/util/RandomSource;IILnet/minecraft/core/Direction;)V", constant = @Constant(intValue = 17))
        private static int monumentBuilding5(int constant, @Local(ordinal = 0) int x, @Local(ordinal = 1) int z){
            return constant + OceanMonumentHelper.offset.getOrDefault(OceanMonumentHelper.key(x,z), constant);
        }
    }
}
