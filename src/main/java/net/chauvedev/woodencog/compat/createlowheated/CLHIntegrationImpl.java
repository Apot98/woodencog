package net.chauvedev.woodencog.compat.createlowheated;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.chauvedev.woodencog.config.WoodenCogCommonConfigs;
import net.minecraft.world.level.block.entity.BlockEntity;
import zeh.createlowheated.content.processing.basicburner.BasicBurnerBlockEntity;

public class CLHIntegrationImpl implements ICLHIntegration {

    public float lowHeatTemp(BlazeBurnerBlock.HeatLevel heatLevel){
        if(heatLevel.name().equals("LOW")) return WoodenCogCommonConfigs.BLAZE_BURNER_SMOULDERING.get().floatValue() / 2.0f;
        return 0;
    }

    /**
     * Get a tfc temperature value for a given blockEntity
     *
     * @param be BlockEntity of other mods
     */
    @Override
    public float getTFCTemperatureOf(BlockEntity be) {
        if(be instanceof BasicBurnerBlockEntity burner){
            //return CogUtil.heatLevelToTemp(burner.getHeatLevelFromBlock());
            return ((IMixinBasicBurnerBlockEntity)burner).WoodenCog$getTemperature();
        }
        return 0;
    }
}
