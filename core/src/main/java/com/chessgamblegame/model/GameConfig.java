package com.chessgamblegame.model;

import com.chessgamblegame.model.enums.PieceType;

import java.util.EnumMap;
import java.util.Map;

// ============================================================================
//  FILE CẤU HÌNH DUY NHẤT CỦA GAME
//  Muốn chỉnh độ khó, điểm, tốc độ hiệu ứng, màu sắc... chỉ cần sửa trong file này.
//
//  - Phần 1–5 (luật chơi): là biến của đối tượng GameConfig, test có thể tạo cấu hình riêng.
//  - Phần 6–7 (hiển thị, màu): là hằng số static, chỉ đọc khi vẽ.
//  Vị trí/kích thước từng ô, nút trên màn hình vẫn nằm trong các class vẽ (render/, screen/).
// ============================================================================
public class GameConfig
{
    // ---------- 1. Người chơi ----------
    public int maxHp = 3;
    public int startHp = 3;

    // ---------- 2. Điểm ----------
    public final Map<PieceType, Integer> captureScore = new EnumMap<>(PieceType.class);
    public int surviveTurnScore = 1;          // Endless: sống sót qua 1 lượt được cộng điểm này
    public int healEveryScore = 100;          // Endless: cứ mỗi bấy nhiêu điểm tích luỹ thì hồi 1 máu

    // ---------- 3. Xúc xắc ----------
    // Trọng số mỗi mặt, mặt nào số lớn hơn thì dễ ra hơn (0 = không bao giờ ra).
    public final Map<PieceType, Integer> diceWeights = new EnumMap<>(PieceType.class);

    // ---------- 4. Quân địch ----------
    // Tốt địch xuống hàng cuối được phong ngẫu nhiên 1 trong các loại này (địch không có Vua).
    public PieceType[] promotionTypes = {PieceType.Knight, PieceType.Bishop, PieceType.Rook, PieceType.Queen};

    // ---------- 5. Endless ----------
    public String endlessPlayerStart = "e1";
    public int endlessStartPawns = 2;

    // Sinh quân: cứ endlessSpawnEvery lượt sinh 1 đợt ở hàng trên cùng.
    public int endlessSpawnEvery = 3;
    // Số quân mỗi đợt tăng theo điểm: bắt đầu endlessSpawnCountStart con,
    // cứ thêm endlessSpawnCountScoreStep điểm thì đợt sau sinh thêm 1 con, tối đa endlessSpawnCountMax.
    public int endlessSpawnCountStart = 1;
    public int endlessSpawnCountScoreStep = 100;
    public int endlessSpawnCountMax = 6;
    public int endlessMaxEnemiesOnBoard = 12;

    // Loại quân được sinh: cứ endlessTierEvery lượt mở thêm 1 bậc (bậc cuối dùng mãi về sau).
    public int endlessTierEvery = 10;
    public PieceType[][] endlessTiers = {
        {PieceType.Pawn},
        {PieceType.Pawn, PieceType.Knight, PieceType.Bishop},
        {PieceType.Pawn, PieceType.Knight, PieceType.Bishop, PieceType.Rook},
        {PieceType.Pawn, PieceType.Knight, PieceType.Bishop, PieceType.Rook, PieceType.Queen}
    };

    // Số quân địch được đi mỗi lượt: bắt đầu từ Start, cứ IncreaseEvery lượt thêm 1, tối đa Max.
    public int endlessEnemiesPerTurnStart = 1;
    public int endlessEnemiesPerTurnIncreaseEvery = 10;
    public int endlessMaxEnemiesPerTurn = 3;

    public GameConfig()
    {
        captureScore.put(PieceType.Pawn, 10);
        captureScore.put(PieceType.Knight, 30);
        captureScore.put(PieceType.Bishop, 30);
        captureScore.put(PieceType.Rook, 50);
        captureScore.put(PieceType.Queen, 90);

        for (PieceType type : PieceType.values())
        {
            diceWeights.put(type, 1);
        }
    }

    public int scoreFor(PieceType capturedType)
    {
        return captureScore.getOrDefault(capturedType, 0);
    }

    // ---------- 6. Hiển thị & hiệu ứng (giây, pixel) ----------
    public static final class Display
    {
        public static final int WINDOW_WIDTH = 1000;
        public static final int WINDOW_HEIGHT = 720;

        public static final String FONT_FILE = "fonts/PressStart2P-Regular.ttf";
        public static final int FONT_TINY = 8;
        public static final int FONT_SMALL = 12;
        public static final int FONT_UI = 16;
        public static final int FONT_TITLE = 32;

        // Xúc xắc lăn bao lâu và đổi mặt nhanh cỡ nào
        public static final float DICE_ROLL_TIME = 0.8f;
        public static final float DICE_FACE_CHANGE_EVERY = 0.08f;

        // Quân địch sắp đi lắc qua lại
        public static final float SHAKE_AMPLITUDE = 1.5f;
        public static final float SHAKE_SPEED = 40f;

        // Quân trượt khi phát lại lượt đi
        public static final float PIECE_MOVE_SPEED = 12f;      // càng lớn trượt càng nhanh
        public static final float PIECE_MAX_MOVE_TIME = 0.35f;
        public static final float EVENT_SHORT_PAUSE = 0.05f;
        public static final float EVENT_LONG_PAUSE = 0.25f;    // phong cấp, hồi máu, quân mới xuất hiện

        // Bị đánh: quân người chơi chớp tắt và màn hình rung nhẹ
        public static final float HIT_TIME = 0.5f;
        public static final float HIT_BLINK_EVERY = 0.08f;
        public static final float HIT_SHAKE_PX = 4f;

        // Bị chiếu: tia toả trên đầu người chơi
        public static final int CHECK_SPARK_FRAMES = 5;
        public static final float CHECK_SPARK_FRAME_TIME = 0.08f;
        public static final float CHECK_SPARK_PAUSE = 0.25f;

        // Hết ván, đợi bao lâu rồi mới sang màn kết quả
        public static final float RESULT_DELAY = 1.0f;

        private Display()
        {
        }
    }

    // ---------- 7. Màu sắc (mã hex RRGGBB) ----------
    public static final class Colors
    {
        public static final String BACKGROUND = "1A1C2C";
        public static final String PANEL = "262840";
        public static final String LIGHT_SQUARE = "F4D3B0";
        public static final String DARK_SQUARE = "8B86A6";
        public static final String BORDER = "5C5A7A";
        public static final String OUTLINE = "0F1020";
        public static final String ACCENT = "E8264A";
        public static final String TEXT = "E8E4F0";
        public static final String DIM_TEXT = "8B86A6";
        public static final String CARD = "F4D3B0";
        public static final String CARD_TEXT = "1A1C2C";
        public static final String EMPTY_HEART = "3A3B55";
        public static final String WIN = "7BD88F";

        private Colors()
        {
        }
    }
}
