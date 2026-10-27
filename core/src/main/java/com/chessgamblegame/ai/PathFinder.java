package com.chessgamblegame.ai;

import com.chessgamblegame.model.Board;
import com.chessgamblegame.model.MoveGenerator;
import com.chessgamblegame.model.Piece;
import com.chessgamblegame.model.Position;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class PathFinder
{
    private PathFinder()
    {
    }

    // Trả về nước đi tiếp theo của địch, null nếu không có đường
    public static Position nextStep(Board board, Piece enemy)
    {
        List<Position> path = shortestPath(board, enemy);
        return path == null ? null : path.get(0);
    }

    // Số nước của đường đi ngắn nhất tới người chơi, nếu không tới được trả về -1, nếu đang chếu thì trả về 1
    public static int distanceToPlayer(Board board, Piece enemy)
    {
        List<Position> path = shortestPath(board, enemy);
        return path == null ? -1 : path.size();
    }

    // Tìm đường đi ngắn nhất tới người chơi, các quần ịch còn lại coi như ật cản, không có đường trả về null
    public static List<Position> shortestPath(Board board, Piece enemy)
    {
        Position start = enemy.pos;
        Position target = board.playerPos;

        Queue<Position> queue = new ArrayDeque<>();
        Map<Position, Position> cameFrom = new HashMap<>();
        queue.add(start);
        cameFrom.put(start, null);

        while (!queue.isEmpty())
        {
            Position cur = queue.poll();
            for (Position next : MoveGenerator.enemyMoves(board, enemy.type, cur))
            {
                if (cameFrom.containsKey(next))
                {
                    continue;
                }
                cameFrom.put(next, cur);
                if (next.equals(target))
                {
                    return buildPath(cameFrom, next);
                }
                queue.add(next);
            }
        }
        return null;
    }

    // Lần ngược từ ô cuối về ô xuất phát (ô có cameFrom = null), rồi đảo lại cho đúng thứ tự đi.
    private static List<Position> buildPath(Map<Position, Position> cameFrom, Position end)
    {
        LinkedList<Position> path = new LinkedList<>();
        for (Position p = end; cameFrom.get(p) != null; p = cameFrom.get(p))
        {
            path.addFirst(p);
        }
        return path;
    }
}
