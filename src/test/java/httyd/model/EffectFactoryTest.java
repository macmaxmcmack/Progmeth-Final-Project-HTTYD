package httyd.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import httyd.model.skill.DotEffect;
import httyd.model.skill.EffectFactory;
import httyd.model.skill.HealSelfEffect;
import httyd.model.skill.MoveEffect;
import httyd.model.skill.PierceEffect;
import httyd.model.skill.StatDebuffEffect;
import java.util.List;
import org.junit.jupiter.api.Test;

class EffectFactoryTest {

    @Test
    void noneAndBlankGiveEmptyList() {
        assertTrue(EffectFactory.parseList("NONE").isEmpty());
        assertTrue(EffectFactory.parseList("").isEmpty());
        assertTrue(EffectFactory.parseList(null).isEmpty());
    }

    @Test
    void plusSeparatesSeveralEffects() {
        List<MoveEffect> list = EffectFactory.parseList("PIERCE:20+SELF_FLAT:SPD:10");
        assertEquals(2, list.size());
        assertTrue(list.get(0) instanceof PierceEffect);
    }

    @Test
    void everyCodeIsRecognised() {
        String all = "PIERCE:10+CRIT+CRITBONUS:50+HITS:3+SUREHIT+BURN:5:2+POISON:8:2+ATK_DOWN:15+DEF_DOWN:20"
                + "+SPD_DOWN:25+SELF_FLAT:ATK:5+SELF_PCT:DEF:10:2+HEAL:25+DODGE:25:2";
        assertEquals(14, EffectFactory.parseList(all).size());
    }

    @Test
    void typesMatchTheCode() {
        assertTrue(EffectFactory.parseList("BURN:6:2").get(0) instanceof DotEffect);
        assertTrue(EffectFactory.parseList("SPD_DOWN:25").get(0) instanceof StatDebuffEffect);
        assertTrue(EffectFactory.parseList("HEAL:25").get(0) instanceof HealSelfEffect);
    }

    @Test
    void unknownCodeThrows() {
        assertThrows(IllegalArgumentException.class, () -> EffectFactory.parseList("EXPLODE:100"));
    }
}
