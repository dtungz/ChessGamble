package com.chessgamblegame.level;

import java.util.ArrayList;

public class LevelData
{
    public int id;
    public String name;
    public String playerStart;
    public int enemiesPerTurn;
    public ArrayList<EnemyData> enemies = new ArrayList<>();

    public static class EnemyData
    {
        public String type;
        public String pos;

        public EnemyData()
        {
        }

        public EnemyData(String type, String pos)
        {
            this.type = type;
            this.pos = pos;
        }
    }
}
