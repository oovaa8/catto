package com.oovaa8.catto.mixin;

import com.oovaa8.catto.platform.Services;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CompatMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("ocl")) {
            return Services.PLATFORM.isModLoadedEarly("c2me-opts-accel-opencl") || Services.PLATFORM.isModLoadedEarly("c2me_opts_accel_opencl");
        }
        else if (mixinClassName.contains("sodium")) {
            return Services.PLATFORM.isModLoadedEarly("sodium");
        }else if (mixinClassName.contains("cosmonautics")) {
            return Services.PLATFORM.isModLoadedEarly("rocketnautics");
        }
        else if (mixinClassName.contains("deepsea")) {
            return Services.PLATFORM.isModLoadedEarly("create_submarine");
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
