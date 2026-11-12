package httyd.fx;

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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * The battle screen. It shows the arena, a message box and the command buttons, and plays the events
 * returned by the {@link BattleSession} one after another.
 */
public class BattleScreen extends ScreenPane {
    private enum UiState { ANIMATING, CHOOSING, ITEMS, REPLACING, FINISHED }

    private static final int BOT_THINK_MILLIS = 650;
    private static final double COMMAND_W = 400;
    private static final double COMMAND_H = 230;
    private static final Color PURPLE = Theme.rgb(0xB34CD6);

    private final Arena arena = new Arena();
    private final MessageBox messageBox = new MessageBox(Arena.WIDTH - 28 - 12 - COMMAND_W, COMMAND_H);
    private final StackPane commandArea = new StackPane();
    private final VBox itemsPanel = new VBox(8);
    private final VBox replacePanel = new VBox(8);
    private final GridPane mainPanel = new GridPane();
    private final VBox waitPanel = new VBox(10);
    private final VBox resultPanel = new VBox(8);

    private final RetroButton attackButton = new RetroButton("Basic Attack", Theme.ACCENT);
    private final RetroButton signatureButton = new RetroButton("Signature", Theme.ACCENT_2);
    private final RetroButton ultimateButton = new RetroButton("Ultimate", PURPLE);
    private final RetroButton itemButton = new RetroButton("Items", Theme.darker(Theme.GOLD, 0.3));
    private final RetroButton restButton = new RetroButton("Rest & Reload", Theme.rgb(0x5A6FD6));
    private final RetroButton forfeitButton = new RetroButton("Forfeit", Theme.rgb(0x7A4A4A));

    private final AnimationTimer timer;
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
    private boolean running;
    private long lastTick;
    private double clockAccumulator;

    /**
     * @param window  the game window
     * @param context the shared game objects
     */
    public BattleScreen(GameWindow window, GameContext context) {
        super(window, context);
        arena.setOnMouseClicked(e -> skipCurrent());
        messageBox.setOnMouseClicked(e -> skipCurrent());
        content.setCenter(arena);

        buildCommandArea();
        commandArea.setMinSize(COMMAND_W, COMMAND_H);
        commandArea.setPrefSize(COMMAND_W, COMMAND_H);
        commandArea.setMaxSize(COMMAND_W, COMMAND_H);
        HBox bottom = new HBox(12, messageBox, commandArea);
        bottom.setPadding(new Insets(8, 14, 12, 14));
        bottom.setPrefHeight(250);
        content.setBottom(bottom);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                tick(now);
            }
        };
    }

    // ------------------------------------------------------------------ setup

    private void buildCommandArea() {
        mainPanel.setHgap(8);
        mainPanel.setVgap(8);
        List<RetroButton> buttons = List.of(attackButton, signatureButton, ultimateButton, itemButton, restButton, forfeitButton);
        for (int i = 0; i < buttons.size(); i++) {
            RetroButton b = buttons.get(i);
            b.setFont(Theme.font(16, true));
            b.setFixedSize((COMMAND_W - 8) / 2, 70);
            mainPanel.add(b, i % 2, i / 2);
        }
        attackButton.setOnAction(e -> doAttack(MoveKind.TYPELESS));
        signatureButton.setOnAction(e -> doAttack(MoveKind.SIGNATURE));
        ultimateButton.setOnAction(e -> doAttack(MoveKind.ULTIMATE));
        itemButton.setOnAction(e -> showItems());
        restButton.setOnAction(e -> perform(new BattleAction.Rest()));
        forfeitButton.setOnAction(e -> forfeit());
        hint(attackButton, () -> describe(MoveKind.TYPELESS));
        hint(signatureButton, () -> describe(MoveKind.SIGNATURE));
        hint(ultimateButton, () -> describe(MoveKind.ULTIMATE));
        hint(itemButton, () -> "Use one item from your bag. It costs your whole turn.");
        hint(restButton, () -> "Skip attacking this turn and regain +" + GameRules.REST_RELOAD_SHOTS + " shots");
        hint(forfeitButton, () -> "Give up and end the battle now");

        Label label = new Label("Battling...\nClick the message box to skip one message at a time");
        label.setTextFill(Theme.PAPER);
        label.setFont(Theme.font(15, false));
        label.setTextAlignment(TextAlignment.CENTER);
        label.setWrapText(true);
        label.setMaxWidth(COMMAND_W);
        RetroButton skip = new RetroButton("Skip >>", Theme.ACCENT_2);
        skip.setFixedSize(160, 46);
        skip.setOnAction(e -> fastForward = true);
        waitPanel.setAlignment(Pos.CENTER);
        waitPanel.getChildren().addAll(label, skip);

        itemsPanel.setAlignment(Pos.CENTER);
        replacePanel.setAlignment(Pos.CENTER);

        RetroButton again = new RetroButton("Play Again", Theme.ACCENT);
        again.setFixedSize(COMMAND_W, 60);
        again.setOnAction(e -> window.showFieldSelect(setup));
        RetroButton menu = new RetroButton("Main Menu", Theme.ACCENT_2);
        menu.setFixedSize(COMMAND_W, 60);
        menu.setOnAction(e -> window.showMenu());
        resultPanel.setAlignment(Pos.CENTER);
        resultPanel.getChildren().addAll(again, menu);

        commandArea.getChildren().addAll(mainPanel, waitPanel, itemsPanel, replacePanel, resultPanel);
    }

    private void showCard(Node card) {
        for (Node n : commandArea.getChildren()) {
            n.setVisible(n == card);
        }
    }

    private void hint(RetroButton button, Supplier<String> text) {
        button.setOnMouseEntered(e -> {
            if (state == UiState.CHOOSING) {
                messageBox.showPrompt(text.get());
            }
        });
        button.setOnMouseExited(e -> {
            if (state == UiState.CHOOSING) {
                messageBox.showPrompt(prompt());
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
        showCard(waitPanel);
    }

    /** Starts the match: plays the opening events. */
    public void begin() {
        lastTick = System.nanoTime();
        running = true;
        timer.start();
        enqueue(session.start());
    }

    @Override
    public void onHide() {
        running = false;
        timer.stop();
    }

    // ------------------------------------------------------------------ event playback

    private void enqueue(List<BattleEvent> events) {
        engine.suspendClock();
        state = UiState.ANIMATING;
        showCard(waitPanel);
        queue.addAll(events);
    }

    private void tick(long now) {
        if (!running) {
            return;
        }
        double dt = (now - lastTick) / 1_000_000.0;
        lastTick = now;
        dt = Math.min(dt, 100);
        arena.advance(dt);
        if (state != UiState.ANIMATING) {
            messageBox.advance(dt);
        }

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
        messageBox.redraw();
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
                    Combatant c = engine.getActive(s);
                    if (c != null && e.text().startsWith(c.getName() + " uses ")) {
                        arena.lunge(s);
                        sound.play(SoundPlayer.Sound.CLICK);
                    }
                }
            }
            case DAMAGE, REFLECT -> {
                arena.changeHp(side, -e.value());
                arena.floatText(side, "-" + e.value(), Theme.rgb(0xFF5A4D), 30);
                sound.play(SoundPlayer.Sound.HIT);
            }
            case CRIT -> {
                arena.changeHp(side, -e.value());
                arena.floatText(side, "-" + e.value() + "!", Theme.rgb(0xFFD23C), 40);
                sound.play(SoundPlayer.Sound.CRIT);
            }
            case DODGE -> {
                arena.dodge(side);
                arena.floatText(side, "Dodged!", Theme.rgb(0x7FD6FF), 30);
                sound.play(SoundPlayer.Sound.DODGE);
            }
            case HEAL -> {
                arena.changeHp(side, e.value());
                arena.floatText(side, "+" + e.value(), Theme.rgb(0x66E07A), 30);
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
        showCard(mainPanel);
        updateClock(0);
    }

    private void refreshCommands() {
        Combatant me = engine.getActive(Side.PLAYER);
        attackButton.setDisable(!engine.canPerform(Side.PLAYER, new BattleAction.Attack(MoveKind.TYPELESS)));
        signatureButton.setText("Signature " + me.getShots() + "/" + me.getMaxShots());
        signatureButton.setDisable(!engine.canPerform(Side.PLAYER, new BattleAction.Attack(MoveKind.SIGNATURE)));
        ultimateButton.setText("Ultimate " + me.getUltimateCharge() + "/" + GameRules.ULTIMATE_CHARGE_NEEDED);
        ultimateButton.setDisable(!engine.canPerform(Side.PLAYER, new BattleAction.Attack(MoveKind.ULTIMATE)));
        boolean anyItem = false;
        int bag = engine.getPlayer(Side.PLAYER).getBag().size();
        for (int i = 0; i < bag; i++) {
            anyItem |= engine.canPerform(Side.PLAYER, new BattleAction.UseItem(i));
        }
        itemButton.setText("Items (" + bag + ")");
        itemButton.setDisable(!anyItem);
        restButton.setDisable(!engine.canPerform(Side.PLAYER, new BattleAction.Rest()));
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
        itemsPanel.getChildren().clear();
        List<Item> bag = engine.getPlayer(Side.PLAYER).getBag();
        for (int i = 0; i < bag.size(); i++) {
            Item item = bag.get(i);
            int index = i;
            RetroButton b = new RetroButton(item.getDisplayName(), Theme.darker(Theme.GOLD, 0.3));
            b.setFont(Theme.font(16, true));
            b.setFixedSize(COMMAND_W, 62);
            b.setDisable(!engine.canPerform(Side.PLAYER, new BattleAction.UseItem(index)));
            b.setOnAction(e -> perform(new BattleAction.UseItem(index)));
            b.setOnMouseEntered(e -> messageBox.showPrompt(item.getDescription()));
            b.setOnMouseExited(e -> messageBox.showPrompt("Choose an item to use"));
            itemsPanel.getChildren().add(b);
        }
        RetroButton back = new RetroButton("Back", Theme.ACCENT_2);
        back.setFixedSize(COMMAND_W, 62);
        back.setOnAction(e -> {
            state = UiState.CHOOSING;
            messageBox.showPrompt(prompt());
            showCard(mainPanel);
        });
        itemsPanel.getChildren().add(back);
        messageBox.showPrompt("Choose an item to use");
        showCard(itemsPanel);
    }

    private void forfeit() {
        if (Dialogs.confirm(window.getStage(), "Forfeit?", "If you forfeit, you lose the match immediately.",
                "Forfeit", "Keep Fighting")) {
            enqueue(engine.forfeit(Side.PLAYER));
        }
    }

    private void showReplacement() {
        state = UiState.REPLACING;
        replacePanel.getChildren().clear();
        List<Combatant> team = engine.getTeam(Side.PLAYER);
        for (int i = 0; i < team.size(); i++) {
            Combatant c = team.get(i);
            if (c.isFainted()) {
                continue;
            }
            int index = i;
            RetroButton b = new RetroButton(c.getName() + "  HP " + c.getCurrentHp() + "/" + c.getMaxHp(),
                    Theme.darker(Theme.rgb(c.getDragonClass().getColorRgb()), 0.3));
            b.setFont(Theme.font(16, true));
            b.setFixedSize(COMMAND_W, 62);
            b.setOnAction(e -> {
                context.getSound().play(SoundPlayer.Sound.CLICK);
                enqueue(session.playerReplacement(index));
            });
            replacePanel.getChildren().add(b);
        }
        messageBox.showPrompt("Your dragon fainted! Choose the next one to send out");
        showCard(replacePanel);
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
                color = Theme.rgb(0xFF6B5E);
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
        showCard(resultPanel);
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
