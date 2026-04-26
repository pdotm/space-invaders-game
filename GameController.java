import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class GameController {
    // This class will coordinate input, model updates, and view refresh timing.
    // It will also wire GameModel and GameView together and run the game loop.

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameModel model = new GameModel();
            GameView view = new GameView();

            JFrame frame = new JFrame("Space Invaders");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 600);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(view);
            frame.setVisible(true);
        });
    }
}
