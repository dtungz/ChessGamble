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

    private final GameConfig cfg;

    public EndlessSpawner(GameConfig cfg)
    {
        this.cfg = cfg;
    }

    // Bàn bắt đầu: người chơi ở ô endlessPlayerStart, vài quân Tốt ngẫu nhiên ở hàng trên cùng.
    public Board createStartBoard(Random rnd)
    {
        Board board = new Board();
        board.playerPos = Position.parse(cfg.endlessPlayerStart);
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

    // Số quân sinh ra trong 1 đợt, tăng dần theo điểm hiện có.
    public int spawnCount(int score)
    {
        int n = cfg.endlessSpawnCountStart + Math.max(0, score) / cfg.endlessSpawnCountScoreStep;
        return Math.min(n, cfg.endlessSpawnCountMax);
    }

    public PieceType typeForTurn(int turn, Random rnd)
    {
        PieceType[][] tiers = cfg.endlessTiers;
        int tier = Math.min((turn - 1) / cfg.endlessTierEvery, tiers.length - 1);
        PieceType[] choices = tiers[tier];
        return choices[rnd.nextInt(choices.length)];
    }

    // Sinh 1 đợt gồm spawnCount(score) quân. Trả về các quân thật sự được đặt (có thể ít hơn nếu hết chỗ).
    public List<Piece> spawnWave(Board board, int turn, int score, Random rnd)
    {
        List<Piece> spawned = new ArrayList<>();
        for (int i = 0; i < spawnCount(score); i++)
        {
            Piece e = spawn(board, turn, rnd);
            if (e == null)
            {
                break;
            }
            spawned.add(e);
        }
        return spawned;
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
