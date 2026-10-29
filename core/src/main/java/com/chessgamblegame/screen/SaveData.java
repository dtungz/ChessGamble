package com.chessgamblegame.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

// Dữ liệu lưu lại giữa các lần mở game, dùng Preferences của libGDX.
// Trên Windows file nằm ở thư mục người dùng: .prefs/chessgamblegame
public final class SaveData
{
    private static final String FILE = "chessgamblegame";
    private static final String ENDLESS_HIGH_SCORE = "endlessHighScore";

    private SaveData()
    {
    }

    private static Preferences prefs()
    {
        return Gdx.app.getPreferences(FILE);
    }

    public static int getEndlessHighScore()
    {
        return prefs().getInteger(ENDLESS_HIGH_SCORE, 0);
    }

    // Ghi điểm vừa chơi xong. Trả về true nếu đây là kỷ lục mới (điểm cũ đã được thay).
    public static boolean submitEndlessScore(int score)
    {
        Preferences p = prefs();
        if (score <= p.getInteger(ENDLESS_HIGH_SCORE, 0))
        {
            return false;
        }
        p.putInteger(ENDLESS_HIGH_SCORE, score);
        p.flush();   // flush mới thực sự ghi xuống file
        return true;
    }
}
