package httyd.data;

import httyd.battle.BattleEngine;
import httyd.model.GameRules;
import httyd.model.dragon.OwnedDragon;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Everything the player owns: coins, the dragon collection and the win/loss record.
 */
public class Profile {
    /** Coins given to a new player. */
    public static final int STARTING_COINS = 100;
    /** Number of free starter dragons. */
    public static final int STARTER_DRAGONS = 3;
    private static final int WIN_REWARD = 60;
    private static final int DRAW_REWARD = 25;
    private static final int LOSS_REWARD = 20;

    private int coins;
    private int nextId;
    private int wins;
    private int losses;
    private int draws;
    private final List<OwnedDragon> dragons = new ArrayList<>();

    /** Creates an empty profile; use {@link #createNew} for a new player. */
    public Profile() {
        this.nextId = 1;
    }

    /**
     * Creates a new player's profile with starter coins and three free dragons.
     *
     * @param gacha the egg service
     * @return the profile
     */
    public static Profile createNew(GachaService gacha) {
        Profile profile = new Profile();
        profile.coins = STARTING_COINS;
        for (int i = 0; i < STARTER_DRAGONS; i++) {
            profile.add(gacha.rollDragon(profile.nextId++));
        }
        return profile;
    }

    /** @return the coins */
    public int getCoins() {
        return coins;
    }

    /** @param coins the new amount, not negative */
    public void setCoins(int coins) {
        this.coins = Math.max(0, coins);
    }

    /**
     * Adds coins to the balance (used by the free demo top-up). The balance never goes above
     * {@link GameRules#MAX_COINS}.
     *
     * @param amount the coins to add, greater than zero
     * @return the coins that were really added (less than {@code amount} if the cap was reached)
     * @throws IllegalArgumentException if {@code amount} is not positive
     */
    public int addCoins(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        int before = coins;
        coins = (int) Math.min((long) GameRules.MAX_COINS, (long) coins + amount);
        return coins - before;
    }

    /** @return number of wins */
    public int getWins() {
        return wins;
    }

    /** @return number of losses */
    public int getLosses() {
        return losses;
    }

    /** @return number of draws */
    public int getDraws() {
        return draws;
    }

    /**
     * Restores the win/loss record from a save file.
     *
     * @param wins   wins
     * @param losses losses
     * @param draws  draws
     */
    public void setRecord(int wins, int losses, int draws) {
        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
    }

    /** @return the next free dragon id */
    public int getNextId() {
        return nextId;
    }

    /** @param nextId the next free dragon id (used when loading) */
    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    /** @return an unmodifiable view of the collection */
    public List<OwnedDragon> getDragons() {
        return List.copyOf(dragons);
    }

    /**
     * Adds a dragon to the collection.
     *
     * @param dragon the dragon
     */
    public void add(OwnedDragon dragon) {
        dragons.add(dragon);
        nextId = Math.max(nextId, dragon.getId() + 1);
    }

    /**
     * Finds a dragon by id.
     *
     * @param id the dragon id
     * @return the dragon if it is in the collection
     */
    public Optional<OwnedDragon> find(int id) {
        return dragons.stream().filter(d -> d.getId() == id).findFirst();
    }

    /** @return true if the player can pay for an egg */
    public boolean canAffordEgg() {
        return coins >= GameRules.EGG_PRICE;
    }

    /**
     * Pays for an egg and adds the dragon that hatches.
     *
     * @param gacha the egg service
     * @return the new dragon
     * @throws IllegalStateException if the player has too few coins
     */
    public OwnedDragon buyEgg(GachaService gacha) {
        if (!canAffordEgg()) {
            throw new IllegalStateException("not enough coins");
        }
        coins -= GameRules.EGG_PRICE;
        OwnedDragon dragon = gacha.rollDragon(nextId++);
        dragons.add(dragon);
        return dragon;
    }

    /**
     * Checks whether a duplicate can be fused into a dragon.
     *
     * @param targetId the dragon that gets the star
     * @param fodderId the duplicate that is consumed
     * @return true if both exist, are different dragons of the same species and the target is
     *         below the maximum star level
     */
    public boolean canFuse(int targetId, int fodderId) {
        Optional<OwnedDragon> target = find(targetId);
        Optional<OwnedDragon> fodder = find(fodderId);
        return targetId != fodderId && target.isPresent() && fodder.isPresent()
                && target.get().getSpecies() == fodder.get().getSpecies()
                && target.get().getStar() < GameRules.MAX_STAR;
    }

    /**
     * Fuses a duplicate into another dragon of the same species: the duplicate disappears and
     * the target gains one star.
     *
     * @param targetId the dragon that gets the star
     * @param fodderId the duplicate that is consumed
     * @throws IllegalStateException if the fusion is not allowed
     */
    public void fuse(int targetId, int fodderId) {
        if (!canFuse(targetId, fodderId)) {
            throw new IllegalStateException("these dragons cannot be fused");
        }
        find(targetId).orElseThrow().starUp();
        dragons.removeIf(d -> d.getId() == fodderId);
    }

    /**
     * Records the result of a match and pays the coin reward.
     *
     * @param outcome the outcome from the player's point of view
     * @return the coins earned
     */
    public int recordResult(BattleEngine.Outcome outcome) {
        int reward;
        switch (outcome) {
            case PLAYER_WIN -> {
                wins++;
                reward = WIN_REWARD;
            }
            case OPPONENT_WIN -> {
                losses++;
                reward = LOSS_REWARD;
            }
            default -> {
                draws++;
                reward = DRAW_REWARD;
            }
        }
        coins += reward;
        return reward;
    }
}
