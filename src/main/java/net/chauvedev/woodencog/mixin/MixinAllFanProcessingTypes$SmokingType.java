package net.chauvedev.woodencog.mixin;

import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.chauvedev.woodencog.compat.Compat;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zeh.createlowheated.content.processing.basicburner.BasicBurnerBlock;

@Mixin(
        value = AllFanProcessingTypes.SmokingType.class,
        remap = false
)
public class MixinAllFanProcessingTypes$SmokingType {

    @Inject(
            method = {"canProcess(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;)Z"},
            at = {@At(value = "RETURN")},
            cancellable = true
    )
    public void canProcess(ItemStack stack, Level level, CallbackInfoReturnable<Boolean> cir) {
        IHeat cap = HeatCapability.get(stack);
        if (cap != null) {
            cir.setReturnValue(true);
        }
        //else cir.setReturnValue(cir.getReturnValue());
    }
    /*
    @Inject(
            method = "isValidAt",
            at = @At(value = "RETURN"),
            cancellable = true
    )
    public void mixinIsValidAt(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!(cir.getReturnValue())) {
            BlockState blockState = level.getBlockState(pos);
            if (blockState.is(BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Compat.CLH_MOD_ID, "basic_burner")))) {
                cir.setReturnValue(blockState.getValue(BasicBurnerBlock.HEAT_LEVEL) == (BlazeBurnerBlock.HeatLevel.SMOULDERING));
            }
        }
    }
     */

}