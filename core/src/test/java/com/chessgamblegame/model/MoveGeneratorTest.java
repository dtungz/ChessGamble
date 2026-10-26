package com.chessgamblegame.model;

import com.chessgamblegame.model.enums.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MoveGeneratorTest
{
    private Board board;

    @BeforeEach
    void setUp()
    {
        board = new Board();
    }

    private List<Position> movesFrom(String square, PieceType type)
    {
        board.playerPos = Position.parse(square);
        return MoveGenerator.playerMoves(board, type);
    }

    private void addEnemy(PieceType type, String square)
    {
        board.addEnemy(new Piece(type, Position.parse(square)));
    }

    private static Set<Position> squares(String... names)
    {
        Set<Position> set = new HashSet<>();
        for (String n : names)
        {
            set.add(Position.parse(n));
        }
        return set;
    }

    // Vua

    @Test
    void kingCanTeleportToEverySquareExceptItsOwn()
    {
        List<Position> moves = movesFrom("e1", PieceType.King);

        assertEquals(63, moves.size());
        assertFalse(moves.contains(Position.parse("e1")));
        assertTrue(moves.contains(Position.parse("a8")));
        assertTrue(moves.contains(Position.parse("h1")));
    }

    @Test
    void kingCanTeleportOntoEnemyToCaptureIt()
    {
        addEnemy(PieceType.Queen, "d8");

        List<Position> moves = movesFrom("e1", PieceType.King);

        assertEquals(63, moves.size());
        assertTrue(moves.contains(Position.parse("d8")));
    }

    @Test
    void kingMovesHaveNoDuplicates()
    {
        List<Position> moves = movesFrom("d4", PieceType.King);
        assertEquals(moves.size(), new HashSet<>(moves).size());
    }

    // Ma

    @Test
    void knightInCenterHasEightMoves()
    {
        Set<Position> moves = new HashSet<>(movesFrom("d4", PieceType.Knight));

        assertEquals(squares("c6", "e6", "f5", "f3", "e2", "c2", "b3", "b5"), moves);
    }

    @Test
    void knightInCornerHasTwoMoves()
    {
        Set<Position> moves = new HashSet<>(movesFrom("a1", PieceType.Knight));

        assertEquals(squares("b3", "c2"), moves);
    }

    @Test
    void knightOnEdgeHasFourMoves()
    {
        assertEquals(4, movesFrom("a4", PieceType.Knight).size());
    }

    @Test
    void knightJumpsOverPieces()
    {
        addEnemy(PieceType.Pawn, "d5");
        addEnemy(PieceType.Pawn, "e4");
        addEnemy(PieceType.Pawn, "c4");
        addEnemy(PieceType.Pawn, "d3");

        assertEquals(8, movesFrom("d4", PieceType.Knight).size());
    }

    @Test
    void knightCanCaptureEnemy()
    {
        addEnemy(PieceType.Rook, "c6");

        assertTrue(movesFrom("d4", PieceType.Knight).contains(Position.parse("c6")));
    }

    @Test
    void knightNeverLeavesTheBoard()
    {
        for (int x = 0; x < Board.SIZE; x++)
        {
            for (int y = 0; y < Board.SIZE; y++)
            {
                board.playerPos = new Position(x, y);
                for (Position p : MoveGenerator.playerMoves(board, PieceType.Knight))
                {
                    assertTrue(p.inBounds(), "Mã ra ngoài bàn từ " + board.playerPos.toNotation());
                }
            }
        }
    }

    // Xe

    @Test
    void rookOnEmptyBoardHasFourteenMoves()
    {
        assertEquals(14, movesFrom("d4", PieceType.Rook).size());
        assertEquals(14, movesFrom("a1", PieceType.Rook).size());
    }

    @Test
    void rookIsBlockedByEnemyAndCanCaptureIt()
    {
        addEnemy(PieceType.Pawn, "a4");

        List<Position> moves = movesFrom("a1", PieceType.Rook);

        assertTrue(moves.contains(Position.parse("a4")));
        assertFalse(moves.contains(Position.parse("a5")));
        assertEquals(squares("a2", "a3", "a4", "b1", "c1", "d1", "e1", "f1", "g1", "h1"), new HashSet<>(moves));
    }

    @Test
    void rookDoesNotMoveDiagonally()
    {
        assertFalse(movesFrom("d4", PieceType.Rook).contains(Position.parse("e5")));
    }

    @Test
    void rookSurroundedByEnemiesCanOnlyCaptureThem()
    {
        addEnemy(PieceType.Pawn, "d5");
        addEnemy(PieceType.Pawn, "d3");
        addEnemy(PieceType.Pawn, "c4");
        addEnemy(PieceType.Pawn, "e4");

        assertEquals(squares("d5", "d3", "c4", "e4"), new HashSet<>(movesFrom("d4", PieceType.Rook)));
    }

    // Tuong

    @Test
    void bishopInCenterHasThirteenMoves()
    {
        assertEquals(13, movesFrom("d4", PieceType.Bishop).size());
    }

    @Test
    void bishopInCornerHasSevenMoves()
    {
        assertEquals(squares("b2", "c3", "d4", "e5", "f6", "g7", "h8"),
            new HashSet<>(movesFrom("a1", PieceType.Bishop)));
    }

    @Test
    void bishopIsBlockedByEnemyAndCanCaptureIt()
    {
        addEnemy(PieceType.Knight, "f6");

        List<Position> moves = movesFrom("d4", PieceType.Bishop);

        assertTrue(moves.contains(Position.parse("e5")));
        assertTrue(moves.contains(Position.parse("f6")));
        assertFalse(moves.contains(Position.parse("g7")));
        assertFalse(moves.contains(Position.parse("h8")));
    }

    @Test
    void bishopDoesNotMoveStraight()
    {
        List<Position> moves = movesFrom("d4", PieceType.Bishop);
        assertFalse(moves.contains(Position.parse("d5")));
        assertFalse(moves.contains(Position.parse("e4")));
    }

    // Hau

    @Test
    void queenInCenterHasTwentySevenMoves()
    {
        assertEquals(27, movesFrom("d4", PieceType.Queen).size());
    }

    @Test
    void queenEqualsRookPlusBishop()
    {
        addEnemy(PieceType.Pawn, "d6");
        addEnemy(PieceType.Pawn, "f6");
        addEnemy(PieceType.Pawn, "b4");

        Set<Position> expected = new HashSet<>(movesFrom("d4", PieceType.Rook));
        expected.addAll(movesFrom("d4", PieceType.Bishop));

        assertEquals(expected, new HashSet<>(movesFrom("d4", PieceType.Queen)));
    }

    @Test
    void queenStopsAtFirstEnemyInEachDirection()
    {
        addEnemy(PieceType.Pawn, "d6");
        addEnemy(PieceType.Rook, "d7");

        List<Position> moves = movesFrom("d4", PieceType.Queen);

        assertTrue(moves.contains(Position.parse("d6")));
        assertFalse(moves.contains(Position.parse("d7")));
    }

    // Tot

    @Test
    void pawnMovesOneSquareUp()
    {
        assertEquals(List.of(Position.parse("e2")), movesFrom("e1", PieceType.Pawn));
    }

    @Test
    void pawnHasNoTwoSquareMove()
    {
        assertFalse(movesFrom("e2", PieceType.Pawn).contains(Position.parse("e4")));
    }

    @Test
    void pawnBlockedStraightHasNoMoves()
    {
        addEnemy(PieceType.Rook, "e5");

        assertTrue(movesFrom("e4", PieceType.Pawn).isEmpty());
    }

    @Test
    void pawnCapturesDiagonallyUp()
    {
        addEnemy(PieceType.Knight, "d5");
        addEnemy(PieceType.Bishop, "f5");

        assertEquals(squares("e5", "d5", "f5"), new HashSet<>(movesFrom("e4", PieceType.Pawn)));
    }

    @Test
    void pawnCanCaptureEvenWhenBlockedStraight()
    {
        addEnemy(PieceType.Rook, "e5");
        addEnemy(PieceType.Pawn, "f5");

        assertEquals(squares("f5"), new HashSet<>(movesFrom("e4", PieceType.Pawn)));
    }

    @Test
    void pawnDoesNotCaptureBackwardsOrSideways()
    {
        addEnemy(PieceType.Pawn, "d3");
        addEnemy(PieceType.Pawn, "d4");
        addEnemy(PieceType.Pawn, "e3");

        assertEquals(squares("e5"), new HashSet<>(movesFrom("e4", PieceType.Pawn)));
    }

    @Test
    void pawnOnTopRowHasNoMoves()
    {
        assertTrue(movesFrom("e8", PieceType.Pawn).isEmpty());
    }

    @Test
    void pawnOnEdgeDoesNotLeaveBoard()
    {
        addEnemy(PieceType.Pawn, "b5");

        assertEquals(squares("a5", "b5"), new HashSet<>(movesFrom("a4", PieceType.Pawn)));
    }

    // All

    @Test
    void everyPieceStaysOnBoardAndNeverIncludesOwnSquare()
    {
        addEnemy(PieceType.Pawn, "c6");
        addEnemy(PieceType.Rook, "f3");

        for (PieceType type : PieceType.values())
        {
            for (int x = 0; x < Board.SIZE; x++)
            {
                for (int y = 0; y < Board.SIZE; y++)
                {
                    Position from = new Position(x, y);
                    if (board.getEnemyAt(from) != null)
                    {
                        continue;
                    }
                    board.playerPos = from;
                    List<Position> moves = MoveGenerator.playerMoves(board, type);
                    for (Position p : moves)
                    {
                        assertTrue(p.inBounds(), type + " ra ngoài bàn từ " + from.toNotation());
                        assertNotEquals(from, p, type + " đứng yên tại " + from.toNotation());
                    }
                    assertEquals(moves.size(), new HashSet<>(moves).size(), type + " có ô trùng tại " + from.toNotation());
                }
            }
        }
    }
}
