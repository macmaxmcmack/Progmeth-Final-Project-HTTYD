package httyd.battle;

import httyd.model.GameRules;
import httyd.model.dragon.DragonClass;
import httyd.model.dragon.DragonSpecies;
import httyd.model.dragon.OwnedDragon;
import httyd.model.dragon.Stat;
import httyd.model.field.Battlefield;
import httyd.model.skill.PassiveSkill;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A dragon during a battle: the owned dragon plus all temporary state (HP, shots, charge,
 * modifiers, damage-over-time). A new instance is created for every dragon in every match.
 */
public class Combatant {

    /** Remaining damage-over-time status. */
    private static final class Dot {
        private final double fraction;
        private int turnsLeft;

        private Dot(double fraction, int turnsLeft) {
            this.fraction = fraction;
            this.turnsLeft = turnsLeft;
        }
    }

    /** Extra dodge chance that lasts a few turns. */
    private static final class DodgeBonus {
        private final double percent;
        private int turnsLeft;

        private DodgeBonus(double percent, int turnsLeft) {
            this.percent = percent;
            this.turnsLeft = turnsLeft;
        }
    }

    private final Side side;
    private final OwnedDragon source;
    private final PassiveSkill passive;
    private final int maxHp;
    private final int maxShots;
    private int currentHp;
    private int shots;
    private int ultimateCharge;
    private int ownTurns;
    private final List<StatModifier> modifiers = new ArrayList<>();
    private final List<DodgeBonus> dodgeBonuses = new ArrayList<>();
    private final Map<DotType, Dot> dots = new EnumMap<>(DotType.class);

    /**
     * @param side   the side the dragon fights for
     * @param source the owned dragon
     */
    public Combatant(Side side, OwnedDragon source) {
        this.side = side;
        this.source = source;
        this.passive = source.getSpecies().createPassive(source.getPassiveStrength());
        this.maxHp = source.getBattleHp();
        this.currentHp = maxHp;
        this.maxShots = source.getShotLimit();
        this.shots = maxShots;
    }

    /** @return the side */
    public Side getSide() {
        return side;
    }

    /** @return the owned dragon this combatant was created from */
    public OwnedDragon getSource() {
        return source;
    }

    /** @return the species */
    public DragonSpecies getSpecies() {
        return source.getSpecies();
    }

    /** @return the class of the dragon */
    public DragonClass getDragonClass() {
        return source.getSpecies().getDragonClass();
    }

    /** @return the short display name */
    public String getName() {
        return source.getSpecies().getShortName();
    }

    /** @return the passive ability instance */
    public PassiveSkill getPassive() {
        return passive;
    }

    /** @return maximum HP */
    public int getMaxHp() {
        return maxHp;
    }

    /** @return current HP */
    public int getCurrentHp() {
        return currentHp;
    }

    /** @return true if HP is 0 */
    public boolean isFainted() {
        return currentHp <= 0;
    }

    /** @return remaining Signature shots */
    public int getShots() {
        return shots;
    }

    /** @return maximum Signature shots */
    public int getMaxShots() {
        return maxShots;
    }

    /** Uses one Signature shot. */
    public void spendShot() {
        if (shots <= 0) {
            throw new IllegalStateException("no shots left");
        }
        shots--;
    }

    /**
     * Adds shots, never above the maximum.
     *
     * @param amount shots to add
     * @return the number of shots actually added
     */
    public int addShots(int amount) {
        int added = Math.min(amount, maxShots - shots);
        shots += Math.max(0, added);
        return Math.max(0, added);
    }

    /** @return Ultimate charge from 0 to the needed amount */
    public int getUltimateCharge() {
        return ultimateCharge;
    }

    /** @return true if the Ultimate can be used */
    public boolean isUltimateReady() {
        return ultimateCharge >= GameRules.ULTIMATE_CHARGE_NEEDED;
    }

    /** Adds one point of Ultimate charge. */
    public void gainCharge() {
        ultimateCharge = Math.min(GameRules.ULTIMATE_CHARGE_NEEDED, ultimateCharge + 1);
    }

    /** Empties the Ultimate charge. */
    public void resetCharge() {
        ultimateCharge = 0;
    }

    /** @return how many turns this dragon has finished */
    public int getOwnTurns() {
        return ownTurns;
    }

    /** @return the stat after roll, nature and star but before any temporary change */
    public int getBaseStat(Stat stat) {
        return stat == Stat.HP ? maxHp : source.getStat(stat);
    }

    /**
     * Calculates a stat including modifiers, passives, the battlefield and the opponent's aura.
     *
     * @param stat  the stat (not HP)
     * @param foe   the opposing dragon, may be {@code null}
     * @param field the battlefield
     * @return the effective value, at least 1
     */
    public double effectiveStat(Stat stat, Combatant foe, Battlefield field) {
        double flat = passive.flatStatBonus(this, foe, stat);
        double percent = 0;
        for (StatModifier m : modifiers) {
            if (m.getStat() == stat) {
                flat += m.getFlat();
                percent += m.getPercent();
            }
        }
        double value = (getBaseStat(stat) + flat) * Math.max(0.1, 1.0 + percent);
        if (field != null) {
            value *= field.multiplier(getDragonClass(), stat);
        }
        if (foe != null) {
            value *= foe.passive.statMultiplierOnOpponent(stat);
        }
        return Math.max(1.0, value);
    }

    /**
     * Adds a stat modifier. A new timed modifier replaces an older timed one of the same stat
     * and direction, so debuffs never pile up.
     *
     * @param stat        the stat
     * @param percent     percentage fraction (negative for a debuff)
     * @param flat        flat amount
     * @param turns       duration in own turns, or a negative number for permanent
     * @param selfApplied true if the owner applied it in its own turn (lasts a full {@code turns})
     * @return false if the dragon is immune to the debuff
     */
    public boolean addModifier(Stat stat, double percent, int flat, int turns, boolean selfApplied) {
        boolean debuff = percent < 0 || flat < 0;
        if (debuff && passive.immuneToDebuff(stat)) {
            return false;
        }
        int duration = turns;
        if (turns > 0) {
            modifiers.removeIf(m -> m.getStat() == stat && !m.isPermanent() && m.isDebuff() == debuff);
            if (selfApplied) {
                duration = turns + 1;
            }
        }
        modifiers.add(new StatModifier(stat, percent, flat, duration));
        return true;
    }

    /** @return an unmodifiable view of the active modifiers (for the GUI) */
    public List<StatModifier> getModifiers() {
        return List.copyOf(modifiers);
    }

    /**
     * Gives extra dodge chance for a number of own turns.
     *
     * @param percent extra dodge chance in percent
     * @param turns   own turns
     */
    public void addDodgeBonus(double percent, int turns) {
        dodgeBonuses.removeIf(b -> true);
        dodgeBonuses.add(new DodgeBonus(percent, turns + 1));
    }

    /** @return the total dodge bonus from the passive and temporary effects, in percent */
    public double getDodgeBonus() {
        double total = passive.dodgeBonus(this);
        for (DodgeBonus b : dodgeBonuses) {
            total += b.percent;
        }
        return total;
    }

    /**
     * Applies a damage-over-time status. A new status of the same type replaces the old one.
     *
     * @param type     the status type
     * @param fraction damage per turn as a fraction of max HP
     * @param turns    own turns
     */
    public void applyDot(DotType type, double fraction, int turns) {
        dots.put(type, new Dot(fraction, turns));
    }

    /**
     * @param type the status type
     * @return true if the dragon currently has this status
     */
    public boolean hasDot(DotType type) {
        return dots.containsKey(type);
    }

    /** @return true if there is a damage-over-time status or a stat debuff to remove */
    public boolean hasCleansableStatus() {
        return !dots.isEmpty() || modifiers.stream().anyMatch(StatModifier::isDebuff);
    }

    /**
     * Removes all damage-over-time statuses and all stat debuffs.
     *
     * @return true if anything was removed
     */
    public boolean cleanse() {
        boolean any = !dots.isEmpty();
        dots.clear();
        int before = modifiers.size();
        modifiers.removeIf(StatModifier::isDebuff);
        return any || modifiers.size() != before;
    }

    /**
     * Reduces HP. If the damage would be lethal and the passive saves the dragon, 1 HP is left.
     *
     * @param amount damage, must not be negative
     * @return the damage that was really dealt
     */
    public int takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("damage must not be negative");
        }
        int before = currentHp;
        if (amount >= currentHp) {
            if (currentHp > 1 && passive.preventFaint(this)) {
                currentHp = 1;
            } else {
                currentHp = 0;
            }
        } else {
            currentHp -= amount;
        }
        return before - currentHp;
    }

    /**
     * Restores HP up to the maximum.
     *
     * @param amount HP to restore
     * @return the HP actually restored
     */
    public int heal(int amount) {
        if (isFainted()) {
            return 0;
        }
        int healed = Math.min(amount, maxHp - currentHp);
        currentHp += healed;
        return healed;
    }

    /**
     * Finishes the owner's turn: damage-over-time ticks, then temporary effects count down.
     *
     * @param log the battle log
     */
    public void endOwnTurn(BattleLog log) {
        for (Map.Entry<DotType, Dot> entry : new ArrayList<>(dots.entrySet())) {
            if (isFainted()) {
                break;
            }
            Dot dot = entry.getValue();
            int amount = Math.max(1, (int) Math.round(maxHp * dot.fraction));
            int dealt = takeDamage(amount);
            log.add(EventType.DAMAGE, side,
                    getName() + " takes " + dealt + " damage from " + entry.getKey().getDisplayName(), dealt);
            dot.turnsLeft--;
            if (dot.turnsLeft <= 0) {
                dots.remove(entry.getKey());
            }
        }
        modifiers.removeIf(StatModifier::tick);
        dodgeBonuses.removeIf(b -> --b.turnsLeft <= 0);
        ownTurns++;
    }
}
