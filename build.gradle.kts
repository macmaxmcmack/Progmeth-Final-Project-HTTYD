plugins {
    application
}

group = "httyd"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
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
