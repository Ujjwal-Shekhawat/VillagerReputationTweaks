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