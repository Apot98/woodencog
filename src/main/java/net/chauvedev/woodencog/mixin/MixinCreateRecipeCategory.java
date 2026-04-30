package net.chauvedev.woodencog.mixin;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.dries007.tfc.util.data.FluidHeat;
import net.dries007.tfc.util.Metal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CreateRecipeCategory.class, remap = false)
public abstract class MixinCreateRecipeCategory {
    public MixinCreateRecipeCategory() {
    }

    @Inject(method = {"getResultItem(Lnet/minecraft/world/item/crafting/Recipe;)Lnet/minecraft/world/item/ItemStack;"}, at = {@At(value = "RETURN")}, cancellable = true, remap = false)
    private static void woodencog$getResultItem(Recipe<?> recipe, CallbackInfoReturnable<ItemStack> cir) {
        var output = cir.getReturnValue();
        if (!output.isEmpty()) {
            if (recipe instanceof FillingRecipe fillingRecipe /*&& fillingRecipe.getId().toString().startsWith("woodencog:advanced_filling/")*/) {
                /*
                var fluidStack = fillingRecipe.getRequiredFluid().getFluids()[0];
                var metal = Metal.get(fluidStack.getFluid());
                if (metal != null) {
                    var mold = ItemStackProvider.of(output).getSingleStack(ItemStack.EMPTY);
                    if (HeatCapability.has(mold) && (HeatCapability.get(mold) != null)) {
                        HeatCapability.setTemperature(mold, FluidHeat.getOrUnknown(fluidStack).meltTemperature());
                    }
                    var cap = mold.getCapability(Capabilities.FluidHandler.ITEM);
                    if (cap != null) {
                        if (cap.getFluidInTank(0).getAmount() < fluidStack.getAmount()) {
                            cap.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                        }
                    };

                 */

                    var mold = ItemStackProvider.of(output).getSingleStack(ItemStack.EMPTY);
                    cir.setReturnValue(mold);
                //}
            }
        }
    }
}
