package httyd.model.field;

import httyd.model.GameRules;
import httyd.model.dragon.DragonClass;
import httyd.model.dragon.Stat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The state of the battlefield during a match: the original terrain and a temporary override
 * created by an elemental Ultimate move.
 */
public class Battlefield {
    private final BattlefieldType originalField;
    private BattlefieldType currentField;
    private int overrideTurnsLeft;

    /**
     * @param original the terrain selected at the start of the match
     */
    public Battlefield(BattlefieldType original) {
        this.originalField = original;
        this.currentField = original;
    }

    /** @return the terrain selected at the start */
    public BattlefieldType getOriginalField() {
        return originalField;
    }

    /** @return the terrain currently in effect */
    public BattlefieldType getCurrentField() {
        return currentField;
    }

    /** @return number of turns left before the terrain returns to the original one */
    public int getOverrideTurnsLeft() {
        return overrideTurnsLeft;
    }

    /**
     * Replaces the terrain for {@link GameRules#TERRAIN_SHIFT_TURNS} turns. A new Ultimate
     * overrides the previous one.
     *
     * @param field the new terrain
     */
    public void shiftTo(BattlefieldType field) {
        currentField = field;
        overrideTurnsLeft = GameRules.TERRAIN_SHIFT_TURNS;
    }

    /**
     * Counts one finished action. When the counter reaches 0 the original terrain returns.
     *
     * @return true if the terrain just returned to the original
     */
    public boolean endAction() {
        if (overrideTurnsLeft <= 0) {
            return false;
        }
        overrideTurnsLeft--;
        if (overrideTurnsLeft == 0) {
            currentField = originalField;
            return true;
        }
        return false;
    }

    /**
     * Returns the multiplier of the current terrain for one stat of a dragon class.
     *
     * @param dragonClass the dragon class
     * @param stat        the stat
     * @return the multiplier
     */
    public double multiplier(DragonClass dragonClass, Stat stat) {
        return currentField.multiplier(dragonClass, stat);
    }

    /**
     * Draws the real battlefield from three candidates.
     *
     * @param playerPicks the fields chosen by the player (two for PvE)
     * @param botPick     the field chosen by the bot
     * @param random      the random source
     * @return one of the three candidates
     */
    public static BattlefieldType draw(List<BattlefieldType> playerPicks, BattlefieldType botPick, Random random) {
        List<BattlefieldType> pool = new ArrayList<>(playerPicks);
        pool.add(botPick);
        return pool.get(random.nextInt(pool.size()));
    }
}
