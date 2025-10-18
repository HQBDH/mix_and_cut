package com.hqbdh.hqbdh_mod.recipe;



import com.hqbdh.hqbdh_mod.hqbdhMod;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, hqbdhMod.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, hqbdhMod.MODID);

    // 注册配方类型
    public static final RegistryObject<RecipeType<MixCutRecipe>> MIX_CUT_TYPE =
            RECIPE_TYPES.register("mix_cutting", () -> new RecipeType<MixCutRecipe>() {
                @Override
                public String toString() {
                    return "mix_cutting";
                }
            });


    // 注册配方序列化器
    public static final RegistryObject<RecipeSerializer<MixCutRecipe>> MIX_CUT_SERIALIZER =
            SERIALIZERS.register("mix_cutting", () -> new MixCutRecipe.Serializer());

}