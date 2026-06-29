package net.chauvedev.woodencog.mixin.heat;

import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.chauvedev.woodencog.compat.Compat;
import net.chauvedev.woodencog.mixin.blockEnitites.accessors.BlockEntityAccessor;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.util.data.Fuel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import zeh.createlowheated.common.Configuration;
import zeh.createlowheated.content.processing.basicburner.BasicBurnerBlock;
import zeh.createlowheated.content.processing.basicburner.BasicBurnerBlockEntity;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(value = BasicBurnerBlockEntity.class, remap = false)
public class MixinBasicBurnerBlockEntity extends SmartBlockEntity {

    public MixinBasicBurnerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow
    public ItemStackHandler inputInv;

    @Shadow
    protected BasicBurnerBlockEntity.FuelType activeFuel;

    @Shadow
    protected int remainingBurnTime;

    @Shadow
    public BlazeBurnerBlock.HeatLevel getHeatLevelFromBlock() {
        return BlazeBurnerBlock.HeatLevel.NONE;
    }

    @Shadow public void updateBlockState() {}

    @Shadow
    protected void playSound() {}

    @Shadow
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Inject(method = "tickFuel",at = @At("HEAD"))
    void tick(CallbackInfo ci){
        Level level = ((BlockEntityAccessor) this).getLevel();
        float temp = Compat.CLH_INSTANCE.getTFCTemperatureOf((BasicBurnerBlockEntity)(Object)this);
        HeatCapability.provideHeatTo(level, ((BlockEntityAccessor) this).getWorldPosition().above(), Direction.DOWN, temp);
    }

    @Inject(method = "isFuelValid", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    void isFuelTFCVaild(ItemStack stack, CallbackInfoReturnable<Boolean> cir, int burnTime, boolean tagged){
        if (!stack.isEmpty()) {
            Fuel tfcfuel = Fuel.get(stack);
            if (tfcfuel != null) {
                cir.setReturnValue(tfcfuel.duration() > 0 && tagged && this.inputInv.isItemValid(0, stack));
                //cir.cancel();
            }
        }
    }
    //at = @At(value = "INVOKE_ASSIGN",
    //    target ="Lnet/minecraft/world/item/ItemStack;getBurnTime(Lnet/minecraft/world/item/crafting/RecipeType;)I")
    @Inject(method = "tryUpdateFuel", at = @At(value = "INVOKE",
            target ="Lnet/minecraft/world/item/ItemStack;getBurnTime(Lnet/minecraft/world/item/crafting/RecipeType;)I"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    void tryUpdateFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<Boolean> cir, BasicBurnerBlockEntity.FuelType newFuel) {
        Fuel tfcFuel = Fuel.get(itemStack);
        if (tfcFuel != null && tfcFuel.duration() > 0) {
            int newBurnTime = tfcFuel.duration();
            if (newFuel == this.activeFuel) {
                if (this.remainingBurnTime <= 400) {
                    newBurnTime += this.remainingBurnTime;
                } else {
                    if (!forceOverflow || newFuel != BasicBurnerBlockEntity.FuelType.NORMAL) {
                        cir.setReturnValue(false);
                        return;
                    }
                    if (this.remainingBurnTime + newBurnTime >= 4000) {
                        cir.setReturnValue(false);
                        return;
                    }
                    newBurnTime = Math.min(this.remainingBurnTime + newBurnTime, 4000);
                }
            }

            if (simulate) {
                cir.setReturnValue(true);
            } else {
                this.activeFuel = newFuel;
                this.remainingBurnTime = newBurnTime;
                BlazeBurnerBlock.HeatLevel prev = this.getHeatLevelFromBlock();
                this.playSound();
                this.updateBlockState();
                if (prev != this.getHeatLevelFromBlock()) {
                    super.level.playSound((Player)null, this.worldPosition, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.125F + this.level.random.nextFloat() * 0.125F, 1.15F - this.level.random.nextFloat() * 0.25F);
                }
                cir.setReturnValue(true);
            }
        }
    }
}
