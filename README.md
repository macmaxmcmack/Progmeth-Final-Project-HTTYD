# java-team-project

Java 25 console app, built with Gradle (Kotlin DSL) and tested with JUnit 5.

## Requirements

- **Nothing else to install for Gradle** — use the wrapper (`./gradlew`), it downloads Gradle 9.8.1 automatically.
- **JDK 25** — you don't have to install it by hand: Gradle's toolchain downloads it on first build.
  (If you want it on your machine anyway: `sdk env install` with [SDKMAN](https://sdkman.io), or install Temurin 25.)
- The JDK that *runs Gradle itself* must be Java 17 or newer (Java 25 is fine).

## Commands

| What | macOS / Linux | Windows |
|---|---|---|
| Run the app | `./gradlew run` | `gradlew.bat run` |
| Run tests | `./gradlew test` | `gradlew.bat test` |
| Build everything | `./gradlew build` | `gradlew.bat build` |

Always use `./gradlew`, never a globally installed `gradle`, so everyone uses the same version.

## Team rules

1. Don't change the Java version or Gradle version in your own branch without telling the team.
   To upgrade Gradle: `./gradlew wrapper --gradle-version <version>` and commit the result.
2. Don't commit `build/`, `.gradle/`, or IDE files (`.idea/`, `.vscode/`) — `.gitignore` already covers them.
3. Make sure your IDE uses the Gradle wrapper and JDK 25
   (IntelliJ: Settings → Build Tools → Gradle → "Use Gradle from: wrapper", Gradle JVM: 25).

## Project layout

```
src/main/java/com/example/app/Main.java
src/test/java/com/example/app/MainTest.java
build.gradle.kts      # build config + Java toolchain (25)
settings.gradle.kts   # project name + toolchain auto-download plugin
gradle/wrapper/       # pins Gradle version
```
