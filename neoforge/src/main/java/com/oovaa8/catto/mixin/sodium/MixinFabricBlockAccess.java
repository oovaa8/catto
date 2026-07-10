package com.oovaa8.catto.mixin.sodium;

import com.oovaa8.catto.MainMod;
import net.caffeinemc.mods.sodium.neoforge.block.NeoForgeBlockAccess;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NeoForgeBlockAccess.class)
public class MixinFabricBlockAccess {
    @Inject(method = "shouldOccludeFluid", at = @At("RETURN"), cancellable = true)
    void brine(Direction adjDirection, BlockState adjBlockState, FluidState fluid, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(adjBlockState.getFluidState().getType().isSame(MainMod.TOXIC_BRINE));
    }
}