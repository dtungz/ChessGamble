package com.chessgamblegame.screen;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.chessgamblegame.ChessGambleGame;
import com.chessgamblegame.render.UiButton;

import java.util.ArrayList;
import java.util.List;

// Lưới nút chọn level. Số nút = số file levelXX.json đang có trong assets/levels.
public class LevelSelectScreen extends BaseScreen
{
    private static final int COLUMNS = 5;
    private static final float CELL_WIDTH = 110;
    private static final float CELL_HEIGHT = 80;
    private static final float GAP = 24;
    private static final float GRID_TOP = 520;

    private final List<UiButton> levelButtons = new ArrayList<>();
    private final UiButton backButton = new UiButton("BACK", 40, 40, 160, 50);

    public LevelSelectScreen(ChessGambleGame game)
    {
        super(game);
        int count = LevelFiles.count();
        float gridWidth = COLUMNS * CELL_WIDTH + (COLUMNS - 1) * GAP;
        float startX = (ChessGambleGame.WORLD_WIDTH - gridWidth) / 2;
        for (int i = 0; i < count; i++)
        {
            int col = i % COLUMNS;
            int row = i / COLUMNS;
            float x = startX + col * (CELL_WIDTH + GAP);
            float y = GRID_TOP - CELL_HEIGHT - row * (CELL_HEIGHT + GAP);
            levelButtons.add(new UiButton(String.valueOf(i + 1), x, y, CELL_WIDTH, CELL_HEIGHT));
        }
    }

    @Override
    public void render(float delta)
    {
        updateMouse();
        boolean click = clicked();
        if (backButton.isClicked(mouse, click))
        {
            switchTo(new MainMenuScreen(game));
            return;
        }
        for (int i = 0; i < levelButtons.size(); i++)
        {
            if (levelButtons.get(i).isClicked(mouse, click))
            {
                switchTo(GameScreen.forLevel(game, i + 1));
                return;
            }
        }

        beginFrame();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (UiButton b : levelButtons)
        {
            b.drawShape(shapes, mouse);
        }
        backButton.drawShape(shapes, mouse);
        shapes.end();

        batch.begin();
        drawCentered(titleFont, "CHOOSE A LEVEL", 640, TEXT);
        if (levelButtons.isEmpty())
        {
            drawCentered(uiFont, "NO LEVEL FILES FOUND", 400, DIM_TEXT);
        }
        for (UiButton b : levelButtons)
        {
            b.drawLabel(batch, titleFont);
        }
        backButton.drawLabel(batch, uiFont);
        batch.end();
    }
}
