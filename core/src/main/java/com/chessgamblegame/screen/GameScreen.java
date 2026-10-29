package com.chessgamblegame.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.chessgamblegame.ChessGambleGame;
import com.chessgamblegame.ai.AttackerFirstStrategy;
import com.chessgamblegame.level.EndlessSpawner;
import com.chessgamblegame.level.LevelData;
import com.chessgamblegame.level.LevelLoader;
import com.chessgamblegame.model.Board;
import com.chessgamblegame.model.GameConfig;
import com.chessgamblegame.model.GameEvent;
import com.chessgamblegame.model.Position;
import com.chessgamblegame.model.TurnManager;
import com.chessgamblegame.model.TurnManager.Phase;
import com.chessgamblegame.render.BoardRenderer;
import com.chessgamblegame.render.BoardView;
import com.chessgamblegame.render.DiceWidget;
import com.chessgamblegame.render.HudRenderer;
import com.chessgamblegame.render.PieceRenderer;
import com.chessgamblegame.render.UiButton;

import java.util.List;
import java.util.Random;

// Màn chơi: vẽ trạng thái từ TurnManager và chuyển click chuột thành lệnh cho TurnManager.
// Màn hình chỉ đọc dữ liệu, mọi luật chơi nằm trong TurnManager.
public class GameScreen extends BaseScreen
{
    private static final float PANEL_X = BoardRenderer.BOARD_X + BoardRenderer.BOARD_SIZE + 40;
    private static final float PANEL_WIDTH = ChessGambleGame.WORLD_WIDTH - PANEL_X - 20;
    private static final float DICE_SIZE = 130;

    // Hết ván thì đợi 1 giây cho người chơi nhìn bàn cờ rồi mới sang màn kết quả
    private static final float RESULT_DELAY = 1.0f;

    // Bị đánh: màn hình nháy đỏ mờ dần trong 0.4s và rung nhẹ tối đa 7px
    private static final float HIT_FLASH_TIME = 0.4f;
    private static final float HIT_FLASH_ALPHA = 0.35f;
    private static final float HIT_SHAKE_PX = 7f;
    private static final Color HIT_FLASH = new Color(0.9f, 0.1f, 0.1f, 0f);

    private final BitmapFont pieceFont = createFont(2.2f);
    private final BitmapFont diceFont = createFont(4f);

    private final BoardRenderer boardRenderer = new BoardRenderer();
    private final PieceRenderer pieceRenderer = new PieceRenderer();
    private final DiceWidget dice = new DiceWidget(PANEL_X + (PANEL_WIDTH - DICE_SIZE) / 2, 330, DICE_SIZE);
    private final HudRenderer hud = new HudRenderer(PANEL_X + (PANEL_WIDTH - 200) / 2, 240, 200, 44);
    private final UiButton menuButton = new UiButton("Menu", PANEL_X, 30, 110, 40);
    private final BoardView view = new BoardView(this::onEventStart);

    private final GameConfig cfg = new GameConfig();
    private final int levelNumber;   // 0 = Endless
    private final LevelData level;   // null khi Endless
    private Board board;
    private TurnManager turn;
    private String lastAction = "";
    private float time;
    private float hitFlash;
    private float gameOverTimer;

    public static GameScreen forLevel(ChessGambleGame game, int levelNumber)
    {
        return new GameScreen(game, levelNumber, LevelFiles.load(levelNumber));
    }

    public static GameScreen endless(ChessGambleGame game)
    {
        return new GameScreen(game, 0, null);
    }

    private GameScreen(ChessGambleGame game, int levelNumber, LevelData level)
    {
        super(game);
        this.levelNumber = levelNumber;
        this.level = level;
        start();
    }

    private boolean isEndless()
    {
        return level == null;
    }

    private void start()
    {
        Random rnd = new Random();
        if (isEndless())
        {
            EndlessSpawner spawner = new EndlessSpawner(cfg);
            board = spawner.createStartBoard(rnd);
            turn = new TurnManager(board, cfg, rnd, new AttackerFirstStrategy(), spawner);
        }
        else
        {
            board = LevelLoader.createBoard(level);
            turn = new TurnManager(board, cfg, level.enemiesPerTurn, rnd, new AttackerFirstStrategy());
        }
        dice.reset();
        view.sync(board, turn.getHp());
        lastAction = "";
        hitFlash = 0;
        gameOverTimer = 0;
    }

    @Override
    public void render(float delta)
    {
        time += delta;
        hitFlash = Math.max(0, hitFlash - delta);
        dice.update(delta);
        view.update(delta);
        if (!view.isBusy())
        {
            view.sync(board, turn.getHp());   // hết animation thì vẽ đúng y như Board thật
        }

        updateMouse();
        handleInput();
        if (leaving)
        {
            return;
        }
        if (turn.isGameOver() && !animating())
        {
            gameOverTimer += delta;
            if (gameOverTimer >= RESULT_DELAY)
            {
                switchTo(new ResultScreen(game, turn.getPhase() == Phase.WON, turn.getScore(),
                    turn.getTurnNumber(), levelNumber));
                return;
            }
        }

        beginFrame();
        shakeCamera();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        boardRenderer.drawSquares(shapes);
        if (canChooseSquare())
        {
            boardRenderer.drawMoveHints(shapes, board, turn.getValidMoves());
        }
        pieceRenderer.drawShapes(shapes, view, time);
        hud.drawHearts(shapes, PANEL_X, 585, view.getHp(), cfg.maxHp);
        dice.drawShapes(shapes, canRoll());
        if (canSkip())
        {
            hud.drawSkipButton(shapes);
        }
        menuButton.drawShape(shapes, mouse);
        shapes.end();

        batch.begin();
        boardRenderer.drawLabels(batch, uiFont);
        pieceRenderer.drawLetters(batch, pieceFont, view, dice.isRolling() ? null : turn.getRolledType(), time);
        dice.drawText(batch, diceFont, uiFont, DIM_TEXT);
        if (canSkip())
        {
            hud.drawSkipLabel(batch, uiFont);
        }
        menuButton.drawLabel(batch, uiFont);
        drawPanelText();
        batch.end();

        drawHitFlash();
    }

    // ---------- Hiệu ứng bị đánh ----------

    private void onEventStart(GameEvent ev)
    {
        if (ev.kind() == GameEvent.Kind.ENEMY_REMOVED_AFTER_HIT)
        {
            hitFlash = HIT_FLASH_TIME;
        }
    }

    private void shakeCamera()
    {
        float strength = HIT_SHAKE_PX * (hitFlash / HIT_FLASH_TIME);
        camera.position.set(
            ChessGambleGame.WORLD_WIDTH / 2f + MathUtils.random(-strength, strength),
            ChessGambleGame.WORLD_HEIGHT / 2f + MathUtils.random(-strength, strength),
            0);
        camera.update();
    }

    private void drawHitFlash()
    {
        if (hitFlash <= 0)
        {
            return;
        }
        HIT_FLASH.a = HIT_FLASH_ALPHA * (hitFlash / HIT_FLASH_TIME);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(HIT_FLASH);
        shapes.rect(-20, -20, ChessGambleGame.WORLD_WIDTH + 40, ChessGambleGame.WORLD_HEIGHT + 40);
        shapes.end();
    }

    // ---------- Trạng thái được phép làm gì ----------

    // Đang có animation (xúc xắc lăn hoặc quân đang di chuyển) thì khoá thao tác.
    private boolean animating()
    {
        return dice.isRolling() || view.isBusy();
    }

    private boolean canRoll()
    {
        return turn.getPhase() == Phase.WAIT_ROLL && !animating();
    }

    private boolean canChooseSquare()
    {
        return turn.getPhase() == Phase.WAIT_PLAYER_MOVE && !animating();
    }

    private boolean canSkip()
    {
        return turn.getPhase() == Phase.WAIT_SKIP && !animating();
    }

    // ---------- Input ----------

    private void handleInput()
    {
        boolean click = clicked();
        if (menuButton.isClicked(mouse, click) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
        {
            switchTo(new MainMenuScreen(game));
            return;
        }
        if (!click || animating() || turn.isGameOver())
        {
            return;
        }

        if (canRoll() && dice.contains(mouse))
        {
            dice.startRoll(turn.rollDice());
        }
        else if (canSkip() && hud.skipButtonContains(mouse))
        {
            afterTurn(turn.skipTurn());
        }
        else if (canChooseSquare())
        {
            Position cell = BoardRenderer.toCell(mouse);
            if (cell != null && turn.getValidMoves().contains(cell))
            {
                afterTurn(turn.playerMoveTo(cell));
            }
        }
    }

    private void afterTurn(List<GameEvent> events)
    {
        lastAction = describe(events);
        dice.reset();
        view.play(events);
    }

    // Tóm tắt lượt vừa xong thành 1–2 dòng chữ cho người chơi đọc.
    private static String describe(List<GameEvent> events)
    {
        StringBuilder sb = new StringBuilder();
        int hits = 0;
        for (GameEvent ev : events)
        {
            switch (ev.kind())
            {
                case PLAYER_CAPTURED_ENEMY -> sb.append("You captured a ").append(ev.piece().type).append("!\n");
                case PLAYER_HIT -> hits++;
                case ENEMY_PROMOTED -> sb.append("Enemy pawn promoted to ").append(ev.newType()).append("!\n");
                case HEAL -> sb.append("+1 HP\n");
                case ENEMY_SPAWNED -> sb.append("A new enemy appeared!\n");
                default -> { }
            }
        }
        if (hits > 0)
        {
            sb.append("You were hit! -").append(hits).append(" HP\n");
        }
        return sb.toString().trim();
    }

    // ---------- Chữ trên panel ----------

    private void drawPanelText()
    {
        float top = ChessGambleGame.WORLD_HEIGHT - 30;
        uiFont.setColor(TEXT);
        uiFont.draw(batch, "ChessGambleGame", PANEL_X, top);
        uiFont.setColor(DIM_TEXT);
        uiFont.draw(batch, isEndless() ? "Endless mode" : "Level " + levelNumber, PANEL_X, top - 34);

        uiFont.setColor(TEXT);
        uiFont.draw(batch, "Turn: " + turn.getTurnNumber(), PANEL_X, 560);
        uiFont.draw(batch, "Score: " + turn.getScore(), PANEL_X, 530);

        uiFont.setColor(statusColor());
        uiFont.draw(batch, statusText(), PANEL_X, 215, PANEL_WIDTH, Align.left, true);
        uiFont.setColor(DIM_TEXT);
        uiFont.draw(batch, lastAction, PANEL_X, 140, PANEL_WIDTH, Align.left, true);
    }

    private String statusText()
    {
        if (dice.isRolling())
        {
            return "Rolling...";
        }
        if (view.isBusy())
        {
            return "Enemy turn...";
        }
        return switch (turn.getPhase())
        {
            case WAIT_ROLL -> "Click the dice to roll.";
            case WAIT_PLAYER_MOVE -> "Move as a " + turn.getRolledType() + ".\nGreen = move, red = capture.";
            case WAIT_SKIP -> "No legal move.\nClick Skip turn.";
            case ENEMY_TURN -> "Enemy turn...";
            case WON -> "YOU WIN!";
            case LOST -> "GAME OVER";
        };
    }

    private Color statusColor()
    {
        return switch (turn.getPhase())
        {
            case WON -> WIN_TEXT;
            case LOST -> LOSE_TEXT;
            default -> TEXT;
        };
    }

    @Override
    protected void disposeExtra()
    {
        pieceFont.dispose();
        diceFont.dispose();
    }
}
