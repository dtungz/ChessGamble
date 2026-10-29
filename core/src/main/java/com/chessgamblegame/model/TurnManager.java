package com.chessgamblegame.model;

import com.chessgamblegame.ai.EnemySelectionStrategy;
import com.chessgamblegame.ai.PathFinder;
import com.chessgamblegame.level.EndlessSpawner;
import com.chessgamblegame.model.enums.PieceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TurnManager
{
    public enum Phase
    {
        WAIT_ROLL,          // chờ người chơi bấm xúc xắc
        WAIT_PLAYER_MOVE,   // đã tung, chờ người chơi chọn ô
        WAIT_SKIP,          // đã tung nhưng không có nước đi, chỉ được bỏ lượt
        ENEMY_TURN,
        WON,
        LOST
    }

    // Tốt địch xuống hàng cuối được phong ngẫu nhiên 1 trong các loại này (địch không có Vua).
    private static final PieceType[] PROMOTION_TYPES = {
        PieceType.Knight, PieceType.Bishop, PieceType.Rook, PieceType.Queen
    };

    private final Board board;
    private final GameConfig cfg;
    private final Random rnd;
    private final EnemySelectionStrategy strategy;
    private final Dice dice;
    private final EndlessSpawner endless;   // null = chế độ Level

    private int enemiesPerTurn;
    private Phase phase;
    private int hp;
    private int score;
    private int healMilestonesReached;      // số mốc hồi máu (mỗi healEveryScore điểm) đã xét
    private int turnNumber;
    private PieceType rolledType;
    private List<Position> validMoves = new ArrayList<>();
    private List<Piece> shakingEnemies = new ArrayList<>();

    // Chế độ Level: thắng khi ăn hết địch.
    public TurnManager(Board board, GameConfig cfg, int enemiesPerTurn, Random rnd, EnemySelectionStrategy strategy)
    {
        this(board, cfg, enemiesPerTurn, rnd, strategy, null);
    }

    // Chế độ Endless: không có thắng, địch sinh thêm theo lượt, có điểm sống sót và hồi máu.
    public TurnManager(Board board, GameConfig cfg, Random rnd, EnemySelectionStrategy strategy, EndlessSpawner endless)
    {
        this(board, cfg, endless.enemiesPerTurn(1), rnd, strategy, endless);
    }

    private TurnManager(Board board, GameConfig cfg, int enemiesPerTurn, Random rnd,
                        EnemySelectionStrategy strategy, EndlessSpawner endless)
    {
        if (board.playerPos == null)
        {
            throw new IllegalArgumentException("Chưa đặt vị trí người chơi trên bàn cờ");
        }
        this.board = board;
        this.cfg = cfg;
        this.enemiesPerTurn = enemiesPerTurn;
        this.rnd = rnd;
        this.strategy = strategy;
        this.endless = endless;
        this.dice = new Dice(rnd, cfg.diceWeights);
        this.hp = cfg.startHp;

        startPlayerTurn();
    }

    // Đầu lượt người chơi: chọn trước các quân địch sẽ đi ở lượt địch tới và cho chúng rung.
    public void startPlayerTurn()
    {
        turnNumber++;
        if (endless != null)
        {
            enemiesPerTurn = endless.enemiesPerTurn(turnNumber);
        }

        stopShaking();
        shakingEnemies = new ArrayList<>(strategy.select(board, enemiesPerTurn, rnd));
        for (Piece e : shakingEnemies)
        {
            e.shaking = true;
        }

        rolledType = null;
        validMoves = new ArrayList<>();
        phase = Phase.WAIT_ROLL;
    }

    public PieceType rollDice()
    {
        if (phase != Phase.WAIT_ROLL)
        {
            throw new IllegalStateException("Chỉ được tung xúc xắc ở đầu lượt, phase hiện tại: " + phase);
        }
        rolledType = dice.roll();
        validMoves = MoveGenerator.playerMoves(board, rolledType);
        phase = validMoves.isEmpty() ? Phase.WAIT_SKIP : Phase.WAIT_PLAYER_MOVE;
        return rolledType;
    }

    // Người chơi đi tới ô target (ăn quân nếu có), sau đó chạy luôn lượt địch.
    // Trả về mọi event theo thứ tự để giao diện phát lại.
    public List<GameEvent> playerMoveTo(Position target)
    {
        if (phase != Phase.WAIT_PLAYER_MOVE)
        {
            throw new IllegalStateException("Chưa thể đi quân, phase hiện tại: " + phase);
        }
        if (!validMoves.contains(target))
        {
            throw new IllegalArgumentException("Nước đi không hợp lệ: " + target.toNotation());
        }

        List<GameEvent> events = new ArrayList<>();
        Position from = board.playerPos;
        Piece captured = board.getEnemyAt(target);

        board.playerPos = target;
        events.add(GameEvent.playerMoved(from, target));

        if (captured != null)
        {
            board.removeEnemy(captured);
            captured.shaking = false;
            shakingEnemies.remove(captured);
            events.add(GameEvent.playerCaptured(captured, target));
            addScore(cfg.scoreFor(captured.type), events);
        }

        if (endless == null && board.getEnemies().isEmpty())
        {
            phase = Phase.WON;
            return events;
        }

        runEnemyTurn(events);
        return events;
    }

    public List<GameEvent> skipTurn()
    {
        if (phase != Phase.WAIT_SKIP)
        {
            throw new IllegalStateException("Chỉ được bỏ lượt khi không có nước đi, phase hiện tại: " + phase);
        }
        List<GameEvent> events = new ArrayList<>();
        runEnemyTurn(events);
        return events;
    }

    private void runEnemyTurn(List<GameEvent> events)
    {
        phase = Phase.ENEMY_TURN;

        for (Piece e : new ArrayList<>(shakingEnemies))
        {
            e.shaking = false;
            Position step = PathFinder.nextStep(board, e);
            if (step == null)
            {
                continue;
            }

            if (step.equals(board.playerPos))
            {
                hp--;
                events.add(GameEvent.playerHit(e, e.pos, board.playerPos));
                board.removeEnemy(e);
                events.add(GameEvent.enemyRemovedAfterHit(e, e.pos));
                if (hp <= 0)
                {
                    stopShaking();
                    phase = Phase.LOST;
                    return;
                }
            }
            else
            {
                Position from = e.pos;
                board.moveEnemy(e, step);
                events.add(GameEvent.enemyMoved(e, from, step));
                promoteIfOnBottomRow(e, events);
            }
        }
        shakingEnemies.clear();

        if (endless != null)
        {
            endOfEndlessTurn(events);
        }
        else if (board.getEnemies().isEmpty())
        {
            phase = Phase.WON;
            return;
        }

        startPlayerTurn();
    }

    // Endless: cộng điểm sống sót, rồi sinh quân mới nếu tới mốc (hoặc bàn đã hết địch).
    private void endOfEndlessTurn(List<GameEvent> events)
    {
        addScore(cfg.surviveTurnScore, events);
        if (endless.shouldSpawn(turnNumber) || board.getEnemies().isEmpty())
        {
            Piece spawned = endless.spawn(board, turnNumber, rnd);
            if (spawned != null)
            {
                events.add(GameEvent.enemySpawned(spawned, spawned.pos));
            }
        }
    }

    // Endless: cứ đạt mỗi healEveryScore điểm tích luỹ thì hồi 1 máu.
    // Đang đầy máu thì mốc đó bị bỏ qua, không để dành.
    private void addScore(int points, List<GameEvent> events)
    {
        score += points;
        if (endless == null)
        {
            return;
        }
        int milestones = score / cfg.healEveryScore;
        while (healMilestonesReached < milestones)
        {
            healMilestonesReached++;
            if (hp < cfg.maxHp)
            {
                hp++;
                events.add(GameEvent.heal(board.playerPos));
            }
        }
    }

    private void stopShaking()
    {
        for (Piece e : board.getEnemies())
        {
            e.shaking = false;
        }
        shakingEnemies.clear();
    }

    private void promoteIfOnBottomRow(Piece e, List<GameEvent> events)
    {
        if (e.type == PieceType.Pawn && e.pos.y() == 0)
        {
            e.type = PROMOTION_TYPES[rnd.nextInt(PROMOTION_TYPES.length)];
            events.add(GameEvent.enemyPromoted(e, e.pos, e.type));
        }
    }

    public boolean isEndless()
    {
        return endless != null;
    }

    public int getEnemiesPerTurn()
    {
        return enemiesPerTurn;
    }

    public boolean isGameOver()
    {
        return phase == Phase.WON || phase == Phase.LOST;
    }

    public Phase getPhase()
    {
        return phase;
    }

    public int getHp()
    {
        return hp;
    }

    public int getScore()
    {
        return score;
    }

    public int getTurnNumber()
    {
        return turnNumber;
    }

    // Mặt xúc xắc vừa tung, null nếu lượt này chưa tung.
    public PieceType getRolledType()
    {
        return rolledType;
    }

    // Collections.unmodifiableList : Tạo ra một vỏ bọc, hiểu đơn giản là mình lấy cái list kia, nhưng chỉ read được thôi, không thể thay đổi (add, remove) các thứ được
    public List<Position> getValidMoves()
    {
        return Collections.unmodifiableList(validMoves);
    }

    public List<Piece> getShakingEnemies()
    {
        return Collections.unmodifiableList(shakingEnemies);
    }

    public Board getBoard()
    {
        return board;
    }
}
