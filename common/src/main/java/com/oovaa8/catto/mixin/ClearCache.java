package com.oovaa8.catto.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Deprecated
@Mixin(MinecraftServer.class)
public class ClearCache {
    @Inject(
            method = "loadLevel",
            at = @At("HEAD")
    )
    public void clear(CallbackInfo ci){
    }
}
