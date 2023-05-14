plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
}

android {
    namespace = "dora.dcache"
    compileSdk = 33
    defaultConfig {
        minSdk = 21
        targetSdk = 33
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
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")

    implementation("com.squareup.okhttp3:okhttp:4.8.0")
    implementation("com.squareup.retrofit2:converter-gson:2.8.1")
    implementation("com.squareup.retrofit2:retrofit:2.8.1")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.8.1")
    implementation("io.reactivex.rxjava2:rxjava:2.0.4")
    implementation("io.reactivex.rxjava2:rxandroid:2.0.1")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.5.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2")

    implementation("com.tencent:mmkv:1.2.16")
}

afterEvaluate {
    publishing {
        publications {
            register("release", MavenPublication::class) {
                from(components["release"])
                groupId = "com.github.dora4"
                artifactId = "dcache-android"
                version = "1.7.3"
            }
        }
    }
}
