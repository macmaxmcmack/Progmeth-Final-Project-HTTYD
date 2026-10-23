package httyd.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import httyd.model.dragon.OwnedDragon;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SaveManagerTest {
    private DragonRepository repo;
    private Path dir;

    @BeforeEach
    void setUp() throws IOException {
        repo = DragonRepository.loadDefault();
        dir = Files.createTempDirectory("httyd-test");
    }

    @Test
    void saveThenLoadGivesTheSameProfile() throws IOException {
        Profile original = Profile.createNew(new GachaService(repo, new Random(11)));
        original.setCoins(777);
        original.setRecord(4, 2, 1);
        original.getDragons().get(0).starUp();
        SaveManager manager = new SaveManager(dir.resolve("nested").resolve("save.txt"), repo);
        manager.save(original);

        Profile loaded = manager.load().orElseThrow();
        assertEquals(777, loaded.getCoins());
        assertEquals(4, loaded.getWins());
        assertEquals(2, loaded.getLosses());
        assertEquals(1, loaded.getDraws());
        assertEquals(original.getNextId(), loaded.getNextId());
        assertEquals(original.getDragons().size(), loaded.getDragons().size());
        for (int i = 0; i < original.getDragons().size(); i++) {
            OwnedDragon a = original.getDragons().get(i);
            OwnedDragon b = loaded.getDragons().get(i);
            assertEquals(a.toSaveString(), b.toSaveString());
        }
    }

    @Test
    void missingFileGivesEmpty() {
        assertTrue(new SaveManager(dir.resolve("none.txt"), repo).load().isEmpty());
    }

    @Test
    void corruptFileGivesEmptyInsteadOfCrashing() throws IOException {
        Path file = dir.resolve("bad.txt");
        Files.writeString(file, "coins=abc\ndragon=1,unknown,x\n", StandardCharsets.UTF_8);
        Optional<Profile> loaded = new SaveManager(file, repo).load();
        assertTrue(loaded.isEmpty());
    }
}
