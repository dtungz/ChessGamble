package com.chessgamblegame.model;

import com.chessgamblegame.model.enums.PieceType;

import java.util.Random;

public class Dice
{
    private static final PieceType[] FACES = PieceType.values();

    private final Random rnd;

    public Dice(Random rnd)
    {
        this.rnd = rnd;
    }

    public PieceType roll()
    {
        return FACES[rnd.nextInt(FACES.length)];
    }
}
