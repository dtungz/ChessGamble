package com.chessgamblegame.model;

import com.chessgamblegame.model.enums.PieceType;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DiceTest
{
    @Test
    void sameSeedGivesSameRolls()
    {
        Dice a = new Dice(new Random(42));
        Dice b = new Dice(new Random(42));

        for (int i = 0; i < 20; i++)
        {
            assertEquals(a.roll(), b.roll());
        }
    }

    @Test
    void allSixFacesAppearRoughlyEvenly()
    {
        Dice dice = new Dice(new Random(7));
        Map<PieceType, Integer> count = new EnumMap<>(PieceType.class);

        for (int i = 0; i < 6000; i++)
        {
            count.merge(dice.roll(), 1, Integer::sum);
        }

        assertEquals(6, count.size());
        for (int n : count.values())
        {
            assertTrue(n > 800 && n < 1200, "Mặt xúc xắc lệch quá nhiều: " + count);
        }
    }

    @Test
    void neverReturnsNull()
    {
        Dice dice = new Dice(new Random());
        for (int i = 0; i < 100; i++)
        {
            assertNotNull(dice.roll());
        }
    }
}
