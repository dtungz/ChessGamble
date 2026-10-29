package com.chessgamblegame.render;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

// Nút bấm kiểu pixel: viền vuông dày, rê chuột vào thì viền chuyển đỏ.
public class UiButton
{
    private static final float BORDER = 3;

    public final Rectangle bounds;
    public String label;
    public boolean enabled = true;

    private final GlyphLayout layout = new GlyphLayout();

    public UiButton(String label, float x, float y, float width, float height)
    {
        this.label = label;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public boolean isClicked(Vector2 mouse, boolean clicked)
    {
        return enabled && clicked && bounds.contains(mouse);
    }

    // Gọi giữa shapes.begin(Filled) và shapes.end()
    public void drawShape(ShapeRenderer shapes, Vector2 mouse)
    {
        boolean hover = enabled && bounds.contains(mouse);
        shapes.setColor(!enabled ? Palette.PANEL : hover ? Palette.ACCENT : Palette.BORDER);
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        shapes.setColor(hover ? Palette.BORDER : Palette.PANEL);
        shapes.rect(bounds.x + BORDER, bounds.y + BORDER, bounds.width - 2 * BORDER, bounds.height - 2 * BORDER);
    }

    // Gọi giữa batch.begin() và batch.end()
    public void drawLabel(SpriteBatch batch, BitmapFont font)
    {
        font.setColor(enabled ? Palette.TEXT : Palette.BORDER);
        layout.setText(font, label);
        font.draw(batch, label, Math.round(bounds.x + (bounds.width - layout.width) / 2),
            Math.round(bounds.y + (bounds.height + layout.height) / 2));
    }
}
