package httyd.support;

import httyd.model.dragon.DragonClass;
import httyd.model.dragon.DragonSpecies;
import httyd.model.dragon.Nature;
import httyd.model.dragon.OwnedDragon;
import httyd.model.dragon.Tier;
import httyd.model.item.Item;
import httyd.model.player.Player;
import httyd.model.skill.EffectFactory;
import httyd.model.skill.Move;
import httyd.model.skill.MoveKind;
import java.util.List;

/** Small factory methods that build simple dragons and players for the tests. */
public final class TestData {

    private TestData() {
    }

    /** Builds a species with plain moves: Signature power 1.4, Ultimate power 2.0. */
    public static DragonSpecies species(String id, DragonClass cls, int hp, int atk, int def, int spd,
                                        double crit, int shots, String sigFx, String ultFx, String passive) {
        Move typeless = new Move("Bite", "", MoveKind.TYPELESS, 1.0, false, List.of());
        Move signature = new Move("Flame", "", MoveKind.SIGNATURE, 1.4, true, EffectFactory.parseList(sigFx));
        Move ultimate = new Move("Ultimate", "", MoveKind.ULTIMATE, 2.0, true, EffectFactory.parseList(ultFx));
        return new DragonSpecies(id, id, cls, Tier.A, hp, atk, def, spd, crit, shots,
                typeless, signature, ultimate, "Test passive", "", passive);
    }

    /** Builds a plain dragon: Atk 100, Def 100, Spd 100, HP 100 (300 in battle), no crit. */
    public static DragonSpecies basic(String id, DragonClass cls) {
        return species(id, cls, 100, 100, 100, 100, 0.0, 3, "NONE", "NONE", "NONE");
    }

    /** Wraps a species into a neutral owned dragon (no IV, calm nature, 1 star). */
    public static OwnedDragon owned(int id, DragonSpecies species) {
        return new OwnedDragon(id, species, 0, 0, 0, 0, Nature.CALM, false, 1);
    }

    /** Builds a player whose three dragons are the given species. */
    public static Player player(String name, DragonSpecies a, DragonSpecies b, DragonSpecies c, Item... items) {
        return new Player(name, List.of(owned(1, a), owned(2, b), owned(3, c)), List.of(items));
    }

    /** Builds a player with three copies of one species. */
    public static Player player(String name, DragonSpecies s, Item... items) {
        return player(name, s, s, s, items);
    }
}
