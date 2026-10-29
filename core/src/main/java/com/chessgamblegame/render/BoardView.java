package com.chessgamblegame.render;

import com.chessgamblegame.model.Board;
import com.chessgamblegame.model.GameEvent;
import com.chessgamblegame.model.Piece;
import com.chessgamblegame.model.Position;
import com.chessgamblegame.model.enums.PieceType;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

// "Bản sao để vẽ" của bàn cờ. TurnManager tính xong cả lượt ngay lập tức, còn BoardView
// phát lại từng GameEvent thành animation (trượt quân, quân biến mất...) cho người chơi kịp nhìn.
// Khi không có animation, BoardView được đồng bộ lại đúng y như Board thật.
public class BoardView
{
    private static final float SPEED = 12f;          // càng lớn quân trượt càng nhanh
    private static final float MAX_MOVE_TIME = 0.35f;
    private static final float SHORT_PAUSE = 0.05f;
    private static final float LONG_PAUSE = 0.25f;

    public static class Sprite
    {
        public float x, y;          // góc dưới-trái của ô đang vẽ
        public float targetX, targetY;
        public PieceType type;
        public boolean shaking;

        void placeAt(Position p)
        {
            x = targetX = BoardRenderer.cellX(p.x());
            y = targetY = BoardRenderer.cellY(p.y());
        }

        void moveTo(Position p)
        {
            targetX = BoardRenderer.cellX(p.x());
            targetY = BoardRenderer.cellY(p.y());
        }
    }

    private final Map<Piece, Sprite> enemies = new LinkedHashMap<>();
    private final Sprite player = new Sprite();
    private final Queue<GameEvent> queue = new ArrayDeque<>();
    private final Consumer<GameEvent> onEventStart;

    private GameEvent current;
    private Sprite moving;
    private float currentTime;
    private int hp;

    // onEventStart: được gọi mỗi khi bắt đầu phát 1 event (GameScreen dùng để nháy đỏ khi bị đánh).
    public BoardView(Consumer<GameEvent> onEventStart)
    {
        this.onEventStart = onEventStart;
    }

    // Chép lại toàn bộ vị trí từ Board thật (không có animation).
    public void sync(Board board, int hp)
    {
        this.hp = hp;
        enemies.clear();
        for (Piece e : board.getEnemies())
        {
            Sprite s = new Sprite();
            s.placeAt(e.pos);
            s.type = e.type;
            s.shaking = e.shaking;
            enemies.put(e, s);
        }
        player.placeAt(board.playerPos);
    }

    public void play(List<GameEvent> events)
    {
        for (Sprite s : enemies.values())
        {
            s.shaking = false;
        }
        queue.addAll(events);
    }

    public boolean isBusy()
    {
        return current != null || !queue.isEmpty();
    }

    public void update(float delta)
    {
        if (current == null)
        {
            if (queue.isEmpty())
            {
                return;
            }
            start(queue.poll());
        }
        currentTime += delta;

        if (moving != null)
        {
            float t = Math.min(1f, delta * SPEED);
            moving.x += (moving.targetX - moving.x) * t;
            moving.y += (moving.targetY - moving.y) * t;
            boolean arrived = Math.abs(moving.targetX - moving.x) < 1 && Math.abs(moving.targetY - moving.y) < 1;
            if (arrived || currentTime >= MAX_MOVE_TIME)
            {
                moving.x = moving.targetX;
                moving.y = moving.targetY;
                current = null;
            }
        }
        else if (currentTime >= pauseFor(current))
        {
            current = null;
        }
    }

    private void start(GameEvent ev)
    {
        current = ev;
        currentTime = 0;
        moving = null;
        Sprite enemy = ev.piece() == null ? null : enemies.get(ev.piece());

        switch (ev.kind())
        {
            case PLAYER_MOVED ->
            {
                moving = player;
                player.moveTo(ev.to());
            }
            case ENEMY_MOVED, PLAYER_HIT ->
            {
                // PLAYER_HIT: quân địch lao vào ô người chơi rồi mới biến mất (event kế tiếp)
                if (enemy != null)
                {
                    moving = enemy;
                    enemy.moveTo(ev.to());
                }
            }
            case PLAYER_CAPTURED_ENEMY -> enemies.remove(ev.piece());
            case ENEMY_REMOVED_AFTER_HIT ->
            {
                enemies.remove(ev.piece());
                hp--;
            }
            case ENEMY_PROMOTED ->
            {
                if (enemy != null)
                {
                    enemy.type = ev.newType();
                }
            }
            case HEAL -> hp++;
            case ENEMY_SPAWNED ->
            {
                Sprite s = new Sprite();
                s.placeAt(ev.to());
                s.type = ev.piece().type;
                enemies.put(ev.piece(), s);
            }
        }
        onEventStart.accept(ev);
    }

    private static float pauseFor(GameEvent ev)
    {
        return switch (ev.kind())
        {
            case ENEMY_PROMOTED, HEAL, ENEMY_SPAWNED, ENEMY_REMOVED_AFTER_HIT -> LONG_PAUSE;
            default -> SHORT_PAUSE;
        };
    }

    public Collection<Sprite> getEnemySprites()
    {
        return enemies.values();
    }

    public Sprite getPlayerSprite()
    {
        return player;
    }

    // Máu đang hiển thị: chỉ giảm khi animation bị đánh phát tới, không giảm trước.
    public int getHp()
    {
        return hp;
    }
}
