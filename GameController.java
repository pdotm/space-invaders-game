import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GameController {
    // This class coordinates input, model updates, and view refresh timing.
    // It wires GameModel and GameView together and drives the game loop via a Swing Timer.

    private static final int TICK_MS = 16;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameModel model = new GameModel();
            GameView view = new GameView(model);

            // Track which keys are currently held so movement is smooth across ticks
            boolean[] leftHeld = {false};
            boolean[] rightHeld = {false};

            // Game loop: update model each tick, repaint, stop when game over
            Timer timer = new Timer(TICK_MS, null);
            timer.addActionListener((ActionEvent e) -> {
                if (model.isGameOver()) {
                    timer.stop();
                    view.repaint();
                    return;
                }

                if (leftHeld[0])  model.movePlayerLeft();
                if (rightHeld[0]) model.movePlayerRight();

                model.update();
                view.repaint();
            });

            // Keyboard input: track held movement keys, fire on press
            view.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT  -> leftHeld[0]  = true;
                        case KeyEvent.VK_RIGHT -> rightHeld[0] = true;
                        case KeyEvent.VK_SPACE -> model.firePlayerBullet();
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

            JFrame frame = new JFrame("Space Invaders");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(view);
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            view.requestFocusInWindow();
            timer.start();
        });
    }
}
