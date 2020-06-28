import pl.allegro.tech.build.axion.release.domain.hooks.HookContext
import pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    id("java")
    id("pl.allegro.tech.build.axion-release") version "1.10.3"
}

val repoRef = "SimpleMC\\/SimpleReserve"
val mcApiVersion = "1.15"

group = "org.simplemc"
version = scmVersion.version

scmVersion {
    hooks(closureOf<HooksConfig> {
        pre(
            "fileUpdate",
            mapOf(
                "file" to "src/main/resources/plugin.yml",
                "pattern" to KotlinClosure2<String, HookContext, String>({ v, _ -> "version: $v\\napi-version: \".+\"" }),
                "replacement" to KotlinClosure2<String, HookContext, String>({ v, _ -> "version: $v\napi-version: \"$mcApiVersion\"" })
            )
        )
        // "normal" changelog update--changelog already contains a history
        pre(
            "fileUpdate",
            mapOf(
                "file" to "CHANGELOG.md",
                "pattern" to KotlinClosure2<String, HookContext, String>({ v, _ ->
                    "\\[Unreleased\\]([\\s\\S]+?)\\n(?:^\\[Unreleased\\]: https:\\/\\/github\\.com\\/$repoRef\\/compare\\/release-$v\\.\\.\\.HEAD\$([\\s\\S]*))?\\z"
                }),
                "replacement" to KotlinClosure2<String, HookContext, String>({ v, c ->
                    """
                        \[Unreleased\]
                        
                        ## \[$v\] - ${currentDateString()}$1
                        \[Unreleased\]: https:\/\/github\.com\/$repoRef\/compare\/release-$v...HEAD
                        \[$v\]: https:\/\/github\.com\/$repoRef\/compare\/release-${c.previousVersion}...release-$v$2
                    """.trimIndent()
                })
            )
        )
        // first-time changelog update--changelog has only unreleased info
        pre(
            "fileUpdate",
            mapOf(
                "file" to "CHANGELOG.md",
                "pattern" to KotlinClosure2<String, HookContext, String>({ v, _ ->
                    "Unreleased([\\s\\S]+?\\nand this project adheres to \\[Semantic Versioning\\]\\(https:\\/\\/semver\\.org\\/spec\\/v2\\.0\\.0\\.html\\).)\\s\\z"
                }),
                "replacement" to KotlinClosure2<String, HookContext, String>({ v, c ->
                    """
                        \[Unreleased\]
                        
                        ## \[$v\] - ${currentDateString()}$1
                        
                        \[Unreleased\]: https:\/\/github\.com\/$repoRef\/compare\/release-$v...HEAD
                        \[$v\]: https:\/\/github\.com\/$repoRef\/releases\/tag\/release-$v
                    """.trimIndent()
                })
            )
        )
        pre("commit")
    })
}

fun currentDateString() = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_DATE)

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    jcenter()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly(group = "org.spigotmc", name = "spigot-api", version = "$mcApiVersion+")
}

tasks.wrapper {
    gradleVersion = "6.5"
    distributionType = Wrapper.DistributionType.ALL
}
