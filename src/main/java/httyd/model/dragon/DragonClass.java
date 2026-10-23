package httyd.model.dragon;

/**
 * The seven dragon classes from the "How to Train Your Dragon" universe.
 * Each class has a display name and a theme colour used by the GUI.
 */
public enum DragonClass {
    STRIKE("Strike", "Strike", 0xF2C94C),
    STOKER("Stoker", "Stoker", 0xE5533D),
    BOULDER("Boulder", "Boulder", 0x9C7A55),
    TRACKER("Tracker", "Tracker", 0x4FA64F),
    SHARP("Sharp", "Sharp", 0x9AA5B1),
    TIDAL("Tidal", "Tidal", 0x3B8FD9),
    MYSTERY("Mystery", "Mystery", 0x8E5BC8);

    private final String englishName;
    private final String displayName;
    private final int colorRgb;

    DragonClass(String englishName, String displayName, int colorRgb) {
        this.englishName = englishName;
        this.displayName = displayName;
        this.colorRgb = colorRgb;
    }

    /** @return the English class name, e.g. "Strike" */
    public String getEnglishName() {
        return englishName;
    }

    /** @return the class name used in the UI */
    public String getDisplayName() {
        return displayName;
    }

    /** @return the class theme colour as 0xRRGGBB */
    public int getColorRgb() {
        return colorRgb;
    }

    /**
     * Parses a class name ignoring case.
     *
     * @param text the class name such as "Strike"
     * @return the matching class
     * @throws IllegalArgumentException if no class matches
     */
    public static DragonClass parse(String text) {
        return valueOf(text.trim().toUpperCase());
    }
}
