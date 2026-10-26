package com.chessgamblegame.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Board
{
    public static final int SIZE = 8;

    public Position playerPos;
    private final List<Piece> enemies = new ArrayList<>();

    public Piece getEnemyAt(Position p)
    {
        for (Piece e : enemies)
        {
            if (e.pos.equals(p))
            {
                return e;
            }
        }
        return null;
    }

    public boolean isEmpty(Position p)
    {
        return getEnemyAt(p) == null && !p.equals(playerPos);
    }

    public List<Piece> getEnemies()
    {
        return Collections.unmodifiableList(enemies);
    }

    public void addEnemy(Piece e)
    {
        if (!e.pos.inBounds())
        {
            throw new IllegalArgumentException("Ô nằm ngoài bàn cờ: " + e.pos);
        }
        if (!isEmpty(e.pos))
        {
            throw new IllegalArgumentException("Ô đã có quân: " + e.pos.toNotation());
        }
        enemies.add(e);
    }

    public void removeEnemy(Piece e)
    {
        enemies.remove(e);
    }

    public void moveEnemy(Piece e, Position to)
    {
        if (!to.inBounds())
        {
            throw new IllegalArgumentException("Ô nằm ngoài bàn cờ: " + to);
        }
        if (!isEmpty(to))
        {
            throw new IllegalArgumentException("Ô đích không trống: " + to.toNotation());
        }
        e.pos = to;
    }
}
