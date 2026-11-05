package com.chessgamblegame.render;

import com.badlogic.gdx.graphics.Color;
import com.chessgamblegame.model.GameConfig.Colors;

// Bảng màu chung của cả game. Mã màu được khai báo trong GameConfig.Colors,
// muốn đổi tông màu thì sửa ở đó, không sửa file này.
public final class Palette
{
    public static final Color BACKGROUND = Color.valueOf(Colors.BACKGROUND);
    public static final Color PANEL = Color.valueOf(Colors.PANEL);
    public static final Color LIGHT_SQUARE = Color.valueOf(Colors.LIGHT_SQUARE);
    public static final Color DARK_SQUARE = Color.valueOf(Colors.DARK_SQUARE);
    public static final Color BORDER = Color.valueOf(Colors.BORDER);
    public static final Color OUTLINE = Color.valueOf(Colors.OUTLINE);
    public static final Color ACCENT = Color.valueOf(Colors.ACCENT);
    public static final Color TEXT = Color.valueOf(Colors.TEXT);
    public static final Color DIM_TEXT = Color.valueOf(Colors.DIM_TEXT);
    public static final Color CARD = Color.valueOf(Colors.CARD);
    public static final Color CARD_TEXT = Color.valueOf(Colors.CARD_TEXT);
    public static final Color EMPTY_HEART = Color.valueOf(Colors.EMPTY_HEART);
    public static final Color WIN = Color.valueOf(Colors.WIN);

    private Palette()
    {
    }

    // Bản sao có độ trong suốt khác (không sửa màu gốc).
    public static Color withAlpha(Color c, float alpha)
    {
        return new Color(c.r, c.g, c.b, alpha);
    }
}
