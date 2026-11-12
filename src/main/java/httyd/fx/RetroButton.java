package httyd.fx;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 * A chunky button with a flat colour, a thick dark border and a drop shadow. It is a normal JavaFX
 * {@link Button} (so {@code setOnAction}, focus and disabling all work); only its backgrounds are
 * replaced to get the retro look.
 */
public class RetroButton extends Button {
    private static final double SHADOW = 4;
    private static final Color DISABLED_FACE = Theme.rgb(0x6B7280);
    private static final Color DISABLED_TEXT = Theme.rgb(0xC9CDD4);
    private Color baseColor;

    /**
     * @param text  the label
     * @param color the face colour
     */
    public RetroButton(String text, Color color) {
        super(text);
        this.baseColor = color;
        setFont(Theme.font(17, true));
        setFocusTraversable(false);
        setCursor(Cursor.HAND);
        setPrefSize(200, 46);
        hoverProperty().addListener((o, a, b) -> restyle());
        pressedProperty().addListener((o, a, b) -> restyle());
        disabledProperty().addListener((o, a, b) -> restyle());
        restyle();
    }

    /**
     * Creates a button with the default accent colour.
     *
     * @param text the label
     */
    public RetroButton(String text) {
        this(text, Theme.ACCENT);
    }

    /**
     * Changes the face colour (used by toggle buttons).
     *
     * @param color the new face colour
     */
    public void setColor(Color color) {
        this.baseColor = color;
        restyle();
    }

    /**
     * Sets the same width and height for the minimum, preferred and maximum size.
     *
     * @param width  the width
     * @param height the height
     */
    public void setFixedSize(double width, double height) {
        setMinSize(width, height);
        setPrefSize(width, height);
        setMaxSize(width, height);
    }

    private void restyle() {
        double dy = isPressed() ? SHADOW - 1 : 0;
        Color face = isDisabled() ? DISABLED_FACE
                : isHover() ? Theme.mix(baseColor, Color.WHITE, 0.18) : baseColor;
        setBackground(new Background(
                new BackgroundFill(Theme.INK, CornerRadii.EMPTY, new Insets(SHADOW, 2, 0, 2)),
                new BackgroundFill(Theme.INK, CornerRadii.EMPTY, new Insets(dy, 0, SHADOW - dy, 0)),
                new BackgroundFill(face, CornerRadii.EMPTY, new Insets(dy + 3, 3, SHADOW - dy + 3, 3))));
        setBorder(new Border(new BorderStroke(
                Theme.mix(face, Color.WHITE, 0.35), Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT,
                BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE,
                CornerRadii.EMPTY, new BorderWidths(3, 0, 0, 0), new Insets(dy + 3, 3, SHADOW - dy + 3, 3))));
        setPadding(new Insets(dy + 3, 3, SHADOW - dy + 3, 3));
        setTextFill(isDisabled() ? DISABLED_TEXT : Color.WHITE);
    }
}
