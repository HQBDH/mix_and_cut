package com.hqbdh.hqbdh_mod;

import com.hqbdh.hqbdh_mod.registry.*;
import com.hqbdh.hqbdh_mod.util.FluidBucketSupport;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
//这个是物品栈类
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.hqbdh.hqbdh_mod.screen.MixCutTableScreen; // 新增导入
import net.minecraft.client.gui.screens.MenuScreens; // 新增导入
import net.minecraftforge.api.distmarker.Dist; // 新增导入
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent; // 新增导入
import com.hqbdh.hqbdh_mod.recipe.ModRecipes; // 新增导入
import com.hqbdh.hqbdh_mod.registry.FluidColorRegistry; // 确保这个导入存在
import com.hqbdh.hqbdh_mod.networking.Networking; // 关键：导入自定义的Networking类

import com.hqbdh.hqbdh_mod.screen.MixCutTableScreen; // 新增导入
import net.minecraft.client.gui.screens.MenuScreens; // 新增导入
import net.minecraftforge.api.distmarker.Dist; // 新增导入
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent; // 新增导入
import com.hqbdh.hqbdh_mod.recipe.ModRecipes; // 新增导入
import com.hqbdh.hqbdh_mod.registry.FluidColorRegistry; // 确保这个导入存在
import com.hqbdh.hqbdh_mod.networking.Networking; // 关键：导入自定义的Networking类

import java.util.List;

// 模组主类（MODID需与资源文件命名空间一致，即"mixing_cutting"）
@Mod(hqbdhMod.MODID)
public class hqbdhMod {
    public static final String MODID = "mixing_cutting";  // 必须与资源文件夹"assets/mixing_cutting"一致,这个是模组主类

    public hqbdhMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModCreativeTab.register(modEventBus); // 注册创造模式物品栏
        // 注册方块和物品
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModRecipes.SERIALIZERS.register(modEventBus);//配方初始化
        ModRecipes.RECIPE_TYPES.register(modEventBus);//配方初始化
        // 初始化“流体桶支持”（加载所有支持的流体-桶映射）
        FluidBucketSupport.init();
        // 注册事件总线
        MinecraftForge.EVENT_BUS.register(this);
        // 注册客户端设置事件监听器
        Networking.registerPackets();
        modEventBus.addListener(this::clientSetup);//这个调用的是后者private，为什么
        modEventBus.addListener(this::commonSetup);
    }
    private void clientSetup(final FMLClientSetupEvent event) {
        // 在专门的线程中执行屏幕注册
        event.enqueueWork(() -> {
            // 将菜单类型与屏幕类绑定
            MenuScreens.register(ModMenus.MIX_CUT_TABLE.get(), MixCutTableScreen::new);

            // ▼▼▼【 新增代码从这里开始 】▼▼▼
            // 1. 创建一个新的配方书分类
            //    第一个参数是分类的ID（自定义），第二个参数是分类标签页显示的图标
            //RecipeBookCategories MIXING_CATEGORY = RecipeBookCategories.create("MIX_CUTTING", new ItemStack(ModItems.mix_cut_table.get())); // TODO: 将 YOUR_ICON_ITEM 替换成一个能代表你功能的物品
            // 2. 将你的配方类型与新创建的分类以及原版的“合成”分类关联起来
            //    这样你的配方会同时出现在这两个标签页下
            //  RecipeBookRegistry.addCategories(ModRecipes.MIX_CUT_TYPE.get(), List.of(RecipeBookCategories.CRAFTING, MIXING_CATEGORY));
            // 3. (可选) 如果你只想让配方显示在“合成”标签页，可以这样写：
            // RecipeBookRegistry.addCategories(ModRecipes.MIX_CUT_TYPE.get(), List.of(RecipeBookCategories.CRAFTING));
            // ▲▲▲【 新增代码到这里结束 】▲▲▲
        });
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // System.out.println("[模组初始化] 开始通用设置");

        // 使用 enqueueWork 确保在主线程执行
        event.enqueueWork(() -> {
            FluidColorRegistry.init(); // 调用流体颜色注册表的初始化方法
            //   System.out.println("[模组初始化] 流体颜色注册表初始化完成");
            Networking.registerPackets();
            // System.out.println("[DEBUG] 网络包注册完成 | MOD_ID=" + MODID);
        });

        // System.out.println("[模组初始化] 通用设置完成");
    }

}