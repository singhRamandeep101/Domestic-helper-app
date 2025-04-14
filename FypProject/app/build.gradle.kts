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
}

chaquopy {
    productFlavors {
        getByName("py310") { version = "3.8" }

    }
}

chaquopy {
    defaultConfig {
        pip {
            install("pdfplumber==0.5.28")
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
    annotationProcessor(libs.glide.compiler)

    implementation("org.jitsi.react:jitsi-meet-sdk:10.3.0") {
        isTransitive = true
    }
}