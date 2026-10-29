package com.chessgamblegame.level;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;
import com.chessgamblegame.model.Board;
import com.chessgamblegame.model.Piece;
import com.chessgamblegame.model.Position;
import com.chessgamblegame.model.enums.PieceType;

// Chỉ script này được thêm LibGDX
public class LevelLoader
{
    private LevelLoader()
    {
    }

    // Dùng trong game: LevelLoader.load(Gdx.files.internal("levels/level01.json"))
    public static LevelData load(FileHandle file)
    {
        return parse(file.readString("UTF-8"));
    }

    public static LevelData parse(String jsonText)
    {
        Json json = new Json();
        json.setIgnoreUnknownFields(true);
        LevelData data;
        try
        {
            data = json.fromJson(LevelData.class, jsonText);
        }
        catch (SerializationException ex)
        {
            throw new IllegalArgumentException("File level không phải JSON hợp lệ: " + ex.getMessage(), ex);
        }
        validate(data);
        return data;
    }

    // Tạo bàn cờ mới từ dữ liệu level (mỗi lần gọi là 1 bàn mới, dùng cho "Chơi lại").
    public static Board createBoard(LevelData data)
    {
        Board board = new Board();
        board.playerPos = parseSquare(data.playerStart, "playerStart");
        for (LevelData.EnemyData e : data.enemies)
        {
            Position pos = parseSquare(e.pos, "vị trí quân địch");
            if (!board.isEmpty(pos))
            {
                throw new IllegalArgumentException("Level " + data.id + ": ô " + e.pos + " bị đặt 2 quân");
            }
            board.addEnemy(new Piece(parseEnemyType(e.type), pos));
        }
        return board;
    }

    private static void validate(LevelData data)
    {
        if (data == null)
        {
            throw new IllegalArgumentException("File level rỗng");
        }
        if (data.enemies == null || data.enemies.isEmpty())
        {
            throw new IllegalArgumentException("Level " + data.id + " không có quân địch nào");
        }
        if (data.enemiesPerTurn < 1)
        {
            throw new IllegalArgumentException("Level " + data.id + ": enemiesPerTurn phải >= 1");
        }
        createBoard(data);
    }

    private static Position parseSquare(String s, String what)
    {
        if (s == null || !s.matches("[a-h][1-8]"))
        {
            throw new IllegalArgumentException("Ô không hợp lệ cho " + what + ": " + s);
        }
        return Position.parse(s);
    }

    // Chấp nhận "KNIGHT", "knight", "Knight". Địch không có Vua.
    static PieceType parseEnemyType(String s)
    {
        if (s != null)
        {
            for (PieceType t : PieceType.values())
            {
                if (t != PieceType.King && t.name().equalsIgnoreCase(s.trim()))
                {
                    return t;
                }
            }
        }
        throw new IllegalArgumentException("Loại quân địch không hợp lệ: " + s);
    }
}
