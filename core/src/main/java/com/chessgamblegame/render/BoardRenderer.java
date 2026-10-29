package com.chessgamblegame.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.chessgamblegame.model.Board;
import com.chessgamblegame.model.Position;

import java.util.List;

// Vẽ bàn cờ 8x8 và đổi qua lại giữa toạ độ màn hình và ô cờ.
// Gốc toạ độ libGDX ở góc dưới-trái, khớp với quy ước model (a1 = góc dưới-trái).
public class BoardRenderer
{
    public static final float BOARD_X = 40;
    public static final float BOARD_Y = 40;
    public static final float CELL = 80;
    public static final float BOARD_SIZE = CELL * Board.SIZE;

    private static final Color LIGHT = Color.valueOf("F0D9B5");
    private static final Color DARK = Color.valueOf("B58863");
    private static final Color LABEL = Color.valueOf("C8C8D0");
    private static final Color MOVE_HINT = new Color(0f, 0.78f, 0f, 0.40f);
    private static final Color CAPTURE_HINT = new Color(0.86f, 0f, 0f, 0.45f);

    private final GlyphLayout layout = new GlyphLayout();

    // Gọi giữa shapes.begin(Filled) và shapes.end()
    public void drawSquares(ShapeRenderer shapes)
    {
        for (int x = 0; x < Board.SIZE; x++)
        {
            for (int y = 0; y < Board.SIZE; y++)
            {
                shapes.setColor((x + y) % 2 == 0 ? DARK : LIGHT);   // a1 là ô tối
                shapes.rect(cellX(x), cellY(y), CELL, CELL);
            }
        }
    }

    // Tô 1 ô bằng màu bán trong suốt (cần bật blending trước khi gọi).
    public void highlight(ShapeRenderer shapes, Position p, Color color)
    {
        shapes.setColor(color);
        shapes.rect(cellX(p.x()), cellY(p.y()), CELL, CELL);
    }

    // Ô đi được tô xanh, ô có địch (ăn được) tô đỏ. Cần bật blending trước khi gọi.
    public void drawMoveHints(ShapeRenderer shapes, Board board, List<Position> moves)
    {
        for (Position p : moves)
        {
            highlight(shapes, p, board.getEnemyAt(p) != null ? CAPTURE_HINT : MOVE_HINT);
        }
    }

    // Chữ a–h dưới bàn cờ và 1–8 bên trái. Gọi giữa batch.begin() và batch.end()
    public void drawLabels(SpriteBatch batch, BitmapFont font)
    {
        font.setColor(LABEL);
        for (int i = 0; i < Board.SIZE; i++)
        {
            String file = String.valueOf((char) ('a' + i));
            layout.setText(font, file);
            font.draw(batch, file, cellX(i) + (CELL - layout.width) / 2, BOARD_Y - 12);

            String rank = String.valueOf(i + 1);
            layout.setText(font, rank);
            font.draw(batch, rank, BOARD_X - 12 - layout.width, cellY(i) + (CELL + layout.height) / 2);
        }
    }

    public static float cellX(int x)
    {
        return BOARD_X + x * CELL;
    }

    public static float cellY(int y)
    {
        return BOARD_Y + y * CELL;
    }

    // Toạ độ thế giới (đã qua viewport) -> ô cờ; null nếu nằm ngoài bàn.
    public static Position toCell(Vector2 world)
    {
        if (world.x < BOARD_X || world.y < BOARD_Y)
        {
            return null;
        }
        Position p = new Position((int) ((world.x - BOARD_X) / CELL), (int) ((world.y - BOARD_Y) / CELL));
        return p.inBounds() ? p : null;
    }
}
