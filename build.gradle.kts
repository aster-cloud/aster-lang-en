plugins {
    `java-library`
    `maven-publish`
}

group = "cloud.aster-lang"
version = "0.0.1"

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
}

dependencies {
    implementation("cloud.aster-lang:aster-lang-core:0.0.1")
}
