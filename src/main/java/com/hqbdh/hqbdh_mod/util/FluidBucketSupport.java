package com.hqbdh.hqbdh_mod.util;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.HashMap;
import java.util.Map;

public class FluidBucketSupport {
    // 存储"流体ID → 对应桶物品"的映射
    private static final Map<ResourceLocation, Item> FLUID_TO_BUCKET = new HashMap<>();

    // 初始化支持的流体（游戏启动时调用）
    public static void init() {
        // Vanilla 流体
        addSupport(Fluids.WATER, Items.WATER_BUCKET);
        addSupport(Fluids.LAVA, Items.LAVA_BUCKET);

        // 示例：Alex's Caves 酸液
        addSupport("alexscaves:acid", "alexscaves:acid_bucket");

        // 可继续添加其他模组流体，格式：addSupport("模组ID:流体ID", "模组ID:桶物品ID")
    }

    // 添加"流体 → 桶"的映射（Fluid对象版）
    private static void addSupport(Fluid fluid, Item bucketItem) {
        ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluid);
        if (fluidRL != null) {
            FLUID_TO_BUCKET.put(fluidRL, bucketItem);
        }
    }

    // 添加"流体 → 桶"的映射（注册表名版）
    private static void addSupport(String fluidRLStr, String bucketRLStr) {
        ResourceLocation fluidRL = new ResourceLocation(fluidRLStr);
        ResourceLocation bucketRL = new ResourceLocation(bucketRLStr);

        // 修改1：使用 containsKey 和 getValue 替代 getOptional
        if (ForgeRegistries.ITEMS.containsKey(bucketRL)) {
            Item bucket = ForgeRegistries.ITEMS.getValue(bucketRL);
            if (bucket != null) {
                FLUID_TO_BUCKET.put(fluidRL, bucket);
            }
        }
    }

    // 判断"物品是否为支持的流体桶"
    public static boolean isSupportedFluidBucket(ItemStack stack) {
        // 情况1：是Minecraft默认桶
        if (stack.getItem() instanceof BucketItem) {
            ResourceLocation bucketRL = ForgeRegistries.ITEMS.getKey(stack.getItem());
            return FLUID_TO_BUCKET.containsValue(stack.getItem())
                    || (bucketRL != null && FLUID_TO_BUCKET.entrySet().stream()
                    .anyMatch(e -> ForgeRegistries.ITEMS.getKey(e.getValue()).equals(bucketRL)));
        }
        // 情况2：是带流体能力的物品（如其他模组的桶）
        // 修改：使用 ForgeCapabilities 替代 CapabilityFluidHandler
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
    }

    // 从"桶物品"中提取流体
    public static FluidStack getFluidFromBucket(ItemStack stack) {
        // Vanilla 桶
        if (stack.is(Items.WATER_BUCKET)) return new FluidStack(Fluids.WATER, 1000);
        if (stack.is(Items.LAVA_BUCKET)) return new FluidStack(Fluids.LAVA, 1000);

        // Alex's Caves 酸液桶（示例）
        ResourceLocation stackRL = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (stackRL != null && stackRL.equals(new ResourceLocation("alexscaves:acid_bucket"))) {
            Fluid acid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("alexscaves:acid"));
            return acid != null ? new FluidStack(acid, 1000) : FluidStack.EMPTY;
        }

        // 其他带流体的物品（如模组桶）- 使用Capability
        // 修改2：直接使用 handler，移除模式匹配
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(handler -> handler.getFluidInTank(0))
                .orElse(FluidStack.EMPTY);
    }

    // 判断"流体是否被支持"
    public static boolean isSupportedFluid(Fluid fluid) {
        ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluid);
        return fluidRL != null && FLUID_TO_BUCKET.containsKey(fluidRL);
    }

    // 判断"流体处理器是否被支持"（简化判断，可根据需求细化）
    // 修改：使用正确的 IFluidHandler 类型
    public static boolean isSupportedFluidHandler(IFluidHandler handler) {
        return true;
    }

    // 创建"装满的桶物品"
    public static ItemStack createFilledBucket(Fluid fluid) {
        ResourceLocation fluidRL = ForgeRegistries.FLUIDS.getKey(fluid);
        if (fluidRL != null && FLUID_TO_BUCKET.containsKey(fluidRL)) {
            return new ItemStack(FLUID_TO_BUCKET.get(fluidRL)); // 返回预定义的桶物品
        }

        // 通用逻辑：用空桶创建装满的流体桶
        ItemStack emptyBucket = new ItemStack(Items.BUCKET);

        // 修改3：直接使用 handler，移除类型转换
        return emptyBucket.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(handler -> {
                    FluidStack fluidToFill = new FluidStack(fluid, 1000);
                    // 模拟填充，检查是否能成功
                    if (handler.fill(fluidToFill, IFluidHandler.FluidAction.SIMULATE) == 1000) {
                        // 实际填充
                        handler.fill(fluidToFill, IFluidHandler.FluidAction.EXECUTE);
                        return handler.getContainer();
                    }
                    return ItemStack.EMPTY;
                })
                .orElse(ItemStack.EMPTY);
    }
}