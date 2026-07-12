package net.chauvedev.woodencog.compat.electroenergetics;

import com.george_vi.electroenergetics.content.resistive_heater.ResistiveHeaterBlockEntity;
import net.chauvedev.woodencog.config.WoodenCogCommonConfigs;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CEEIntegrationImpl implements ICEEIntegration {

    @Override
    public float getTFCTemperatureOf(BlockEntity be) {
        if (be instanceof ResistiveHeaterBlockEntity heaterBE) {
            return heaterBE.heat * WoodenCogCommonConfigs.RESISTIVE_HEATER_MAX_TEMP.get();
        }
        return 0.0f;
    }
}
