package com.chessgamblegame.level;

import com.chessgamblegame.model.Board;
import com.chessgamblegame.model.GameConfig;
import com.chessgamblegame.model.Piece;
import com.chessgamblegame.model.Position;
import com.chessgamblegame.model.enums.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EndlessSpawnerTest
{
    private GameConfig cfg;
    private EndlessSpawner spawner;

    @BeforeEach
    void setUp()
    {
        cfg = new GameConfig();
        spawner = new EndlessSpawner(cfg);
    }

    private Set<PieceType> typesSeenAtTurn(int turn)
    {
        Random rnd = new Random(1);
        Set<PieceType> seen = EnumSet.noneOf(PieceType.class);
        for (int i = 0; i < 500; i++)
        {
            seen.add(spawner.typeForTurn(turn, rnd));
        }
        return seen;
    }

    // Bàn bắt đầu

    @Test
    void startBoardHasPlayerAndTwoPawnsOnTopRow()
    {
        Board board = spawner.createStartBoard(new Random(0));

        assertEquals(Position.parse("e1"), board.playerPos);
        assertEquals(2, board.getEnemies().size());
        for (Piece e : board.getEnemies())
        {
            assertEquals(PieceType.Pawn, e.type);
            assertEquals(7, e.pos.y());
        }
    }

    @Test
    void startPawnCountComesFromConfig()
    {
        cfg.endlessStartPawns = 4;

        assertEquals(4, spawner.createStartBoard(new Random(0)).getEnemies().size());
    }

    // Độ khó tăng dần

    @Test
    void enemiesPerTurnGrowsEveryTenTurnsUpToThree()
    {
        assertEquals(1, spawner.enemiesPerTurn(1));
        assertEquals(1, spawner.enemiesPerTurn(10));
        assertEquals(2, spawner.enemiesPerTurn(11));
        assertEquals(3, spawner.enemiesPerTurn(21));
        assertEquals(3, spawner.enemiesPerTurn(100));
    }

    @Test
    void spawnHappensEveryThreeTurns()
    {
        assertFalse(spawner.shouldSpawn(1));
        assertFalse(spawner.shouldSpawn(2));
        assertTrue(spawner.shouldSpawn(3));
        assertFalse(spawner.shouldSpawn(4));
        assertTrue(spawner.shouldSpawn(6));
    }

    @Test
    void earlyTurnsOnlySpawnPawns()
    {
        assertEquals(EnumSet.of(PieceType.Pawn), typesSeenAtTurn(1));
        assertEquals(EnumSet.of(PieceType.Pawn), typesSeenAtTurn(10));
    }

    @Test
    void strongerPiecesUnlockOverTime()
    {
        assertEquals(EnumSet.of(PieceType.Pawn, PieceType.Knight, PieceType.Bishop), typesSeenAtTurn(11));
        assertEquals(EnumSet.of(PieceType.Pawn, PieceType.Knight, PieceType.Bishop, PieceType.Rook), typesSeenAtTurn(21));
        assertEquals(EnumSet.of(PieceType.Pawn, PieceType.Knight, PieceType.Bishop, PieceType.Rook, PieceType.Queen),
            typesSeenAtTurn(31));
        assertFalse(typesSeenAtTurn(500).contains(PieceType.King));
    }

    // Sinh quân

    @Test
    void spawnPutsNewPieceOnEmptyTopSquare()
    {
        Board board = new Board();
        board.playerPos = Position.parse("e1");

        Piece e = spawner.spawn(board, 1, new Random(0));

        assertNotNull(e);
        assertEquals(7, e.pos.y());
        assertSame(e, board.getEnemyAt(e.pos));
    }

    @Test
    void spawnNeverReusesAnOccupiedSquare()
    {
        Board board = new Board();
        board.playerPos = Position.parse("e1");
        Random rnd = new Random(3);

        Set<Position> used = new HashSet<>();
        for (int i = 0; i < 8; i++)
        {
            Piece e = spawner.spawn(board, 1, rnd);
            assertTrue(used.add(e.pos), "Sinh trùng ô " + e.pos.toNotation());
        }
    }

    @Test
    void fullTopRowMeansNoSpawn()
    {
        Board board = new Board();
        board.playerPos = Position.parse("e1");
        for (int x = 0; x < Board.SIZE; x++)
        {
            board.addEnemy(new Piece(PieceType.Pawn, new Position(x, 7)));
        }

        assertNull(spawner.spawn(board, 1, new Random(0)));
        assertEquals(8, board.getEnemies().size());
    }

    @Test
    void playerOnTopRowSquareIsNeverUsed()
    {
        Board board = new Board();
        board.playerPos = Position.parse("d8");
        Random rnd = new Random(5);

        for (int i = 0; i < 7; i++)
        {
            assertNotEquals(Position.parse("d8"), spawner.spawn(board, 1, rnd).pos);
        }
        assertNull(spawner.spawn(board, 1, rnd));
    }

    @Test
    void boardLimitStopsSpawning()
    {
        cfg.endlessMaxEnemiesOnBoard = 2;
        Board board = new Board();
        board.playerPos = Position.parse("e1");
        Random rnd = new Random(0);

        assertNotNull(spawner.spawn(board, 1, rnd));
        assertNotNull(spawner.spawn(board, 1, rnd));
        assertNull(spawner.spawn(board, 1, rnd));
    }
}
