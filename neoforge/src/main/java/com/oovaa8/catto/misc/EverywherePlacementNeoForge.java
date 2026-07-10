package com.oovaa8.catto.misc;

import com.mojang.serialization.MapCodec;
import com.oovaa8.catto.Registration;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class EverywherePlacementNeoForge extends EverywherePlacement{

    private static final EverywherePlacementNeoForge INSTANCE = new EverywherePlacementNeoForge();
    public static final MapCodec<EverywherePlacementNeoForge> CODEC = MapCodec.unit(() -> INSTANCE);

    public EverywherePlacementNeoForge() {
        super();
    }

    public static EverywherePlacementNeoForge spread() {
        return INSTANCE;
    }

    @Override
    public PlacementModifierType<?> type() {
        return Registration.EVERYWHERE_PLACEMENT;
    }
}