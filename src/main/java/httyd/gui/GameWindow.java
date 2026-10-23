package httyd.gui;

import httyd.battle.BattleSession;
import httyd.model.field.BattlefieldType;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * The main window. It holds all screens in a card layout and moves between them.
 */
public class GameWindow extends JFrame {
    /** Preferred size of the game area. */
    public static final Dimension SIZE = new Dimension(1024, 720);

    private final GameContext context;
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final Map<String, ScreenPanel> screens = new HashMap<>();
    private ScreenPanel current;

    private final MenuPanel menu;
    private final EggPanel egg;
    private final CollectionPanel collection;
    private final TeamSelectPanel teamSelect;
    private final FieldSelectPanel fieldSelect;
    private final BattlePanel battle;
    private final HowToPanel howTo;
    private final TopUpPanel topUp;

    /**
     * @param context the shared game objects
     */
    public GameWindow(GameContext context) {
        super("HTTYD Dragon Battle");
        this.context = context;
        menu = new MenuPanel(this, context);
        egg = new EggPanel(this, context);
        collection = new CollectionPanel(this, context);
        teamSelect = new TeamSelectPanel(this, context);
        fieldSelect = new FieldSelectPanel(this, context);
        battle = new BattlePanel(this, context);
        howTo = new HowToPanel(this, context);
        topUp = new TopUpPanel(this, context);
        register("menu", menu);
        register("egg", egg);
        register("collection", collection);
        register("team", teamSelect);
        register("field", fieldSelect);
        register("battle", battle);
        register("howto", howTo);
        register("topup", topUp);

        setContentPane(root);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        root.setPreferredSize(SIZE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        showMenu();
    }

    private void register(String name, ScreenPanel panel) {
        screens.put(name, panel);
        root.add(panel, name);
    }

    private void show(String name) {
        if (current != null) {
            current.onHide();
        }
        current = screens.get(name);
        current.onShow();
        cards.show(root, name);
        root.revalidate();
        root.repaint();
    }

    /** Shows the main menu. */
    public void showMenu() {
        battle.stopTimers();
        show("menu");
        context.getSound().startMusic();
    }

    /** Shows the egg hatching screen. */
    public void showEgg() {
        show("egg");
    }

    /** Shows the collection screen. */
    public void showCollection() {
        show("collection");
    }

    /** Shows the team selection screen. */
    public void showTeamSelect() {
        show("team");
    }

    /** Shows the free demo coin shop. */
    public void showTopUp() {
        show("topup");
    }

    /** Shows the how-to-play screen. */
    public void showHowTo() {
        show("howto");
    }

    /**
     * Moves from team selection to the battlefield selection.
     *
     * @param setup the player's choices
     */
    public void showFieldSelect(MatchSetup setup) {
        fieldSelect.prepare(setup);
        show("field");
    }

    /**
     * Starts the battle.
     *
     * @param setup   the player's choices
     * @param session the prepared battle
     * @param field   the battlefield that was drawn
     */
    public void startBattle(MatchSetup setup, BattleSession session, BattlefieldType field) {
        battle.prepare(setup, session, field);
        show("battle");
        battle.begin();
    }
}
