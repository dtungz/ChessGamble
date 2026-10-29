package com.chessgamblegame.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.chessgamblegame.level.LevelData;
import com.chessgamblegame.level.LevelLoader;

// Tìm và đọc các file assets/levels/level01.json, level02.json, ... theo số thứ tự.
public class LevelFiles
{
    private static final int MAX_LEVELS = 30;

    private LevelFiles()
    {
    }

    private static FileHandle file(int number)
    {
        return Gdx.files.internal(String.format("levels/level%02d.json", number));
    }

    public static boolean exists(int number)
    {
        return number >= 1 && number <= MAX_LEVELS && file(number).exists();
    }

    // Số level liên tiếp có file, tính từ level 1.
    public static int count()
    {
        int n = 0;
        while (exists(n + 1))
        {
            n++;
        }
        return n;
    }

    public static LevelData load(int number)
    {
        return LevelLoader.load(file(number));
    }
}
