plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
}

kotlin {
    sourceSets {
        main {
            // This repo holds only the bot's own decision-making code (search, evaluation,
            // configuration). Move legality, check/checkmate detection, etc. come from
            // chess-engine, pulled in here (source, not a compiled artifact) via the
            // chess-engine-src submodule — see README.md for why and how to update it.
            kotlin.srcDir("chess-engine-src/chess-engine/src/main/kotlin")
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
