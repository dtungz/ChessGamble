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
