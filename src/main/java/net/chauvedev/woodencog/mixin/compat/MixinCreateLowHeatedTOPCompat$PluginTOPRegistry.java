package net.chauvedev.woodencog.mixin.compat;

import net.chauvedev.woodencog.compat.createlowheated.IMixinBasicBurnerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zeh.createlowheated.compat.TOPCompat;
import zeh.createlowheated.content.processing.basicburner.BasicBurnerBlockEntity;

import static net.minecraft.util.Mth.floor;

@Mixin(value = TOPCompat.PluginTOPRegistry.class, remap = false)
public abstract class MixinCreateLowHeatedTOPCompat$PluginTOPRegistry {

    @Inject(method = "litTime", at = @At(value = "RETURN"), cancellable = true)
    public void mixinLitTime(BasicBurnerBlockEntity entity, CallbackInfoReturnable<Integer> cir) {
        IMixinBasicBurnerBlockEntity mixinBE = (IMixinBasicBurnerBlockEntity) entity;
        if (mixinBE != null) {
            cir.setReturnValue(floor((entity.getRemainingBurnTime() + mixinBE.WoodenCog$getRemainingBurnTimeFractional()) / mixinBE.WoodenCog$getFuelConsumption()) / 20);
        }
    }

}
