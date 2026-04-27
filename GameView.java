import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

import javax.swing.JPanel;

public class GameView extends JPanel {
    private static final int PLAYER_BULLET_WIDTH = 4;
    private static final int PLAYER_BULLET_HEIGHT = 10;
    private static final int ALIEN_BULLET_WIDTH = 4;
    private static final int ALIEN_BULLET_HEIGHT = 10;
    private static final int STAR_COUNT = 120;

    private GameModel model;
    private final int[] starX;
    private final int[] starY;
    private final int[] starSize;

    // Menu state
    private boolean menuActive = true;
    private int     menuHighScore = 0;
    private boolean menuNewHighScore = false;

    // Cached bounds of the START button, set during drawMenu and used for hit-testing
    private int btnX, btnY, btnW, btnH;

    public GameView(GameModel model) {
        this.model = model;
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(model.getGameWidth(), model.getGameHeight()));
        setFocusable(true);

        // Generate a fixed starfield so it does not flicker between repaints
        Random rng = new Random(98765);
        starX    = new int[STAR_COUNT];
        starY    = new int[STAR_COUNT];
        starSize = new int[STAR_COUNT];
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i]    = rng.nextInt(model.getGameWidth());
            starY[i]    = rng.nextInt(model.getGameHeight());
            starSize[i] = (rng.nextInt(5) == 0) ? 2 : 1;
        }
    }

    // --- Public API for controller ---

    public void setModel(GameModel m) {
        this.model = m;
    }

    public void showMenu(int highScore, boolean newHighScore) {
        this.menuActive       = true;
        this.menuHighScore    = highScore;
        this.menuNewHighScore = newHighScore;
    }

    public void showGame() {
        this.menuActive = false;
    }

    public boolean isMenuActive() {
        return menuActive;
    }

    /** Returns true when (mx, my) falls inside the START button drawn in the last menu repaint. */
    public boolean isStartButtonAt(int mx, int my) {
        return mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        try {
            drawStars(g2);
            if (menuActive) {
                drawMenu(g2);
            } else {
                drawPowerup(g2);
                drawPlayer(g2);
                drawAliens(g2);
                drawBullets(g2);
                drawHud(g2);
            }
        } finally {
            g2.dispose();
        }
    }

    // --- Menu screen ---

    private void drawMenu(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        // Title
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        String title = "SPACE INVADERS";
        FontMetrics tfm = g2.getFontMetrics();
        int tx = (w - tfm.stringWidth(title)) / 2;
        g2.setColor(new Color(0, 200, 255));
        g2.drawString(title, tx, h / 2 - 110);

        // New high score banner
        if (menuNewHighScore) {
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
            String banner = "** NEW HIGH SCORE! **";
            FontMetrics bfm = g2.getFontMetrics();
            g2.setColor(new Color(255, 215, 0));
            g2.drawString(banner, (w - bfm.stringWidth(banner)) / 2, h / 2 - 60);
        }

        // High score line
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        String hsLine = "High Score:  " + menuHighScore;
        FontMetrics hfm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        g2.drawString(hsLine, (w - hfm.stringWidth(hsLine)) / 2, h / 2 - 20);

        // START button
        btnW = 200;
        btnH = 50;
        btnX = (w - btnW) / 2;
        btnY = h / 2 + 20;

        g2.setColor(new Color(0, 160, 80));
        g2.fillRoundRect(btnX, btnY, btnW, btnH, 12, 12);
        g2.setColor(new Color(0, 220, 110));
        g2.drawRoundRect(btnX, btnY, btnW, btnH, 12, 12);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        String btnLabel = "START GAME";
        FontMetrics lfm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        g2.drawString(btnLabel,
            btnX + (btnW - lfm.stringWidth(btnLabel)) / 2,
            btnY + (btnH + lfm.getAscent() - lfm.getDescent()) / 2);

        // Keyboard hint
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        String hint = "or press ENTER / SPACE";
        FontMetrics efm = g2.getFontMetrics();
        g2.setColor(new Color(160, 160, 160));
        g2.drawString(hint, (w - efm.stringWidth(hint)) / 2, btnY + btnH + 30);
    }

    // --- Background ---

    private void drawStars(Graphics2D g2) {
        for (int i = 0; i < STAR_COUNT; i++) {
            // Vary brightness across four levels so the field feels dimensional
            int b = 110 + (i % 4) * 35;
            g2.setColor(new Color(b, b, b));
            g2.fillRect(starX[i], starY[i], starSize[i], starSize[i]);
        }
    }

    // --- Player ship ---
    // Shape: fighter jet with swept wings, nose pointing up.
    // Bounding box: PLAYER_WIDTH=40, PLAYER_HEIGHT=30.
    //
    //          (x+20, y+0)     <- nose tip
    //         /             \
    //   (x+7,y+14)     (x+33,y+14)  <- shoulders
    //  /                       \
    // (x+0,y+30)           (x+40,y+30) <- wing tips
    //         \             /
    //    (x+12,y+22)  (x+28,y+22)  <- engine notches

    private void drawPlayer(Graphics2D g2) {
        int x = model.getPlayerX();
        int y = model.getPlayerY();

        // Main hull
        int[] xs = { x+20, x+33, x+40, x+28, x+12, x+0,  x+7  };
        int[] ys = { y+0,  y+14, y+30, y+22, y+22, y+30, y+14 };
        g2.setColor(Color.CYAN);
        g2.fillPolygon(xs, ys, xs.length);

        // Centre ridge accent (darker spine running nose to engines)
        g2.setColor(new Color(30, 130, 45));
        int[] spineX = { x+20, x+24, x+20, x+16 };
        int[] spineY = { y+2,  y+20, y+22, y+20 };
        g2.fillPolygon(spineX, spineY, spineX.length);

        // Cockpit window
        g2.setColor(new Color(110, 200, 255));
        g2.fillOval(x+15, y+9, 10, 8);

        // Engine glow (two small rectangles at the base)
        g2.setColor(new Color(255, 150, 20, 210));
        g2.fillRect(x+13, y+23, 6, 5);
        g2.fillRect(x+21, y+23, 6, 5);
    }

    // --- Alien ships ---
    // Shape: classic crab-invader silhouette.
    // Bounding box: ALIEN_WIDTH=30, ALIEN_HEIGHT=30.
    //
    //   [ant] [ant]        <- antennae (small filled squares at top)
    //  +------hex------+
    //  |   O       O   |  <- eyes
    //  |   . . .   .   |  <- mouth dots
    //  +---------------+
    //  [leg][leg][leg]     <- bottom prongs

    private void drawAliens(Graphics2D g2) {
        for (int row = 0; row < model.getAlienRows(); row++) {
            for (int col = 0; col < model.getAlienCols(); col++) {
                if (model.isAlienAlive(row, col)) {
                    drawAlien(g2, model.getAlienX(row, col), model.getAlienY(row, col));
                }
            }
        }
    }

    private void drawAlien(Graphics2D g2, int x, int y) {
        // Body: octagonal silhouette
        int[] bx = { x+8,  x+22, x+28, x+28, x+22, x+8,  x+2,  x+2  };
        int[] by = { y+5,  y+5,  y+10, y+20, y+25, y+25, y+20, y+10 };
        g2.setColor(Color.GREEN);
        g2.fillPolygon(bx, by, bx.length);

        // Antennae
        g2.fillRect(x+7,  y+0, 3, 6);
        g2.fillRect(x+20, y+0, 3, 6);

        // Bottom legs / prongs
        g2.fillRect(x+2,  y+25, 4, 5);
        g2.fillRect(x+13, y+25, 4, 5);
        g2.fillRect(x+24, y+25, 4, 5);

        // Eyes (dark sockets)
        g2.setColor(new Color(0, 40, 70));
        g2.fillOval(x+8,  y+11, 6, 6);
        g2.fillOval(x+16, y+11, 6, 6);

        // Eye highlights
        g2.setColor(new Color(160, 255, 255));
        g2.fillOval(x+9,  y+12, 2, 2);
        g2.fillOval(x+17, y+12, 2, 2);

        // Mouth dots
        g2.setColor(new Color(0, 40, 70));
        g2.fillRect(x+9,  y+20, 3, 2);
        g2.fillRect(x+14, y+20, 3, 2);
        g2.fillRect(x+19, y+20, 3, 2);
    }

    // --- Bullets ---

    private void drawBullets(Graphics2D g2) {
        g2.setColor(Color.YELLOW);
        if (model.hasPlayerBullet()) {
            g2.fillRect(model.getPlayerBulletX(), model.getPlayerBulletY(), PLAYER_BULLET_WIDTH, PLAYER_BULLET_HEIGHT);
        }
        if (model.hasPlayerBulletLeft()) {
            g2.fillRect(model.getPlayerBulletLeftX(), model.getPlayerBulletLeftY(), PLAYER_BULLET_WIDTH, PLAYER_BULLET_HEIGHT);
        }
        if (model.hasPlayerBulletRight()) {
            g2.fillRect(model.getPlayerBulletRightX(), model.getPlayerBulletRightY(), PLAYER_BULLET_WIDTH, PLAYER_BULLET_HEIGHT);
        }

        g2.setColor(Color.RED);
        for (int i = 0; i < model.getAlienBulletCount(); i++) {
            g2.fillRect(model.getAlienBulletX(i), model.getAlienBulletY(i), ALIEN_BULLET_WIDTH, ALIEN_BULLET_HEIGHT);
        }
    }

    private void drawPowerup(Graphics2D g2) {
        if (!model.hasPowerup()) return;
        int x  = model.getPowerupX();
        int y  = model.getPowerupY();
        int s  = model.getPowerupSize();
        int cx = x + s / 2;
        int cy = y + s / 2;

        // Soft outer glow
        g2.setColor(new Color(255, 200, 0, 80));
        g2.fillOval(cx - 14, cy - 14, 28, 28);

        // Gold diamond body
        int[] px = { cx,       cx + s/2, cx,       cx - s/2 };
        int[] py = { cy - s/2, cy,       cy + s/2, cy       };
        g2.setColor(new Color(255, 215, 0));
        g2.fillPolygon(px, py, 4);

        // Inner highlight diamond
        int h = s / 4;
        int[] hx = { cx,     cx + h, cx,     cx - h };
        int[] hy = { cy - h, cy,     cy + h, cy     };
        g2.setColor(new Color(255, 255, 180));
        g2.fillPolygon(hx, hy, 4);

        // Centre sparkle
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - 2, cy - 2, 4, 4);
    }

    // --- HUD ---

    private void drawHud(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        g2.drawString("Score: " + model.getScore(), 10, 24);
        g2.drawString("Lives: " + model.getLives(), getWidth() - 100, 24);
        if (model.hasTripleShot()) {
            g2.setColor(new Color(255, 215, 0));
            g2.drawString("TRIPLE", getWidth() / 2 - 28, 24);
        }
    }

}
