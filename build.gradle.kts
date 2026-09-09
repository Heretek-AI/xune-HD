// Root build file. AGP 9+ provides built-in Kotlin support —
// the org.jetbrains.kotlin.android plugin must NOT be applied.
//
// Built-in Kotlin ships KGP 2.2.10; 2026-era androidx libraries pull
// kotlin-stdlib 2.4.x whose metadata the 2.2.0 compiler cannot read.
// Per the official guidance, force the newer KGP on the buildscript
// classpath (upgrades only) so the whole toolchain aligns on 2.4.20.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
