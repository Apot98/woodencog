package net.chauvedev.woodencog.content.fan_types;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.damageTypes.CreateDamageSources;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import net.createmod.catnip.theme.Color;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class HeatingFadingType implements FanProcessingType {
    public HeatingFadingType () {
    }

    @Override
    public int getPriority() {
        return 2400;
    }

    @Override
    public boolean canProcess(ItemStack stack, Level level) {
        IHeat cap = HeatCapability.get(stack);
        if (cap != null) {
            return true;
        }
        Optional<RecipeHolder<SmeltingRecipe>> smeltingRecipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);

        Optional<RecipeHolder<BlastingRecipe>> blastingRecipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.BLASTING, new SingleRecipeInput(stack), level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);

        return (smeltingRecipe.isPresent() && blastingRecipe.isEmpty());
    }

    @Override
    @Nullable
    public List<ItemStack> process(ItemStack stack, Level level) {
        Optional<RecipeHolder<SmokingRecipe>> smokingRecipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMOKING, new SingleRecipeInput(stack), level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);

        Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> smeltingRecipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);

        Optional<RecipeHolder<BlastingRecipe>> blastingRecipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.BLASTING, new SingleRecipeInput(stack), level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);

        if (smeltingRecipe.isPresent()) {
            RegistryAccess registryAccess = level.registryAccess();
            if ((smokingRecipe.isEmpty() || !ItemStack.isSameItem(smokingRecipe.get().value()
                            .getResultItem(registryAccess),
                    smeltingRecipe.get().value()
                            .getResultItem(registryAccess)))
                    && ((blastingRecipe.isEmpty() || !ItemStack.isSameItem(blastingRecipe.get().value()
                            .getResultItem(registryAccess),
                    smeltingRecipe.get().value()
                            .getResultItem(registryAccess))))) {
                return RecipeApplier.applyRecipeOn(level, stack, smeltingRecipe.get().value(), false);
            }
        }

        return null;
    }

    @Override
    public boolean isValidAt(Level level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        BlazeBurnerBlock.HeatLevel heatLevel = BlazeBurnerBlock.HeatLevel.NONE;
        if ((AllTags.AllBlockTags.FAN_PROCESSING_CATALYSTS_SMOKING.matches(blockState) || AllTags.AllBlockTags.FAN_PROCESSING_CATALYSTS_SMOKING.matches(blockState)) && blockState.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
            heatLevel = blockState.getValue(BlazeBurnerBlock.HEAT_LEVEL);
            return (heatLevel.isAtLeast(BlazeBurnerBlock.HeatLevel.FADING) && !heatLevel.isAtLeast(BlazeBurnerBlock.HeatLevel.KINDLED));
        }/*
        else if (blockState.is(BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Compat.CLH_MOD_ID, "basic_burner")))) {
            heatLevel = blockState.getValue(BasicBurnerBlock.HEAT_LEVEL);
            return (heatLevel.isAtLeast(BlazeBurnerBlock.HeatLevel.FADING) && !heatLevel.isAtLeast(BlazeBurnerBlock.HeatLevel.KINDLED));
        }*/
        else return false;
    }

    @Override
    public void spawnProcessingParticles(Level level, Vec3 pos) {
        if (level.random.nextInt(8) != 0)
            return;
        level.addParticle(ParticleTypes.LARGE_SMOKE, pos.x, pos.y + .25f, pos.z, 0, 1 / 16f, 0);
    }

    @Override
    public void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random) {
        particleAccess.setColor(Color.mixColors(0xFF0000, 0xFF5555, random.nextFloat()));
        particleAccess.setAlpha(.5f);
        if (random.nextFloat() < 1 / 32f)
            particleAccess.spawnExtraParticle(ParticleTypes.FLAME, .1825f);
        if (random.nextFloat() < 1 / 16f)
            particleAccess.spawnExtraParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.LAVA.defaultBlockState()), .1825f);
    }

    @Override
    public void affectEntity(Entity entity, Level level) {
        if (level.isClientSide)
            return;

        if (!entity.fireImmune()) {
            entity.igniteForSeconds(5);
            entity.hurt(CreateDamageSources.fanLava(level), 3);
        }
    }

}
