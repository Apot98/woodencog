package net.chauvedev.woodencog.mixin.heat;

import com.jesz.createdieselgenerators.content.burner.BurnerBlock;
import com.jesz.createdieselgenerators.content.burner.BurnerBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.CreateLang;
import net.chauvedev.woodencog.compat.Compat;
import net.chauvedev.woodencog.compat.createdieselgenerators.CDGIntegrationImpl;
import net.chauvedev.woodencog.mixin.blockEnitites.accessors.BlockEntityAccessor;
import net.chauvedev.woodencog.utils.CogUtil;
import net.dries007.tfc.common.component.heat.Heat;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

import static net.minecraft.util.Mth.floor;

@Mixin(value = BurnerBlockEntity.class, remap = false)
public abstract class MixinDieselBurnerBlockEntity extends KineticBlockEntity {

    @Shadow
    public float heat = -1.0F;

    @Shadow
    public int redstoneOutput = 0;

    @Shadow
    SmartFluidTank tank = new SmartFluidTank(100, (f) -> {});

    @Shadow
    int tick;

    @Shadow
    float multiplier;

    @Shadow
    boolean ignited;

    @Shadow
    int ignitionTries;

    @Unique
    float drainedFractional;

    @Shadow public abstract BlazeBurnerBlock.HeatLevel calculateHeatLevel(float heat);

    public MixinDieselBurnerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lcom/jesz/createdieselgenerators/content/burner/BurnerBlockEntity;valveState:F", ordinal = 4), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void mixinTick(CallbackInfo ci, float valveOrRedstoneState, boolean containsValidFuel) {
        if (valveOrRedstoneState == 0.0F || !containsValidFuel) {
            this.heat = -1.0F;
            if (!((BlockEntityAccessor) this).getLevel().isClientSide) {
                if (this.ignited) {
                    ((BlockEntityAccessor) this).getLevel().playSound((Player) null, ((BlockEntityAccessor)this).getWorldPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3F, this.level.getRandom().nextFloat() * 0.4F + 0.7F);
                }
                this.ignited = false;
                this.ignitionTries = 0;
            }
        }

        if (containsValidFuel && valveOrRedstoneState != 0.0F) {
            this.heat = (valveOrRedstoneState + 1.0F) * this.multiplier;
            if (((BlockEntityAccessor) this).getLevel().isClientSide) {
                return;
            }

            if (this.tick % 5 == 0 && !this.ignited) {
                ((BlockEntityAccessor) this).getLevel().playSound((Player)null, ((BlockEntityAccessor) this).getWorldPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.5F, this.level.getRandom().nextFloat() * 0.4F + 0.7F);
                ++this.ignitionTries;
                if (this.ignitionTries > 4) {
                    this.ignited = true;
                }
            }

            float totalDrain = (valveOrRedstoneState / 10.0f) + this.drainedFractional;
            int fuelDrain = floor(totalDrain);
            this.drainedFractional = totalDrain - fuelDrain;
            if (fuelDrain > 0) {
                if (this.tank.getFluid().getAmount() < fuelDrain) {
                    if (this.ignitionTries > 4) {
                        this.ignitionTries = 4;
                    }
                    this.ignited = false;
                    this.drainedFractional = 0.0f;
                } else {
                    this.tank.drain(fuelDrain, IFluidHandler.FluidAction.EXECUTE);
                    this.sendData();
                    this.setChanged();
                }
            }
        }

        if (!((BlockEntityAccessor) this).getLevel().isClientSide) {
            BlazeBurnerBlock.HeatLevel newHeat = this.calculateHeatLevel(this.heat);
            if (this.getBlockState().getValue(BurnerBlock.HEAT_LEVEL) != newHeat) {
                ((BlockEntityAccessor) this).getLevel().setBlockAndUpdate(((BlockEntityAccessor) this).getWorldPosition(), (BlockState)((BlockState)this.getBlockState().setValue(BurnerBlock.HEAT_LEVEL, newHeat)).setValue(BurnerBlock.LIT, this.heat > 0.0F));
                this.notifyUpdate();
            }

            int newRedstoneOutput = (int) Mth.clamp(this.heat * 6.0F, 0.0F, 15.0F);
            if (this.redstoneOutput != newRedstoneOutput) {
                this.redstoneOutput = newRedstoneOutput;
                this.setChanged();
            }
        }

        HeatCapability.provideHeatTo(((BlockEntityAccessor) this).getLevel(), ((BlockEntityAccessor) this).getWorldPosition().above(), Direction.DOWN, this.WoodenCog$getTemperature());

        ci.cancel();
    }

    @Unique
    public float WoodenCog$getTemperature() {
        return Compat.CDG_INSTANCE.getTFCTemperatureOf((BurnerBlockEntity)(Object)this);
    }

    @Inject(method = "calculateHeatLevel", at = @At(value = "HEAD"), cancellable = true)
    public void mixinCalculateHeatLevel(float heat, CallbackInfoReturnable<BlazeBurnerBlock.HeatLevel> cir) {
        cir.setReturnValue(CogUtil.tempToHeatLevel(this.WoodenCog$getTemperature()));
    }

    @Inject(method = "addToGoggleTooltip", at = @At(value = "RETURN"), cancellable = true)
    public void mixinAddToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        if (this.heat > 0.0f) {
            float temp = WoodenCog$getTemperature();
            ChatFormatting color = ChatFormatting.GRAY;
            Component displayName = Component.literal("");
            if (temp > Heat.BRILLIANT_WHITE.getMax()) {
                color = Heat.BRILLIANT_WHITE.getColor();
                displayName = Component.literal(Heat.BRILLIANT_WHITE.name());
            } else {
                for (Heat heat : Heat.values()) {
                    if (temp > heat.getMin() && temp <= heat.getMax()) {
                        color = heat.getColor();
                        displayName = Component.literal(heat.name());
                        break;
                    }
                }
            }

            CreateLang.text("")
                    .add(Component.literal(temp + " ºC ")).style(color)
                    .add(displayName)
                    .forGoggles(tooltip, 0);

            cir.setReturnValue(true);
        }
    }

    @Inject(method = "write", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;write(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Z)V"))
    public void writeAdditional(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        tag.putFloat("drainedFractional", this.drainedFractional);
    }

    @Inject(method = "read", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Z)V"))
    public void readAdditional(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        this.drainedFractional = tag.getFloat("drainedFractional");
    }
}
