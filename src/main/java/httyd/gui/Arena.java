package httyd.gui;

import httyd.battle.BattleEngine;
import httyd.battle.Combatant;
import httyd.battle.DotType;
import httyd.battle.Side;
import httyd.battle.StatModifier;
import httyd.model.field.BattlefieldType;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;

/**
 * The picture part of the battle screen: backdrop, the two dragons, their information boxes and
 * short animations (attack lunge, hit blink, dodge, faint, floating numbers).
 */
public class Arena extends JComponent {
    private static final double HP_BAR_SECONDS = 0.9;
    private static final int OPPONENT_SCALE = 5;
    private static final int PLAYER_SCALE = 7;
    private static final int MAX_SHOT_PIPS = 8;

    /** A number or word that floats up and fades. */
    private static final class Floater {
        final String text;
        final double x;
        double y;
        final Color color;
        final int size;
        double life = 1.0;

        Floater(String text, double x, double y, Color color, int size) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
            this.size = size;
        }
    }

    private BattleEngine engine;
    private BattlefieldType field = BattlefieldType.PLAINS;
    private final Combatant[] shown = new Combatant[2];
    private final double[] hpShown = new double[2];
    private final double[] hpTarget = new double[2];
    private final double[] lunge = new double[2];
    private final double[] blink = new double[2];
    private final double[] dodge = new double[2];
    private final double[] enter = new double[2];
    private final double[] faint = new double[2];
    private final boolean[] gone = new boolean[2];
    private final List<Floater> floaters = new ArrayList<>();
    private String clockText = "";
    private boolean clockLow;
    private String banner;
    private Color bannerColor = Theme.GOLD;
    private double bannerAge;

    /** Creates the arena. */
    public Arena() {
        setPreferredSize(new Dimension(1024, 470));
    }

    /**
     * Resets everything for a new match.
     *
     * @param engine the battle engine
     * @param field  the starting battlefield
     */
    public void reset(BattleEngine engine, BattlefieldType field) {
        this.engine = engine;
        this.field = field;
        for (int i = 0; i < 2; i++) {
            shown[i] = null;
            hpShown[i] = 0;
            hpTarget[i] = 0;
            lunge[i] = 0;
            blink[i] = 0;
            dodge[i] = 0;
            enter[i] = 0;
            faint[i] = 0;
            gone[i] = false;
        }
        floaters.clear();
        banner = null;
        clockText = "";
        clockLow = false;
    }

    /** @param field the battlefield to draw */
    public void setField(BattlefieldType field) {
        this.field = field;
    }

    /**
     * Shows the text of the clock in the corner.
     *
     * @param text the text
     * @param low  true to draw it in red
     */
    public void setClock(String text, boolean low) {
        this.clockText = text;
        this.clockLow = low;
    }

    /**
     * Shows a big banner such as "Victory!".
     *
     * @param text  the text
     * @param color the colour
     */
    public void setBanner(String text, Color color) {
        this.banner = text;
        this.bannerColor = color;
        this.bannerAge = 0;
    }

    /**
     * A new dragon enters the field.
     *
     * @param side the side
     * @param c    the dragon
     */
    public void sendOut(Side side, Combatant c) {
        int i = side.ordinal();
        shown[i] = c;
        hpShown[i] = c.getMaxHp();
        hpTarget[i] = c.getMaxHp();
        gone[i] = false;
        faint[i] = 0;
        enter[i] = 1;
    }

    /**
     * Starts the attack animation.
     *
     * @param side the attacker
     */
    public void lunge(Side side) {
        lunge[side.ordinal()] = 1;
    }

    /**
     * Changes the HP the bar moves toward.
     *
     * @param side  the side
     * @param delta negative for damage, positive for healing
     */
    public void changeHp(Side side, int delta) {
        int i = side.ordinal();
        if (shown[i] == null) {
            return;
        }
        hpTarget[i] = Math.max(0, Math.min(shown[i].getMaxHp(), hpTarget[i] + delta));
        if (delta < 0) {
            blink[i] = 1;
        }
    }

    /**
     * Starts the dodge animation.
     *
     * @param side the dodging side
     */
    public void dodge(Side side) {
        dodge[side.ordinal()] = 1;
    }

    /**
     * Starts the faint animation.
     *
     * @param side the fainting side
     */
    public void faint(Side side) {
        faint[side.ordinal()] = 0.0001;
        hpTarget[side.ordinal()] = 0;
    }

    /**
     * Makes the HP bars show the real values again after a series of events.
     */
    public void syncWithEngine() {
        if (engine == null) {
            return;
        }
        for (Side side : Side.values()) {
            int i = side.ordinal();
            if (shown[i] != null && shown[i] == engine.getActive(side)) {
                hpTarget[i] = shown[i].getCurrentHp();
            }
        }
    }

    /**
     * Adds a floating text above a dragon.
     *
     * @param side  the side
     * @param text  the text
     * @param color the colour
     * @param size  the font size
     */
    public void floatText(Side side, String text, Color color, int size) {
        double[] pos = spritePosition(side);
        floaters.add(new Floater(text, pos[0], pos[1] - 40, color, size));
    }

    /**
     * Moves all animations forward.
     *
     * @param millis time that passed
     */
    public void advance(double millis) {
        double s = millis / 1000.0;
        for (int i = 0; i < 2; i++) {
            lunge[i] = Math.max(0, lunge[i] - s / 0.45);
            blink[i] = Math.max(0, blink[i] - s / 0.55);
            dodge[i] = Math.max(0, dodge[i] - s / 0.55);
            enter[i] = Math.max(0, enter[i] - s / 0.5);
            if (faint[i] > 0 && !gone[i]) {
                faint[i] += s / 0.7;
                if (faint[i] >= 1) {
                    gone[i] = true;
                }
            }
            if (shown[i] != null) {
                double speed = shown[i].getMaxHp() / HP_BAR_SECONDS;
                if (hpShown[i] > hpTarget[i]) {
                    hpShown[i] = Math.max(hpTarget[i], hpShown[i] - speed * s);
                } else if (hpShown[i] < hpTarget[i]) {
                    hpShown[i] = Math.min(hpTarget[i], hpShown[i] + speed * s);
                }
            }
        }
        Iterator<Floater> it = floaters.iterator();
        while (it.hasNext()) {
            Floater f = it.next();
            f.life -= s / 1.1;
            f.y -= 38 * s;
            if (f.life <= 0) {
                it.remove();
            }
        }
        bannerAge += s;
        repaint();
    }

    /** @return centre-bottom point of a side's sprite as {x, baseY} */
    private double[] spritePosition(Side side) {
        return side == Side.PLAYER ? new double[] {250, 455} : new double[] {770, 262};
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        BackgroundFactory.draw(g2, field, w, h);

        // opponent first so the player's bigger dragon is drawn in front
        drawSprite(g2, Side.OPPONENT);
        drawSprite(g2, Side.PLAYER);
        if (engine != null) {
            drawInfoBox(g2, Side.OPPONENT, 20, 18, 340);
            drawInfoBox(g2, Side.PLAYER, w - 380, h - 136, 360);
            drawTopBar(g2, w);
        }
        for (Floater f : floaters) {
            g2.setFont(Theme.font(f.size, true));
            int tw = g2.getFontMetrics().stringWidth(f.text);
            int a = (int) Math.max(0, Math.min(255, f.life * 400));
            g2.setColor(new Color(0, 0, 0, a));
            g2.drawString(f.text, (int) f.x - tw / 2 + 2, (int) f.y + 2);
            g2.setColor(new Color(f.color.getRed(), f.color.getGreen(), f.color.getBlue(), a));
            g2.drawString(f.text, (int) f.x - tw / 2, (int) f.y);
        }
        if (banner != null) {
            double pop = Math.min(1.0, bannerAge / 0.35);
            int size = (int) (40 + 56 * pop);
            g2.setColor(new Color(0, 0, 0, (int) (150 * pop)));
            g2.fillRect(0, h / 2 - 80, w, 140);
            g2.setFont(Theme.font(size, true));
            int tw = g2.getFontMetrics().stringWidth(banner);
            g2.setColor(Theme.INK);
            g2.drawString(banner, (w - tw) / 2 + 5, h / 2 + 30 + 5);
            g2.setColor(bannerColor);
            g2.drawString(banner, (w - tw) / 2, h / 2 + 30);
        }
        g2.dispose();
    }

    private void drawSprite(Graphics2D g2, Side side) {
        int i = side.ordinal();
        Combatant c = shown[i];
        if (c == null || gone[i]) {
            return;
        }
        boolean isPlayer = side == Side.PLAYER;
        BufferedImage img = SpriteFactory.dragon(c.getSpecies(), isPlayer, isPlayer ? PLAYER_SCALE : OPPONENT_SCALE);
        double[] pos = spritePosition(side);
        double dir = isPlayer ? 1 : -1;
        double dx = 0;
        double dy = 0;
        if (lunge[i] > 0) {
            double t = Math.sin(Math.PI * (1 - lunge[i]));
            dx += dir * 70 * t;
            dy -= dir * 30 * t;
        }
        if (dodge[i] > 0) {
            dx -= dir * 70 * Math.sin(Math.PI * (1 - dodge[i]));
        }
        if (enter[i] > 0) {
            dx += -dir * 400 * enter[i] * enter[i];
        }
        if (blink[i] > 0) {
            dx += Math.sin(blink[i] * 40) * 6;
        }
        // soft shadow on the ground
        int shadowW = img.getWidth() * 3 / 4;
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillOval((int) (pos[0] - shadowW / 2.0), (int) pos[1] - 14, shadowW, 28);
        float alpha = 1f;
        if (faint[i] > 0) {
            alpha = (float) Math.max(0, 1 - faint[i]);
            dy += 50 * faint[i];
        }
        boolean hidden = blink[i] > 0 && ((int) (blink[i] * 14)) % 2 == 0;
        if (!hidden) {
            java.awt.Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.drawImage(img, (int) (pos[0] - img.getWidth() / 2.0 + dx), (int) (pos[1] - img.getHeight() + dy), null);
            g2.setComposite(old);
        }
    }

    private void drawTopBar(Graphics2D g2, int w) {
        g2.setFont(Theme.font(14, true));
        String fieldName = engine.getBattlefield().getCurrentField().getDisplayName();
        int overrideLeft = engine.getBattlefield().getOverrideTurnsLeft();
        String text = "Field: " + fieldName + (overrideLeft > 0 ? " (temporary " + overrideLeft + ")" : "");
        int cw = g2.getFontMetrics().stringWidth(text) + 20;
        GuiUtil.chip(g2, text, (w - cw) / 2, 8, 26, overrideLeft > 0 ? Theme.ACCENT : Theme.PAPER, overrideLeft > 0 ? Color.WHITE : Theme.INK);
        if (!clockText.isEmpty()) {
            g2.setFont(Theme.font(15, true));
            int tw = g2.getFontMetrics().stringWidth(clockText) + 14;
            GuiUtil.chip(g2, clockText, w - tw - 16, 12, 28, clockLow ? Theme.HP_LOW : Theme.PAPER,
                    clockLow ? Color.WHITE : Theme.INK);
        }
    }

    private void drawInfoBox(Graphics2D g2, Side side, int x, int y, int w) {
        int h = 118;
        Combatant c = shown[side.ordinal()];
        g2.setColor(Theme.INK);
        g2.fillRect(x, y, w, h);
        g2.setColor(Theme.PAPER);
        g2.fillRect(x + 4, y + 4, w - 8, h - 8);
        g2.setColor(new Color(c == null ? 0x999999 : c.getDragonClass().getColorRgb()));
        g2.fillRect(x + 4, y + 4, 8, h - 8);
        if (c == null) {
            return;
        }
        int left = x + 22;
        g2.setColor(Theme.INK);
        g2.setFont(Theme.font(18, true));
        g2.drawString(c.getName(), left, y + 28);
        int nameWidth = g2.getFontMetrics().stringWidth(c.getName());
        if (c.getSource().getStar() > 0) {
            GuiUtil.stars(g2, left + nameWidth + 8, y + 13, c.getSource().getStar(), 13, Theme.GOLD);
        }
        // remaining dragons of the side as little balls
        int alive = engine.remaining(side);
        for (int k = 0; k < 3; k++) {
            g2.setColor(Theme.INK);
            g2.fillOval(x + w - 22 - k * 18, y + 14, 14, 14);
            g2.setColor(k < alive ? Theme.ACCENT : new Color(0x9A9A9A));
            g2.fillOval(x + w - 20 - k * 18, y + 16, 10, 10);
        }
        // HP bar
        double maxHp = c.getMaxHp();
        double ratio = hpShown[side.ordinal()] / maxHp;
        g2.setFont(Theme.font(12, true));
        g2.setColor(Theme.INK);
        g2.drawString("HP", left, y + 49);
        GuiUtil.bar(g2, left + 26, y + 36, w - 50 - 26 - 70, 16, ratio, Theme.hpColor(ratio));
        String hpText = (int) Math.ceil(hpShown[side.ordinal()]) + "/" + c.getMaxHp();
        g2.setColor(Theme.INK);
        g2.drawString(hpText, x + w - 14 - g2.getFontMetrics().stringWidth(hpText) - 8, y + 49);
        // shots and ultimate charge
        g2.setFont(Theme.font(12, false));
        g2.drawString("Shots", left, y + 72);
        int px = left + 46;
        int shotsWidth;
        if (c.getMaxShots() <= MAX_SHOT_PIPS) {
            for (int k = 0; k < c.getMaxShots(); k++) {
                g2.setColor(Theme.INK);
                g2.fillRect(px + k * 13, y + 61, 11, 11);
                g2.setColor(k < c.getShots() ? Theme.GOLD : new Color(0xB9B49F));
                g2.fillRect(px + k * 13 + 2, y + 63, 7, 7);
            }
            shotsWidth = Math.max(4, c.getMaxShots()) * 13;
        } else {
            String shots = c.getShots() + "/" + c.getMaxShots();
            g2.setFont(Theme.font(13, true));
            g2.drawString(shots, px, y + 72);
            shotsWidth = g2.getFontMetrics().stringWidth(shots) + 6;
            g2.setFont(Theme.font(12, false));
        }
        int ux = px + shotsWidth + 22;
        g2.setColor(Theme.INK);
        g2.drawString("Ultimate", ux, y + 72);
        int cx = ux + g2.getFontMetrics().stringWidth("Ultimate") + 6;
        for (int k = 0; k < httyd.model.GameRules.ULTIMATE_CHARGE_NEEDED; k++) {
            g2.setColor(Theme.INK);
            g2.fillRect(cx + k * 15, y + 61, 13, 11);
            g2.setColor(k < c.getUltimateCharge() ? Theme.ACCENT : new Color(0xB9B49F));
            g2.fillRect(cx + k * 15 + 2, y + 63, 9, 7);
        }
        // status chips
        g2.setFont(Theme.font(11, true));
        int sx = left;
        int sy = y + 82;
        for (DotType type : DotType.values()) {
            if (c.hasDot(type)) {
                sx += GuiUtil.chip(g2, type.getDisplayName(), sx, sy, 20, type == DotType.BURN ? new Color(0xE9803A) : new Color(0x8B5CC4), Color.WHITE) + 4;
            }
        }
        for (StatModifier m : c.getModifiers()) {
            String label = shortStat(m) + " " + (m.isDebuff() ? "-" : "+")
                    + (m.getPercent() != 0 ? Math.round(Math.abs(m.getPercent()) * 100) + "%" : String.valueOf(Math.abs(m.getFlat())))
                    + (m.isPermanent() ? "" : " (" + m.getTurnsLeft() + ")");
            int cw = g2.getFontMetrics().stringWidth(label) + 14;
            if (sx + cw > x + w - 10) {
                break;
            }
            sx += GuiUtil.chip(g2, label, sx, sy, 20, m.isDebuff() ? new Color(0xD9534F) : new Color(0x4C9F5B), Color.WHITE) + 4;
        }
        if (c.getDodgeBonus() > 0) {
            String label = "Dodge +" + Math.round(c.getDodgeBonus()) + "%";
            int cw = g2.getFontMetrics().stringWidth(label) + 14;
            if (sx + cw <= x + w - 10) {
                GuiUtil.chip(g2, label, sx, sy, 20, Theme.ACCENT_2, Color.WHITE);
            }
        }
    }

    private static String shortStat(StatModifier m) {
        return switch (m.getStat()) {
            case HP -> "HP";
            case ATK -> "ATK";
            case DEF -> "DEF";
            case SPD -> "SPD";
        };
    }
}
