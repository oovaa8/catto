package com.oovaa8.catto;

import com.oovaa8.catto.misc.MagmaSmokeProvider;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GrassColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientRegistration {

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, world, pos, tintIndex) -> world != null && pos != null
                        ? BiomeColors.getAverageGrassColor(world, pos)
                        : GrassColor.getDefaultColor(),
                MainMod.TALL_MARSH_GRASS.get()
        );
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MainMod.MAGMA_SMOKE.get(), MagmaSmokeProvider.Provider::new);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block/toxic_brine_still");
            }
            @Override
            public ResourceLocation getFlowingTexture() {
                return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block/toxic_brine_flow");
            }
            @Override
            public ResourceLocation getOverlayTexture() {
                return ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_overlay");
            }
        }, MainMod.TOXIC_BRINE_TYPE.value());
    }
}
