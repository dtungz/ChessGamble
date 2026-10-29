package com.chessgamblegame.level;

import com.chessgamblegame.ai.AttackerFirstStrategy;
import com.chessgamblegame.model.Board;
import com.chessgamblegame.model.GameConfig;
import com.chessgamblegame.model.Position;
import com.chessgamblegame.model.TurnManager;
import com.chessgamblegame.model.enums.PieceType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class LevelLoaderTest
{
    private static String readAsset(String name) throws IOException
    {
        Path path = Path.of("../assets", name);
        if (!Files.exists(path))
        {
            path = Path.of("assets", name);
        }
        return Files.readString(path);
    }

    private static String level(String playerStart, int enemiesPerTurn, String enemiesJson)
    {
        return """
            {
              "id": 99,
              "name": "Test",
              "playerStart": "%s",
              "enemiesPerTurn": %d,
              "enemies": [ %s ]
            }
            """.formatted(playerStart, enemiesPerTurn, enemiesJson);
    }

    // Đọc file thật

    @Test
    void level01FileLoadsCorrectly() throws IOException
    {
        LevelData data = LevelLoader.parse(readAsset("levels/level01.json"));

        assertEquals(1, data.id);
        assertEquals("Làm quen", data.name);
        assertEquals(1, data.enemiesPerTurn);
        assertEquals(3, data.enemies.size());

        Board board = LevelLoader.createBoard(data);
        assertEquals(Position.parse("e1"), board.playerPos);
        assertEquals(3, board.getEnemies().size());
        for (String s : new String[]{"c7", "e7", "g7"})
        {
            assertEquals(PieceType.Pawn, board.getEnemyAt(Position.parse(s)).type);
        }
    }

    @Test
    void loadedLevelCanStartAGame() throws IOException
    {
        LevelData data = LevelLoader.parse(readAsset("levels/level01.json"));
        Board board = LevelLoader.createBoard(data);

        TurnManager tm = new TurnManager(board, new GameConfig(), data.enemiesPerTurn, new Random(0), new AttackerFirstStrategy());

        assertEquals(TurnManager.Phase.WAIT_ROLL, tm.getPhase());
        assertEquals(1, tm.getShakingEnemies().size());
    }

    // Định dạng

    @Test
    void enemyTypeIsCaseInsensitive()
    {
        LevelData data = LevelLoader.parse(level("e1", 1,
            "{ \"type\": \"KNIGHT\", \"pos\": \"b8\" }, { \"type\": \"queen\", \"pos\": \"d8\" }"));

        Board board = LevelLoader.createBoard(data);

        assertEquals(PieceType.Knight, board.getEnemyAt(Position.parse("b8")).type);
        assertEquals(PieceType.Queen, board.getEnemyAt(Position.parse("d8")).type);
    }

    @Test
    void unknownFieldsAreIgnored()
    {
        String json = """
            {
              "id": 3, "name": "Hai con ngựa", "playerStart": "e1", "enemiesPerTurn": 2,
              "selectionStrategy": "NEAREST_FIRST",
              "enemies": [ { "type": "KNIGHT", "pos": "b8" } ]
            }
            """;

        assertEquals(2, LevelLoader.parse(json).enemiesPerTurn);
    }

    @Test
    void eachCreateBoardGivesAFreshBoard()
    {
        LevelData data = LevelLoader.parse(level("e1", 1, "{ \"type\": \"Pawn\", \"pos\": \"a7\" }"));

        Board first = LevelLoader.createBoard(data);
        first.removeEnemy(first.getEnemies().get(0));
        Board second = LevelLoader.createBoard(data);

        assertEquals(1, second.getEnemies().size());
        assertNotSame(first, second);
    }

    // Lỗi trong file level

    @Test
    void kingIsNotAllowedForEnemies()
    {
        assertThrows(IllegalArgumentException.class,
            () -> LevelLoader.parse(level("e1", 1, "{ \"type\": \"King\", \"pos\": \"e8\" }")));
    }

    @Test
    void unknownTypeIsRejected()
    {
        assertThrows(IllegalArgumentException.class,
            () -> LevelLoader.parse(level("e1", 1, "{ \"type\": \"Dragon\", \"pos\": \"e8\" }")));
    }

    @Test
    void squareOutsideBoardIsRejected()
    {
        assertThrows(IllegalArgumentException.class,
            () -> LevelLoader.parse(level("e1", 1, "{ \"type\": \"Pawn\", \"pos\": \"i9\" }")));
        assertThrows(IllegalArgumentException.class,
            () -> LevelLoader.parse(level("z0", 1, "{ \"type\": \"Pawn\", \"pos\": \"a7\" }")));
    }

    @Test
    void twoPiecesOnSameSquareIsRejected()
    {
        assertThrows(IllegalArgumentException.class, () -> LevelLoader.parse(level("e1", 1,
            "{ \"type\": \"Pawn\", \"pos\": \"a7\" }, { \"type\": \"Rook\", \"pos\": \"a7\" }")));
    }

    @Test
    void enemyOnPlayerSquareIsRejected()
    {
        assertThrows(IllegalArgumentException.class,
            () -> LevelLoader.parse(level("e1", 1, "{ \"type\": \"Pawn\", \"pos\": \"e1\" }")));
    }

    @Test
    void levelWithoutEnemiesIsRejected()
    {
        assertThrows(IllegalArgumentException.class, () -> LevelLoader.parse(level("e1", 1, "")));
    }

    @Test
    void enemiesPerTurnMustBePositive()
    {
        assertThrows(IllegalArgumentException.class,
            () -> LevelLoader.parse(level("e1", 0, "{ \"type\": \"Pawn\", \"pos\": \"a7\" }")));
    }

    @Test
    void missingPlayerStartIsRejected()
    {
        String json = """
            { "id": 5, "enemiesPerTurn": 1, "enemies": [ { "type": "Pawn", "pos": "a7" } ] }
            """;

        assertThrows(IllegalArgumentException.class, () -> LevelLoader.parse(json));
    }

    @Test
    void brokenJsonIsRejected()
    {
        assertThrows(IllegalArgumentException.class, () -> LevelLoader.parse("{ \"id\": 1, \"enemies\": [ "));
    }
}
