package com.chessgamblegame.model;

import com.chessgamblegame.ai.AttackerFirstStrategy;
import com.chessgamblegame.model.GameEvent.Kind;
import com.chessgamblegame.model.TurnManager.Phase;
import com.chessgamblegame.model.enums.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// B7–B10: người chơi đi/ăn quân, lượt địch, phong cấp, thắng/thua, bỏ lượt.
class TurnFlowTest
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

    private void diceAlways(PieceType face)
    {
        for (PieceType t : PieceType.values())
        {
            cfg.diceWeights.put(t, t == face ? 1 : 0);
        }
    }

    private static Position at(String square)
    {
        return Position.parse(square);
    }

    private static List<Kind> kinds(List<GameEvent> events)
    {
        return events.stream().map(GameEvent::kind).toList();
    }

    private static long count(List<GameEvent> events, Kind kind)
    {
        return events.stream().filter(ev -> ev.kind() == kind).count();
    }

    // B7: người chơi đi

    @Test
    void playerMovesToEmptySquareThenEnemiesMoveAndNewTurnStarts()
    {
        diceAlways(PieceType.Rook);
        Piece pawn = addEnemy(PieceType.Pawn, "h8");
        TurnManager tm = newGame(1);
        tm.rollDice();

        List<GameEvent> events = tm.playerMoveTo(at("e4"));

        assertEquals(at("e4"), board.playerPos);
        assertEquals(GameEvent.playerMoved(at("e1"), at("e4")), events.get(0));
        assertEquals(at("h7"), pawn.pos);
        assertEquals(Phase.WAIT_ROLL, tm.getPhase());
        assertEquals(2, tm.getTurnNumber());
    }

    @Test
    void capturingEnemyRemovesItAndAddsScore()
    {
        diceAlways(PieceType.Rook);
        Piece knight = addEnemy(PieceType.Knight, "e5");
        addEnemy(PieceType.Pawn, "a8");
        TurnManager tm = newGame(1);
        tm.rollDice();

        List<GameEvent> events = tm.playerMoveTo(at("e5"));

        assertNull(board.getEnemyAt(at("e5")));
        assertFalse(board.getEnemies().contains(knight));
        assertEquals(30, tm.getScore());
        assertEquals(List.of(Kind.PLAYER_MOVED, Kind.PLAYER_CAPTURED_ENEMY), kinds(events));
        assertSame(knight, events.get(1).piece());
    }

    @Test
    void capturingTheShakingEnemyMeansItDoesNotMove()
    {
        diceAlways(PieceType.Rook);
        Piece knight = addEnemy(PieceType.Knight, "e5");   // gần nhất -> được chọn rung
        Piece pawn = addEnemy(PieceType.Pawn, "a8");
        TurnManager tm = newGame(1);
        assertTrue(knight.shaking);
        tm.rollDice();

        List<GameEvent> events = tm.playerMoveTo(at("e5"));

        assertEquals(0, count(events, Kind.ENEMY_MOVED));
        assertEquals(at("a8"), pawn.pos);
    }

    @Test
    void capturingLastEnemyWins()
    {
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Knight, "e5");
        TurnManager tm = newGame(1);
        tm.rollDice();

        tm.playerMoveTo(at("e5"));

        assertEquals(Phase.WON, tm.getPhase());
        assertTrue(tm.isGameOver());
        assertEquals(1, tm.getTurnNumber());
    }

    @Test
    void kingFaceCanTeleportOntoEnemyAndCaptureIt()
    {
        diceAlways(PieceType.King);
        addEnemy(PieceType.Queen, "d8");
        addEnemy(PieceType.Pawn, "a7");
        TurnManager tm = newGame(1);
        tm.rollDice();

        tm.playerMoveTo(at("d8"));

        assertEquals(at("d8"), board.playerPos);
        assertEquals(90, tm.getScore());
    }

    @Test
    void invalidSquareIsRejected()
    {
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Pawn, "a8");
        TurnManager tm = newGame(1);
        tm.rollDice();

        assertThrows(IllegalArgumentException.class, () -> tm.playerMoveTo(at("d2")));
        assertEquals(Phase.WAIT_PLAYER_MOVE, tm.getPhase());
        assertEquals(at("e1"), board.playerPos);
    }

    @Test
    void cannotMoveBeforeRolling()
    {
        addEnemy(PieceType.Pawn, "a8");
        TurnManager tm = newGame(1);

        assertThrows(IllegalStateException.class, () -> tm.playerMoveTo(at("e2")));
    }

    // B8: lượt địch

    @Test
    void shakingEnemyMovesTowardNewPlayerPosition()
    {
        diceAlways(PieceType.Rook);
        Piece rook = addEnemy(PieceType.Rook, "a8");
        TurnManager tm = newGame(1);
        tm.rollDice();

        List<GameEvent> events = tm.playerMoveTo(at("e2"));

        assertTrue(Set.of(at("a2"), at("e8")).contains(rook.pos), "Xe đang ở " + rook.pos.toNotation());
        assertEquals(GameEvent.enemyMoved(rook, at("a8"), rook.pos), events.get(1));
    }

    @Test
    void onlyShakingEnemiesMove()
    {
        diceAlways(PieceType.Rook);
        Piece rook = addEnemy(PieceType.Rook, "a8");       // 2 nước -> được chọn
        Piece knight = addEnemy(PieceType.Knight, "b8");   // xa hơn -> đứng yên
        TurnManager tm = newGame(1);
        assertTrue(rook.shaking);
        assertFalse(knight.shaking);
        tm.rollDice();

        tm.playerMoveTo(at("e2"));

        assertEquals(at("b8"), knight.pos);
        assertNotEquals(at("a8"), rook.pos);
    }

    @Test
    void attackCostsOneHpAndAttackerDisappears()
    {
        diceAlways(PieceType.Rook);
        Piece rook = addEnemy(PieceType.Rook, "a8");
        addEnemy(PieceType.Pawn, "h8");
        TurnManager tm = newGame(1);
        tm.rollDice();

        List<GameEvent> events = tm.playerMoveTo(at("a1"));

        assertEquals(2, tm.getHp());
        assertFalse(board.getEnemies().contains(rook));
        assertEquals(at("a1"), board.playerPos);
        assertEquals(List.of(Kind.PLAYER_MOVED, Kind.PLAYER_HIT, Kind.ENEMY_REMOVED_AFTER_HIT), kinds(events));
        assertEquals(GameEvent.playerHit(rook, at("a8"), at("a1")), events.get(1));
        assertEquals(Phase.WAIT_ROLL, tm.getPhase());
    }

    @Test
    void twoAttackersTakeOneHpEach()
    {
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Rook, "a8");
        addEnemy(PieceType.Rook, "h8");
        addEnemy(PieceType.Pawn, "a3");
        TurnManager tm = newGame(2);
        tm.rollDice();

        List<GameEvent> events = tm.playerMoveTo(at("e8"));

        assertEquals(1, tm.getHp());
        assertEquals(2, count(events, Kind.PLAYER_HIT));
        assertEquals(1, board.getEnemies().size());
        assertEquals(Phase.WAIT_ROLL, tm.getPhase());
    }

    @Test
    void lastEnemyHittingAndVanishingWinsIfPlayerSurvives()
    {
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Rook, "a8");
        TurnManager tm = newGame(1);
        tm.rollDice();

        tm.playerMoveTo(at("a1"));

        assertEquals(2, tm.getHp());
        assertEquals(Phase.WON, tm.getPhase());
    }

    // B9: phong cấp

    @Test
    void enemyPawnReachingBottomRowIsPromoted()
    {
        diceAlways(PieceType.Rook);
        Piece pawn = addEnemy(PieceType.Pawn, "a2");
        TurnManager tm = newGame(1);
        tm.rollDice();

        List<GameEvent> events = tm.playerMoveTo(at("e4"));

        assertEquals(at("a1"), pawn.pos);
        assertNotEquals(PieceType.Pawn, pawn.type);
        assertNotEquals(PieceType.King, pawn.type);
        GameEvent last = events.get(events.size() - 1);
        assertEquals(Kind.ENEMY_PROMOTED, last.kind());
        assertEquals(pawn.type, last.newType());
        assertEquals(at("a1"), last.to());
    }

    @Test
    void promotionIsRandomAndNeverKing()
    {
        diceAlways(PieceType.Rook);
        Random seeds = new Random(1);
        Set<PieceType> seen = EnumSet.noneOf(PieceType.class);

        for (int i = 0; i < 200; i++)
        {
            board = new Board();
            board.playerPos = at("e1");
            Piece pawn = addEnemy(PieceType.Pawn, "a2");
            TurnManager tm = new TurnManager(board, cfg, 1, new Random(seeds.nextLong()), new AttackerFirstStrategy());
            tm.rollDice();
            tm.playerMoveTo(at("e4"));
            seen.add(pawn.type);
        }

        assertEquals(EnumSet.of(PieceType.Knight, PieceType.Bishop, PieceType.Rook, PieceType.Queen), seen);
    }

    @Test
    void pawnNotOnBottomRowIsNotPromoted()
    {
        diceAlways(PieceType.Rook);
        Piece pawn = addEnemy(PieceType.Pawn, "a3");
        TurnManager tm = newGame(1);
        tm.rollDice();

        tm.playerMoveTo(at("e4"));

        assertEquals(at("a2"), pawn.pos);
        assertEquals(PieceType.Pawn, pawn.type);
    }

    // B10: thua và bỏ lượt

    @Test
    void losingLastHpEndsGameImmediately()
    {
        cfg.startHp = 1;
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Rook, "a8");
        addEnemy(PieceType.Rook, "h8");
        addEnemy(PieceType.Pawn, "a3");
        TurnManager tm = newGame(2);
        tm.rollDice();

        List<GameEvent> events = tm.playerMoveTo(at("e8"));

        assertEquals(0, tm.getHp());
        assertEquals(Phase.LOST, tm.getPhase());
        assertTrue(tm.isGameOver());
        assertEquals(1, count(events, Kind.PLAYER_HIT));   // quân thứ 2 không kịp đánh
        assertEquals(2, board.getEnemies().size());
        assertTrue(board.getEnemies().stream().noneMatch(e -> e.shaking));
    }

    @Test
    void skipTurnLetsEnemiesAct()
    {
        diceAlways(PieceType.Pawn);
        addEnemy(PieceType.Rook, "e2");     // chặn Tốt và đang tấn công e1
        addEnemy(PieceType.Pawn, "a8");
        TurnManager tm = newGame(1);
        tm.rollDice();
        assertEquals(Phase.WAIT_SKIP, tm.getPhase());

        List<GameEvent> events = tm.skipTurn();

        assertEquals(2, tm.getHp());
        assertEquals(List.of(Kind.PLAYER_HIT, Kind.ENEMY_REMOVED_AFTER_HIT), kinds(events));
        assertEquals(at("e1"), board.playerPos);
        assertEquals(Phase.WAIT_ROLL, tm.getPhase());
        assertEquals(2, tm.getTurnNumber());
    }

    @Test
    void cannotSkipWhenMovesExist()
    {
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Pawn, "a8");
        TurnManager tm = newGame(1);
        tm.rollDice();

        assertThrows(IllegalStateException.class, tm::skipTurn);
    }

    @Test
    void cannotSkipBeforeRolling()
    {
        addEnemy(PieceType.Pawn, "a8");
        TurnManager tm = newGame(1);

        assertThrows(IllegalStateException.class, tm::skipTurn);
    }

    @Test
    void nothingWorksAfterGameOver()
    {
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Knight, "e5");
        TurnManager tm = newGame(1);
        tm.rollDice();
        tm.playerMoveTo(at("e5"));

        assertThrows(IllegalStateException.class, tm::rollDice);
        assertThrows(IllegalStateException.class, () -> tm.playerMoveTo(at("e6")));
        assertThrows(IllegalStateException.class, tm::skipTurn);
    }

    // Chơi thử nhiều lượt

    @Test
    void randomGamesAlwaysEndOrKeepConsistentState()
    {
        Random seeds = new Random(2);
        for (int game = 0; game < 30; game++)
        {
            board = new Board();
            board.playerPos = at("e1");
            addEnemy(PieceType.Pawn, "a7");
            addEnemy(PieceType.Pawn, "h7");
            addEnemy(PieceType.Knight, "b8");
            addEnemy(PieceType.Rook, "h8");
            Random rnd = new Random(seeds.nextLong());
            TurnManager tm = new TurnManager(board, new GameConfig(), 2, rnd, new AttackerFirstStrategy());

            for (int turn = 0; turn < 100 && !tm.isGameOver(); turn++)
            {
                tm.rollDice();
                if (tm.getPhase() == Phase.WAIT_SKIP)
                {
                    tm.skipTurn();
                }
                else
                {
                    List<Position> moves = tm.getValidMoves();
                    tm.playerMoveTo(moves.get(rnd.nextInt(moves.size())));
                }

                assertTrue(tm.getHp() >= 0 && tm.getHp() <= 3);
                assertTrue(board.playerPos.inBounds());
                assertNull(board.getEnemyAt(board.playerPos), "Người chơi đứng chung ô với địch");
                assertTrue(board.getEnemies().stream().noneMatch(e -> e.type == PieceType.King));
                if (!tm.isGameOver())
                {
                    assertEquals(Phase.WAIT_ROLL, tm.getPhase());
                }
            }
        }
    }
}
