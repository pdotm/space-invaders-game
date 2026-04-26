import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class GameView extends JPanel {
    private static final int PLAYER_BULLET_WIDTH = 4;
    private static final int PLAYER_BULLET_HEIGHT = 10;
    private static final int ALIEN_BULLET_WIDTH = 4;
    private static final int ALIEN_BULLET_HEIGHT = 10;

    private final GameModel model;

    public GameView(GameModel model) {
        this.model = model;
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(model.getGameWidth(), model.getGameHeight()));
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        try {
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

    private void drawPlayer(Graphics2D g2) {
        g2.setColor(Color.GREEN);
        g2.fillRect(model.getPlayerX(), model.getPlayerY(), model.getPlayerWidth(), model.getPlayerHeight());
    }

    private void drawAliens(Graphics2D g2) {
        g2.setColor(Color.CYAN);
        for (int row = 0; row < model.getAlienRows(); row++) {
            for (int col = 0; col < model.getAlienCols(); col++) {
                if (model.isAlienAlive(row, col)) {
                    g2.fillRect(
                        model.getAlienX(row, col),
                        model.getAlienY(row, col),
                        model.getAlienWidth(),
                        model.getAlienHeight()
                    );
                }
            }
        }
    }

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

    private void drawHud(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        g2.drawString("Score: " + model.getScore(), 10, 24);
        g2.drawString("Lives: " + model.getLives(), getWidth() - 100, 24);
    }

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
