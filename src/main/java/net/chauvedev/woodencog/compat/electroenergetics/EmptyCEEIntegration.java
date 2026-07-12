package net.chauvedev.woodencog.compat.electroenergetics;

import net.minecraft.world.level.block.entity.BlockEntity;

public class EmptyCEEIntegration implements ICEEIntegration {

    @Override
    public float getTFCTemperatureOf(BlockEntity be) {
        return 0.0f;
    }
}
