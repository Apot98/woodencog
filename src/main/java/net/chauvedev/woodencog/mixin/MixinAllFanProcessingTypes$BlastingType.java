package net.chauvedev.woodencog.mixin;

import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
        value = AllFanProcessingTypes.BlastingType.class,
        remap = false
)
public class MixinAllFanProcessingTypes$BlastingType {

    @Inject(
            method = {"canProcess(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;)Z"},
            at = {@At(value = "RETURN", ordinal = 2)},
            cancellable = true
    )
    public void canProcess(ItemStack stack, Level level, CallbackInfoReturnable<Boolean> cir) {
        IHeat cap = HeatCapability.get(stack);
        if (cap != null) {
            cir.setReturnValue(true);
        }
        //else cir.setReturnValue(cir.getReturnValue());
    }

}
