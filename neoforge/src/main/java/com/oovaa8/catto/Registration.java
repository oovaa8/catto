package com.oovaa8.catto;

import com.oovaa8.catto.density_functions.*;
import com.oovaa8.catto.misc.EverywherePlacementNeoForge;
import com.oovaa8.catto.misc.VentFeature;
import com.oovaa8.catto.misc.WaterFossilFeature;
import com.oovaa8.catto.misc.WaterRockFeature;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.HeightmapLayersRuleSource;
import net.minecraft.world.level.levelgen.feature.FossilFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

public class Registration {

    public static PlacementModifierType<EverywherePlacementNeoForge> EVERYWHERE_PLACEMENT;

    @SubscribeEvent // on the mod event bus
    public static void registerDFs(RegisterEvent event) {
        event.register(
                // This is the registry key of the registry.
                // Get these from BuiltInRegistries for vanilla registries,
                // or from NeoForgeRegistries.Keys for NeoForge registries.
                BuiltInRegistries.DENSITY_FUNCTION_TYPE.key(),
                // Register your objects here.
                registry -> {
                    Constants.LOG.info("REGISTERING DENSITY FUNCTIONS");
                    registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "erosion"), Erosion.CODEC.codec());
                    registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "steepness"), Steepness.CODEC.codec());
                    registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "cliffs"), Cliffs.CODEC.codec());
                    registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "cliffs_inverted"), CliffsInverted.CODEC.codec());

                    registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "x"), CoordinateX.CODEC.codec());
                    registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "y"), CoordinateY.CODEC.codec());
                    registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "z"), CoordinateZ.CODEC.codec());
                }
        );
    }

    @SubscribeEvent
    public static void registerPlacements(RegisterEvent event) {
        event.register(
                BuiltInRegistries.PLACEMENT_MODIFIER_TYPE.key(),
                registry -> {
                    PlacementModifierType<EverywherePlacementNeoForge> type = () -> EverywherePlacementNeoForge.CODEC;
                    EVERYWHERE_PLACEMENT = type;
                    registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "everywhere"), type);
                }
        );
    }

    @SubscribeEvent
    public static void registerFeatures(RegisterEvent event) {
        event.register(
                BuiltInRegistries.FEATURE.key(),
                registry -> {
                    registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "vent"), new VentFeature(NoneFeatureConfiguration.CODEC));
                    registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "water_fossil"), new WaterFossilFeature(FossilFeatureConfiguration.CODEC));
                    registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "water_rock"), new WaterRockFeature(BlockStateConfiguration.CODEC));
                }
        );
    }

    @SubscribeEvent
    public static void registerFluids(RegisterEvent event) {
        event.register(
                BuiltInRegistries.FLUID.key(),
                registry -> {
                    registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "flowing_toxic_brine"), MainMod.FLOWING_TOXIC_BRINE);
                    registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "toxic_brine"), MainMod.TOXIC_BRINE);
                }
        );
    }

    @SubscribeEvent
    public static void registerMaterialRules(RegisterEvent event) {
        event.register(
                BuiltInRegistries.MATERIAL_RULE.key(),
                registry -> {
                    registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "heightmap_layers"), HeightmapLayersRuleSource.CODEC.codec());
                }
        );
    }
}
