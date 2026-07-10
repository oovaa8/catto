package com.oovaa8.catto.mixin;

import net.minecraft.world.entity.animal.WaterAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WaterAnimal.class)
public class WaterAnimalMixin {
    @ModifyConstant(method = "checkSurfaceWaterAnimalSpawnRules", constant = @Constant(intValue = 13))
    private static int changeSpawnY(int constant){
        return 304;
    }
}