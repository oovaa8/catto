package com.oovaa8.catto.blocks;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class ActiveMagma extends Block {

    public static final SimpleParticleType MAGMA_SMOKE = FabricParticleTypes.simple();


    public static final ActiveMagma ACTIVE_MAGMA = new ActiveMagma(Properties.of()
            .mapColor(MapColor.NETHER)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .lightLevel(p_152684_ -> 8)
            .strength(0.5F)
            .isValidSpawn((p_187421_, p_187422_, p_187423_, p_187424_) -> p_187424_.fireImmune())
            .hasPostProcess((x, y, z) -> true)
            .emissiveRendering((x, y, z) -> true));

    public ActiveMagma(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (entity instanceof LivingEntity) { // Hurts no matter what cause it's active or something
            entity.hurt(level.damageSources().hotFloor(), 1.0F);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        int ra = random.nextInt(1);

        for (int i = 0; i < ra + 1; i++) {
            level.addParticle(
                    ParticleTypes.LAVA,
                    (double)pos.getX() + 0.5,
                    (double)pos.getY() + 1,
                    (double)pos.getZ() + 0.5,
                    (double)(random.nextFloat() / 2.0F),
                    5.0E-5,
                    (double)(random.nextFloat() / 2.0F)
            );
        }

        int r = random.nextInt(9);


        for (int i = 0; i < r + 6; i++) {
            level.addParticle(
                    MAGMA_SMOKE,
                    true,
                    (double) pos.getX() + 0.5 + random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1),
                    (double) pos.getY() + 1 + random.nextDouble() + random.nextDouble(),
                    (double) pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1),
                    0.0,
                    0.07,
                    0.0
            );
        }
    }

    /*
    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.UP) {
            if(facingState.is(Blocks.WATER) || facingState.is(Blocks.AIR)){
                this.stateDefinition.any().setValue(LIT, true);
            }else{
                this.stateDefinition.any().setValue(LIT, false);
            }
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }*/
}
