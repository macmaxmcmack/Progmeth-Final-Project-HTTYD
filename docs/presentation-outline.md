# 10-minute presentation script

| Time | Topic | What to show / say |
|---|---|---|
| 0:00-0:45 | Introduce the game | HTTYD theme, bot opponent, eggs, 7 battlefields, free top-up shop. Open the main menu |
| 0:45-2:00 | Demo 1: top up, hatch, fuse | Top Up Coins (free, no real money), egg opening, IV / Nature / Alpha, grades, stars, Titan Wing |
| 2:00-4:00 | Demo 2: a full match | Team / items / fields, then fight: type advantage, ultimate changes the field, items, clocks, win / loss |
| 4:00-5:30 | Architecture | The 3-layer diagram (`docs/uml/04-battle-engine.png`); the GUI contains no rules |
| 5:30-8:00 | All four OOP topics | Real code one by one: `Item` and its 4 subclasses, `MoveEffect`, `BotStrategy`, `Player` / `BotPlayer.isTimed()`, `private` / `protected` / package-private |
| 8:00-9:00 | Tests and build | `./gradlew test` passes, `./gradlew jar`, run the jar from a new folder |
| 9:00-10:00 | Wrap-up | Limits (placeholder art and sound, self-designed type chart), future work (PvP, real art) |

Likely questions
- Why an interface and not an abstract class for `MoveEffect`? A move combines several effects and they share no state.
- How does `Player.isTimed()` work for bots? Polymorphism: `BotPlayer` overrides it to return false.
- How did you test the clock? A `TimeSource` interface with a `FakeTime` implementation in the tests.
- Is the top-up real? No. `CoinPack.claim(profile)` just calls `Profile.addCoins`, and no payment code exists.
