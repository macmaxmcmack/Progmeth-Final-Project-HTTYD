package httyd.battle;

/**
 * One thing that happened in the battle.
 *
 * @param type  what kind of event it is
 * @param side  the side the event is about, or {@code null} for general messages
 * @param text  the message to show
 * @param value an amount such as damage or healing, otherwise 0
 */
public record BattleEvent(EventType type, Side side, String text, int value) {
}
