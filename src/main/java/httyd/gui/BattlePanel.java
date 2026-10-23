package httyd.gui;

import httyd.battle.BattleAction;
import httyd.battle.BattleEngine;
import httyd.battle.BattleEvent;
import httyd.battle.BattleSession;
import httyd.battle.Combatant;
import httyd.battle.Side;
import httyd.battle.TypeChart;
import httyd.model.GameRules;
import httyd.model.field.BattlefieldType;
import httyd.model.item.Item;
import httyd.model.skill.Move;
import httyd.model.skill.MoveKind;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * The battle screen. It shows the arena, a message box and the command buttons, and plays the
 * events returned by the {@link BattleSession} one after another.
 */
public class BattlePanel extends ScreenPanel {
    private enum UiState { ANIMATING, CHOOSING, ITEMS, REPLACING, FINISHED }

    private static final int TICK_MILLIS = 30;
    private static final int BOT_THINK_MILLIS = 650;

    private final Arena arena = new Arena();
    private final MessageBox messageBox = new MessageBox();
    private final CardLayout commandCards = new CardLayout();
    private final JPanel commandArea = new JPanel(commandCards);
    private final JPanel itemsPanel = new JPanel(new GridLayout(0, 1, 8, 8));
    private final JPanel replacePanel = new JPanel(new GridLayout(0, 1, 8, 8));

    private final RetroButton attackButton = new RetroButton("Basic Attack", Theme.ACCENT);
    private final RetroButton signatureButton = new RetroButton("Signature", Theme.ACCENT_2);
    private final RetroButton ultimateButton = new RetroButton("Ultimate", new Color(0xB34CD6));
    private final RetroButton itemButton = new RetroButton("Items", Theme.GOLD.darker());
    private final RetroButton restButton = new RetroButton("Rest & Reload", new Color(0x5A6FD6));
    private final RetroButton forfeitButton = new RetroButton("Forfeit", new Color(0x7A4A4A));

    private final Timer timer;
    private final Deque<BattleEvent> queue = new ArrayDeque<>();

    private MatchSetup setup;
    private BattleSession session;
    private BattleEngine engine;
    private UiState state = UiState.ANIMATING;
    private boolean eventActive;
    private double holdLeft;
    private double delayLeft;
    private Runnable afterDelay;
    private boolean fastForward;
    private boolean rewarded;
    private long lastTick;
    private double clockAccumulator;

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    public BattlePanel(GameWindow window, GameContext context) {
        super(window, context);
        arena.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                skipCurrent();
            }
        });
        messageBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                skipCurrent();
            }
        });
        add(arena, BorderLayout.CENTER);

        buildCommandArea();
        JPanel bottom = new JPanel(new BorderLayout(12, 0));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 14, 12, 14));
        bottom.setPreferredSize(new Dimension(1024, 250));
        bottom.add(messageBox, BorderLayout.CENTER);
        commandArea.setOpaque(false);
        commandArea.setPreferredSize(new Dimension(400, 230));
        bottom.add(commandArea, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        timer = new Timer(TICK_MILLIS, e -> tick());
    }

    // ------------------------------------------------------------------ setup

    private void buildCommandArea() {
        JPanel main = new JPanel(new GridLayout(3, 2, 8, 8));
        main.setOpaque(false);
        for (RetroButton b : List.of(attackButton, signatureButton, ultimateButton, itemButton, restButton, forfeitButton)) {
            b.setFont(Theme.font(16, true));
            main.add(b);
        }
        attackButton.addActionListener(e -> doAttack(MoveKind.TYPELESS));
        signatureButton.addActionListener(e -> doAttack(MoveKind.SIGNATURE));
        ultimateButton.addActionListener(e -> doAttack(MoveKind.ULTIMATE));
        itemButton.addActionListener(e -> showItems());
        restButton.addActionListener(e -> perform(new BattleAction.Rest()));
        forfeitButton.addActionListener(e -> forfeit());
        hint(attackButton, () -> describe(MoveKind.TYPELESS));
        hint(signatureButton, () -> describe(MoveKind.SIGNATURE));
        hint(ultimateButton, () -> describe(MoveKind.ULTIMATE));
        hint(itemButton, () -> "Use one item from your bag. It costs your whole turn.");
        hint(restButton, () -> "Skip attacking this turn and regain +" + GameRules.REST_RELOAD_SHOTS + " shots");
        hint(forfeitButton, () -> "Give up and end the battle now");

        JPanel waiting = new JPanel(new BorderLayout());
        waiting.setOpaque(false);
        RetroButton skip = new RetroButton("Skip >>", Theme.ACCENT_2);
        skip.setPreferredSize(new Dimension(160, 46));
        skip.addActionListener(e -> fastForward = true);
        JPanel skipHolder = new JPanel();
        skipHolder.setOpaque(false);
        skipHolder.add(skip);
        JLabel label = new JLabel("<html><center>Battling...<br>Click the message box to skip one message at a time</center></html>", JLabel.CENTER);
        label.setForeground(Theme.PAPER);
        label.setFont(Theme.font(15, false));
        waiting.add(label, BorderLayout.CENTER);
        waiting.add(skipHolder, BorderLayout.SOUTH);

        itemsPanel.setOpaque(false);
        replacePanel.setOpaque(false);

        JPanel result = new JPanel(new GridLayout(2, 1, 8, 8));
        result.setOpaque(false);
        result.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        RetroButton again = new RetroButton("Play Again", Theme.ACCENT);
        again.addActionListener(e -> window.showFieldSelect(setup));
        RetroButton menu = new RetroButton("Main Menu", Theme.ACCENT_2);
        menu.addActionListener(e -> window.showMenu());
        result.add(again);
        result.add(menu);

        commandArea.add(main, "main");
        commandArea.add(waiting, "wait");
        commandArea.add(itemsPanel, "items");
        commandArea.add(replacePanel, "replace");
        commandArea.add(result, "result");
    }

    private void hint(RetroButton button, Supplier<String> text) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (state == UiState.CHOOSING) {
                    messageBox.showPrompt(text.get());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (state == UiState.CHOOSING) {
                    messageBox.showPrompt(prompt());
                }
            }
        });
    }

    /**
     * Prepares a new match.
     *
     * @param setup   the player's choices
     * @param session the battle
     * @param field   the battlefield
     */
    public void prepare(MatchSetup setup, BattleSession session, BattlefieldType field) {
        this.setup = setup;
        this.session = session;
        this.engine = session.getEngine();
        queue.clear();
        eventActive = false;
        holdLeft = 0;
        delayLeft = 0;
        afterDelay = null;
        fastForward = false;
        rewarded = false;
        clockAccumulator = 0;
        arena.reset(engine, field);
        messageBox.clear();
        state = UiState.ANIMATING;
        commandCards.show(commandArea, "wait");
    }

    /** Starts the match: plays the opening events. */
    public void begin() {
        lastTick = System.nanoTime();
        timer.start();
        enqueue(session.start());
    }

    /** Stops the animation timer. */
    public void stopTimers() {
        timer.stop();
    }

    @Override
    public void onHide() {
        timer.stop();
    }

    // ------------------------------------------------------------------ event playback

    private void enqueue(List<BattleEvent> events) {
        engine.suspendClock();
        state = UiState.ANIMATING;
        commandCards.show(commandArea, "wait");
        queue.addAll(events);
    }

    private void tick() {
        long now = System.nanoTime();
        double dt = (now - lastTick) / 1_000_000.0;
        lastTick = now;
        dt = Math.min(dt, 100);
        arena.advance(dt);
        if (state != UiState.ANIMATING) {
            messageBox.advance(dt);
        }
        messageBox.repaint();

        if (state == UiState.ANIMATING) {
            if (eventActive) {
                if (fastForward) {
                    messageBox.revealAll();
                }
                messageBox.advance(dt);
                if (messageBox.isRevealed()) {
                    holdLeft -= dt;
                    if (holdLeft <= 0) {
                        eventActive = false;
                    }
                }
            }
            if (!eventActive) {
                BattleEvent next = queue.poll();
                if (next != null) {
                    startEvent(next);
                } else if (afterDelay != null) {
                    delayLeft -= dt;
                    if (delayLeft <= 0 || fastForward) {
                        Runnable run = afterDelay;
                        afterDelay = null;
                        run.run();
                    }
                } else {
                    onQueueDrained();
                }
            }
        } else if (state == UiState.CHOOSING || state == UiState.ITEMS) {
            updateClock(dt);
        }
    }

    private void skipCurrent() {
        if (state == UiState.ANIMATING && eventActive) {
            if (!messageBox.isRevealed()) {
                messageBox.revealAll();
            } else {
                holdLeft = 0;
            }
        }
    }

    private void startEvent(BattleEvent e) {
        eventActive = true;
        messageBox.push(e.text());
        holdLeft = fastForward ? 40 : holdFor(e);
        SoundPlayer sound = context.getSound();
        Side side = e.side();
        switch (e.type()) {
            case MESSAGE -> {
                for (Side s : Side.values()) {
                    Combatant c = shownCombatant(s);
                    if (c != null && e.text().startsWith(c.getName() + " uses ")) {
                        arena.lunge(s);
                        sound.play(SoundPlayer.Sound.CLICK);
                    }
                }
            }
            case DAMAGE, REFLECT -> {
                arena.changeHp(side, -e.value());
                arena.floatText(side, "-" + e.value(), new Color(0xFF5A4D), 30);
                sound.play(SoundPlayer.Sound.HIT);
            }
            case CRIT -> {
                arena.changeHp(side, -e.value());
                arena.floatText(side, "-" + e.value() + "!", new Color(0xFFD23C), 40);
                sound.play(SoundPlayer.Sound.CRIT);
            }
            case DODGE -> {
                arena.dodge(side);
                arena.floatText(side, "Dodged!", new Color(0x7FD6FF), 30);
                sound.play(SoundPlayer.Sound.DODGE);
            }
            case HEAL -> {
                arena.changeHp(side, e.value());
                arena.floatText(side, "+" + e.value(), new Color(0x66E07A), 30);
                sound.play(SoundPlayer.Sound.HEAL);
            }
            case STATUS -> sound.play(SoundPlayer.Sound.STATUS);
            case FAINT -> {
                arena.faint(side);
                sound.play(SoundPlayer.Sound.FAINT);
            }
            case SWITCH_IN -> {
                arena.sendOut(side, engine.getTeam(side).get(e.value()));
                sound.play(SoundPlayer.Sound.EGG_OPEN);
            }
            case FIELD -> {
                arena.setField(engine.getBattlefield().getCurrentField());
                sound.play(SoundPlayer.Sound.ULTIMATE);
            }
            case ITEM -> sound.play(SoundPlayer.Sound.HEAL);
            case REST -> sound.play(SoundPlayer.Sound.CLICK);
            case END -> { }
            default -> { }
        }
    }

    private Combatant shownCombatant(Side side) {
        // the arena shows the dragon that was sent out; the engine's active one is the same during a batch
        return engine.getActive(side);
    }

    private double holdFor(BattleEvent e) {
        return switch (e.type()) {
            case MESSAGE -> 520;
            case DAMAGE, REFLECT, HEAL -> 650;
            case CRIT -> 800;
            case DODGE -> 650;
            case STATUS, ITEM, REST -> 600;
            case FAINT -> 1000;
            case SWITCH_IN -> 750;
            case FIELD -> 850;
            case END -> 900;
        };
    }

    private void onQueueDrained() {
        arena.syncWithEngine();
        if (engine.getPhase() == BattleEngine.Phase.FINISHED) {
            finishMatch();
        } else if (session.playerMustReplace()) {
            showReplacement();
        } else if (session.botHasDecision()) {
            afterDelay = () -> enqueue(session.advanceBot());
            delayLeft = BOT_THINK_MILLIS;
        } else {
            startPlayerTurn();
        }
    }

    // ------------------------------------------------------------------ the player's turn

    private String prompt() {
        return "What will " + engine.getActive(Side.PLAYER).getName() + " do?";
    }

    private void startPlayerTurn() {
        fastForward = false;
        state = UiState.CHOOSING;
        engine.resumeClock();
        refreshCommands();
        messageBox.showPrompt(prompt());
        commandCards.show(commandArea, "main");
        updateClock(0);
    }

    private void refreshCommands() {
        Combatant me = engine.getActive(Side.PLAYER);
        attackButton.setEnabled(engine.canPerform(Side.PLAYER, new BattleAction.Attack(MoveKind.TYPELESS)));
        signatureButton.setText("Signature " + me.getShots() + "/" + me.getMaxShots());
        signatureButton.setEnabled(engine.canPerform(Side.PLAYER, new BattleAction.Attack(MoveKind.SIGNATURE)));
        ultimateButton.setText("Ultimate " + me.getUltimateCharge() + "/" + GameRules.ULTIMATE_CHARGE_NEEDED);
        ultimateButton.setEnabled(engine.canPerform(Side.PLAYER, new BattleAction.Attack(MoveKind.ULTIMATE)));
        boolean anyItem = false;
        int bag = engine.getPlayer(Side.PLAYER).getBag().size();
        for (int i = 0; i < bag; i++) {
            anyItem |= engine.canPerform(Side.PLAYER, new BattleAction.UseItem(i));
        }
        itemButton.setText("Items (" + bag + ")");
        itemButton.setEnabled(anyItem);
        restButton.setEnabled(engine.canPerform(Side.PLAYER, new BattleAction.Rest()));
    }

    private String describe(MoveKind kind) {
        Combatant me = engine.getActive(Side.PLAYER);
        Combatant foe = engine.getActive(Side.OPPONENT);
        Move move = me.getSpecies().getMove(kind);
        StringBuilder text = new StringBuilder(move.getName()).append(" - ").append(move.getDescription());
        if (move.isElemental()) {
            double mult = TypeChart.multiplier(me.getDragonClass(), foe.getDragonClass());
            if (mult > 1) {
                text.append("  [Type advantage x").append(mult).append("]");
            } else if (mult < 1) {
                text.append("  [Type disadvantage x").append(mult).append("]");
            }
        }
        return text.toString();
    }

    private void doAttack(MoveKind kind) {
        perform(new BattleAction.Attack(kind));
    }

    private void perform(BattleAction action) {
        if (state != UiState.CHOOSING && state != UiState.ITEMS) {
            return;
        }
        context.getSound().play(SoundPlayer.Sound.CLICK);
        enqueue(session.playerAction(action));
    }

    private void showItems() {
        state = UiState.ITEMS;
        itemsPanel.removeAll();
        List<Item> bag = engine.getPlayer(Side.PLAYER).getBag();
        for (int i = 0; i < bag.size(); i++) {
            Item item = bag.get(i);
            int index = i;
            RetroButton b = new RetroButton(item.getDisplayName(), Theme.GOLD.darker());
            b.setFont(Theme.font(16, true));
            b.setEnabled(engine.canPerform(Side.PLAYER, new BattleAction.UseItem(index)));
            b.addActionListener(e -> perform(new BattleAction.UseItem(index)));
            b.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    messageBox.showPrompt(item.getDescription());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    messageBox.showPrompt("Choose an item to use");
                }
            });
            itemsPanel.add(b);
        }
        RetroButton back = new RetroButton("Back", Theme.ACCENT_2);
        back.addActionListener(e -> {
            state = UiState.CHOOSING;
            messageBox.showPrompt(prompt());
            commandCards.show(commandArea, "main");
        });
        itemsPanel.add(back);
        itemsPanel.revalidate();
        messageBox.showPrompt("Choose an item to use");
        commandCards.show(commandArea, "items");
    }

    private void forfeit() {
        if (Dialogs.confirm(this, "Forfeit?", "If you forfeit, you lose the match immediately.", "Forfeit", "Keep Fighting")) {
            enqueue(engine.forfeit(Side.PLAYER));
        }
    }

    private void showReplacement() {
        state = UiState.REPLACING;
        replacePanel.removeAll();
        List<Combatant> team = engine.getTeam(Side.PLAYER);
        for (int i = 0; i < team.size(); i++) {
            Combatant c = team.get(i);
            if (c.isFainted()) {
                continue;
            }
            int index = i;
            RetroButton b = new RetroButton(c.getName() + "  HP " + c.getCurrentHp() + "/" + c.getMaxHp(),
                    new Color(c.getDragonClass().getColorRgb()).darker());
            b.setFont(Theme.font(16, true));
            b.addActionListener(e -> {
                context.getSound().play(SoundPlayer.Sound.CLICK);
                enqueue(session.playerReplacement(index));
            });
            replacePanel.add(b);
        }
        replacePanel.revalidate();
        messageBox.showPrompt("Your dragon fainted! Choose the next one to send out");
        commandCards.show(commandArea, "replace");
    }

    // ------------------------------------------------------------------ clock

    private void updateClock(double dt) {
        clockAccumulator += dt;
        if (clockAccumulator < 200 && dt != 0) {
            return;
        }
        clockAccumulator = 0;
        long turn = Math.max(0, engine.getClock().turnLeftMillis(Side.PLAYER));
        long total = Math.max(0, engine.getClock().totalLeftMillis(Side.PLAYER));
        arena.setClock("Turn " + format(turn) + "   Total " + format(total), turn < 10_000 || total < 30_000);
        List<BattleEvent> timeout = engine.checkTimeout();
        if (!timeout.isEmpty()) {
            enqueue(timeout);
        }
    }

    private static String format(long millis) {
        long seconds = (millis + 999) / 1000;
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    // ------------------------------------------------------------------ end of the match

    private void finishMatch() {
        state = UiState.FINISHED;
        arena.setClock("", false);
        BattleEngine.Outcome outcome = engine.getOutcome();
        int reward = 0;
        if (!rewarded) {
            rewarded = true;
            reward = context.getProfile().recordResult(outcome);
            context.save();
        }
        String title;
        Color color;
        switch (outcome) {
            case PLAYER_WIN -> {
                title = "Victory!";
                color = Theme.GOLD;
                context.getSound().play(SoundPlayer.Sound.WIN);
            }
            case OPPONENT_WIN -> {
                title = "Defeat";
                color = new Color(0xFF6B5E);
                context.getSound().play(SoundPlayer.Sound.LOSE);
            }
            default -> {
                title = "Draw";
                color = Theme.PAPER;
                context.getSound().play(SoundPlayer.Sound.LOSE);
            }
        }
        arena.setBanner(title, color);
        messageBox.push(title + "  " + reasonText(outcome) + "\nYou earned " + reward + " coins (balance: "
                + context.getProfile().getCoins() + ")");
        commandCards.show(commandArea, "result");
    }

    private String reasonText(BattleEngine.Outcome outcome) {
        BattleEngine.EndReason reason = engine.getEndReason();
        if (reason == null) {
            return "";
        }
        return switch (reason) {
            case KNOCKOUT -> outcome == BattleEngine.Outcome.DRAW ? "The last dragons fainted at the same time"
                    : outcome == BattleEngine.Outcome.PLAYER_WIN ? "You defeated all of the bot's dragons" : "All of your dragons fainted";
            case TIMEOUT -> "You ran out of time";
            case FORFEIT -> "You forfeited";
            case TURN_LIMIT -> "Reached " + GameRules.MAX_ACTIONS + " actions; decided by remaining HP";
        };
    }
}
