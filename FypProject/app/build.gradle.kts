plugins {

    alias(libs.plugins.google.gms.google.services)
    id("com.android.application")
    id("com.chaquo.python")
}

android {
    namespace = "com.project.fypproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.project.fypproject"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    flavorDimensions += "pyVersion"
    productFlavors {
        create("py310") { dimension = "pyVersion" }

    }

    productFlavors {
        all {
            if (name == "py310") {
                version = "3.10"
            }
        }
    }
}


dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebaseui)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.rounded.image.view)
    implementation(libs.glide)
    implementation (libs.pdfbox.android)
    implementation(libs.libausbc)
    implementation(libs.cardview)
    implementation(libs.core)
    annotationProcessor(libs.glide.compiler)

    implementation("org.jitsi.react:jitsi-meet-sdk:11.1.0") {
        isTransitive = true
    }
}