package com.oovaa8.catto;

import com.mojang.serialization.MapCodec;
import com.oovaa8.catto.blocks.ActiveMagma;
import com.oovaa8.catto.blocks.HalfWaterPlant;
import com.oovaa8.catto.blocks.ToxicBrineBlock;
import com.oovaa8.catto.blocks.ToxicBrineFluidFabric;
import com.oovaa8.catto.density_functions.*;
import com.oovaa8.catto.misc.EverywherePlacementFabric;
import com.oovaa8.catto.misc.VentFeature;
import com.oovaa8.catto.misc.WaterFossilFeature;
import com.oovaa8.catto.misc.WaterRockFeature;
import com.oovaa8.catto.platform.Services;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.HeightmapLayersRuleSource;
import net.minecraft.world.level.levelgen.feature.FossilFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import static net.minecraft.world.item.Items.BUCKET;

public class MainMod implements ModInitializer {

    public static PlacementModifierType<EverywherePlacementFabric> EVERYWHERE_PLACEMENT;

    public static final FlowingFluid FLOWING_TOXIC_BRINE = new ToxicBrineFluidFabric.Flowing();
    public static final FlowingFluid TOXIC_BRINE = new ToxicBrineFluidFabric.Source();

    public static final Block TOXIC_BRINE_BLOCK = new ToxicBrineBlock(
            TOXIC_BRINE,
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
    );

    public static final Block TALL_MARSH_GRASS = new HalfWaterPlant(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .replaceable()
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)
    );

    public static final Item TOXIC_BRINE_BUCKET = new BucketItem(TOXIC_BRINE, new Item.Properties().craftRemainder(BUCKET).stacksTo(1));

    @Override
    public void onInitialize() {
        if (Services.PLATFORM.isModLoaded("mr_blooming_biosphere")) {
            FabricLoader.getInstance().getModContainer(Constants.MOD_ID).ifPresent(modContainer -> {
                ResourceManagerHelper.registerBuiltinResourcePack(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "bb_compat"), modContainer, ResourcePackActivationType.ALWAYS_ENABLED);
            });
        }else{
            FabricLoader.getInstance().getModContainer(Constants.MOD_ID).ifPresent(modContainer -> {
                ResourceManagerHelper.registerBuiltinResourcePack(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "bb_compat"), modContainer, ResourcePackActivationType.NORMAL);
            });
        }

        //TODO: Clean this up and split it into different folders
        CommonClass.init();

        if (Services.PLATFORM.isModLoaded("c2me-opts-accel-opencl")) {
            try {
                Class.forName("com.oovaa8.catto.C2MECompat").getMethod("register").invoke(null);
                Constants.LOG.info("[CATTO]: Successfully initialized C2ME compat");
            } catch (ReflectiveOperationException | LinkageError e) {
                throw new RuntimeException("[CATTO]: Failed to initialize OCL compat", e);
            }
        }else{
            Constants.LOG.warn("[CATTO]: c2me-ocl not loaded, expect slow worldgen");
        }

        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "erosion"), Erosion.CODEC.codec());
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "steepness"), Steepness.CODEC.codec());
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "cliffs"), Cliffs.CODEC.codec());
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "cliffs_inverted"), CliffsInverted.CODEC.codec());

        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "x"), CoordinateX.CODEC.codec());
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "y"), CoordinateY.CODEC.codec());
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "z"), CoordinateZ.CODEC.codec());

        Registry.register(BuiltInRegistries.MATERIAL_RULE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "heightmap_layers"), HeightmapLayersRuleSource.CODEC.codec());
        Registry.register(BuiltInRegistries.FEATURE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "vent"), new VentFeature(NoneFeatureConfiguration.CODEC));
        Registry.register(BuiltInRegistries.FEATURE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "water_fossil"), new WaterFossilFeature(FossilFeatureConfiguration.CODEC));
        Registry.register(BuiltInRegistries.FEATURE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "water_rock"), new WaterRockFeature(BlockStateConfiguration.CODEC));
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "active_magma"), ActiveMagma.ACTIVE_MAGMA);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "active_magma"), new BlockItem(ActiveMagma.ACTIVE_MAGMA, new Item.Properties()));

        Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "magma_smoke"), ActiveMagma.MAGMA_SMOKE);

        Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "flowing_toxic_brine"), FLOWING_TOXIC_BRINE);
        Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "toxic_brine"), TOXIC_BRINE);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "toxic_brine"), TOXIC_BRINE_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "toxic_brine_bucket"), TOXIC_BRINE_BUCKET);

        EVERYWHERE_PLACEMENT = registerPlacement("everywhere", EverywherePlacementFabric.CODEC);

        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "tall_marsh_grass"), MainMod.TALL_MARSH_GRASS);

        ColorProviderRegistry.BLOCK.register(
                (state, world, pos, tintIndex) -> world != null && pos != null
                        ? BiomeColors.getAverageGrassColor(world, pos)
                        : GrassColor.getDefaultColor(),
                MainMod.TALL_MARSH_GRASS
        );
    }

    private static <P extends PlacementModifier> PlacementModifierType<P> registerPlacement(String name, MapCodec<P> codec) {
        return Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), () -> codec);
    }
}
