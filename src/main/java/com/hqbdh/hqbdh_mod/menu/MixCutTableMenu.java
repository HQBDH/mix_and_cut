package com.hqbdh.hqbdh_mod.menu;

import com.hqbdh.hqbdh_mod.block.entity.MixCutTableBlockEntity;
import com.hqbdh.hqbdh_mod.recipe.MixCutRecipe;
import com.hqbdh.hqbdh_mod.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.IItemHandler;
//同步数据的接口类
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

import java.util.Optional;

public class MixCutTableMenu extends AbstractContainerMenu {
    private final MixCutTableBlockEntity blockEntity;
    private final ContainerLevelAccess levelAccess;

    // 槽位位置常量
    private static final int INPUT_SLOT_START_X = 8;
    private static final int INPUT_SLOT_START_Y = 18;

    private static final int OUTPUT_SLOT_X = 141;
    private static final int OUTPUT_SLOT_Y = 30;

    private static final int PLAYER_INV_START_X = 8;
    private static final int PLAYER_INV_START_Y = 84;

    private static final int HOTBAR_START_Y = 142;

    private static final int RESULT_SLOT_X = 76;
    private static final int RESULT_SLOT_Y = 20;

    private static final int RECIPE_DISPLAY_SLOT_X = 82; // 修改: 命名更明确，指向配方浏览槽的X
    private static final int RECIPE_DISPLAY_SLOT_Y = 20; // 修改: 命名更明确，指向配方浏览槽的Y

    private   ContainerData data; // 正确：只声明，不初始化
    //滚动数据偏移
    //可能存在问题
    //private final ContainerData data=null;

    // 1. 服务端构造函数（完整参数：包含实体和位置）
    public MixCutTableMenu(int windowId, Inventory playerInventory, MixCutTableBlockEntity entity, BlockPos pos) {
        super(ModMenus.MIX_CUT_TABLE.get(), windowId);
        this.blockEntity = entity;
        this.levelAccess = ContainerLevelAccess.create(entity.getLevel(), pos);

// 新增：初始化 ContainerData（从方块实体获取，null时用默认实现）↓

        this.data = entity.getContainerData();//这个是配方数
        // 注册所有槽位
        if (entity != null) {
            IItemHandler entityInventory = entity.getItemHandler();
            registerSlots(entityInventory, playerInventory);
            // 新增：注册数据同步（关键！让客户端能接收数据）↓
            addDataSlots(data);
        }else{
            this.data = new SimpleContainerData(4); // 长度4，与服务端data的getCount()一致
            this.addDataSlots(this.data);
            //System.out.println("[客户端菜单] 警告：方块实体为null（位置：" + pos + "），使用默认ContainerData");
        }
    }

    // 2. 客户端构造函数（从缓冲区读取数据）
    public MixCutTableMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(ModMenus.MIX_CUT_TABLE.get(), windowId);
        // 从缓冲区读取方块位置
        BlockPos pos = buf.readBlockPos();
        // 根据位置获取方块实体
        MixCutTableBlockEntity entity = getBlockEntityFromBuf(playerInventory, pos);
        this.blockEntity = entity;
        // 初始化levelAccess（基于读取到的pos，避免依赖entity）
        this.levelAccess = ContainerLevelAccess.create(playerInventory.player.level(), pos);

        // 注册客户端槽位
        if (entity != null) {
            //现在实体不为Null了
            IItemHandler entityInventory = entity.getItemHandler();
            registerSlots(entityInventory, playerInventory);

            // 读取并同步流体数据
            CompoundTag fluidTag = buf.readNbt();
            if (fluidTag != null && fluidTag.contains("MixCutTable_FluidTank")) {
                CompoundTag fluidTankTag = fluidTag.getCompound("MixCutTable_FluidTank");
                entity.getFluidTank().readFromNBT(fluidTankTag);
              //  System.out.println("[客户端菜单] 同步流体数据：" + entity.getFluidTank().getFluidAmount() + "mB");
            }
// 【必须添加1】从方块实体获取ContainerData并赋值给客户端data
            this.data = entity.getContainerData();
            // 【必须添加2】注册数据同步，让客户端创建长度为4的数据列表
            this.addDataSlots(this.data);
          //  System.out.println("方块实体不为null（位置：" + pos + "）");
        }
        else {
            this.data = new SimpleContainerData(4);
            this.addDataSlots(this.data);
             System.out.println("[客户端菜单] 警告：方块实体为null（位置：" + pos + "）");
             }

    }

    // 3. 统一注册所有槽位（修复索引冲突：配方槽从10开始）
    private void registerSlots(IItemHandler entityInventory, Inventory playerInventory) {
        // ① 输入槽（0-8）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotIndex = row * 3 + col;
                int x = INPUT_SLOT_START_X+1 + col * 18;
                int y = INPUT_SLOT_START_Y+1+ row * 18;
                this.addSlot(new SlotItemHandler(entityInventory, slotIndex, x, y));
            }
        }

// ② 输出槽（9）
        this.addSlot(new SlotItemHandler(entityInventory, 9, OUTPUT_SLOT_X+6, OUTPUT_SLOT_Y+24) {

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // 禁止放入
            }

            @Override // 修改: 移除，让 getItem 返回实际槽位物品
            public ItemStack getItem() {
                return entityInventory.getStackInSlot(9); // 返回实际的物品堆栈
            }
//Minecraft的菜单系统（Container）为了更新UI，会调用第9号 对象的 方法，参数是服务端发来的“8个石头”。Slotset(ItemStack stack)
        //    @Override // 修改: 移除，让 set 方法更新实际槽位
        //    public void set(ItemStack stack) {
         //       entityInventory.insertItem(9, stack, false); // 直接设置到实际槽位
           // }
public void selectRecipeClientSide(String recipeId) {
    // blockEntity 同时存在于客户端和服务端
    // 当在客户端调用时，它会更新客户端的方块实体实例

}
            @Override // 修改: 移除 hasItem，依赖 SlotItemHandler 默认行为
            public boolean hasItem() {
                return !getItem().isEmpty();
            }

            @Override // 修改: 移除 remove，依赖 SlotItemHandler 默认行为
            public ItemStack remove(int amount) {
                return entityInventory.extractItem(9, amount, false);
            }

            @Override // 修改: 在取出时触发制作
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
               // System.out.println("[DEBUG] onTake 触发 | 服务端？" + !player.level().isClientSide);
                // 关键：拿取物品时，强制触发服务端的合成逻辑（消耗输入、流体，生成结果）
                if (blockEntity != null && !player.level().isClientSide()) {//若是在客户端来的，则转到服务端
                //    System.out.println("[DEBUG] onTake：调用 craftSelectedRecipe");
                    blockEntity.craftSelectedRecipe();

                }
            }
            //原本设置的是只有物品时才能取出
            @Override // 修改: 移除 mayPickup，依赖 SlotItemHandler 默认行为
            public boolean mayPickup(Player player) {
                return !getItem().isEmpty();
            }
        });

        // ③ 配方结果槽（10-18，修复索引冲突）,使用的是配方结果设置
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotIndex = 10 + row * 3 + col; // 从10开始，避开输出槽9
                int x = RESULT_SLOT_X + col * 18;
                int y = RESULT_SLOT_Y + row * 18;
                this.addSlot(new SlotItemHandler(entityInventory, slotIndex, x, y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false; // 禁止放入
                    }
                });
            }
        }

        // ④ 玩家背包（27格：9-35）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9 + 9;
                int x = PLAYER_INV_START_X+1 + col * 18;
                int y = PLAYER_INV_START_Y+1 + row * 18;
                this.addSlot(new Slot(playerInventory, slotIndex, x, y));
            }
        }

        // ⑤ 玩家快捷栏（9格：0-8）
        for (int col = 0; col < 9; col++) {
            int slotIndex = col;
            int x = PLAYER_INV_START_X+1 + col * 18;
            int y = HOTBAR_START_Y+1;
            this.addSlot(new Slot(playerInventory, slotIndex, x, y));
        }
    }

    // 4. 根据位置获取方块实体（客户端专用）
    private static MixCutTableBlockEntity getBlockEntityFromBuf(Inventory playerInventory, BlockPos pos) {
        Level level = playerInventory.player.level();
        BlockEntity rawEntity = level.getBlockEntity(pos);
        return rawEntity instanceof MixCutTableBlockEntity ? (MixCutTableBlockEntity) rawEntity : null;
    }

    // 5. 菜单有效性验证
    @Override
    public boolean stillValid(Player player) {
        return this.levelAccess.evaluate((level, pos) -> {
            BlockEntity currentEntity = level.getBlockEntity(pos);
            boolean isEntityValid = currentEntity instanceof MixCutTableBlockEntity;
            boolean isDistanceValid = player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0D;
            return isEntityValid && isDistanceValid;
        }, false);
    }

    // 6. 快速移动物品（）快速得到结果，也就是点击获取物品的逻辑
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // 如果点击的是结果槽（索引9）
            if (index == 9) {
                // 触发结果槽的 onTake 方法
                if (!this.moveItemStackTo(itemstack1, 27, 55, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
                if (blockEntity != null && !player.level().isClientSide()) {
                    blockEntity.craftSelectedRecipe();
                }
            }
            // 如果点击的是配方浏览槽（索引10-18）
            else if (index >= 10 && index <= 18) {
                // 1. 计算当前浏览槽对应的配方索引（槽位索引10-18 → 偏移0-8）
                int recipeSlotIndex = index - 10;
                // 2. 结合滚动偏移，得到实际配方在可用配方列表中的索引
                int actualRecipeIndex = blockEntity.getScrollOffset() + recipeSlotIndex;
                // 3. 获取该配方并触发合成（扣减材料）
                if (blockEntity != null && actualRecipeIndex < blockEntity.getAvailableRecipes().size()) {
                    MixCutRecipe selectedRecipe = blockEntity.getAvailableRecipes().get(actualRecipeIndex);
                    blockEntity.selectRecipe(selectedRecipe.getRecipeId()); // 选中配方
                    if (!player.level().isClientSide) {
                        blockEntity.craftSelectedRecipe();
                    }
                   // blockEntity.craftSelectedRecipe(); // 扣减输入材料
                }
                // 4. 转移物品到玩家库存（此时物品已通过合成逻辑生成，可安全转移）
                if (!this.moveItemStackTo(itemstack1, 27, 63, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // 如果点击的是玩家库存，尝试移动到输入槽
            else if (index >= 27 && index < 55) {
                if (!this.moveItemStackTo(itemstack1, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }
    // 7. Getter方法（供GUI使用）
    public MixCutTableBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public ContainerLevelAccess getLevelAccess() {
        return levelAccess;
    }
    // 新增：数据获取方法（给 GUI 用，获取同步过来的进度、滚动偏移等）↓
    public int getProgress() {
        return data.get(0); // 对应方块实体 data 中的「进度」（index=0）
    }

    public int getMaxProgress() {
        return data.get(1); // 对应方块实体 data 中的「最大进度」（index=1）
    }

    public int getScrollOffset() {
        return data.get(2); // 对应方块实体 data 中的「滚动偏移」（index=2）
    }

    public int getAvailableRecipeCount() {
        return data.get(3); // 对应方块实体 data 中的「可用配方数量」（index=3）
    }

    // 新增：服务器端方法调用（GUI 通过菜单间接调用方块实体的功能，保证安全）↓
    public void setScrollOffset(int offset) {
        if (blockEntity != null) {
            blockEntity.setScrollOffset(offset); // 调用方块实体的「设置滚动偏移」方法
        }
    }

    public void selectRecipe(String recipeId) {
        if (blockEntity != null) {
            blockEntity.selectRecipe(recipeId); // 调用方块实体的「选择配方」方法
        }
    }

    public void craftRecipe() {
        if (blockEntity != null) {
            blockEntity.craftSelectedRecipe(); // 调用方块实体的「制作配方」方法
        }
    }
    public void selectRecipeClientSide(String recipeId) {
        // 在这个内部类里，没有名为 blockEntity 的字段，所以报错
        if (this.blockEntity != null) { // 错误1发生于此
            this.blockEntity.selectRecipe(recipeId);
        }
    }

}