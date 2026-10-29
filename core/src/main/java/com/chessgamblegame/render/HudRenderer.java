package com.chessgamblegame.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

// Tim (máu) vẽ kiểu pixel: mỗi trái tim là 1 hình 7x6 điểm, phóng to mỗi điểm thành ô vuông.
public class HudRenderer
{
    private static final String[] HEART = {
        ".XX.XX.",
        "XXXXXXX",
        "XXXXXXX",
        ".XXXXX.",
        "..XXX..",
        "...X..."
    };
    private static final float PIXEL = 4;
    private static final float HEART_WIDTH = HEART[0].length() * PIXEL;
    private static final float GAP = 8;

    // (x, y) là góc dưới-trái của dãy tim. Gọi giữa shapes.begin(Filled) và shapes.end()
    public void drawHearts(ShapeRenderer shapes, float x, float y, int hp, int maxHp)
    {
        for (int i = 0; i < maxHp; i++)
        {
            drawHeart(shapes, x + i * (HEART_WIDTH + GAP), y, i < hp ? Palette.ACCENT : Palette.EMPTY_HEART);
        }
    }

    public static float heartsWidth(int count)
    {
        return count * HEART_WIDTH + (count - 1) * GAP;
    }

    private static void drawHeart(ShapeRenderer shapes, float x, float y, Color color)
    {
        shapes.setColor(color);
        for (int row = 0; row < HEART.length; row++)
        {
            String line = HEART[row];
            float py = y + (HEART.length - 1 - row) * PIXEL;   // hàng đầu tiên nằm trên cùng
            for (int col = 0; col < line.length(); col++)
            {
                if (line.charAt(col) == 'X')
                {
                    shapes.rect(x + col * PIXEL, py, PIXEL, PIXEL);
                }
            }
        }
    }
}
