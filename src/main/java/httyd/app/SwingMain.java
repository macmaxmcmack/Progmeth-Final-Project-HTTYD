package httyd.app;

import httyd.gui.GameContext;
import httyd.gui.GameWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Legacy Swing entry point, kept until the JavaFX port is finished.
 */
public final class SwingMain {

    private SwingMain() {
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
