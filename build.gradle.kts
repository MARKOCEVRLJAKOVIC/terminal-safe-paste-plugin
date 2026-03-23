plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.10.2"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
}

group = "dev.marko"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2025.3")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        bundledPlugin("org.jetbrains.plugins.terminal")
        bundledModule("intellij.terminal.frontend")

        composeUI()
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "252.25557"
        }

        changeNotes = """
            Initial version
        """.trimIndent()
    }

    pluginVerification {
        // List every IDE build you want the verifier to check against.
        // At minimum: the oldest supported build (sinceBuild) and the latest stable.
        ides {
            ide(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdeaCommunity, "2024.1")
            ide(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdeaCommunity, "2024.2")
            ide(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdeaCommunity, "2025.1")
            ide(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdeaUltimate, "2025.1")
        }
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")

        // Uncomment to publish to a non-default channel (e.g. "beta", "eap"):
        // channels = listOf(providers.environmentVariable("PLUGIN_CHANNEL").orElse("default"))
    }

    signing {
        // Optional: sign the plugin before publishing.
        // Requires SIGNING_KEY, SIGNING_KEY_ID, SIGNING_KEY_PASSPHRASE secrets.
        // See: https://plugins.jetbrains.com/docs/intellij/plugin-signing.html
        //
        // certificateChain = providers.environmentVariable("SIGNING_CERTIFICATE_CHAIN")
        // privateKey        = providers.environmentVariable("SIGNING_PRIVATE_KEY")
        // password          = providers.environmentVariable("SIGNING_KEY_PASSPHRASE")
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}