plugins {
    id("java")
}

group = "org.simplemc"
version = "1.15-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    jcenter()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly(group = "org.spigotmc", name = "spigot-api", version = "1.15+")
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}
