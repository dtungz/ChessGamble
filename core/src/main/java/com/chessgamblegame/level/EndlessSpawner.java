package com.chessgamblegame.level;

import com.chessgamblegame.model.Board;
import com.chessgamblegame.model.GameConfig;
import com.chessgamblegame.model.Piece;
import com.chessgamblegame.model.Position;
import com.chessgamblegame.model.enums.PieceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Luật sinh quân và độ khó tăng dần của chế độ Endless (GDD 7.1). Mọi con số lấy từ GameConfig.
public class EndlessSpawner
{
    private static final int TOP_ROW = Board.SIZE - 1;

    // Bậc 0: chỉ Tốt; bậc 1: thêm Mã, Tượng; bậc 2: thêm Xe; bậc 3 trở đi: thêm Hậu.
    private static final PieceType[][] TIERS = {
        {PieceType.Pawn},
        {PieceType.Pawn, PieceType.Knight, PieceType.Bishop},
        {PieceType.Pawn, PieceType.Knight, PieceType.Bishop, PieceType.Rook},
        {PieceType.Pawn, PieceType.Knight, PieceType.Bishop, PieceType.Rook, PieceType.Queen}
    };

    private final GameConfig cfg;

    public EndlessSpawner(GameConfig cfg)
    {
        this.cfg = cfg;
    }

    // Bàn bắt đầu: người chơi ở e1, vài quân Tốt ngẫu nhiên ở hàng trên cùng.
    public Board createStartBoard(Random rnd)
    {
        Board board = new Board();
        board.playerPos = Position.parse("e1");
        for (int i = 0; i < cfg.endlessStartPawns; i++)
        {
            Position p = randomEmptyTopSquare(board, rnd);
            if (p != null)
            {
                board.addEnemy(new Piece(PieceType.Pawn, p));
            }
        }
        return board;
    }

    public int enemiesPerTurn(int turn)
    {
        int n = cfg.endlessEnemiesPerTurnStart + (turn - 1) / cfg.endlessEnemiesPerTurnIncreaseEvery;
        return Math.min(n, cfg.endlessMaxEnemiesPerTurn);
    }

    public boolean shouldSpawn(int turn)
    {
        return turn % cfg.endlessSpawnEvery == 0;
    }

    public PieceType typeForTurn(int turn, Random rnd)
    {
        int tier = Math.min((turn - 1) / cfg.endlessTierEvery, TIERS.length - 1);
        PieceType[] choices = TIERS[tier];
        return choices[rnd.nextInt(choices.length)];
    }

    // Đặt 1 quân mới ở ô trống ngẫu nhiên trên hàng trên cùng. Trả về null nếu hàng đã kín hoặc bàn đã đủ quân.
    public Piece spawn(Board board, int turn, Random rnd)
    {
        if (board.getEnemies().size() >= cfg.endlessMaxEnemiesOnBoard)
        {
            return null;
        }
        Position p = randomEmptyTopSquare(board, rnd);
        if (p == null)
        {
            return null;
        }
        Piece e = new Piece(typeForTurn(turn, rnd), p);
        board.addEnemy(e);
        return e;
    }

    private static Position randomEmptyTopSquare(Board board, Random rnd)
    {
        List<Position> free = new ArrayList<>();
        for (int x = 0; x < Board.SIZE; x++)
        {
            Position p = new Position(x, TOP_ROW);
            if (board.isEmpty(p))
            {
                free.add(p);
            }
        }
        return free.isEmpty() ? null : free.get(rnd.nextInt(free.size()));
    }
}
