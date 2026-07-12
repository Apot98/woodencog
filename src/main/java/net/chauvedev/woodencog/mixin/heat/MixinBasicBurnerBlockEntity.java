package net.chauvedev.woodencog.mixin.heat;

import com.simibubi.create.content.kinetics.fan.EncasedFanBlock;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.chauvedev.woodencog.compat.Compat;
import net.chauvedev.woodencog.config.WoodenCogCommonConfigs;
import net.chauvedev.woodencog.compat.createlowheated.IMixinBasicBurnerBlockEntity;
import net.chauvedev.woodencog.mixin.blockEnitites.accessors.BlockEntityAccessor;
import net.chauvedev.woodencog.utils.CogUtil;
import net.chauvedev.woodencog.compat.createlowheated.IMixinBasicBurnerBlock;
import net.dries007.tfc.common.component.heat.Heat;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.util.data.Fuel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import zeh.createlowheated.content.processing.basicburner.BasicBurnerBlock;
import zeh.createlowheated.content.processing.basicburner.BasicBurnerBlockEntity;

import java.util.List;

import static net.chauvedev.woodencog.utils.CogUtil.max;
import static net.minecraft.util.Mth.*;

@Mixin(value = BasicBurnerBlockEntity.class, remap = false)
public abstract class MixinBasicBurnerBlockEntity extends SmartBlockEntity implements IMixinBasicBurnerBlockEntity {

    public MixinBasicBurnerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void mixinInit(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        this.WoodenCog$updateAllFanSpeed();
    }

    @Shadow
    public ItemStackHandler inputInv;

    @Shadow
    protected BasicBurnerBlockEntity.FuelType activeFuel;

    @Shadow
    protected int remainingBurnTime;

    @Unique
    protected Item WoodenCog$activeFuelItem = Items.AIR;

    @Unique
    private static final Item WoodenCog$EMPTY = Items.AIR;

    @Unique
    public ItemStack WoodenCog$getActiveFuelStack() {
        if (this.WoodenCog$activeFuelItem == null) {
            return ItemStack.EMPTY;
        }
        return this.WoodenCog$activeFuelItem.getDefaultInstance();
    }

    @Unique
    protected float WoodenCog$remainingBurnTimeFractional;

    @Unique
    public float WoodenCog$getRemainingBurnTimeFractional() {
        return this.WoodenCog$remainingBurnTimeFractional;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lzeh/createlowheated/content/processing/basicburner/BasicBurnerBlockEntity;tickFuel()V"), cancellable = true)
    public void mixinTickRemainingBurnTime(CallbackInfo ci) {
        this.tickFuel();
        if (this.remainingBurnTime > 0) {
            float rate = this.WoodenCog$getFuelConsumption();
            int rateInteger = (int) (floor(rate));
            float rateFractional = rate - (float) rateInteger;
            rateFractional = clamp(rateFractional, 0.0f, 1.0f);
            if (rateFractional > this.WoodenCog$remainingBurnTimeFractional) {
                this.remainingBurnTime -= (rateInteger + 1);
                this.WoodenCog$remainingBurnTimeFractional += (1.0f - rateFractional);
            } else {
                this.remainingBurnTime -= rateInteger;
                this.WoodenCog$remainingBurnTimeFractional -= rateFractional;
            }
        }
        else {
            this.activeFuel = BasicBurnerBlockEntity.FuelType.NONE;
            this.WoodenCog$activeFuelItem = WoodenCog$EMPTY;
            if (this.getLitFromBlock()) {
                this.level.setBlockAndUpdate(((BlockEntityAccessor)this).getWorldPosition(), (BlockState)this.getBlockState().setValue(BasicBurnerBlock.LIT, false));
                this.notifyUpdate();
            }
            this.updateBlockState();
        }

        if (this.remainingBurnTime < 0) { this.remainingBurnTime = 0; }

        if (this.activeFuel == BasicBurnerBlockEntity.FuelType.NORMAL) {
            this.updateBlockState();
        }

        ci.cancel();
    }

    @Unique
    protected float[] WoodenCog$fanSpeedArray = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};

    @Unique
    protected float WoodenCog$fanSpeed;

    @Unique
    public void WoodenCog$updateFanSpeed(float speed, Direction direction) {
        this.WoodenCog$fanSpeedArray[clamp(direction.ordinal(),0,5)] = abs(speed);
        if (speed >= this.WoodenCog$fanSpeed) {
            this.WoodenCog$fanSpeed = speed;
        } else {
            this.WoodenCog$fanSpeed = max(this.WoodenCog$fanSpeedArray);
        }
        //this.WoodenCog$fanSpeed = max(this.WoodenCog$fanSpeedArray);
        this.WoodenCog$updateInterpolationRatio();
    }

    @Unique
    public void WoodenCog$updateAllFanSpeed() {
        for (Direction direction : Direction.values()) {
            int ordinal = direction.ordinal();
            int mask = 0B100000 >> ordinal;
            if ((this.getBlockState().getValue(BasicBurnerBlock.DUNSWE) & mask) != 0) {
                if (ordinal < 6) {
                    Level level = ((BlockEntityAccessor) this).getLevel();
                    if (level != null /* && !level.isClientSide */) {
                        BlockEntity fanBE = level.getBlockEntity(((BlockEntityAccessor)this).getWorldPosition().relative(direction));
                        if (fanBE instanceof EncasedFanBlockEntity) {
                            Direction fanFacing = fanBE.getBlockState().getValue(EncasedFanBlock.FACING);
                            if (fanFacing.getOpposite() == direction) {
                                this.WoodenCog$fanSpeedArray[ordinal] = abs(((EncasedFanBlockEntity) fanBE).getSpeed());
                            }
                        }
                    }
                }
            }
        }
        this.WoodenCog$fanSpeed = max(this.WoodenCog$fanSpeedArray);
        this.WoodenCog$updateInterpolationRatio();
    }

    @Unique
    public float WoodenCog$getTemperature() {
        Fuel fuel = Fuel.get(this.WoodenCog$getActiveFuelStack());
        if (fuel != null) {
            float temp = fuel.temperature();
            temp *= this.WoodenCog$getTempMultiplier();
            return temp;
        }
        return 0.0f;
    }

    @Unique
    public float WoodenCog$getTempMultiplier() {
        float maxTempMult = WoodenCogCommonConfigs.BASIC_BURNER_MAX_TEMP_MULT.get();
        float minTempMult = WoodenCogCommonConfigs.BASIC_BURNER_MIN_TEMP_MULT.get();
        return (((maxTempMult - minTempMult) * this.WoodenCog$getInterpolationRatio()) + minTempMult);
    }

    @Unique
    public float WoodenCog$getFuelConsumption() {
        float maxFuelRate = WoodenCogCommonConfigs.BASIC_BURNER_MAX_FUEL_RATE.get();
        float minFuelRate = WoodenCogCommonConfigs.BASIC_BURNER_MIN_FUEL_RATE.get();
        return (((maxFuelRate - minFuelRate) * this.WoodenCog$getInterpolationRatio()) + minFuelRate);
    }

    @Unique
    public void WoodenCog$updateInterpolationRatio() {
        float maxMultSpeed = WoodenCogCommonConfigs.BASIC_BURNER_MAX_FAN_SPEED.get();
        float minMultSpeed = WoodenCogCommonConfigs.BASIC_BURNER_MIN_FAN_SPEED.get();
        float target = maxMultSpeed;
        float speed = this.WoodenCog$fanSpeed;
        if (speed < minMultSpeed) {
            this.WoodenCog$setInterpolationRatio(0.0f);
            return;
        }
        target *= 0.8409f;
        if (speed >= target) {
            this.WoodenCog$setInterpolationRatio(1.0f);
            return;
        }
        int i = 0;
        int j = 0;
        while (target > minMultSpeed) {
            if (!(target > speed)) {j += 1;}
            i += 1;
            target *= 0.8409f; // 2 ^ -0.25
        }
        this.WoodenCog$setInterpolationRatio((j + 1)/(float)(i + 1));
    }

    @Unique
    private float WoodenCog$interpolationRatio;

    @Unique
    public void WoodenCog$setInterpolationRatio(float ratio) {
        this.WoodenCog$interpolationRatio = ratio;
    }

    @Unique
    public float WoodenCog$getInterpolationRatio() {
        return this.WoodenCog$interpolationRatio;
    }

    @Inject(method = "write", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;write(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Z)V"))
    public void writeAdditional(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        compound.putFloat("fanSpeed", this.WoodenCog$fanSpeed);
        compound.putFloat("InterpRatio", this.WoodenCog$interpolationRatio);
        compound.putFloat("BurnTimeFractional", this.WoodenCog$remainingBurnTimeFractional);
        float[] array = this.WoodenCog$fanSpeedArray;
        ListTag list = new ListTag();
        for (int i = 0; i < 6; i++) {
            list.add(FloatTag.valueOf(array[i]));
        }
        compound.put("fanSpeeds", list);
        if (this.WoodenCog$activeFuelItem == null) {
            compound.putString("activeFuelItem", "minecraft:air");
        }
        else {
            compound.putString("activeFuelItem", this.WoodenCog$activeFuelItem.toString());
        }
    }

    @Inject(method = "read", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Z)V"))
    protected void readAdditional(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        this.WoodenCog$fanSpeed = compound.getFloat("fanSpeed");
        this.WoodenCog$interpolationRatio = compound.getFloat("InterpRatio");
        this.WoodenCog$remainingBurnTimeFractional = compound.getFloat("BurnTimeFractional");
        ListTag list = compound.getList("fanSpeeds", Tag.TAG_FLOAT);
        float[] array = new float[6];
        for (int i = 0; i < 6; i++) {
            array[i] = list.getFloat(i);
        }
        this.WoodenCog$fanSpeedArray = array;
        this.WoodenCog$activeFuelItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(compound.getString("activeFuelItem")));
    }

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

    @Shadow public abstract boolean getLitFromBlock();

    @Shadow public abstract void tickFuel();

    @Shadow public abstract int getRemainingBurnTime();

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
                if ((this.WoodenCog$fanSpeed == 0) && (this.getBlockState().getValue(BasicBurnerBlock.DUNSWE) != 0)) {
                    this.WoodenCog$updateAllFanSpeed();
                }
                this.activeFuel = newFuel;
                this.WoodenCog$activeFuelItem = itemStack.getItem();
                this.remainingBurnTime = newBurnTime;
                BlazeBurnerBlock.HeatLevel prev = this.getHeatLevelFromBlock();
                this.playSound();
                this.updateBlockState();
                if (prev != this.getHeatLevelFromBlock()) {
                    super.level.playSound((Player)null, ((BlockEntityAccessor)this).getWorldPosition(), SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.125F + this.level.random.nextFloat() * 0.125F, 1.15F - this.level.random.nextFloat() * 0.25F);
                }
                cir.setReturnValue(true);
            }
        }
    }

    /**
     * @author Apot2s
     * @reason get block heat level according to temperature-heat level mapping
     */
    @Inject(method = "getHeatLevel", at = @At(value = "HEAD"), cancellable = true)
    protected void getHeatLevel(CallbackInfoReturnable<BlazeBurnerBlock.HeatLevel> cir) {
        BlazeBurnerBlock.HeatLevel level = BlazeBurnerBlock.HeatLevel.NONE;
        if (!this.getLitFromBlock()) {
            cir.setReturnValue(level);
        } else {
            cir.setReturnValue(CogUtil.tempToHeatLevel(this.WoodenCog$getTemperature()));
        }
        cir.cancel();
    }

    @Inject(method = "addToGoggleTooltip",
            at = @At(value = "FIELD", target = "Lzeh/createlowheated/content/processing/basicburner/BasicBurnerBlockEntity;remainingBurnTime:I"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    public void WoodenCog$mixinAddToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir, boolean isEmpty, ItemStack stackInSlot) {
        float temp = this.WoodenCog$getTemperature();
        if(this.remainingBurnTime > 0) {
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
            float tempMult = this.WoodenCog$getTempMultiplier();
            CreateLang.text("")
                    .add(Component.literal(temp + " ºC ")).style(color)
                    .add(displayName)
                    .add(CreateLang.text(tempMult > 1.0f ? (" (x" + String.format("%.2f", tempMult) + ")") : ""))
                    .forGoggles(tooltip, 0);
        }

        if (!isEmpty) {
            CreateLang.translate("addon.basicburner.burner_contents", new Object[0]).forGoggles(tooltip);
            CreateLang.text("")
                    .add(Component.translatable(stackInSlot.getDescriptionId()).withStyle(ChatFormatting.GRAY))
                    .add(CreateLang.text(" x" + stackInSlot.getCount()).style(ChatFormatting.GREEN))
                    .forGoggles(tooltip, 1);
        }
        cir.setReturnValue(!isEmpty);
    }

    @Inject(method = "setBlockHeat", at = @At(value = "INVOKE", target = "Lzeh/createlowheated/content/processing/basicburner/BasicBurnerBlockEntity;notifyUpdate()V"))
    protected void MixinSetBlockHeat(BlazeBurnerBlock.HeatLevel heat, CallbackInfo ci) {
        this.level.setBlockAndUpdate(((BlockEntityAccessor)this).getWorldPosition(), (BlockState)this.getBlockState().setValue(IMixinBasicBurnerBlock.BLAZE_HEAT_LEVEL, heat));
    }
}
