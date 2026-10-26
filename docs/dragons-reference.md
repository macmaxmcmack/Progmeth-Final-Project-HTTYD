# Dragon Reference

Generated from `src/main/resources/data/dragons.csv` by `docs/tools/make_dragon_reference.py`. Edit the CSV, not this page.

28 dragons. Tiers: S (rarest), A, B, C (most common). Shots = how many Signature moves a dragon can fire before it must rest. Stats are the base values from the CSV (in battle, HP is multiplied by 3 and each dragon also gets random IVs and a Nature). The code in backticks is the move-effect spec read by the game.

## Overview

| Dragon | Class | Tier | HP | Atk | Def | Spd | Crit | Shots | Passive |
|---|---|---|--:|--:|--:|--:|--:|--:|---|
| Skrill | Strike | S | 86 | 94 | 82 | 92 | 22% | 4 | Conductor |
| Toothless (Night Fury) | Strike | S | 85 | 95 | 75 | 98 | 20% | 6 | Night Camouflage |
| Light Fury | Strike | A | 80 | 88 | 72 | 96 | 18% | 6 | Mirrored Scales |
| Triple Stryke | Strike | A | 84 | 89 | 85 | 88 | 16% | 6 | Tough Carapace |
| Fireworm Queen | Stoker | S | 92 | 93 | 88 | 75 | 14% | 10 | Blazing Radiance |
| Hookfang (Monstrous Nightmare) | Stoker | A | 82 | 91 | 76 | 80 | 15% | 10 | Fire Jacket |
| Typhoomerang | Stoker | A | 80 | 88 | 74 | 87 | 16% | 8 | Sparks Jet |
| Terrible Terror | Stoker | C | 62 | 68 | 60 | 84 | 25% | 5 | Nimble Body |
| Crimson Goregutter | Boulder | S | 98 | 92 | 94 | 64 | 10% | 6 | Titan Bulk |
| Whispering Death | Boulder | A | 86 | 89 | 88 | 78 | 18% | 8 | Spine Defense |
| Hotburple (Grump) | Boulder | B | 94 | 74 | 95 | 60 | 6% | 6 | Lazy Slumber |
| Meatlug (Gronckle) | Boulder | B | 90 | 72 | 96 | 62 | 8% | 6 | Mineral Digestion |
| Skullcrusher (Rumblehorn) | Tracker | A | 88 | 85 | 89 | 76 | 12% | 4 | Scent Lock |
| Stormfly (Deadly Nadder) | Tracker | A | 82 | 88 | 80 | 86 | 24% | 6 | Keen Senses |
| Thunderclaw | Tracker | B | 76 | 78 | 75 | 80 | 14% | 6 | Trail Tracker |
| Snifflehunch | Tracker | C | 68 | 66 | 68 | 72 | 10% | 6 | Survival Instinct |
| Cloudjumper (Stormcutter) | Sharp | S | 87 | 93 | 83 | 92 | 20% | 8 | X-Wing Maneuver |
| Razorwhip (Windshear) | Sharp | A | 82 | 90 | 90 | 85 | 18% | 4 | Steel Plating |
| Timberjack | Sharp | A | 78 | 87 | 72 | 89 | 22% | 8 | Sharp Wings |
| Speed Stinger | Sharp | B | 72 | 79 | 70 | 95 | 18% | 4 | Water Runner |
| Bewilderbeast | Tidal | S | 99 | 96 | 95 | 60 | 10% | 8 | Alpha Armor |
| Scauldron | Tidal | A | 88 | 86 | 84 | 76 | 12% | 14 | Oleander Venom |
| Thunderdrum (Thornado) | Tidal | A | 85 | 88 | 82 | 78 | 14% | 6 | Deafening Roar |
| Seashocker | Tidal | B | 80 | 82 | 76 | 82 | 15% | 6 | Echolocation |
| Death Song | Mystery | S | 88 | 92 | 82 | 86 | 18% | 8 | Predator of Dragons |
| Changewing | Mystery | A | 78 | 87 | 72 | 90 | 20% | 10 | Hypnotic Camouflage |
| Barf & Belch (Hideous Zippleback) | Mystery | B | 82 | 84 | 78 | 74 | 15% | 6 | Two Heads Advantage |
| Boneknapper | Mystery | B | 86 | 83 | 91 | 70 | 12% | 6 | Bone Armor |

## Strike

### Skrill  (tier S)

- **Stats:** HP 86 | Atk 94 | Def 82 | Spd 92 | Crit 22% | 4 shots
- **Basic attack:** Metallic Wing Slash
- **Signature:** Lightning Channel (power 1.2) `SPD_DOWN:25`  
  Fires a high-voltage bolt that shocks the target and lowers its Speed by 25%
- **Ultimate:** Thunderstorm Cataclysm (power 2.3, elemental)  
  Draws in lightning for one enormous electrical blast on a single target
- **Passive:** Conductor `CONDUCTOR:5`  
  Charges up whenever hit: +5 Attack on the next turn
- **Picture file:** `sprites/skrill.png`

### Toothless (Night Fury)  (tier S)

- **Stats:** HP 85 | Atk 95 | Def 75 | Spd 98 | Crit 20% | 6 shots
- **Basic attack:** Dive Bomb & Tail Swipe
- **Signature:** Plasma Blast (power 1.4)  
  A highly accurate plasma bolt that hits hard
- **Ultimate:** Alpha Lightning Strike (power 2.0, elemental) `PIERCE:20+SELF_FLAT:SPD:10`  
  A single devastating lightning strike that pierces 20% Defense and raises own Speed by 10
- **Passive:** Night Camouflage `DODGE:10:1`  
  Raises own Dodge Rate by 10% on the first turn of the battle
- **Picture file:** `sprites/toothless.png`

### Light Fury  (tier A)

- **Stats:** HP 80 | Atk 88 | Def 72 | Spd 96 | Crit 18% | 6 shots
- **Basic attack:** Acrobatic Claw Strike
- **Signature:** Superheated Plasma (power 1.2) `BURN:5:2`  
  Superheated plasma that inflicts Burn, draining 5% HP for 2 turns
- **Ultimate:** Vapor Cloak Strike (power 2.0, elemental) `CRIT`  
  Breathes fire through a vapor cloak and vanishes; a single-target attack with 100% critical chance
- **Passive:** Mirrored Scales `REFLECT_DMG:15`  
  Reflective scales return 15% of damage taken to the attacker
- **Picture file:** `sprites/light_fury.png`

### Triple Stryke  (tier A)

- **Stats:** HP 84 | Atk 89 | Def 85 | Spd 88 | Crit 16% | 6 shots
- **Basic attack:** Triple Tail Pincer
- **Signature:** Blinding Fire Flash (power 1.2) `ATK_DOWN:15`  
  A blinding flash of fire that lowers the target's Attack by 15%
- **Ultimate:** Venomous Stinger Flurry (power 2.0, elemental) `POISON:8:2`  
  Stabs a single target with three tail spikes and inflicts Poison, draining 8% HP for 2 turns
- **Passive:** Tough Carapace `CRIT_RESIST:20`  
  A thick carapace reduces critical damage taken by 20%
- **Picture file:** `sprites/triple_stryke.png`

## Stoker

### Fireworm Queen  (tier S)

- **Stats:** HP 92 | Atk 93 | Def 88 | Spd 75 | Crit 14% | 10 shots
- **Basic attack:** Royal Stomp & Leg Swipe
- **Signature:** Solar Flare Beam (power 1.2) `DEF_DOWN:20`  
  A beam as hot as the sun that melts armor and lowers Defense by 20%
- **Ultimate:** Solar Rejuvenation (power 0, type-less) `HEAL:25+SELF_FLAT:ATK:10`  
  Awakens the solar nest, restoring 25% of own HP and raising own Attack by 10
- **Passive:** Blazing Radiance `AURA:DEF:10`  
  Intense heat around it permanently lowers enemy dragons' Defense by 10%
- **Picture file:** `sprites/fireworm_queen.png`

### Hookfang (Monstrous Nightmare)  (tier A)

- **Stats:** HP 82 | Atk 91 | Def 76 | Spd 80 | Crit 15% | 10 shots
- **Basic attack:** Flaming Horn Bash
- **Signature:** Kerosene Gel Fire (power 1.2) `BURN:6:2`  
  Sprays kerosene gel fire that inflicts Burn, draining 6% HP per turn for 2 turns
- **Ultimate:** Fire Coat Explosion (power 2.1, elemental)  
  Engulfs its whole body in flames and rams a single target
- **Passive:** Fire Jacket `REFLECT_ATK:15`  
  When hit by a close-range attack, reflects fire for 15% of its own Attack
- **Picture file:** `sprites/hookfang.png`

### Typhoomerang  (tier A)

- **Stats:** HP 80 | Atk 88 | Def 74 | Spd 87 | Crit 16% | 8 shots
- **Basic attack:** Cyclonic Wing Buffet
- **Signature:** Flaming Cyclone (power 1.2) `SPD_DOWN:15`  
  Spins and breathes a drill of fire that deals damage and lowers enemy Speed by 15%
- **Ultimate:** Firestorm Vortex (power 2.0, elemental) `HITS:3`  
  A fire tornado drills into a single target for 3 consecutive hits
- **Passive:** Sparks Jet `SPARKS:5:3`  
  Sparks Jet: +5 own Speed each time a Signature move is used, stacking up to 3 times
- **Picture file:** `sprites/typhoomerang.png`

### Terrible Terror  (tier C)

- **Stats:** HP 62 | Atk 68 | Def 60 | Spd 84 | Crit 25% | 5 shots
- **Basic attack:** Quick Nip Bite
- **Signature:** Sniper Fireball (power 1.4) `SUREHIT`  
  Fires a precise fireball that always deals fixed damage
- **Ultimate:** Pack Ambush Strike (power 2.0, elemental) `HITS:4`  
  Calls the pack to swarm a single target with 4 consecutive hits
- **Passive:** Nimble Body `DODGE:8:0`  
  Its tiny, agile body grants +8% base Dodge Rate
- **Picture file:** `sprites/terrible_terror.png`

## Boulder

### Crimson Goregutter  (tier S)

- **Stats:** HP 98 | Atk 92 | Def 94 | Spd 64 | Crit 10% | 6 shots
- **Basic attack:** Massive Antler Ram
- **Signature:** Molten Rock Stream (power 1.2) `BURN:5:2`  
  Spews a stream of molten rock that inflicts Burn, 5% HP for 2 turns
- **Ultimate:** Colossal Wall Breaker (power 2.0, elemental) `DEF_DOWN:30`  
  Charges with its colossal horn, shattering enemy Defense by 30%
- **Passive:** Titan Bulk `REDUCE_ALL:10`  
  Titan Bulk: reduces all physical damage taken by 10%
- **Picture file:** `sprites/crimson_goregutter.png`

### Whispering Death  (tier A)

- **Stats:** HP 86 | Atk 89 | Def 88 | Spd 78 | Crit 18% | 8 shots
- **Basic attack:** Rotating Jaw Grind
- **Signature:** Concentric Fire Ring (power 1.2) `DEF_DOWN:15`  
  Breathes a spinning ring of fire that lowers enemy Defense by 15%
- **Ultimate:** Subterranean Ambush (power 2.1, elemental)  
  Burrows underground and bursts out to maul a single target
- **Passive:** Spine Defense `REFLECT_DMG:10`  
  Spine Defense: fires spines back at the attacker for 10% reflected damage
- **Picture file:** `sprites/whispering_death.png`

### Hotburple (Grump)  (tier B)

- **Stats:** HP 94 | Atk 74 | Def 95 | Spd 60 | Crit 6% | 6 shots
- **Basic attack:** Heavy Body Slam
- **Signature:** Molten Iron Slag (power 1.2) `SPD_DOWN:20`  
  Spews sticky molten iron slag that lowers enemy Speed by 20%
- **Ultimate:** Slag Quake Slam (power 2.0, elemental) `ATK_DOWN:15+SPD_DOWN:15`  
  Slams the ground, lowering the enemy's Attack and Speed by 15%
- **Passive:** Lazy Slumber `LAZY:5`  
  Lazy Slumber: if no Signature move was used last turn, restores 5% HP immediately
- **Picture file:** `sprites/hotburple.png`

### Meatlug (Gronckle)  (tier B)

- **Stats:** HP 90 | Atk 72 | Def 96 | Spd 62 | Crit 8% | 6 shots
- **Basic attack:** Blunt Headbutt
- **Signature:** Gronckle Iron Lava (power 1.4)  
  Spews molten lava at the target for heavy damage
- **Ultimate:** Boulder Crash Roll (power 2.0, elemental) `PIERCE:15`  
  Curls into a giant stone ball and rolls over the target, piercing 15% Defense
- **Passive:** Mineral Digestion `DEF_STACK:2:10`  
  Mineral Digestion: +2 own Defense each time it is hit, up to 10
- **Picture file:** `sprites/meatlug.png`

## Tracker

### Skullcrusher (Rumblehorn)  (tier A)

- **Stats:** HP 88 | Atk 85 | Def 89 | Spd 76 | Crit 12% | 4 shots
- **Basic attack:** Armored Horn Charge
- **Signature:** Fire Missiles (power 1.4)  
  Fires compressed fire missiles from long range
- **Ultimate:** Unstoppable Tracker Charge (power 2.0, elemental) `PIERCE:100`  
  Charges a single target and ignores Defense
- **Passive:** Scent Lock `NEVER_DODGED`  
  Scent Lock: permanently reduces enemy dragons' Dodge Rate to 0%
- **Picture file:** `sprites/skullcrusher.png`

### Stormfly (Deadly Nadder)  (tier A)

- **Stats:** HP 82 | Atk 88 | Def 80 | Spd 86 | Crit 24% | 6 shots
- **Basic attack:** Spiked Tail Whip
- **Signature:** Magnesium Fire (power 1.2) `BURN:6:2`  
  Intensely hot magnesium fire that inflicts Burn, 6% HP for 2 turns
- **Ultimate:** Tail Spine Barrage (power 2.0, elemental) `CRITBONUS:50`  
  Fires a barrage of poisonous tail spines at a single target with +50% critical chance
- **Passive:** Keen Senses `NEVER_DODGED`  
  Keen Senses: Stormfly's attacks cannot be dodged
- **Picture file:** `sprites/stormfly.png`

### Thunderclaw  (tier B)

- **Stats:** HP 76 | Atk 78 | Def 75 | Spd 80 | Crit 14% | 6 shots
- **Basic attack:** Trampling Claw
- **Signature:** Scorching Green Flame (power 1.2) `BURN:5:2`  
  Breathes a scorching green flame that inflicts Burn, 5% HP for 2 turns
- **Ultimate:** Bison Horn Rush (power 2.0, elemental) `SPD_DOWN:20`  
  Rams a single target and lowers its Speed by 20%
- **Passive:** Trail Tracker `FASTER_ATK:8`  
  Trail Tracker: +8 own Attack if own Speed is higher than the enemy's
- **Picture file:** `sprites/thunderclaw.png`

### Snifflehunch  (tier C)

- **Stats:** HP 68 | Atk 66 | Def 68 | Spd 72 | Crit 10% | 6 shots
- **Basic attack:** Chin Horn Jab
- **Signature:** Fire Blossom (power 1.2) `ATK_DOWN:10`  
  Spits a fire blossom that bursts apart and lowers enemy Attack by 10%
- **Ultimate:** Intuitive Evasion (power 0, type-less) `DODGE:25:2`  
  Senses danger ahead: raises own Dodge Rate to 25% for 2 turns
- **Passive:** Survival Instinct `LOWHP_DEF:15:40`  
  Survival Instinct: gains +15 Defense when HP drops below 40%
- **Picture file:** `sprites/snifflehunch.png`

## Sharp

### Cloudjumper (Stormcutter)  (tier S)

- **Stats:** HP 87 | Atk 93 | Def 83 | Spd 92 | Crit 20% | 8 shots
- **Basic attack:** Four-Wing Slash
- **Signature:** Torus Fire Tornado (power 1.2) `DEF_DOWN:15`  
  Breathes a powerful ring of fire tornado that lowers enemy Defense by 15%
- **Ultimate:** Sky King's Cleave (power 2.0, elemental) `PIERCE:100`  
  Slashes with all four wings at once for single-target damage that pierces Defense
- **Passive:** X-Wing Maneuver `DODGE:12:0`  
  X-Wing Maneuver: four wings allow acrobatic flight, +12% own Dodge Rate
- **Picture file:** `sprites/cloudjumper.png`

### Razorwhip (Windshear)  (tier A)

- **Stats:** HP 82 | Atk 90 | Def 90 | Spd 85 | Crit 18% | 4 shots
- **Basic attack:** Barbed Wing Scythe
- **Signature:** Intense Blue Lance (power 1.4)  
  Breathes an intense blue flame as hot as a lance of fire
- **Ultimate:** Metallic Tail Decapitation (power 2.0, elemental) `CRITBONUS:50`  
  Lashes with a razor-sharp steel tail, 50% critical chance
- **Passive:** Steel Plating `REFLECT_DMG:15`  
  Steel Plating: reflects 15% of damage back to the attacker
- **Picture file:** `sprites/razorwhip.png`

### Timberjack  (tier A)

- **Stats:** HP 78 | Atk 87 | Def 72 | Spd 89 | Crit 22% | 8 shots
- **Basic attack:** Razor Wing Sweep
- **Signature:** Crossfire Burst (power 1.4)  
  Fires an X-shaped burst of fire
- **Ultimate:** Forest Cleaver Slice (power 2.0, elemental) `CRIT`  
  Spreads its huge wings and cleaves a single target with 100% critical chance
- **Passive:** Sharp Wings `TYPELESS_CRIT:10`  
  Sharp Wings: Basic Attacks crit more easily (+10%)
- **Picture file:** `sprites/timberjack.png`

### Speed Stinger  (tier B)

- **Stats:** HP 72 | Atk 79 | Def 70 | Spd 95 | Crit 18% | 4 shots
- **Basic attack:** Rapid Raptor Claw
- **Signature:** Paralyzing Stinger Strike (power 1.2) `SPD_DOWN:35`  
  Stabs with its venomous tail stinger, instantly lowering enemy Speed by 35%
- **Ultimate:** Overdrive Hyper Sprint (power 2.0, elemental) `HITS:3`  
  Dashes at high speed for 3 hits on a single target
- **Passive:** Water Runner `IMMUNE:SPD`  
  Water Runner: immune to every kind of Speed reduction
- **Picture file:** `sprites/speed_stinger.png`

## Tidal

### Bewilderbeast  (tier S)

- **Stats:** HP 99 | Atk 96 | Def 95 | Spd 60 | Crit 10% | 8 shots
- **Basic attack:** Glacial Tusk Smash
- **Signature:** Freezing Ice Breath (power 1.2) `SPD_DOWN:25`  
  Breathes freezing air that deals damage and lowers enemy Speed by 25%
- **Ultimate:** Alpha Intimidation Roar (power 1.8, elemental) `ATK_DOWN:25`  
  Lets out an alpha roar that intimidates the target, lowering its Attack by 25%
- **Passive:** Alpha Armor `REDUCE_ALL:15`  
  Alpha Armor: dense ice scales reduce all damage taken by 15%
- **Picture file:** `sprites/bewilderbeast.png`

### Scauldron  (tier A)

- **Stats:** HP 88 | Atk 86 | Def 84 | Spd 76 | Crit 12% | 14 shots
- **Basic attack:** Tail Fin Slap
- **Signature:** Scalding Boiling Water (power 1.2) `DEF_DOWN:20`  
  Sprays boiling water that melts armor and lowers enemy Defense by 20%
- **Ultimate:** Tsunami Geyser (power 2.1, elemental)  
  A geyser of boiling water blasts a single target for heavy damage
- **Passive:** Oleander Venom `POISON_SIG:5:2`  
  Oleander Venom: Signature moves inflict Poison, 5% HP for 2 turns
- **Picture file:** `sprites/scauldron.png`

### Thunderdrum (Thornado)  (tier A)

- **Stats:** HP 85 | Atk 88 | Def 82 | Spd 78 | Crit 14% | 6 shots
- **Basic attack:** Expanding Jaw Clamp
- **Signature:** Concussive Sonic Blast (power 1.4) `PIERCE:15`  
  Releases a destructive sonic wave that pierces 15% Defense
- **Ultimate:** Soundwave Cannon (power 2.0, elemental) `ATK_DOWN:20`  
  Fires a sonic cannon at a single target and lowers its Attack by 20%
- **Passive:** Deafening Roar `IMMUNE:ATK`  
  Deafening Roar: immune to every kind of Attack reduction
- **Picture file:** `sprites/thunderdrum.png`

### Seashocker  (tier B)

- **Stats:** HP 80 | Atk 82 | Def 76 | Spd 82 | Crit 15% | 6 shots
- **Basic attack:** Twin-Head Bite
- **Signature:** Bio-Electric Shock (power 1.2) `SPD_DOWN:20`  
  Releases an electric shock that lowers enemy Speed by 20%
- **Ultimate:** Depth Charge Discharge (power 2.1, elemental)  
  Both heads combine into a lightning ball that hits a single target hard
- **Passive:** Echolocation `DODGE:10:0`  
  Echolocation: uses sound to locate targets, +10% own Dodge Rate
- **Picture file:** `sprites/seashocker.png`

## Mystery

### Death Song  (tier S)

- **Stats:** HP 88 | Atk 92 | Def 82 | Spd 86 | Crit 18% | 8 shots
- **Basic attack:** Resonant Wing Flap
- **Signature:** Amber Spit (power 1.2) `SPD_DOWN:40`  
  Spits sticky amber that deals damage and lowers enemy Speed by 40%
- **Ultimate:** Siren Song Blast (power 2.0, elemental) `ATK_DOWN:20`  
  Sings a hypnotic song that unleashes a violent sound wave, lowering enemy Attack by 20%
- **Passive:** Predator of Dragons `DMG_UP:10`  
  Predator of Dragons: hunter's instinct, deals 10% more damage to enemy dragons
- **Picture file:** `sprites/death_song.png`

### Changewing  (tier A)

- **Stats:** HP 78 | Atk 87 | Def 72 | Spd 90 | Crit 20% | 10 shots
- **Basic attack:** Prehensile Tail Strike
- **Signature:** Hot Acid Spray (power 1.2) `DEF_DOWN:20`  
  Sprays hot green acid that melts armor and lowers Defense by 20%
- **Ultimate:** Chameleon Shadow Strike (power 2.0, elemental) `CRIT`  
  Camouflages, then strikes a single target with a guaranteed critical hit
- **Passive:** Hypnotic Camouflage `DODGE:15:2`  
  Hypnotic Camouflage: invisible at the start of battle, +15% own Dodge Rate for the first 2 turns
- **Picture file:** `sprites/changewing.png`

### Barf & Belch (Hideous Zippleback)  (tier B)

- **Stats:** HP 82 | Atk 84 | Def 78 | Spd 74 | Crit 15% | 6 shots
- **Basic attack:** Dual Head Horn Headbutt
- **Signature:** Gas & Spark Combo (power 1.2) `BURN:6:2`  
  Sprays green gas then ignites it with a spark to inflict Burn, 6% HP for 2 turns
- **Ultimate:** Ouroboros Flaming Wheel (power 2.1, elemental)  
  Bites its own tail and rolls into a flaming wheel that rams a single target
- **Passive:** Two Heads Advantage `FLAT:DEF:8`  
  Two Heads Advantage: two heads watch for danger, permanent +8 Defense
- **Picture file:** `sprites/barf_belch.png`

### Boneknapper  (tier B)

- **Stats:** HP 86 | Atk 83 | Def 91 | Spd 70 | Crit 12% | 6 shots
- **Basic attack:** Bone Club Tail
- **Signature:** Phosphorescent Fire (power 1.2) `BURN:5:2`  
  Breathes a bright phosphorescent green fire that inflicts Burn, 5% HP for 2 turns
- **Ultimate:** Roar of the Undead (power 1.8, elemental) `ATK_DOWN:20`  
  Roars to spread fear, lowering enemy Attack by 20%
- **Passive:** Bone Armor `SURVIVE`  
  Bone Armor: wearing the bones of dead dragons, survives a fatal hit with 1 HP (once)
- **Picture file:** `sprites/boneknapper.png`
