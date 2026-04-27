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

    private final GameModel model;
    private final int[] starX;
    private final int[] starY;
    private final int[] starSize;

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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        try {
            drawStars(g2);
            drawPlayer(g2);
            drawAliens(g2);
            drawBullets(g2);
            drawHud(g2);

            if (model.isGameOver()) {
                drawGameOver(g2);
            }
        } finally {
            g2.dispose();
        }
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
        if (model.hasPlayerBullet()) {
            g2.setColor(Color.YELLOW);
            g2.fillRect(model.getPlayerBulletX(), model.getPlayerBulletY(), PLAYER_BULLET_WIDTH, PLAYER_BULLET_HEIGHT);
        }

        g2.setColor(Color.RED);
        for (int i = 0; i < model.getAlienBulletCount(); i++) {
            g2.fillRect(model.getAlienBulletX(i), model.getAlienBulletY(i), ALIEN_BULLET_WIDTH, ALIEN_BULLET_HEIGHT);
        }
    }

    // --- HUD ---

    private void drawHud(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        g2.drawString("Score: " + model.getScore(), 10, 24);
        g2.drawString("Lives: " + model.getLives(), getWidth() - 100, 24);
    }

    // --- Game-over overlay ---

    private void drawGameOver(Graphics2D g2) {
        String message = model.getLives() <= 0 ? "GAME OVER" : "YOU WIN";
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 42));
        FontMetrics metrics = g2.getFontMetrics();
        int x = (getWidth() - metrics.stringWidth(message)) / 2;
        int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(message, x, y);
    }
}
