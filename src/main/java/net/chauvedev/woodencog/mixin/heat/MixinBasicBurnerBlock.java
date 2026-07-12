package net.chauvedev.woodencog.mixin.heat;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlock;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.block.IBE;
import net.chauvedev.woodencog.compat.createlowheated.IMixinBasicBurnerBlock;
import net.chauvedev.woodencog.config.WoodenCogCommonConfigs;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import zeh.createlowheated.common.Configuration;
import zeh.createlowheated.content.processing.basicburner.BasicBurnerBlock;
import zeh.createlowheated.content.processing.basicburner.BasicBurnerBlockEntity;

import java.util.Arrays;

@Mixin(value = BasicBurnerBlock.class, remap = false)
public abstract class MixinBasicBurnerBlock extends HorizontalDirectionalBlock implements IBE<BasicBurnerBlockEntity>, IWrenchable, IMixinBasicBurnerBlock {
    //@Unique
    //protected static final EnumProperty<BlazeBurnerBlock.HeatLevel> BLAZE_HEAT_LEVEL = EnumProperty.create("blaze", BlazeBurnerBlock.HeatLevel.class);

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lzeh/createlowheated/content/processing/basicburner/BasicBurnerBlock;registerDefaultState(Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private BlockState registerExtraState(BlockState blockState) {
        return blockState.setValue(BLAZE_HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE);
    }

    @ModifyArg(method = "createBlockStateDefinition", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/StateDefinition$Builder;add([Lnet/minecraft/world/level/block/state/properties/Property;)Lnet/minecraft/world/level/block/state/StateDefinition$Builder;"))
    private Property[] mixinBlockStateDefinition(Property[] properties) {
        Property[] newArray = Arrays.copyOf(properties, properties.length + 1);
        newArray[newArray.length - 1] = BLAZE_HEAT_LEVEL;
        return newArray;
    }

    @Inject(method = "getStateForPlacement", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void mixinGetStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        boolean isEmpowered = false;
        int dunswe = 0;
        BlockPos burnerPos = context.getClickedPos();
        Direction[] var5 = Iterate.directions;
        int var6 = var5.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            Direction side = var5[var7];
            BlockPos fanPos = burnerPos.relative(side);
            BlockEntity fan = context.getLevel().getBlockEntity(fanPos);
            if (fan instanceof EncasedFanBlockEntity fanBE) {
                Direction fanFacingDir = (Direction)fan.getBlockState().getValue(EncasedFanBlock.FACING);
                BlockPos fanFacingPos = fanPos.relative(fanFacingDir);
                if (burnerPos.equals(fanFacingPos)) {
                    boolean empowering = Mth.abs(fanBE.getSpeed()) >= WoodenCogCommonConfigs.BASIC_BURNER_MIN_FAN_SPEED.get();
                    if (empowering) {
                        int mask = 32 >> side.ordinal();
                        dunswe |= mask;
                    }

                    if ((!(Boolean)Configuration.FAN_HORIZONTAL_ONLY.get() || side.getAxis().isHorizontal()) && !isEmpowered) {
                        isEmpowered = true;
                    }
                }
            }
        }

        cir.setReturnValue(((BlockState)super.getStateForPlacement(context).setValue(BasicBurnerBlock.EMPOWERED, isEmpowered)).setValue(BasicBurnerBlock.DUNSWE, dunswe));
    }

    public MixinBasicBurnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
