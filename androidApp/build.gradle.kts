plugins {
    id("com.android.application")
}

android {
    namespace = "hr.vascharlie.lana3"
    compileSdk = 36

    defaultConfig {
        applicationId = "hr.vascharlie.lana3"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0-dev"
    }
}

dependencies {
    implementation(project(":shared"))
}
