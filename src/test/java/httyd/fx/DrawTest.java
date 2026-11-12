package httyd.fx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

/** Tests of the pure parts of the drawing helpers (no JavaFX toolkit is started). */
class DrawTest {
    /** Every character is 10 pixels wide, which makes the expected lines easy to work out. */
    private static double tenPerChar(String s) {
        return s.length() * 10.0;
    }

    @Test
    void shortTextStaysOnOneLine() {
        assertEquals(List.of("hello world"), Draw.wrap("hello world", DrawTest::tenPerChar, 1000));
    }

    @Test
    void longTextIsWrappedToTheWidth() {
        String text = "the quick brown fox jumps over the lazy dog and keeps running far away";
        List<String> lines = Draw.wrap(text, DrawTest::tenPerChar, 150);
        assertTrue(lines.size() > 1);
        for (String line : lines) {
            assertTrue(tenPerChar(line) <= 150, "line too wide: " + line);
        }
        assertEquals(text, String.join(" ", lines));
    }

    @Test
    void newlineStartsANewLine() {
        assertEquals(List.of("one", "two"), Draw.wrap("one\ntwo", DrawTest::tenPerChar, 1000));
    }

    @Test
    void emptyTextGivesOneEmptyLine() {
        assertEquals(List.of(""), Draw.wrap("", DrawTest::tenPerChar, 100));
    }

    @Test
    void hpColourChangesWithTheRatio() {
        assertEquals(Theme.HP_GOOD, Theme.hpColor(1.0));
        assertEquals(Theme.HP_MID, Theme.hpColor(0.4));
        assertEquals(Theme.HP_LOW, Theme.hpColor(0.1));
    }

    @Test
    void mixGivesTheEndsAndTheMiddle() {
        Color a = Color.rgb(0, 0, 0);
        Color b = Color.rgb(200, 100, 50);
        assertEquals(a, Theme.mix(a, b, 0));
        assertEquals(b, Theme.mix(a, b, 1));
        assertEquals(Color.rgb(100, 50, 25), Theme.mix(a, b, 0.5));
    }
}
