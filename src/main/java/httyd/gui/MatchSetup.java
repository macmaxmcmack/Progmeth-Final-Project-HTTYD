package httyd.gui;

import httyd.model.dragon.OwnedDragon;
import httyd.model.item.ItemType;
import httyd.model.player.Difficulty;
import java.util.List;

/**
 * The choices of the player before a match.
 *
 * @param team       the three dragons in battle order
 * @param items      the chosen trainer items (at most two)
 * @param difficulty the bot's difficulty
 */
public record MatchSetup(List<OwnedDragon> team, List<ItemType> items, Difficulty difficulty) {
}
