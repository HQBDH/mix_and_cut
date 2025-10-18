package com.hqbdh.hqbdh_mod.networking;

import com.hqbdh.hqbdh_mod.hqbdhMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

// 网络注册工具类：管理网络通道和数据包
public class Networking {
    // 定义网络通道（MODID必须与你的模组ID一致，版本号用于兼容检查）
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(hqbdhMod.MODID, "main"), // 通道唯一标识：modid:main
            () -> PROTOCOL_VERSION, // 客户端支持的版本
            PROTOCOL_VERSION::equals, // 服务端接受的版本
            PROTOCOL_VERSION::equals  // 客户端接受的版本
    );

    // 注册所有数据包（在模组初始化时调用）
    public static void registerPackets() {
        // 数据包ID（从0开始，每个包唯一）
        int id = 0;

        // 注册“选择配方”数据包（假设你已创建 SelectRecipePacket 类）
        CHANNEL.messageBuilder(SelectRecipePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SelectRecipePacket::toBytes) // 编码方法（客户端→服务端）
                .decoder(SelectRecipePacket::new)    // 解码方法（服务端接收）
                .consumerMainThread(SelectRecipePacket::handle) // 处理方法（主线程执行）
                .add();

        // 如需其他数据包（如更新流体、同步数据等），按上述格式继续注册
        // CHANNEL.messageBuilder(OtherPacket.class, id++, ...).add();
    }
}