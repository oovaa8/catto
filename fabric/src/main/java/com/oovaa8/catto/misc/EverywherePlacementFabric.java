package com.oovaa8.catto.misc;

import com.mojang.serialization.MapCodec;
import com.oovaa8.catto.MainMod;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class EverywherePlacementFabric extends EverywherePlacement{

    private static final EverywherePlacementFabric INSTANCE = new EverywherePlacementFabric();
    public static final MapCodec<EverywherePlacementFabric> CODEC = MapCodec.unit(() -> INSTANCE);

    public EverywherePlacementFabric() {
        super();
    }

    public static EverywherePlacementFabric spread() {
        return INSTANCE;
    }

    @Override
    public PlacementModifierType<?> type() {
        return MainMod.EVERYWHERE_PLACEMENT;
    }
}
