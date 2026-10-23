package httyd.gui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MessageBoxTest {

    @Test
    void pushedTextIsTypedOutOverTime() {
        MessageBox box = new MessageBox();
        box.push("a message that needs some time to type");
        assertFalse(box.isRevealed());
        box.advance(10_000);
        assertTrue(box.isRevealed());
    }

    @Test
    void promptsAppearAtOnce() {
        MessageBox box = new MessageBox();
        box.showPrompt("choose");
        assertTrue(box.isRevealed());
    }

    @Test
    void revealAllShowsEverything() {
        MessageBox box = new MessageBox();
        box.push("another long message");
        box.revealAll();
        assertTrue(box.isRevealed());
    }

    @Test
    void emptyBoxCountsAsRevealed() {
        MessageBox box = new MessageBox();
        box.clear();
        assertTrue(box.isRevealed());
    }
}
