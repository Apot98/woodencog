package net.chauvedev.woodencog.compat.createdieselgenerators;

import com.jesz.createdieselgenerators.content.burner.BurnerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import static java.lang.Math.min;

public class CDGIntegrationImpl implements ICDGIntegration{

    @Override
    public float getTFCTemperatureOf(BlockEntity be) {
        if (be instanceof BurnerBlockEntity dieselBurner) {
            return min(dieselBurner.heat * 1000, 2500.0f);
        }
        return 0.0f;
    }
}
