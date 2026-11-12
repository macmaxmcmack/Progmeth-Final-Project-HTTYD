# JavaFX migration plan

The course requires JavaFX, so the Swing GUI is being replaced. The engine, rules, bots, data and their tests
(`model`, `battle`, `bot`, `data`) are not touched. The old Swing version is kept in the `swing-version` git branch and
can still be started with `./gradlew runSwing` until the port is finished.

## Approach: hybrid
- Menu-style screens (menu, top up, team, fields, how to play, collection list) use real JavaFX controls, layouts and a CSS file.
- Pixel-art areas (battle arena, dragon sprites, egg, tiles) are drawn on a `Canvas` with `GraphicsContext`.
- All generated pictures use `WritableImage` / `PixelWriter`. No `java.awt` / `javax.swing` is left at the end.
- One bundled pixel font so Mac and Windows look the same. Fixed logical size 1024x720, scaled to fit.

## Targets
Presentation machine: MacBook Air M3 (Apple Silicon). Also must work on a Windows PC.
The exported jar bundles JavaFX natives for Apple Silicon, Windows and Linux (not Intel Mac).
GitHub Actions builds, tests and smoke-runs the jar on Windows and macOS for every push.

## Steps
| # | Step | Status |
|---|---|---|
| 1 | Build setup, `Main` launcher, `FxApp` empty window, fat jar, CI | done, verified on the Mac |
| 2 | Theme/font/CSS, picture factories (`Raster` + `PixelWriter`), `ScreenPane`, `GameWindow`, menu | written, waiting for a run on the Mac |
| 3 | Egg, top up, collection, team, fields, how to play | todo |
| 4 | Battle screen and arena | todo |
| 5 | Tests, remove the Swing code, jar check, README / manual / UML / JavaDoc / screenshots | todo |

Deadline: end of November 2026.
