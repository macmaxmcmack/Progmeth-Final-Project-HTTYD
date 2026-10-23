package httyd.data;

/**
 * The packs offered by the demo coin shop. Every pack is free: no real money is ever involved.
 */
public enum CoinPack {
    POUCH("Coin Pouch", 100),
    BAG("Coin Bag", 500),
    CHEST("Treasure Chest", 1_000),
    HOARD("Dragon Hoard", 5_000);

    private final String displayName;
    private final int coins;

    CoinPack(String displayName, int coins) {
        this.displayName = displayName;
        this.coins = coins;
    }

    /** @return the name shown on the card */
    public String getDisplayName() {
        return displayName;
    }

    /** @return how many coins the pack gives */
    public int getCoins() {
        return coins;
    }

    /**
     * Gives this pack to a player.
     *
     * @param profile the player's profile
     * @return the coins that were added
     */
    public int claim(Profile profile) {
        return profile.addCoins(coins);
    }
}
