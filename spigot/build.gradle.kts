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
    mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:${minecraftVersion}-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot:${minecraftVersion}-R0.1-SNAPSHOT")
}

tasks.processResources {
    val version = project.version.toString()
    val minecraftVersion = minecraftVersion

    filesMatching("plugin.yml") {
        expand("version" to version, "minecraft_version" to minecraftVersion)
    }
}

tasks.named<Jar>("jar") {
    archiveFileName.set("${rootProject.name}-${project.name}-${project.version}.jar")
}

// https://stackoverflow.com/questions/64290545/task-preparekotlinbuildscriptmodel-not-found-in-project-app
tasks.register("prepareKotlinBuildScriptModel") {}