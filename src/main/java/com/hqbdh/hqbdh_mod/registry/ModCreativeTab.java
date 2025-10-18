package com.hqbdh.hqbdh_mod.registry;


import com.hqbdh.hqbdh_mod.hqbdhMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModCreativeTab {
    // 1. 创建一个 CreativeModeTab 的 DeferredRegister
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, hqbdhMod.MODID);

    // 2. 定义物品栏的翻译键 (用于语言文件)
    public static final String MIXING_CUTTING_TAB_STRING = "creativetab.mixing_cutting_tab";

    // 3. 注册你的物品栏
    public static final Supplier<CreativeModeTab> MIXING_CUTTING_TAB = CREATIVE_MODE_TABS.register("mixing_cutting_tab", () -> CreativeModeTab.builder()
            // .withTabsBefore(CreativeModeTabs.COMBAT) // 可选：让你的标签页出现在“战斗”标签页之前
            .title(Component.translatable(MIXING_CUTTING_TAB_STRING)) // 设置标题，使用上面的翻译键

            .icon(() -> ModItems.MIX_CUT_TABLE.get().getDefaultInstance()) // 设置图标为 mix_cut_table 物品

            .displayItems((pParameters, pOutput) -> {
                // 在这里添加所有你想在这个物品栏中显示的物品
                pOutput.accept(ModItems.MIX_CUT_TABLE.get());

                // 示例：如果你还有其他物品，也可以像这样添加
                // pOutput.accept(ModItems.ANOTHER_ITEM.get());
                // pOutput.accept(ModBlocks.ANOTHER_BLOCK.get());

            })
            .build());


    // 4. 注册到事件总线的方法
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
