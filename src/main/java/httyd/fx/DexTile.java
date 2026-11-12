package httyd.fx;

import httyd.model.dragon.DragonSpecies;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * A card in the dragon index: the picture of one species with its class, tier, the chance of
 * getting it from an egg and whether the player owns it.
 */
public class DexTile extends Canvas {
    private final DragonSpecies species;
    private final double dropChance;
    private final int ownedCount;
    private boolean selected;
    private boolean hover;

    /**
     * @param species    the species to show
     * @param dropChance the chance in one egg, as a fraction
     * @param ownedCount how many the player owns
     * @param onClick    called when the tile is clicked
     */
    public DexTile(DragonSpecies species, double dropChance, int ownedCount, Runnable onClick) {
        super(DragonTile.WIDTH, DragonTile.HEIGHT);
        this.species = species;
        this.dropChance = dropChance;
        this.ownedCount = ownedCount;
        setCursor(Cursor.HAND);
        setOnMouseEntered(e -> {
            hover = true;
            redraw();
        });
        setOnMouseExited(e -> {
            hover = false;
            redraw();
        });
        setOnMouseClicked(e -> onClick.run());
        redraw();
    }

    /** @return the species on this tile */
    public DragonSpecies getSpecies() {
        return species;
    }

    /** @param selected true to draw the highlighted frame */
    public void setSelected(boolean selected) {
        this.selected = selected;
        redraw();
    }

    private void redraw() {
        GraphicsContext g = getGraphicsContext2D();
        g.setImageSmoothing(false);
        double w = DragonTile.WIDTH;
        double h = DragonTile.HEIGHT;
        g.setFill(selected ? Theme.GOLD : Theme.INK);
        g.fillRect(0, 0, w, h);
        double inset = selected ? 5 : 3;
        g.setFill(hover ? Theme.mix(Theme.PAPER, Color.WHITE, 0.5) : Theme.PAPER);
        g.fillRect(inset, inset, w - 2 * inset, h - 2 * inset);
        g.setFill(Theme.rgb(species.getDragonClass().getColorRgb()));
        g.fillRect(3, 3, w - 6, 6);

        Image sprite = SpriteFactory.dragon(species, true, 2);
        g.drawImage(sprite, Math.round((w - sprite.getWidth()) / 2), 12);

        g.setFill(Theme.INK);
        g.setFont(Theme.font(15, true));
        String name = species.getShortName();
        g.fillText(name, Math.max(6, (w - Draw.width(g.getFont(), name)) / 2), 105);

        g.setFont(Theme.font(12, false));
        g.fillText(species.getDragonClass().getDisplayName(), 8, 126);
        g.setFont(Theme.font(12, true));
        double chipW = Draw.width(g.getFont(), DragonDetailPane.formatPercent(dropChance)) + 14;
        Draw.chip(g, DragonDetailPane.formatPercent(dropChance), w - chipW - 6, 114, 18, Theme.PAPER_DARK, Theme.INK);
        g.setFont(Theme.font(11, true));
        Draw.chip(g, "Tier " + species.getTier().getLabel(), w - 62, 14, 17, tierColor(), Color.WHITE);
        if (ownedCount > 0) {
            Draw.chip(g, ownedCount > 1 ? "Owned x" + ownedCount : "Owned", 8, 14, 17, Theme.rgb(0x2E9E52), Color.WHITE);
        }
    }

    private Color tierColor() {
        return switch (species.getTier()) {
            case S -> Theme.rgb(0xD9A21B);
            case A -> Theme.rgb(0x8B5CC4);
            case B -> Theme.rgb(0x2F8F9D);
            case C -> Theme.rgb(0x6B7280);
        };
    }
}
