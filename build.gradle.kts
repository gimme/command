plugins {
    kotlin("jvm") version "1.5.20"
    `maven-publish`
    id("org.jetbrains.dokka") version "1.4.32"
}

group = "dev.gimme.command"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Test
    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.10.6")
    testImplementation("com.github.spotbugs:spotbugs-annotations:4.2.3")

    // Other
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "14"
        allWarningsAsErrors = true
        freeCompilerArgs = listOf("-progressive")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
