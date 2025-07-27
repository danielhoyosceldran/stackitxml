plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // added by us
    // id("com.android.application") // da error, dice que ya está añadido
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.stackitxml"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.stackitxml"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // added by us
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))


    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.firebase:firebase-auth-ktx") // Per a l'autenticació
    implementation("com.google.firebase:firebase-firestore-ktx") // Per a Firestore

    // Kotlin Coroutines per a tasques de Firebase asíncrones (utilitzat al repositori amb .await())
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

    implementation("androidx.cardview:cardview:1.0.0")
}