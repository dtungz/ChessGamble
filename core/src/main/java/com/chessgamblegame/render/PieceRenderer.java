package com.chessgamblegame.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.chessgamblegame.model.enums.PieceType;
import com.chessgamblegame.render.BoardView.Sprite;

// Vẽ quân cờ tạm bằng hình tròn + chữ cái (P N B R Q K). Sau này có ảnh PNG thì chỉ cần thay class này.
// Vẽ theo BoardView (vị trí đang animation), không vẽ thẳng từ Board.
public class PieceRenderer
{
    private static final float RADIUS = BoardRenderer.CELL * 0.36f;
    private static final float RING = 4;

    // Rung: lệch qua lại tối đa 3px, khoảng 6 lần/giây
    private static final float SHAKE_AMPLITUDE = 3f;
    private static final float SHAKE_SPEED = 40f;

    private static final Color ENEMY_FILL = Color.valueOf("222222");
    private static final Color ENEMY_RING = Color.valueOf("D23C3C");
    private static final Color SHAKING_RING = Color.valueOf("FF9F1C");
    private static final Color ENEMY_TEXT = Color.valueOf("F0F0F0");
    private static final Color PLAYER_FILL = Color.valueOf("FAFAFA");
    private static final Color PLAYER_RING = Color.valueOf("2F7BEA");
    private static final Color PLAYER_TEXT = Color.valueOf("1A1A1A");

    private final GlyphLayout layout = new GlyphLayout();

    // time: tổng thời gian đã chạy (giây), dùng cho hiệu ứng rung.
    // Gọi giữa shapes.begin(Filled) và shapes.end()
    public void drawShapes(ShapeRenderer shapes, BoardView view, float time)
    {
        for (Sprite s : view.getEnemySprites())
        {
            circle(shapes, s.x + shakeOffset(s, time), s.y, s.shaking ? SHAKING_RING : ENEMY_RING, ENEMY_FILL);
        }
        Sprite p = view.getPlayerSprite();
        circle(shapes, p.x, p.y, PLAYER_RING, PLAYER_FILL);
    }

    // playerType: mặt xúc xắc hiện tại, null = chưa tung (vẽ dấu "?").
    // Gọi giữa batch.begin() và batch.end()
    public void drawLetters(SpriteBatch batch, BitmapFont font, BoardView view, PieceType playerType, float time)
    {
        for (Sprite s : view.getEnemySprites())
        {
            letter(batch, font, s.x + shakeOffset(s, time), s.y, letterOf(s.type), ENEMY_TEXT);
        }
        Sprite p = view.getPlayerSprite();
        letter(batch, font, p.x, p.y, playerType == null ? "?" : letterOf(playerType), PLAYER_TEXT);
    }

    public static String letterOf(PieceType type)
    {
        return switch (type)
        {
            case Pawn -> "P";
            case Knight -> "N";
            case Bishop -> "B";
            case Rook -> "R";
            case Queen -> "Q";
            case King -> "K";
        };
    }

    // Chỉ đổi chỗ vẽ, không đổi vị trí thật của quân.
    private static float shakeOffset(Sprite s, float time)
    {
        return s.shaking ? (float) Math.sin(time * SHAKE_SPEED) * SHAKE_AMPLITUDE : 0f;
    }

    private static void circle(ShapeRenderer shapes, float cellX, float cellY, Color ring, Color fill)
    {
        float cx = cellX + BoardRenderer.CELL / 2;
        float cy = cellY + BoardRenderer.CELL / 2;
        shapes.setColor(ring);
        shapes.circle(cx, cy, RADIUS, 40);
        shapes.setColor(fill);
        shapes.circle(cx, cy, RADIUS - RING, 40);
    }

    private void letter(SpriteBatch batch, BitmapFont font, float cellX, float cellY, String text, Color color)
    {
        font.setColor(color);
        layout.setText(font, text);
        float x = cellX + (BoardRenderer.CELL - layout.width) / 2;
        float y = cellY + (BoardRenderer.CELL + layout.height) / 2;
        font.draw(batch, text, x, y);
    }
}
