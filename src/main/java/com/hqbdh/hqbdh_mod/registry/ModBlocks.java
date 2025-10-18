package com.hqbdh.hqbdh_mod.registry;

import com.hqbdh.hqbdh_mod.hqbdhMod;  // 主类（后续创建）
import com.hqbdh.hqbdh_mod.block.MixCutTableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.Direction; // 导入此行

// 方块注册类
public class ModBlocks {
    // 定义方块延迟注册器（关联MODID）
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, hqbdhMod.MODID);

    // 注册“混合切制机”方块（ID：mix_cut_table）
    public static final RegistryObject<Block> MIX_CUT_TABLE = BLOCKS.register("mix_cut_table",
            MixCutTableBlock::new);  // 关联方块类
}