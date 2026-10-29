package com.chessgamblegame.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

// Nút bấm đơn giản tự vẽ (hình chữ nhật + chữ), sáng lên khi rê chuột vào.
public class UiButton
{
    private static final Color FILL = Color.valueOf("3B6FD8");
    private static final Color HOVER = Color.valueOf("5A8CF0");
    private static final Color DISABLED = Color.valueOf("4A4A55");
    private static final Color TEXT = Color.valueOf("FFFFFF");
    private static final Color DISABLED_TEXT = Color.valueOf("8A8A92");

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
        if (!enabled)
        {
            shapes.setColor(DISABLED);
        }
        else
        {
            shapes.setColor(bounds.contains(mouse) ? HOVER : FILL);
        }
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    // Gọi giữa batch.begin() và batch.end()
    public void drawLabel(SpriteBatch batch, BitmapFont font)
    {
        font.setColor(enabled ? TEXT : DISABLED_TEXT);
        layout.setText(font, label);
        font.draw(batch, label, bounds.x + (bounds.width - layout.width) / 2,
            bounds.y + (bounds.height + layout.height) / 2);
    }
}
