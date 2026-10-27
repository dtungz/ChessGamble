package com.chessgamblegame.ai;

import com.chessgamblegame.model.Board;
import com.chessgamblegame.model.Piece;
import com.chessgamblegame.model.Position;
import com.chessgamblegame.model.enums.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AttackerFirstStrategyTest
{
    private Board board;
    private final EnemySelectionStrategy strategy = new AttackerFirstStrategy();

    @BeforeEach
    void setUp()
    {
        board = new Board();
        board.playerPos = Position.parse("e1");
    }

    private Piece addEnemy(PieceType type, String square)
    {
        Piece e = new Piece(type, Position.parse(square));
        board.addEnemy(e);
        return e;
    }

    // Ưu tiên

    @Test
    void attackerIsChosenBeforeCloserLookingPieces()
    {
        Piece rook = addEnemy(PieceType.Rook, "a8");      // 2 nước
        Piece knight = addEnemy(PieceType.Knight, "f3");  // 1 nước = đang tấn công
        addEnemy(PieceType.Pawn, "d7");                  // 6 nước

        // List.of là cdj wtf
        assertEquals(List.of(knight), strategy.select(board, 1, new Random(0)));
        assertNotEquals(List.of(rook), strategy.select(board, 1, new Random(0)));
    }

    @Test
    void remainingSlotsGoToClosestPieces()
    {
        Piece knight = addEnemy(PieceType.Knight, "f3");  // 1
        Piece rook = addEnemy(PieceType.Rook, "a8");      // 2
        addEnemy(PieceType.Pawn, "d7");                  // 6

        assertEquals(List.of(knight, rook), strategy.select(board, 2, new Random(0)));
    }

    @Test
    void resultIsOrderedByDistance()
    {
        Piece pawn = addEnemy(PieceType.Pawn, "d7");      // 6
        Piece rook = addEnemy(PieceType.Rook, "a8");      // 2
        Piece knight = addEnemy(PieceType.Knight, "f3");  // 1

        assertEquals(List.of(knight, rook, pawn), strategy.select(board, 3, new Random(5)));
    }

    @Test
    void unreachablePiecesComeLast()
    {
        board.playerPos = Position.parse("b1");
        addEnemy(PieceType.Bishop, "a1");                // khác màu ô -> không tới được
        Piece pawn = addEnemy(PieceType.Pawn, "c7");      // 6 nước, xa nhưng tới được

        assertEquals(List.of(pawn), strategy.select(board, 1, new Random(0)));
    }

    // Nhiều quân cùng ưu tiên -> ngẫu nhiên

    @Test
    void whenTooManyAttackersOnlyAttackersAreChosen()
    {
        Piece rook = addEnemy(PieceType.Rook, "e8");      // 1
        Piece knight = addEnemy(PieceType.Knight, "f3");  // 1
        Piece queen = addEnemy(PieceType.Queen, "a5");    // 1
        Piece pawn = addEnemy(PieceType.Pawn, "d7");      // 6

        // Dùng 1 Random chung cho cả vòng lặp, giống game thật.
        // (new Random(0), new Random(1)... cho số đầu tiên gần như giống nhau nên không dùng được.)
        Random rnd = new Random(1);
        Set<Set<Piece>> differentPicks = new HashSet<>();
        for (int i = 0; i < 50; i++)
        {
            List<Piece> picked = strategy.select(board, 2, rnd);

            assertEquals(2, picked.size());
            assertFalse(picked.contains(pawn));
            assertTrue(Set.of(rook, knight, queen).containsAll(picked));
            differentPicks.add(new HashSet<>(picked));
        }
        assertTrue(differentPicks.size() > 1, "Luôn chọn cùng 1 cặp, không ngẫu nhiên");
    }

    @Test
    void tiesAreBrokenRandomly()
    {
        Piece left = addEnemy(PieceType.Rook, "a8");      // 2
        Piece right = addEnemy(PieceType.Rook, "h8");     // 2

        Random rnd = new Random(1);
        Set<Piece> seen = new HashSet<>();
        for (int i = 0; i < 50; i++)
        {
            seen.addAll(strategy.select(board, 1, rnd));
        }
        assertEquals(Set.of(left, right), seen);
    }

    @Test
    void sameSeedGivesSameChoice()
    {
        addEnemy(PieceType.Rook, "a8");
        addEnemy(PieceType.Rook, "h8");
        addEnemy(PieceType.Knight, "b8");

        assertEquals(strategy.select(board, 2, new Random(42)), strategy.select(board, 2, new Random(42)));
    }

    // Biên

    @Test
    void askingForMoreThanAvailableReturnsAll()
    {
        addEnemy(PieceType.Rook, "a8");
        addEnemy(PieceType.Pawn, "d7");

        assertEquals(2, strategy.select(board, 5, new Random(0)).size());
    }

    @Test
    void zeroOrNegativeCountReturnsEmpty()
    {
        addEnemy(PieceType.Rook, "a8");

        assertTrue(strategy.select(board, 0, new Random(0)).isEmpty());
        assertTrue(strategy.select(board, -1, new Random(0)).isEmpty());
    }

    @Test
    void noEnemiesReturnsEmpty()
    {
        assertTrue(strategy.select(board, 3, new Random(0)).isEmpty());
    }

    @Test
    void neverPicksTheSamePieceTwice()
    {
        addEnemy(PieceType.Rook, "a8");
        addEnemy(PieceType.Rook, "h8");
        addEnemy(PieceType.Knight, "f3");
        addEnemy(PieceType.Pawn, "d7");

        List<Piece> picked = strategy.select(board, 4, new Random(9));

        assertEquals(4, new HashSet<>(picked).size());
    }

    @Test
    void selectingDoesNotChangeTheBoard()
    {
        Piece rook = addEnemy(PieceType.Rook, "a8");
        Piece knight = addEnemy(PieceType.Knight, "f3");

        strategy.select(board, 2, new Random(0));

        assertEquals(Position.parse("a8"), rook.pos);
        assertEquals(Position.parse("f3"), knight.pos);
        assertFalse(rook.shaking);
        assertFalse(knight.shaking);
        assertEquals(2, board.getEnemies().size());
    }
}
