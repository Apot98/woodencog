package net.chauvedev.woodencog.mixin;

import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessing;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import net.chauvedev.woodencog.config.WoodenCogCommonConfigs;
import net.chauvedev.woodencog.content.fan_types.WoodenCogFanProcessingTypes;
import net.chauvedev.woodencog.datapack.DataPackRegistries;
import net.chauvedev.woodencog.utils.ModTags;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.FoodTraits;
import net.dries007.tfc.common.component.heat.Heat;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeat;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(value = FanProcessing.class, remap = false)
public class MixinFanProcessing {

    @Unique
    private static boolean applyTemp(ItemStack inputStack, IHeat cap, FanProcessingType type, RegistryAccess registryAccess) {
        if(HeatCapability.get(inputStack) == null) return false;
        float targetTemp = 0.0f;
        boolean heatApplied = false;
        if (type.equals(WoodenCogFanProcessingTypes.HEATING_SEETHING_TYPE)) {
            targetTemp = WoodenCogCommonConfigs.BLAZE_BURNER_SEETHING.get().floatValue();
            if(!DataPackRegistries.isInTempBlacklist(inputStack, registryAccess) && cap.getTemperature() < targetTemp) {
                HeatCapability.addTemp(cap, targetTemp);
                heatApplied = true;
            }
        } else if(type.equals(AllFanProcessingTypes.BLASTING)) {
            targetTemp = WoodenCogCommonConfigs.BLAZE_BURNER_KINDLED.get().floatValue();
            if(!DataPackRegistries.isInTempBlacklist(inputStack, registryAccess) && cap.getTemperature() < targetTemp) {
                HeatCapability.addTemp(cap, targetTemp);
                heatApplied = true;
            }
        } else if (type.equals(WoodenCogFanProcessingTypes.HEATING_FADING_TYPE)) {
            targetTemp = WoodenCogCommonConfigs.BLAZE_BURNER_FADING.get().floatValue();
            if(!DataPackRegistries.isInTempBlacklist(inputStack, registryAccess) && cap.getTemperature() < targetTemp) {
                HeatCapability.addTemp(cap, targetTemp);
                heatApplied = true;
            }
        } else if (type.equals(AllFanProcessingTypes.SMOKING)) {
            targetTemp = WoodenCogCommonConfigs.BLAZE_BURNER_SMOULDERING.get().floatValue();
            if(!DataPackRegistries.isInTempBlacklist(inputStack, registryAccess) && cap.getTemperature() < targetTemp) {
                HeatCapability.addTemp(cap, targetTemp);
                heatApplied = true;
            }
        } else if (type.equals(AllFanProcessingTypes.SPLASHING)) {
            if(!DataPackRegistries.isInTempBlacklist(inputStack, registryAccess) && cap.getTemperature() > targetTemp) {
                cap.setTemperature(HeatCapability.adjustTempTowards(cap.getTemperature(), 0, 8));
                heatApplied = true;
            }
        } else {
            cap.setTemperature(cap.getTemperature() - 2F);
            if(cap.getTemperature() <= 0F) {
                cap.setTemperature(0F);
            }
            heatApplied = true;
        }
        return heatApplied;
    }

    @Unique
    private static ItemStack applyTFCHeatingRecipe(ItemStack inputStack, IHeat cap){
        HeatingRecipe recipe = HeatingRecipe.getRecipe(inputStack);

        if (recipe!=null){
            if (recipe.isValidTemperature(cap.getTemperature())) {
                ItemStack output = recipe.assembleItem(inputStack);
                if(output.isEmpty()) {
                    FluidStack fluidStack = recipe.assembleFluid(inputStack);
                    if (!fluidStack.isEmpty()) {
                        return ItemStack.EMPTY; //Melting recipe input is distorted
                    }
                    else {
                        return inputStack; //No output for this recipe do not change input
                    }
                }

                if(FoodCapability.has(output)) FoodCapability.applyTrait(output, FoodTraits.WOOD_GRILLED);

                output.setCount(inputStack.getCount());
                return output;
            }
        }
        return inputStack;
    }

    @Inject(
            method = {"applyProcessing(Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;Lnet/minecraft/world/level/Level;Lcom/simibubi/create/content/kinetics/fan/processing/FanProcessingType;)Lcom/simibubi/create/content/kinetics/belt/behaviour/TransportedItemStackHandlerBehaviour$TransportedResult;"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private static void applyProcessing(TransportedItemStack transported, Level world, FanProcessingType type,CallbackInfoReturnable<TransportedItemStackHandlerBehaviour.TransportedResult> cir) {

        ItemStack inputStack = transported.stack;
        IHeat cap = HeatCapability.get(inputStack);

        if(cap != null && WoodenCogCommonConfigs.HANDLE_TEMPERATURE.get()){
            boolean heatApplied = MixinFanProcessing.applyTemp(inputStack, cap, type, world.registryAccess());
            ItemStack result = MixinFanProcessing.applyTFCHeatingRecipe(inputStack, cap);

            if(result.equals(inputStack)){
                cir.setReturnValue(TransportedItemStackHandlerBehaviour.TransportedResult.doNothing());
                return;
            }

            if(result == ItemStack.EMPTY){
                cir.setReturnValue(TransportedItemStackHandlerBehaviour.TransportedResult.removeItem());
                return;
            }else{
                TransportedItemStack newTransportedStack = transported.getSimilar();
                newTransportedStack.stack = result;
                cir.setReturnValue(TransportedItemStackHandlerBehaviour.TransportedResult.convertTo(newTransportedStack));
                return;
            } /*
            if (heatApplied) {
                cir.setReturnValue(TransportedItemStackHandlerBehaviour.TransportedResult.doNothing());
                //cir.cancel()
            }
            */
        }
    }


    @Inject(
            method = {"applyProcessing(Lnet/minecraft/world/entity/item/ItemEntity;Lcom/simibubi/create/content/kinetics/fan/processing/FanProcessingType;)Z"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private static void applyProcessing(ItemEntity entity, FanProcessingType type, CallbackInfoReturnable<Boolean> cir) {
        ItemStack inputStack = entity.getItem();
        IHeat cap = HeatCapability.get(inputStack);

        if(cap != null && WoodenCogCommonConfigs.HANDLE_TEMPERATURE.get()){
            boolean heatApplied = MixinFanProcessing.applyTemp(inputStack, cap, type, entity.level().registryAccess());
            ItemStack result = MixinFanProcessing.applyTFCHeatingRecipe(inputStack, cap);

            if(result.equals(inputStack)){
                cir.setReturnValue(false);
                return;
            }
            if (result == ItemStack.EMPTY){
                entity.kill();
            }else{
                entity.setItem(result);
            }
            cir.setReturnValue(true);
            //cir.cancel();
        }
    }

    @Inject(
            method = {"applyProcessing(Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;Lnet/minecraft/world/level/Level;Lcom/simibubi/create/content/kinetics/fan/processing/FanProcessingType;)Lcom/simibubi/create/content/kinetics/belt/behaviour/TransportedItemStackHandlerBehaviour$TransportedResult;"},
            at = {@At(value = "INVOKE_ASSIGN", target = "Lcom/simibubi/create/content/kinetics/fan/processing/FanProcessingType;process(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;)Ljava/util/List;", shift = At.Shift.BY, by = 1)},
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void cancelDiscard(TransportedItemStack transported, Level world, FanProcessingType type, CallbackInfoReturnable<TransportedItemStackHandlerBehaviour.TransportedResult> cir, TransportedItemStackHandlerBehaviour.TransportedResult ignore, List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            ItemStack inputStack = transported.stack;
            if (inputStack.has(DataComponents.FIRE_RESISTANT) || HeatCapability.has(inputStack) || ((ModTags.Items.UNBURNABLE != null) && inputStack.is(ModTags.Items.UNBURNABLE))) {
                cir.setReturnValue(TransportedItemStackHandlerBehaviour.TransportedResult.doNothing());
            }
        }
    }

    @Inject(
            method = "applyProcessing(Lnet/minecraft/world/entity/item/ItemEntity;Lcom/simibubi/create/content/kinetics/fan/processing/FanProcessingType;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V"),
            cancellable = true
    )
    private static void cancelDiscard(ItemEntity entity, FanProcessingType type, CallbackInfoReturnable<Boolean> cir){
        if (type.equals(AllFanProcessingTypes.BLASTING) || type.equals(WoodenCogFanProcessingTypes.HEATING_SEETHING_TYPE)) {
            ItemStack stack = entity.getItem();
            if (stack.has(DataComponents.FIRE_RESISTANT) || (HeatCapability.has(stack)) || ((ModTags.Items.UNBURNABLE != null) && stack.is(ModTags.Items.UNBURNABLE))) {
                cir.setReturnValue(false);
            }
        }
    }

}
