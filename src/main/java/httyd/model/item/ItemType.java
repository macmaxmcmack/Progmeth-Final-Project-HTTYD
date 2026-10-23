package httyd.model.item;

import httyd.model.dragon.Stat;

/**
 * The catalogue of the five trainer items. {@link #create()} builds a fresh item object.
 */
public enum ItemType {
    ARCTIC_CHAR,
    FIRE_GEL,
    DRAGON_ROOT,
    IRON_DUST,
    DRAGON_NIP;

    /** @return a new item of this type */
    public Item create() {
        return switch (this) {
            case ARCTIC_CHAR -> new HealItem("Fresh Arctic Char", "Fresh Arctic Char", 30);
            case FIRE_GEL -> new ReloadItem("Monstrous Fire Gel", "Monstrous Fire Gel", 3);
            case DRAGON_ROOT -> new BuffItem("Dragon Root Extract", "Dragon Root Extract", Stat.ATK, 20, 2);
            case IRON_DUST -> new BuffItem("Gronckle Iron Dust", "Gronckle Iron Dust", Stat.DEF, 25, 2);
            case DRAGON_NIP -> new CleanseItem("Dragon Nip", "Dragon Nip");
        };
    }
}
