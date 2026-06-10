plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.dermacare.clinic"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dermacare.clinic"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }



    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.viewpager2)
    implementation(libs.cardview)
    implementation(libs.swiperefreshlayout)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
 
    // Retrofit & Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
 
    // Google Authentication
    implementation(libs.play.services.auth)


    // STOMP over WebSocket for Real-time Chat
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    // Stripe
    implementation("com.stripe:stripe-android:20.49.0")


    // PDF generation support
    implementation("com.itextpdf:kernel:7.2.5") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
    }
    implementation("com.itextpdf:layout:7.2.5") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
    }
    implementation("com.itextpdf:io:7.2.5") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
    }


}
