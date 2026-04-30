package net.chauvedev.woodencog.compat.createaddition;

import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlock;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.chauvedev.woodencog.utils.CogUtil;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CCAIntegrationImpl implements ICCAIntegration{

    @Override
    public float getTFCTemperatureOf(BlockEntity be) {
        if(be instanceof LiquidBlazeBurnerBlockEntity){
            return CogUtil.heatLevelToTemp(be.getBlockState().getValue(BlazeBurnerBlock.HEAT_LEVEL));
        }
        return 0;
    }
}
