plugins {
    id("java")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

group = "com.kami"
version = "0.1.0"

val minecraftVersion = "1.21.4"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${minecraftVersion}-R0.1-SNAPSHOT")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version, "minecraft_version" to minecraftVersion)
    }
}

tasks.register<Jar>("paper") {
    archiveFileName.set("${project.name}-paper-${project.version}.jar")
    destinationDirectory.set(layout.buildDirectory.dir("paper"))
    from(sourceSets.main.get().output)
}

tasks.register<Jar>("purpur") {
    archiveFileName.set("${project.name}-purpur-${project.version}.jar")
    destinationDirectory.set(layout.buildDirectory.dir("purpur"))
    from(sourceSets.main.get().output)
}

tasks.register("buildAll") {
    dependsOn("paper", "purpur")
    group = "build"
    description = "Builds both Paper and Purpur JARs."
    doLast {
        delete(layout.buildDirectory.dir("tmp"))
    }
}
