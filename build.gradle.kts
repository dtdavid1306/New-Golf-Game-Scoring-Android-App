// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.8.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}

subprojects {
    afterEvaluate {
        dependencies {
            add("implementation", "androidx.core:core-ktx:1.13.1")
            add("implementation", "androidx.appcompat:appcompat:1.6.1")
            add("implementation", "com.google.android.material:material:1.12.0")
            add("implementation", "androidx.recyclerview:recyclerview:1.3.2")
        }
    }
}