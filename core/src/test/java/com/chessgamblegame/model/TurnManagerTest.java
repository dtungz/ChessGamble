package com.chessgamblegame.model;

import com.chessgamblegame.ai.AttackerFirstStrategy;
import com.chessgamblegame.model.TurnManager.Phase;
import com.chessgamblegame.model.enums.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class
TurnManagerTest
{
    private Board board;
    private GameConfig cfg;

    @BeforeEach
    void setUp()
    {
        board = new Board();
        board.playerPos = Position.parse("e1");
        cfg = new GameConfig();
    }

    private Piece addEnemy(PieceType type, String square)
    {
        Piece e = new Piece(type, Position.parse(square));
        board.addEnemy(e);
        return e;
    }

    private TurnManager newGame(int enemiesPerTurn)
    {
        return new TurnManager(board, cfg, enemiesPerTurn, new Random(0), new AttackerFirstStrategy());
    }

    // Xúc xắc chỉ ra 1 mặt duy nhất -> test không phụ thuộc may rủi.
    private void diceAlways(PieceType face)
    {
        for (PieceType t : PieceType.values())
        {
            cfg.diceWeights.put(t, t == face ? 1 : 0);
        }
    }

    // Trạng thái ban đầu

    @Test
    void newGameWaitsForRoll()
    {
        TurnManager tm = newGame(1);

        assertEquals(Phase.WAIT_ROLL, tm.getPhase());
        assertEquals(3, tm.getHp());
        assertEquals(0, tm.getScore());
        assertEquals(1, tm.getTurnNumber());
        assertNull(tm.getRolledType());
        assertTrue(tm.getValidMoves().isEmpty());
    }

    @Test
    void startHpComesFromConfig()
    {
        cfg.startHp = 2;

        assertEquals(2, newGame(1).getHp());
    }

    @Test
    void playerPositionIsRequired()
    {
        board.playerPos = null;

        assertThrows(IllegalArgumentException.class, () -> newGame(1));
    }

    // Chọn quân rung

    @Test
    void attackerIsShakingAtStart()
    {
        Piece knight = addEnemy(PieceType.Knight, "f3");  // đang tấn công e1
        Piece rook = addEnemy(PieceType.Rook, "a8");

        TurnManager tm = newGame(1);

        assertEquals(List.of(knight), tm.getShakingEnemies());
        assertTrue(knight.shaking);
        assertFalse(rook.shaking);
    }

    @Test
    void numberOfShakingEnemiesEqualsEnemiesPerTurn()
    {
        addEnemy(PieceType.Rook, "a8");
        addEnemy(PieceType.Rook, "h8");
        addEnemy(PieceType.Knight, "b8");
        addEnemy(PieceType.Pawn, "d7");

        TurnManager tm = newGame(2);

        assertEquals(2, tm.getShakingEnemies().size());
        assertEquals(2, board.getEnemies().stream().filter(e -> e.shaking).count());
    }

    @Test
    void fewerEnemiesThanEnemiesPerTurnShakesAll()
    {
        addEnemy(PieceType.Rook, "a8");

        TurnManager tm = newGame(3);

        assertEquals(1, tm.getShakingEnemies().size());
    }

    @Test
    void newTurnClearsOldShakingFlags()
    {
        Piece knight = addEnemy(PieceType.Knight, "f3");  // tấn công e1
        Piece rook = addEnemy(PieceType.Rook, "a8");      // tấn công a1
        TurnManager tm = newGame(1);
        assertTrue(knight.shaking);

        board.playerPos = Position.parse("a1");
        tm.startPlayerTurn();

        assertFalse(knight.shaking);
        assertTrue(rook.shaking);
        assertEquals(List.of(rook), tm.getShakingEnemies());
    }

    @Test
    void newTurnIncreasesTurnNumberAndResetsRoll()
    {
        diceAlways(PieceType.Queen);
        TurnManager tm = newGame(1);
        tm.rollDice();

        tm.startPlayerTurn();

        assertEquals(2, tm.getTurnNumber());
        assertEquals(Phase.WAIT_ROLL, tm.getPhase());
        assertNull(tm.getRolledType());
        assertTrue(tm.getValidMoves().isEmpty());
    }

    // Tung xúc xắc

    @Test
    void rollingGivesValidMovesForThatFace()
    {
        diceAlways(PieceType.Knight);
        TurnManager tm = newGame(1);

        assertEquals(PieceType.Knight, tm.rollDice());

        assertEquals(PieceType.Knight, tm.getRolledType());
        assertEquals(Phase.WAIT_PLAYER_MOVE, tm.getPhase());
        assertEquals(MoveGenerator.playerMoves(board, PieceType.Knight), tm.getValidMoves());
    }

    @Test
    void blockedPawnMustSkip()
    {
        diceAlways(PieceType.Pawn);
        addEnemy(PieceType.Rook, "e2");
        TurnManager tm = newGame(1);

        tm.rollDice();

        assertEquals(Phase.WAIT_SKIP, tm.getPhase());
        assertTrue(tm.getValidMoves().isEmpty());
    }

    @Test
    void pawnOnTopRowMustSkip()
    {
        diceAlways(PieceType.Pawn);
        board.playerPos = Position.parse("e8");
        TurnManager tm = newGame(1);

        tm.rollDice();

        assertEquals(Phase.WAIT_SKIP, tm.getPhase());
    }

    @Test
    void cannotRollTwiceInOneTurn()
    {
        TurnManager tm = newGame(1);
        tm.rollDice();

        assertThrows(IllegalStateException.class, tm::rollDice);
    }

    @Test
    void sameSeedGivesSameRolls()
    {
        List<PieceType> first = new ArrayList<>();
        List<PieceType> second = new ArrayList<>();
        TurnManager a = new TurnManager(board, cfg, 1, new Random(7), new AttackerFirstStrategy());
        for (int i = 0; i < 10; i++)
        {
            first.add(a.rollDice());
            a.startPlayerTurn();
        }
        TurnManager b = new TurnManager(board, cfg, 1, new Random(7), new AttackerFirstStrategy());
        for (int i = 0; i < 10; i++)
        {
            second.add(b.rollDice());
            b.startPlayerTurn();
        }

        assertEquals(first, second);
    }

    @Test
    void listsGivenToUiAreReadOnly()
    {
        addEnemy(PieceType.Rook, "a8");
        TurnManager tm = newGame(1);
        tm.rollDice();

        assertThrows(UnsupportedOperationException.class, () -> tm.getValidMoves().clear());
        assertThrows(UnsupportedOperationException.class, () -> tm.getShakingEnemies().clear());
    }

    @Test
    void rollingDoesNotMoveAnything()
    {
        Piece rook = addEnemy(PieceType.Rook, "a8");
        TurnManager tm = newGame(1);

        tm.rollDice();

        assertEquals(Position.parse("e1"), board.playerPos);
        assertEquals(Position.parse("a8"), rook.pos);
    }
}
