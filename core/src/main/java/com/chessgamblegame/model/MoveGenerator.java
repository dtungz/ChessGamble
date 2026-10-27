package com.chessgamblegame.model;

import com.chessgamblegame.model.enums.PieceType;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator
{
    private static final int[][] KNIGHT_STEPS = {
        {1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}
    };
    private static final int[][] ORTHOGONAL = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] DIAGONAL = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    private static final int[][] ALL_DIRECTIONS = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private MoveGenerator()
    {
    }
    public static List<Position> playerMoves(Board board, PieceType type)
    {
        Position from = board.playerPos;
        return switch (type)
        {
            case King -> kingTeleport(from);
            case Knight -> knightMoves(from);
            case Rook -> playerSlide(board, from, ORTHOGONAL);
            case Bishop -> playerSlide(board, from, DIAGONAL);
            case Queen -> playerSlide(board, from, ALL_DIRECTIONS);
            case Pawn -> playerPawnMoves(board, from);
        };
    }

    public static List<Position> enemyMoves(Board board, Piece enemy)
    {
        return enemyMoves(board, enemy.type, enemy.pos);
    }

    public static List<Position> enemyMoves(Board board, PieceType type, Position from)
    {
        return switch (type)
        {
            case Knight -> enemyKnightMoves(board, from);
            case Rook -> enemySlide(board, from, ORTHOGONAL);
            case Bishop -> enemySlide(board, from, DIAGONAL);
            case Queen -> enemySlide(board, from, ALL_DIRECTIONS);
            case Pawn -> enemyPawnMoves(board, from);
            case King -> throw new IllegalArgumentException("Địch không có Vua");
        };
    }

    private static List<Position> kingTeleport(Position from)
    {
        List<Position> moves = new ArrayList<>();
        for (int x = 0; x < Board.SIZE; x++)
        {
            for (int y = 0; y < Board.SIZE; y++)
            {
                Position p = new Position(x, y);
                if (!p.equals(from))
                {
                    moves.add(p);
                }
            }
        }
        return moves;
    }

    private static List<Position> knightMoves(Position from)
    {
        List<Position> moves = new ArrayList<>();
        for (int[] step : KNIGHT_STEPS)
        {
            Position p = new Position(from.x() + step[0], from.y() + step[1]);
            if (p.inBounds())
            {
                moves.add(p);
            }
        }
        return moves;
    }

    // Xe, Tuong, Hau xe di theo duong thang, Di het duong hay gap quan dich thi dung lai
    private static List<Position> playerSlide(Board board, Position from, int[][] directions)
    {
        List<Position> moves = new ArrayList<>();
        for (int[] d : directions)
        {
            Position p = new Position(from.x() + d[0], from.y() + d[1]);
            while (p.inBounds())
            {
                moves.add(p);
                if (board.getEnemyAt(p) != null)
                {
                    break;
                }
                p = new Position(p.x() + d[0], p.y() + d[1]);
            }
        }
        return moves;
    }

    private static List<Position> playerPawnMoves(Board board, Position from)
    {
        List<Position> moves = new ArrayList<>();

        Position forward = new Position(from.x(), from.y() + 1);
        if (forward.inBounds() && board.isEmpty(forward))
        {
            moves.add(forward);
        }

        for (int dx : new int[]{-1, 1})
        {
            Position diagonal = new Position(from.x() + dx, from.y() + 1);
            if (diagonal.inBounds() && board.getEnemyAt(diagonal) != null)
            {
                moves.add(diagonal);
            }
        }
        return moves;
    }

    private static List<Position> enemyKnightMoves(Board board, Position from)
    {
        List<Position> moves = new ArrayList<>();
        for (Position p : knightMoves(from))
        {
            if (board.getEnemyAt(p) == null)
            {
                moves.add(p);
            }
        }
        return moves;
    }

    private static List<Position> enemySlide(Board board, Position from, int[][] directions)
    {
        List<Position> moves = new ArrayList<>();
        for (int[] d : directions)
        {
            Position p = new Position(from.x() + d[0], from.y() + d[1]);
            while (p.inBounds() && board.getEnemyAt(p) == null)
            {
                moves.add(p);
                if (p.equals(board.playerPos))
                {
                    break;
                }
                p = new Position(p.x() + d[0], p.y() + d[1]);
            }
        }
        return moves;
    }

    private static List<Position> enemyPawnMoves(Board board, Position from)
    {
        List<Position> moves = new ArrayList<>();

        Position forward = new Position(from.x(), from.y() - 1);
        if (forward.inBounds() && board.isEmpty(forward))
        {
            moves.add(forward);
        }

        for (int dx : new int[]{-1, 1})
        {
            Position diagonal = new Position(from.x() + dx, from.y() - 1);
            if (diagonal.equals(board.playerPos))
            {
                moves.add(diagonal);
            }
        }
        return moves;
    }
}
