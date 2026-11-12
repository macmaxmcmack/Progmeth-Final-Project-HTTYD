package httyd.fx;

import httyd.model.dragon.Grade;
import httyd.model.dragon.OwnedDragon;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * A small card showing one owned dragon. It can be selected and can carry a number badge (the place in
 * the team).
 */
public class DragonTile extends Canvas {
    /** Width of a tile. */
    public static final int WIDTH = 160;
    /** Height of a tile. */
    public static final int HEIGHT = 142;

    private final OwnedDragon dragon;
    private boolean selected;
    private boolean hover;
    private boolean dimmed;
    private int badge;

    /**
     * @param dragon  the dragon to show
     * @param onClick called when the tile is clicked, may be {@code null}
     */
    public DragonTile(OwnedDragon dragon, Runnable onClick) {
        super(WIDTH, HEIGHT);
        this.dragon = dragon;
        setCursor(Cursor.HAND);
        setOnMouseEntered(e -> {
            hover = true;
            redraw();
        });
        setOnMouseExited(e -> {
            hover = false;
            redraw();
        });
        setOnMouseClicked(e -> {
            if (onClick != null) {
                onClick.run();
            }
        });
        redraw();
    }

    /** @return the dragon on this tile */
    public OwnedDragon getDragon() {
        return dragon;
    }

    /** @param selected true to draw the highlighted frame */
    public void setSelected(boolean selected) {
        this.selected = selected;
        redraw();
    }

    /** @param dimmed true to draw the tile faded (it cannot be chosen now) */
    public void setDimmed(boolean dimmed) {
        this.dimmed = dimmed;
        redraw();
    }

    /** @param badge the place number to show in the corner, 0 for none */
    public void setBadge(int badge) {
        this.badge = badge;
        redraw();
    }

    private void redraw() {
        GraphicsContext g = getGraphicsContext2D();
        g.setImageSmoothing(false);
        double w = WIDTH;
        double h = HEIGHT;
        Color classColor = Theme.rgb(dragon.getSpecies().getDragonClass().getColorRgb());
        g.clearRect(0, 0, w, h);
        g.setFill(selected ? Theme.GOLD : Theme.INK);
        g.fillRect(0, 0, w, h);
        g.setFill(hover ? Theme.mix(Theme.PAPER, Color.WHITE, 0.5) : Theme.PAPER);
        double inset = selected ? 5 : 3;
        g.fillRect(inset, inset, w - 2 * inset, h - 2 * inset);
        g.setFill(classColor);
        g.fillRect(3, 3, w - 6, 6);

        Image sprite = SpriteFactory.dragon(dragon.getSpecies(), true, 2);
        g.drawImage(sprite, Math.round((w - sprite.getWidth()) / 2), 12);

        g.setFill(Theme.INK);
        g.setFont(Theme.font(15, true));
        String name = dragon.getSpecies().getShortName();
        g.fillText(name, Math.max(6, (w - Draw.width(g.getFont(), name)) / 2), 105);

        g.setFont(Theme.font(12, false));
        String className = dragon.getSpecies().getDragonClass().getDisplayName();
        g.fillText(className, 8, 126);
        if (dragon.getStar() > 0) {
            Draw.stars(g, 8 + Draw.width(g.getFont(), className) + 6, 114, dragon.getStar(), 12, Theme.GOLD);
        }
        Grade grade = dragon.getGrade();
        g.setFont(Theme.font(12, true));
        Color gradeColor = grade == Grade.SSS ? Theme.GOLD : grade == Grade.A ? Theme.rgb(0x8FD694) : Theme.rgb(0xB8B8B8);
        double chipW = Draw.width(g.getFont(), grade.getLabel()) + 14;
        Draw.chip(g, grade.getLabel(), w - chipW - 6, 114, 18, gradeColor, Theme.INK);
        if (dragon.isAlpha()) {
            g.setFont(Theme.font(11, true));
            Draw.chip(g, "Alpha", w - 52, 14, 17, Theme.rgb(0xB34CD6), Color.WHITE);
        }
        if (badge > 0) {
            g.setFill(Theme.INK);
            g.fillOval(5, 12, 26, 26);
            g.setFill(Theme.ACCENT);
            g.fillOval(7, 14, 22, 22);
            g.setFill(Color.WHITE);
            g.setFont(Theme.font(16, true));
            Draw.centered(g, String.valueOf(badge), 18, 31);
        }
        if (dimmed) {
            g.setFill(Color.rgb(40, 44, 58, 150 / 255.0));
            g.fillRect(0, 0, w, h);
        }
    }
}
