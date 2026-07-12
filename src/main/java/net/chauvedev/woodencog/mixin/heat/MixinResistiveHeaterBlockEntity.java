package net.chauvedev.woodencog.mixin.heat;

import com.george_vi.electroenergetics.content.resistive_heater.ResistiveHeaterBlockEntity;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import net.chauvedev.woodencog.compat.Compat;
import net.chauvedev.woodencog.mixin.blockEnitites.accessors.BlockEntityAccessor;
import net.chauvedev.woodencog.utils.CogUtil;
import net.dries007.tfc.common.component.heat.Heat;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ResistiveHeaterBlockEntity.class, remap = false)
public abstract class MixinResistiveHeaterBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    public MixinResistiveHeaterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void tickProvideHeatTo(CallbackInfo ci) {
        Level level = ((BlockEntityAccessor) this).getLevel();
        float temp = this.WoodenCog$getTFCTemperature();
        HeatCapability.provideHeatTo(level, ((BlockEntityAccessor) this).getWorldPosition().above(), Direction.DOWN, temp);
    }

    @Inject(method = "calculateHeatLevel", at = @At(value = "HEAD"), cancellable = true)
    public void mixinCalculateHeatLevel(float heat, CallbackInfoReturnable<BlazeBurnerBlock.HeatLevel> cir) {
        float temp = this.WoodenCog$getTFCTemperature();
        cir.setReturnValue(CogUtil.tempToHeatLevel(temp));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        float temp = this.WoodenCog$getTFCTemperature();
        if(temp > 0) {
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
            return true;
        }
        return false;
    }

    @Unique
    public float WoodenCog$getTFCTemperature() {
        return Compat.CEE_INSTANCE.getTFCTemperatureOf((ResistiveHeaterBlockEntity)(Object)this);
    }

}
