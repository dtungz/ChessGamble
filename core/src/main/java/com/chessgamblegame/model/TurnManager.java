package com.chessgamblegame.model;

import com.chessgamblegame.ai.EnemySelectionStrategy;
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

    private final Board board;
    private final GameConfig cfg;
    private final int enemiesPerTurn;
    private final Random rnd;
    private final EnemySelectionStrategy strategy;
    private final Dice dice;

    private Phase phase;
    private int hp;
    private int score;
    private int turnNumber;
    private PieceType rolledType;
    private List<Position> validMoves = new ArrayList<>();
    private List<Piece> shakingEnemies = new ArrayList<>();

    public TurnManager(Board board, GameConfig cfg, int enemiesPerTurn, Random rnd, EnemySelectionStrategy strategy)
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
        this.dice = new Dice(rnd, cfg.diceWeights);
        this.hp = cfg.startHp;

        startPlayerTurn();
    }

    // Đầu lượt người chơi: chọn trước các quân địch sẽ đi ở lượt địch tới và cho chúng rung.
    public void startPlayerTurn()
    {
        for (Piece e : board.getEnemies())
        {
            e.shaking = false;
        }
        shakingEnemies = new ArrayList<>(strategy.select(board, enemiesPerTurn, rnd));
        for (Piece e : shakingEnemies)
        {
            e.shaking = true;
        }

        rolledType = null;
        validMoves = new ArrayList<>();
        turnNumber++;
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
