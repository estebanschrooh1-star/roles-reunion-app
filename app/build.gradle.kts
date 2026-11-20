plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.estebangomez.asignaciones"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.estebangomez.asignaciones"
        minSdk = 26
        targetSdk = 34
        // La versión es "<VERSION>.<n.º de commits>". GitHub Actions la completa sola con -PappVersion;
        // en una compilación local se usa el número de VERSION seguido de .0
        val appVer = (project.findProperty("appVersion") as String?)
            ?: (File(rootDir, "VERSION").readText().trim() + ".0")
        versionName = appVer
        versionCode = appVer.split(".").let { it[0].toInt() * 1000000 + it[1].toInt() * 10000 + it[2].toInt() }
        // Repositorio donde se buscan las actualizaciones (GitHub Actions lo completa solo).
        val updateRepo = (project.findProperty("updateRepo") as String?) ?: ""
        buildConfigField("String", "UPDATE_REPO", "\"$updateRepo\"")
    }

    buildFeatures {
        buildConfig = true
    }

    // Clave fija: permite instalar cada versión nueva encima de la anterior sin perder datos.
    signingConfigs {
        getByName("debug") {
            storeFile = file("asignaciones.keystore")
            storePassword = "asignaciones"
            keyAlias = "asignaciones"
            keyPassword = "asignaciones"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.webkit:webkit:1.11.0")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
}
