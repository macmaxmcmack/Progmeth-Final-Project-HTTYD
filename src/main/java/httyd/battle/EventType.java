package httyd.battle;

/** Kinds of things that can happen in a battle, used by the GUI to animate and play sounds. */
public enum EventType {
    MESSAGE, DAMAGE, CRIT, DODGE, HEAL, STATUS, FAINT, SWITCH_IN, FIELD, ITEM, REST, REFLECT, END
}
