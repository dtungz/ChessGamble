package com.chessgamblegame.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.chessgamblegame.ChessGambleGame;
import com.chessgamblegame.ai.AttackerFirstStrategy;
import com.chessgamblegame.ai.PathFinder;
import com.chessgamblegame.level.EndlessSpawner;
import com.chessgamblegame.level.LevelData;
import com.chessgamblegame.level.LevelLoader;
import com.chessgamblegame.model.Board;
import com.chessgamblegame.model.GameConfig;
import com.chessgamblegame.model.GameEvent;
import com.chessgamblegame.model.Piece;
import com.chessgamblegame.model.Position;
import com.chessgamblegame.model.TurnManager;
import com.chessgamblegame.model.TurnManager.Phase;
import com.chessgamblegame.model.enums.PieceType;
import com.chessgamblegame.render.BoardRenderer;
import com.chessgamblegame.render.BoardView;
import com.chessgamblegame.render.DiceWidget;
import com.chessgamblegame.render.HudRenderer;
import com.chessgamblegame.render.Palette;
import com.chessgamblegame.render.PieceRenderer;
import com.chessgamblegame.render.CheckSparksRenderer;
import com.chessgamblegame.render.UiButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Màn chơi: vẽ trạng thái từ TurnManager và chuyển click chuột thành lệnh cho TurnManager.
// Màn hình chỉ đọc dữ liệu, mọi luật chơi nằm trong TurnManager.
//
// Bố cục:  [thẻ NEXT MOVE, thông báo, Menu]  [  bàn cờ  ]  [xúc xắc, tim, Skip, trạng thái]
public class GameScreen extends BaseScreen
{
    private static final float SIDE_WIDTH = 164;
    // Cột xúc xắc ở bên phải (thuận tay phải), cột thông tin NEXT MOVE ở bên trái
    private static final float INFO_X = 24;
    private static final float DICE_X = BoardRenderer.BOARD_X + BoardRenderer.BOARD_SIZE + 24;
    private static final float BOARD_RIGHT = BoardRenderer.BOARD_X + BoardRenderer.BOARD_SIZE;
    private static final float DICE_SIZE = 128;

    // Thẻ "NEXT MOVE": các quân địch sẽ đi ở lượt tới
    private static final float CARD_WIDTH = 76;
    private static final float CARD_HEIGHT = 96;
    private static final float CARD_GAP = 12;
    private static final float CARD_TOP = 606;
    private static final float CARD_PIECE = 64;

    // Hết ván thì đợi 1 giây cho người chơi nhìn bàn cờ rồi mới sang màn kết quả
    private static final float RESULT_DELAY = 1.0f;

    // Bị đánh: quân người chơi chớp tắt/hiện trong 0.5s (mỗi 0.08s) và màn hình rung nhẹ tối đa 4px.
    // Không nháy đỏ cả màn hình cho đỡ chói mắt.
    private static final float HIT_FLASH_TIME = 0.5f;
    private static final float HIT_BLINK_EVERY = 0.08f;
    private static final float HIT_SHAKE_PX = 4f;

    private final BitmapFont tinyFont = createFont(8);
    private final GlyphLayout layout = new GlyphLayout();

    private final BoardRenderer boardRenderer = new BoardRenderer();
    private final PieceRenderer pieceRenderer = new PieceRenderer();
    private final HudRenderer hud = new HudRenderer();
    private final CheckSparksRenderer checkSparks = new CheckSparksRenderer();
    private final DiceWidget dice = new DiceWidget(DICE_X + (SIDE_WIDTH - DICE_SIZE) / 2, 466, DICE_SIZE);
    private final UiButton skipButton = new UiButton("SKIP", DICE_X, 346, SIDE_WIDTH, 44);
    private final UiButton menuButton = new UiButton("MENU", INFO_X, 20, SIDE_WIDTH, 40);
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
    // Hình đang hiện của quân người chơi: bắt đầu là Vua trắng, sau đó giữ mặt xúc xắc gần nhất.
    private PieceType playerLook;

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
        playerLook = PieceType.King;
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

        // Xúc xắc dừng thì quân người chơi đổi sang hình của mặt vừa tung; đang lăn thì giữ hình cũ.
        if (!dice.isRolling() && turn.getRolledType() != null)
        {
            playerLook = turn.getRolledType();
        }
        beginFrame();
        shakeCamera();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        boardRenderer.drawFrame(shapes);
        boardRenderer.drawSquares(shapes);
        if (canChooseSquare())
        {
            // Mặt Vua đi được gần như mọi ô: không chấm từng ô cho đỡ rối, chỉ đánh dấu ô ăn được
            // và khung ô đang rê chuột.
            boolean dots = turn.getRolledType() != PieceType.King;
            boardRenderer.drawMoveHints(shapes, board, turn.getValidMoves(), dots);
            Position target = hoveredMove();
            if (target != null)
            {
                boardRenderer.drawHover(shapes, target);
            }
        }
        pieceRenderer.drawShapes(shapes, view, playerLook);
        hud.drawHearts(shapes, DICE_X + (SIDE_WIDTH - HudRenderer.heartsWidth(cfg.maxHp)) / 2, 418,
            view.getHp(), cfg.maxHp);
        dice.drawShapes(shapes, canRoll(), time);
        if (canSkip())
        {
            skipButton.drawShape(shapes, mouse);
        }
        menuButton.drawShape(shapes, mouse);
        drawCardShapes();
        shapes.end();

        batch.begin();
        pieceRenderer.drawPieces(batch, uiFont, view, playerLook, playerVisible(), time);
        dice.drawText(batch, titleFont, uiFont, pieceRenderer);
        if (canSkip())
        {
            skipButton.drawLabel(batch, uiFont);
        }
        menuButton.drawLabel(batch, uiFont);
        drawCardContents();
        drawTexts();
        batch.end();

        // Bị chiếu: tia toả trên đầu người chơi, vẽ sau cùng để không bị quân ở ô phía trên che mất
        if (!attackersNextTurn().isEmpty())
        {
            BoardView.Sprite p = view.getPlayerSprite();
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            checkSparks.draw(shapes, p.x, p.y, time);
            shapes.end();
        }

    }

    // Ô đi được đang nằm dưới con trỏ chuột (null nếu không có).
    private Position hoveredMove()
    {
        Position cell = BoardRenderer.toCell(mouse);
        return cell != null && turn.getValidMoves().contains(cell) ? cell : null;
    }

    // ---------- Cảnh báo tấn công ----------

    // Vị trí các quân địch đang rung mà lượt địch tới sẽ đánh trúng người chơi (nếu người chơi đứng yên).
    private List<Position> attackersNextTurn()
    {
        List<Position> attackers = new ArrayList<>();
        if (animating() || turn.isGameOver())
        {
            return attackers;
        }
        for (Piece e : turn.getShakingEnemies())
        {
            if (board.playerPos.equals(PathFinder.nextStep(board, e)))
            {
                attackers.add(e.pos);
            }
        }
        return attackers;
    }

    // ---------- Thẻ NEXT MOVE ----------

    private List<Piece> nextMovers()
    {
        return animating() || turn.isGameOver() ? List.of() : turn.getShakingEnemies();
    }

    private float cardX(int i)
    {
        return INFO_X + (i % 2) * (CARD_WIDTH + CARD_GAP);
    }

    private float cardY(int i)
    {
        return CARD_TOP - CARD_HEIGHT - (i / 2) * (CARD_HEIGHT + CARD_GAP);
    }

    private void drawCardShapes()
    {
        List<Piece> movers = nextMovers();
        for (int i = 0; i < movers.size(); i++)
        {
            float x = cardX(i);
            float y = cardY(i);
            shapes.setColor(Palette.OUTLINE);
            shapes.rect(x, y, CARD_WIDTH, CARD_HEIGHT);
            shapes.setColor(Palette.CARD);
            shapes.rect(x + 3, y + 3, CARD_WIDTH - 6, CARD_HEIGHT - 6);
            shapes.setColor(Palette.ACCENT);
            shapes.rect(x + 3, y + CARD_HEIGHT - 9, CARD_WIDTH - 6, 6);
        }
    }

    private void drawCardContents()
    {
        List<Piece> movers = nextMovers();
        for (int i = 0; i < movers.size(); i++)
        {
            Piece e = movers.get(i);
            float x = cardX(i);
            float y = cardY(i);
            Texture tex = pieceRenderer.blackTexture(e.type);
            if (tex != null)
            {
                batch.draw(tex, x + (CARD_WIDTH - CARD_PIECE) / 2, y + 22, CARD_PIECE, CARD_PIECE);
            }
            drawCentered(tinyFont, e.pos.toNotation().toUpperCase(), x + CARD_WIDTH / 2, y + 16, Palette.CARD_TEXT);
        }
    }

    // ---------- Chữ ----------

    private void drawTexts()
    {
        float top = ChessGambleGame.WORLD_HEIGHT - 26;

        // Thanh trên: level bên trái, điểm bên phải bàn cờ
        uiFont.setColor(Palette.DIM_TEXT);
        uiFont.draw(batch, isEndless() ? "ENDLESS" : "LEVEL " + levelNumber, BoardRenderer.BOARD_X, top);
        String score = "SCORE " + turn.getScore();
        layout.setText(uiFont, score);
        uiFont.setColor(Palette.ACCENT);
        uiFont.draw(batch, score, BOARD_RIGHT - layout.width, top);

        // Dưới bàn cờ
        drawCentered(uiFont, "TURN: " + turn.getTurnNumber(), BoardRenderer.BOARD_X + BoardRenderer.BOARD_SIZE / 2, 44,
            Palette.BORDER);

        // Cột trái: trạng thái
        smallFont.setColor(statusColor());
        smallFont.draw(batch, statusText(), DICE_X, 318, SIDE_WIDTH, Align.left, true);

        // Cột phải
        uiFont.setColor(Palette.TEXT);
        uiFont.draw(batch, "NEXT MOVE", INFO_X, 640);
        if (nextMovers().isEmpty())
        {
            smallFont.setColor(Palette.DIM_TEXT);
            smallFont.draw(batch, "-", INFO_X, CARD_TOP - 10);
        }
        float y = 290;
        if (isEndless())
        {
            smallFont.setColor(Palette.DIM_TEXT);
            smallFont.draw(batch, spawnText(), INFO_X, y, SIDE_WIDTH, Align.left, true);
            y -= 60;
        }
        smallFont.setColor(Palette.TEXT);
        smallFont.draw(batch, lastAction, INFO_X, y, SIDE_WIDTH, Align.left, true);
    }

    // Endless: quân mới xuất hiện sau lượt thứ k * endlessSpawnEvery.
    private String spawnText()
    {
        int every = cfg.endlessSpawnEvery;
        int left = (every - turn.getTurnNumber() % every) % every;
        return left == 0 ? "NEW ENEMY\nTHIS TURN" : "NEW ENEMY\nIN " + left + " TURNS";
    }

    private String statusText()
    {
        if (dice.isRolling())
        {
            return "ROLLING...";
        }
        if (view.isBusy())
        {
            return "ENEMY TURN...";
        }
        return switch (turn.getPhase())
        {
            case WAIT_ROLL -> "CLICK THE\nDICE TO ROLL";
            case WAIT_PLAYER_MOVE -> turn.getRolledType() == PieceType.King
                ? "KING!\nTELEPORT TO\nANY SQUARE\n\nRED = CAPTURE"
                : "MOVE AS\n" + turn.getRolledType().name().toUpperCase() + "\n\nDOT = MOVE\nRED = CAPTURE";
            case WAIT_SKIP -> "NO LEGAL MOVE\nPRESS SKIP";
            case ENEMY_TURN -> "ENEMY TURN...";
            case WON -> "YOU WIN!";
            case LOST -> "GAME OVER";
        };
    }

    private Color statusColor()
    {
        return switch (turn.getPhase())
        {
            case WON -> Palette.WIN;
            case LOST -> Palette.ACCENT;
            default -> Palette.TEXT;
        };
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

    // Trong lúc chớp: khung hình chẵn hiện, khung hình lẻ ẩn quân người chơi.
    private boolean playerVisible()
    {
        return hitFlash <= 0 || (int) (hitFlash / HIT_BLINK_EVERY) % 2 == 0;
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
        else if (canSkip() && skipButton.isClicked(mouse, true))
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

    // Tóm tắt lượt vừa xong thành vài dòng chữ ngắn cho người chơi đọc.
    private static String describe(List<GameEvent> events)
    {
        StringBuilder sb = new StringBuilder();
        int hits = 0;
        for (GameEvent ev : events)
        {
            switch (ev.kind())
            {
                case PLAYER_CAPTURED_ENEMY -> sb.append("CAPTURED ").append(ev.piece().type.name().toUpperCase()).append("!\n");
                case PLAYER_HIT -> hits++;
                case ENEMY_PROMOTED -> sb.append("PAWN BECAME\n").append(ev.newType().name().toUpperCase()).append("!\n");
                case HEAL -> sb.append("+1 HP\n");
                case ENEMY_SPAWNED -> sb.append("NEW ENEMY!\n");
                default -> { }
            }
        }
        if (hits > 0)
        {
            sb.append("HIT! -").append(hits).append(" HP\n");
        }
        return sb.toString().trim();
    }

    @Override
    protected void disposeExtra()
    {
        tinyFont.dispose();
        pieceRenderer.dispose();
    }
}
