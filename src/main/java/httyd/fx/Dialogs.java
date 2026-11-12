package httyd.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Pop-up messages in the style of the game. They are small modal windows, so they look the same on
 * every operating system. Call them from a click handler, never from inside an animation timer.
 */
public final class Dialogs {
    private Dialogs() {
    }

    /**
     * Shows a message with one button and waits until it is closed.
     *
     * @param owner   the window that owns the pop-up
     * @param title   the title
     * @param message the message
     */
    public static void info(Window owner, String title, String message) {
        show(owner, title, message, "OK", null);
    }

    /**
     * Asks a yes/no question and waits for the answer.
     *
     * @param owner   the window that owns the pop-up
     * @param title   the title
     * @param message the question
     * @param yes     the label of the yes button
     * @param no      the label of the no button
     * @return true if the player chose yes
     */
    public static boolean confirm(Window owner, String title, String message, String yes, String no) {
        return show(owner, title, message, yes, no);
    }

    private static boolean show(Window owner, String title, String message, String yes, String no) {
        Stage dialog = new Stage(StageStyle.UNDECORATED);
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        boolean[] result = {false};

        Label titleLabel = new Label(title);
        titleLabel.setFont(Theme.font(22, true));
        titleLabel.setTextFill(Theme.INK);
        Label text = new Label(message);
        text.setFont(Theme.font(17, false));
        text.setTextFill(Theme.INK);
        text.setWrapText(true);
        text.setMinWidth(420);
        text.setPrefWidth(420);
        text.setMaxWidth(420);

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        if (no != null) {
            RetroButton noButton = new RetroButton(no, Theme.ACCENT_2);
            noButton.setFixedSize(140, 46);
            noButton.setOnAction(e -> dialog.close());
            buttons.getChildren().add(noButton);
        }
        RetroButton yesButton = new RetroButton(yes, Theme.ACCENT);
        yesButton.setFixedSize(140, 46);
        yesButton.setOnAction(e -> {
            result[0] = true;
            dialog.close();
        });
        buttons.getChildren().add(yesButton);

        VBox box = new VBox(12, titleLabel, text, buttons);
        box.setPadding(new Insets(10, 14, 6, 14));
        RetroPanel panel = new RetroPanel();
        panel.getChildren().add(box);
        panel.setPadding(new Insets(14, 20, 12, 20));
        Scene scene = new Scene(panel);
        scene.getStylesheets().add(Dialogs.class.getResource("/fx/retro.css").toExternalForm());
        dialog.setScene(scene);
        dialog.setOnShown(e -> {
            if (owner != null) {
                dialog.setX(owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2);
                dialog.setY(owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
            }
        });
        dialog.showAndWait();
        return result[0];
    }
}
