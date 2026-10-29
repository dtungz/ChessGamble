package com.chessgamblegame.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.chessgamblegame.ChessGambleGame;
import com.chessgamblegame.render.UiButton;

// Menu chính: chọn chế độ Level, Endless hoặc thoát.
public class MainMenuScreen extends BaseScreen
{
    private static final float BUTTON_WIDTH = 300;
    private static final float BUTTON_HEIGHT = 60;
    private static final float BUTTON_X = (ChessGambleGame.WORLD_WIDTH - BUTTON_WIDTH) / 2;

    private final UiButton levelButton = new UiButton("LEVEL MODE", BUTTON_X, 380, BUTTON_WIDTH, BUTTON_HEIGHT);
    private final UiButton endlessButton = new UiButton("ENDLESS MODE", BUTTON_X, 295, BUTTON_WIDTH, BUTTON_HEIGHT);
    private final UiButton quitButton = new UiButton("QUIT", BUTTON_X, 210, BUTTON_WIDTH, BUTTON_HEIGHT);

    private final int endlessHighScore;

    public MainMenuScreen(ChessGambleGame game)
    {
        super(game);
        endlessHighScore = SaveData.getEndlessHighScore();
    }

    @Override
    public void render(float delta)
    {
        updateMouse();
        boolean click = clicked();
        if (levelButton.isClicked(mouse, click))
        {
            switchTo(new LevelSelectScreen(game));
            return;
        }
        if (endlessButton.isClicked(mouse, click))
        {
            switchTo(GameScreen.endless(game));
            return;
        }
        if (quitButton.isClicked(mouse, click))
        {
            Gdx.app.exit();
            return;
        }

        beginFrame();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        levelButton.drawShape(shapes, mouse);
        endlessButton.drawShape(shapes, mouse);
        quitButton.drawShape(shapes, mouse);
        shapes.end();

        batch.begin();
        drawCentered(titleFont, "CHESSGAMBLEGAME", 590, TEXT);
        drawCentered(smallFont, "ROLL THE DICE. MOVE LIKE THAT PIECE. SURVIVE.", 520, DIM_TEXT);
        levelButton.drawLabel(batch, uiFont);
        endlessButton.drawLabel(batch, uiFont);
        quitButton.drawLabel(batch, uiFont);
        if (endlessHighScore > 0)
        {
            drawCentered(smallFont, "ENDLESS BEST: " + endlessHighScore, 175, DIM_TEXT);
        }
        batch.end();
    }
}
