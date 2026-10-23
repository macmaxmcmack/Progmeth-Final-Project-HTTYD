package httyd.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GuiUtilTest {
    private FontMetrics metrics;

    @BeforeEach
    void setUp() {
        Graphics2D g = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB).createGraphics();
        g.setFont(Theme.font(16, false));
        metrics = g.getFontMetrics();
    }

    @Test
    void shortTextStaysOnOneLine() {
        assertEquals(List.of("hello world"), GuiUtil.wrap("hello world", metrics, 1000));
    }

    @Test
    void longEnglishTextIsWrappedToTheWidth() {
        String text = "the quick brown fox jumps over the lazy dog and keeps running far away";
        List<String> lines = GuiUtil.wrap(text, metrics, 150);
        assertTrue(lines.size() > 1);
        for (String line : lines) {
            assertTrue(metrics.stringWidth(line) <= 150, "line too wide: " + line);
        }
        assertEquals(text, String.join(" ", lines));
    }

    @Test
    void newlineStartsANewLine() {
        assertEquals(List.of("one", "two"), GuiUtil.wrap("one\ntwo", metrics, 1000));
    }

    @Test
    void emptyTextGivesOneEmptyLine() {
        assertEquals(List.of(""), GuiUtil.wrap("", metrics, 100));
    }

    @Test
    void hpColourChangesWithTheRatio() {
        assertEquals(Theme.HP_GOOD, Theme.hpColor(1.0));
        assertEquals(Theme.HP_MID, Theme.hpColor(0.4));
        assertEquals(Theme.HP_LOW, Theme.hpColor(0.1));
    }

    @Test
    void mixGivesTheEndsAndTheMiddle() {
        java.awt.Color a = new java.awt.Color(0, 0, 0);
        java.awt.Color b = new java.awt.Color(200, 100, 50);
        assertEquals(a, Theme.mix(a, b, 0));
        assertEquals(b, Theme.mix(a, b, 1));
        assertEquals(new java.awt.Color(100, 50, 25), Theme.mix(a, b, 0.5));
    }
}
