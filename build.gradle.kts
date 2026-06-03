// Project-level build.gradle.kts

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.google.services) apply false
}

task("clean", type = Delete::class) {
    delete(layout.buildDirectory)
}
