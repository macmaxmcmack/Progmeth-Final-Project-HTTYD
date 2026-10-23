package httyd.bot;

import httyd.battle.BattleAction;
import httyd.battle.BattleEngine;
import httyd.battle.Combatant;
import httyd.battle.Side;
import httyd.model.skill.MoveKind;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods shared by the bot strategies.
 */
final class BotSupport {

    private BotSupport() {
    }

    /** @return all legal attack and rest actions (no items) */
    static List<BattleAction> legalBasicActions(BattleEngine engine, Side me) {
        List<BattleAction> result = new ArrayList<>();
        for (MoveKind kind : MoveKind.values()) {
            BattleAction action = new BattleAction.Attack(kind);
            if (engine.canPerform(me, action)) {
                result.add(action);
            }
        }
        BattleAction rest = new BattleAction.Rest();
        if (engine.canPerform(me, rest)) {
            result.add(rest);
        }
        return result;
    }

    /** @return the index of the first healthy dragon, or -1 */
    static int firstHealthy(BattleEngine engine, Side me) {
        List<Combatant> team = engine.getTeam(me);
        for (int i = 0; i < team.size(); i++) {
            if (!team.get(i).isFainted()) {
                return i;
            }
        }
        return -1;
    }
}
