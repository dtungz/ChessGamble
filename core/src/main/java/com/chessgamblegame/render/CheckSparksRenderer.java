package com.chessgamblegame.render;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

// Cảnh báo "đang bị chiếu": các tia nhỏ toả ra từ đỉnh đầu quân người chơi theo hình vòng cung.
// Tia bay từ trong ra ngoài qua 5 khung hình (mỗi khung 0.08s), tắt một chút rồi lặp lại.
public class CheckSparksRenderer
{
    private static final int FRAMES = 5;
    private static final float FRAME_TIME = 0.08f;
    private static final float PAUSE_TIME = 0.25f;
    private static final float CYCLE = FRAMES * FRAME_TIME + PAUSE_TIME;

    // 5 tia trên vòng cung phía trên đầu (độ, 90 = thẳng lên trên)
    private static final float[] ANGLES = {25, 57, 90, 123, 155};
    private static final float START_RADIUS = BoardRenderer.CELL * 0.18f;
    private static final float END_RADIUS = BoardRenderer.CELL * 0.50f;
    private static final float HEAD_HEIGHT = BoardRenderer.CELL * 0.72f;   // đỉnh đầu quân, tính từ đáy ô
    private static final float GRID = 2;

    // (cellX, cellY): góc dưới-trái ô của người chơi (lấy từ BoardView để khớp animation).
    // Gọi giữa shapes.begin(Filled) và shapes.end(). time: tổng thời gian đã chạy (giây).
    public void draw(ShapeRenderer shapes, float cellX, float cellY, float time)
    {
        float t = time % CYCLE;
        if (t >= FRAMES * FRAME_TIME)
        {
            return;   // khoảng nghỉ giữa 2 lần toả
        }
        int frame = (int) (t / FRAME_TIME);                  // 0..4
        float progress = frame / (float) (FRAMES - 1);       // 0..1
        float radius = START_RADIUS + (END_RADIUS - START_RADIUS) * progress;
        float size = frame < 2 ? 4 : (frame < 4 ? 6 : 4);    // nhỏ -> to -> nhỏ lại khi tan ra

        float cx = cellX + BoardRenderer.CELL / 2;
        float cy = cellY + HEAD_HEIGHT;
        for (float deg : ANGLES)
        {
            double rad = Math.toRadians(deg);
            spark(shapes, cx + (float) Math.cos(rad) * radius, cy + (float) Math.sin(rad) * radius, size);
        }
    }

    // Một tia = hình thoi pixel: viền tối, lõi sáng.
    private static void spark(ShapeRenderer shapes, float x, float y, float size)
    {
        float sx = Math.round(x / GRID) * GRID;
        float sy = Math.round(y / GRID) * GRID;
        float h = size / 2;
        shapes.setColor(Palette.OUTLINE);
        shapes.rect(sx - h, sy - h - 2, size, size + 4);
        shapes.rect(sx - h - 2, sy - h, size + 4, size);
        shapes.setColor(Palette.CARD);
        shapes.rect(sx - h + 1, sy - h + 1, size - 2, size - 2);
    }
}
