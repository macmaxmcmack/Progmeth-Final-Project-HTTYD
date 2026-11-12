plugins {
    application
}

group = "httyd"
version = "1.0.0"

repositories {
    mavenCentral()
}

// ---- JavaFX -------------------------------------------------------------------------------------
// Normal builds (./gradlew run, tests, IDE) get the JavaFX natives of the machine they run on.
// The exported jar additionally bundles the natives of every platform below so that one
// `java -jar httyd-battle.jar` works on Apple Silicon Macs, Windows and Linux.
// (Intel Macs cannot be bundled in the same jar: their native files have the same names as Apple Silicon's.)
val fxVersion = "21.0.5"
val fxModules = listOf("base", "graphics", "controls")
val fxJarPlatforms = listOf("mac-aarch64", "win", "linux")
val fxAllPlatforms: Configuration by configurations.creating

dependencies {
    fxModules.forEach { module -> implementation("org.openjfx:javafx-$module:$fxVersion") }
    fxJarPlatforms.forEach { platform ->
        fxModules.forEach { module -> fxAllPlatforms("org.openjfx:javafx-$module:$fxVersion:$platform") }
    }
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "httyd.app.Main"
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    // Build with JDK 25 but produce Java 21 bytecode, so the exported jar also runs on JDK 21.
    options.release = 21
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-serial", "-Xlint:-this-escape"))
}

tasks.test {
    useJUnitPlatform()
    systemProperty("java.awt.headless", "true")
    testLogging {
        events("passed", "skipped", "failed")
    }
}

// `./gradlew jar` -> build/libs/httyd-battle.jar (double-click or `java -jar httyd-battle.jar`)
tasks.jar {
    archiveBaseName = "httyd-battle"
    archiveVersion = ""
    manifest {
        attributes["Main-Class"] = "httyd.app.Main"
    }
    // Fat jar: our classes + JavaFX for every platform (see above)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
        (configurations.runtimeClasspath.get().files + fxAllPlatforms.files)
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })
    exclude("module-info.class", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

// The old Swing version, kept until the JavaFX port is finished: ./gradlew runSwing
tasks.register<JavaExec>("runSwing") {
    group = "application"
    mainClass = "httyd.app.SwingMain"
    classpath = sourceSets.main.get().runtimeClasspath
}

// `./gradlew javadoc` -> build/docs/javadoc/index.html
tasks.javadoc {
    options.encoding = "UTF-8"
    title = "HTTYD Dragon Battle API"
    (options as StandardJavadocDocletOptions).apply {
        charSet = "UTF-8"
        docEncoding = "UTF-8"
        addStringOption("Xdoclint:all,-missing", "-quiet")
    }
}
