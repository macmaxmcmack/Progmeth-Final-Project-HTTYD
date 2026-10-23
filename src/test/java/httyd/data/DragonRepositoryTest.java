package httyd.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import httyd.model.dragon.DragonClass;
import httyd.model.dragon.DragonSpecies;
import httyd.model.dragon.Stat;
import httyd.model.skill.MoveKind;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DragonRepositoryTest {
    private DragonRepository repo;

    @BeforeEach
    void setUp() {
        repo = DragonRepository.loadDefault();
    }

    @Test
    void containsTwentyEightDragons() {
        assertEquals(28, repo.size());
    }

    @Test
    void everyClassHasFourDragons() {
        for (DragonClass c : DragonClass.values()) {
            assertEquals(4, repo.byClass(c).size(), c.name());
        }
    }

    @Test
    void idsAreUnique() {
        Set<String> ids = new HashSet<>();
        for (DragonSpecies s : repo.all()) {
            assertTrue(ids.add(s.getId()), "duplicate " + s.getId());
        }
    }

    @Test
    void statsAreInTheDesignedRange() {
        for (DragonSpecies s : repo.all()) {
            assertTrue(s.getBase(Stat.HP) >= 60 && s.getBase(Stat.HP) <= 99, s.getName() + " hp");
            assertTrue(s.getBase(Stat.ATK) >= 60 && s.getBase(Stat.ATK) <= 99, s.getName() + " atk");
            assertTrue(s.getBase(Stat.DEF) >= 60 && s.getBase(Stat.DEF) <= 99, s.getName() + " def");
            assertTrue(s.getBase(Stat.SPD) >= 60 && s.getBase(Stat.SPD) <= 99, s.getName() + " spd");
            assertTrue(s.getCritRate() > 0 && s.getCritRate() < 0.5, s.getName() + " crit");
            assertTrue(s.getShotLimit() > 0, s.getName() + " shots");
        }
    }

    @Test
    void everyDragonHasThreeMovesAndAWorkingPassive() {
        for (DragonSpecies s : repo.all()) {
            for (MoveKind kind : MoveKind.values()) {
                assertFalse(s.getMove(kind).getName().isBlank(), s.getName() + " " + kind);
            }
            assertEquals(MoveKind.ULTIMATE, s.getMove(MoveKind.ULTIMATE).getKind());
            assertFalse(s.createPassive(1.0).getName().isBlank());
            assertFalse(s.getPassiveDescription().isBlank());
        }
    }

    @Test
    void toothlessMatchesTheDatabase() {
        DragonSpecies t = repo.byId("toothless");
        assertEquals(DragonClass.STRIKE, t.getDragonClass());
        assertEquals(85, t.getBase(Stat.HP));
        assertEquals(95, t.getBase(Stat.ATK));
        assertEquals(98, t.getBase(Stat.SPD));
        assertEquals("Plasma Blast", t.getMove(MoveKind.SIGNATURE).getName());
        assertEquals(2, t.getMove(MoveKind.ULTIMATE).getEffects().size());
    }

    @Test
    void shortNameDropsTheBracket() {
        assertEquals("Toothless", repo.byId("toothless").getShortName());
        assertEquals("Skrill", repo.byId("skrill").getShortName());
    }

    @Test
    void unknownIdThrows() {
        assertThrows(NoSuchElementException.class, () -> repo.byId("godzilla"));
    }

    @Test
    void malformedLineIsRejected() {
        BufferedReader reader = new BufferedReader(new StringReader("header\nonly|three|columns\n"));
        assertThrows(IllegalArgumentException.class, () -> DragonRepository.load(reader));
    }

    @Test
    void emptyFileGivesEmptyRepository() throws IOException {
        DragonRepository empty = DragonRepository.load(new BufferedReader(new StringReader("header\n")));
        assertEquals(0, empty.size());
    }
}
