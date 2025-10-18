package com.hqbdh.hqbdh_mod.registry;

import com.hqbdh.hqbdh_mod.block.entity.MixCutTableBlockEntity;
import com.hqbdh.hqbdh_mod.hqbdhMod;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.core.Direction; // 导入此行

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, hqbdhMod.MODID);

    // 注册“混合切制机”方块实体
    public static final RegistryObject<BlockEntityType<MixCutTableBlockEntity>> MIX_CUT_TABLE =
            BLOCK_ENTITIES.register(
                    "mix_cut_table",
                    () -> BlockEntityType.Builder.of(
                            MixCutTableBlockEntity::new, // 实体构造器
                            ModBlocks.MIX_CUT_TABLE.get() // 绑定的方块（必须是你的方块）
                    ).build(null)
            );
}