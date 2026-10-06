plugins {
    id("com.android.application")
}

val lanaVersionCode = providers.gradleProperty("lanaVersionCode")
    .orElse("1")
    .get()
    .toInt()
val lanaVersionName = providers.gradleProperty("lanaVersionName")
    .orElse("0.1.0-dev")
    .get()

val lanaSigningStoreFile = providers.gradleProperty("lanaSigningStoreFile").orNull
val lanaSigningStorePassword = providers.gradleProperty("lanaSigningStorePassword").orNull
val lanaSigningKeyAlias = providers.gradleProperty("lanaSigningKeyAlias").orNull
val lanaSigningKeyPassword = providers.gradleProperty("lanaSigningKeyPassword").orNull

android {
    namespace = "hr.vascharlie.lana3"
    compileSdk = 36

    defaultConfig {
        applicationId = "hr.vascharlie.lana3"
        minSdk = 26
        targetSdk = 36
        versionCode = lanaVersionCode
        versionName = lanaVersionName
    }

    if (
        lanaSigningStoreFile != null &&
        lanaSigningStorePassword != null &&
        lanaSigningKeyAlias != null &&
        lanaSigningKeyPassword != null
    ) {
        signingConfigs {
            create("lanaRelease") {
                storeFile = file(lanaSigningStoreFile)
                storePassword = lanaSigningStorePassword
                keyAlias = lanaSigningKeyAlias
                keyPassword = lanaSigningKeyPassword
            }
        }

        buildTypes {
            getByName("release") {
                signingConfig = signingConfigs.getByName("lanaRelease")
                isMinifyEnabled = false
            }
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.core:core-ktx:1.17.0")
}
