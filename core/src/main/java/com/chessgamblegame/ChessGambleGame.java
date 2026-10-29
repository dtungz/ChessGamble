package com.chessgamblegame;

import com.badlogic.gdx.Game;
import com.chessgamblegame.screen.MainMenuScreen;

// Điểm khởi đầu của game: giữ màn hình hiện tại, đổi màn bằng setScreen(...).
public class ChessGambleGame extends Game
{
    public static final int WORLD_WIDTH = 1000;
    public static final int WORLD_HEIGHT = 720;

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
