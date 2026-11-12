package httyd.fx;

import httyd.battle.BattleEngine;
import httyd.battle.Combatant;
import httyd.battle.DotType;
import httyd.battle.Side;
import httyd.battle.StatModifier;
import httyd.model.GameRules;
import httyd.model.field.BattlefieldType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * The picture part of the battle screen: backdrop, the two dragons, their information boxes and short
 * animations (attack lunge, hit blink, dodge, faint, floating numbers).
 */
public class Arena extends Canvas {
    /** Width of the arena. */
    public static final double WIDTH = 1024;
    /** Height of the arena. */
    public static final double HEIGHT = 470;
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
        super(WIDTH, HEIGHT);
        redraw();
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

    /** Makes the HP bars show the real values again after a series of events. */
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
     * Moves all animations forward and draws the new picture.
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
        redraw();
    }

    /** @return centre-bottom point of a side's sprite as {x, baseY} */
    private double[] spritePosition(Side side) {
        return side == Side.PLAYER ? new double[] {250, 455} : new double[] {770, 262};
    }

    private void redraw() {
        GraphicsContext g = getGraphicsContext2D();
        g.setImageSmoothing(false);
        double w = WIDTH;
        double h = HEIGHT;
        g.drawImage(BackgroundFactory.backdrop(field), 0, 0, w, h);

        // opponent first so the player's bigger dragon is drawn in front
        drawSprite(g, Side.OPPONENT);
        drawSprite(g, Side.PLAYER);
        if (engine != null) {
            drawInfoBox(g, Side.OPPONENT, 20, 18, 340);
            drawInfoBox(g, Side.PLAYER, w - 380, h - 136, 360);
            drawTopBar(g, w);
        }
        for (Floater f : floaters) {
            g.setFont(Theme.font(f.size, true));
            double tw = Draw.width(g.getFont(), f.text);
            double a = Math.max(0, Math.min(1, f.life * 400 / 255.0));
            g.setFill(Color.color(0, 0, 0, a));
            g.fillText(f.text, Math.floor(f.x - tw / 2 + 2), Math.floor(f.y + 2));
            g.setFill(Color.color(f.color.getRed(), f.color.getGreen(), f.color.getBlue(), a));
            g.fillText(f.text, Math.floor(f.x - tw / 2), Math.floor(f.y));
        }
        if (banner != null) {
            double pop = Math.min(1.0, bannerAge / 0.35);
            double size = Math.floor(40 + 56 * pop);
            g.setFill(Color.color(0, 0, 0, 150 * pop / 255.0));
            g.fillRect(0, h / 2 - 80, w, 140);
            g.setFont(Theme.font(size, true));
            double tw = Draw.width(g.getFont(), banner);
            g.setFill(Theme.INK);
            g.fillText(banner, (w - tw) / 2 + 5, h / 2 + 30 + 5);
            g.setFill(bannerColor);
            g.fillText(banner, (w - tw) / 2, h / 2 + 30);
        }
    }

    private void drawSprite(GraphicsContext g, Side side) {
        int i = side.ordinal();
        Combatant c = shown[i];
        if (c == null || gone[i]) {
            return;
        }
        boolean isPlayer = side == Side.PLAYER;
        Image img = SpriteFactory.dragon(c.getSpecies(), isPlayer, isPlayer ? PLAYER_SCALE : OPPONENT_SCALE);
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
        double shadowW = Math.floor(img.getWidth() * 3 / 4);
        g.setFill(Color.color(0, 0, 0, 70 / 255.0));
        g.fillOval(Math.floor(pos[0] - shadowW / 2.0), Math.floor(pos[1] - 14), shadowW, 28);
        double alpha = 1;
        if (faint[i] > 0) {
            alpha = Math.max(0, 1 - faint[i]);
            dy += 50 * faint[i];
        }
        boolean hidden = blink[i] > 0 && ((int) (blink[i] * 14)) % 2 == 0;
        if (!hidden) {
            g.setGlobalAlpha(alpha);
            g.drawImage(img, Math.floor(pos[0] - img.getWidth() / 2.0 + dx), Math.floor(pos[1] - img.getHeight() + dy));
            g.setGlobalAlpha(1);
        }
    }

    private void drawTopBar(GraphicsContext g, double w) {
        g.setFont(Theme.font(14, true));
        String fieldName = engine.getBattlefield().getCurrentField().getDisplayName();
        int overrideLeft = engine.getBattlefield().getOverrideTurnsLeft();
        String text = "Field: " + fieldName + (overrideLeft > 0 ? " (temporary " + overrideLeft + ")" : "");
        double cw = Draw.width(g.getFont(), text) + 20;
        Draw.chip(g, text, Math.floor((w - cw) / 2), 8, 26, overrideLeft > 0 ? Theme.ACCENT : Theme.PAPER,
                overrideLeft > 0 ? Color.WHITE : Theme.INK);
        if (!clockText.isEmpty()) {
            g.setFont(Theme.font(15, true));
            double tw = Draw.width(g.getFont(), clockText) + 14;
            Draw.chip(g, clockText, w - tw - 16, 12, 28, clockLow ? Theme.HP_LOW : Theme.PAPER,
                    clockLow ? Color.WHITE : Theme.INK);
        }
    }

    private void drawInfoBox(GraphicsContext g, Side side, double x, double y, double w) {
        double h = 118;
        Combatant c = shown[side.ordinal()];
        g.setFill(Theme.INK);
        g.fillRect(x, y, w, h);
        g.setFill(Theme.PAPER);
        g.fillRect(x + 4, y + 4, w - 8, h - 8);
        g.setFill(c == null ? Theme.rgb(0x999999) : Theme.rgb(c.getDragonClass().getColorRgb()));
        g.fillRect(x + 4, y + 4, 8, h - 8);
        if (c == null) {
            return;
        }
        double left = x + 22;
        g.setFill(Theme.INK);
        g.setFont(Theme.font(18, true));
        g.fillText(c.getName(), left, y + 28);
        double nameWidth = Draw.width(g.getFont(), c.getName());
        if (c.getSource().getStar() > 0) {
            Draw.stars(g, left + nameWidth + 8, y + 13, c.getSource().getStar(), 13, Theme.GOLD);
        }
        // remaining dragons of the side as little balls
        int alive = engine.remaining(side);
        for (int k = 0; k < 3; k++) {
            g.setFill(Theme.INK);
            g.fillOval(x + w - 22 - k * 18, y + 14, 14, 14);
            g.setFill(k < alive ? Theme.ACCENT : Theme.rgb(0x9A9A9A));
            g.fillOval(x + w - 20 - k * 18, y + 16, 10, 10);
        }
        // HP bar
        double maxHp = c.getMaxHp();
        double ratio = hpShown[side.ordinal()] / maxHp;
        g.setFont(Theme.font(12, true));
        g.setFill(Theme.INK);
        g.fillText("HP", left, y + 49);
        Draw.bar(g, left + 26, y + 36, w - 50 - 26 - 70, 16, ratio, Theme.hpColor(ratio));
        String hpText = (int) Math.ceil(hpShown[side.ordinal()]) + "/" + c.getMaxHp();
        g.setFill(Theme.INK);
        g.fillText(hpText, x + w - 14 - Draw.width(g.getFont(), hpText) - 8, y + 49);
        // shots and ultimate charge
        g.setFont(Theme.font(12, false));
        g.fillText("Shots", left, y + 72);
        double px = left + 46;
        double shotsWidth;
        if (c.getMaxShots() <= MAX_SHOT_PIPS) {
            for (int k = 0; k < c.getMaxShots(); k++) {
                g.setFill(Theme.INK);
                g.fillRect(px + k * 13, y + 61, 11, 11);
                g.setFill(k < c.getShots() ? Theme.GOLD : Theme.rgb(0xB9B49F));
                g.fillRect(px + k * 13 + 2, y + 63, 7, 7);
            }
            shotsWidth = Math.max(4, c.getMaxShots()) * 13;
        } else {
            String shots = c.getShots() + "/" + c.getMaxShots();
            g.setFont(Theme.font(13, true));
            g.setFill(Theme.INK);
            g.fillText(shots, px, y + 72);
            shotsWidth = Draw.width(g.getFont(), shots) + 6;
            g.setFont(Theme.font(12, false));
        }
        double ux = px + shotsWidth + 22;
        g.setFill(Theme.INK);
        g.fillText("Ultimate", ux, y + 72);
        double cx = ux + Draw.width(g.getFont(), "Ultimate") + 6;
        for (int k = 0; k < GameRules.ULTIMATE_CHARGE_NEEDED; k++) {
            g.setFill(Theme.INK);
            g.fillRect(cx + k * 15, y + 61, 13, 11);
            g.setFill(k < c.getUltimateCharge() ? Theme.ACCENT : Theme.rgb(0xB9B49F));
            g.fillRect(cx + k * 15 + 2, y + 63, 9, 7);
        }
        // status chips
        g.setFont(Theme.font(11, true));
        double sx = left;
        double sy = y + 82;
        for (DotType type : DotType.values()) {
            if (c.hasDot(type)) {
                sx += Draw.chip(g, type.getDisplayName(), sx, sy, 20,
                        type == DotType.BURN ? Theme.rgb(0xE9803A) : Theme.rgb(0x8B5CC4), Color.WHITE) + 4;
            }
        }
        for (StatModifier m : c.getModifiers()) {
            String label = shortStat(m) + " " + (m.isDebuff() ? "-" : "+")
                    + (m.getPercent() != 0 ? Math.round(Math.abs(m.getPercent()) * 100) + "%" : String.valueOf(Math.abs(m.getFlat())))
                    + (m.isPermanent() ? "" : " (" + m.getTurnsLeft() + ")");
            double cw = Draw.width(g.getFont(), label) + 14;
            if (sx + cw > x + w - 10) {
                break;
            }
            sx += Draw.chip(g, label, sx, sy, 20, m.isDebuff() ? Theme.rgb(0xD9534F) : Theme.rgb(0x4C9F5B), Color.WHITE) + 4;
        }
        if (c.getDodgeBonus() > 0) {
            String label = "Dodge +" + Math.round(c.getDodgeBonus()) + "%";
            double cw = Draw.width(g.getFont(), label) + 14;
            if (sx + cw <= x + w - 10) {
                Draw.chip(g, label, sx, sy, 20, Theme.ACCENT_2, Color.WHITE);
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
