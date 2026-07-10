package com.oovaa8.catto.mixin;

import com.oovaa8.catto.MainMod;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityBrineDamageMixin {

    @Shadow
    @Deprecated
    public abstract boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> fluidTag, double motionScale);

    @Shadow
    public abstract boolean hurt(DamageSource source, float amount);

    @Shadow
    public abstract DamageSources damageSources();

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getZ();

    @Shadow
    private Level level;

    @Inject(method = "updateInWaterStateAndDoWaterCurrentPushing", at =  @At("HEAD"))
    void brineDamage(CallbackInfo ci){
        BlockPos blockpos = BlockPos.containing(this.getX(), getY(), this.getZ());
        FluidState fluidstate = this.level.getFluidState(blockpos);
        if(fluidstate.is(MainMod.TOXIC_BRINE)){
            this.hurt(this.damageSources().wither(),2.5F);
        }
    }
}
