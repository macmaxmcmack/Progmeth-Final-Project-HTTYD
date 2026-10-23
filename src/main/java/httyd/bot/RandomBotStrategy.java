package httyd.bot;

import httyd.battle.BattleAction;
import httyd.battle.BattleEngine;
import httyd.battle.Side;
import httyd.model.skill.MoveKind;
import java.util.List;
import java.util.Random;

/**
 * The easy bot: mostly plain attacks, sometimes a Signature move, and it never plans.
 */
public class RandomBotStrategy implements BotStrategy {

    @Override
    public String getName() {
        return "Rookie Bot";
    }

    @Override
    public BattleAction chooseAction(BattleEngine engine, Side me, Random random) {
        List<BattleAction> options = BotSupport.legalBasicActions(engine, me);
        BattleAction typeless = new BattleAction.Attack(MoveKind.TYPELESS);
        if (random.nextInt(100) < 55) {
            return typeless;
        }
        return options.get(random.nextInt(options.size()));
    }

    @Override
    public int chooseReplacement(BattleEngine engine, Side me, Random random) {
        int tries = 20;
        while (tries-- > 0) {
            int index = random.nextInt(engine.getTeam(me).size());
            if (!engine.getTeam(me).get(index).isFainted()) {
                return index;
            }
        }
        return BotSupport.firstHealthy(engine, me);
    }
}
