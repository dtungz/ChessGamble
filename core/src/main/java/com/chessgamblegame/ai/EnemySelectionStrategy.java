package com.chessgamblegame.ai;

import com.chessgamblegame.model.Board;
import com.chessgamblegame.model.Piece;

import java.util.List;
import java.util.Random;

public interface EnemySelectionStrategy
{
    List<Piece> select(Board board, int n, Random rnd);
}
