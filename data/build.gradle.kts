import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    // Hilt uses kapt
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
    // Room uses KSP
    id("com.google.devtools.ksp")
    id("androidx.room")
}

android {
    namespace = "com.devusercode.data"

    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 28
    }

    buildFeatures { buildConfig = false }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(project(":core"))

    // Hilt (kapt)
    val hiltVersion = "2.57.2" // or the version you are using consistently
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")

    // Room (KSP)
    val roomVersion = "2.8.3"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // DataStore, Firebase, etc.
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
}
