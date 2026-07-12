package net.chauvedev.woodencog.compat.createdieselgenerators;

import net.minecraft.world.level.block.entity.BlockEntity;

public class EmptyCDGIntegration implements ICDGIntegration{

    @Override
    public float getTFCTemperatureOf(BlockEntity be) {
        return 0.0f;
    }
}
