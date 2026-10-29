package com.chessgamblegame.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

// Phần thông tin bên phải bàn cờ: tim (máu) và nút "Skip turn".
public class HudRenderer
{
    private static final float HEART_SIZE = 34;
    private static final float HEART_GAP = 12;

    private static final Color HEART_FULL = Color.valueOf("E5484D");
    private static final Color HEART_EMPTY = Color.valueOf("4A4A55");
    private static final Color BUTTON_FILL = Color.valueOf("3B6FD8");
    private static final Color BUTTON_TEXT = Color.valueOf("FFFFFF");

    private final Rectangle skipButton;
    private final GlyphLayout layout = new GlyphLayout();

    public HudRenderer(float skipX, float skipY, float skipWidth, float skipHeight)
    {
        skipButton = new Rectangle(skipX, skipY, skipWidth, skipHeight);
    }

    // (x, y) là góc dưới-trái của dãy tim. Gọi giữa shapes.begin(Filled) và shapes.end()
    public void drawHearts(ShapeRenderer shapes, float x, float y, int hp, int maxHp)
    {
        for (int i = 0; i < maxHp; i++)
        {
            float cx = x + HEART_SIZE / 2 + i * (HEART_SIZE + HEART_GAP);
            drawHeart(shapes, cx, y + HEART_SIZE / 2, HEART_SIZE, i < hp ? HEART_FULL : HEART_EMPTY);
        }
    }

    // Trái tim = 2 hình tròn ở trên + 1 tam giác chúc xuống.
    private static void drawHeart(ShapeRenderer shapes, float cx, float cy, float size, Color color)
    {
        float r = size * 0.27f;
        shapes.setColor(color);
        shapes.circle(cx - size * 0.24f, cy + size * 0.12f, r, 24);
        shapes.circle(cx + size * 0.24f, cy + size * 0.12f, r, 24);
        shapes.triangle(cx - size * 0.5f, cy + size * 0.06f,
            cx + size * 0.5f, cy + size * 0.06f,
            cx, cy - size * 0.5f);
    }

    public boolean skipButtonContains(Vector2 world)
    {
        return skipButton.contains(world);
    }

    public void drawSkipButton(ShapeRenderer shapes)
    {
        shapes.setColor(BUTTON_FILL);
        shapes.rect(skipButton.x, skipButton.y, skipButton.width, skipButton.height);
    }

    public void drawSkipLabel(SpriteBatch batch, BitmapFont font)
    {
        String text = "Skip turn";
        font.setColor(BUTTON_TEXT);
        layout.setText(font, text);
        font.draw(batch, text, skipButton.x + (skipButton.width - layout.width) / 2,
            skipButton.y + (skipButton.height + layout.height) / 2);
    }
}
