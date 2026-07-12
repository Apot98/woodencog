package net.chauvedev.woodencog.content.fan_types;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.damageTypes.CreateDamageSources;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import net.chauvedev.woodencog.compat.Compat;
import net.chauvedev.woodencog.utils.ModTags;
import net.createmod.catnip.theme.Color;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HeatingSeethingType implements FanProcessingType {
    public HeatingSeethingType() {
    }

    @Override
    public int getPriority() {
        return 1300;
    } //higher than CDnDesire Seething 1200
    /*public int getPriority() {
        return 1100;
    } //lower than CDnDesire Seething 1200

     */

    @Override
    public boolean canProcess(ItemStack stack, Level level) {
        IHeat cap = HeatCapability.get(stack);
        if (cap != null) {
            return true;
        }
        if (Compat.isCDNDESIRELoaded()) {
            RecipeType seethingType = BuiltInRegistries.RECIPE_TYPE.get(ResourceLocation.fromNamespaceAndPath(Compat.CDNDESIRE_MOD_ID, "seething"));
            if (seethingType != null) {
                Optional<RecipeHolder<?>> seethingRecipe = level.getRecipeManager()
                        .getRecipeFor(seethingType, new SingleRecipeInput(stack), level)
                        .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
                if (seethingRecipe.isPresent()) {return true;}
            }
        }

        Optional<RecipeHolder<SmeltingRecipe>> smeltingRecipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
        if (smeltingRecipe.isPresent()) {return true;}

        Optional<RecipeHolder<BlastingRecipe>> blastingRecipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.BLASTING, new SingleRecipeInput(stack), level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
        if (blastingRecipe.isPresent()) {return true;}

        return !(stack.has(DataComponents.FIRE_RESISTANT) || ((ModTags.Items.UNBURNABLE != null) && stack.is(ModTags.Items.UNBURNABLE)));
    }

    @Override
    @Nullable
    public List<ItemStack> process(ItemStack stack, Level level) {
        if (Compat.isCDNDESIRELoaded()) {
            RecipeType seethingType = BuiltInRegistries.RECIPE_TYPE.get(ResourceLocation.fromNamespaceAndPath(Compat.CDNDESIRE_MOD_ID, "seething"));
            if (seethingType != null) {
                Optional<RecipeHolder<?>> seethingRecipe = level.getRecipeManager()
                        .getRecipeFor(seethingType, new SingleRecipeInput(stack), level)
                        .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
                if (seethingRecipe.isPresent()) {
                    return RecipeApplier.applyRecipeOn(level, stack, seethingRecipe.get().value(), false);
                }
            }
        }

        Optional<RecipeHolder<SmokingRecipe>> smokingRecipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMOKING, new SingleRecipeInput(stack), level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);

        Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> smeltingRecipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);

        if (smeltingRecipe.isEmpty()) {
            smeltingRecipe = level.getRecipeManager()
                    .getRecipeFor(RecipeType.BLASTING, new SingleRecipeInput(stack), level)
                    .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
        }

        if (smeltingRecipe.isPresent()) {
            RegistryAccess registryAccess = level.registryAccess();
            if (smokingRecipe.isEmpty() || !ItemStack.isSameItem(smokingRecipe.get().value()
                            .getResultItem(registryAccess),
                    smeltingRecipe.get().value()
                            .getResultItem(registryAccess))) {
                return RecipeApplier.applyRecipeOn(level, stack, smeltingRecipe.get().value(), false);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public boolean isValidAt(Level level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        BlazeBurnerBlock.HeatLevel heatLevel;
        if (AllTags.AllBlockTags.FAN_PROCESSING_CATALYSTS_BLASTING.matches(blockState) && blockState.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
            heatLevel = blockState.getValue(BlazeBurnerBlock.HEAT_LEVEL);
            return heatLevel.isAtLeast(BlazeBurnerBlock.HeatLevel.SEETHING);
        }/*
        else if (blockState.is(BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Compat.CLH_MOD_ID, "basic_burner")))) {
            heatLevel = blockState.getValue(BasicBurnerBlock.HEAT_LEVEL);
            return heatLevel.isAtLeast(BlazeBurnerBlock.HeatLevel.SEETHING);
        }*/
        else return false;
    }

    @Override
    public void spawnProcessingParticles(Level level, Vec3 pos) {
        if (level.random.nextInt(8) != 0)
            return;
        Vector3f color = new Color(0xFFFF55).asVectorF();
        level.addParticle(new DustParticleOptions(color, 1.0F), pos.x + (double)((level.random.nextFloat() - 0.5F) * 0.5F), pos.y + 0.5, pos.z + (double)((level.random.nextFloat() - 0.5F) * 0.5F), 0.0, 0.125, 0.0);
        level.addParticle(ParticleTypes.LARGE_SMOKE, pos.x, pos.y + .25f, pos.z, 0, 1 / 16f, 0);
    }

    @Override
    public void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random) {
        particleAccess.setColor(Color.mixColors(0xFFFF55, 0xFFFFFF, random.nextFloat()));
        particleAccess.setAlpha(.5f);
        if (random.nextFloat() < 1 / 32f)
            particleAccess.spawnExtraParticle(ParticleTypes.FLAME, .30f);
        if (random.nextFloat() < 1 / 16f)
            particleAccess.spawnExtraParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.LAVA.defaultBlockState()), .30f);
    }

    @Override
    public void affectEntity(Entity entity, Level level) {
        if (level.isClientSide)
            return;

        if (!entity.fireImmune()) {
            entity.igniteForSeconds(15);
            entity.hurt(CreateDamageSources.fanLava(level), 4);
        }
    }

}
