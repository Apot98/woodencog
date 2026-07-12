package net.chauvedev.woodencog.compat.createlowheated;

import net.minecraft.core.Direction;

public interface IMixinBasicBurnerBlockEntity {
    void WoodenCog$updateFanSpeed(float speed, Direction direction);

    float WoodenCog$getTemperature();

    float WoodenCog$getRemainingBurnTimeFractional();

    float WoodenCog$getFuelConsumption();
}
