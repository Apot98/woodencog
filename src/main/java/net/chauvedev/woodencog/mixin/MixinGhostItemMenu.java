package net.chauvedev.woodencog.mixin;

import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.IFood;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = GhostItemMenu.class,remap = false)
public class MixinGhostItemMenu {

    @Redirect(method = "clicked",at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/items/ItemStackHandler;setStackInSlot(ILnet/minecraft/world/item/ItemStack;)V"))
    public void setStackInSlot(ItemStackHandler instance, int slot, ItemStack stack){
        if(FoodCapability.has(stack)){
            FoodCapability.setCreationDate(stack, -2L);
        }
        instance.setStackInSlot(slot, stack);
    }

    @Redirect(method = "quickMoveStack",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack quickMoveStackCopy(ItemStack instance){
        ItemStack stack = instance.copy();
        if(FoodCapability.has(stack)){
            FoodCapability.setCreationDate(stack, -2L);
        }
        return stack;
    }
}
