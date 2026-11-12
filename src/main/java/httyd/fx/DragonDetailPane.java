package httyd.fx;

import httyd.data.DragonRepository;
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
        redraw();
    }

    /** @param hint the text shown when no dragon is selected */
    public void setHint(String hint) {
        this.hint = hint;
        redraw();
    }

    private void redraw() {
        GraphicsContext g = getGraphicsContext2D();
        g.setImageSmoothing(false);
        g.setFill(Theme.INK);
        g.fillRect(0, 0, W, H);
        g.setFill(Theme.PAPER);
        g.fillRect(4, 4, W - 8, H - 8);
        if (dragon == null) {
            g.setFill(Theme.INK);
            g.setFont(Theme.font(18, false));
            Draw.centered(g, hint, W / 2, H / 2);
            return;
        }
        DragonSpecies species = dragon.getSpecies();
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
        if (dragon.isAlpha()) {
            Draw.chip(g, "Alpha", cx, 46, 22, Theme.rgb(0xB34CD6), Color.WHITE);
        }
        g.setFont(Theme.font(14, false));
        g.setFill(Theme.INK);
        g.fillText("Grade " + dragon.getGrade().getLabel() + " (" + dragon.getGrade().getDisplayName() + ")", x, 90);
        g.fillText("Nature: " + dragon.getNature().getDisplayName() + "  " + dragon.getNature().getSummary(), x, 112);
        String starText = dragon.isTitanWing() ? "Titan Wing  (passive +50%)"
                : dragon.getStar() + (dragon.getStar() == 1 ? " star" : " stars");
        g.fillText(starText, x + (dragon.getStar() > 0 ? dragon.getStar() * 18 + 4 : 0), 134);
        Draw.stars(g, x, 121, dragon.getStar(), 16, Theme.GOLD);

        // stats
        double y = 176;
        for (Stat stat : Stat.values()) {
            g.setFill(Theme.INK);
            g.setFont(Theme.font(14, true));
            g.fillText(stat.getDisplayName(), 16, y + 14);
            int value = dragon.getStat(stat);
            double ratio = value / (maxBase[stat.ordinal()] * 1.35);
            Color barColor = switch (stat) {
                case HP -> Theme.HP_GOOD;
                case ATK -> Theme.ACCENT;
                case DEF -> Theme.ACCENT_2;
                case SPD -> Theme.GOLD;
            };
            Draw.bar(g, 130, y, 170, 18, ratio, barColor);
            g.setFill(Theme.INK);
            String valueText = stat == Stat.HP ? value + " (x3 = " + dragon.getBattleHp() + ")" : String.valueOf(value);
            g.fillText(valueText, 308, y + 14);
            int iv = dragon.getIv(stat);
            g.setFill(iv > 0 ? Theme.rgb(0x1E8A3A) : iv < 0 ? Theme.rgb(0xB5322A) : Theme.rgb(0x6C6C6C));
            g.setFont(Theme.font(12, true));
            g.fillText("IV " + (iv > 0 ? "+" : "") + iv, 16, y + 28);
            y += 36;
        }
        g.setFill(Theme.INK);
        g.setFont(Theme.font(13, false));
        g.fillText("Signature shots: " + dragon.getShotLimit() + "   Crit rate: "
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
