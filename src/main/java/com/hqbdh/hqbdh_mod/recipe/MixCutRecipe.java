package com.hqbdh.hqbdh_mod.recipe;

//注册 “混合切制” 配方类型，定义配方的数据结构（输入、流体、输出），
// 实现配方的序列化 反序列化（让游戏能加载自定义格式的配方文件
//package com.hqbdh.hqbdh_mod.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;
import io.netty.handler.codec.DecoderException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.world.item.crafting.Ingredient; // 确保这个导入存在
import javax.annotation.Nullable;
import java.util.*;

public class MixCutRecipe implements Recipe<Container> {
    public static class CountedIngredient {
        public final Ingredient ingredient;
        public final int count;

        public CountedIngredient(Ingredient ingredient, int count) {
            this.ingredient = ingredient;
            this.count = count;
        }
    }
    // ▲▲▲ 【新增代码结束】 ▲▲▲
    private final ResourceLocation id;
    private final NonNullList<CountedIngredient> ingredients;//修改后带数量
    private final FluidStack fluidRequirement;
    private final ItemStack result;
    private final int processTime;
    private final String recipeId;

    public MixCutRecipe(ResourceLocation id, NonNullList<CountedIngredient> ingredients, FluidStack fluidRequirement, ItemStack result, int processTime, String recipeId) {
        // ▲▲▲ 【修改结束】 ▲▲▲
        this.id = id;
        this.ingredients = ingredients;
        this.fluidRequirement = fluidRequirement;
        this.result = result;
        this.processTime = processTime;
        this.recipeId = recipeId;
    }

    @Override
    public boolean matches(Container container, Level level) {
        //这个含义是
        // 基础匹配逻辑，具体实现在方块实体中
        return true;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MIX_CUT_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.MIX_CUT_TYPE.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        // 为了兼容原版接口，我们返回一个只包含Ingredient的列表
        // 但在我们自己的逻辑中，不会使用这个方法
        NonNullList<Ingredient> flattenedIngredients = NonNullList.create();
        for (CountedIngredient ci : this.ingredients) {
            flattenedIngredients.add(ci.ingredient);
        }
        return flattenedIngredients;
    }
    public NonNullList<CountedIngredient> getCountedIngredients() {
        return this.ingredients;
    }
    public FluidStack getFluidRequirement() {
        return fluidRequirement.copy();
    }

    public int getProcessTime() {
        return processTime;
    }

    public String getRecipeId() {
        return recipeId;
    }

    // 检查输入是否匹配此配方
    public boolean matchesInput(NonNullList<ItemStack> inputItems, FluidStack availableFluid) {
        // 检查流体
        if (!fluidRequirement.isEmpty()) {
            if (availableFluid.isEmpty() ||
                    !availableFluid.isFluidEqual(fluidRequirement) || // 正确：使用 isFluidEqual 比较流体类型
                    availableFluid.getAmount() < fluidRequirement.getAmount()) {
                return false;
            }
        } else {
            // 情况2：配方不需要流体 → 强制要求可用流体为空（否则不匹配）
            if (!availableFluid.isEmpty()) {
                return false; // 有任何流体（水/岩浆等），直接不匹配
            }
        }


        // 2. 【种类严格检查】确保所有输入物品的“种类”都在配方需求列表中
        for (ItemStack inputStack : inputItems) {
            if (inputStack.isEmpty()) {
                continue; // 跳过空格子
            }

            // 检查当前这个输入物品是否是配方需要的种类
            boolean isRequiredSpecies = false;
            for (CountedIngredient requirement : this.getCountedIngredients()) {
                if (requirement.ingredient.test(inputStack)) {
                    isRequiredSpecies = true;
                    break; // 找到了匹配的种类，可以停止检查这个物品，跳到下一个
                }
            }

            // 如果遍历完所有配方需求，当前物品的种类都不是配方所需要的，则匹配失败
            if (!isRequiredSpecies) {
                return false; // 发现多余种类的物品，匹配失败！
            }
        }

        // 3. 【数量宽松检查】如果所有种类都正确，再检查每种物品的数量是否足够
        // 遍历配方中的每一个原料需求
        for (CountedIngredient requirement : this.getCountedIngredients()) {
            int needed = requirement.count;
            int found = 0;

            // 累加所有输入物品中匹配当前需求的数量
            for (ItemStack inputStack : inputItems) {
                if (requirement.ingredient.test(inputStack)) {
                    found += inputStack.getCount();
                }
            }

            // 如果找到的总数小于需要的数量，则匹配失败
            if (found < needed) {
                return false; // 物品数量不足，匹配失败！
            }
        }

        // 4. 如果通过了“种类严格”和“数量宽松”的所有检查，则匹配成功！
        return true;
        // ▲▲▲ 【修改结束】 ▲▲▲
    }

    // 序列化器
    public static class Serializer implements RecipeSerializer<MixCutRecipe> {

        @Override
        public MixCutRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            // 解析输入物品
            NonNullList<CountedIngredient> ingredients = readCountedIngredients(GsonHelper.getAsJsonArray(json, "ingredients"));

            // 解析流体需求
            FluidStack fluidStack = FluidStack.EMPTY;
            if (json.has("fluid")) {
                JsonObject fluidJson = GsonHelper.getAsJsonObject(json, "fluid");
                String fluidId = GsonHelper.getAsString(fluidJson, "fluid");
                int amount = GsonHelper.getAsInt(fluidJson, "amount", 100);
                fluidStack = new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidId)), amount);
            }

            // 解析输出物品
            JsonObject resultJson = GsonHelper.getAsJsonObject(json, "result");
            ItemStack result = ShapedRecipe.itemStackFromJson(resultJson);
            //这里需要解析NBT类
            if (resultJson.has("nbt")) {
                try {
                    // 将NBT字符串（例如 "{Potion:\"minecraft:water\"}"）解析为CompoundTag
                    String nbtString = resultJson.get("nbt").getAsString();
                    CompoundTag nbt = TagParser.parseTag(nbtString);
                    // 将解析出的NBT附加到ItemStack上
                    result.setTag(nbt);
                } catch (CommandSyntaxException e) {
                    // 如果NBT格式错误，抛出异常，防止加载错误的配方
                    System.out.println("[DEBUG] 错误NBT啊");
                    throw new JsonParseException("Invalid NBT format for result item", e);

                }
            }
            // 解析处理时间
            int processTime = GsonHelper.getAsInt(json, "process_time", 200);

            // 解析配方ID
            String customRecipeId = GsonHelper.getAsString(json, "recipe_id", recipeId.toString());

            return new MixCutRecipe(recipeId, ingredients, fluidStack, result, processTime, customRecipeId);
        }

        private NonNullList<CountedIngredient> readCountedIngredients(JsonArray ingredientArray) {
            NonNullList<CountedIngredient> countedIngredients = NonNullList.create();
            for (int i = 0; i < ingredientArray.size(); i++) {
                JsonObject ingredientJson = ingredientArray.get(i).getAsJsonObject();
                // 先解析为普通的 Ingredient
                Ingredient ingredient = Ingredient.fromJson(ingredientJson);
                // 再读取 "count" 字段，如果不存在则默认为 1
                int count = GsonHelper.getAsInt(ingredientJson, "count", 1);

                if (!ingredient.isEmpty()) {
                    countedIngredients.add(new CountedIngredient(ingredient, count));
                }
            }
            return countedIngredients;
        }

        @Nullable
        @Override
        public MixCutRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            // 读取输入物品
            int ingredientCount = buffer.readVarInt();
            NonNullList<CountedIngredient> ingredients = NonNullList.withSize(ingredientCount, null);
            for (int i = 0; i < ingredientCount; i++) {
                Ingredient ing = Ingredient.fromNetwork(buffer);
                int count = buffer.readVarInt();
                ingredients.set(i, new CountedIngredient(ing, count));
            }

            // 读取流体
            FluidStack fluidStack = FluidStack.readFromPacket(buffer);

            // 读取输出物品
            ItemStack result = buffer.readItem();

            // 读取处理时间
            int processTime = buffer.readVarInt();

            // 读取配方ID
            String customRecipeId = buffer.readUtf();

            //ItemStack result = buffer.readItem();
            return new MixCutRecipe(recipeId, ingredients, fluidStack, result, processTime, customRecipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, MixCutRecipe recipe) {
            // 写入输入物品数量
            buffer.writeVarInt(recipe.ingredients.size());

            // 写入输入物品
        //    buffer.writeVarInt(recipe.ingredients.size());
            for (CountedIngredient ci : recipe.ingredients) {
                ci.ingredient.toNetwork(buffer);
                buffer.writeVarInt(ci.count);
            }

            // 写入流体
            recipe.fluidRequirement.writeToPacket(buffer);

            // 写入输出物品
            buffer.writeItem(recipe.result);

            // 写入处理时间
            buffer.writeVarInt(recipe.processTime);

            // 写入配方ID
            buffer.writeUtf(recipe.recipeId);
        }
    }

}