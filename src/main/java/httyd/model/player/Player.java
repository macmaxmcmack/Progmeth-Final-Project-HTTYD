package httyd.model.player;

import httyd.model.GameRules;
import httyd.model.dragon.OwnedDragon;
import httyd.model.item.Item;
import java.util.ArrayList;
import java.util.List;

/**
 * A participant of a match: a name, a deck of three dragons in battle order and a bag of at most
 * two trainer items. The human player is the base class; see {@link BotPlayer}.
 */
public class Player {
    private final String name;
    private final List<OwnedDragon> team;
    private final List<Item> bag;

    /**
     * @param name display name
     * @param team the dragons in battle order, exactly {@link GameRules#DECK_SIZE}
     * @param bag  the items, at most {@link GameRules#BAG_SIZE}
     * @throws IllegalArgumentException if the sizes are wrong
     */
    public Player(String name, List<OwnedDragon> team, List<Item> bag) {
        if (team.size() != GameRules.DECK_SIZE) {
            throw new IllegalArgumentException("a deck needs exactly " + GameRules.DECK_SIZE + " dragons");
        }
        if (bag.size() > GameRules.BAG_SIZE) {
            throw new IllegalArgumentException("a bag holds at most " + GameRules.BAG_SIZE + " items");
        }
        this.name = name;
        this.team = List.copyOf(team);
        this.bag = new ArrayList<>(bag);
    }

    /** @return the display name */
    public String getName() {
        return name;
    }

    /** @return the dragons in battle order */
    public List<OwnedDragon> getTeam() {
        return team;
    }

    /** @return the live list of items; used items are removed from it */
    public List<Item> getBag() {
        return bag;
    }

    /** @return true if the turn clock applies to this participant */
    public boolean isTimed() {
        return true;
    }

    /** @return true if this participant is controlled by the computer */
    public boolean isBot() {
        return false;
    }
}
