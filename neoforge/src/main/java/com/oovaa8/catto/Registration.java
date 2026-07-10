package com.oovaa8.catto;

import com.oovaa8.catto.density_functions.*;
import com.oovaa8.catto.misc.EverywherePlacementNeoForge;
import com.oovaa8.catto.misc.VentFeature;
import com.oovaa8.catto.misc.WaterFossilFeature;
import com.oovaa8.catto.misc.WaterRockFeature;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.levelgen.feature.FossilFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
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
    public static void registerMaterialRules(RegisterEvent event) {
        event.register(
                BuiltInRegistries.MATERIAL_RULE.key(),
                registry -> {
                    registry.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "heightmap_layers"), com.oovaa8.catto.misc.HeightmapLayersRuleSource.CODEC.codec());
                }
        );
    }


    @SubscribeEvent
    public static void addPackFinders(AddPackFindersEvent event) {
        event.addPackFinders(
            // Path relative to your mods 'resources' pointing towards this pack
            // Take note this also defines your packs id using the following format
            // mod/<namespace>:<path>`, e.g. `mod/examplemod:data/examplemod/datapacks/experimental`
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "resourcepacks/bb_compat"),

            // What kind of resources are contained within this pack
            // 'CLIENT_RESOURCES' for packs with client assets (resource packs)
            // 'SERVER_DATA' for packs with server data (data packs)
            PackType.SERVER_DATA,

            // Display name shown in the Experiments screen
            Component.literal("CATTO: Blooming Biosphere compatibility data"),

            // In order for this pack to load and enable feature flags, this MUST be 'FEATURE',
            // any other PackSource type is invalid here
            PackSource.FEATURE,

            // If this is true, the pack is always active and cannot be disabled, should always be false for feature packs
            false,

            // Priority to load resources from this pack in
            // 'TOP' this pack will be prioritized over other packs
            // 'BOTTOM' other packs will be prioritized over this pack
            Pack.Position.TOP
        );
    }
}
