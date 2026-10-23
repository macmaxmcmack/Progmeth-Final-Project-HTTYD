package httyd.app;

import httyd.gui.GameContext;
import httyd.gui.GameWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point of the game "HTTYD Dragon Battle".
 */
public final class Main {

    private Main() {
    }

    /**
     * Starts the game window.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // the default look is fine too
        }
        SwingUtilities.invokeLater(() -> {
            GameContext context = GameContext.createDefault();
            GameWindow window = new GameWindow(context);
            window.setVisible(true);
        });
    }
}
