public class ModelTester {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testPlayerLeftEdge();
        testPlayerRightEdge();
        testFireWhileBulletInFlight();
        testBulletRemovedAtTop();
        testDestroyAlienIncreasesScore();
        testLosingAllLivesTriggersGameOver();

        System.out.println();
        System.out.println("Total Passed: " + passed);
        System.out.println("Total Failed: " + failed);
    }

    private static void testPlayerLeftEdge() {
        GameModel model = new GameModel();

        for (int i = 0; i < 500; i++) {
            model.movePlayerLeft();
        }

        int atEdge = model.getPlayerX();
        model.movePlayerLeft();
        int afterExtraMove = model.getPlayerX();

        boolean ok = atEdge >= 0 && afterExtraMove == atEdge;
        report("Player cannot move past left edge", ok,
            "x at edge=" + atEdge + ", after extra move=" + afterExtraMove);
    }

    private static void testPlayerRightEdge() {
        GameModel model = new GameModel();

        for (int i = 0; i < 500; i++) {
            model.movePlayerRight();
        }

        int atEdge = model.getPlayerX();
        model.movePlayerRight();
        int afterExtraMove = model.getPlayerX();

        int maxAllowed = model.getGameWidth() - model.getPlayerWidth();
        boolean ok = atEdge <= maxAllowed && afterExtraMove == atEdge;
        report("Player cannot move past right edge", ok,
            "x at edge=" + atEdge + ", max allowed=" + maxAllowed + ", after extra move=" + afterExtraMove);
    }

    private static void testFireWhileBulletInFlight() {
        GameModel model = new GameModel();

        model.firePlayerBullet();
        int firstX = model.getPlayerBulletX();
        int firstY = model.getPlayerBulletY();

        model.firePlayerBullet();
        int secondX = model.getPlayerBulletX();
        int secondY = model.getPlayerBulletY();

        boolean ok = model.hasPlayerBullet() && firstX == secondX && firstY == secondY;
        report("Firing while bullet in flight does nothing", ok,
            "first=(" + firstX + "," + firstY + "), second=(" + secondX + "," + secondY + ")");
    }

    private static void testBulletRemovedAtTop() {
        GameModel model = new GameModel();

        movePlayerToX(model, model.getGameWidth() - model.getPlayerWidth());
        model.firePlayerBullet();

        int steps = 0;
        while (model.hasPlayerBullet() && steps < 500) {
            model.update();
            steps++;
        }

        boolean ok = !model.hasPlayerBullet();
        report("Bullet is removed when it reaches top", ok,
            "steps until removal=" + steps + ", still has bullet=" + model.hasPlayerBullet());
    }

    private static void testDestroyAlienIncreasesScore() {
        GameModel model = new GameModel();
        int initialScore = model.getScore();

        boolean destroyed = attemptDestroyOneAlien(model, 20);
        int finalScore = model.getScore();

        boolean ok = destroyed && finalScore > initialScore;
        report("Destroying an alien increases score", ok,
            "initial score=" + initialScore + ", final score=" + finalScore);
    }

    private static void testLosingAllLivesTriggersGameOver() {
        GameModel model = new GameModel();

        int safetySteps = 0;
        while (model.getLives() > 0 && safetySteps < 400000) {
            if (model.getAlienBulletCount() > 0) {
                int targetPlayerX = model.getAlienBulletX(0) - model.getPlayerWidth() / 2;
                movePlayerToX(model, targetPlayerX);
            }

            model.update();
            safetySteps++;
        }

        boolean ok = model.getLives() <= 0 && model.isGameOver();
        report("Losing all lives triggers game over", ok,
            "lives=" + model.getLives() + ", gameOver=" + model.isGameOver() + ", steps=" + safetySteps);
    }

    private static boolean attemptDestroyOneAlien(GameModel model, int maxAttempts) {
        final int bulletSpeed = 7;
        final int alienSpeed = 2;
        final int playerBulletOffsetX = model.getPlayerWidth() / 2 - 2;
        final int bulletStartY = model.getGameHeight() - model.getPlayerHeight() - 10;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int targetRow = model.getAlienRows() - 1;
            int targetCol = (attempt % model.getAlienCols());

            if (!model.isAlienAlive(targetRow, targetCol)) {
                continue;
            }

            int alienX = model.getAlienX(targetRow, targetCol);
            int alienY = model.getAlienY(targetRow, targetCol);
            int alienCenterY = alienY + model.getAlienHeight() / 2;
            int ticksToCenter = Math.max(1, (bulletStartY - alienCenterY) / bulletSpeed);

            int predictedAlienCenterX = alienX + alienSpeed * ticksToCenter + model.getAlienWidth() / 2;
            int targetPlayerX = predictedAlienCenterX - playerBulletOffsetX;
            movePlayerToX(model, targetPlayerX);

            if (!model.hasPlayerBullet()) {
                model.firePlayerBullet();
            }

            int startScore = model.getScore();
            int ticks = 0;
            while (ticks < 400 && model.hasPlayerBullet() && model.getScore() == startScore) {
                model.update();
                ticks++;
            }

            if (model.getScore() > startScore) {
                return true;
            }

            while (model.hasPlayerBullet() && ticks < 700) {
                model.update();
                ticks++;
            }
        }

        return false;
    }

    private static void movePlayerToX(GameModel model, int targetX) {
        int clampedTarget = Math.max(0, Math.min(targetX, model.getGameWidth() - model.getPlayerWidth()));
        int guard = 0;

        while (model.getPlayerX() < clampedTarget && guard < 5000) {
            model.movePlayerRight();
            guard++;
        }

        while (model.getPlayerX() > clampedTarget && guard < 10000) {
            model.movePlayerLeft();
            guard++;
        }
    }

    private static void report(String name, boolean ok, String details) {
        if (ok) {
            passed++;
            System.out.println("PASS: " + name + " (" + details + ")");
        } else {
            failed++;
            System.out.println("FAIL: " + name + " (" + details + ")");
        }
    }
}
