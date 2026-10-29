package com.chessgamblegame.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chessgamblegame.model.enums.PieceType;

import java.util.Random;

// Xúc xắc trên màn hình: bấm vào để tung. Kết quả thật đã do TurnManager chọn từ trước,
// widget chỉ đổi mặt ngẫu nhiên vài lần cho có cảm giác "lăn" rồi dừng ở kết quả đó.
public class DiceWidget
{
    private static final float ROLL_TIME = 0.8f;
    private static final float FACE_CHANGE_EVERY = 0.08f;

    private static final Color BORDER = Color.valueOf("F2C14E");
    private static final Color FILL = Color.valueOf("FAFAFA");
    private static final Color DISABLED_FILL = Color.valueOf("8A8A92");
    private static final Color TEXT = Color.valueOf("1A1A1A");

    private final Rectangle bounds;
    private final Random rnd = new Random();
    private final GlyphLayout layout = new GlyphLayout();

    private PieceType shownFace;
    private PieceType result;
    private float rollTimeLeft;
    private float faceTimer;

    public DiceWidget(float x, float y, float size)
    {
        bounds = new Rectangle(x, y, size, size);
    }

    public boolean contains(Vector2 world)
    {
        return bounds.contains(world);
    }

    public void startRoll(PieceType result)
    {
        this.result = result;
        rollTimeLeft = ROLL_TIME;
        faceTimer = 0;
    }

    public void reset()
    {
        shownFace = null;
        result = null;
        rollTimeLeft = 0;
    }

    public boolean isRolling()
    {
        return rollTimeLeft > 0;
    }

    public void update(float delta)
    {
        if (!isRolling())
        {
            return;
        }
        rollTimeLeft -= delta;
        faceTimer -= delta;
        if (rollTimeLeft <= 0)
        {
            shownFace = result;
        }
        else if (faceTimer <= 0)
        {
            PieceType[] faces = PieceType.values();
            shownFace = faces[rnd.nextInt(faces.length)];
            faceTimer = FACE_CHANGE_EVERY;
        }
    }

    // canRoll = true thì xúc xắc sáng và có viền vàng để mời người chơi bấm.
    public void drawShapes(ShapeRenderer shapes, boolean canRoll)
    {
        if (canRoll)
        {
            shapes.setColor(BORDER);
            shapes.rect(bounds.x - 5, bounds.y - 5, bounds.width + 10, bounds.height + 10);
        }
        shapes.setColor(canRoll || isRolling() || shownFace != null ? FILL : DISABLED_FILL);
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void drawText(SpriteBatch batch, BitmapFont bigFont, BitmapFont smallFont, Color captionColor)
    {
        String letter = shownFace == null ? "?" : PieceRenderer.letterOf(shownFace);
        bigFont.setColor(TEXT);
        layout.setText(bigFont, letter);
        bigFont.draw(batch, letter, bounds.x + (bounds.width - layout.width) / 2,
            bounds.y + (bounds.height + layout.height) / 2);

        String caption = shownFace == null ? "Click to roll" : shownFace.name();
        smallFont.setColor(captionColor);
        layout.setText(smallFont, caption);
        smallFont.draw(batch, caption, bounds.x + (bounds.width - layout.width) / 2, bounds.y - 14);
    }
}
