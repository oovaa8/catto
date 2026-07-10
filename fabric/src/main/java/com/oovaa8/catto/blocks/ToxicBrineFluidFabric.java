package com.oovaa8.catto.blocks;

import com.oovaa8.catto.MainMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public abstract class ToxicBrineFluidFabric extends ToxicBrineFluid{
    @Override
    public Fluid getFlowing() {
        return MainMod.FLOWING_TOXIC_BRINE;
    }

    @Override
    public Fluid getSource() {
        return MainMod.TOXIC_BRINE;
    }

    @Override
    public Item getBucket() {
        return MainMod.TOXIC_BRINE_BUCKET;
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == getSource() || fluid == getFlowing();// || fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return MainMod.TOXIC_BRINE_BLOCK.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state)); // Fuck you
    }


    public static class Flowing extends ToxicBrineFluidFabric {
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

    public static class Source extends ToxicBrineFluidFabric {
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
