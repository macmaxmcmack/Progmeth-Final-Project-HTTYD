package httyd.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Pop-up messages in the style of the game. The standard option panes are avoided because their
 * look different on every operating system.
 */
public final class Dialogs {
    private Dialogs() {
    }

    /**
     * Shows a message with one button.
     *
     * @param parent  a component of the window that owns the pop-up
     * @param title   the title
     * @param message the message
     */
    public static void info(Component parent, String title, String message) {
        show(parent, title, message, "OK", null);
    }

    /**
     * Asks a yes/no question.
     *
     * @param parent  a component of the window that owns the pop-up
     * @param title   the title
     * @param message the question
     * @param yes     the label of the yes button
     * @param no      the label of the no button
     * @return true if the player chose yes
     */
    public static boolean confirm(Component parent, String title, String message, String yes, String no) {
        return show(parent, title, message, yes, no);
    }

    private static boolean show(Component parent, String title, String message, String yes, String no) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        boolean[] result = {false};

        RetroPanel content = new RetroPanel(new BorderLayout(0, 12));
        content.setBorder(BorderFactory.createEmptyBorder(18, 24, 16, 24));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.font(22, true));
        titleLabel.setForeground(Theme.INK);
        content.add(titleLabel, BorderLayout.NORTH);

        JTextArea text = new JTextArea(message);
        text.setEditable(false);
        text.setOpaque(false);
        text.setFocusable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(Theme.font(17, false));
        text.setForeground(Theme.INK);
        text.setPreferredSize(new Dimension(420, Math.max(60, 26 * (message.length() / 28 + 2))));
        content.add(text, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        RetroButton yesButton = new RetroButton(yes, Theme.ACCENT);
        yesButton.setPreferredSize(new Dimension(140, 42));
        yesButton.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });
        if (no != null) {
            RetroButton noButton = new RetroButton(no, Theme.ACCENT_2);
            noButton.setPreferredSize(new Dimension(140, 42));
            noButton.addActionListener(e -> dialog.dispose());
            buttons.add(noButton);
        }
        buttons.add(yesButton);
        content.add(buttons, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }
}
