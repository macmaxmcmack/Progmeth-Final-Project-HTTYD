package httyd.fx;

import httyd.data.DragonRepository;
import httyd.model.GameRules;
import httyd.model.dragon.DragonClass;
import httyd.model.dragon.DragonSpecies;
import httyd.model.dragon.OwnedDragon;
import httyd.model.dragon.Stat;
import httyd.model.skill.Move;
import httyd.model.skill.MoveKind;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * The full information card of one dragon: picture, class, grade, nature, stats with their IV bonus,
 * passive skill and the three moves.
 */
public class DragonDetailPane extends Canvas {
    private static final double W = 430;
    private static final double H = 560;

    private final int[] maxBase = new int[Stat.values().length];
    private OwnedDragon dragon;
    private DragonSpecies species;
    private double dropChance;
    private int ownedCount;
    private String hint = "Select a dragon to see its details";

    /**
     * @param repository used to scale the stat bars against the strongest species
     */
    public DragonDetailPane(DragonRepository repository) {
        super(W, H);
        for (DragonSpecies species : repository.all()) {
            for (Stat stat : Stat.values()) {
                maxBase[stat.ordinal()] = Math.max(maxBase[stat.ordinal()], species.getBase(stat));
            }
        }
        redraw();
    }

    /** @param dragon the dragon to show, or {@code null} to show the hint text */
    public void setDragon(OwnedDragon dragon) {
        this.dragon = dragon;
        this.species = null;
        redraw();
    }

    /**
     * Shows a species instead of one owned dragon: base stats, moves and how often an egg gives it.
     *
     * @param species    the species, or {@code null} to show the hint text
     * @param dropChance the chance of this species in one egg, as a fraction
     * @param ownedCount how many of this species the player owns
     */
    public void setSpecies(DragonSpecies species, double dropChance, int ownedCount) {
        this.dragon = null;
        this.species = species;
        this.dropChance = dropChance;
        this.ownedCount = ownedCount;
        redraw();
    }

    /** @param hint the text shown when no dragon is selected */
    public void setHint(String hint) {
        this.hint = hint;
        redraw();
    }

    /**
     * Formats a chance for display.
     *
     * @param fraction the chance from 0 to 1
     * @return text such as "2.5%"
     */
    static String formatPercent(double fraction) {
        double percent = fraction * 100;
        return (percent == Math.rint(percent) ? String.valueOf((long) percent) : String.format(java.util.Locale.US, "%.2f", percent)) + "%";
    }

    private void redraw() {
        GraphicsContext g = getGraphicsContext2D();
        g.setImageSmoothing(false);
        g.setFill(Theme.INK);
        g.fillRect(0, 0, W, H);
        g.setFill(Theme.PAPER);
        g.fillRect(4, 4, W - 8, H - 8);
        if (dragon == null && species == null) {
            g.setFill(Theme.INK);
            g.setFont(Theme.font(18, false));
            Draw.centered(g, hint, W / 2, H / 2);
            return;
        }
        boolean owned = dragon != null;
        DragonSpecies species = owned ? dragon.getSpecies() : this.species;
        DragonClass dragonClass = species.getDragonClass();
        Color classColor = Theme.rgb(dragonClass.getColorRgb());

        // header band with the sprite
        g.setFill(Theme.mix(classColor, Theme.PAPER, 0.55));
        g.fillRect(4, 4, W - 8, 150);
        Image sprite = SpriteFactory.dragon(species, true, 3);
        g.drawImage(sprite, 14, 156 - sprite.getHeight());

        double x = 170;
        g.setFill(Theme.INK);
        g.setFont(Theme.font(22, true));
        g.fillText(species.getName(), x, 36);
        g.setFont(Theme.font(14, true));
        double cx = x;
        cx += Draw.chip(g, dragonClass.getDisplayName(), cx, 46, 22, classColor, Color.WHITE) + 6;
        cx += Draw.chip(g, "Tier " + species.getTier().getLabel(), cx, 46, 22, Theme.PAPER_DARK, Theme.INK) + 6;
        if (owned && dragon.isAlpha()) {
            Draw.chip(g, "Alpha", cx, 46, 22, Theme.rgb(0xB34CD6), Color.WHITE);
        }
        g.setFont(Theme.font(14, false));
        g.setFill(Theme.INK);
        if (!owned) {
            g.fillText("Chance in an egg: " + formatPercent(dropChance), x, 90);
            g.fillText(ownedCount == 0 ? "You do not own this dragon yet" : "You own " + ownedCount, x, 112);
            g.fillText("Base stats (before IV and Nature)", x, 134);
        } else {
            g.fillText("Grade " + dragon.getGrade().getLabel() + " (" + dragon.getGrade().getDisplayName() + ")", x, 90);
            g.fillText("Nature: " + dragon.getNature().getDisplayName() + "  " + dragon.getNature().getSummary(), x, 112);
            String starText = dragon.isTitanWing() ? "Titan Wing  (passive +50%)"
                    : dragon.getStar() + (dragon.getStar() == 1 ? " star" : " stars");
            g.fillText(starText, x + (dragon.getStar() > 0 ? dragon.getStar() * 18 + 4 : 0), 134);
            Draw.stars(g, x, 121, dragon.getStar(), 16, Theme.GOLD);
        }

        // stats
        double y = 176;
        for (Stat stat : Stat.values()) {
            g.setFill(Theme.INK);
            g.setFont(Theme.font(14, true));
            g.fillText(stat.getDisplayName(), 16, y + 14);
            int value = owned ? dragon.getStat(stat) : species.getBase(stat);
            double ratio = value / (maxBase[stat.ordinal()] * (owned ? 1.35 : 1.0));
            Color barColor = switch (stat) {
                case HP -> Theme.HP_GOOD;
                case ATK -> Theme.ACCENT;
                case DEF -> Theme.ACCENT_2;
                case SPD -> Theme.GOLD;
            };
            Draw.bar(g, 130, y, 170, 18, ratio, barColor);
            g.setFill(Theme.INK);
            int battleHp = owned ? dragon.getBattleHp() : value * GameRules.HP_SCALE;
            String valueText = stat == Stat.HP ? value + " (x3 = " + battleHp + ")" : String.valueOf(value);
            g.fillText(valueText, 308, y + 14);
            if (owned) {
                int iv = dragon.getIv(stat);
                g.setFill(iv > 0 ? Theme.rgb(0x1E8A3A) : iv < 0 ? Theme.rgb(0xB5322A) : Theme.rgb(0x6C6C6C));
                g.setFont(Theme.font(12, true));
                g.fillText("IV " + (iv > 0 ? "+" : "") + iv, 16, y + 28);
            }
            y += 36;
        }
        g.setFill(Theme.INK);
        g.setFont(Theme.font(13, false));
        g.fillText("Signature shots: " + (owned ? dragon.getShotLimit() : species.getShotLimit()) + "   Crit rate: "
                + Math.round(species.getCritRate() * 100) + "%", 16, y + 6);
        y += 18;

        // passive
        g.setFill(Theme.mix(Theme.PAPER, Theme.INK, 0.12));
        g.fillRect(10, y, W - 20, 68);
        g.setFill(Theme.INK);
        g.setFont(Theme.font(14, true));
        g.fillText("Passive: " + species.getPassiveName(), 16, y + 20);
        g.setFont(Theme.font(13, false));
        Draw.wrapped(g, species.getPassiveDescription(), 16, y + 40, W - 36, 18);
        y += 76;

        // moves
        for (MoveKind kind : MoveKind.values()) {
            Move move = species.getMove(kind);
            g.setFont(Theme.font(13, true));
            g.setFill(kind == MoveKind.ULTIMATE ? Theme.ACCENT
                    : kind == MoveKind.SIGNATURE ? Theme.ACCENT_2 : Theme.rgb(0x555555));
            g.fillText(kind.getDisplayName() + ": " + move.getName(), 16, y + 14);
            g.setFill(Theme.INK);
            g.setFont(Theme.font(12, false));
            y = Draw.wrapped(g, move.getDescription(), 16, y + 31, W - 36, 16) - 6;
        }
    }
}
