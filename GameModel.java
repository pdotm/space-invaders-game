import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameModel {
    // This class will store and update all game state (player, enemies, bullets, score, and rules).
    // It will expose plain Java methods for game logic and avoid any Swing/UI dependencies.

    // Game constants
    private static final int GAME_WIDTH = 600;
    private static final int GAME_HEIGHT = 600;
    private static final int PLAYER_WIDTH = 40;
    private static final int PLAYER_HEIGHT = 30;
    private static final int PLAYER_SPEED = 5;
    private static final int ALIEN_WIDTH = 30;
    private static final int ALIEN_HEIGHT = 30;
    private static final int ALIEN_COLS = 11;
    private static final int ALIEN_ROWS = 5;
    private static final int ALIEN_SPEED = 2;
    private static final int BULLET_SPEED = 7;
    private static final int ALIEN_BULLET_SPEED = 4;
    private static final int ALIEN_FIRE_INTERVAL = 60; // frames

    // Game state
    private int playerX;
    private int score;
    private int lives;
    private Alien[][] aliens;
    private Bullet playerBullet;
    private List<Bullet> alienBullets;
    private int alienDirection; // 1 for right, -1 for left
    private int alienFireCounter;
    private Random random;

    // Inner class for Alien
    private static class Alien {
        int x, y;
        boolean alive;

        Alien(int x, int y) {
            this.x = x;
            this.y = y;
            this.alive = true;
        }
    }

    // Inner class for Bullet
    private static class Bullet {
        int x, y;
        boolean active;
        boolean isPlayerBullet;

        Bullet(int x, int y, boolean isPlayerBullet) {
            this.x = x;
            this.y = y;
            this.active = true;
            this.isPlayerBullet = isPlayerBullet;
        }
    }

    public GameModel() {
        this.playerX = GAME_WIDTH / 2 - PLAYER_WIDTH / 2;
        this.score = 0;
        this.lives = 3;
        this.playerBullet = null;
        this.alienBullets = new ArrayList<>();
        this.alienDirection = 1;
        this.alienFireCounter = 0;
        this.random = new Random();

        // Initialize alien formation
        this.aliens = new Alien[ALIEN_ROWS][ALIEN_COLS];
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                int x = col * (ALIEN_WIDTH + 10) + 20;
                int y = row * (ALIEN_HEIGHT + 10) + 20;
                aliens[row][col] = new Alien(x, y);
            }
        }
    }

    public void movePlayerLeft() {
        if (playerX - PLAYER_SPEED > 0) {
            playerX -= PLAYER_SPEED;
        }
    }

    public void movePlayerRight() {
        if (playerX + PLAYER_SPEED < GAME_WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
        }
    }

    public void firePlayerBullet() {
        if (playerBullet == null) {
            int bulletX = playerX + PLAYER_WIDTH / 2 - 2; // center the bullet
            int bulletY = GAME_HEIGHT - PLAYER_HEIGHT - 10;
            playerBullet = new Bullet(bulletX, bulletY, true);
        }
    }

    public void update() {
        // Advance player bullet
        if (playerBullet != null && playerBullet.active) {
            playerBullet.y -= BULLET_SPEED;
            if (playerBullet.y < 0) {
                playerBullet.active = false;
            }
        }

        // Advance alien bullets
        for (Bullet bullet : alienBullets) {
            if (bullet.active) {
                bullet.y += ALIEN_BULLET_SPEED;
                if (bullet.y > GAME_HEIGHT) {
                    bullet.active = false;
                }
            }
        }
        alienBullets.removeIf(b -> !b.active);

        // Move alien formation
        boolean shouldMoveDown = false;
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                if (aliens[row][col].alive) {
                    aliens[row][col].x += alienDirection * ALIEN_SPEED;
                    // Check if any alien hit the edge
                    if ((alienDirection == 1 && aliens[row][col].x + ALIEN_WIDTH >= GAME_WIDTH) ||
                        (alienDirection == -1 && aliens[row][col].x <= 0)) {
                        shouldMoveDown = true;
                    }
                }
            }
        }

        // If edge reached, move all aliens down and reverse direction
        if (shouldMoveDown) {
            for (int row = 0; row < ALIEN_ROWS; row++) {
                for (int col = 0; col < ALIEN_COLS; col++) {
                    aliens[row][col].y += ALIEN_HEIGHT;
                }
            }
            alienDirection *= -1;
        }

        // Fire alien bullets at random intervals
        alienFireCounter++;
        if (alienFireCounter >= ALIEN_FIRE_INTERVAL) {
            fireRandomAlienBullet();
            alienFireCounter = 0;
        }

        // Detect collisions
        detectCollisions();
    }

    private void fireRandomAlienBullet() {
        // Pick a random alive alien and fire from it
        int attempts = 0;
        while (attempts < 10) {
            int row = random.nextInt(ALIEN_ROWS);
            int col = random.nextInt(ALIEN_COLS);
            if (aliens[row][col].alive) {
                int bulletX = aliens[row][col].x + ALIEN_WIDTH / 2 - 2;
                int bulletY = aliens[row][col].y + ALIEN_HEIGHT;
                alienBullets.add(new Bullet(bulletX, bulletY, false));
                break;
            }
            attempts++;
        }
    }

    private void detectCollisions() {
        // Collision: player bullet hits alien
        if (playerBullet != null && playerBullet.active) {
            for (int row = 0; row < ALIEN_ROWS; row++) {
                for (int col = 0; col < ALIEN_COLS; col++) {
                    Alien alien = aliens[row][col];
                    if (alien.alive && checkBulletAlienCollision(playerBullet, alien)) {
                        alien.alive = false;
                        playerBullet.active = false;
                        score += 10;
                    }
                }
            }
        }

        // Collision: alien bullet hits player
        for (Bullet bullet : alienBullets) {
            if (bullet.active && checkBulletPlayerCollision(bullet)) {
                bullet.active = false;
                lives--;
            }
        }
    }

    private boolean checkBulletAlienCollision(Bullet bullet, Alien alien) {
        return bullet.x < alien.x + ALIEN_WIDTH &&
               bullet.x + 4 > alien.x &&
               bullet.y < alien.y + ALIEN_HEIGHT &&
               bullet.y + 10 > alien.y;
    }

    private boolean checkBulletPlayerCollision(Bullet bullet) {
        return bullet.x < playerX + PLAYER_WIDTH &&
               bullet.x + 4 > playerX &&
               bullet.y < GAME_HEIGHT - PLAYER_HEIGHT &&
               bullet.y + 10 > GAME_HEIGHT - PLAYER_HEIGHT;
    }

    // Getters
    public int getPlayerX() {
        return playerX;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public Alien[][] getAliens() {
        return aliens;
    }

    public Bullet getPlayerBullet() {
        return playerBullet;
    }

    public List<Bullet> getAlienBullets() {
        return alienBullets;
    }

    public int getGameWidth() {
        return GAME_WIDTH;
    }

    public int getGameHeight() {
        return GAME_HEIGHT;
    }

    public int getPlayerWidth() {
        return PLAYER_WIDTH;
    }

    public int getPlayerHeight() {
        return PLAYER_HEIGHT;
    }

    public int getAlienWidth() {
        return ALIEN_WIDTH;
    }

    public int getAlienHeight() {
        return ALIEN_HEIGHT;
    }

    public int getAlienRows() {
        return ALIEN_ROWS;
    }

    public int getAlienCols() {
        return ALIEN_COLS;
    }

    public int getPlayerY() {
        return GAME_HEIGHT - PLAYER_HEIGHT;
    }

    public boolean isAlienAlive(int row, int col) {
        return aliens[row][col].alive;
    }

    public int getAlienX(int row, int col) {
        return aliens[row][col].x;
    }

    public int getAlienY(int row, int col) {
        return aliens[row][col].y;
    }

    public boolean hasPlayerBullet() {
        return playerBullet != null && playerBullet.active;
    }

    public int getPlayerBulletX() {
        return playerBullet.x;
    }

    public int getPlayerBulletY() {
        return playerBullet.y;
    }

    public int getAlienBulletCount() {
        return alienBullets.size();
    }

    public int getAlienBulletX(int index) {
        return alienBullets.get(index).x;
    }

    public int getAlienBulletY(int index) {
        return alienBullets.get(index).y;
    }

    public boolean isGameOver() {
        return lives <= 0 || areAllAliensDefeated();
    }

    private boolean areAllAliensDefeated() {
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                if (aliens[row][col].alive) {
                    return false;
                }
            }
        }
        return true;
    }
}
