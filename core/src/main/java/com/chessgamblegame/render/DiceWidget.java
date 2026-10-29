package com.chessgamblegame.render;

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

    private static final float FRAME = 4;
    private static final float FACE_SIZE = 96;   // ảnh 16px phóng x6

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

    // Khung xúc xắc: viền đỏ nhấp nháy khi tới lượt bấm, viền xám khi không bấm được.
    public void drawShapes(ShapeRenderer shapes, boolean canRoll, float time)
    {
        boolean blinkOn = canRoll && (int) (time * 3) % 2 == 0;
        shapes.setColor(blinkOn ? Palette.ACCENT : Palette.BORDER);
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        shapes.setColor(Palette.PANEL);
        shapes.rect(bounds.x + FRAME, bounds.y + FRAME, bounds.width - 2 * FRAME, bounds.height - 2 * FRAME);
    }

    // Tên mặt xúc xắc ở trên khung, ảnh quân trắng ở giữa; chưa tung thì hiện dấu "?".
    public void drawText(SpriteBatch batch, BitmapFont bigFont, BitmapFont labelFont, PieceRenderer pieces)
    {
        if (shownFace == null)
        {
            bigFont.setColor(Palette.TEXT);
            layout.setText(bigFont, "?");
            bigFont.draw(batch, "?", Math.round(bounds.x + (bounds.width - layout.width) / 2),
                Math.round(bounds.y + (bounds.height + layout.height) / 2));
        }
        else
        {
            batch.draw(pieces.whiteTexture(shownFace), bounds.x + (bounds.width - FACE_SIZE) / 2,
                bounds.y + (bounds.height - FACE_SIZE) / 2, FACE_SIZE, FACE_SIZE);
        }

        String caption = shownFace == null ? "ROLL" : shownFace.name().toUpperCase();
        labelFont.setColor(Palette.TEXT);
        layout.setText(labelFont, caption);
        labelFont.draw(batch, caption, Math.round(bounds.x + (bounds.width - layout.width) / 2),
            bounds.y + bounds.height + 14 + layout.height);
    }
}
