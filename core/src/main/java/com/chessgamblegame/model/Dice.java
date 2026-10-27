package com.chessgamblegame.model;

import com.chessgamblegame.model.enums.PieceType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class Dice
{
    private final Random rnd;
    private final Map<PieceType, Integer> weights;
    private final int totalWeight;

    public Dice(Random rnd)
    {
        this(rnd, new GameConfig().diceWeights);
    }

    public Dice(Random rnd, Map<PieceType, Integer> weights)
    {
        int total = 0;
        for (int w : weights.values())
        {
            if (w < 0)
            {
                throw new IllegalArgumentException("Trọng số xúc xắc không được âm: " + weights);
            }
            total += w;
        }
        if (total == 0)
        {
            throw new IllegalArgumentException("Tổng trọng số xúc xắc phải lớn hơn 0");
        }
        this.rnd = rnd;
        this.weights = new EnumMap<>(weights);
        this.totalWeight = total;
    }

    public PieceType roll()
    {
        int r = rnd.nextInt(totalWeight);
        for (Map.Entry<PieceType, Integer> face : weights.entrySet())
        {
            r -= face.getValue();
            if (r < 0)
            {
                return face.getKey();
            }
        }
        throw new IllegalStateException("Không thể xảy ra");
    }
}
