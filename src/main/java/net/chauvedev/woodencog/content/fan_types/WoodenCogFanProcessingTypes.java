package net.chauvedev.woodencog.content.fan_types;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import net.chauvedev.woodencog.WoodenCog;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus.Internal;

public class WoodenCogFanProcessingTypes {
    public static final HeatingFadingType HEATING_FADING_TYPE = (HeatingFadingType)register("heating_fading", new HeatingFadingType());
    public static final HeatingSeethingType HEATING_SEETHING_TYPE = (HeatingSeethingType)register("heating_seething", new HeatingSeethingType());

    public WoodenCogFanProcessingTypes() {
    }

    private static <T extends FanProcessingType> T register(String name, T type) {
        return Registry.register(CreateBuiltInRegistries.FAN_PROCESSING_TYPE, ResourceLocation.fromNamespaceAndPath(WoodenCog.MOD_ID, name), type);
    }

    @Internal
    public static void init() {
    }

    static {
        Object2ReferenceOpenHashMap<String, FanProcessingType> map = new Object2ReferenceOpenHashMap();
        map.put("HEATING_FADING", HEATING_FADING_TYPE);
        map.put("HEATING_SEETHING", HEATING_SEETHING_TYPE);
        map.trim();
    }
}
