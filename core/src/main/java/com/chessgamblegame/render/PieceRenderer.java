package com.chessgamblegame.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.chessgamblegame.model.enums.PieceType;
import com.chessgamblegame.render.BoardView.Sprite;

import java.util.EnumMap;
import java.util.Map;

// Vẽ quân cờ bằng ảnh pixel 16x16 trong assets/pieces (phóng to x4 = 64px).
// Quân trắng = người chơi, quân đen = địch. Vẽ theo BoardView (vị trí đang animation), không vẽ thẳng từ Board.
public class PieceRenderer
{
    private static final float PIECE_SIZE = 64;
    private static final float PIECE_MARGIN = (BoardRenderer.CELL - PIECE_SIZE) / 2;
    private static final float MARKER_RADIUS = BoardRenderer.CELL * 0.40f;
    private static final float RING = 4;

    // Rung: lệch qua lại tối đa 1.5px, khoảng 6 lần/giây. Chỉ quân rung, ô cờ đứng yên.
    private static final float SHAKE_AMPLITUDE = 1.5f;
    private static final float SHAKE_SPEED = 40f;

    private static final Color PLAYER_SHADOW = Palette.withAlpha(Palette.OUTLINE, 0.30f);
    private static final Color UNKNOWN_FILL = Palette.CARD;
    private static final Color UNKNOWN_RING = Palette.BORDER;
    private static final Color UNKNOWN_TEXT = Palette.CARD_TEXT;

    private final Map<PieceType, Texture> white = new EnumMap<>(PieceType.class);
    private final Map<PieceType, Texture> black = new EnumMap<>(PieceType.class);
    private final GlyphLayout layout = new GlyphLayout();

    public PieceRenderer()
    {
        for (PieceType t : PieceType.values())
        {
            white.put(t, load("pieces/white_" + fileName(t) + ".png"));
            if (t != PieceType.King)   // địch không có Vua
            {
                black.put(t, load("pieces/black_" + fileName(t) + ".png"));
            }
        }
    }

    private static Texture load(String path)
    {
        Texture tex = new Texture(Gdx.files.internal(path));
        // Nearest: phóng to ảnh pixel mà vẫn giữ nét vuông vức, không bị mờ
        tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return tex;
    }

    private static String fileName(PieceType t)
    {
        return t.name().toLowerCase();
    }

    // Bóng mờ dưới chân người chơi cho dễ nhìn.
    // playerType null = chưa tung xúc xắc, vẽ ô tròn có dấu "?". Gọi giữa shapes.begin(Filled) và shapes.end()
    public void drawShapes(ShapeRenderer shapes, BoardView view, PieceType playerType)
    {
        Sprite p = view.getPlayerSprite();
        if (playerType == null)
        {
            disc(shapes, p.x, p.y, MARKER_RADIUS * 0.9f, UNKNOWN_RING);
            disc(shapes, p.x, p.y, MARKER_RADIUS * 0.9f - RING, UNKNOWN_FILL);
        }
        else
        {
            shapes.setColor(PLAYER_SHADOW);
            shapes.ellipse(p.x + BoardRenderer.CELL * 0.18f, p.y + 4, BoardRenderer.CELL * 0.64f, 12);
        }
    }

    // Gọi giữa batch.begin() và batch.end()
    // playerVisible = false: ẩn quân người chơi trong khung hình này (dùng cho hiệu ứng chớp khi bị đánh).
    public void drawPieces(SpriteBatch batch, BitmapFont font, BoardView view, PieceType playerType,
                           boolean playerVisible, float time)
    {
        for (Sprite s : view.getEnemySprites())
        {
            Texture tex = black.get(s.type);
            if (tex != null)
            {
                batch.draw(tex, s.x + shakeOffset(s, time) + PIECE_MARGIN, s.y + PIECE_MARGIN, PIECE_SIZE, PIECE_SIZE);
            }
        }
        Sprite p = view.getPlayerSprite();
        if (!playerVisible)
        {
            return;
        }
        if (playerType == null)
        {
            font.setColor(UNKNOWN_TEXT);
            layout.setText(font, "?");
            font.draw(batch, "?", p.x + (BoardRenderer.CELL - layout.width) / 2,
                p.y + (BoardRenderer.CELL + layout.height) / 2);
        }
        else
        {
            batch.draw(white.get(playerType), p.x + PIECE_MARGIN, p.y + PIECE_MARGIN, PIECE_SIZE, PIECE_SIZE);
        }
    }

    // Ảnh quân trắng, dùng cho xúc xắc.
    public Texture whiteTexture(PieceType type)
    {
        return white.get(type);
    }

    // Ảnh quân đen (địch), dùng cho thẻ "NEXT MOVE".
    public Texture blackTexture(PieceType type)
    {
        return black.get(type);
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

    private static void disc(ShapeRenderer shapes, float cellX, float cellY, float radius, Color color)
    {
        shapes.setColor(color);
        shapes.circle(cellX + BoardRenderer.CELL / 2, cellY + BoardRenderer.CELL / 2, radius, 40);
    }

    public void dispose()
    {
        for (Texture t : white.values())
        {
            t.dispose();
        }
        for (Texture t : black.values())
        {
            t.dispose();
        }
    }
}
