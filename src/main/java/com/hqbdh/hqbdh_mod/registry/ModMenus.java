package com.hqbdh.hqbdh_mod.registry;

import com.hqbdh.hqbdh_mod.hqbdhMod;
import com.hqbdh.hqbdh_mod.menu.MixCutTableMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, hqbdhMod.MODID);

    public static final RegistryObject<MenuType<MixCutTableMenu>> MIX_CUT_TABLE =
            MENUS.register("mix_cut_table", () -> {
                // 1. 先创建 MenuType 实例（与原逻辑一致）
                MenuType<MixCutTableMenu> menuType = IForgeMenuType.create(
                        (windowId, inv, data) -> new MixCutTableMenu(windowId, inv, data)
                );
                // 2. 插入日志：打印注册的菜单类型（含ID和类信息）
           //     System.out.println("[菜单注册] 已成功注册菜单类型！ID：mix_cut_table，类型：" + menuType);
                // 3. 返回创建好的实例（完成注册）
                return menuType;
            });
}