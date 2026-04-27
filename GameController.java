import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GameController {
    // This class coordinates input, model updates, and view refresh timing.
    // It wires GameModel and GameView together and drives the game loop via a Swing Timer.

    private static final int TICK_MS = 16;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Mutable references so they can be captured by lambdas and refreshed on restart
            GameModel[] modelRef  = { new GameModel() };
            int[]       highScore = { 0 };

            GameView view = new GameView(modelRef[0]);

            // Track held movement keys
            boolean[] leftHeld  = { false };
            boolean[] rightHeld = { false };

            // Timer is created now; startGame restarts it
            Timer[] timerRef = { null };
            Timer timer = new Timer(TICK_MS, null);
            timerRef[0] = timer;

            // Start (or restart) a game: fresh model, reset held keys, show game, fire timer
            Runnable startGame = () -> {
                modelRef[0] = new GameModel();
                view.setModel(modelRef[0]);
                leftHeld[0]  = false;
                rightHeld[0] = false;
                view.showGame();
                timerRef[0].restart();
            };

            // Game loop
            timer.addActionListener((ActionEvent e) -> {
                GameModel model = modelRef[0];

                if (model.isGameOver()) {
                    timer.stop();
                    int  finalScore = model.getScore();
                    boolean isNew   = finalScore > highScore[0];
                    if (isNew) highScore[0] = finalScore;
                    view.showMenu(highScore[0], isNew);
                    view.repaint();
                    return;
                }

                if (leftHeld[0])  model.movePlayerLeft();
                if (rightHeld[0]) model.movePlayerRight();

                model.update();
                view.repaint();
            });

            // Keyboard: Enter or Space starts game from menu; arrows and space control during game
            view.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (view.isMenuActive()) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER ||
                            e.getKeyCode() == KeyEvent.VK_SPACE) {
                            startGame.run();
                        }
                        return;
                    }
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT  -> leftHeld[0]  = true;
                        case KeyEvent.VK_RIGHT -> rightHeld[0] = true;
                        case KeyEvent.VK_SPACE -> modelRef[0].firePlayerBullet();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT  -> leftHeld[0]  = false;
                        case KeyEvent.VK_RIGHT -> rightHeld[0] = false;
                    }
                }
            });

            // Mouse: clicking the START button starts the game
            view.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (view.isMenuActive() && view.isStartButtonAt(e.getX(), e.getY())) {
                        startGame.run();
                    }
                }
            });

            JFrame frame = new JFrame("Space Invaders");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(view);
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            view.requestFocusInWindow();
            view.showMenu(0, false);
        });
    }
}
