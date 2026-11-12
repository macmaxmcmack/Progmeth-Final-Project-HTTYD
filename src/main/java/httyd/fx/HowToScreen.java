package httyd.fx;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;

/**
 * The "how to play" screen with the rules in English: a scrolling page of text.
 */
public class HowToScreen extends ScreenPane {
    private static final String TEXT = String.join("\n",
        "Goal",
        "  Take your team of 3 dragons into battle against the bot. You win when the other team has no dragons left or runs out of time.",
        "",
        "Where do dragons come from?",
        "  - Open the \"Hatch a Dragon Egg\" screen. One egg costs 50 coins. Each dragon rolls random IVs (-3 to +4), a Nature and a 5% chance of being an Alpha (+5 to every stat).",
        "  - The grade (B / A / SSS) tells you how good the IV roll was.",
        "  - Got a duplicate? Open \"Dragon Collection\" and fuse it to add a star: 2 stars give +5% HP and +1 shot, 3 stars make a Titan Wing (passive 50% stronger).",
        "  - Out of coins? Use \"Top Up Coins\" in the main menu. It is a free demo shop, no real money is involved.",
        "  - A win pays 60 coins, a draw 25 and a loss 20.",
        "",
        "Before the battle",
        "  1) Pick 3 dragons and put them in order. The top one fights first.",
        "  2) Pick up to 2 trainer items and the bot's difficulty.",
        "  3) Pick 2 of the 7 battlefields. The bot picks 1, then the game draws 1 of the 3 as the real battlefield.",
        "",
        "During the battle (you take turns, one action per turn)",
        "  - Basic Attack: no element, unlimited use.",
        "  - Signature: stronger and can inflict effects. It uses 1 shot. Elemental advantage or disadvantage changes the damage.",
        "  - Ultimate: needs 4 charge points (you gain one when you attack and when you are hit). Using it turns the battlefield into your class's home field for 2 turns.",
        "  - Items: use 1 item for a whole turn (heal, reload shots, boost Attack/Defense, cleanse status effects).",
        "  - Rest & Reload: skip attacking to regain +2 shots.",
        "",
        "Formulas",
        "  - Damage = Attack x 100 / (100 + Defense), times move power, type multiplier and x1.5 on a critical hit.",
        "  - Dodge chance = (defender Speed - attacker Speed) / 4 %, capped at 25%.",
        "  - The side with the higher Speed moves first.",
        "  - Burning and poison drain 5-8% of max HP per turn for 2 turns.",
        "",
        "Type chart (Signature and Ultimate moves: advantage x1.25 / disadvantage x0.8)",
        "  Strike beats Boulder, Tidal      Boulder beats Sharp, Mystery",
        "  Sharp beats Stoker, Strike       Stoker beats Tracker, Mystery",
        "  Tracker beats Boulder, Strike    Tidal beats Stoker, Tracker",
        "  Mystery beats Sharp, Tidal",
        "",
        "Time",
        "  - 1 minute per turn; running out loses the match. Each match also has a 4-minute total clock per side.",
        "  - If 140 actions pass without a winner, the side with more HP left wins.",
        "  - If the last dragons of both sides faint at the same time (for example through reflected damage), the match is a draw."
    );

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    public HowToScreen(GameWindow window, GameContext context) {
        super(window, context);
        content.setTop(header("How to Play", "Main Menu", window::showMenu));
        Label label = new Label(TEXT);
        label.setFont(Theme.font(17, false));
        label.setTextFill(Theme.INK);
        label.setWrapText(true);
        label.setPadding(new Insets(16, 22, 16, 22));
        label.setPrefWidth(870);
        label.setMaxWidth(870);
        label.setMinWidth(870);

        ScrollPane scroll = new ScrollPane(label);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setFitToWidth(true);
        scroll.setBackground(new Background(new BackgroundFill(Theme.PAPER, CornerRadii.EMPTY, Insets.EMPTY)));
        scroll.setStyle("-fx-background-color: #" + String.format("%06X", 0xF6EFD9) + ";");
        scroll.setBorder(new Border(new BorderStroke(Theme.INK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(4))));
        label.setBackground(new Background(new BackgroundFill(Theme.PAPER, CornerRadii.EMPTY, Insets.EMPTY)));
        scroll.setPrefSize(900, 560);

        StackPane holder = new StackPane(scroll);
        holder.setPadding(new Insets(6, 30, 26, 30));
        content.setCenter(holder);
    }
}
