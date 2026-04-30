package net.chauvedev.woodencog.utils;

import net.dries007.tfc.util.Metal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public class FluidAccess {

    public static Fluid copper = CogUtil.findNotNullFluid(TFCMetalRS(Metal.COPPER));
    public static Fluid bismuth = CogUtil.findNotNullFluid(TFCMetalRS(Metal.BISMUTH));
    public static Fluid zinc = CogUtil.findNotNullFluid(TFCMetalRS(Metal.ZINC));
    public static Fluid bismuthBronze = CogUtil.findNotNullFluid(TFCMetalRS(Metal.BISMUTH_BRONZE));
    public static Fluid silver = CogUtil.findNotNullFluid(TFCMetalRS(Metal.SILVER));
    public static Fluid gold = CogUtil.findNotNullFluid(TFCMetalRS(Metal.GOLD));
    public static Fluid blackBronze = CogUtil.findNotNullFluid(TFCMetalRS(Metal.BLACK_BRONZE));
    public static Fluid brass = CogUtil.findNotNullFluid(TFCMetalRS(Metal.BRASS));
    public static Fluid tin = CogUtil.findNotNullFluid(TFCMetalRS(Metal.TIN));
    public static Fluid bronze = CogUtil.findNotNullFluid(TFCMetalRS(Metal.BRONZE));
    public static Fluid roseGold = CogUtil.findNotNullFluid(TFCMetalRS(Metal.ROSE_GOLD));
    public static Fluid sterlingSilver = CogUtil.findNotNullFluid(TFCMetalRS(Metal.STERLING_SILVER));
    public static Fluid blackSteel = CogUtil.findNotNullFluid(TFCMetalRS(Metal.BLACK_STEEL));
    public static Fluid steel = CogUtil.findNotNullFluid(TFCMetalRS(Metal.STEEL));
    public static Fluid weakBlueSteel = CogUtil.findNotNullFluid(TFCMetalRS(Metal.WEAK_BLUE_STEEL));
    public static Fluid weakRedSteel = CogUtil.findNotNullFluid(TFCMetalRS(Metal.WEAK_RED_STEEL));
    public static Fluid nickel = CogUtil.findNotNullFluid(TFCMetalRS(Metal.NICKEL));
    public static Fluid weakSteel = CogUtil.findNotNullFluid(TFCMetalRS(Metal.WEAK_STEEL));

    private static ResourceLocation TFCMetalRS(Metal metalEnum) {
        return ResourceLocation.tryBuild("tfc","metal/"+metalEnum.getSerializedName());
    }
}
