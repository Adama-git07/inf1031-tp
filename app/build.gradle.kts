plugins {
    alias(libs.plugins.android.application)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.citoyenactif"
    compileSdk = 35

    buildFeatures {
        dataBinding = true
    }

    defaultConfig {
        applicationId = "com.example.citoyenactif"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                )
            }
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.multidex:multidex:2.0.1")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.room.runtime)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    annotationProcessor(libs.room.compiler)

    // ViewModel + LiveData (architecture MVVM)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.7")
    implementation("androidx.fragment:fragment:1.8.5")
    // Firebase BOM — gère toutes les versions automatiquement
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))
    // Firestore — base de données cloud
    implementation("com.google.firebase:firebase-firestore")
    // Firebase Authentication — nécessaire pour les règles de sécurité Firestore
    implementation("com.google.firebase:firebase-auth")
    // FCM — notifications push (bonus +5)
    implementation("com.google.firebase:firebase-messaging")
}
