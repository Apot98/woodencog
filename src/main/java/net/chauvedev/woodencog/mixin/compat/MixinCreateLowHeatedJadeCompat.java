package net.chauvedev.woodencog.mixin.compat;

import net.chauvedev.woodencog.compat.createlowheated.IMixinBasicBurnerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaPlugin;
import zeh.createlowheated.compat.JadeCompat;
import zeh.createlowheated.content.processing.basicburner.BasicBurnerBlockEntity;

import static net.minecraft.util.Mth.floor;

@Mixin(value = JadeCompat.class, remap = false)
public abstract class MixinCreateLowHeatedJadeCompat implements IWailaPlugin, IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Inject(method = "litTicks", at = @At(value = "RETURN"), cancellable = true)
    public void mixinLitTicks(BasicBurnerBlockEntity entity, CallbackInfoReturnable<Integer> cir) {
        IMixinBasicBurnerBlockEntity mixinBE = (IMixinBasicBurnerBlockEntity)entity;
        if (mixinBE != null) {
            cir.setReturnValue(floor((entity.getRemainingBurnTime() + mixinBE.WoodenCog$getRemainingBurnTimeFractional()) / mixinBE.WoodenCog$getFuelConsumption()));
        }
    }

}
