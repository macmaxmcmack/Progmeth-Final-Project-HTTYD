package httyd.gui;

import httyd.data.DragonRepository;
import httyd.data.GachaService;
import httyd.data.Profile;
import httyd.data.SaveManager;
import java.io.IOException;
import java.util.Random;

/**
 * The objects shared by all screens: the dragon database, the player's profile, saving, the egg
 * service and the sound player.
 */
public class GameContext {
    private final DragonRepository repository;
    private final Profile profile;
    private final SaveManager saveManager;
    private final GachaService gacha;
    private final SoundPlayer sound;
    private final Random random;

    /**
     * @param repository  the dragon database
     * @param profile     the player's profile
     * @param saveManager where the profile is saved
     * @param gacha       the egg service
     * @param sound       the sound player
     * @param random      the random source
     */
    public GameContext(DragonRepository repository, Profile profile, SaveManager saveManager,
                       GachaService gacha, SoundPlayer sound, Random random) {
        this.repository = repository;
        this.profile = profile;
        this.saveManager = saveManager;
        this.gacha = gacha;
        this.sound = sound;
        this.random = random;
    }

    /**
     * Creates the normal game context: loads the save file or starts a new profile.
     *
     * @return the context
     */
    public static GameContext createDefault() {
        DragonRepository repository = DragonRepository.loadDefault();
        Random random = new Random();
        GachaService gacha = new GachaService(repository, random);
        SaveManager saveManager = new SaveManager(SaveManager.defaultFile(), repository);
        Profile profile = saveManager.load().orElseGet(() -> Profile.createNew(gacha));
        GameContext context = new GameContext(repository, profile, saveManager, gacha, new SoundPlayer(), random);
        context.save();
        return context;
    }

    /** @return the dragon database */
    public DragonRepository getRepository() {
        return repository;
    }

    /** @return the player's profile */
    public Profile getProfile() {
        return profile;
    }

    /** @return the egg service */
    public GachaService getGacha() {
        return gacha;
    }

    /** @return the sound player */
    public SoundPlayer getSound() {
        return sound;
    }

    /** @return the random source */
    public Random getRandom() {
        return random;
    }

    /** Saves the profile. A failure is ignored because the game can go on without saving. */
    public void save() {
        try {
            saveManager.save(profile);
        } catch (IOException e) {
            System.err.println("Could not save: " + e.getMessage());
        }
    }
}
