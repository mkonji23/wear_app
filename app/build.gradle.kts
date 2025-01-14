plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize") // 이 줄 추가
}

android {
    namespace = "com.example.home_project"
    compileSdk = 34
    // flavorDimensions 정의
//    flavorDimensions("tier")
//    productFlavors {
//        create("free") {
//            dimension = "tier"
//            applicationIdSuffix = ".free"
//        }
//        create("paid") {
//            dimension = "tier"
//            applicationIdSuffix = ".paid"
//        }
//    }
    defaultConfig {
        applicationId = "com.example.home_project"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "2.0"
        vectorDrawables {
            useSupportLibrary = true
        }

    }


    applicationVariants.all {
        this.outputs
            .map { it as com.android.build.gradle.internal.api.ApkVariantOutputImpl }
            .forEach { output ->
                println("flavorName=${this.flavorName} buildTypeName= ${this.buildType.name}")
                val variant = this.buildType.name
                var apkName =
                    "watch_app_" + this.versionName
                if (variant.isNotEmpty()) apkName += "_$variant"
                apkName += ".apk"
                println("ApkName=$apkName ${this.buildType.name}")

                output.outputFileName = apkName
            }
    }

    signingConfigs {
        create("release") {
            storeFile = file(project.properties["STORE_FILE"] as String) // Keystore 경로
            storePassword = project.properties["STORE_PASSWORD"] as String // Keystore 비밀번호
            keyAlias = project.properties["KEY_ALIAS"] as String // 키 별칭
            keyPassword = project.properties["KEY_PASSWORD"] as String // 키 비밀번호
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// 빌드 후 APK 파일 복사
tasks.register<Copy>("moveApk") {
    val buildType = "release" // "release"로 변경 가능
    val sourceApkDir = file("$buildDir/outputs/apk/$buildType/")
    val destinationDir = file("C:/apk/$buildType/")

    from(sourceApkDir)
    into(destinationDir)

    println("destinationDir:$destinationDir")
    // 디렉토리 생성 및 파일 복사 설정
    doFirst {
        destinationDir.mkdirs()
    }
}

// 빌드 후 moveApk Task를 실행하도록 설정
afterEvaluate {
    tasks.named("assembleRelease") {
        finalizedBy("moveApk")
    }
}

dependencies {

    implementation("com.google.android.gms:play-services-wearable:18.2.0")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.wear.compose:compose-material:1.1.2")
    implementation("androidx.wear.compose:compose-foundation:1.1.2")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.android.horologist:horologist-compose-tools:0.4.8")
    implementation("com.google.android.horologist:horologist-tiles:0.4.8")
    implementation("androidx.wear.watchface:watchface-complications-data-source-ktx:1.1.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.wear:wear:1.3.0")
    // Wear OS Data Layer API
    implementation("com.google.android.gms:play-services-wearable:18.0.0")
    // Optional: Google Play Services for common functionality
    implementation("com.google.android.gms:play-services-base:18.2.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.wear.tiles:tiles:1.1.0")
    implementation("androidx.wear.tiles:tiles-material:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.github.bumptech.glide:glide:4.15.1")
}