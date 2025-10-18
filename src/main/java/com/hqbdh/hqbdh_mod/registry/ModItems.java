package com.hqbdh.hqbdh_mod.registry;

import com.hqbdh.hqbdh_mod.hqbdhMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// 物品注册类
public class ModItems {
    // 定义物品延迟注册器
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, hqbdhMod.MODID);

    // 注册“混合切制机”物品（关联方块，ID与方块一致）
    public static final RegistryObject<Item> MIX_CUT_TABLE = ITEMS.register("mix_cut_table",
            () -> new BlockItem(ModBlocks.MIX_CUT_TABLE.get(),  // 绑定到对应的方块
                    new Item.Properties()  // 物品属性（无特殊属性）
            ));
}