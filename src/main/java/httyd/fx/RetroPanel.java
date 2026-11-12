package httyd.fx;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * A panel with a cream face and a thick dark double border, used for text boxes and cards. Put
 * children in it like in any {@link StackPane}; the padding keeps them clear of the border.
 */
public class RetroPanel extends StackPane {
    /**
     * @param face the face colour
     */
    public RetroPanel(Color face) {
        setBackground(new Background(
                new BackgroundFill(Theme.INK, CornerRadii.EMPTY, Insets.EMPTY),
                new BackgroundFill(face, CornerRadii.EMPTY, new Insets(4))));
        Color shade = Theme.mix(face, Theme.INK, 0.35);
        setBorder(new Border(
                new BorderStroke(shade, Color.TRANSPARENT, Color.TRANSPARENT, shade,
                        BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY, new BorderWidths(3, 0, 0, 3), new Insets(4)),
                new BorderStroke(Theme.INK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                        new BorderWidths(1), new Insets(7))));
        setPadding(new Insets(12));
    }

    /** Creates a cream panel. */
    public RetroPanel() {
        this(Theme.PAPER);
    }
}
