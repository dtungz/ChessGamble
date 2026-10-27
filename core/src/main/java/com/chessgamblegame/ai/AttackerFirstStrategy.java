package com.chessgamblegame.ai;

import com.chessgamblegame.model.Board;
import com.chessgamblegame.model.Piece;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AttackerFirstStrategy implements EnemySelectionStrategy
{
    @Override
    public List<Piece> select(Board board, int n, Random rnd)
    {
        Map<Piece, Integer> distance = new HashMap<>();
        for (Piece e : board.getEnemies())
        {
            int d = PathFinder.distanceToPlayer(board, e);
            distance.put(e, d == -1 ? Integer.MAX_VALUE : d);
        }

        // Xáo trộn trước rồi mới sắp xếp: List.sort giữ nguyên thứ tự các quân bằng nhau,
        // nên các quân cùng khoảng cách sẽ có thứ tự ngẫu nhiên.
        List<Piece> candidates = new ArrayList<>(board.getEnemies());
        Collections.shuffle(candidates, rnd);
        candidates.sort(Comparator.comparingInt(distance::get));

        int count = Math.max(0, Math.min(n, candidates.size()));
        return new ArrayList<>(candidates.subList(0, count));
    }
}
