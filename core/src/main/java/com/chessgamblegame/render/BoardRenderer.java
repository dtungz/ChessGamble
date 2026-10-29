package com.chessgamblegame.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.chessgamblegame.ChessGambleGame;
import com.chessgamblegame.model.Board;
import com.chessgamblegame.model.Position;

import java.util.List;

// Vẽ bàn cờ 8x8 kiểu pixel và đổi qua lại giữa toạ độ màn hình và ô cờ.
// Gốc toạ độ libGDX ở góc dưới-trái, khớp với quy ước model (a1 = góc dưới-trái).
public class BoardRenderer
{
    public static final float CELL = 72;
    public static final float BOARD_SIZE = CELL * Board.SIZE;
    public static final float BOARD_X = (ChessGambleGame.WORLD_WIDTH - BOARD_SIZE) / 2;
    public static final float BOARD_Y = 72;

    private static final float FRAME = 6;
    private static final float MOVE_DOT = 14;
    private static final float CAPTURE_FRAME = 5;

    private static final Color MOVE_HINT = Palette.withAlpha(Palette.BORDER, 0.85f);
    private static final float HOVER_FRAME = 4;
    private static final Color HOVER_FILL = Palette.withAlpha(Palette.OUTLINE, 0.18f);

    // Viền tối quanh bàn cờ + dải gạch đứt dưới đáy. Gọi giữa shapes.begin(Filled) và shapes.end()
    public void drawFrame(ShapeRenderer shapes)
    {
        shapes.setColor(Palette.OUTLINE);
        shapes.rect(BOARD_X - FRAME, BOARD_Y - FRAME, BOARD_SIZE + 2 * FRAME, BOARD_SIZE + 2 * FRAME);
        shapes.setColor(Palette.BORDER);
        for (float x = BOARD_X; x < BOARD_X + BOARD_SIZE; x += 24)
        {
            shapes.rect(x, BOARD_Y - FRAME - 8, 14, 4);
        }
    }

    public void drawSquares(ShapeRenderer shapes)
    {
        for (int x = 0; x < Board.SIZE; x++)
        {
            for (int y = 0; y < Board.SIZE; y++)
            {
                shapes.setColor((x + y) % 2 == 0 ? Palette.DARK_SQUARE : Palette.LIGHT_SQUARE);   // a1 là ô tối
                shapes.rect(cellX(x), cellY(y), CELL, CELL);
            }
        }
    }

    // Ô đi được: chấm vuông nhỏ ở giữa (bỏ chấm nếu dots = false). Ô ăn được: khung đỏ quanh ô.
    public void drawMoveHints(ShapeRenderer shapes, Board board, List<Position> moves, boolean dots)
    {
        for (Position p : moves)
        {
            float x = cellX(p.x());
            float y = cellY(p.y());
            if (board.getEnemyAt(p) != null)
            {
                drawFrame(shapes, x, y, CAPTURE_FRAME, Palette.ACCENT);
            }
            else if (dots)
            {
                shapes.setColor(MOVE_HINT);
                shapes.rect(x + (CELL - MOVE_DOT) / 2, y + (CELL - MOVE_DOT) / 2, MOVE_DOT, MOVE_DOT);
            }
        }
    }

    // Ô đi được đang rê chuột: phủ mờ + khung tối, để biết bấm vào sẽ đi tới đâu.
    public void drawHover(ShapeRenderer shapes, Position p)
    {
        float x = cellX(p.x());
        float y = cellY(p.y());
        shapes.setColor(HOVER_FILL);
        shapes.rect(x, y, CELL, CELL);
        drawFrame(shapes, x, y, HOVER_FRAME, Palette.OUTLINE);
    }

    public static void drawFrame(ShapeRenderer shapes, float x, float y, float thickness, Color color)
    {
        shapes.setColor(color);
        shapes.rect(x, y, CELL, thickness);
        shapes.rect(x, y + CELL - thickness, CELL, thickness);
        shapes.rect(x, y, thickness, CELL);
        shapes.rect(x + CELL - thickness, y, thickness, CELL);
    }

    public static float cellX(int x)
    {
        return BOARD_X + x * CELL;
    }

    public static float cellY(int y)
    {
        return BOARD_Y + y * CELL;
    }

    public static float centerX(Position p)
    {
        return cellX(p.x()) + CELL / 2;
    }

    public static float centerY(Position p)
    {
        return cellY(p.y()) + CELL / 2;
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
