package com.chessgamblegame.model;

import com.chessgamblegame.model.enums.PieceType;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GameConfigTest
{
    @Test
    void defaultValuesMatchGdd()
    {
        GameConfig cfg = new GameConfig();

        assertEquals(3, cfg.maxHp);
        assertEquals(3, cfg.startHp);
        assertEquals(100, cfg.healEveryScore);
        assertEquals(1, cfg.surviveTurnScore);
    }

    @Test
    void captureScoreMatchesGdd()
    {
        GameConfig cfg = new GameConfig();

        assertEquals(10, cfg.scoreFor(PieceType.Pawn));
        assertEquals(30, cfg.scoreFor(PieceType.Knight));
        assertEquals(30, cfg.scoreFor(PieceType.Bishop));
        assertEquals(50, cfg.scoreFor(PieceType.Rook));
        assertEquals(90, cfg.scoreFor(PieceType.Queen));
        assertEquals(0, cfg.scoreFor(PieceType.King));
    }

    @Test
    void diceIsFairByDefault()
    {
        GameConfig cfg = new GameConfig();

        assertEquals(6, cfg.diceWeights.size());
        for (int w : cfg.diceWeights.values())
        {
            assertEquals(1, w);
        }
    }

    @Test
    void configCanBeChanged()
    {
        GameConfig cfg = new GameConfig();
        cfg.healEveryScore = 50;
        cfg.captureScore.put(PieceType.Queen, 200);

        assertEquals(50, cfg.healEveryScore);
        assertEquals(200, cfg.scoreFor(PieceType.Queen));
    }

    @Test
    void eachConfigHasItsOwnMaps()
    {
        GameConfig a = new GameConfig();
        GameConfig b = new GameConfig();
        a.captureScore.put(PieceType.Pawn, 999);

        assertEquals(10, b.scoreFor(PieceType.Pawn));
    }

    // Xúc xắc có trọng số

    @Test
    void zeroWeightFaceNeverAppears()
    {
        GameConfig cfg = new GameConfig();
        cfg.diceWeights.put(PieceType.King, 0);
        Dice dice = new Dice(new Random(1), cfg.diceWeights);

        for (int i = 0; i < 1000; i++)
        {
            assertNotEquals(PieceType.King, dice.roll());
        }
    }

    @Test
    void onlyOneFaceWithWeightAlwaysAppears()
    {
        Map<PieceType, Integer> weights = new EnumMap<>(PieceType.class);
        weights.put(PieceType.Rook, 5);
        Dice dice = new Dice(new Random(1), weights);

        for (int i = 0; i < 100; i++)
        {
            assertEquals(PieceType.Rook, dice.roll());
        }
    }

    @Test
    void heavierFaceAppearsMoreOften()
    {
        GameConfig cfg = new GameConfig();
        cfg.diceWeights.put(PieceType.Pawn, 5);
        Dice dice = new Dice(new Random(3), cfg.diceWeights);

        int pawns = 0;
        for (int i = 0; i < 10000; i++)
        {
            if (dice.roll() == PieceType.Pawn)
            {
                pawns++;
            }
        }
        // Tốt có trọng số 5/10 => khoảng 5000 lần
        assertTrue(pawns > 4500 && pawns < 5500, "Tốt ra " + pawns + " lần");
    }

    @Test
    void invalidWeightsAreRejected()
    {
        Map<PieceType, Integer> allZero = new EnumMap<>(PieceType.class);
        allZero.put(PieceType.Pawn, 0);
        assertThrows(IllegalArgumentException.class, () -> new Dice(new Random(), allZero));

        Map<PieceType, Integer> negative = new EnumMap<>(PieceType.class);
        negative.put(PieceType.Pawn, -1);
        negative.put(PieceType.Knight, 3);
        assertThrows(IllegalArgumentException.class, () -> new Dice(new Random(), negative));
    }

    @Test
    void changingConfigAfterCreatingDiceDoesNotAffectIt()
    {
        GameConfig cfg = new GameConfig();
        Dice dice = new Dice(new Random(1), cfg.diceWeights);
        cfg.diceWeights.put(PieceType.Queen, 0);
        cfg.diceWeights.put(PieceType.Pawn, 0);
        cfg.diceWeights.put(PieceType.Knight, 0);
        cfg.diceWeights.put(PieceType.Bishop, 0);
        cfg.diceWeights.put(PieceType.Rook, 0);

        boolean sawNonKing = false;
        for (int i = 0; i < 100; i++)
        {
            if (dice.roll() != PieceType.King)
            {
                sawNonKing = true;
            }
        }
        assertTrue(sawNonKing);
    }
}
