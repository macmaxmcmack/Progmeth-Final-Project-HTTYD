package httyd.model.skill;

import java.util.List;

/**
 * An immutable description of one attack of a dragon species.
 */
public final class Move {
    private final String name;
    private final String description;
    private final MoveKind kind;
    private final double power;
    private final boolean elemental;
    private final List<MoveEffect> effects;

    /**
     * Creates a move.
     *
     * @param name        English name of the move
     * @param description description
     * @param kind        the kind of the move
     * @param power       damage multiplier compared with a plain attack
     * @param elemental   true if the type chart and terrain shift apply
     * @param effects     extra effects of the move
     */
    public Move(String name, String description, MoveKind kind, double power,
                boolean elemental, List<MoveEffect> effects) {
        this.name = name;
        this.description = description;
        this.kind = kind;
        this.power = power;
        this.elemental = elemental;
        this.effects = List.copyOf(effects);
    }

    /** @return the English name of the move */
    public String getName() {
        return name;
    }

    /** @return the description of the move */
    public String getDescription() {
        return description;
    }

    /** @return the kind of the move */
    public MoveKind getKind() {
        return kind;
    }

    /** @return the damage multiplier */
    public double getPower() {
        return power;
    }

    /** @return true if the move is elemental */
    public boolean isElemental() {
        return elemental;
    }

    /** @return the unmodifiable list of effects */
    public List<MoveEffect> getEffects() {
        return effects;
    }
}
