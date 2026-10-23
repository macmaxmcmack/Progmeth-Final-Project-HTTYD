package httyd.data;

import httyd.model.dragon.Nature;
import httyd.model.dragon.OwnedDragon;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Saves and loads the {@link Profile} as a small text file. The file lives in the user's home
 * folder, so the game can run from any folder.
 */
public class SaveManager {
    private final Path file;
    private final DragonRepository repository;

    /**
     * @param file       the save file
     * @param repository the species database used to restore dragons
     */
    public SaveManager(Path file, DragonRepository repository) {
        this.file = file;
        this.repository = repository;
    }

    /**
     * @return the default save location {@code ~/.httyd-battle/save.txt}
     */
    public static Path defaultFile() {
        return Path.of(System.getProperty("user.home"), ".httyd-battle", "save.txt");
    }

    /**
     * Writes the profile.
     *
     * @param profile the profile
     * @throws IOException if the file cannot be written
     */
    public void save(Profile profile) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("coins=" + profile.getCoins());
        lines.add("next=" + profile.getNextId());
        lines.add("record=" + profile.getWins() + "," + profile.getLosses() + "," + profile.getDraws());
        for (OwnedDragon d : profile.getDragons()) {
            lines.add("dragon=" + d.toSaveString());
        }
        Files.createDirectories(file.getParent());
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    /**
     * Reads the profile.
     *
     * @return the profile, or empty if there is no usable save file
     */
    public Optional<Profile> load() {
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            Profile profile = new Profile();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                int eq = line.indexOf('=');
                if (eq < 0) {
                    continue;
                }
                String key = line.substring(0, eq);
                String value = line.substring(eq + 1);
                switch (key) {
                    case "coins" -> profile.setCoins(Integer.parseInt(value));
                    case "next" -> profile.setNextId(Integer.parseInt(value));
                    case "record" -> {
                        String[] r = value.split(",");
                        profile.setRecord(Integer.parseInt(r[0]), Integer.parseInt(r[1]), Integer.parseInt(r[2]));
                    }
                    case "dragon" -> profile.add(parseDragon(value));
                    default -> { }
                }
            }
            return profile.getDragons().isEmpty() ? Optional.empty() : Optional.of(profile);
        } catch (IOException | RuntimeException e) {
            return Optional.empty();
        }
    }

    private OwnedDragon parseDragon(String text) {
        String[] p = text.split(",");
        return new OwnedDragon(Integer.parseInt(p[0]), repository.byId(p[1]),
                Integer.parseInt(p[2]), Integer.parseInt(p[3]), Integer.parseInt(p[4]),
                Integer.parseInt(p[5]), Nature.valueOf(p[6]), p[7].equals("1"), Integer.parseInt(p[8]));
    }
}
