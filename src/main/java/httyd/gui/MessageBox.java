package httyd.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/**
 * The text box at the bottom of the battle screen. The newest message is typed out letter by
 * letter above a short history of the older ones.
 */
public class MessageBox extends JComponent {
    private static final int PADDING = 18;
    private static final int HISTORY_LINES = 2;
    private static final double MILLIS_PER_CHAR = 14.0;

    private final List<String> history = new ArrayList<>();
    private String text = "";
    private List<String> wrapped = List.of();
    private int wrappedWidth = -1;
    private int totalChars;
    private double revealed;
    private boolean isPrompt;

    /**
     * Shows a message that is added to the history once it is replaced by {@link #push}.
     *
     * @param message the message
     */
    public void push(String message) {
        if (!text.isBlank() && !isPrompt) {
            history.add(text);
            while (history.size() > HISTORY_LINES) {
                history.remove(0);
            }
        }
        isPrompt = false;
        setTextInternal(message, false);
    }

    /**
     * Shows a prompt or hint at once, without typing and without adding it to the history.
     *
     * @param message the text
     */
    public void showPrompt(String message) {
        isPrompt = true;
        setTextInternal(message, true);
    }

    private void setTextInternal(String message, boolean instant) {
        text = message;
        wrapped = List.of();
        wrappedWidth = -1;
        totalChars = message.length();
        revealed = instant ? totalChars : 0;
        repaint();
    }

    /** Clears the history (a new match starts). */
    public void clear() {
        history.clear();
        isPrompt = false;
        setTextInternal("", true);
    }

    /**
     * Types more letters.
     *
     * @param millis time that passed
     */
    public void advance(double millis) {
        if (revealed < totalChars) {
            revealed = Math.min(totalChars, revealed + millis / MILLIS_PER_CHAR);
            repaint();
        }
    }

    /** Shows the whole current message at once. */
    public void revealAll() {
        revealed = totalChars;
        repaint();
    }

    /** @return true when the whole current message is visible */
    public boolean isRevealed() {
        return revealed >= totalChars;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g2.setColor(Theme.INK);
        g2.fillRect(0, 0, w, h);
        g2.setColor(Theme.PAPER);
        g2.fillRect(5, 5, w - 10, h - 10);
        g2.setColor(Theme.mix(Theme.PAPER, Theme.INK, 0.35));
        g2.fillRect(5, 5, w - 10, 3);

        int width = w - PADDING * 2;
        int y = PADDING + 8;
        g2.setFont(Theme.font(14, false));
        g2.setColor(new Color(0x7B7360));
        FontMetrics small = g2.getFontMetrics();
        for (String old : history) {
            for (String line : GuiUtil.wrap(old, small, width)) {
                g2.drawString(line, PADDING, y);
                y += 19;
            }
        }
        g2.setFont(Theme.font(19, true));
        FontMetrics fm = g2.getFontMetrics();
        if (wrappedWidth != width) {
            wrapped = GuiUtil.wrap(text, fm, width);
            wrappedWidth = width;
        }
        g2.setColor(Theme.INK);
        y += 6;
        int remaining = (int) Math.floor(revealed);
        for (String line : wrapped) {
            if (remaining <= 0) {
                break;
            }
            int count = Math.min(line.length(), remaining);
            // do not cut in the middle of a combining mark
            while (count < line.length() && Character.getType(line.charAt(count)) == Character.NON_SPACING_MARK) {
                count++;
            }
            g2.drawString(line.substring(0, count), PADDING, y);
            remaining -= line.length();
            y += 27;
        }
        if (isRevealed() && !isPrompt && !text.isEmpty()) {
            g2.setColor(Theme.ACCENT);
            int bx = w - 34;
            int by = h - 26 + (int) (Math.sin(System.currentTimeMillis() / 150.0) * 3);
            g2.fillPolygon(new int[] {bx, bx + 14, bx + 7}, new int[] {by, by, by + 8}, 3);
        }
        g2.dispose();
    }
}
