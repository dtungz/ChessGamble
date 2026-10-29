package com.chessgamblegame.level;

import com.chessgamblegame.ai.PathFinder;
import com.chessgamblegame.model.Board;
import com.chessgamblegame.model.Piece;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

// Kiểm tra mọi file assets/levels/levelXX.json: đọc được, đánh số đúng, và không có quân nào
// đánh được người chơi ngay từ lượt đầu (người chơi chưa kịp làm gì đã mất máu).
class AllLevelsTest
{
    private static final int LEVEL_COUNT = 10;

    // Số quân địch mỗi level theo bảng mẫu trong GDD 6.3
    private static final int[] ENEMY_COUNT = {3, 5, 4, 4, 4, 5, 4, 4, 10, 15};

    private static Path levelFile(int n)
    {
        String name = String.format("levels/level%02d.json", n);
        Path path = Path.of("../assets", name);
        return Files.exists(path) ? path : Path.of("assets", name);
    }

    private static LevelData load(int n) throws IOException
    {
        return LevelLoader.parse(Files.readString(levelFile(n)));
    }

    @Test
    void allTenLevelFilesExist()
    {
        for (int n = 1; n <= LEVEL_COUNT; n++)
        {
            assertTrue(Files.exists(levelFile(n)), "Thiếu file level " + n);
        }
    }

    @Test
    void everyLevelHasMatchingIdAndSaneSettings() throws IOException
    {
        for (int n = 1; n <= LEVEL_COUNT; n++)
        {
            LevelData data = load(n);
            assertEquals(n, data.id, "Sai id trong level " + n);
            assertNotNull(data.name);
            assertTrue(data.enemiesPerTurn >= 1 && data.enemiesPerTurn <= 3, "enemiesPerTurn level " + n);
            assertEquals(ENEMY_COUNT[n - 1], data.enemies.size(), "Số quân địch level " + n);
        }
    }

    @Test
    void noEnemyCanHitThePlayerOnTheFirstTurn() throws IOException
    {
        for (int n = 1; n <= LEVEL_COUNT; n++)
        {
            Board board = LevelLoader.createBoard(load(n));
            for (Piece e : board.getEnemies())
            {
                assertNotEquals(1, PathFinder.distanceToPlayer(board, e),
                    "Level " + n + ": " + e + " đánh được người chơi ngay lượt đầu");
            }
        }
    }

    @Test
    void levelsGetHarderOverall() throws IOException
    {
        assertTrue(load(LEVEL_COUNT).enemies.size() > load(1).enemies.size());
        assertTrue(load(LEVEL_COUNT).enemiesPerTurn >= load(1).enemiesPerTurn);
    }
}
