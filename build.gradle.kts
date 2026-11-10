// Top-level build file
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}

// Bu blok, GitHub'a "Gradle'ı indir ve çalıştır" der.
tasks.register<Wrapper>("wrapper") {
    gradleVersion = "8.2"
}
