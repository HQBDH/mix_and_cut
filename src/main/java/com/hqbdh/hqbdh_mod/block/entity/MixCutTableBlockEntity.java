package com.hqbdh.hqbdh_mod.block.entity;

import com.hqbdh.hqbdh_mod.menu.MixCutTableMenu;
import com.hqbdh.hqbdh_mod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import com.hqbdh.hqbdh_mod.menu.MixCutTableMenu;
import com.hqbdh.hqbdh_mod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.*;

import org.jetbrains.annotations.Nullable;
import com.hqbdh.hqbdh_mod.recipe.MixCutRecipe;
import com.hqbdh.hqbdh_mod.recipe.ModRecipes;

public class MixCutTableBlockEntity extends BlockEntity implements MenuProvider {
    private boolean isUpdatingResults = false;//设置递归检查的布尔变量
    // 新增：物品容器（管理10个槽位的物品）
    private final ItemStackHandler itemHandler = new ItemStackHandler(19) {
        //GUI的设置槽位在这里管理
        @Override
        protected void onContentsChanged(int slot) {
            // 当物品变化时，标记方块实体为“已修改”（触发世界保存、同步到客户端）
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3); // 通知客户端更新
            }
            // 新增：物品变化时更新配方
            // 修改1：添加槽位检查，只有输入槽变化时才更新配方
            // 0-9是输入槽和输出槽，10-18是结果槽
            if (!isUpdatingResults && slot < 9) {
                updateAvailableRecipes();//根据当前配方显示结果
            }
        }

    };

    // 流体槽（容量1000mB）
    private final FluidTank fluidTank = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            //updateAvailableRecipes();
            setChanged(); // 流体变化时标记为“已修改”
            // 关键：通知客户端方块数据更新（流体变化时触发）
            if (level != null) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
            // 新增：物品变化时更新配方
            if (!isUpdatingResults) {
                updateAvailableRecipes();
            }
        }
    };

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT()); // 将物品处理器的状态序列化为 NBT
        tag.put("fluidTank", fluidTank.writeToNBT(new CompoundTag())); // 保存流体罐数据
    }

    // 从 NBT 加载物品数据（世界加载时调用）
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("inventory")) { // 检查是否有物品数据
            itemHandler.deserializeNBT(tag.getCompound("inventory")); // 反序列化物品数据
        }
        if (tag.contains("fluidTank")) {
            fluidTank.readFromNBT(tag.getCompound("fluidTank")); // 加载流体罐数据
        }
    }
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // 将方块实体数据打包成数据包
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // 关键：提供客户端同步的标签数据（包含流体和物品信息）
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag); // 将流体和物品数据写入标签
        return tag;
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }
    public FluidTank getFluidTank() {
        return fluidTank;
    }
    public MixCutTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIX_CUT_TABLE.get(), pos, state);
    }

    // GUI的标题（将在语言文件中翻译）
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.mixing_cutting.mix_cut_table");
    }
    // 创建菜单实例，供右键方块时调用
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new MixCutTableMenu(windowId, inventory, this, this.getBlockPos());
    }

    //配方方面
    // 新增配方相关字段
    private int progress = 0;
    private int maxProgress = 200;
    private String selectedRecipeId = "";
    private final List<MixCutRecipe> availableRecipes = new ArrayList<>();
    private int scrollOffset = 0;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> scrollOffset;
                case 3 -> availableRecipes.size();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> scrollOffset = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    // 获取当前匹配的配方
    public void updateAvailableRecipes() {
        if (level == null) return;

        availableRecipes.clear();
        NonNullList<ItemStack> inputItems = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int i = 0; i < 9; i++) {
            inputItems.set(i, itemHandler.getStackInSlot(i));
        }

        FluidStack currentFluid = fluidTank.getFluid();

        // 获取所有配方并检查匹配
        List<MixCutRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(ModRecipes.MIX_CUT_TYPE.get());
        for (MixCutRecipe recipe : allRecipes) {
            if (recipe.matchesInput(inputItems, currentFluid)) {
                availableRecipes.add(recipe);
            }
        }
        setChanged();
        updateResultSlot();
    }

    // 选择配方
    public void selectRecipe(String recipeId) {
        this.selectedRecipeId = recipeId;
        updateResultSlot();
        setChanged();

    }
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        // 首先，让原版逻辑处理NBT数据的加载
        super.onDataPacket(net, pkt);

        // 关键：在客户端接收到数据更新后，立即重新计算可用的配方列表。
        // 这能确保在玩家看到流体/物品变化的同时，客户端的配方缓存也已经刷新。
        if (this.level != null && this.level.isClientSide) {
            updateAvailableRecipes();
        }
    }
    // 修改 updateResultSlot 方法
    private void updateResultSlot() {
        if (level == null) return;

        isUpdatingResults = true; // +++ 新增：保护标志 +++
        try {
            // 清空所有配方预览槽位（10-18）
            for (int i = 10; i <= 18; i++) {
                itemHandler.setStackInSlot(i, ItemStack.EMPTY);
            }

            // 清空输出槽（9） - 只在没有选中配方时清空
            if (selectedRecipeId.isEmpty()) {
                itemHandler.setStackInSlot(9, ItemStack.EMPTY);
            }

            if (!selectedRecipeId.isEmpty()) {
                Optional<MixCutRecipe> selectedRecipe = availableRecipes.stream()
                        .filter(recipe -> recipe.getRecipeId().equals(selectedRecipeId))
                        .findFirst();

                if (selectedRecipe.isPresent()) {
                    ItemStack result = selectedRecipe.get().getResultItem(level.registryAccess());
                    // 将选中的配方结果放到输出槽 (9)
                    itemHandler.setStackInSlot(9, result.copy());
                } else {
                    // 如果选中的配方不在可用列表中，清空选中状态
                    this.selectedRecipeId = "";
                    itemHandler.setStackInSlot(9, ItemStack.EMPTY);
                }
            }
            // 移除自动选择第一个配方的逻辑！让玩家手动选择
        } finally {
            isUpdatingResults = false;
        }
    }

    // 修改 craftSelectedRecipe 方法
    public void craftSelectedRecipe() {
        // if (level == null || selectedRecipeId.isEmpty() || level.isClientSide) {
        //   System.out.println("[DEBUG] craftSelectedRecipe 跳过：level=null/客户端/无选中配方");
        // return;
        //}
        // System.out.println("[DEBUG] 准备调用，跳过");
        if (this.level == null) {
            //      System.out.println("[DEBUG] level == null：方块实体不在世界中，位置：" + this.getBlockPos());
            return;
        }
        if (selectedRecipeId.isEmpty()) {
            //    System.out.println("[DEBUG] 未选中配方，跳过");
            return;
        }

        if (this.level.isClientSide) {
            //  System.out.println("[DEBUG] 在客户端调用，跳过");
            return;
        }
        //    System.out.println("[DEBUG] 在服务端调用，跳过");

        Optional<MixCutRecipe> recipeOpt = availableRecipes.stream()
                .filter(recipe -> recipe.getRecipeId().equals(selectedRecipeId))
                .findFirst();

        if (recipeOpt.isPresent()) {
            MixCutRecipe recipe = recipeOpt.get();

            // 1. 收集当前输入和流体
            NonNullList<ItemStack> inputItems = NonNullList.withSize(9, ItemStack.EMPTY);
            for (int i = 0; i < 9; i++) {
                inputItems.set(i, itemHandler.getStackInSlot(i));
            }
            FluidStack currentFluid = fluidTank.getFluid();
            // 2. 确认配方是否匹配
            //   boolean isMatch = recipe.matchesInput(inputItems, currentFluid);
            //    System.out.println("[DEBUG] 配方匹配结果：" + isMatch + " | 流体量：" + currentFluid.getAmount() + "mB | 配方需求流体：" + recipe.getFluidRequirement().getAmount());

            // 2. 验证配方是否匹配（输入、流体是否满足）
            if (recipe.matchesInput(inputItems, currentFluid)) {
                // 3. 检查输出槽容量
                ItemStack result = recipe.getResultItem(level.registryAccess()).copy();
                ItemStack outputSlot = itemHandler.getStackInSlot(9);

                boolean canOutput = false;
                if (outputSlot.isEmpty()) {
                    canOutput = true;
                } else if (ItemStack.isSameItemSameTags(outputSlot, result)) {
                    int newCount = outputSlot.getCount() + result.getCount();
                    if (newCount <= outputSlot.getMaxStackSize()) {
                        canOutput = true;
                    }
                }

                if (!canOutput) {
                    return; // 输出槽已满，无法制作
                }

                // 4. 消耗输入物品
                consumeInputItems(recipe);
                // System.out.println("执行了吗");

                // 5. 消耗流体
                if (!recipe.getFluidRequirement().isEmpty()) {
                    fluidTank.drain(recipe.getFluidRequirement().getAmount(), IFluidHandler.FluidAction.EXECUTE);
                }

                // 6. 生成结果到输出槽
                if (outputSlot.isEmpty()) {
                    itemHandler.setStackInSlot(9, result);
                } else {
                    outputSlot.grow(result.getCount());
                    itemHandler.setStackInSlot(9, outputSlot);
                }
                //这里是插入制作物品的声效
                level.playSound(
                        null, // 传入 null 代表这是一个服务器端发出的、所有附近玩家都能听到的声音
                        getBlockPos(), // 声音播放的位置，即方块实体的位置
                        net.minecraft.sounds.SoundEvents.UI_STONECUTTER_TAKE_RESULT, // 切石机取出成品的音效
                        net.minecraft.sounds.SoundSource.BLOCKS, // 声音来源类型，设为方块
                        1.0F, // 音量 (1.0F为默认)
                        1.0F  // 音调 (1.0F为默认)
                );
                // 7. 同步到客户端
                setChanged();
                if (level != null) {
                    //      System.out.println("执行了吗客户端");
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);

                }

                // 8. 更新配方（材料消耗后，可能影响可用配方列表）
                updateAvailableRecipes();
            }
        }
    }

    // 消耗输入物品
    private void consumeInputItems(MixCutRecipe recipe) {
        // ▼▼▼ 【修改】完全重写消耗逻辑 ▼▼▼
        // 遍历配方中每一个带数量的原料
        for (MixCutRecipe.CountedIngredient countedIngredient : recipe.getCountedIngredients()) {
            int needed = countedIngredient.count;

            // 遍历所有输入槽（0-8）
            for (int i = 0; i < 9; i++) {
                ItemStack stackInSlot = this.itemHandler.getStackInSlot(i);

                // 如果槽内物品匹配当前原料，并且还有需要消耗的数量
                if (countedIngredient.ingredient.test(stackInSlot) && needed > 0) {
                    // 计算从这个槽中可以拿走多少物品
                    int toExtract = Math.min(stackInSlot.getCount(), needed);

                    // 从槽中抽取物品
                    this.itemHandler.extractItem(i, toExtract, false);

                    // 更新还需要消耗的数量
                    needed -= toExtract;
                }
            }
        }
        // ▲▲▲ 【修改结束】 ▲▲▲
    }


    // 获取可用配方
    public List<MixCutRecipe> getAvailableRecipes() {
        return new ArrayList<>(availableRecipes);
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setScrollOffset(int offset) {
        this.scrollOffset = Math.max(0, offset);setChanged();
    }

    public String getSelectedRecipeId() {
        return selectedRecipeId;
    }

    public ContainerData getContainerData() {
        return data;
    }

    // 在物品或流体变化时更新配方
    // 1. 物品容器：在物品变化时更新配方


    // 2. 流体槽：在流体变化时更新配方


}