import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    `java-library`
    `maven-publish`
    id("org.jetbrains.dokka") version "1.4.32"
}

group = "dev.gimme.gimmeapi.mc"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/groups/public")
    mavenLocal()
}

dependencies {
    // Project
    api(project(":gimmeapi-core"))
    api(project(":gimmeapi-boot"))

    // Spigot
    compileOnly("org.spigotmc:spigot-api:1.16.4-R0.1-SNAPSHOT")
}


tasks.withType<KotlinCompile>() {
    kotlinOptions {
        jvmTarget = "14"
        allWarningsAsErrors = true
        freeCompilerArgs = listOf("-progressive")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_13
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
            val javadocJar by tasks.creating(Jar::class) {
                dependsOn(dokkaHtml)
                archiveClassifier.set("javadoc")
                from(dokkaHtml.outputDirectory)
            }
            artifact(javadocJar)

            from(components["java"])
        }
    }
}
