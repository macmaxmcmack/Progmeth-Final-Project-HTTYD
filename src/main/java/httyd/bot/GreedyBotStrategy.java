package httyd.bot;

import httyd.battle.BattleAction;
import httyd.battle.BattleEngine;
import httyd.battle.Combatant;
import httyd.battle.Side;
import httyd.battle.TypeChart;
import httyd.model.skill.MoveKind;
import java.util.List;
import java.util.Random;

/**
 * The normal bot: uses the Ultimate as soon as it is ready, prefers Signature moves when it has
 * shots, and rests when it has none.
 */
public class GreedyBotStrategy implements BotStrategy {

    @Override
    public String getName() {
        return "Fighter Bot";
    }

    @Override
    public BattleAction chooseAction(BattleEngine engine, Side me, Random random) {
        BattleAction ultimate = new BattleAction.Attack(MoveKind.ULTIMATE);
        if (engine.canPerform(me, ultimate)) {
            return ultimate;
        }
        BattleAction signature = new BattleAction.Attack(MoveKind.SIGNATURE);
        if (engine.canPerform(me, signature)) {
            return signature;
        }
        BattleAction rest = new BattleAction.Rest();
        if (engine.canPerform(me, rest) && random.nextInt(100) < 25) {
            return rest;
        }
        return new BattleAction.Attack(MoveKind.TYPELESS);
    }

    @Override
    public int chooseReplacement(BattleEngine engine, Side me, Random random) {
        Combatant foe = engine.getActive(me.other());
        int best = -1;
        double bestScore = -1;
        List<Combatant> team = engine.getTeam(me);
        for (int i = 0; i < team.size(); i++) {
            if (team.get(i).isFainted()) {
                continue;
            }
            double score = TypeChart.multiplier(team.get(i).getDragonClass(), foe.getDragonClass());
            if (score > bestScore) {
                bestScore = score;
                best = i;
            }
        }
        return best;
    }
}
