package com.chessgamblegame.render;

import com.badlogic.gdx.graphics.Color;

// Bảng màu chung của cả game (tông tối + hồng đào + tím xám + đỏ nhấn).
// Muốn đổi tông màu chỉ cần sửa ở đây.
public final class Palette
{
    public static final Color BACKGROUND = Color.valueOf("1A1C2C");
    public static final Color PANEL = Color.valueOf("262840");
    public static final Color LIGHT_SQUARE = Color.valueOf("F4D3B0");
    public static final Color DARK_SQUARE = Color.valueOf("8B86A6");
    public static final Color BORDER = Color.valueOf("5C5A7A");
    public static final Color OUTLINE = Color.valueOf("0F1020");
    public static final Color ACCENT = Color.valueOf("E8264A");
    public static final Color TEXT = Color.valueOf("E8E4F0");
    public static final Color DIM_TEXT = Color.valueOf("8B86A6");
    public static final Color CARD = Color.valueOf("F4D3B0");
    public static final Color CARD_TEXT = Color.valueOf("1A1C2C");
    public static final Color EMPTY_HEART = Color.valueOf("3A3B55");
    public static final Color WIN = Color.valueOf("7BD88F");

    private Palette()
    {
    }

    // Bản sao có độ trong suốt khác (không sửa màu gốc).
    public static Color withAlpha(Color c, float alpha)
    {
        return new Color(c.r, c.g, c.b, alpha);
    }
}
