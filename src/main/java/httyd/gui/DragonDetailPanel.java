package httyd.gui;

import httyd.data.DragonRepository;
import httyd.model.dragon.DragonClass;
import httyd.model.dragon.DragonSpecies;
import httyd.model.dragon.OwnedDragon;
import httyd.model.dragon.Stat;
import httyd.model.skill.Move;
import httyd.model.skill.MoveKind;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * The full information card of one dragon: picture, class, grade, nature, stats with their IV
 * bonus, passive skill and the three moves.
 */
public class DragonDetailPanel extends JComponent {
    private final int[] maxBase = new int[Stat.values().length];
    private OwnedDragon dragon;
    private String hint = "Select a dragon to see its details";

    /**
     * @param repository used to scale the stat bars against the strongest species
     */
    public DragonDetailPanel(DragonRepository repository) {
        for (DragonSpecies species : repository.all()) {
            for (Stat stat : Stat.values()) {
                maxBase[stat.ordinal()] = Math.max(maxBase[stat.ordinal()], species.getBase(stat));
            }
        }
        setPreferredSize(new Dimension(430, 560));
    }

    /** @param dragon the dragon to show, or {@code null} to show the hint text */
    public void setDragon(OwnedDragon dragon) {
        this.dragon = dragon;
        repaint();
    }

    /** @param hint the text shown when no dragon is selected */
    public void setHint(String hint) {
        this.hint = hint;
        repaint();
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
        g2.fillRect(4, 4, w - 8, h - 8);

        if (dragon == null) {
            g2.setColor(Theme.INK);
            g2.setFont(Theme.font(18, false));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(hint, (w - fm.stringWidth(hint)) / 2, h / 2);
            g2.dispose();
            return;
        }
        DragonSpecies species = dragon.getSpecies();
        DragonClass dragonClass = species.getDragonClass();
        Color classColor = new Color(dragonClass.getColorRgb());

        // header band with the sprite
        g2.setColor(Theme.mix(classColor, Theme.PAPER, 0.55));
        g2.fillRect(4, 4, w - 8, 150);
        BufferedImage sprite = SpriteFactory.dragon(species, true, 3);
        g2.drawImage(sprite, 14, 156 - sprite.getHeight(), null);

        int x = 170;
        g2.setColor(Theme.INK);
        g2.setFont(Theme.font(22, true));
        g2.drawString(species.getName(), x, 36);
        g2.setFont(Theme.font(14, true));
        int cx = x;
        cx += GuiUtil.chip(g2, dragonClass.getDisplayName(), cx, 46, 22, classColor, Color.WHITE) + 6;
        cx += GuiUtil.chip(g2, "Tier " + species.getTier().getLabel(), cx, 46, 22, Theme.PAPER_DARK, Theme.INK) + 6;
        if (dragon.isAlpha()) {
            GuiUtil.chip(g2, "Alpha", cx, 46, 22, new Color(0xB34CD6), Color.WHITE);
        }
        g2.setFont(Theme.font(14, false));
        g2.setColor(Theme.INK);
        g2.drawString("Grade " + dragon.getGrade().getLabel() + " (" + dragon.getGrade().getDisplayName() + ")", x, 90);
        g2.drawString("Nature: " + dragon.getNature().getDisplayName() + "  " + dragon.getNature().getSummary(), x, 112);
        String starText = dragon.isTitanWing() ? "Titan Wing  (passive +50%)" : dragon.getStar() + (dragon.getStar() == 1 ? " star" : " stars");
        g2.drawString(starText, x + (dragon.getStar() > 0 ? dragon.getStar() * 18 + 4 : 0), 134);
        GuiUtil.stars(g2, x, 121, dragon.getStar(), 16, Theme.GOLD);

        // stats
        int y = 176;
        g2.setFont(Theme.font(14, true));
        for (Stat stat : Stat.values()) {
            g2.setColor(Theme.INK);
            g2.drawString(stat.getDisplayName(), 16, y + 14);
            int value = dragon.getStat(stat);
            double ratio = value / (maxBase[stat.ordinal()] * 1.35);
            Color barColor = switch (stat) {
                case HP -> Theme.HP_GOOD;
                case ATK -> Theme.ACCENT;
                case DEF -> Theme.ACCENT_2;
                case SPD -> Theme.GOLD;
            };
            GuiUtil.bar(g2, 130, y, 170, 18, ratio, barColor);
            g2.setColor(Theme.INK);
            String valueText = stat == Stat.HP ? value + " (x3 = " + dragon.getBattleHp() + ")" : String.valueOf(value);
            g2.drawString(valueText, 308, y + 14);
            int iv = dragon.getIv(stat);
            g2.setColor(iv > 0 ? new Color(0x1E8A3A) : iv < 0 ? new Color(0xB5322A) : new Color(0x6C6C6C));
            g2.setFont(Theme.font(12, true));
            g2.drawString("IV " + (iv > 0 ? "+" : "") + iv, 16, y + 28);
            g2.setFont(Theme.font(14, true));
            y += 36;
        }
        g2.setColor(Theme.INK);
        g2.setFont(Theme.font(13, false));
        g2.drawString("Signature shots: " + dragon.getShotLimit() + "   Crit rate: "
                + Math.round(species.getCritRate() * 100) + "%", 16, y + 6);
        y += 18;

        // passive
        g2.setColor(Theme.mix(Theme.PAPER, Theme.INK, 0.12));
        g2.fillRect(10, y, w - 20, 68);
        g2.setColor(Theme.INK);
        g2.setFont(Theme.font(14, true));
        g2.drawString("Passive: " + species.getPassiveName(), 16, y + 20);
        g2.setFont(Theme.font(13, false));
        GuiUtil.drawWrapped(g2, species.getPassiveDescription(), 16, y + 40, w - 36, 18);
        y += 76;

        // moves
        for (MoveKind kind : MoveKind.values()) {
            Move move = species.getMove(kind);
            g2.setFont(Theme.font(13, true));
            g2.setColor(kind == MoveKind.ULTIMATE ? Theme.ACCENT : kind == MoveKind.SIGNATURE ? Theme.ACCENT_2 : new Color(0x555555));
            g2.drawString(kind.getDisplayName() + ": " + move.getName(), 16, y + 14);
            g2.setColor(Theme.INK);
            g2.setFont(Theme.font(12, false));
            y = GuiUtil.drawWrapped(g2, move.getDescription(), 16, y + 31, w - 36, 16) - 6;
        }
        g2.dispose();
    }
}
