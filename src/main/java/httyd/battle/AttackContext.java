package httyd.battle;

import httyd.model.field.Battlefield;
import httyd.model.skill.Move;
import java.util.Random;

/**
 * All information about one attack in progress. Move effects read and change it, which lets a
 * Pierce or Critical effect modify the damage without the engine knowing the effect.
 */
public class AttackContext {
    private final Combatant attacker;
    private final Combatant defender;
    private final Move move;
    private final Battlefield field;
    private final BattleLog log;
    private final Random random;

    private double pierce;
    private double critBonus;
    private boolean forcedCrit;
    private boolean sureHit;
    private int hits = 1;

    private boolean dodged;
    private int totalDamage;
    private boolean anyCrit;

    /**
     * @param attacker the attacking dragon
     * @param defender the defending dragon
     * @param move     the move being used
     * @param field    the battlefield
     * @param log      the battle log
     * @param random   the random source
     */
    public AttackContext(Combatant attacker, Combatant defender, Move move,
                         Battlefield field, BattleLog log, Random random) {
        this.attacker = attacker;
        this.defender = defender;
        this.move = move;
        this.field = field;
        this.log = log;
        this.random = random;
    }

    /** @return the attacking dragon */
    public Combatant getAttacker() {
        return attacker;
    }

    /** @return the defending dragon */
    public Combatant getDefender() {
        return defender;
    }

    /** @return the move */
    public Move getMove() {
        return move;
    }

    /** @return the battlefield */
    public Battlefield getField() {
        return field;
    }

    /** @return the battle log */
    public BattleLog getLog() {
        return log;
    }

    /** @return the random source */
    public Random getRandom() {
        return random;
    }

    /** @return the fraction of DEF that is ignored */
    public double getPierce() {
        return pierce;
    }

    /** @param pierce the fraction of DEF to ignore, from 0 to 1 */
    public void setPierce(double pierce) {
        this.pierce = Math.min(1.0, Math.max(0.0, pierce));
    }

    /** @return extra critical chance from 0 to 1 */
    public double getCritBonus() {
        return critBonus;
    }

    /** @param critBonus extra critical chance from 0 to 1 */
    public void setCritBonus(double critBonus) {
        this.critBonus = critBonus;
    }

    /** @return true if every hit is critical */
    public boolean isForcedCrit() {
        return forcedCrit;
    }

    /** @param forcedCrit true to make every hit critical */
    public void setForcedCrit(boolean forcedCrit) {
        this.forcedCrit = forcedCrit;
    }

    /** @return true if the move cannot be dodged */
    public boolean isSureHit() {
        return sureHit;
    }

    /** @param sureHit true to make the move undodgeable */
    public void setSureHit(boolean sureHit) {
        this.sureHit = sureHit;
    }

    /** @return the number of hits */
    public int getHits() {
        return hits;
    }

    /** @param hits the number of hits, at least 1 */
    public void setHits(int hits) {
        this.hits = Math.max(1, hits);
    }

    /** @return true if the defender dodged */
    public boolean isDodged() {
        return dodged;
    }

    void setDodged(boolean dodged) {
        this.dodged = dodged;
    }

    /** @return the total damage that was dealt */
    public int getTotalDamage() {
        return totalDamage;
    }

    void addDamage(int amount) {
        totalDamage += amount;
    }

    /** @return true if at least one hit was critical */
    public boolean isAnyCrit() {
        return anyCrit;
    }

    void markCrit() {
        anyCrit = true;
    }
}
