package com.chessgamblegame.model;

import com.chessgamblegame.model.enums.PieceType;

public class Piece
{
    public PieceType type;
    public Position pos;
    public boolean shaking;

    public Piece(PieceType type, Position pos)
    {
        this.type = type;
        this.pos = pos;
    }

    @Override
    public String toString()
    {
        return type + "@" + pos.toNotation();
    }
}
