package httyd.model.player;

/** Difficulty levels of the bot. */
public enum Difficulty {
    EASY("Easy", 60),
    NORMAL("Normal", 100),
    HARD("Hard", 160);

    private final String displayName;
    private final int sWeightPercent;

    Difficulty(String displayName, int sWeightPercent) {
        this.displayName = displayName;
        this.sWeightPercent = sWeightPercent;
    }

    /** @return the display name */
    public String getDisplayName() {
        return displayName;
    }

    /** @return how strongly the bot's deck favours rare dragons, 100 = normal rates */
    public int getRarityPercent() {
        return sWeightPercent;
    }
}
