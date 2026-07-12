package net.chauvedev.woodencog.compat.createlowheated;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public interface IMixinBasicBurnerBlock {
    EnumProperty<BlazeBurnerBlock.HeatLevel> BLAZE_HEAT_LEVEL = BlazeBurnerBlock.HEAT_LEVEL;
}
