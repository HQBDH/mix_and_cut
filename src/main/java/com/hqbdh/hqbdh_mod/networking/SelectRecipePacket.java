package com.hqbdh.hqbdh_mod.networking;

import com.hqbdh.hqbdh_mod.block.entity.MixCutTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// 选择配方的数据包（客户端→服务端）
public class SelectRecipePacket {
    private final BlockPos blockPos; // 方块位置
    private final String recipeId;   // 选中的配方ID

    public SelectRecipePacket(BlockPos blockPos, String recipeId) {
        this.blockPos = blockPos;
        this.recipeId = recipeId;
    }

    // 编码：将数据写入缓冲区（客户端发送时调用）
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos); // 写入方块位置
        buf.writeUtf(recipeId);     // 写入配方ID
    }

    // 解码：从缓冲区读取数据（服务端接收时调用）
    public SelectRecipePacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.recipeId = buf.readUtf();
    }

    // 处理：服务端接收后执行的逻辑
    public static void handle(SelectRecipePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> { // 在主线程执行
            // 获取发送数据包的玩家
            var player = ctx.get().getSender();
            if (player == null) return;

            // 从服务端世界中获取方块实体
            var level = player.level();
            var blockEntity = level.getBlockEntity(packet.blockPos);
            if (blockEntity instanceof MixCutTableBlockEntity mixEntity) {
                // 更新方块实体的选中配方
                mixEntity.selectRecipe(packet.recipeId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}