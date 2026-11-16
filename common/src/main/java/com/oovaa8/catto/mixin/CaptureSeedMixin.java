package com.oovaa8.catto.mixin;

import com.oovaa8.catto.misc.SeedHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//This should hopefully run before any world gen does
@Mixin(MinecraftServer.class)
public abstract class CaptureSeedMixin {
    @Shadow public abstract WorldData getWorldData();

    @Inject(method = "loadLevel", at = @At("HEAD"))
    private void mymod$captureSeed(CallbackInfo ci) {
        SeedHolder.seed = (this.getWorldData().worldGenOptions().seed());
    }
}
