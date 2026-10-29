package com.chessgamblegame.model;

import com.chessgamblegame.model.enums.PieceType;

import java.util.EnumMap;
import java.util.Map;

public class GameConfig
{
    public int maxHp = 3;
    public int startHp = 3;

    public int healEveryScore = 100;
    public int surviveTurnScore = 1;

    // Endless
    public int endlessStartPawns = 2;
    public int endlessSpawnEvery = 3;                    // cứ 3 lượt sinh thêm 1 quân ở hàng trên cùng
    public int endlessMaxEnemiesOnBoard = 12;
    public int endlessTierEvery = 10;                    // cứ 10 lượt mở thêm loại quân mạnh hơn
    public int endlessEnemiesPerTurnStart = 1;
    public int endlessEnemiesPerTurnIncreaseEvery = 10;  // cứ 10 lượt thêm 1 quân được đi mỗi lượt
    public int endlessMaxEnemiesPerTurn = 3;

    public final Map<PieceType, Integer> captureScore = new EnumMap<>(PieceType.class);

    public final Map<PieceType, Integer> diceWeights = new EnumMap<>(PieceType.class);

    public GameConfig()
    {
        captureScore.put(PieceType.Pawn, 10);
        captureScore.put(PieceType.Knight, 30);
        captureScore.put(PieceType.Bishop, 30);
        captureScore.put(PieceType.Rook, 50);
        captureScore.put(PieceType.Queen, 90);

        for (PieceType type : PieceType.values())
        {
            diceWeights.put(type, 1);
        }
    }

    public int scoreFor(PieceType capturedType)
    {
        return captureScore.getOrDefault(capturedType, 0);
    }
}
