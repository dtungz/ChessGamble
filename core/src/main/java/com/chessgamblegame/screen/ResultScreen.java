package com.chessgamblegame.screen;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.chessgamblegame.ChessGambleGame;
import com.chessgamblegame.render.UiButton;

// Màn kết quả sau khi thắng hoặc thua.
public class ResultScreen extends BaseScreen
{
    private static final float BUTTON_WIDTH = 260;
    private static final float BUTTON_HEIGHT = 56;
    private static final float BUTTON_X = (ChessGambleGame.WORLD_WIDTH - BUTTON_WIDTH) / 2;

    private final boolean won;
    private final int score;
    private final int turns;
    private final int levelNumber;   // 0 = Endless

    private final UiButton againButton = new UiButton("PLAY AGAIN", BUTTON_X, 300, BUTTON_WIDTH, BUTTON_HEIGHT);
    private final UiButton nextButton = new UiButton("NEXT LEVEL", BUTTON_X, 225, BUTTON_WIDTH, BUTTON_HEIGHT);
    private final UiButton menuButton = new UiButton("MENU", BUTTON_X, 150, BUTTON_WIDTH, BUTTON_HEIGHT);

    public ResultScreen(ChessGambleGame game, boolean won, int score, int turns, int levelNumber)
    {
        super(game);
        this.won = won;
        this.score = score;
        this.turns = turns;
        this.levelNumber = levelNumber;
        nextButton.enabled = won && levelNumber > 0 && LevelFiles.exists(levelNumber + 1);
    }

    private boolean isEndless()
    {
        return levelNumber == 0;
    }

    @Override
    public void render(float delta)
    {
        updateMouse();
        boolean click = clicked();
        if (againButton.isClicked(mouse, click))
        {
            switchTo(isEndless() ? GameScreen.endless(game) : GameScreen.forLevel(game, levelNumber));
            return;
        }
        if (!isEndless() && nextButton.isClicked(mouse, click))
        {
            switchTo(GameScreen.forLevel(game, levelNumber + 1));
            return;
        }
        if (menuButton.isClicked(mouse, click))
        {
            switchTo(new MainMenuScreen(game));
            return;
        }

        beginFrame();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        againButton.drawShape(shapes, mouse);
        if (!isEndless())
        {
            nextButton.drawShape(shapes, mouse);
        }
        menuButton.drawShape(shapes, mouse);
        shapes.end();

        batch.begin();
        String title = won ? "YOU WIN!" : "GAME OVER";
        drawCentered(titleFont, title, 600, won ? WIN_TEXT : LOSE_TEXT);
        drawCentered(uiFont, isEndless() ? "ENDLESS MODE" : "LEVEL " + levelNumber, 520, DIM_TEXT);
        drawCentered(uiFont, "SCORE " + score + "    TURNS " + turns, 470, TEXT);
        if (won && levelNumber > 0 && !nextButton.enabled)
        {
            drawCentered(uiFont, "THAT WAS THE LAST LEVEL!", 420, DIM_TEXT);
        }
        againButton.drawLabel(batch, uiFont);
        if (!isEndless())
        {
            nextButton.drawLabel(batch, uiFont);
        }
        menuButton.drawLabel(batch, uiFont);
        batch.end();
    }
}
