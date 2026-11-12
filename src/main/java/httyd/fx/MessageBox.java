package httyd.fx;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * The text box at the bottom of the battle screen. The newest message is typed out letter by letter
 * above a short history of the older ones.
 */
public class MessageBox extends Canvas {
    private static final double PADDING = 18;
    private static final int HISTORY_LINES = 2;
    private static final double MILLIS_PER_CHAR = 14.0;
    private static final Font HISTORY_FONT = Theme.font(14, false);
    private static final Font TEXT_FONT = Theme.font(19, true);

    private final List<String> history = new ArrayList<>();
    private List<String> historyLines = List.of();
    private String text = "";
    private List<String> wrapped = List.of();
    private int totalChars;
    private double revealed;
    private boolean isPrompt;

    /**
     * @param width  the width of the box
     * @param height the height of the box
     */
    public MessageBox(double width, double height) {
        super(width, height);
        redraw();
    }

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
            rewrapHistory();
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
        wrapped = Draw.wrap(message, s -> Draw.width(TEXT_FONT, s), getWidth() - PADDING * 2);
        totalChars = message.length();
        revealed = instant ? totalChars : 0;
        redraw();
    }

    private void rewrapHistory() {
        List<String> lines = new ArrayList<>();
        for (String old : history) {
            lines.addAll(Draw.wrap(old, s -> Draw.width(HISTORY_FONT, s), getWidth() - PADDING * 2));
        }
        historyLines = lines;
    }

    /** Clears the history (a new match starts). */
    public void clear() {
        history.clear();
        historyLines = List.of();
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
        }
    }

    /** Shows the whole current message at once. */
    public void revealAll() {
        revealed = totalChars;
    }

    /** @return true when the whole current message is visible */
    public boolean isRevealed() {
        return revealed >= totalChars;
    }

    /** Draws the box (called by the screen on every frame). */
    public void redraw() {
        GraphicsContext g = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();
        g.setFill(Theme.INK);
        g.fillRect(0, 0, w, h);
        g.setFill(Theme.PAPER);
        g.fillRect(5, 5, w - 10, h - 10);
        g.setFill(Theme.mix(Theme.PAPER, Theme.INK, 0.35));
        g.fillRect(5, 5, w - 10, 3);

        double y = PADDING + 8;
        g.setFont(HISTORY_FONT);
        g.setFill(Theme.rgb(0x7B7360));
        for (String line : historyLines) {
            g.fillText(line, PADDING, y);
            y += 19;
        }
        g.setFont(TEXT_FONT);
        g.setFill(Theme.INK);
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
            g.fillText(line.substring(0, count), PADDING, y);
            remaining -= line.length();
            y += 27;
        }
        if (isRevealed() && !isPrompt && !text.isEmpty()) {
            g.setFill(Theme.ACCENT);
            double bx = w - 34;
            double by = h - 26 + Math.sin(System.currentTimeMillis() / 150.0) * 3;
            g.fillPolygon(new double[] {bx, bx + 14, bx + 7}, new double[] {by, by, by + 8}, 3);
        }
    }
}
