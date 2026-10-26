package com.chessgamblegame.model;

import com.chessgamblegame.model.enums.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest
{
    private Board board;

    @BeforeEach
    void setUp()
    {
        board = new Board();
        board.playerPos = Position.parse("e1");
    }

    @Test
    void addEnemyThenFindIt()
    {
        Piece knight = new Piece(PieceType.Knight, Position.parse("b8"));
        board.addEnemy(knight);

        assertSame(knight, board.getEnemyAt(Position.parse("b8")));
        assertEquals(1, board.getEnemies().size());
    }

    @Test
    void emptySquareHasNoEnemy()
    {
        assertNull(board.getEnemyAt(Position.parse("d4")));
        assertTrue(board.isEmpty(Position.parse("d4")));
    }

    @Test
    void playerSquareIsNotEmpty()
    {
        assertFalse(board.isEmpty(Position.parse("e1")));
    }

    @Test
    void moveEnemyUpdatesPosition()
    {
        Piece pawn = new Piece(PieceType.Pawn, Position.parse("b8"));
        board.addEnemy(pawn);

        board.moveEnemy(pawn, Position.parse("b7"));

        assertEquals(Position.parse("b7"), pawn.pos);
        assertTrue(board.isEmpty(Position.parse("b8")));
        assertSame(pawn, board.getEnemyAt(Position.parse("b7")));
    }

    @Test
    void removeEnemy()
    {
        Piece rook = new Piece(PieceType.Rook, Position.parse("a8"));
        board.addEnemy(rook);

        board.removeEnemy(rook);

        assertTrue(board.getEnemies().isEmpty());
        assertTrue(board.isEmpty(Position.parse("a8")));
    }

    @Test
    void cannotAddTwoPiecesOnSameSquare()
    {
        board.addEnemy(new Piece(PieceType.Pawn, Position.parse("c7")));

        assertThrows(IllegalArgumentException.class,
            () -> board.addEnemy(new Piece(PieceType.Queen, Position.parse("c7"))));
    }

    @Test
    void cannotAddEnemyOnPlayer()
    {
        assertThrows(IllegalArgumentException.class,
            () -> board.addEnemy(new Piece(PieceType.Pawn, Position.parse("e1"))));
    }

    @Test
    void cannotMoveOntoAnotherEnemy()
    {
        Piece a = new Piece(PieceType.Pawn, Position.parse("a7"));
        Piece b = new Piece(PieceType.Pawn, Position.parse("a6"));
        board.addEnemy(a);
        board.addEnemy(b);

        assertThrows(IllegalArgumentException.class, () -> board.moveEnemy(a, Position.parse("a6")));
        assertEquals(Position.parse("a7"), a.pos);  // không di chuyển khi lỗi
    }

    @Test
    void cannotMoveOutOfBoard()
    {
        Piece pawn = new Piece(PieceType.Pawn, Position.parse("a1"));
        board.addEnemy(pawn);

        assertThrows(IllegalArgumentException.class, () -> board.moveEnemy(pawn, new Position(0, -1)));
    }

    @Test
    void enemyListIsReadOnly()
    {
        assertThrows(UnsupportedOperationException.class,
            () -> board.getEnemies().add(new Piece(PieceType.Pawn, Position.parse("a7"))));
    }
}
