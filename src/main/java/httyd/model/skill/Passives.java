package httyd.model.skill;

import httyd.battle.BattleLog;
import httyd.battle.Combatant;
import httyd.battle.DotType;
import httyd.model.dragon.Stat;
import java.util.List;

/**
 * The concrete passive abilities. Each one is a small subclass of {@link AbstractPassive}; the
 * dragon database only chooses which one to use and with which numbers.
 */
public final class Passives {

    private Passives() {
    }

    /** Extra dodge chance, either permanent or only during the first turns of the battle. */
    public static class DodgeUp extends AbstractPassive {
        private final double percent;
        private final int turns;

        /** @param turns number of own turns the bonus lasts, 0 for the whole battle */
        public DodgeUp(String n, String d, double s, double percent, int turns) {
            super(n, d, s);
            this.percent = percent;
            this.turns = turns;
        }

        @Override
        public double dodgeBonus(Combatant self) {
            if (turns > 0 && self.getOwnTurns() >= turns) {
                return 0;
            }
            return percent * getStrength();
        }
    }

    /** Reflects a percentage of the damage received back to the attacker. */
    public static class ReflectDamage extends AbstractPassive {
        private final double percent;

        public ReflectDamage(String n, String d, double s, double percent) {
            super(n, d, s);
            this.percent = percent;
        }

        @Override
        public void onAttacked(Combatant self, Combatant attacker, int damage, MoveKind kind, BattleLog log) {
            int back = (int) Math.round(damage * percent * getStrength() / 100.0);
            if (back > 0) {
                log.reflect(self, attacker, attacker.takeDamage(back));
            }
        }
    }

    /** Reflects a percentage of the owner's own Atk when hit by a plain attack. */
    public static class ReflectAttack extends AbstractPassive {
        private final double percent;

        public ReflectAttack(String n, String d, double s, double percent) {
            super(n, d, s);
            this.percent = percent;
        }

        @Override
        public void onAttacked(Combatant self, Combatant attacker, int damage, MoveKind kind, BattleLog log) {
            if (kind != MoveKind.TYPELESS) {
                return;
            }
            int back = (int) Math.round(self.getBaseStat(Stat.ATK) * percent * getStrength() / 100.0);
            if (back > 0) {
                log.reflect(self, attacker, attacker.takeDamage(back));
            }
        }
    }

    /** Gains a short Atk bonus every time the owner is hit (Skrill). */
    public static class Conductor extends AbstractPassive {
        private final int flat;

        public Conductor(String n, String d, double s, int flat) {
            super(n, d, s);
            this.flat = flat;
        }

        @Override
        public void onAttacked(Combatant self, Combatant attacker, int damage, MoveKind kind, BattleLog log) {
            self.addModifier(Stat.ATK, 0, (int) Math.round(flat * getStrength()), 1, false);
            log.status(self, self.getName() + " charges lightning! Attack increased");
        }
    }

    /** Critical hits against the owner deal less damage. */
    public static class CritResist extends AbstractPassive {
        private final double percent;

        public CritResist(String n, String d, double s, double percent) {
            super(n, d, s);
            this.percent = percent;
        }

        @Override
        public double incomingDamageMultiplier(MoveKind kind, boolean crit) {
            return crit ? 1.0 - percent * getStrength() / 100.0 : 1.0;
        }
    }

    /** Permanently lowers one stat of the opposing dragon. */
    public static class Aura extends AbstractPassive {
        private final Stat stat;
        private final double percent;

        public Aura(String n, String d, double s, Stat stat, double percent) {
            super(n, d, s);
            this.stat = stat;
            this.percent = percent;
        }

        @Override
        public double statMultiplierOnOpponent(Stat other) {
            return other == stat ? 1.0 - percent * getStrength() / 100.0 : 1.0;
        }
    }

    /** Gains Spd every time a Signature move is used, up to a maximum number of stacks. */
    public static class SparksJet extends AbstractPassive {
        private final int flat;
        private final int maxStacks;
        private int stacks;

        public SparksJet(String n, String d, double s, int flat, int maxStacks) {
            super(n, d, s);
            this.flat = flat;
            this.maxStacks = maxStacks;
        }

        @Override
        public void onMoveUsed(Combatant self, MoveKind kind, BattleLog log) {
            if (kind == MoveKind.SIGNATURE && stacks < maxStacks) {
                stacks++;
                self.addModifier(Stat.SPD, 0, (int) Math.round(flat * getStrength()), -1, true);
                log.status(self, self.getName() + " Speed increased");
            }
        }
    }

    /** Gains Def every time the owner is hit, up to a limit. */
    public static class DefStack extends AbstractPassive {
        private final int flat;
        private final int maxTotal;
        private int total;

        public DefStack(String n, String d, double s, int flat, int maxTotal) {
            super(n, d, s);
            this.flat = flat;
            this.maxTotal = maxTotal;
        }

        @Override
        public void onAttacked(Combatant self, Combatant attacker, int damage, MoveKind kind, BattleLog log) {
            int gain = (int) Math.round(flat * getStrength());
            if (total + gain <= maxTotal + 1) {
                total += gain;
                self.addModifier(Stat.DEF, 0, gain, -1, true);
            }
        }
    }

    /** Reduces all damage the owner receives. */
    public static class DamageReduction extends AbstractPassive {
        private final double percent;

        public DamageReduction(String n, String d, double s, double percent) {
            super(n, d, s);
            this.percent = percent;
        }

        @Override
        public double incomingDamageMultiplier(MoveKind kind, boolean crit) {
            return 1.0 - percent * getStrength() / 100.0;
        }
    }

    /** Heals when the previous turn did not use a Signature move (Hotburple). */
    public static class LazySlumber extends AbstractPassive {
        private final double percent;
        private boolean usedSignatureLastTurn = true;
        private boolean actedBefore;

        public LazySlumber(String n, String d, double s, double percent) {
            super(n, d, s);
            this.percent = percent;
        }

        @Override
        public void onTurnStart(Combatant self, BattleLog log) {
            if (actedBefore && !usedSignatureLastTurn) {
                int amount = self.heal((int) Math.round(self.getMaxHp() * percent * getStrength() / 100.0));
                if (amount > 0) {
                    log.heal(self, amount);
                }
            }
            actedBefore = true;
            usedSignatureLastTurn = false;
        }

        @Override
        public void onMoveUsed(Combatant self, MoveKind kind, BattleLog log) {
            if (kind == MoveKind.SIGNATURE) {
                usedSignatureLastTurn = true;
            }
        }
    }

    /** The owner's attacks can never be dodged. */
    public static class NeverDodged extends AbstractPassive {
        public NeverDodged(String n, String d, double s) {
            super(n, d, s);
        }

        @Override
        public boolean attacksIgnoreDodge() {
            return true;
        }
    }

    /** Flat Def bonus while the owner has low HP. */
    public static class LowHpDefense extends AbstractPassive {
        private final int flat;
        private final double thresholdPercent;

        public LowHpDefense(String n, String d, double s, int flat, double thresholdPercent) {
            super(n, d, s);
            this.flat = flat;
            this.thresholdPercent = thresholdPercent;
        }

        @Override
        public double flatStatBonus(Combatant self, Combatant foe, Stat stat) {
            boolean low = self.getCurrentHp() * 100.0 < self.getMaxHp() * thresholdPercent;
            return stat == Stat.DEF && low ? flat * getStrength() : 0;
        }
    }

    /** Flat Atk bonus when the owner is faster than the opponent. */
    public static class FasterAttack extends AbstractPassive {
        private final int flat;

        public FasterAttack(String n, String d, double s, int flat) {
            super(n, d, s);
            this.flat = flat;
        }

        @Override
        public double flatStatBonus(Combatant self, Combatant foe, Stat stat) {
            if (stat == Stat.ATK && foe != null
                    && self.getBaseStat(Stat.SPD) > foe.getBaseStat(Stat.SPD)) {
                return flat * getStrength();
            }
            return 0;
        }
    }

    /** Plain attacks have a higher critical chance. */
    public static class TypelessCrit extends AbstractPassive {
        private final double percent;

        public TypelessCrit(String n, String d, double s, double percent) {
            super(n, d, s);
            this.percent = percent;
        }

        @Override
        public double critChanceBonus(MoveKind kind) {
            return kind == MoveKind.TYPELESS ? percent * getStrength() / 100.0 : 0;
        }
    }

    /** Immune to reductions of one stat. */
    public static class DebuffImmunity extends AbstractPassive {
        private final Stat stat;

        public DebuffImmunity(String n, String d, double s, Stat stat) {
            super(n, d, s);
            this.stat = stat;
        }

        @Override
        public boolean immuneToDebuff(Stat other) {
            return other == stat;
        }
    }

    /** Every Signature move of the owner also poisons the target. */
    public static class PoisonSignature extends AbstractPassive {
        private final double percent;
        private final int turns;

        public PoisonSignature(String n, String d, double s, double percent, int turns) {
            super(n, d, s);
            this.percent = percent;
            this.turns = turns;
        }

        @Override
        public List<MoveEffect> extraEffects(MoveKind kind) {
            if (kind == MoveKind.SIGNATURE) {
                return List.of(new DotEffect(DotType.POISON, percent * getStrength(), turns));
            }
            return List.of();
        }
    }

    /** A permanent flat bonus to one stat. */
    public static class FlatBonus extends AbstractPassive {
        private final Stat stat;
        private final int flat;

        public FlatBonus(String n, String d, double s, Stat stat, int flat) {
            super(n, d, s);
            this.stat = stat;
            this.flat = flat;
        }

        @Override
        public double flatStatBonus(Combatant self, Combatant foe, Stat other) {
            return other == stat ? flat * getStrength() : 0;
        }
    }

    /** The owner deals more damage with every attack. */
    public static class DamageUp extends AbstractPassive {
        private final double percent;

        public DamageUp(String n, String d, double s, double percent) {
            super(n, d, s);
            this.percent = percent;
        }

        @Override
        public double outgoingDamageMultiplier(MoveKind kind) {
            return 1.0 + percent * getStrength() / 100.0;
        }
    }

    /** Survives one lethal hit with 1 HP. */
    public static class SurviveOnce extends AbstractPassive {
        private boolean used;

        public SurviveOnce(String n, String d, double s) {
            super(n, d, s);
        }

        @Override
        public boolean preventFaint(Combatant self) {
            if (used) {
                return false;
            }
            used = true;
            return true;
        }
    }

    /** A passive that does nothing; used when the database has no passive for a dragon. */
    public static class NoPassive extends AbstractPassive {
        public NoPassive() {
            super("-", "No special ability", 1.0);
        }
    }
}
