package com.hqbdh.hqbdh_mod.util;


// GUI 公共常量类：管理 Menu 和 Screen 共用的坐标、尺寸
public class GuiConstants {
    // 1. 混合切制机 GUI 整体尺寸（与 Screen 中的 imageWidth/imageHeight 一致）
    public static final int GUI_WIDTH = 186;
    public static final int GUI_HEIGHT = 166;

    // 2. 输入槽坐标（3×3）
    public static final int INPUT_SLOT_START_X = 1;  // 起始X
    public static final int INPUT_SLOT_START_Y = 25;  // 起始Y
    public static final int SLOT_SIZE = 18;           // 单个槽位尺寸（Minecraft标准18×18像素）

    // 3. 输出槽坐标
    public static final int OUTPUT_SLOT_X = 146;
    public static final int OUTPUT_SLOT_Y = 53;

    // 4. 玩家物品栏坐标
    public static final int PLAYER_INV_START_X = 26;
    public static final int PLAYER_INV_START_Y = 84;

    // 5. 玩家快捷栏坐标
    public static final int HOTBAR_START_Y = 142;

    // 6. 含水量刻度线坐标
    public static final int SCALE_LINE_X = 82;        // 刻度线X位置
    public static final int SCALE_START_Y = 20;       // 刻度线起始Y
    public static final int SCALE_END_Y = 74;         // 刻度线结束Y（20 + 3×18 = 74）
    public static final int SCALE_LINE_WIDTH = 2;     // 刻度线宽度
//配方槽的常量
    public static final int RECIPE_SLOT_START_X = 160;
    public static final int RECIPE_SLOT_START_Y = 18;
    public static final int RECIPE_SLOTS_PER_PAGE = 9; // 3x3网格
    public static final int RECIPE_SCROLLBAR_X = 178;
    public static final int RECIPE_SCROLLBAR_Y = 18;
    public static final int RECIPE_SCROLLBAR_HEIGHT = 54;

    // 液体系统常量
    public static final int FLUID_TANK_X = 82;
    public static final int FLUID_TANK_Y = 18;
    public static final int FLUID_TANK_WIDTH = 12;
    public static final int FLUID_TANK_HEIGHT = 54;
    public static final int MAX_FLUID_CAPACITY = 1000; // 1000mb = 1桶
}