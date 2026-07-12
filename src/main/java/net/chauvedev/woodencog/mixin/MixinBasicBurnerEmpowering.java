package net.chauvedev.woodencog.mixin;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import net.chauvedev.woodencog.compat.createlowheated.IMixinBasicBurnerBlockEntity;
import net.chauvedev.woodencog.mixin.blockEnitites.accessors.BlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zeh.createlowheated.common.Configuration;
import zeh.createlowheated.content.processing.basicburner.BasicBurnerBlockEntity;

@Mixin(
        value = {EncasedFanBlockEntity.class},
        remap = false
)
public abstract class MixinBasicBurnerEmpowering extends KineticBlockEntity {
    @Shadow
    public abstract Direction getAirflowOriginSide();

    public MixinBasicBurnerEmpowering(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Inject(
            method = {"onSpeedChanged"},
            at = @At("HEAD")
    )
    protected void updateBasicBurnerOnSpeedChange(float prevSpeed, CallbackInfo ci) {
        this.WoodenCog$updateBasicBurner(this.getSpeed());
    }

    @Inject(
            method = {"remove"},
            at = @At("HEAD")
    )
    protected void updateBasicBurnerOnRemove(CallbackInfo ci) {
        this.WoodenCog$updateBasicBurner(this.getSpeed());
    }

    @Unique
    public void WoodenCog$updateBasicBurner(float speed) {
        boolean horizontalOnly = Configuration.FAN_HORIZONTAL_ONLY.get();
        Direction fanFacingDir = this.getAirflowOriginSide();
        if (!horizontalOnly || fanFacingDir.getAxis().isHorizontal()) {
            BlockEntity poweredBurner = this.level.getBlockEntity(((BlockEntityAccessor)this).getWorldPosition().relative(fanFacingDir));
            if (poweredBurner instanceof BasicBurnerBlockEntity) {
                BasicBurnerBlockEntity burnerBE = (BasicBurnerBlockEntity) poweredBurner;
                ((IMixinBasicBurnerBlockEntity)burnerBE).WoodenCog$updateFanSpeed(speed, fanFacingDir.getOpposite());
            }
        }
    }
}
