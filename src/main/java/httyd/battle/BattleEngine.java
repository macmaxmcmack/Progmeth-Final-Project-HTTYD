package httyd.battle;

import httyd.model.GameRules;
import httyd.model.dragon.Stat;
import httyd.model.field.Battlefield;
import httyd.model.field.BattlefieldType;
import httyd.model.item.Item;
import httyd.model.player.Player;
import httyd.model.skill.Move;
import httyd.model.skill.MoveEffect;
import httyd.model.skill.MoveKind;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * The rules of the game without any user interface. The engine is a state machine: it waits for
 * an action of the side whose turn it is, resolves it and returns the list of events that
 * happened. The GUI only draws those events, and the unit tests run whole battles through it.
 */
public class BattleEngine {

    /** The state of the match. */
    public enum Phase { WAITING_ACTION, WAITING_REPLACEMENT, FINISHED }

    /** The result of a finished match. */
    public enum Outcome { PLAYER_WIN, OPPONENT_WIN, DRAW }

    /** Why the match ended. */
    public enum EndReason { KNOCKOUT, TIMEOUT, FORFEIT, TURN_LIMIT }

    private final Map<Side, Player> players = new EnumMap<>(Side.class);
    private final Map<Side, List<Combatant>> teams = new EnumMap<>(Side.class);
    private final Map<Side, Integer> activeIndex = new EnumMap<>(Side.class);
    private final Battlefield field;
    private final BattleLog log = new BattleLog();
    private final Random random;
    private final BattleClock clock;
    private final Set<Side> pendingReplacement = EnumSet.noneOf(Side.class);

    private Side current;
    private Phase phase = Phase.WAITING_ACTION;
    private Outcome outcome;
    private EndReason endReason;
    private int actionCount;
    private boolean started;

    /**
     * Creates a match.
     *
     * @param player    the human side
     * @param opponent  the other side
     * @param fieldType the battlefield drawn for this match
     * @param random    the random source (a seeded one makes the match repeatable)
     * @param time      the time source for the clocks
     */
    public BattleEngine(Player player, Player opponent, BattlefieldType fieldType,
                        Random random, TimeSource time) {
        players.put(Side.PLAYER, player);
        players.put(Side.OPPONENT, opponent);
        this.field = new Battlefield(fieldType);
        this.random = random;
        this.clock = new BattleClock(time);
        for (Side side : Side.values()) {
            List<Combatant> list = new ArrayList<>();
            players.get(side).getTeam().forEach(d -> list.add(new Combatant(side, d)));
            teams.put(side, list);
            activeIndex.put(side, 0);
        }
    }

    // ------------------------------------------------------------------ queries

    /** @return the current phase */
    public Phase getPhase() {
        return phase;
    }

    /** @return the side whose turn it is (or whose turn comes after the replacements) */
    public Side getCurrentSide() {
        return current;
    }

    /** @return the outcome, or {@code null} while the match is running */
    public Outcome getOutcome() {
        return outcome;
    }

    /** @return the reason the match ended, or {@code null} */
    public EndReason getEndReason() {
        return endReason;
    }

    /** @return the battlefield state */
    public Battlefield getBattlefield() {
        return field;
    }

    /** @return the clocks */
    public BattleClock getClock() {
        return clock;
    }

    /** @return number of actions performed so far */
    public int getActionCount() {
        return actionCount;
    }

    /**
     * @param side the side
     * @return the participant of that side
     */
    public Player getPlayer(Side side) {
        return players.get(side);
    }

    /**
     * @param side the side
     * @return all three dragons of that side, fainted or not
     */
    public List<Combatant> getTeam(Side side) {
        return List.copyOf(teams.get(side));
    }

    /**
     * @param side the side
     * @return the dragon on the field
     */
    public Combatant getActive(Side side) {
        return teams.get(side).get(activeIndex.get(side));
    }

    /**
     * @param side the side
     * @return how many dragons of that side have not fainted
     */
    public int remaining(Side side) {
        return (int) teams.get(side).stream().filter(c -> !c.isFainted()).count();
    }

    /** @return the sides that must still choose a replacement dragon */
    public Set<Side> getPendingReplacements() {
        return EnumSet.copyOf(pendingReplacement.isEmpty() ? EnumSet.noneOf(Side.class) : pendingReplacement);
    }

    /**
     * Tells whether an action is legal right now.
     *
     * @param side   the side that wants to act
     * @param action the action
     * @return true if the action can be performed
     */
    public boolean canPerform(Side side, BattleAction action) {
        if (phase != Phase.WAITING_ACTION || side != current) {
            return false;
        }
        Combatant me = getActive(side);
        return switch (action) {
            case BattleAction.Attack a -> switch (a.kind()) {
                case TYPELESS -> true;
                case SIGNATURE -> me.getShots() > 0;
                case ULTIMATE -> me.isUltimateReady();
            };
            case BattleAction.UseItem u -> u.bagIndex() >= 0
                    && u.bagIndex() < players.get(side).getBag().size()
                    && players.get(side).getBag().get(u.bagIndex()).isUseful(me);
            case BattleAction.Rest r -> me.getShots() < me.getMaxShots();
        };
    }

    // ------------------------------------------------------------------ flow

    /**
     * Starts the match: the leading dragons enter and the faster one moves first.
     *
     * @return the opening events
     */
    public List<BattleEvent> start() {
        if (started) {
            throw new IllegalStateException("already started");
        }
        started = true;
        log.message("The battle begins at " + field.getCurrentField().getDisplayName());
        sendOut(Side.PLAYER, 0);
        sendOut(Side.OPPONENT, 0);
        double playerSpeed = getActive(Side.PLAYER).effectiveStat(Stat.SPD, getActive(Side.OPPONENT), field);
        double opponentSpeed = getActive(Side.OPPONENT).effectiveStat(Stat.SPD, getActive(Side.PLAYER), field);
        Side first;
        if (playerSpeed > opponentSpeed) {
            first = Side.PLAYER;
        } else if (opponentSpeed > playerSpeed) {
            first = Side.OPPONENT;
        } else {
            first = random.nextBoolean() ? Side.PLAYER : Side.OPPONENT;
        }
        log.message(getActive(first).getName() + " is faster and moves first!");
        beginTurn(first);
        return log.drain();
    }

    /**
     * Performs the action of the side whose turn it is.
     *
     * @param action the action
     * @return the events that happened
     * @throws IllegalStateException if the action is not legal now
     */
    public List<BattleEvent> perform(BattleAction action) {
        if (!canPerform(current, action)) {
            throw new IllegalStateException("illegal action " + action + " for " + current + " in " + phase);
        }
        Side actor = current;
        Combatant me = getActive(actor);
        Combatant foe = getActive(actor.other());
        clock.stop();
        actionCount++;
        me.getPassive().onTurnStart(me, log);
        boolean terrainShifted = false;

        switch (action) {
            case BattleAction.Attack a -> terrainShifted = attack(me, foe, a.kind());
            case BattleAction.UseItem u -> {
                Item item = players.get(actor).getBag().remove(u.bagIndex());
                log.add(EventType.ITEM, actor, players.get(actor).getName() + " uses " + item.getDisplayName(), 0);
                item.use(me, log);
            }
            case BattleAction.Rest r -> {
                int added = me.addShots(GameRules.REST_RELOAD_SHOTS);
                log.add(EventType.REST, actor, me.getName() + " rests and reloads +" + added, added);
            }
        }

        if (!me.isFainted()) {
            me.endOwnTurn(log);
        }
        if (!terrainShifted && field.endAction()) {
            log.add(EventType.FIELD, null, "The field returns to " + field.getCurrentField().getDisplayName(), 0);
        }
        resolveKnockouts(actor);
        return log.drain();
    }

    /**
     * Sends a new dragon after the previous one fainted.
     *
     * @param side  the side that chooses
     * @param index index in the team of a dragon that has not fainted
     * @return the events
     * @throws IllegalStateException    if the side does not have to choose now
     * @throws IllegalArgumentException if the dragon cannot fight
     */
    public List<BattleEvent> chooseReplacement(Side side, int index) {
        if (phase != Phase.WAITING_REPLACEMENT || !pendingReplacement.contains(side)) {
            throw new IllegalStateException(side + " does not need a replacement");
        }
        if (index < 0 || index >= teams.get(side).size() || teams.get(side).get(index).isFainted()) {
            throw new IllegalArgumentException("that dragon cannot fight");
        }
        sendOut(side, index);
        pendingReplacement.remove(side);
        if (pendingReplacement.isEmpty()) {
            beginTurn(current);
        }
        return log.drain();
    }

    /**
     * Ends the match if the side that is thinking ran out of time.
     *
     * @return the events, empty if nobody timed out
     */
    public List<BattleEvent> checkTimeout() {
        if (phase == Phase.WAITING_ACTION && clock.isExpired(current)) {
            log.message(players.get(current).getName() + " ran out of time!");
            finish(current.other() == Side.PLAYER ? Outcome.PLAYER_WIN : Outcome.OPPONENT_WIN, EndReason.TIMEOUT);
        }
        return log.drain();
    }

    /**
     * A side gives up.
     *
     * @param side the side that gives up
     * @return the events
     */
    public List<BattleEvent> forfeit(Side side) {
        if (phase != Phase.FINISHED) {
            log.message(players.get(side).getName() + " gives up");
            finish(side == Side.PLAYER ? Outcome.OPPONENT_WIN : Outcome.PLAYER_WIN, EndReason.FORFEIT);
        }
        return log.drain();
    }

    /** Pauses the turn clock without charging time (used while the GUI plays an animation). */
    public void suspendClock() {
        clock.discard();
    }

    /** Restarts the turn clock of the side to act, after an animation finished. */
    public void resumeClock() {
        if (phase == Phase.WAITING_ACTION && players.get(current).isTimed()) {
            clock.startFresh(current);
        }
    }

    // ------------------------------------------------------------------ internals

    private void beginTurn(Side side) {
        current = side;
        phase = Phase.WAITING_ACTION;
        if (players.get(side).isTimed()) {
            clock.start(side);
        } else {
            clock.stop();
        }
    }

    private void sendOut(Side side, int index) {
        activeIndex.put(side, index);
        Combatant dragon = teams.get(side).get(index);
        log.add(EventType.SWITCH_IN, side, players.get(side).getName() + " sends out " + dragon.getName() + "!", index);
        dragon.getPassive().onSwitchIn(dragon, log);
    }

    /** Resolves an attack. @return true if the terrain was shifted by an Ultimate. */
    private boolean attack(Combatant me, Combatant foe, MoveKind kind) {
        Move move = me.getSpecies().getMove(kind);
        if (kind == MoveKind.SIGNATURE) {
            me.spendShot();
        }
        if (kind == MoveKind.ULTIMATE) {
            me.resetCharge();
        } else {
            me.gainCharge();
        }
        log.message(me.getName() + " uses " + move.getName() + "!");

        AttackContext ctx = new AttackContext(me, foe, move, field, log, random);
        List<MoveEffect> effects = new ArrayList<>(move.getEffects());
        effects.addAll(me.getPassive().extraEffects(kind));
        effects.forEach(e -> e.modifyAttack(ctx));
        effects.forEach(e -> e.onUse(ctx));

        if (move.getPower() > 0) {
            resolveDamage(ctx, effects, kind);
        }
        me.getPassive().onMoveUsed(me, kind, log);

        if (kind == MoveKind.ULTIMATE && move.isElemental()) {
            BattlefieldType home = BattlefieldType.homeOf(me.getDragonClass());
            field.shiftTo(home);
            log.add(EventType.FIELD, null,
                    "The field changes to " + home.getDisplayName() + " for " + GameRules.TERRAIN_SHIFT_TURNS + " turns", 0);
            return true;
        }
        return false;
    }

    private void resolveDamage(AttackContext ctx, List<MoveEffect> effects, MoveKind kind) {
        Combatant me = ctx.getAttacker();
        Combatant foe = ctx.getDefender();
        Move move = ctx.getMove();

        if (!ctx.isSureHit() && !me.getPassive().attacksIgnoreDodge()) {
            double speedDodge = DamageCalculator.speedDodgePercent(
                    me.effectiveStat(Stat.SPD, foe, field), foe.effectiveStat(Stat.SPD, me, field));
            double chance = DamageCalculator.totalDodgePercent(speedDodge, foe.getDodgeBonus());
            if (random.nextDouble() * 100.0 < chance) {
                ctx.setDodged(true);
                log.dodge(foe);
                return;
            }
        }

        double typeMultiplier = move.isElemental()
                ? TypeChart.multiplier(me.getDragonClass(), foe.getDragonClass()) : 1.0;
        if (typeMultiplier > 1.0) {
            log.message("Type advantage! Damage increased");
        } else if (typeMultiplier < 1.0) {
            log.message("Type disadvantage... Damage reduced");
        }

        int hits = ctx.getHits();
        for (int i = 0; i < hits && !foe.isFainted() && !me.isFainted(); i++) {
            double attack = me.effectiveStat(Stat.ATK, foe, field);
            double defense = foe.effectiveStat(Stat.DEF, me, field) * (1.0 - ctx.getPierce());
            double critChance = me.getSpecies().getCritRate()
                    + me.getPassive().critChanceBonus(kind) + ctx.getCritBonus();
            boolean crit = ctx.isForcedCrit() || random.nextDouble() < critChance;

            double power = move.getPower() * (hits > 1 ? 1.2 / hits : 1.0);
            double multiplier = typeMultiplier
                    * me.getPassive().outgoingDamageMultiplier(kind)
                    * foe.getPassive().incomingDamageMultiplier(kind, crit)
                    * (crit ? GameRules.CRIT_MULTIPLIER : 1.0);
            int damage = DamageCalculator.toDamage(DamageCalculator.baseDamage(attack, defense) * power * multiplier);

            int dealt = foe.takeDamage(damage);
            ctx.addDamage(dealt);
            if (crit) {
                ctx.markCrit();
            }
            log.damage(foe, dealt, crit);
            foe.gainCharge();
            foe.getPassive().onAttacked(foe, me, dealt, kind, log);
        }
        if (!foe.isFainted()) {
            effects.forEach(e -> e.afterHit(ctx));
        }
    }

    private void resolveKnockouts(Side actor) {
        Set<Side> fainted = EnumSet.noneOf(Side.class);
        for (Side side : Side.values()) {
            Combatant active = getActive(side);
            if (active.isFainted()) {
                fainted.add(side);
                log.add(EventType.FAINT, side, active.getName() + " fainted!", 0);
            }
        }
        boolean playerOut = remaining(Side.PLAYER) == 0;
        boolean opponentOut = remaining(Side.OPPONENT) == 0;
        if (playerOut || opponentOut) {
            Outcome result = playerOut && opponentOut ? Outcome.DRAW
                    : playerOut ? Outcome.OPPONENT_WIN : Outcome.PLAYER_WIN;
            finish(result, EndReason.KNOCKOUT);
            return;
        }
        if (actionCount >= GameRules.MAX_ACTIONS) {
            log.message("Turn limit reached. The winner is decided by remaining HP");
            double playerHp = hpRatio(Side.PLAYER);
            double opponentHp = hpRatio(Side.OPPONENT);
            Outcome result = Math.abs(playerHp - opponentHp) < 1e-9 ? Outcome.DRAW
                    : playerHp > opponentHp ? Outcome.PLAYER_WIN : Outcome.OPPONENT_WIN;
            finish(result, EndReason.TURN_LIMIT);
            return;
        }
        current = actor.other();
        if (fainted.isEmpty()) {
            beginTurn(current);
        } else {
            phase = Phase.WAITING_REPLACEMENT;
            pendingReplacement.addAll(fainted);
            clock.stop();
        }
    }

    private double hpRatio(Side side) {
        return teams.get(side).stream().mapToDouble(c -> (double) c.getCurrentHp() / c.getMaxHp()).sum();
    }

    private void finish(Outcome result, EndReason reason) {
        clock.stop();
        phase = Phase.FINISHED;
        outcome = result;
        endReason = reason;
        pendingReplacement.clear();
        String text = switch (result) {
            case PLAYER_WIN -> "The player wins!";
            case OPPONENT_WIN -> "The player is defeated";
            case DRAW -> "It's a draw!";
        };
        log.add(EventType.END, null, text, 0);
    }
}
