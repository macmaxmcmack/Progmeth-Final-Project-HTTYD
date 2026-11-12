package httyd.fx;

import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;

/**
 * A transparent scrolling area that lays {@link DragonTile}s out in three columns.
 */
final class TileScroll extends ScrollPane {
    /** Number of tiles in one row. */
    static final int COLUMNS = 3;
    private final GridPane grid = new GridPane();

    TileScroll() {
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(4));
        setContent(grid);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setFitToWidth(false);
        setMinWidth(300);
        setPrefWidth(DragonTile.WIDTH * COLUMNS + 10 * (COLUMNS + 1) + 24);
        // a click on the empty part must not scroll or select anything
        setPannable(false);
        getStyleClass().add("tile-scroll");
    }

    void setTiles(List<? extends Node> tiles) {
        grid.getChildren().clear();
        for (int i = 0; i < tiles.size(); i++) {
            grid.add(tiles.get(i), i % COLUMNS, i / COLUMNS);
        }
    }
}
