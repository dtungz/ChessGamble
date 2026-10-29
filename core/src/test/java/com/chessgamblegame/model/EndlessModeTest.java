package com.chessgamblegame.model;

import com.chessgamblegame.ai.AttackerFirstStrategy;
import com.chessgamblegame.level.EndlessSpawner;
import com.chessgamblegame.model.GameEvent.Kind;
import com.chessgamblegame.model.TurnManager.Phase;
import com.chessgamblegame.model.enums.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

// B13–B15: chế độ Endless trong TurnManager.
class EndlessModeTest
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

    private TurnManager endlessGame()
    {
        return new TurnManager(board, cfg, new Random(0), new AttackerFirstStrategy(), new EndlessSpawner(cfg));
    }

    private TurnManager levelGame()
    {
        return new TurnManager(board, cfg, 1, new Random(0), new AttackerFirstStrategy());
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

    private static long count(List<GameEvent> events, Kind kind)
    {
        return events.stream().filter(ev -> ev.kind() == kind).count();
    }

    // Không có thắng

    @Test
    void endlessFlagAndStartValues()
    {
        TurnManager tm = endlessGame();

        assertTrue(tm.isEndless());
        assertEquals(1, tm.getEnemiesPerTurn());
        assertFalse(levelGame().isEndless());
    }

    @Test
    void capturingLastEnemyDoesNotWinAndANewOneSpawns()
    {
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Knight, "e5");
        TurnManager tm = endlessGame();
        tm.rollDice();

        List<GameEvent> events = tm.playerMoveTo(at("e5"));

        assertEquals(Phase.WAIT_ROLL, tm.getPhase());
        assertEquals(1, count(events, Kind.ENEMY_SPAWNED));
        assertEquals(1, board.getEnemies().size());
        assertEquals(7, board.getEnemies().get(0).pos.y());
    }

    @Test
    void randomEndlessGamesNeverWin()
    {
        Random seeds = new Random(4);
        for (int game = 0; game < 20; game++)
        {
            GameConfig c = new GameConfig();
            Random rnd = new Random(seeds.nextLong());
            EndlessSpawner spawner = new EndlessSpawner(c);
            Board b = spawner.createStartBoard(rnd);
            TurnManager tm = new TurnManager(b, c, rnd, new AttackerFirstStrategy(), spawner);

            for (int turn = 0; turn < 150 && !tm.isGameOver(); turn++)
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

                assertNotEquals(Phase.WON, tm.getPhase());
                assertTrue(tm.getHp() >= 0 && tm.getHp() <= c.maxHp);
                assertTrue(b.getEnemies().size() <= c.endlessMaxEnemiesOnBoard);
                assertTrue(b.getEnemies().stream().noneMatch(e -> e.type == PieceType.King));
            }
        }
    }

    // Điểm

    @Test
    void survivingATurnGivesOnePointInEndlessOnly()
    {
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Pawn, "h8");
        TurnManager endless = endlessGame();
        endless.rollDice();
        endless.playerMoveTo(at("e2"));

        assertEquals(1, endless.getScore());

        setUp();
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Pawn, "h8");
        TurnManager level = levelGame();
        level.rollDice();
        level.playerMoveTo(at("e2"));

        assertEquals(0, level.getScore());
    }

    @Test
    void captureScoreAndSurvivalAddUp()
    {
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Rook, "e5");
        addEnemy(PieceType.Pawn, "h8");
        TurnManager tm = endlessGame();
        tm.rollDice();

        tm.playerMoveTo(at("e5"));

        assertEquals(50 + 1, tm.getScore());
    }

    // Hồi máu

    @Test
    void reachingMilestoneHealsWhenBelowMax()
    {
        cfg.startHp = 2;
        cfg.healEveryScore = 10;
        cfg.surviveTurnScore = 0;
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Pawn, "a1");
        addEnemy(PieceType.Pawn, "h8");
        TurnManager tm = endlessGame();
        tm.rollDice();

        List<GameEvent> events = tm.playerMoveTo(at("a1"));

        assertEquals(3, tm.getHp());
        assertEquals(1, count(events, Kind.HEAL));
        assertEquals(Kind.HEAL, events.get(2).kind());   // ngay sau khi ăn quân
    }

    @Test
    void healingNeverGoesAboveMaxHp()
    {
        cfg.startHp = 1;
        cfg.healEveryScore = 10;
        cfg.surviveTurnScore = 0;
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Queen, "a1");   // 90 điểm = 9 mốc
        addEnemy(PieceType.Pawn, "h8");
        TurnManager tm = endlessGame();
        tm.rollDice();

        List<GameEvent> events = tm.playerMoveTo(at("a1"));

        assertEquals(3, tm.getHp());
        assertEquals(2, count(events, Kind.HEAL));
    }

    @Test
    void milestoneReachedAtFullHpIsNotSavedForLater()
    {
        cfg.startHp = 3;
        cfg.healEveryScore = 10;
        cfg.surviveTurnScore = 0;
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Pawn, "a1");
        addEnemy(PieceType.Rook, "a8");    // được chọn rung, sẽ đánh người chơi ở a1
        TurnManager tm = endlessGame();
        tm.rollDice();

        List<GameEvent> events = tm.playerMoveTo(at("a1"));

        assertEquals(10, tm.getScore());
        assertEquals(2, tm.getHp());
        assertEquals(0, count(events, Kind.HEAL));
        assertEquals(1, count(events, Kind.PLAYER_HIT));
    }

    @Test
    void levelModeNeverHeals()
    {
        cfg.startHp = 2;
        cfg.healEveryScore = 10;
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Pawn, "a1");
        addEnemy(PieceType.Pawn, "h8");
        TurnManager tm = levelGame();
        tm.rollDice();

        List<GameEvent> events = tm.playerMoveTo(at("a1"));

        assertEquals(2, tm.getHp());
        assertEquals(0, count(events, Kind.HEAL));
    }

    // Độ khó tăng dần

    @Test
    void enemiesPerTurnGrowsOverTurns()
    {
        cfg.endlessEnemiesPerTurnIncreaseEvery = 1;
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Pawn, "a7");
        addEnemy(PieceType.Pawn, "b7");
        addEnemy(PieceType.Pawn, "g7");
        addEnemy(PieceType.Pawn, "h7");
        TurnManager tm = endlessGame();
        assertEquals(1, tm.getShakingEnemies().size());

        tm.rollDice();
        tm.playerMoveTo(at("e2"));
        assertEquals(2, tm.getEnemiesPerTurn());
        assertEquals(2, tm.getShakingEnemies().size());

        tm.rollDice();
        tm.playerMoveTo(at("e3"));
        assertEquals(3, tm.getEnemiesPerTurn());
        assertEquals(3, tm.getShakingEnemies().size());
    }

    @Test
    void newEnemySpawnsEveryThreeTurns()
    {
        diceAlways(PieceType.Rook);
        addEnemy(PieceType.Pawn, "a7");
        addEnemy(PieceType.Pawn, "h7");
        TurnManager tm = endlessGame();
        String[] path = {"e2", "e1", "e2"};
        long[] spawnedPerTurn = new long[path.length];

        for (int i = 0; i < path.length; i++)
        {
            tm.rollDice();
            spawnedPerTurn[i] = count(tm.playerMoveTo(at(path[i])), Kind.ENEMY_SPAWNED);
        }

        assertArrayEquals(new long[]{0, 0, 1}, spawnedPerTurn);
        assertEquals(3, board.getEnemies().size());
    }
}
