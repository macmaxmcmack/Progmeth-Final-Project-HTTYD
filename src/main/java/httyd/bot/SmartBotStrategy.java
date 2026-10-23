package httyd.bot;

import httyd.battle.BattleAction;
import httyd.battle.BattleEngine;
import httyd.battle.Combatant;
import httyd.battle.Side;
import httyd.battle.TypeChart;
import httyd.model.item.Item;
import httyd.model.skill.MoveKind;
import java.util.List;
import java.util.Random;

/**
 * The hard bot: heals when low, cleanses when debuffed, reloads at the right time, saves the
 * Ultimate for a dragon that can be knocked out or is weak to it, and picks the replacement
 * with the best type matchup.
 */
public class SmartBotStrategy implements BotStrategy {
    private final GreedyBotStrategy fallback = new GreedyBotStrategy();

    @Override
    public String getName() {
        return "Tactician Bot";
    }

    @Override
    public BattleAction chooseAction(BattleEngine engine, Side me, Random random) {
        Combatant self = engine.getActive(me);
        Combatant foe = engine.getActive(me.other());
        List<Item> bag = engine.getPlayer(me).getBag();

        for (int i = 0; i < bag.size(); i++) {
            Item item = bag.get(i);
            BattleAction use = new BattleAction.UseItem(i);
            if (!engine.canPerform(me, use)) {
                continue;
            }
            double hpRatio = (double) self.getCurrentHp() / self.getMaxHp();
            String name = item.getName();
            if (name.equals("Fresh Arctic Char") && hpRatio < 0.4) {
                return use;
            }
            if (name.equals("Dragon Nip") && (self.hasCleansableStatus()) && hpRatio < 0.8) {
                return use;
            }
            if (name.equals("Monstrous Fire Gel") && self.getShots() == 0) {
                return use;
            }
            if (name.equals("Dragon Root Extract") && self.isUltimateReady()) {
                return use;
            }
        }

        BattleAction ultimate = new BattleAction.Attack(MoveKind.ULTIMATE);
        if (engine.canPerform(me, ultimate)) {
            double advantage = TypeChart.multiplier(self.getDragonClass(), foe.getDragonClass());
            boolean foeLow = foe.getCurrentHp() < foe.getMaxHp() * 0.5;
            if (advantage >= 1.0 || foeLow || random.nextInt(100) < 40) {
                return ultimate;
            }
        }
        double typeAdvantage = TypeChart.multiplier(self.getDragonClass(), foe.getDragonClass());
        BattleAction signature = new BattleAction.Attack(MoveKind.SIGNATURE);
        if (engine.canPerform(me, signature) && typeAdvantage >= 1.0) {
            return signature;
        }
        return fallback.chooseAction(engine, me, random);
    }

    @Override
    public int chooseReplacement(BattleEngine engine, Side me, Random random) {
        return fallback.chooseReplacement(engine, me, random);
    }
}
