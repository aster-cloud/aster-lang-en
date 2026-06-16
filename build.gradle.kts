plugins {
    `java-library`
    `maven-publish`
}

group = "cloud.aster-lang"
version = "1.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "aster-lang-en"
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/aster-cloud/${rootProject.name}")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
}

dependencies {
    implementation("cloud.aster-lang:aster-lang-core:1.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.0")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

/**
 * verifyLexiconParity: 校验 aster-lang-en 中的 en-US.json 与 aster-lang-core
 * 内嵌副本（resources/builtin/en-US.json）逐字节一致。
 *
 * 背景：core 自带 en-US.json 作为永久 backbone（FallbackLexicon 需要）。
 * aster-lang-en 仍保留 lexicon 文件以向后兼容旧消费者，但**两份内容必须同步**。
 * CI 跑此 task：任一仓库改了 lexicon JSON 未同步另一份 → 立即 fail。
 *
 * 同步流程：改完一方后，cp <src> <dst>，commit 两份。
 */
tasks.register("verifyLexiconParity") {
    group = "verification"
    description = "Ensure aster-lang-en/en-US.json matches aster-lang-core/builtin/en-US.json byte-for-byte"

    val ours = file("src/main/resources/lexicons/en-US.json")
    val coreCopy = file("../aster-lang-core/src/main/resources/builtin/en-US.json")

    inputs.file(ours)

    doLast {
        if (!coreCopy.exists()) {
            logger.warn(
                "verifyLexiconParity: aster-lang-core builtin/en-US.json not found at ${coreCopy.absolutePath}. " +
                    "Skipping (sibling repo absent — likely non-monorepo CI). " +
                    "Run from a workspace where both repos are checked out."
            )
            return@doLast
        }
        val oursBytes = ours.readBytes()
        val coreBytes = coreCopy.readBytes()
        if (!oursBytes.contentEquals(coreBytes)) {
            throw GradleException(
                "en-US.json drift detected:\n" +
                    "  aster-lang-en:   ${ours.absolutePath} (${oursBytes.size} bytes)\n" +
                    "  aster-lang-core: ${coreCopy.absolutePath} (${coreBytes.size} bytes)\n" +
                    "Sync the two files (cp one to the other) and commit both before merging."
            )
        }
        logger.lifecycle("verifyLexiconParity: en-US.json matches between aster-lang-en and aster-lang-core ✓")
    }
}

// Wire verifyLexiconParity into the standard check chain so CI runs it.
tasks.named("check") {
    dependsOn("verifyLexiconParity")
}
