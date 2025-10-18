package com.hqbdh.hqbdh_mod.block;

import com.hqbdh.hqbdh_mod.util.FluidBucketSupport;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock; // 新增导入
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import com.hqbdh.hqbdh_mod.block.entity.MixCutTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import java.util.stream.Stream; // 新增导入
import net.minecraft.world.level.block.RenderShape; // 新增导入
import net.minecraft.world.level.BlockGetter; // 新增导入
import net.minecraft.world.phys.shapes.CollisionContext; // 新增导入
import net.minecraft.world.phys.shapes.VoxelShape; // 新增导入
import net.minecraftforge.fluids.capability.IFluidHandlerItem; // 导入子接口

import net.minecraft.tags.BlockTags;

// 新增导入
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;

public class MixCutTableBlock extends Block implements EntityBlock { // 实现EntityBlock接口
    public MixCutTableBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.0F, 6.0F) // 设置硬度为3.0F，爆炸抗性为6.0F
                .sound(SoundType.STONE)
                        .noOcclusion() // 非实心方块，允许看穿
               // .requiresCorrectToolForDrops()
              //  .tag(BlockTags.MINEABLE_WITH_PICKAXE)  // ③ 标记为“可被镐子挖掘”
        );
    }
    private static final VoxelShape SHAPE = Stream.of(
            Block.box(0.5, 15, 0.5, 15.5, 16, 15.5),
            // 2. 桌腿1：左下角（桌面左下角正下方），2×2底面积，高度15单位
            Block.box(0, 0, 0, 2, 15, 2),
            // 3. 桌腿2：右下角（桌面右下角正下方），2×2底面积，高度15单位
            Block.box(14, 0, 0, 16, 15, 2),
            // 4. 桌腿3：左上角（桌面左上角正下方），2×2底面积，高度15单位
            Block.box(0, 0, 14, 2, 15, 16),
            // 5. 桌腿4：右上角（桌面右上角正下方），2×2底面积，高度15单位
            Block.box(14, 0, 14, 16, 15, 16)
    ).reduce((v1, v2) -> Shapes.or(v1, v2)).get();

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    //    System.out.println("创建混合切制机实体！位置：" + pos); // 日志
        return new MixCutTableBlockEntity(pos, state);//返回实体
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // 1. 仅服务端执行（客户端不处理GUI逻辑）
     //   System.out.println("服务端传递的方块位置：" + pos.getX() + "," + pos.getY() + "," + pos.getZ()); // 日志
        if (!level.isClientSide) {
            // 2. 安全强转 ServerPlayer（避免崩溃）
            if (!(player instanceof ServerPlayer serverPlayer)) {
            //    System.out.println("警告：点击位置与方块位置不一致！");
                return InteractionResult.PASS; // 非服务端玩家，跳过
            }
            // 3. 获取方块实体
            BlockEntity entity = level.getBlockEntity(pos);
            // 4. 实体为null或类型不匹配时，返回PASS（逻辑完整）
            if (!(entity instanceof MixCutTableBlockEntity mixCutEntity)) {
                return InteractionResult.PASS;
            }else{
                mixCutEntity.updateAvailableRecipes();
                // 标记方块需要保存和同步
                mixCutEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }//如果交互不为空则进行更新并保存。


            ItemStack heldStack = player.getItemInHand(hand);
            if (heldStack.getItem() instanceof BucketItem && FluidBucketSupport.isSupportedFluidBucket(heldStack)) {
                FluidStack fluid = FluidBucketSupport.getFluidFromBucket(heldStack);
                if (!fluid.isEmpty()) {
                    mixCutEntity.getFluidTank().setFluid(new FluidStack(fluid.getFluid(), mixCutEntity.getFluidTank().getCapacity()));
                    if (!player.isCreative()) {
                        player.setItemInHand(hand, new ItemStack(net.minecraft.world.item.Items.BUCKET));
                    }
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.CONSUME;
                }
            }

            // 2. 空桶交互：≥500mB时装桶，否则清空
            if (heldStack.is(net.minecraft.world.item.Items.BUCKET)) {
                int fluidAmount = mixCutEntity.getFluidTank().getFluidAmount();
                if (fluidAmount > 0) {
                    FluidStack tankFluid = mixCutEntity.getFluidTank().getFluid();
                    if (fluidAmount >= 500) {
                        // 装桶成功，生成装满的桶
                        ItemStack filledBucket = FluidBucketSupport.createFilledBucket(tankFluid.getFluid());
                        if (!filledBucket.isEmpty()) {
                            mixCutEntity.getFluidTank().drain(fluidAmount, IFluidHandler.FluidAction.EXECUTE);
                            if (!player.isCreative()) {
                                heldStack.shrink(1);
                                if (heldStack.isEmpty()) {
                                    player.setItemInHand(hand, filledBucket);
                                } else if (!player.getInventory().add(filledBucket)) {
                                    player.drop(filledBucket, false);
                                }
                            }
                            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BUCKET_FILL, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                    } else {
                        // 不足500mB，清空流体
                        mixCutEntity.getFluidTank().drain(fluidAmount, IFluidHandler.FluidAction.EXECUTE);
                    }

                    return InteractionResult.CONSUME;
                }
            }

            // 3. 处理其他模组流体桶（通过流体能力）
            // 关键修正：明确使用 IFluidHandlerItem 类型，不向上转型为 IFluidHandler
            heldStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(fluidHandler -> {
                // fluidHandler 直接是 IFluidHandlerItem 类型，无需转换
                FluidStack bucketFluid = fluidHandler.getFluidInTank(0);

                // 3.1 桶内有流体，倒入储罐
                if (!bucketFluid.isEmpty() && FluidBucketSupport.isSupportedFluid(bucketFluid.getFluid())) {
                    int filled = mixCutEntity.getFluidTank().fill(bucketFluid, IFluidHandler.FluidAction.EXECUTE);
                    if (filled > 0) {
                        fluidHandler.drain(filled, IFluidHandler.FluidAction.EXECUTE); // 子接口可直接调用父接口方法
                        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        // 更新玩家手中的物品（子接口特有方法 getContainer()）
                        player.setItemInHand(hand, fluidHandler.getContainer());
                    }
                }
                // 3.2 桶为空，尝试装流体（使用子接口实例直接传入，利用向上转型特性）
                else if (bucketFluid.isEmpty() && FluidBucketSupport.isSupportedFluidHandler(fluidHandler)) {
                    FluidStack tankFluid = mixCutEntity.getFluidTank().getFluid();
                    if (!tankFluid.isEmpty() && FluidBucketSupport.isSupportedFluid(tankFluid.getFluid())) {
                        int amount = mixCutEntity.getFluidTank().getFluidAmount();
                        if (amount >= 500) {
                            FluidStack toFill = new FluidStack(tankFluid.getFluid(), 500);
                            int filled = fluidHandler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                            if (filled > 0) {
                                mixCutEntity.getFluidTank().drain(filled, IFluidHandler.FluidAction.EXECUTE);
                                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                                // 更新玩家手中的物品
                                player.setItemInHand(hand, fluidHandler.getContainer());
                            }
                        }
                    }
                }
            });

            // 5. 正确调用 NetworkHooks.openScreen：第三个参数用Consumer写入BlockPos
            NetworkHooks.openScreen(serverPlayer, mixCutEntity, buf -> {
                buf.writeBlockPos(pos); // 写入位置
                // 写入流体数据
                CompoundTag fluidTag = new CompoundTag();
                CompoundTag fluidTankTag = new CompoundTag();
                mixCutEntity.getFluidTank().writeToNBT(fluidTankTag);
                fluidTag.put("MixCutTable_FluidTank", fluidTankTag);
                buf.writeNbt(fluidTag);
            });

            return InteractionResult.CONSUME;
        }

        // 客户端返回SUCCESS（不影响，仅标记交互成功）
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }


}