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

    // Trả về nước đi tiếp theo của địch. Không có đường tới người chơi thì đi tới ô gần người chơi nhất;
    // null chỉ khi quân địch không có nước đi nào (đứng yên).
    public static Position nextStep(Board board, Piece enemy)
    {
        List<Position> path = shortestPath(board, enemy);
        if (path != null)
        {
            return path.get(0);
        }
        return closestMove(board, enemy);
    }

    // Chọn nước làm quân địch đứng gần người chơi nhất, kể cả khi không gần hơn chỗ đang đứng
    // (để Tốt đã đi quá người chơi vẫn tiếp tục đi xuống và được phong cấp).
    private static Position closestMove(Board board, Piece enemy)
    {
        Position best = null;
        for (Position p : MoveGenerator.enemyMoves(board, enemy))
        {
            if (best == null || isCloser(p, best, board.playerPos))
            {
                best = p;
            }
        }
        return best;
    }

    // So theo khoảng cách Chebyshev (số bước vua), bằng nhau thì so Manhattan.
    private static boolean isCloser(Position a, Position b, Position target)
    {
        int ca = chebyshev(a, target);
        int cb = chebyshev(b, target);
        if (ca != cb)
        {
            return ca < cb;
        }
        return manhattan(a, target) < manhattan(b, target);
    }

    private static int chebyshev(Position a, Position b)
    {
        return Math.max(Math.abs(a.x() - b.x()), Math.abs(a.y() - b.y()));
    }

    private static int manhattan(Position a, Position b)
    {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
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
