package com.oovaa8.catto;

import com.oovaa8.catto.blocks.ActiveMagma;
import com.oovaa8.catto.misc.MagmaSmokeProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class MainModClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        Constants.LOG.info("Initializing Client");
        ParticleFactoryRegistry.getInstance().register(ActiveMagma.MAGMA_SMOKE, MagmaSmokeProvider.Provider::new);

        FluidRenderHandlerRegistry.INSTANCE.register(MainMod.TOXIC_BRINE, MainMod.FLOWING_TOXIC_BRINE, new SimpleFluidRenderHandler(
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block/toxic_brine_still"),
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block/toxic_brine_flow"),
                ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_overlay")));

        BlockRenderLayerMap.INSTANCE.putFluids(RenderType.translucent(), MainMod.TOXIC_BRINE, MainMod.FLOWING_TOXIC_BRINE);

        BlockRenderLayerMap.INSTANCE.putBlock(CommonClass.TALL_MARSH_GRASS, RenderType.cutout());
    }

}