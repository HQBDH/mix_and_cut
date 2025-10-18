package com.hqbdh.hqbdh_mod.screen;

import com.hqbdh.hqbdh_mod.menu.MixCutTableMenu;
import com.hqbdh.hqbdh_mod.registry.FluidColorRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
//网络数据包导入
import com.hqbdh.hqbdh_mod.networking.Networking; // 导入自定义的Networking类
import com.hqbdh.hqbdh_mod.networking.SelectRecipePacket; // 导入配方选择数据包类
//用于导入自定义配方
import java.util.List;
import com.hqbdh.hqbdh_mod.recipe.MixCutRecipe;

import static com.hqbdh.hqbdh_mod.util.GuiConstants.*;

public class MixCutTableScreen extends AbstractContainerScreen<MixCutTableMenu> {
    private static final int DEBUG_BORDER_COLOR_RED = 0xFFFF0000;      // 红色 - 默认
    private static final int DEBUG_BORDER_COLOR_GREEN = 0xFF00FF00;    // 绿色 - 悬停
    private static final int DEBUG_BORDER_COLOR_YELLOW = 0xFFFFFF00;   // 黄色 - 滑块轨道
    private static final int DEBUG_BORDER_COLOR_PURPLE = 0xFFFF00FF;   // 紫色 - 输入槽
    private static final int DEBUG_BORDER_COLOR_CYAN = 0xFF00FFFF;     // 青色 - 输出槽

    private static final ResourceLocation TEXTURE = new ResourceLocation("mixing_cutting:textures/gui/container/mix_gui.png");//GUI大致纹理
    private static final ResourceLocation BG_TEXTURE = new ResourceLocation("mixing_cutting:textures/gui/mix_cut_table.png");//配方槽背景
   //滑块贴图
    private static final ResourceLocation SLIDER_TEXTURE = new ResourceLocation("mixing_cutting:textures/gui/container/slider.png");//滑块贴图

    private static final ResourceLocation FLUID_OVERLAY = new ResourceLocation("mixing_cutting:textures/gui/fluid_overlay.png");//流体层颜色
//输入槽
private static final ResourceLocation INPUT_SLOT_TEXTURE = new ResourceLocation("mixing_cutting:textures/gui/container/thing_design.png");
//背包27槽
    private static final ResourceLocation PLAYER_BACKPACK_TEXTURE = new ResourceLocation("mixing_cutting:textures/gui/container/thing_design.png");
    //快捷栏槽
    private static final ResourceLocation HOTBAR_TEXTURE = new ResourceLocation("mixing_cutting:textures/gui/container/thing_design.png");
    // 添加流体区域变量
    private int fluidTankX, fluidTankY, fluidTankWidth, fluidTankHeight;
    //配方浏览槽的
    private static final ResourceLocation PLAYER_BACKPACK_TEXTURE_1 = new ResourceLocation("mixing_cutting:textures/gui/container/recipes_design.png");

    private int recipeDisplayCount = 9; // 配方槽显示数量（3×3网格）
    private boolean isDraggingSlider = false; // 滑块是否正在被拖动
//滑块区域常量
    private static final int SLIDER_AREA_OFFSET_X = 135; // 滑块相对于GUI左上角的X偏移
    private static final int SLIDER_AREA_OFFSET_Y = 20; // 滑块相对于GUI左上角的Y偏移 (顶部)
    private static final int SLIDER_TOTAL_HEIGHT = 54; // 滑块可移动的总高度 (与流体刻度高度一致)
    private static final int SLIDER_HANDLE_HEIGHT = 10; // 滑块把手的高度
    private static final int SLIDER_HANDLE_WIDTH = 16; // 滑块把手的宽度
    private static final int SLIDER_STP_X = 132; // 滑块同用偏移量横
    private static final int SLIDER_STP_Y = 132; // 滑块同用偏移量横
    public MixCutTableScreen(MixCutTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 186;
        this.imageHeight = 166;

        // 初始化流体区域尺寸（与渲染的流体柱一致）
        this.fluidTankWidth = 7;  // 从65到72，宽度7像素
        this.fluidTankHeight = 54; // 刻度线总高度
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        //渲染输入槽
        RenderSystem.setShader(GameRenderer::getPositionTexShader); // 切换到2D纹理着色器
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // 重置颜色为“白色、不透明”
        RenderSystem.setShaderTexture(0, INPUT_SLOT_TEXTURE); // 切换到输入槽纹理

        int inputSlotStartX = x + 8;  // 输入槽起始 X 坐标（与 Menu 中 INPUT_SLOT_START_X 对应）
        int inputSlotStartY = y + 18; // 输入槽起始 Y 坐标（与 Menu 中 INPUT_SLOT_START_Y 对应）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotX = inputSlotStartX + col * 18; // 单个槽位 X 坐标（每列间隔 18 像素）
                int slotY = inputSlotStartY + row * 18; // 单个槽位 Y 坐标（每行间隔 18 像素）
                guiGraphics.blit(INPUT_SLOT_TEXTURE, slotX, slotY, 0, 0, 18, 18); // 绘制 18×18 纹理
            }
        }

        // 更新流体区域位置（用于悬停检测）
        this.fluidTankX = x + 65;
        this.fluidTankY = y + 20;

        // 渲染其他背景元素...
      //  RenderSystem.setShaderTexture(0, new ResourceLocation("minecraft:textures/gui/container/generic_54.png"));
//背包槽绘制
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, PLAYER_BACKPACK_TEXTURE);
        int playerBackpackStartX = x+PLAYER_INV_START_X-18; // 背包起始X（复用Menu中定义的常量）
        int playerBackpackStartY = y + PLAYER_INV_START_Y; // 背包起始Y
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = playerBackpackStartX + col * 18;
                int slotY = playerBackpackStartY + row * 18;

                int borderColor = 0xFF000000;//黑色
                int borderColor_1 = 0xFF666666; // 使用40%灰色边框
                int borderColor_2 = 0xFFFFFFFF;//使用白色
                //上边框黑色部分
         //       guiGraphics.fill(slotX-1, slotY-1, slotX + 18, slotY, borderColor);
                //上边框灰色部分
           //     guiGraphics.fill(slotX+18, slotY-1, slotX + 19, slotY, borderColor_1);

                // 下边框白色部分
             //   guiGraphics.fill(slotX, slotY + 18, slotX + 19, slotY + 19, borderColor_2);
                //下边框灰色部分
             //   guiGraphics.fill(slotX-1, slotY + 18, slotX, slotY + 19, borderColor_1);

                // 左边框黑色部分
               // guiGraphics.fill(slotX-1, slotY, slotX, slotY + 18, borderColor);
                // 右边框白色部分
                //guiGraphics.fill(slotX + 18, slotY, slotX + 19, slotY + 18, borderColor_2);

                guiGraphics.blit(PLAYER_BACKPACK_TEXTURE, slotX, slotY, 0, 0, 18, 18); // 绘制18×18的背包槽位纹理
            }
        }

        // ========== 新增：渲染玩家快捷栏自定义背景 ==========，快捷栏绘制
        RenderSystem.setShaderTexture(0, HOTBAR_TEXTURE);
        int hotbarStartX = x + PLAYER_INV_START_X-18; // 快捷栏起始X（与背包对齐）
        int hotbarStartY = y + HOTBAR_START_Y;     // 快捷栏起始Y（复用Menu中定义的常量）
        for (int col = 0; col < 9; col++) {
            int slotX = hotbarStartX + col * 18;
            int slotY = hotbarStartY;
            guiGraphics.blit(HOTBAR_TEXTURE, slotX, slotY, 0, 0, 18, 18); // 绘制18×18的快捷栏槽位纹理

        }
        // 渲染结果槽背景，
        int resultX = x + 75;
        int resultY = y + 19;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotX = resultX + col * 18;
                int slotY = resultY + row * 18;

                guiGraphics.blit(new ResourceLocation("mixing_cutting:textures/gui/container/recipes_design.png"),
                        slotX, slotY, 0, 0, 18, 18);
            }
        }

        // 渲染滑块（采用动态绘制）
        //      RenderSystem.setShaderTexture(0, SLIDER_TEXTURE);
    //    int sliderX = x + 140;
      //  int sliderY = y + 30;
       // guiGraphics.blit(SLIDER_TEXTURE, sliderX, sliderY, 0, 0, 16, 10);


        // 渲染输出槽背景,现在还是原版
        int outputX =  x+OUTPUT_SLOT_X;
        int outputY =  y+OUTPUT_SLOT_Y;
        guiGraphics.blit(new ResourceLocation("mixing_cutting:textures/gui/container/thing_design.png"),
                outputX, outputY, 0, 0, 18, 18);

        // 渲染刻度线
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int scaleX = x + 66;
        int scaleStartY = y + 20;
        int scaleEndY = y + 20 + 54;
        for (int i = 0; i <= 9; i++) {//10个太长，9个就可以
            int scaleY = scaleStartY + (scaleEndY - scaleStartY) * i / 10;
            guiGraphics.fill(scaleX, scaleY, scaleX + 2, scaleY + 1, 0xFFFFFFFF);
        }

        // 渲染流体柱
        FluidStack fluid = menu.getBlockEntity().getFluidTank().getFluid();
        if (!fluid.isEmpty()) {
            Fluid fluidType = fluid.getFluid();
            int tankCapacity = menu.getBlockEntity().getFluidTank().getCapacity();
            int fluidAmount = fluid.getAmount();
            int fluidHeight = (int) (54 * ((float) fluidAmount / tankCapacity));
            int fluidDrawY = y + 20 + (54 - fluidHeight);
            int tankBottomY = y + 20 + 54;

            int color = FluidColorRegistry.getColor(fluid);
            guiGraphics.fill(x + 65, fluidDrawY, x + 72, tankBottomY, color | 0xFF000000);
        }

        // 新增：渲染配方槽中的可用配方（在结果槽背景之上）↓
        renderAvailableRecipes(guiGraphics, x, y);

        // 新增：渲染动态滑块（在配方槽右侧）↓
        renderSlider(guiGraphics, x, y);
        // 重置纹理和颜色
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 先渲染普通的tooltip
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // 然后单独渲染我们的流体tooltip，避免与EMI冲突
        renderFluidTooltip(guiGraphics, mouseX, mouseY);
   //     renderDebugBorders(guiGraphics, mouseX, mouseY);//测试用的
    }

    // 新增独立的流体tooltip渲染方法，避免与EMI的Mixin冲突
    protected void renderFluidTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isMouseOverFluidTank(mouseX, mouseY)) {
            FluidStack fluid = menu.getBlockEntity().getFluidTank().getFluid();
            if (!fluid.isEmpty()) {
                Component fluidName = fluid.getDisplayName();
                int amount = fluid.getAmount();
                int capacity = menu.getBlockEntity().getFluidTank().getCapacity();

                MutableComponent tooltip = Component.translatable("tooltip.mixing_cutting.fluid_tank.info",
                        fluidName, amount, capacity);

                // 直接渲染tooltip，避免调用可能被EMI修改的方法链
                guiGraphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
            } else {
                Component emptyTooltip = Component.translatable("tooltip.mixing_cutting.fluid_tank.empty");
                guiGraphics.renderTooltip(this.font, emptyTooltip, mouseX, mouseY);
            }
        }
    }

    // 检测鼠标是否在流体区域上
    private boolean isMouseOverFluidTank(int mouseX, int mouseY) {
        return mouseX >= fluidTankX && mouseX < fluidTankX + fluidTankWidth &&
                mouseY >= fluidTankY && mouseY < fluidTankY + fluidTankHeight;
    }
    // 新增：将匹配的配方结果渲染到3×3配方槽中
    private void renderAvailableRecipes(GuiGraphics guiGraphics, int x, int y) {
        // 若方块实体为空，直接返回（避免空指针）
        if (menu.getBlockEntity() == null) return;

        // 从方块实体获取所有匹配的配方
        List<MixCutRecipe> availableRecipes = menu.getBlockEntity().getAvailableRecipes();
        // 从菜单获取当前滚动偏移（控制显示哪一页配方）
        int startIndex = menu.getScrollOffset();

        // 配方槽的起始位置（与现有结果槽背景位置一致）
        int recipeSlotStartX = x + 76;
        int recipeSlotStartY = y + 20;

        // 遍历3×3配方槽，渲染对应配方的结果物品
        for (int i = 0; i < recipeDisplayCount; i++) {
            int recipeIndex = startIndex + i; // 当前要渲染的配方索引（滚动偏移+槽位索引）
            // 计算单个配方槽的坐标（每行3个，间隔18像素）
         int slotX = recipeSlotStartX + (i % 3) * 18; // 列：0→0,1→18,2→36
           int slotY = recipeSlotStartY + (i / 3) * 18; // 行：0→0,3→18,6→36

            // 若配方索引有效（未超出总配方数），渲染配方结果物品
           if (recipeIndex < availableRecipes.size()) {
                MixCutRecipe recipe = availableRecipes.get(recipeIndex);
                // 获取配方的结果物品（null表示用默认注册表）
                ItemStack resultItem = recipe.getResultItem(null).copy();

                // 渲染物品图标和数量（+1是为了与槽位背景对齐，避免重叠）
                guiGraphics.renderItem(resultItem, slotX + 1, slotY + 1);
                guiGraphics.renderItemDecorations(this.font, resultItem, slotX + 1, slotY + 1);
           }
        }

    }

    // 新增：渲染滑块（位置由滚动偏移动态计算）
    private void renderSlider(GuiGraphics guiGraphics, int x, int y) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, SLIDER_TEXTURE);

        // 滑块的固定X坐标（配方槽右侧）
        int sliderX = x + 130;
        // 滑块的动态Y坐标（调用方法计算，随滚动偏移变化）
        int sliderY = y + 19 + calculateSliderPosition();
        //将渲染左边修改都减小向左上

        // 渲染滑块纹理（尺寸16×10，与纹理文件一致）
        guiGraphics.blit(SLIDER_TEXTURE, sliderX, sliderY, 0, 0, 16, 10);
    }
    private int calculateSliderPosition() {
        // 1. 从菜单获取“当前匹配的总配方数”
        int totalRecipes = menu.getAvailableRecipeCount();
        // 2. 若配方数 ≤ 显示数（3×3=9），无需滚动，滑块固定在顶部（偏移0）
        if (totalRecipes <= recipeDisplayCount) {
            return 0;
        }

        // 3. 计算“最大滚动偏移”（总配方数 - 显示数，避免滚动超出范围）
        int maxScrollOffset = totalRecipes - recipeDisplayCount;
        // 4. 从菜单获取“当前滚动偏移”
        int currentScrollOffset = menu.getScrollOffset();
        // 5. 计算滚动进度（0→顶部，1→底部，用float保证精度）
        float scrollProgress = (float) currentScrollOffset / maxScrollOffset;

        // 6. 滑块可移动的总范围（40像素，可根据你的GUI调整，需与滑块区域高度匹配）
        int sliderMoveRange = 45;
        // 7. 返回滑块的Y轴偏移量（进度 × 总范围）
        return (int) (scrollProgress * sliderMoveRange);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 1. 若点击配方槽，处理配方选择
        if (isMouseOverRecipeSlot(mouseX, mouseY)) {
            handleRecipeSlotClick(mouseX, mouseY);
            return true; // 拦截事件，避免与其他点击冲突
        }

        // 2. 若点击滑块，标记为“正在拖动”
        if (isMouseOverSlider(mouseX, mouseY)) {
            isDraggingSlider = true;
            return true;
        }

        // 3. 若点击其他区域，执行默认逻辑（如物品槽点击）
        return super.mouseClicked(mouseX, mouseY, button);
    }
    // 新增：重写鼠标释放事件，结束滑块拖动
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // 若之前正在拖动滑块，释放后标记为“停止拖动”
        if (isDraggingSlider) {
            isDraggingSlider = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    // 新增：重写鼠标拖动事件，更新滑块位置和滚动偏移
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // 若正在拖动滑块，计算新的滚动偏移
        if (isDraggingSlider) {
            handleSliderDrag(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    // 新增：点击配方槽时，选择对应配方
    private void handleRecipeSlotClick(double mouseX, double mouseY) {
        // 计算点击的配方索引 (这部分逻辑不变)
        int guiX = (this.width - this.imageWidth) / 2;
        int guiY = (this.height - this.imageHeight) / 2;
        int recipeSlotStartX = guiX + 76; // 您的代码是 82，保持一致
        int recipeSlotStartY = guiY + 19;
        int clickedCol = (int) ((mouseX - recipeSlotStartX) / 18);
        int clickedRow = (int) ((mouseY - recipeSlotStartY) / 18);
        int slotIndex = clickedRow * 3 + clickedCol;
        int recipeIndex = menu.getScrollOffset() + slotIndex;

        // 获取客户端的可用配方列表
        List<MixCutRecipe> availableRecipes = menu.getBlockEntity().getAvailableRecipes();

        // 如果点击的配方有效
        if (recipeIndex < availableRecipes.size()) {
            MixCutRecipe selectedRecipe = availableRecipes.get(recipeIndex);
            String recipeId = selectedRecipe.getRecipeId();//获取配方字段
            // 【核心修改】创建并发送数据包到服务端
            Networking.CHANNEL.sendToServer(new SelectRecipePacket(
                    this.menu.getBlockEntity().getBlockPos(), // 方块实体的位置
                    selectedRecipe.getRecipeId()              // 选中的配方ID
            ));
            // 步骤 2: 立即在客户端本地执行一次，这是“预测更新”，用于消除UI延迟
            // 我们需要一个方法来触发这个本地更新，将在下一步添加到Menu中
            this.menu.selectRecipeClientSide(recipeId);
        }
    }
    // 新增：拖动滑块时，更新菜单的滚动偏移
    private void handleSliderDrag(double mouseY) {
        // GUI左上角坐标
        int guiX = (this.width - this.imageWidth) / 2;
        int guiY = (this.height - this.imageHeight) / 2;

        // 滑块可拖动的区域范围（顶部Y=30，底部Y=30+40=70）
        int sliderAreaTop = guiY + 24;
        int sliderAreaBottom = guiY + 20 + 44;

        // 计算拖动进度（0→顶部，1→底部，超出范围则钳位）
        float dragProgress = (float) (mouseY - sliderAreaTop) / (sliderAreaBottom - sliderAreaTop);
        dragProgress = Math.max(0, Math.min(1, dragProgress)); // 确保进度在0-1之间

        // 计算新的滚动偏移
        int totalRecipes = menu.getAvailableRecipeCount();
        int maxScrollOffset = Math.max(0, totalRecipes - recipeDisplayCount); // 最大滚动偏移
        int newScrollOffset = (int) (dragProgress * maxScrollOffset); // 新滚动偏移

        // 通过菜单设置滚动偏移（菜单会通知方块实体）
        menu.setScrollOffset(newScrollOffset);
    }
    // 新增：检测鼠标是否在配方槽区域
    private boolean isMouseOverRecipeSlot(double mouseX, double mouseY) {
        int guiX = (this.width - this.imageWidth) / 2;
        int guiY = (this.height - this.imageHeight) / 2;

        // 配方槽区域：X=82→82+54（3×18），Y=20→20+54
        int recipeAreaX1 = guiX + 76;
        int recipeAreaX2 = guiX + 76 + 53;
        int recipeAreaY1 = guiY + 20;
        int recipeAreaY2 = guiY + 20 + 53;

        return mouseX >= recipeAreaX1 && mouseX < recipeAreaX2 &&
                mouseY >= recipeAreaY1 && mouseY < recipeAreaY2;
    }

    // 新增：检测鼠标是否在滑块区域
    private boolean isMouseOverSlider(double mouseX, double mouseY) {
        int guiX = (this.width - this.imageWidth) / 2;
        int guiY = (this.height - this.imageHeight) / 2;

        // 滑块区域：X=140→140+16，Y=动态计算（随滑块位置变化）
        int sliderX1 = guiX + 130;;
        int sliderX2 = guiX + 130 + SLIDER_HANDLE_WIDTH;
        int sliderY1 = guiY + 19 + calculateSliderPosition();
        int sliderY2 = guiY + 20 + calculateSliderPosition() + SLIDER_HANDLE_HEIGHT;

        return mouseX >= sliderX1 && mouseX < sliderX2 &&
                mouseY >= sliderY1 && mouseY < sliderY2;
    }



}