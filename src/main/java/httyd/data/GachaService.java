package httyd.data;

import httyd.model.GameRules;
import httyd.model.dragon.DragonSpecies;
import httyd.model.dragon.Nature;
import httyd.model.dragon.OwnedDragon;
import httyd.model.dragon.Tier;
import java.util.List;
import java.util.Random;

/**
 * The dragon egg: rolls a species by rarity, then the stat deviations (IV), a nature and the
 * chance of an Alpha dragon.
 */
public class GachaService {
    private final DragonRepository repository;
    private final Random random;

    /**
     * @param repository the species database
     * @param random     the random source
     */
    public GachaService(DragonRepository repository, Random random) {
        this.repository = repository;
        this.random = random;
    }

    /**
     * Picks a species. The chance of a species is its tier weight divided by the number of
     * species of that tier, so a tier is as common as its weight says.
     *
     * @param rarityPercent 100 for normal rates; above 100 favours S and A tiers, below 100 favours B and C
     * @return the species
     */
    public DragonSpecies pickSpecies(int rarityPercent) {
        List<DragonSpecies> all = repository.all();
        double[] weights = new double[all.size()];
        double total = 0;
        for (int i = 0; i < all.size(); i++) {
            Tier tier = all.get(i).getTier();
            long sameTier = all.stream().filter(s -> s.getTier() == tier).count();
            double weight = tier.getDropWeight() / (double) sameTier;
            boolean rare = tier == Tier.S || tier == Tier.A;
            weight *= rare ? rarityPercent / 100.0 : 100.0 / rarityPercent;
            weights[i] = weight;
            total += weight;
        }
        double roll = random.nextDouble() * total;
        for (int i = 0; i < weights.length; i++) {
            roll -= weights[i];
            if (roll < 0) {
                return all.get(i);
            }
        }
        return all.get(all.size() - 1);
    }

    /**
     * Rolls a whole dragon from an egg.
     *
     * @param id the id for the new dragon
     * @return the new dragon at 1 star
     */
    public OwnedDragon rollDragon(int id) {
        return rollDragon(id, pickSpecies(100));
    }

    /**
     * Rolls the variation (IV, nature, Alpha) for a chosen species.
     *
     * @param id      the id for the new dragon
     * @param species the species
     * @return the new dragon at 1 star
     */
    public OwnedDragon rollDragon(int id, DragonSpecies species) {
        Nature[] natures = Nature.values();
        return new OwnedDragon(id, species, rollIv(), rollIv(), rollIv(), rollIv(),
                natures[random.nextInt(natures.length)],
                random.nextInt(100) < GameRules.ALPHA_CHANCE_PERCENT, 1);
    }

    private int rollIv() {
        return OwnedDragon.IV_MIN + random.nextInt(OwnedDragon.IV_MAX - OwnedDragon.IV_MIN + 1);
    }
}
