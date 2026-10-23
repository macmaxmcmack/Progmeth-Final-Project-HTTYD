package httyd.data;

import httyd.model.GameRules;
import httyd.model.dragon.DragonClass;
import httyd.model.dragon.DragonSpecies;
import httyd.model.dragon.Tier;
import httyd.model.skill.EffectFactory;
import httyd.model.skill.Move;
import httyd.model.skill.MoveKind;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * The database of all dragon species, loaded from {@code /data/dragons.csv} inside the jar.
 * Reading from the classpath (not from a file path) is what lets the exported jar run anywhere.
 */
public class DragonRepository {
    private static final String DEFAULT_RESOURCE = "/data/dragons.csv";
    private static final int COLUMNS = 23;

    private final Map<String, DragonSpecies> species = new LinkedHashMap<>();

    private DragonRepository() {
    }

    /**
     * Loads the default database from the classpath.
     *
     * @return the repository
     * @throws IllegalStateException if the resource is missing or malformed
     */
    public static DragonRepository loadDefault() {
        try (InputStream in = DragonRepository.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("missing resource " + DEFAULT_RESOURCE);
            }
            return load(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Loads a database from any reader (used by tests).
     *
     * @param reader the CSV text with a header line, columns separated by {@code |}
     * @return the repository
     * @throws IOException              if reading fails
     * @throws IllegalArgumentException if a line is malformed
     */
    public static DragonRepository load(BufferedReader reader) throws IOException {
        DragonRepository repo = new DragonRepository();
        String line = reader.readLine();
        int lineNumber = 1;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] c = line.split("\\|", -1);
            if (c.length != COLUMNS) {
                throw new IllegalArgumentException("line " + lineNumber + ": expected " + COLUMNS
                        + " columns but found " + c.length);
            }
            DragonSpecies s = parse(c);
            repo.species.put(s.getId(), s);
        }
        return repo;
    }

    private static DragonSpecies parse(String[] c) {
        Move typeless = new Move(c[10], "A physical attack with no element", MoveKind.TYPELESS,
                GameRules.TYPELESS_POWER, false, List.of());
        Move signature = new Move(c[11], c[12], MoveKind.SIGNATURE, Double.parseDouble(c[13]),
                true, EffectFactory.parseList(c[14]));
        Move ultimate = new Move(c[15], c[16], MoveKind.ULTIMATE, Double.parseDouble(c[17]),
                c[19].equals("1"), EffectFactory.parseList(c[18]));
        return new DragonSpecies(c[0], c[2], DragonClass.parse(c[1]), Tier.valueOf(c[3]),
                Integer.parseInt(c[4]), Integer.parseInt(c[5]), Integer.parseInt(c[6]),
                Integer.parseInt(c[7]), Double.parseDouble(c[8]), Integer.parseInt(c[9]),
                typeless, signature, ultimate, c[20], c[21], c[22]);
    }

    /** @return all species in database order */
    public List<DragonSpecies> all() {
        return List.copyOf(species.values());
    }

    /** @return the number of species */
    public int size() {
        return species.size();
    }

    /**
     * Finds a species by id.
     *
     * @param id the id, e.g. "toothless"
     * @return the species
     * @throws NoSuchElementException if there is no such species
     */
    public DragonSpecies byId(String id) {
        DragonSpecies s = species.get(id);
        if (s == null) {
            throw new NoSuchElementException("unknown dragon: " + id);
        }
        return s;
    }

    /**
     * @param dragonClass the class
     * @return all species of that class
     */
    public List<DragonSpecies> byClass(DragonClass dragonClass) {
        List<DragonSpecies> result = new ArrayList<>();
        for (DragonSpecies s : species.values()) {
            if (s.getDragonClass() == dragonClass) {
                result.add(s);
            }
        }
        return result;
    }
}
