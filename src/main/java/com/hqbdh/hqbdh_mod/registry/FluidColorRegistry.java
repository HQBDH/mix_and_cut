package com.hqbdh.hqbdh_mod.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class FluidColorRegistry {
    private static final Map<ResourceLocation, Integer> FLUID_COLORS = new HashMap<>();

    public static void init() {
        // 注册原版流体
        registerFluidColor(new ResourceLocation("minecraft", "water"), 0xFF3F76E4);
        registerFluidColor(new ResourceLocation("minecraft", "lava"), 0xFFFF6A00);

        // 注册常见模组流体，例如：
        // 热力膨胀 (Thermal Expansion) 的浓缩液
        registerFluidColor(new ResourceLocation("thermal", "creosote"), 0xFF804000);
        registerFluidColor(new ResourceLocation("thermal", "refined_fuel"), 0xFF7F7F7F);
        // 沉浸工程 (Immersive Engineering) 的生物柴油
        registerFluidColor(new ResourceLocation("immersiveengineering", "biodiesel"), 0xFFC8FF4D);
        // 机械动力 (Create) 的甜浆
        registerFluidColor(new ResourceLocation("create", "honey"), 0xFFF4AC35);
        //机械动力的牛奶
        registerFluidColor(new ResourceLocation("create", "milk"), 0xFFFFFFFF);
        // 林业 (Forestry) 的生物质
        registerFluidColor(new ResourceLocation("forestry", "biomass"), 0xFF2E8B57);
        // ... 添加更多你希望支持的模组流体
    }

    public static void registerFluidColor(ResourceLocation fluidId, int color) {
        FLUID_COLORS.put(fluidId, color);
    }

    public static int getColor(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return 0xFF94E4FD; // 默认颜色
        }
        ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
        return FLUID_COLORS.getOrDefault(fluidId, 0xFF94E4FD); // 返回映射的颜色或默认的浅蓝色
    }
}