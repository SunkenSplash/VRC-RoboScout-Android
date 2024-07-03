import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
}

android {
    namespace = "com.sunkensplashstudios.VRCRoboScout"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sunkensplashstudios.VRCRoboScout"
        minSdk = 30
        targetSdk = 34
        versionCode = 9
        versionName = "1.1.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        //load the values from .properties file
        val keystoreFile = project.rootProject.file("config.properties")
        val properties = Properties()
        properties.load(keystoreFile.inputStream())

        //return empty key in case something goes wrong
        val reAPIKey = properties.getProperty("ROBOTEVENTS_API_KEY") ?: ""
        val key0 = properties.getProperty("key0") ?: ""
        val key1 = properties.getProperty("key1") ?: ""
        val key2 = properties.getProperty("key2") ?: ""
        val key3 = properties.getProperty("key3") ?: ""
        val key4 = properties.getProperty("key4") ?: ""
        val key5 = properties.getProperty("key5") ?: ""
        val key6 = properties.getProperty("key6") ?: ""
        val key7 = properties.getProperty("key7") ?: ""
        val key8 = properties.getProperty("key8") ?: ""
        val key9 = properties.getProperty("key9") ?: ""

        // Default season IDs
        buildConfigField(
            type = "int",
            name = "DEFAULT_V5_SEASON_ID",
            value = "190"
        )
        buildConfigField(
            type = "int",
            name = "DEFAULT_VU_SEASON_ID",
            value = "191"
        )

        // API keys
        buildConfigField(
            type = "String",
            name = "ROBOTEVENTS_API_KEY",
            value = reAPIKey
        )
        buildConfigField(
            type = "String",
            name = "key0",
            value = key0
        )
        buildConfigField(
            type = "String",
            name = "key1",
            value = key1
        )
        buildConfigField(
            type = "String",
            name = "key2",
            value = key2
        )
        buildConfigField(
            type = "String",
            name = "key3",
            value = key3
        )
        buildConfigField(
            type = "String",
            name = "key4",
            value = key4
        )
        buildConfigField(
            type = "String",
            name = "key5",
            value = key5
        )
        buildConfigField(
            type = "String",
            name = "key6",
            value = key6
        )
        buildConfigField(
            type = "String",
            name = "key7",
            value = key7
        )
        buildConfigField(
            type = "String",
            name = "key8",
            value = key8
        )
        buildConfigField(
            type = "String",
            name = "key9",
            value = key9
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val ktorVersion: String by project

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material:material-icons-extended")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.02"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("io.github.raamcosta.compose-destinations:animations-core:1.10.0")
    ksp("io.github.raamcosta.compose-destinations:ksp:1.10.0")

    // license for the cascade library:

    /*
    Copyright 2020 Saket Narayan.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    */

    implementation("me.saket.cascade:cascade:2.3.0")
    implementation("me.saket.cascade:cascade-compose:2.3.0")
    implementation("io.mhssn:colorpicker:1.0.0")
    implementation("org.ejml:ejml-simple:0.38")
}
