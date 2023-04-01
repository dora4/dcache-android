plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    val compile_sdk = 30
    val min_sdk = 21
    val target_sdk = 30
    val release_version_name = "1.6.0"
    namespace = "dora.dcache"
    compileSdk = compile_sdk
    defaultConfig {
        minSdk = min_sdk
        targetSdk = target_sdk
        version = release_version_name
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
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
}
