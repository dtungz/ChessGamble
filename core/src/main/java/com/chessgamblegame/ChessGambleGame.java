package com.chessgamblegame;

import com.badlogic.gdx.Game;
import com.chessgamblegame.model.GameConfig;
import com.chessgamblegame.screen.MainMenuScreen;

// Điểm khởi đầu của game: giữ màn hình hiện tại, đổi màn bằng setScreen(...).
public class ChessGambleGame extends Game
{
    public static final int WORLD_WIDTH = GameConfig.Display.WINDOW_WIDTH;
    public static final int WORLD_HEIGHT = GameConfig.Display.WINDOW_HEIGHT;

    @Override
    public void create()
    {
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose()
    {
        if (getScreen() != null)
        {
            getScreen().dispose();
        }
    }
}
