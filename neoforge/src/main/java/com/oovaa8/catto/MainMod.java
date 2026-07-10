package com.oovaa8.catto;


import com.oovaa8.catto.blocks.ActiveMagma;
import com.oovaa8.catto.blocks.HalfWaterPlant;
import com.oovaa8.catto.blocks.ToxicBrineBlock;
import com.oovaa8.catto.blocks.ToxicBrineFluidNeoforge;
import com.oovaa8.catto.platform.Services;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.*;

import static net.minecraft.world.item.Items.BUCKET;

@Mod(Constants.MOD_ID)
public class MainMod {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, Constants.MOD_ID);

    public static final DeferredBlock<Block> TALL_MARSH_GRASS = BLOCKS.register(
            "tall_marsh_grass",
            registryName -> new HalfWaterPlant(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .replaceable()
                            .noCollission()
                            .instabreak()
                            .sound(SoundType.GRASS)
                            .offsetType(BlockBehaviour.OffsetType.XZ)
                            .ignitedByLava()
                            .pushReaction(PushReaction.DESTROY)
            ));

    public static final DeferredBlock<Block> ACTIVE_MAGMA = BLOCKS.register(
            "active_magma",
            registryName -> new ActiveMagma(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NETHER)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .lightLevel(p_152684_ -> 8)
                    .strength(0.5F)
                    .isValidSpawn((p_187421_, p_187422_, p_187423_, p_187424_) -> p_187424_.fireImmune())
                    .hasPostProcess((x, y, z) -> true)
                    .emissiveRendering((x, y, z) -> true)));

    public static final DeferredItem<BlockItem> ACTIVE_MAGMA_ITEM = ITEMS.registerSimpleBlockItem(
            "active_magma",
            ACTIVE_MAGMA);


    public static final DeferredHolder<Fluid, FlowingFluid> TOXIC_BRINE =
            FLUIDS.register("toxic_brine",
                    ToxicBrineFluidNeoforge.Source::new);

    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_TOXIC_BRINE =
            FLUIDS.register("flowing_toxic_brine",
                    ToxicBrineFluidNeoforge.Flowing::new);

    public static final DeferredBlock<Block> TOXIC_BRINE_BLOCK = BLOCKS.register(
            "toxic_brine",
            registryName -> new ToxicBrineBlock(
                    TOXIC_BRINE.get(),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WATER)
                            .replaceable()
                            .noCollission()
                            .strength(100.0F)
                            .pushReaction(PushReaction.DESTROY)
                            .noLootTable()
                            .liquid()
                            .sound(SoundType.EMPTY)
                            .emissiveRendering((x, y, z) -> true)
                            .lightLevel(p_152684_ -> 3)
            ));

    public static final DeferredItem<BucketItem> TOXIC_BRINE_BUCKET = ITEMS.registerItem("toxic_brine_bucket",
            props -> new BucketItem(TOXIC_BRINE.get(), new Item.Properties().craftRemainder(BUCKET).stacksTo(1)));

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Constants.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> TOXIC_BRINE_TYPE = FLUID_TYPES.register(
            "toxic_brine",
            () -> new FluidType(FluidType.Properties.create()
                    .density(1200)
                    .viscosity(1200)
                    .temperature(300)
                    .lightLevel(3)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY).canPushEntity(true).canDrown(true).canConvertToSource(true).canSwim(true))
    );


    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Constants.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MAGMA_SMOKE =
            PARTICLE_TYPES.register("magma_smoke", () -> new SimpleParticleType(true));

    public MainMod(IEventBus eventBus) {
        CommonClass.init();

        // I can probably move this to the common class but whatever it stays
        if (Services.PLATFORM.isModLoaded("c2me_opts_accel_opencl")) {
            try {
                Class.forName("com.oovaa8.catto.C2MECompat").getMethod("register").invoke(null);
                Constants.LOG.info("Successfully initialized C2ME compat");
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to initialize OCL compat", e);
            }
        }else{
            Constants.LOG.warn("c2me-ocl not loaded, expect slow worldgen");
        }

        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        FLUIDS.register(eventBus);
        FLUID_TYPES.register(eventBus);
        PARTICLE_TYPES.register(eventBus);

        eventBus.register(Registration.class);
        eventBus.register(ClientRegistration.class);
    }
}