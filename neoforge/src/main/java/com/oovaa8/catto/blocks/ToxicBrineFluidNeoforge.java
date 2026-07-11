package com.oovaa8.catto.blocks;

import com.oovaa8.catto.MainMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidType;

public abstract class ToxicBrineFluidNeoforge extends ToxicBrineFluid{
    @Override
    public Fluid getFlowing() {
        return MainMod.FLOWING_TOXIC_BRINE.get();
    }

    @Override
    public Fluid getSource() {
        return MainMod.TOXIC_BRINE.get();
    }

    @Override
    public Item getBucket() {
        return MainMod.TOXIC_BRINE_BUCKET.get();
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == getSource() || fluid == getFlowing();// || fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return MainMod.TOXIC_BRINE_BLOCK.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state)); // Fuck you
    }

    @Override
    public FluidType getFluidType() {
        return MainMod.TOXIC_BRINE_TYPE.get();
    }


    public static class Flowing extends ToxicBrineFluidNeoforge {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends ToxicBrineFluidNeoforge {
        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
