// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // References versions from libs.versions.toml
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    // If you intend to use KSP later (e.g., for Moshi or Room), define and uncomment:
    // alias(libs.plugins.google.devtools.ksp) apply false
}