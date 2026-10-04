plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
