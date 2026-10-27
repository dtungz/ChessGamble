package com.chessgamblegame.model;

import com.chessgamblegame.model.enums.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EnemyMovesTest
{
    private Board board;

    @BeforeEach
    void setUp()
    {
        board = new Board();
        board.playerPos = Position.parse("h1");
    }

    private Piece addEnemy(PieceType type, String square)
    {
        Piece e = new Piece(type, Position.parse(square));
        board.addEnemy(e);
        return e;
    }

    private List<Position> movesOf(Piece e)
    {
        return MoveGenerator.enemyMoves(board, e);
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

    // Mã

    @Test
    void knightInCenterHasEightMoves()
    {
        Piece knight = addEnemy(PieceType.Knight, "d4");

        assertEquals(squares("c6", "e6", "f5", "f3", "e2", "c2", "b3", "b5"), new HashSet<>(movesOf(knight)));
    }

    @Test
    void knightCannotLandOnAnotherEnemy()
    {
        Piece knight = addEnemy(PieceType.Knight, "d4");
        addEnemy(PieceType.Pawn, "c6");

        assertFalse(movesOf(knight).contains(Position.parse("c6")));
        assertEquals(7, movesOf(knight).size());
    }

    @Test
    void knightCanAttackPlayer()
    {
        board.playerPos = Position.parse("e6");
        Piece knight = addEnemy(PieceType.Knight, "d4");

        assertTrue(movesOf(knight).contains(Position.parse("e6")));
    }

    // Xe

    @Test
    void rookOnEmptyBoardHasFourteenMoves()
    {
        Piece rook = addEnemy(PieceType.Rook, "d4");

        assertEquals(14, movesOf(rook).size());
    }

    @Test
    void rookStopsBeforeAnotherEnemy()
    {
        Piece rook = addEnemy(PieceType.Rook, "a8");
        addEnemy(PieceType.Pawn, "a5");

        List<Position> moves = movesOf(rook);

        assertTrue(moves.contains(Position.parse("a6")));
        assertFalse(moves.contains(Position.parse("a5")));
        assertFalse(moves.contains(Position.parse("a4")));
    }

    @Test
    void rookAttacksPlayerAndDoesNotPassThrough()
    {
        board.playerPos = Position.parse("a4");
        Piece rook = addEnemy(PieceType.Rook, "a8");

        List<Position> moves = movesOf(rook);

        assertTrue(moves.contains(Position.parse("a4")));
        assertFalse(moves.contains(Position.parse("a3")));
    }

    // Tượng

    @Test
    void bishopAttacksPlayerOnDiagonal()
    {
        board.playerPos = Position.parse("b2");
        Piece bishop = addEnemy(PieceType.Bishop, "f6");

        List<Position> moves = movesOf(bishop);

        assertTrue(moves.contains(Position.parse("b2")));
        assertFalse(moves.contains(Position.parse("a1")));
    }

    @Test
    void bishopBlockedByEnemyCannotReachPlayer()
    {
        board.playerPos = Position.parse("b2");
        Piece bishop = addEnemy(PieceType.Bishop, "f6");
        addEnemy(PieceType.Pawn, "d4");

        List<Position> moves = movesOf(bishop);

        assertTrue(moves.contains(Position.parse("e5")));
        assertFalse(moves.contains(Position.parse("d4")));
        assertFalse(moves.contains(Position.parse("b2")));
    }

    // Hậu

    @Test
    void queenInCenterHasTwentySevenMoves()
    {
        Piece queen = addEnemy(PieceType.Queen, "d4");

        assertEquals(27, movesOf(queen).size());
    }

    @Test
    void queenSurroundedByEnemiesHasNoMoves()
    {
        Piece queen = addEnemy(PieceType.Queen, "d4");
        for (String s : new String[]{"c3", "c4", "c5", "d3", "d5", "e3", "e4", "e5"})
        {
            addEnemy(PieceType.Pawn, s);
        }

        assertTrue(movesOf(queen).isEmpty());
    }

    // Tốt địch

    @Test
    void pawnMovesOneSquareDown()
    {
        Piece pawn = addEnemy(PieceType.Pawn, "d7");

        assertEquals(List.of(Position.parse("d6")), movesOf(pawn));
    }

    @Test
    void pawnHasNoTwoSquareFirstMove()
    {
        Piece pawn = addEnemy(PieceType.Pawn, "d7");

        assertFalse(movesOf(pawn).contains(Position.parse("d5")));
    }

    @Test
    void pawnBlockedByEnemyHasNoMoves()
    {
        Piece pawn = addEnemy(PieceType.Pawn, "d7");
        addEnemy(PieceType.Rook, "d6");

        assertTrue(movesOf(pawn).isEmpty());
    }

    @Test
    void pawnCannotAttackPlayerStraightAhead()
    {
        board.playerPos = Position.parse("d6");
        Piece pawn = addEnemy(PieceType.Pawn, "d7");

        assertTrue(movesOf(pawn).isEmpty());
    }

    @Test
    void pawnAttacksPlayerDiagonallyDown()
    {
        board.playerPos = Position.parse("e6");
        Piece pawn = addEnemy(PieceType.Pawn, "d7");

        assertEquals(squares("d6", "e6"), new HashSet<>(movesOf(pawn)));
    }

    @Test
    void pawnDoesNotMoveDiagonallyWithoutPlayer()
    {
        Piece pawn = addEnemy(PieceType.Pawn, "d7");
        addEnemy(PieceType.Knight, "e6");

        assertFalse(movesOf(pawn).contains(Position.parse("c6")));
        assertFalse(movesOf(pawn).contains(Position.parse("e6")));
    }

    @Test
    void pawnDoesNotAttackPlayerAboveIt()
    {
        board.playerPos = Position.parse("e8");
        Piece pawn = addEnemy(PieceType.Pawn, "d7");

        assertFalse(movesOf(pawn).contains(Position.parse("e8")));
    }

    @Test
    void pawnOnBottomRowHasNoMoves()
    {
        Piece pawn = addEnemy(PieceType.Pawn, "d1");

        assertTrue(movesOf(pawn).isEmpty());
    }

    // Chung

    @Test
    void enemyCannotBeKing()
    {
        assertThrows(IllegalArgumentException.class,
            () -> MoveGenerator.enemyMoves(board, PieceType.King, Position.parse("d4")));
    }

    @Test
    void virtualPositionOverloadMatchesRealPiece()
    {
        Piece rook = addEnemy(PieceType.Rook, "c3");

        assertEquals(movesOf(rook), MoveGenerator.enemyMoves(board, PieceType.Rook, Position.parse("c3")));
    }

    @Test
    void enemyNeverLandsOnAnotherEnemyOrLeavesBoard()
    {
        addEnemy(PieceType.Pawn, "c6");
        addEnemy(PieceType.Rook, "f3");
        board.playerPos = Position.parse("e4");

        for (PieceType type : new PieceType[]{PieceType.Pawn, PieceType.Knight, PieceType.Bishop, PieceType.Rook, PieceType.Queen})
        {
            for (int x = 0; x < Board.SIZE; x++)
            {
                for (int y = 0; y < Board.SIZE; y++)
                {
                    Position from = new Position(x, y);
                    if (!board.isEmpty(from))
                    {
                        continue;
                    }
                    List<Position> moves = MoveGenerator.enemyMoves(board, type, from);
                    for (Position p : moves)
                    {
                        assertTrue(p.inBounds(), type + " ra ngoài bàn từ " + from.toNotation());
                        assertNull(board.getEnemyAt(p), type + " đè lên quân địch khác từ " + from.toNotation());
                        assertNotEquals(from, p);
                    }
                    assertEquals(moves.size(), new HashSet<>(moves).size(), type + " có ô trùng tại " + from.toNotation());
                }
            }
        }
    }
}
