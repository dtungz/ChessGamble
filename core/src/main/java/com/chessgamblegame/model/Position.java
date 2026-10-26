package com.chessgamblegame.model;

public record Position(int x, int y)
{
    public boolean inBounds()
    {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    public static Position parse(String s)
    {
        return new Position (s.charAt(0) - 'a', s.charAt(1) - '1');
    }

    public String toNotation()
    {
        return "" + (char) ('a' + x) + (char) ('1' + y);
    }
}
