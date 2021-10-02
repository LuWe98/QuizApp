buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        val gradleVersion: String by project
        classpath("com.android.tools.build:gradle:$gradleVersion")

        val kotlinVersion: String by project
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

        val hiltVersion: String by project
        classpath("com.google.dagger:hilt-android-gradle-plugin:$hiltVersion")

        val navigationVersion: String by project
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$navigationVersion")

        classpath("io.realm:realm-gradle-plugin:10.8.0")
    }
}

tasks.register("clean", Delete::class){
    delete(rootProject.buildDir)
}