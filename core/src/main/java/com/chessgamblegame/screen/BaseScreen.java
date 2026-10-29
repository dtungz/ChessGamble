package com.chessgamblegame.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.chessgamblegame.ChessGambleGame;
import com.chessgamblegame.render.Palette;

// Phần chung của mọi màn hình: camera, viewport, bút vẽ, font, vị trí chuột.
public abstract class BaseScreen extends ScreenAdapter
{
    protected static final Color TEXT = Palette.TEXT;
    protected static final Color DIM_TEXT = Palette.DIM_TEXT;
    protected static final Color WIN_TEXT = Palette.WIN;
    protected static final Color LOSE_TEXT = Palette.ACCENT;

    private static final String PIXEL_FONT = "fonts/PressStart2P-Regular.ttf";

    protected final ChessGambleGame game;
    protected final OrthographicCamera camera = new OrthographicCamera();
    protected final Viewport viewport = new FitViewport(ChessGambleGame.WORLD_WIDTH, ChessGambleGame.WORLD_HEIGHT, camera);
    protected final ShapeRenderer shapes = new ShapeRenderer();
    protected final SpriteBatch batch = new SpriteBatch();
    protected final BitmapFont smallFont = createFont(12);
    protected final BitmapFont uiFont = createFont(16);
    protected final BitmapFont titleFont = createFont(32);
    protected final Vector2 mouse = new Vector2();   // vị trí chuột trong toạ độ game

    private final GlyphLayout layout = new GlyphLayout();
    private boolean disposed;

    protected BaseScreen(ChessGambleGame game)
    {
        this.game = game;
    }

    // Xoá màn hình, áp viewport, bật trong suốt. Gọi đầu mỗi render().
    protected void beginFrame()
    {
        ScreenUtils.clear(Palette.BACKGROUND);
        viewport.apply();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    // Luôn đổi toạ độ chuột qua viewport: trục Y của chuột tính từ trên xuống, còn của game từ dưới lên.
    protected void updateMouse()
    {
        viewport.unproject(mouse.set(Gdx.input.getX(), Gdx.input.getY()));
    }

    protected boolean clicked()
    {
        return Gdx.input.justTouched();
    }

    // Chuyển sang màn khác. Màn hiện tại bị dispose ngay (qua hide()), nên sau khi gọi hàm này
    // render() phải return luôn, không được vẽ tiếp: kiểm tra bằng biến leaving.
    protected boolean leaving;

    protected void switchTo(Screen next)
    {
        leaving = true;
        game.setScreen(next);
    }

    // Viết chữ căn giữa theo chiều ngang. Gọi giữa batch.begin() và batch.end()
    protected void drawCentered(BitmapFont font, String text, float y, Color color)
    {
        drawCentered(font, text, ChessGambleGame.WORLD_WIDTH / 2f, y, color);
    }

    protected void drawCentered(BitmapFont font, String text, float centerX, float y, Color color)
    {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, text, centerX - layout.width / 2, y);
    }

    // Font pixel Press Start 2P vẽ ở đúng cỡ chữ (mono + Nearest) để nét chữ vuông vức, không bị mờ.
    protected static BitmapFont createFont(int size)
    {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(PIXEL_FONT));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = size;
        param.mono = true;
        param.minFilter = Texture.TextureFilter.Nearest;
        param.magFilter = Texture.TextureFilter.Nearest;
        BitmapFont font = generator.generateFont(param);
        generator.dispose();
        font.setUseIntegerPositions(true);
        return font;
    }

    @Override
    public void resize(int width, int height)
    {
        viewport.update(width, height, true);
    }

    // Game.setScreen(...) gọi hide() cho màn cũ -> giải phóng luôn tài nguyên của màn đó.
    @Override
    public void hide()
    {
        dispose();
    }

    @Override
    public void dispose()
    {
        if (disposed)
        {
            return;
        }
        disposed = true;
        shapes.dispose();
        batch.dispose();
        smallFont.dispose();
        uiFont.dispose();
        titleFont.dispose();
        disposeExtra();
    }

    // Màn con có tài nguyên riêng (font, ảnh...) thì giải phóng ở đây. Chỉ được gọi đúng 1 lần.
    protected void disposeExtra()
    {
    }
}
