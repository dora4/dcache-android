plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
}

android {
    namespace = "dora.dcache"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
        targetSdk = 34
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    api("com.squareup.okhttp3:okhttp:4.11.0")
    api("com.squareup.retrofit2:converter-gson:2.8.1")
    api("com.squareup.retrofit2:retrofit:2.9.0")
    api("com.squareup.retrofit2:adapter-rxjava2:2.8.1")
    api("io.reactivex.rxjava2:rxjava:2.2.4")
    api("io.reactivex.rxjava2:rxandroid:2.1.0")

    api("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.20")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    api("com.tencent:mmkv:1.2.16")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:2.9.9")
}

afterEvaluate {
    publishing {
        publications {
            register("release", MavenPublication::class) {
                from(components["release"])
                groupId = "com.github.dora4"
                artifactId = "dcache-android"
                version = "2.2.11"
            }
        }
    }
}
